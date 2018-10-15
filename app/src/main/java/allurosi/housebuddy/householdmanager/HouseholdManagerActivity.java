package allurosi.housebuddy.householdmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import allurosi.housebuddy.R;
import allurosi.housebuddy.authentication.LogInActivity;
import allurosi.housebuddy.expensetracker.ExpensesActivity;
import allurosi.housebuddy.todolist.ToDoListActivity;

import static allurosi.housebuddy.authentication.LogInActivity.USER_EMAIL;
import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;
import static allurosi.housebuddy.householdmanager.InviteUserDialogFragment.KEY_INVITES;
import static allurosi.housebuddy.householdmanager.InviteUserDialogFragment.KEY_INVITE_CODE;
import static allurosi.housebuddy.householdmanager.JoinHouseholdDialogFragment.KEY_MEMBERS;

public class HouseholdManagerActivity extends AppCompatActivity implements NewUserDialogFragment.NewUserDialogListener, AddHouseholdListener {

    public static final String LOG_NAME = "HouseHoldManager";
    public static final String KEY_HOUSEHOLD = "household";
    public static final String HOUSEHOLD_PATH = "householdPath";
    public static final String USERS_COLLECTION_PATH = "users";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";

    // All collections that get removed when a household is removed
    private static final String[] HOUSEHOLD_COLLECTION_PATHS = {"to_do_list", "expenses_list"};

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private DocumentReference mHouseholdRef;
    private DocumentReference mUserRef;

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
                // Get stored user id
                mUserId = mSharedPreferences.getString(USER_ID, "");
                mUserRef = mFireStore.collection(USERS_COLLECTION_PATH).document(mUserId);

                if (mHouseholdRef == null) {
                    // Get stored household path
                    String householdPath = mSharedPreferences.getString(HOUSEHOLD_PATH, "");
                    mHouseholdRef = mFireStore.document(householdPath);
                }

                setContentView(R.layout.household_manager);
            } else {
                setContentView(R.layout.no_household_layout);
            }
        }
    }

    private boolean userHasHousehold() {
        String householdPath = mSharedPreferences.getString(HOUSEHOLD_PATH, "");
        return !householdPath.isEmpty();
    }

    private void storeHousehold(String householdPath) {
        mHouseholdRef = mFireStore.document(householdPath);

        // Store the path to the user's household
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(HOUSEHOLD_PATH, householdPath);
        editor.apply();
    }

    private void fetchUserData() {
        // Get document with current userId from the users collection
        mUserRef = mFireStore.collection(USERS_COLLECTION_PATH).document(mUserId);
        mUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Check if user is already registered in the user database
                if (documentSnapshot.exists()) {
                    // Check if user has a household
                    if (documentSnapshot.contains(KEY_HOUSEHOLD)) {
                        DocumentReference householdRef = documentSnapshot.getDocumentReference(KEY_HOUSEHOLD);
                        if (householdRef != null) {
                            storeHousehold(householdRef.getPath());
                            setContentView(R.layout.household_manager);
                        } else {
                            setContentView(R.layout.no_household_layout);
                        }
                    } else {
                        // User is an existing user without a household
                        setContentView(R.layout.no_household_layout);
                    }
                } else {
                    // User is a new user
                    // TODO: maybe check if the user account already defines the name
                    setContentView(R.layout.no_household_layout);
                    addDialogFragment(new NewUserDialogFragment());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, e);
            }
        });
    }

    /**
     * Performs fragment transaction, and sets listener if object implements DialogFragmentInterface
     * @param newDialogFragment takes a DialogFragment as argument
     */
    private void addDialogFragment(DialogFragment newDialogFragment) {
        if (newDialogFragment instanceof DialogFragmentInterface) {
            ((DialogFragmentInterface) newDialogFragment).setListener(this);
        }

        // Add new user DialogFragment with transaction
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newDialogFragment).addToBackStack(null).commit();
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
    public void onAddingStart() {
        // Called when joining is initiated
        setContentView(R.layout.household_manager_loading);
    }

    @Override
    public void onAddingFinish(String householdPath) {
        // Store household and go to normal household layout
        storeHousehold(householdPath);
        setContentView(R.layout.household_manager);
    }

    @Override
    public void onAddingFailure(int stringResource) {
        // Reset to default path if adding failed
        storeHousehold("");

        Toast.makeText(HouseholdManagerActivity.this, getResources().getString(stringResource), Toast.LENGTH_LONG).show();
        setContentView(R.layout.no_household_layout);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HouseholdManagerActivity.this, LogInActivity.class);
                        startActivity(intent);
                    }
                });
    }

    public void buttonJoinHousehold(View view) {
        addDialogFragment(new JoinHouseholdDialogFragment());
    }

    public void buttonCreateHousehold(View view) {
        addDialogFragment(new CreateHouseholdDialogFragment());
    }

    public void buttonSignOut(View view) {
        signOut();
    }

    public void buttonInvite(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, new InviteUserDialogFragment()).addToBackStack(null).commit();
    }

    public void buttonLeaveHousehold(View view) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder = new AlertDialog.Builder(this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setMessage(getResources().getString(R.string.leave_household_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setContentView(R.layout.no_household_layout);

                        // Delete user from household's member collection
                        mHouseholdRef.collection(KEY_MEMBERS).document(mUserId).delete().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_NAME, "Failed to delete user from household: " + e);
                            }
                        });

                        // Delete household reference from user
                        Map<String, Object> mapHousehold = new HashMap<>();
                        mapHousehold.put(KEY_HOUSEHOLD, FieldValue.delete());

                        mUserRef.update(mapHousehold).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_NAME, "Failed to delete household reference from user: " + e);
                                Toast.makeText(HouseholdManagerActivity.this, getResources().getString(R.string.leave_household_failed), Toast.LENGTH_LONG).show();
                            }
                        });

                        // Delete household if it's empty
                        mHouseholdRef.collection(KEY_MEMBERS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    // Household has no more members, check if there is still an invite code
                                    mHouseholdRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                if (documentSnapshot.getString(KEY_INVITE_CODE) != null) {
                                                    // Delete household's invite code if it exists
                                                    String inviteCode = documentSnapshot.getString(KEY_INVITE_CODE);
                                                    if (inviteCode != null) {
                                                        mFireStore.collection(KEY_INVITES).document(inviteCode).delete().addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(LOG_NAME, "Failed to delete household invite code: " + e);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(LOG_NAME, "Failed to retrieve household invite_code: " + e);
                                        }
                                    });

                                    // Delete all household collections (by deleting all documents in the collections)
                                    for (final String collectionPath : HOUSEHOLD_COLLECTION_PATHS) {
                                        mHouseholdRef.collection(collectionPath).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                    document.getReference().delete().addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(LOG_NAME, "Failed to delete document in " + collectionPath + ": " + e);
                                                        }
                                                    });
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(LOG_NAME, "Failed to retrieve collection " + collectionPath + ": " + e);
                                            }
                                        });
                                    }

                                    // Delete household
                                    mHouseholdRef.delete().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(LOG_NAME, "Failed to delete empty household: " + e);
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_NAME, "Failed to retrieve household: " + e);
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void buttonHouseholdFeed(View view) {
        Intent intent = new Intent(this, HouseholdFeedActivity.class);
        startActivity(intent);
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
