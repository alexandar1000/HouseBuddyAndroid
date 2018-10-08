package allurosi.housebuddy.householdmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class HouseholdManagerActivity extends AppCompatActivity implements NewUserDialogFragment.NewUserDialogListener {

    public static final String LOG_NAME = "HouseHoldManager";
    public static final String KEY_HOUSEHOLD = "household";
    public static final String HOUSEHOLD_PATH = "householdPath";
    public static final String USERS_COLLECTION_PATH = "users";

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID);
        mUserEmail = intent.getStringExtra(USER_EMAIL);

        if (mUserId != null) {
            setContentView(R.layout.household_manager_loading);
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
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(this);
        String householdPath = pref.getString(HOUSEHOLD_PATH, "");
        return !householdPath.equals("");
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
                        String householdPath = householdRef.getPath();

                        // Save the path to the user's household
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(HouseholdManagerActivity.this).edit();
                        editor.putString(HOUSEHOLD_PATH, householdPath);
                        editor.apply();

                        setContentView(R.layout.household_manager);
                        // TODO: add invite code function
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

        // Add DialogFragment with transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.household_manager_root_view, newUserDialogFragment).addToBackStack(null).commit();
    }

    @Override
    public void onAddNewUser(String firstName, String lastName) {
        // Add user information to database, document id is the user auth id
        Map<String, Object> user = new HashMap<>();
        user.put("email", mUserEmail);
        user.put("first_name", firstName);
        user.put("last_name", lastName);

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

    public void signOut(View view) {
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
        transaction.add(android.R.id.content, new InviteUserDialog()).addToBackStack(null).commit();
    }

    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

    public void buttonExpensesList(View view) {
        Intent intent = new Intent(this, ExpensesActivity.class);
        startActivity(intent);
    }

}
