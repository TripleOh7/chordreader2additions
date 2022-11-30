package org.hollowbamboo.chordreader2.ui;

/*
Chord Reader 2 - fetch and display chords for your favorite songs from the Internet
Copyright (C) 2021 AndInTheClouds

This program is free software: you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation, either
version 3 of the License, or any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.
If not, see <https://www.gnu.org/licenses/>.

*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.hollowbamboo.chordreader2.R;
import org.hollowbamboo.chordreader2.adapter.RecyclerViewAdapter;
import org.hollowbamboo.chordreader2.databinding.FragmentDraggableListBinding;
import org.hollowbamboo.chordreader2.helper.SaveFileHelper;
import org.hollowbamboo.chordreader2.interfaces.OnItemClickListener;
import org.hollowbamboo.chordreader2.interfaces.StartDragListener;
import org.hollowbamboo.chordreader2.model.DataViewModel;
import org.hollowbamboo.chordreader2.util.ItemMoveCallback;

import java.util.ArrayList;
import java.util.Objects;

public class DraggableListFragment extends Fragment implements OnItemClickListener, StartDragListener {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;

    private ItemTouchHelper touchHelper;

    private FragmentDraggableListBinding binding;
    private DataViewModel dataViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        dataViewModel =
                new ViewModelProvider(requireActivity()).get(DataViewModel.class);

        binding = FragmentDraggableListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;

        setTitle(dataViewModel.getSetListMLD().getValue().replace(".pl",""));

        dataViewModel.getSetListSongsMLD().observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(@Nullable ArrayList<String> setListSongs) {
                setupRecyclerView();
            }
        });

        setupRecyclerView();

        setUpMenu();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        saveSetListToFile();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (dataViewModel.setListSongs.isEmpty())
            startListView();
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemClick(String filename) {
        startSongView(filename);
    }

    private void setupRecyclerView() {

        mAdapter = new RecyclerViewAdapter(dataViewModel.setListSongs, this, this);

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(mAdapter);
        touchHelper  = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);
    }

    private void setTitle(String titleText) {
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(titleText);
    }

    private void setUpMenu() {
        MenuHost menuHost = requireActivity();
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.list_view_menu, menu);
                menu.findItem(R.id.menu_new_file).setVisible(true);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if(itemId == R.id.menu_new_file) {
                    startListView();
                    return true;
                }

                return false;
            }
        };

        menuHost.addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void startSongView(String filename) {
        DraggableListFragmentDirections.ActionNavDragListViewToNavSongView action =
                DraggableListFragmentDirections.actionNavDragListViewToNavSongView(
                        filename.replace(".txt", ""),filename,null);

        Navigation.findNavController(getParentFragment().getView()).navigate(action);
    }

    private void startListView() {
        DraggableListFragmentDirections.ActionNavDragListViewToNavListView action =
                DraggableListFragmentDirections.actionNavDragListViewToNavListView("SetlistSongsSelection");
        View view = getParentFragment().getView();
        Navigation.findNavController(view).navigate(action);
    }

    private void saveSetListToFile() {
        String fileName = dataViewModel.getSetListMLD().getValue();
        if(!fileName.endsWith(".pl"))
            fileName = fileName + ".pl";

        StringBuilder resultText = new StringBuilder();
        for (String line : Objects.requireNonNull(dataViewModel.getSetListSongsMLD().getValue())) {
            resultText.append(line).append(".txt\n");
        }

        SaveFileHelper.saveFile(resultText.toString(),fileName);
    }
}