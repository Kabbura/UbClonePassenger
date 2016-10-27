package com.example.islam.ubclone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView historyEntriesRecyclerView;
    private HistoryEntriesAdapter historyEntriesAdapter;
    private RecyclerView.LayoutManager historyEntriesLayoutManager;
    private ArrayList<HistoryEntry> historyEntriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history); 
        
        historyEntriesRecyclerView = (RecyclerView) findViewById(R.id.history_rec_view);
        ArrayList<HistoryEntry> historyEntries = new ArrayList<HistoryEntry>();
        historyEntries.add(new HistoryEntry("Khartoum 2, Khartoum, Sudan",
                                             "","","3242",
                                            "Omdurman, Khartoum, Sudan","48","Completed","24/10/16-16:02"
                                            ));
        historyEntries.add(new HistoryEntry("Alsalha, Omdurman, Sudan",
                                             "","","3242",
                                            "Shambat, Khartoum North, Sudan","70","Completed","22/10/16-5:22"
                                            ));

        historyEntries.add(new HistoryEntry("Burri, Khartoum, Sudan",
                "","","3242",
                "Elthawra, Omdurman, Sudan","82","Canceled","22/10/16-5:22"
        ));
        historyEntries.add(new HistoryEntry("Burri, Khartoum, Sudan",
                "","","3242",
                "Elthawra, Omdurman, Sudan","90","Canceled","22/10/16-5:22"
        ));

        historyEntries.add(new HistoryEntry("Burri, Khartoum, Sudan",
                "","","3242",
                "Elthawra, Omdurman, Sudan","72","Canceled","22/10/16-5:22"
        ));

        historyEntries.add(new HistoryEntry("Burri, Khartoum, Sudan",
                "","","3242",
                "Elthawra, Omdurman, Sudan","54","Canceled","22/10/16-5:22"
        ));



        historyEntriesRecyclerView.setHasFixedSize(true);

        // Use linear layout manager
        historyEntriesLayoutManager = new LinearLayoutManager(this);
        historyEntriesRecyclerView.setLayoutManager(historyEntriesLayoutManager);

        // specify an adapter (See also next example)
        historyEntriesAdapter = new HistoryEntriesAdapter(this, historyEntries);
        historyEntriesRecyclerView.setAdapter(historyEntriesAdapter);
    }
}
