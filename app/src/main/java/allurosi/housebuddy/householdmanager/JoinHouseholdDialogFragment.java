package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.KEY_HOUSEHOLD;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.LOG_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.USERS_COLLECTION_PATH;
import static allurosi.housebuddy.householdmanager.InviteUserDialogFragment.KEY_INVITES;

import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;

public class JoinHouseholdDialogFragment extends DialogFragment implements DialogFragmentInterface {

    public static final String COLLECTION_PATH_MEMBERS = "members";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_USER_REFERENCE = "user_reference";

    private static final int errorMessageString = R.string.join_household_failed;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private TextInputEditText invitationCodeInput;
    private Context mContext;

    private AddHouseholdListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_join_household, container, false);

        ImageButton closeButton = rootView.findViewById(R.id.button_close);
        Button joinButton = rootView.findViewById(R.id.button_join);
        invitationCodeInput = rootView.findViewById(R.id.invititation_code);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Maybe add warning
                // Get user id from device storage
                String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString(USER_ID, "");
                if (userId.isEmpty()) {
                    // No user id saved, abort
                    Log.w(LOG_NAME, "JOIN DIALOG: No user id saved.");
                    listener.onAddingFailure(errorMessageString);
                    dismiss();
                } else {
                    String invitationCode = invitationCodeInput.getText().toString();
                    if (invitationCode.isEmpty()) {
                        // Nothing entered, give error
                        invitationCodeInput.setError(getResources().getString(R.string.invalid_invitation_code));
                    } else {
                        // Try to join household
                        joinHousehold(invitationCode, userId);
                    }
                }
            }
        });
        return rootView;
    }

    private void joinHousehold(String invitationCode, final String userId) {
        mFireStore.collection(KEY_INVITES).document(invitationCode).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Check if invitation code is valid (if it exists in the database)
                if (documentSnapshot.exists()) {
                    listener.onAddingStart();

                    // Get household linked to invite
                    final DocumentReference householdReference = documentSnapshot.getDocumentReference(KEY_HOUSEHOLD);

                    // Add household reference to user
                    DocumentReference userRef = mFireStore.collection(USERS_COLLECTION_PATH).document(userId);
                    userRef.update(KEY_HOUSEHOLD, householdReference).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onAddingFinish(householdReference.getPath());
                            dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "JOIN DIALOG: Failed to add household to user: " + e);
                            listener.onAddingFailure(errorMessageString);
                        }
                    });

                    // Add user to household members
                    Map<String, Object> map = new HashMap<>();
                    // TODO: randomize color
                    map.put(FIELD_COLOR, "0000FF");
                    map.put(FIELD_USER_REFERENCE, userRef);
                    householdReference.collection(COLLECTION_PATH_MEMBERS).document(userId).set(map).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "JOIN DIALOG: Failed to add user as household member: " + e);
                            listener.onAddingFailure(errorMessageString);
                        }
                    });
                } else {
                    // Invite doesn't exist, give error
                    invitationCodeInput.setError(getResources().getString(R.string.invalid_invitation_code));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "JOIN DIALOG: Failed to retrieve invite: " + e);
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
