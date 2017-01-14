package com.wisam.driver.ubclone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisam.driver.concepts.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by islam on 12/23/16.
 */
public class OngoingRequestAdapter extends RecyclerView.Adapter <OngoingRequestAdapter.ViewHolder> {
    private static final String TAG = "UbDriver";
    private static final int SELECTED_REQUEST_CODE = 43542;
    private List<Ride.RideDetails> entriesList;
    private Context context;
    private PrefManager prefManager;

    public OngoingRequestAdapter(List<Ride.RideDetails> RequestList, Context context) {
        this.entriesList = RequestList;
        this.context = context;
    }


    @Override
    public int getItemCount() {
        return entriesList.size();
    }

    public boolean addRequest(Ride.RideDetails request){
        entriesList.add(getItemCount(), request);
        return true;
    }

    @Override
    public void onBindViewHolder(final ViewHolder RequestViewHolder, int i) {
        final Ride.RideDetails entry = entriesList.get(i);
//                            entry.setTime(String.valueOf(DateUtils.getRelativeTimeSpanString(unixTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));
//                            entry.setPrice(entry.getPrice() + " " + getString(R.string.currency));

        prefManager = new PrefManager(context);

        String time = entry.getTime();
        if (!time.equals("now")){
            Long unixTime = Long.valueOf(entry.getTime());
            String timeString = String.valueOf(DateUtils.getRelativeTimeSpanString(unixTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            RequestViewHolder.date.setText(timeString);
        } else {
            RequestViewHolder.date.setText(time);
        }


        String priceString = entry.price + context.getString(R.string.currency);
        RequestViewHolder.price.setText(priceString);
        RequestViewHolder.price.setTextColor(context.getResources().getColor(R.color.colorPrimary));

//        RequestViewHolder.date.setText(entry.getTime());
//        RequestViewHolder.status.setBackgroundColor(context.getResources().getColor(R.color.white));
        RequestViewHolder.status.setVisibility(View.GONE);
        RequestViewHolder.dest.setText(entry.destText);




        RequestViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectedOngoingRequestActivity.class);
                intent.putExtra("id", entry.requestID);
                ((Activity) context).startActivityForResult(intent,SELECTED_REQUEST_CODE);

            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.history_entry_layout, viewGroup, false);

        return new ViewHolder(itemView);
    }

    public void updateDataSet(ArrayList<Ride.RideDetails> historyEntries) {

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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        protected TextView price;
        protected TextView date;
        protected View status;
        protected TextView dest;
        protected LinearLayout linearLayout;


        public ViewHolder(View v){
            super(v);
            date = (TextView) v.findViewById(R.id.entry_date);
            dest = (TextView) v.findViewById(R.id.entry_to);
            price = (TextView) v.findViewById(R.id.entry_price);
            status = v.findViewById(R.id.entry_status);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.history_card_view);

        }
    }

}
