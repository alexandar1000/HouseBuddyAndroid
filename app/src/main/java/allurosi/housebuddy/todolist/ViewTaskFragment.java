package allurosi.housebuddy.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.todolist.ToDoListFragment.TASK_MESSAGE;

public class ViewTaskFragment extends Fragment implements EditTaskFragment.EditTaskFragmentListener {

    public static final String TASK_MESSAGE_ORIGINAL = "OriginalTask";

    public static final int EDIT_TASK = 1;

    private Context mContext;
    private ActionBar mActionBar;

    private Task mTask;
    private Task originalTask;

    private TextView textTaskDesc;

    private ViewTaskFragmentListener listener;

    public interface ViewTaskFragmentListener {
        void onDeleteTask(Task taskToDelete);

        void onEditTask(Task newTask, Task originalTask);

        void onCloseViewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_task, container, false);

        textTaskDesc = rootView.findViewById(R.id.view_task_description);

        // Get passed task
        Bundle bundle = getArguments();
        mTask = bundle.getParcelable(TASK_MESSAGE);

        // Set values in layout
        textTaskDesc.setText(mTask.getTaskDesc());

        return rootView;
    }

    @Override
    public void onEditTask(Task newTask, Task originalTask) {
        mActionBar.setTitle(newTask.getTaskName());
        textTaskDesc.setText(newTask.getTaskDesc());

        // Replace task with edited task
        listener.onEditTask(newTask, originalTask);

        Toast.makeText(mContext, getString(R.string.task_saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_task, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                listener.onCloseViewTask();
                return true;

            case R.id.action_complete:
                originalTask = new Task(mTask);
                mTask.setCompleted(true);

                // Replace task with completed (edited) task
                listener.onEditTask(mTask, originalTask);

                Toast.makeText(mContext, getResources().getQuantityString(R.plurals.task_marked_completed, 1), Toast.LENGTH_SHORT).show();
                listener.onCloseViewTask();
                return true;

            case R.id.action_edit:
                EditTaskFragment editTaskFragment = new EditTaskFragment();
                editTaskFragment.setListener(this);

                mActionBar.setTitle(getResources().getString(R.string.edit_task));

                // Add task as argument for the fragment
                Bundle bundle = new Bundle();
                bundle.putParcelable(TASK_MESSAGE, mTask);
                editTaskFragment.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.flContent, editTaskFragment).addToBackStack(null).commit();
                return true;

            case R.id.action_delete:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    builder = new AlertDialog.Builder(mContext, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(mContext);
                }

                builder.setMessage(getResources().getQuantityString(R.plurals.delete_task_question, 1))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete task
                        listener.onDeleteTask(mTask);
                        listener.onCloseViewTask();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setListener(ToDoListFragment parent) {
        listener = (ViewTaskFragmentListener) parent;
    }

    // Deprecated method to support lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mActionBar = ((AppCompatActivity) activity).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActionBar = ((AppCompatActivity) context).getSupportActionBar();
    }

}
