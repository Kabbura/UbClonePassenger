package com.example.islam.ubclone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by islam on 10/27/16.
 */
public class HistoryEntriesAdapter extends RecyclerView.Adapter<HistoryEntriesAdapter.ViewHolder> {
    private static final String TAG = "HistoryEntriesAdapter";
    private static final int FROM_HISTORY_CODE = 2;
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
        holder.price.setText(historyEntry.getPrice());
        holder.date.setText(historyEntry.getTime());
//        holder.status.setText(historyEntry.getDisplayStatus(historyEntry.getStatus(), context));
        switch (historyEntry.getStatus()){
            case "completed":
                holder.status.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                break;
            case "canceled":
                holder.status.setBackgroundColor(context.getResources().getColor(R.color.colorRed));
                break;
            case "noDriver":
//                holder.status.setBackground(context.getResources().getColor(R.color.colorAccent));
                holder.status.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                break;

        }
        holder.destination.setText(historyEntry.getDestText().replaceAll("\n", " "));

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectedRequestActivity.class);
                intent.putExtra("passenger_name", historyEntry.getDriverName());
                intent.putExtra("status", historyEntry.getStatus());
                intent.putExtra("time", historyEntry.getTime());
                intent.putExtra("price", historyEntry.getPrice());
                intent.putExtra("request_id", historyEntry.getId());
                intent.putExtra("pickup_text", historyEntry.getPickupText());
                intent.putExtra("dest_text", historyEntry.getDestText());

                ((Activity) context).startActivityForResult(intent, FROM_HISTORY_CODE);
            }
        });

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
        public TextView destination;
        public TextView date;
        public TextView price;
        public View status;
        protected LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);

            // View can be event, past_event, or a separator.
            // In the separator case, the following variables will be null.
            destination = (TextView) v.findViewById(R.id.entry_to);
            date = (TextView) v.findViewById(R.id.entry_date);
            price = (TextView) v.findViewById(R.id.entry_price);
            status =  v.findViewById(R.id.entry_status);
//            dest = (TextView) v.findViewById(R.id.entry_to);
            price = (TextView) v.findViewById(R.id.entry_price);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.history_card_view);

        }

    }

}
