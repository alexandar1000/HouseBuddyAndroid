package allurosi.housebuddy.todolist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import allurosi.housebuddy.R;

public class AddTaskDialogFragment extends DialogFragment {

    private Context mContext;
    private Task newTask;
    private TextInputEditText newTaskNameInput, newTaskDescInput;

    private NewTaskDialogListener listener;

    public interface NewTaskDialogListener {
        void onAddNewTask(Task newTask);

        void onCloseNewTaskDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow fragment to receive onOptionItemSelected calls
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_new_task, container, false);

        newTaskNameInput = rootView.findViewById(R.id.new_task_name);
        newTaskDescInput = rootView.findViewById(R.id.new_task_description);

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!newTaskNameInput.getText().toString().isEmpty() || !newTaskDescInput.getText().toString().isEmpty()) {
                    showDiscardWarning();
                } else {
                    dismiss();
                }
                return true;

            case R.id.action_save:
                if (createTask(newTaskNameInput.getText().toString(), newTaskDescInput.getText().toString())) {
                    // Notify the listener that a new task has to be added
                    listener.onAddNewTask(newTask);
                    dismiss();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListener(ToDoListFragment parent) {
        listener = (NewTaskDialogListener) parent;
    }

    private boolean createTask(String newTaskName, String newTaskDescription) {
        // Notify the user if no name is supplied before pressing create
        if (newTaskName.isEmpty()) {
            newTaskNameInput.setError(getResources().getString(R.string.enter_name_alert));
            return false;
        }

        newTask = new Task(newTaskName);
        newTask.setTaskDesc(newTaskDescription);

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
        listener.onCloseNewTaskDialog();
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
