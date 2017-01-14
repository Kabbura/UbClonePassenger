package com.wisam.driver.ubclone;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by islam on 10/19/16.
 */
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder>  {
    private ArrayList<MapPlace> placesList;
    private Activity activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlacesAdapter(Activity mActivity, ArrayList<MapPlace> mPlacesList) {
        activity = mActivity;
        placesList = mPlacesList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final MapPlace place = placesList.get(position);
        holder.placeName.setText(place.getName());
        holder.PlaceVicinity.setText(place.getVicinity());

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", place.getLat());
                resultIntent.putExtra("ltd", place.getLng());
                resultIntent.putExtra("name", holder.placeName.getText().toString());
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();

            }
        });



    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;
        public TextView PlaceVicinity;
        public ImageView placeIcon;
        public LinearLayout rootLayout;

        public ViewHolder(View v) {
            super(v);
            placeName = (TextView) v.findViewById(R.id.places_name);
            PlaceVicinity = (TextView) v.findViewById(R.id.places_vicinity);
            placeIcon = (ImageView) v.findViewById(R.id.places_image);
            rootLayout = (LinearLayout) v.findViewById(R.id.place_layout_root);
        }

    }


    // Provide a data updater function
    public void updateDataSet(ArrayList<MapPlace> inPlacesList) {
        placesList = inPlacesList;
    }

    public void clearPlaces(){
        int listSize = placesList.size();
        if (listSize > 0) {
            for (int i = 0; i < listSize; i++) {
                placesList.remove(0);
            }

            this.notifyItemRangeRemoved(0, listSize);
        }

    }

}
