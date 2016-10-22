package com.example.islam.ubclone;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlacesFragment extends Fragment {

    private RecyclerView placesRecyclerView;
    private PlacesAdapter placesAdapter;
    private RecyclerView.LayoutManager placesLayoutManager;
    ArrayList<MapPlace> PlacesList;

    public PlacesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new WebDownloaderTask(this, WebDownloaderTask.NEARBY).execute(new String[] { "http://www.test.com/index.html" });
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        placesRecyclerView = (RecyclerView) getActivity().findViewById(R.id.places_rec_view);
        PlacesList = new ArrayList<MapPlace>();
        PlacesList.add(new MapPlace(23.32323, 31.3233, "Khartoum", "Such a nice place"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "Stadium", "Let's play"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));
        PlacesList.add(new MapPlace(23.32323, 31.3233, "The ATM next door", "Wanna get some money?"));

        placesRecyclerView.setHasFixedSize(true);

        // Use linear layout manager
        placesLayoutManager = new LinearLayoutManager(getContext());
        placesRecyclerView.setLayoutManager(placesLayoutManager);

        // specify an adapter (See also next example)
        placesAdapter = new PlacesAdapter(getActivity(), PlacesList);
        placesRecyclerView.setAdapter(placesAdapter);
    }
}
