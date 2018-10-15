package allurosi.housebuddy.householdmanager;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household_feed);

        RecyclerView houseHoldFeed = findViewById(R.id.household_feed);

        fetchLogEntries();
        mListAdapter = new HouseholdFeedAdapter(mLogEntries);
        houseHoldFeed.setAdapter(mListAdapter);
        houseHoldFeed.setLayoutManager(new LinearLayoutManager(this));
    }


    private void fetchLogEntries() {
        // Get stored household path
        String householdPath = PreferenceManager.getDefaultSharedPreferences(this).getString(HOUSEHOLD_PATH, "");
        DocumentReference householdRef = mFireStore.document(householdPath);

        // Order data to show latest changes first
        householdRef.collection(COLLECTION_PATH_CHANGE_LOG).orderBy(FIELD_TIME_STAMP, Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    LogEntry logEntry = document.toObject(LogEntry.class);

                    if (!mLogEntries.contains(logEntry)) {
                        mLogEntries.add(0, logEntry);
                        mListAdapter.notifyItemChanged(0);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to retrieve log entries: " + e);
            }
        });
    }

}
