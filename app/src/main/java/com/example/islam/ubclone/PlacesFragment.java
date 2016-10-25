package com.example.islam.ubclone;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.Map;


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

        placesRecyclerView.setHasFixedSize(true);

        // Use linear layout manager
        placesLayoutManager = new LinearLayoutManager(getContext());
        placesRecyclerView.setLayoutManager(placesLayoutManager);

        // specify an adapter (See also next example)
        placesAdapter = new PlacesAdapter(getActivity(), PlacesList);
        placesRecyclerView.setAdapter(placesAdapter);
    }


    public void setPlaces(ArrayList<MapPlace> latestEvents) {
        Log.i("UbClone", "setPlaces: Set");
        placesAdapter.updateDataSet(latestEvents);
        placesAdapter.notifyDataSetChanged();

    }

    public void clearPlaces(){
        Log.i("UbClone", "setPlaces: Cleared first called");
        placesAdapter.clearPlaces();
    }
}
