package allurosi.housebuddy.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.todolist.ToDoListFragment.TASK_MESSAGE;

public class EditTaskFragment extends Fragment {

    private Context mContext;

    private Task originalTask;
    private Task mTask;

    private TextInputEditText textTaskName;
    private TextInputEditText textTaskDesc;

    private EditTaskFragmentListener listener;

    public interface EditTaskFragmentListener {
        void onEditTask(Task newTask);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_task, container, false);

        textTaskName = rootView.findViewById(R.id.edit_task_name);
        textTaskDesc = rootView.findViewById(R.id.edit_task_description);

        // Get passed task
        Bundle bundle = getArguments();
        originalTask = bundle.getParcelable(TASK_MESSAGE);
        mTask = new Task(originalTask);

        // Set values in layout
        textTaskName.setText(mTask.getTaskName());
        textTaskDesc.setText(mTask.getTaskDesc());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_task, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String newName = textTaskName.getText().toString();
        String newDesc = textTaskDesc.getText().toString();

        if (mTask != null) {
            // Update task
            mTask.setTaskName(newName);
            mTask.setTaskDesc(newDesc);

            // If task didn't change we do nothing
            if (mTask.equals(originalTask)) {
                getFragmentManager().popBackStack();
                return true;
            }
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardWarning();
                return true;

            case R.id.action_save:
                // Check if new name is empty
                if (newName.isEmpty()) {
                    textTaskName.setError(getString(R.string.enter_name_alert));
                } else {
                    listener.onEditTask(mTask);
                    getFragmentManager().popBackStack();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setListener(ViewTaskFragment parent) {
        listener = (EditTaskFragmentListener) parent;
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
                        getFragmentManager().popBackStack();
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
