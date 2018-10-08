package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.KEY_HOUSEHOLD;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.LOG_NAME;

public class InviteUserDialogFragment extends DialogFragment {

    public static final String KEY_INVITES = "invites";
    public static final String KEY_INVITE_CODE = "invite_code";

    private Context mContext;
    private TextView inviteCodeView;
    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    // TODO: maybe store last invite code for if there's no internet
    private String mInviteCode = null;
    private DocumentReference mHouseholdRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_invite_user, container, false);

        inviteCodeView = rootView.findViewById(R.id.invite_code);
        Button renewCodeButton = rootView.findViewById(R.id.button_renew_code);
        ImageButton closeButton = rootView.findViewById(R.id.button_close);

        // Get stored household path in database
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String householdPath = pref.getString(HOUSEHOLD_PATH, "");

        // Get current invite code, if it exists
        mHouseholdRef = mFireStore.document(householdPath);
        mHouseholdRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString(KEY_INVITE_CODE) != null) {
                    // Set invite code if it exists
                    mInviteCode = documentSnapshot.getString(KEY_INVITE_CODE);
                    inviteCodeView.setText(mInviteCode);
                } else {
                    // No invite code stored, generate and store a new code
                    generateNewInviteCode();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "INVITE DIALOG, failed to retrieve household invite_code: " + e);
            }
        });

        inviteCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Copy code to clipboard on click
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invite code", inviteCodeView.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(mContext, R.string.code_copied, Toast.LENGTH_SHORT).show();
            }
        });

        inviteCodeView.requestFocus();

        renewCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewInviteCode();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

    private void generateNewInviteCode() {
        final List<String> existingInvites = new ArrayList<>();
        final CollectionReference invitesCollection = mFireStore.collection(KEY_INVITES);

        invitesCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Add all existing invites to list
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    existingInvites.add(document.getId());
                }

                // Check if there is an old invite code
                if (existingInvites.contains(mInviteCode)) {
                    // Delete old invite code from invites collection
                    invitesCollection.document(mInviteCode).delete().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "INVITE DIALOG, failed to delete invite: " + e);
                        }
                    });
                }

                // Create a new code if the inviteCode already exists
                do {
                    mInviteCode = randomString();
                } while (existingInvites.contains(mInviteCode));

                // Update invite code in household document
                Map<String, Object> mapKey = new HashMap<>();
                mapKey.put(KEY_INVITE_CODE, mInviteCode);
                mHouseholdRef.update(mapKey);

                // Add new invite code to invites collection
                Map<String, Object> mapInvite = new HashMap<>();
                mapInvite.put(KEY_HOUSEHOLD, mHouseholdRef.getPath());
                invitesCollection.document(mInviteCode).set(mapInvite).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_NAME, "INVITE DIALOG, failed to add invite: " + e);
                    }
                });

                inviteCodeView.setText(mInviteCode);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "INVITE DIALOG, failed to retrieve invites: " + e);
            }
        });
    }

    /**
     *    Auxiliary function that creates a random, human readable string
     */
    private String randomString() {
        final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        return sb.toString();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
