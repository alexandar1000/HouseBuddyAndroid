package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import allurosi.housebuddy.R;

public class JoinHouseholdDialogFragment extends DialogFragment {

    private Context mContext;

    private JoinHouseholdDialogListener listener;

    public interface JoinHouseholdDialogListener {
        void onJoinHousehold(String invitationCode);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_join_household, container, false);

        ImageButton closeButton = rootView.findViewById(R.id.button_close);
        Button joinButton = rootView.findViewById(R.id.button_join);
        final EditText invitationCode = rootView.findViewById(R.id.invititation_code);

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
                listener.onJoinHousehold(invitationCode.getText().toString());
                dismiss();
            }
        });
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void setListener(HouseholdManagerActivity parent) {
        listener = (JoinHouseholdDialogListener) parent;
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
