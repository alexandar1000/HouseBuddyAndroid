package allurosi.housebuddy.householdmanager;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.logging.LogEntry;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.todolist.ToDoListActivity.COLLECTION_PATH_CHANGE_LOG;

public class HouseholdFeedActivity extends AppCompatActivity {

    private static final String LOG_NAME = "HouseHoldFeedActivity";
    public static final String FIELD_TIME_STAMP = "timeStamp";

    private List<LogEntry> mLogEntries = new ArrayList<>();
    private HouseholdFeedAdapter mListAdapter;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private ListenerRegistration mListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household_feed);

        RecyclerView houseHoldFeed = findViewById(R.id.household_feed);

        mListAdapter = new HouseholdFeedAdapter(mLogEntries);
        houseHoldFeed.setAdapter(mListAdapter);
        houseHoldFeed.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get stored household path
        String householdPath = PreferenceManager.getDefaultSharedPreferences(this).getString(HOUSEHOLD_PATH, "");
        Query changeLogQuery = mFireStore.document(householdPath).collection(COLLECTION_PATH_CHANGE_LOG).orderBy(FIELD_TIME_STAMP, Query.Direction.DESCENDING);

        // Add listener which updates the change log list if another user changes it
        // Is called once when addSnapshotListener is called
        mListenerRegistration = changeLogQuery.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_NAME, "Listen error: ", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<LogEntry> newLogEntryList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        LogEntry logEntry = document.toObject(LogEntry.class);
                        newLogEntryList.add(logEntry);
                    }
                    mLogEntries.clear();
                    mLogEntries.addAll(newLogEntryList);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Detach listener when it's no longer needed
        mListenerRegistration.remove();
    }

}
