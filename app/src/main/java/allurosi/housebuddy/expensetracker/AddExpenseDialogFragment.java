package allurosi.housebuddy.expensetracker;

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

public class AddExpenseDialogFragment extends DialogFragment {
    private NewExpenseDialogListener listener;
    private TextInputEditText newExpenseNameInput;
    private TextInputEditText newExpensePriceInput;
    private Context mContext;

    private Product newExpense;


    public interface NewExpenseDialogListener {
        void onAddNewExpense(Product newProduct);

        void onCloseNewExpenseDialog();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_new_expense, container, false);

        newExpenseNameInput = rootView.findViewById(R.id.new_expense_name);
        newExpensePriceInput = rootView.findViewById(R.id.new_expense_price);
        ImageButton closeButton = rootView.findViewById(R.id.button_close);
        Button saveButton = rootView.findViewById(R.id.button_save_expense);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!newExpenseNameInput.getText().toString().isEmpty() || !newExpensePriceInput.getText().toString().isEmpty()) {
                    showDiscardWarning();
                } else {
                    dismiss();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = newExpensePriceInput.getText().toString();
                String name =newExpenseNameInput.getText().toString();
                if (createExpense(price, name)) {
                    // Notify the listener that a new task has to be added
                    listener.onAddNewExpense(newExpense);
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

    public void setListener(ExpensesActivity parent) {
        listener = (NewExpenseDialogListener) parent;
    }

    private boolean createExpense(String price, String newExpenseName) {
        // Notify the user if no name is supplied before pressing create
        if (newExpenseName.isEmpty()) {
            newExpenseNameInput.setError(getResources().getString(R.string.enter_name_alert));
            return false;
        }
        if(price.isEmpty()){
            newExpensePriceInput.setError(getResources().getString(R.string.enter_price_alert));
            return false;
        }

         newExpense = new Product(newExpenseName,Double.parseDouble(price));

        return true;
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

    @Override
    public void dismiss() {
        listener.onCloseNewExpenseDialog();
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
        super.dismiss();
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
