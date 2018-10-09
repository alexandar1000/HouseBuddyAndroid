package allurosi.housebuddy.householdmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import allurosi.housebuddy.R;
import allurosi.housebuddy.authentication.LogInActivity;
import allurosi.housebuddy.expensetracker.ExpensesActivity;
import allurosi.housebuddy.todolist.ToDoListActivity;

import static allurosi.housebuddy.authentication.LogInActivity.USER_EMAIL;
import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;

public class HouseholdManagerActivity extends AppCompatActivity
        implements NewUserDialogFragment.NewUserDialogListener, JoinHouseholdDialogFragment.JoinHouseholdDialogListener {

    public static final String LOG_NAME = "HouseHoldManager";
    public static final String KEY_HOUSEHOLD = "household";
    public static final String HOUSEHOLD_PATH = "householdPath";
    public static final String USERS_COLLECTION_PATH = "users";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mUserEmail;

    private SharedPreferences mSharedPreferences;
    private FragmentManager mFragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_manager_loading);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID);
        mUserEmail = intent.getStringExtra(USER_EMAIL);

        if (mUserId != null) {
            fetchUserData();
        } else {
            if (userHasHousehold()) {
                setContentView(R.layout.household_manager);
            } else {
                setContentView(R.layout.no_household_layout);
            }
        }
    }

    private boolean userHasHousehold() {
        String householdPath = mSharedPreferences.getString(HOUSEHOLD_PATH, "");
        return !householdPath.equals("");
    }

    private void saveHousehold(String householdPath) {
        // Save the path to the user's household
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(HOUSEHOLD_PATH, householdPath);
        editor.apply();
    }

    private void fetchUserData() {
        // Get document with current userId from the users collection
        DocumentReference userDocRef = mFireStore.document("users/" + mUserId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Check if user is already registered in the user database
                if (documentSnapshot.exists()) {
                    // Check if user has a household
                    if (documentSnapshot.contains(KEY_HOUSEHOLD)) {
                        DocumentReference householdRef = documentSnapshot.getDocumentReference(KEY_HOUSEHOLD);
                        saveHousehold(householdRef.getPath());

                        setContentView(R.layout.household_manager);
                    } else {
                        // User is an existing user without a household
                        // TODO: create and join household functionality
                        setContentView(R.layout.no_household_layout);
                    }
                } else {
                    // User is a new user
                    setContentView(R.layout.no_household_layout);
                    newUserDialog();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, e);
            }
        });
    }

    private void newUserDialog() {
        NewUserDialogFragment newUserDialogFragment = new NewUserDialogFragment();
        newUserDialogFragment.setListener(this);

        // Add new user DialogFragment with transaction
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newUserDialogFragment).addToBackStack(null).commit();
    }

    @Override
    public void onAddNewUser(String firstName, String lastName) {
        // Add user information to database, document id is the user auth id
        Map<String, Object> user = new HashMap<>();
        user.put(FIELD_EMAIL, mUserEmail);
        user.put(FIELD_FIRST_NAME, firstName);
        user.put(FIELD_LAST_NAME, lastName);

        CollectionReference usersRef = mFireStore.collection(USERS_COLLECTION_PATH);
        usersRef.document(mUserId).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HouseholdManagerActivity.this, getResources().getString(R.string.account_updated), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add user info: " + e);
                Toast.makeText(HouseholdManagerActivity.this, getResources().getString(R.string.account_updated_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void startJoinHousehold() {
        // Called when joining is initiated
        setContentView(R.layout.household_manager_loading);
    }

    @Override
    public void onJoinHousehold(String householdPath) {
        // Save household and go to normal household layout
        saveHousehold(householdPath);
        setContentView(R.layout.household_manager);
    }

    @Override
    public void onJoinFailure() {
        Toast.makeText(HouseholdManagerActivity.this, "Failed to join household.", Toast.LENGTH_LONG).show();
        setContentView(R.layout.no_household_layout);
    }

    public void buttonJoinHousehold(View view) {
        JoinHouseholdDialogFragment joinHouseholdDialogFragment = new JoinHouseholdDialogFragment();
        joinHouseholdDialogFragment.setListener(this);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, joinHouseholdDialogFragment).addToBackStack(null).commit();
    }

    public void buttonSignOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HouseholdManagerActivity.this, LogInActivity.class);
                        startActivity(intent);
                    }
                });
    }

    public void buttonInvite(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, new InviteUserDialogFragment()).addToBackStack(null).commit();
    }

    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

    public void buttonExpensesList(View view) {
        Intent intent = new Intent(this, ExpensesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

}
