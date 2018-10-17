package allurosi.housebuddy.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.logging.LogEntry;
import allurosi.housebuddy.logging.Loggable;

import static allurosi.housebuddy.authentication.LogInActivity.FULL_NAME;
import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_FIRST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_LAST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.USERS_COLLECTION_PATH;

public class ToDoListFragment extends Fragment implements AddTaskDialogFragment.NewTaskDialogListener,
        ViewTaskFragment.ViewTaskFragmentListener, Loggable {

    private static final String LOG_NAME = "ToDoListFragment";
    public static final String TASK_MESSAGE = "Task";

    public static final String COLLECTION_PATH_TO_DO_LIST = "to_do_list";
    public static final String COLLECTION_PATH_CHANGE_LOG = "change_log";
    public static final String LOCATION_TO_DO_LIST = "To Do List";

    private Context mContext;
    private List<Task> toDoList = new ArrayList<>();
    private ToDoListAdapter listAdapter;
    private FloatingActionButton fab;
    private FrameLayout loadingLayout;
    private Task lastDeleted;
    private String mUserId;
    private String mFullName;

    private ActionBar mActionBar;

    private SharedPreferences mSharedPreferences;

    public static Boolean isActionMode = false;
    public static ActionMode mActionMode = null;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private CollectionReference mToDoListRef;
    private CollectionReference mChangeLogRef;

    private ListenerRegistration mListenerRegistration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (mUserId == null) {
            mUserId = mSharedPreferences.getString(USER_ID, "");
            mFullName = mSharedPreferences.getString(FULL_NAME, "");
        }

        // TODO: change to RecyclerView in the future?
        final ListView toDoListView = rootView.findViewById(R.id.to_do_list);
        fab = rootView.findViewById(R.id.add_task_fab);
        loadingLayout = rootView.findViewById(R.id.to_do_list_loading);

        listAdapter = new ToDoListAdapter(mContext, R.layout.to_do_list_item, toDoList);
        toDoListView.setAdapter(listAdapter);

        // Add empty layout to todoListView
        View emptyList = rootView.findViewById(R.id.to_do_list_empty);
        toDoListView.setEmptyView(emptyList);

        // Allow multiple choices in selection mode and set listener
        toDoListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        toDoListView.setMultiChoiceModeListener(modeListener);

        toDoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Task clickedTask = (Task) adapterView.getItemAtPosition(i);

                ViewTaskFragment viewTaskFragment = new ViewTaskFragment();
                viewTaskFragment.setListener(ToDoListFragment.this);

                updateActionBar(clickedTask.getTaskName(), R.drawable.ic_arrow_back_white);

                // Add task as argument for the fragment
                Bundle bundle = new Bundle();
                bundle.putParcelable(TASK_MESSAGE, clickedTask);
                viewTaskFragment.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.flContent, viewTaskFragment).addToBackStack(null).commit();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskDialog();
            }
        });

        return rootView;
    }

    private void updateActionBar(String title, int backDrawableId) {
        mActionBar.setTitle(title);
        mActionBar.setHomeAsUpIndicator(backDrawableId);
    }

    private void addTaskDialog() {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.setListener(this);

        // Hide fab, change actionBar
        fab.hide();
        updateActionBar(mContext.getResources().getString(R.string.new_task), R.drawable.ic_close_white);

        // Add DialogFragment with transaction
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.flContent, addTaskDialogFragment).addToBackStack(null).commit();
    }

    private void addTask(final Task newTask) {
        // Add new task to database
        mToDoListRef.add(newTask).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Set auto generated FireStore id as task id
                newTask.setTaskId(documentReference.getId());

                // Add adding task action to change log
                logChange(R.string.action_added_task);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add task to database: " + e);
            }
        });
    }

    public void logChange(final int changeActionStringResource) {
        mFullName = mSharedPreferences.getString(FULL_NAME, "");
        if (mFullName.equals("")) {
            mFireStore.collection(USERS_COLLECTION_PATH).document(mUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String firstName = documentSnapshot.getString(FIELD_FIRST_NAME);
                    String lastName = documentSnapshot.getString(FIELD_LAST_NAME);
                    mFullName = firstName + " " + lastName;

                    // Add change log containing the change location, change info and a timestamp
                    LogEntry logEntry = new LogEntry(LOCATION_TO_DO_LIST, mContext.getResources().getString(changeActionStringResource), mFullName, new Timestamp(new Date()));
                    mChangeLogRef.add(logEntry).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "Failed to add log entry: " + e);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_NAME, "Failed to retrieve name for logging: " + e);
                }
            });
        } else {
            LogEntry logEntry = new LogEntry(LOCATION_TO_DO_LIST, mContext.getResources().getString(changeActionStringResource), mFullName, new Timestamp(new Date()));
            mChangeLogRef.add(logEntry).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_NAME, "Failed to add log entry: " + e);
                }
            });
        }
    }

    @Override
    public void onAddNewTask(Task newTask) {
        if (newTask.getTaskDesc().isEmpty()) {
            newTask.setTaskDesc(null);
        }
        addTask(newTask);
    }

    @Override
    public void onCloseNewTaskDialog() {
        // Hide keyboard after closing the dialog
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((AppCompatActivity) mContext).getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Return fab and change action bar back
        fab.show();
        updateActionBar(mContext.getResources().getString(R.string.app_name), R.drawable.ic_menu_white);
    }

    @Override
    public void onDeleteTask(final Task taskToDelete) {
        lastDeleted = new Task(taskToDelete);

        // Remove task from database
        mToDoListRef.document(taskToDelete.getTaskId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Add deleting task action to change log
                logChange(R.string.action_removed_task);

                // Show snackbar with option to undo removal
                Snackbar deleteSnackbar = Snackbar.make(((AppCompatActivity) mContext).findViewById(android.R.id.content), mContext.getResources().getQuantityString(R.plurals.task_deleted, 1), Snackbar.LENGTH_LONG);
                deleteSnackbar.setAction(getString(R.string.action_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addTask(lastDeleted);
                    }
                });
                deleteSnackbar.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to delete task: " + e);
                Toast.makeText(mContext, mContext.getResources().getString(R.string.delete_task_failed, taskToDelete.getTaskName()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onEditTask(Task newTask, Task originalTask) {
        // Replace old task in database
        // TODO: do we want to log editing of tasks?
        mToDoListRef.document(originalTask.getTaskId()).set(newTask).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to edit task: " + e);
                Toast.makeText(mContext, mContext.getResources().getString(R.string.edit_task_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCloseViewTask() {
        updateActionBar(mContext.getResources().getString(R.string.app_name), R.drawable.ic_menu_white);
        getFragmentManager().popBackStack();
    }

    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            Task task = toDoList.get(i);

            // Add and remove items from selection when list item is selected
            if (listAdapter.isSelected(task)) {
                listAdapter.removeFromSelection(task);
            } else {
                listAdapter.addToSelection(task);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_todo_list, menu);

            // Clear previous selection, undo no longer possible
            listAdapter.clearSelection();

            isActionMode = true;
            mActionMode = actionMode;

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
            final List<Task> selection = listAdapter.getSelection();
            final int selectionSize = selection.size();

            switch (menuItem.getItemId()) {
                case R.id.action_delete_multiple:

                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        builder = new AlertDialog.Builder(mContext, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(mContext);
                    }

                    builder.setMessage(mContext.getResources().getQuantityString(R.plurals.delete_task_question, selectionSize))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Delete selection from adapter list, finish action mode
                                    listAdapter.deleteSelected();
                                    actionMode.finish();

                                    // Delete selection from database
                                    WriteBatch deleteBatch = mFireStore.batch();
                                    for (Task task : selection) {
                                        deleteBatch.delete(mToDoListRef.document(task.getTaskId()));
                                    }
                                    deleteBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Add deleting tasks action to change log
                                            logChange(R.string.action_removed_tasks);

                                            // Show snackbar with option to undo removal
                                            Snackbar deleteSnackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), mContext.getResources().getQuantityString(R.plurals.task_deleted, selectionSize), Snackbar.LENGTH_LONG);
                                            deleteSnackbar.setAction(getString(R.string.action_undo), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    listAdapter.undoRemoval();

                                                    // Re-add selection to database
                                                    WriteBatch reAddBatch = mFireStore.batch();
                                                    for (final Task task : selection) {
                                                        reAddBatch.set(mToDoListRef.document(task.getTaskId()), task);
                                                    }
                                                    reAddBatch.commit().addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(LOG_NAME, "Failed to add one or more tasks: " + e);
                                                            Toast.makeText(mContext, mContext.getResources().getString(R.string.add_tasks_failed), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            });
                                            deleteSnackbar.show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(LOG_NAME, "Failed to add one or more tasks: " + e);
                                            Toast.makeText(mContext, mContext.getResources().getString(R.string.delete_tasks_failed), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    actionMode.finish();
                                }
                            })
                            .show();
                    return true;

                case R.id.action_complete_multiple:
                    listAdapter.markSelectionCompleted();

                    // Mark selection as completed in database
                    WriteBatch completedBatch = mFireStore.batch();
                    for (Task task : selection) {
                        task.setCompleted(true);
                        completedBatch.set(mToDoListRef.document(task.getTaskId()), task);
                    }
                    completedBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, mContext.getResources().getQuantityString(R.plurals.task_marked_completed, selectionSize), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "Failed to mark one or more tasks as completed: " + e);
                        }
                    });

                    actionMode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            isActionMode = false;
            mActionMode = null;
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Get stored household path
        String householdPath = mSharedPreferences.getString(HOUSEHOLD_PATH, "");
        DocumentReference householdRef = mFireStore.document(householdPath);

        mChangeLogRef = householdRef.collection(COLLECTION_PATH_CHANGE_LOG);
        mToDoListRef = householdRef.collection(COLLECTION_PATH_TO_DO_LIST);

        // Add listener which updates the todoList if another user changes it
        // Is called once when addSnapshotListener is called
        mListenerRegistration = mToDoListRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_NAME, "Listen error: ", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<Task> newTodoList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        task.setTaskId(document.getId());
                        newTodoList.add(task);
                    }
                    toDoList.clear();
                    toDoList.addAll(newTodoList);
                    listAdapter.notifyDataSetChanged();
                    loadingLayout.setVisibility(View.GONE);
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
