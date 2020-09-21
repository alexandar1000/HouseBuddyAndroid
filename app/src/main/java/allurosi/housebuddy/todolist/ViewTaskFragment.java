package allurosi.housebuddy.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.todolist.ToDoListFragment.COLLECTION_PATH_TO_DO_LIST;
import static allurosi.housebuddy.todolist.ToDoListFragment.LOG_NAME;
import static allurosi.housebuddy.todolist.ToDoListFragment.TASK_MESSAGE;

public class ViewTaskFragment extends Fragment implements EditTaskFragment.EditTaskFragmentListener {

    private Context mContext;
    private ActionBar mActionBar;
    private TextView mTextTaskDesc;

    private Task mTask;

    private ViewTaskFragmentListener listener;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private DocumentReference mTaskRef;

    private ListenerRegistration mListenerRegistration;

    public interface ViewTaskFragmentListener {
        void onDeleteTask(Task taskToDelete);

        void onEditTask();

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

        mTextTaskDesc = rootView.findViewById(R.id.view_task_description);

        // Get passed task
        Bundle bundle = getArguments();
        mTask = bundle.getParcelable(TASK_MESSAGE);

        // Set values in layout
        mTextTaskDesc.setText(mTask.getTaskDesc());

        return rootView;
    }

    @Override
    public void onEditTask(Task newTask) {
        // Replace task with edited task in database
        mTaskRef.set(newTask).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener.onEditTask();
                Toast.makeText(mContext, getString(R.string.task_saved), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "View Task: failed to edit task: " + e);
                Toast.makeText(mContext, getString(R.string.edit_task_failed), Toast.LENGTH_LONG).show();
            }
        });
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
                mTask.setCompleted(true);

                // Replace task with completed (edited) task
                onEditTask(mTask);

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

    @Override
    public void onStart() {
        super.onStart();

        // Get stored household path
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String householdPath = sharedPreferences.getString(HOUSEHOLD_PATH, "");
        DocumentReference householdRef = mFireStore.document(householdPath);

        mTaskRef = householdRef.collection(COLLECTION_PATH_TO_DO_LIST).document(mTask.getTaskId());

        // Add listener which updates the task if another user changes it
        // Is called once when addSnapshotListener is called
        mListenerRegistration = mTaskRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Task newTask = documentSnapshot.toObject(Task.class);
                    if (newTask != null) {
                        mActionBar.setTitle(newTask.getTaskName());
                        mTextTaskDesc.setText(newTask.getTaskDesc());

                        // Task in database doesn't have id, so add it
                        newTask.setTaskId(mTask.getTaskId());
                        mTask = new Task(newTask);
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Detach listener when it's no longer needed
        mListenerRegistration.remove();
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
