package allurosi.housebuddy.householdmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import allurosi.housebuddy.R;

public class NewUserDialogFragment extends DialogFragment implements DialogFragmentInterface {

    private Context mContext;
    TextInputEditText newFirstNameInput, newLastNameInput;

    private NewUserDialogListener listener;

    public interface NewUserDialogListener {
        void onAddNewUser(String firstName, String lastName);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_new_user, container, false);

        newFirstNameInput = rootView.findViewById(R.id.new_first_name);
        newLastNameInput = rootView.findViewById(R.id.new_last_name);
        ImageButton closeButton = rootView.findViewById(R.id.button_close);
        Button saveButton = rootView.findViewById(R.id.button_save);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!newFirstNameInput.getText().toString().isEmpty() || !newLastNameInput.getText().toString().isEmpty()) {
                    showDiscardWarning();
                } else {
                    dismiss();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = newFirstNameInput.getText().toString();
                String lastName = newLastNameInput.getText().toString();
                Boolean formCompleted = true;

                if (firstName.isEmpty()) {
                    newFirstNameInput.setError(getResources().getString(R.string.required_field));
                    formCompleted = false;
                }

                if (lastName.isEmpty()) {
                    newLastNameInput.setError(getResources().getString(R.string.required_field));
                    formCompleted = false;
                }

                if (formCompleted) {
                    listener.onAddNewUser(firstName, lastName);
                    dismiss();
                }
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
        listener = (NewUserDialogListener) parent;
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
