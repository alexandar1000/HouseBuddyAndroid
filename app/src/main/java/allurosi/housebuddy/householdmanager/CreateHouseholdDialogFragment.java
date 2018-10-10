package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.KEY_HOUSEHOLD;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.LOG_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.USERS_COLLECTION_PATH;
import static allurosi.housebuddy.householdmanager.JoinHouseholdDialogFragment.FIELD_COLOR;
import static allurosi.housebuddy.householdmanager.JoinHouseholdDialogFragment.FIELD_USER_REFERENCE;
import static allurosi.housebuddy.householdmanager.JoinHouseholdDialogFragment.KEY_MEMBERS;

public class CreateHouseholdDialogFragment extends DialogFragment implements DialogFragmentInterface {

    private static final int errorMessageString = R.string.create_household_failed;
    public static final String HOUSEHOLDS_COLLECTION_PATH = "households";
    public static final String KEY_NAME = "name";

    private Context mContext;
    private TextInputEditText householdNameInput;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();

    private AddHouseholdListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_create_household, container, false);

        ImageButton closeButton = rootView.findViewById(R.id.button_close);
        Button createButton = rootView.findViewById(R.id.button_create);
        householdNameInput = rootView.findViewById(R.id.new_household_name);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (householdNameInput.getText().toString().isEmpty()) {
                    dismiss();
                } else {
                    showDiscardWarning();
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String householdName = householdNameInput.getText().toString();

                if (householdName.isEmpty()) {
                    householdNameInput.setError(getResources().getString(R.string.required_field));
                } else {
                    String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString(USER_ID, "");
                    createHousehold(userId, householdName);

                    dismiss();
                }
            }
        });
        return rootView;
    }

    private void createHousehold(final String userId, String householdName) {
        listener.onAddingStart();

        // Create new household with name
        Map<String, Object> mapHouseHold = new HashMap<>();
        mapHouseHold.put(KEY_NAME, householdName);

        mFireStore.collection(HOUSEHOLDS_COLLECTION_PATH).add(mapHouseHold).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(final DocumentReference documentReference) {
                // Add household reference to user
                DocumentReference userRef = mFireStore.collection(USERS_COLLECTION_PATH).document(userId);
                userRef.update(KEY_HOUSEHOLD, documentReference).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onAddingFinish(documentReference.getPath());
                        dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_NAME, "CREATE DIALOG: Failed to add household to user: " + e);
                        listener.onAddingFailure(errorMessageString);
                    }
                });

                // Add user to household members
                Map<String, Object> mapUser = new HashMap<>();
                // TODO: randomize color
                mapUser.put(FIELD_COLOR, "0000FF");
                mapUser.put(FIELD_USER_REFERENCE, userRef);
                documentReference.collection(KEY_MEMBERS).document(userId).set(mapUser).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_NAME, "CREATE DIALOG: Failed to add user as household member: " + e);
                        listener.onAddingFailure(errorMessageString);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "CREATE DIALOG: Failed to add household: " + e);
                listener.onAddingFailure(errorMessageString);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void showDiscardWarning() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder = new AlertDialog.Builder(mContext, android.R.style.ThemeOverlay_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }

        builder.setMessage(getString(R.string.discard_changes_question))
                .setPositiveButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void setListener(HouseholdManagerActivity parent) {
        listener = (AddHouseholdListener) parent;
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
