package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.logging.LogEntry;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_FIRST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_LAST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.KEY_HOUSEHOLD;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.USERS_COLLECTION_PATH;
import static allurosi.housebuddy.todolist.ToDoListFragment.COLLECTION_PATH_CHANGE_LOG;

public class HouseholdHomeFragment extends Fragment {

    private static final String LOG_NAME = "HouseHoldFeedActivity";
    public static final String FIELD_TIME_STAMP = "timeStamp";
    public static final String FIELD_NAME = "name";
    public static final String HOUSEHOLD_NAME = "household_name";

    private Context mContext;

    private List<LogEntry> mLogEntries = new ArrayList<>();
    private List<String> mMemberNames = new ArrayList<>();

    private TextView mHouseholdNameTextView;

    private HouseholdMembersAdapter mMemberListAdapter;
    private HouseholdFeedAdapter mFeedListAdapter;

    private String mHouseholdPath;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private ListenerRegistration mChangeLogListenerRegistration;
    private ListenerRegistration mMembersListenerRegistration;

    private SharedPreferences mSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Get stored house hold path
        mHouseholdPath = PreferenceManager.getDefaultSharedPreferences(mContext).getString(HOUSEHOLD_PATH, "");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mHouseholdNameTextView = rootView.findViewById(R.id.household_name);
        RecyclerView householdMembers = rootView.findViewById(R.id.household_members);
        RecyclerView householdFeed = rootView.findViewById(R.id.household_feed);

        String householdName = mSharedPreferences.getString(HOUSEHOLD_NAME, "");
        if (householdName.isEmpty()) {
            fetchHouseholdName();
        } else {
            mHouseholdNameTextView.setText(householdName);
        }

        mMemberListAdapter = new HouseholdMembersAdapter(mContext, mMemberNames);
        householdMembers.setAdapter(mMemberListAdapter);
        householdMembers.setLayoutManager(new LinearLayoutManager(mContext));

        mFeedListAdapter = new HouseholdFeedAdapter(mLogEntries);
        householdFeed.setAdapter(mFeedListAdapter);
        householdFeed.setLayoutManager(new LinearLayoutManager(mContext));

        return rootView;
    }

    private void fetchHouseholdName() {
        mFireStore.document(mHouseholdPath).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String householdName = documentSnapshot.getString(FIELD_NAME);

                // Store the household name
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(HOUSEHOLD_NAME, householdName);
                editor.apply();

                mHouseholdNameTextView.setText(householdName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to retrieve household: " + e);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        final DocumentReference householdRef = mFireStore.document(mHouseholdPath);
        mMembersListenerRegistration = mFireStore.collection(USERS_COLLECTION_PATH).whereEqualTo(KEY_HOUSEHOLD, householdRef).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_NAME, "Listen error: ", e);
                    return;
                }

                mMemberNames.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String firstName = documentSnapshot.getString(FIELD_FIRST_NAME);
                    String lastName = documentSnapshot.getString(FIELD_LAST_NAME);

                    mMemberNames.add(0, firstName + " " + lastName);
                    mMemberListAdapter.notifyItemInserted(0);
                }
            }
        });

        // Order logEntries by the most recent first
        Query changeLogQuery = mFireStore.document(mHouseholdPath).collection(COLLECTION_PATH_CHANGE_LOG).orderBy(FIELD_TIME_STAMP, Query.Direction.DESCENDING);

        // Add listener which updates the change log list if another user changes it
        // Is called once when addSnapshotListener is called
        mChangeLogListenerRegistration = changeLogQuery.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_NAME, "Listen error: ", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<LogEntry> newLogEntryList = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            LogEntry logEntry = document.toObject(LogEntry.class);
                            newLogEntryList.add(logEntry);
                        }
                        mLogEntries.clear();
                        mLogEntries.addAll(newLogEntryList);
                        mFeedListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Detach listeners when they are no longer needed
        mMembersListenerRegistration.remove();
        mChangeLogListenerRegistration.remove();
    }

    // Deprecated method to support lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

}
