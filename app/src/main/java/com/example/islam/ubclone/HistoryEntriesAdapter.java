package com.example.islam.ubclone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by islam on 10/27/16.
 */
public class HistoryEntriesAdapter extends RecyclerView.Adapter<HistoryEntriesAdapter.ViewHolder> {
    private static final String TAG = "HistoryEntriesAdapter";
    private Context context;
    private ArrayList<HistoryEntry> entriesList;

    public HistoryEntriesAdapter(Context context, ArrayList<HistoryEntry> entriesList) {
        this.context = context;
        this.entriesList = entriesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_entry_layout, parent, false);

        // set the view's size, margins, padding and layout parameters
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final HistoryEntry historyEntry = entriesList.get(position);
        holder.pickupPoint.setText(historyEntry.getPickupPoint());
        holder.destinationPoint.setText(historyEntry.getDestinationPoint());
        holder.time.setText(historyEntry.getTime());
        holder.price.setText(historyEntry.getPrice());
        holder.status.setText(historyEntry.getStatus());
//        holder.driverName.setText(historyEntry.getDriverName());
//        holder.driverVehicle.setText(historyEntry.getDriverVehicle());

    }

    @Override
    public int getItemCount() {
        return entriesList.size();
    }

    public void updateDataSet(ArrayList<HistoryEntry> historyEntries) {

        this.entriesList = historyEntries;
    }

    public void clearDataSet() {
        Log.i(TAG, "clearEvents: Cleared!");
        int listSize = entriesList.size();
        if (listSize > 0) {
            for (int i = 0; i < listSize; i++) {
                entriesList.remove(0);
            }

            this.notifyItemRangeRemoved(0, listSize);
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView pickupPoint;
    public TextView destinationPoint;
    public TextView time;
    public TextView price;
    public TextView status;

    public ViewHolder(View v) {
        super(v);

        // View can be event, past_event, or a separator.
        // In the separator case, the following variables will be null.
        pickupPoint = (TextView) v.findViewById(R.id.entry_from);
        destinationPoint = (TextView) v.findViewById(R.id.entry_to);
        time = (TextView) v.findViewById(R.id.entry_date);
        price = (TextView) v.findViewById(R.id.entry_price);
        status = (TextView) v.findViewById(R.id.entry_status);
//        driverName = (TextView) v.findViewById(R.id.event_date);
//        driverVehicle = (TextView) v.findViewById(R.id.event_date);

    }

}

}
