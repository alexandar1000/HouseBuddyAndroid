package allurosi.housebuddy.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.todolist.ViewTaskActivity.TASK_MESSAGE_ORIGINAL;

public class ToDoListActivity extends AppCompatActivity implements AddTaskDialogFragment.NewTaskDialogListener {

    private static final String LOG_NAME = "ToDoListActivity";
    public static final String TASK_MESSAGE = "Task";

    public static final int VIEW_TASK = 1;

    public static final int RESULT_DELETE = 1;
    public static final int RESULT_EDIT = 2;

    public static final String COLLECTION_PATH_TO_DO_LIST = "to_do_list";

    private List<Task> toDoList = new ArrayList<>();
    private ToDoListAdapter listAdapter;
    private FloatingActionButton fab;
    private FrameLayout loadingLayout;
    private Task lastDeleted;

    public static Boolean isActionMode = false;
    public static ActionMode mActionMode = null;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private CollectionReference mToDoListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        // Fetch tasks from database
        fetchTasks();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.to_do_list));
        }

        // TODO: change to RecyclerView in the future?
        final ListView toDoListView = findViewById(R.id.to_do_list);
        fab = findViewById(R.id.add_task_fab);
        loadingLayout = findViewById(R.id.to_do_list_loading);

        listAdapter = new ToDoListAdapter(this, R.layout.to_do_list_item, toDoList);
        toDoListView.setAdapter(listAdapter);

        // Add empty layout to todoListView
        View emptyList = findViewById(R.id.to_do_list_empty);
        toDoListView.setEmptyView(emptyList);

        // Allow multiple choices in selection mode and set listener
        toDoListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        toDoListView.setMultiChoiceModeListener(modeListener);
        toDoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Task clickedTask = (Task) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(ToDoListActivity.this, ViewTaskActivity.class);
                intent.putExtra(TASK_MESSAGE, clickedTask);
                startActivityForResult(intent, VIEW_TASK);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskDialog();
            }
        });
    }

    private void fetchTasks() {
        // Get stored household path
        String householdPath = PreferenceManager.getDefaultSharedPreferences(this).getString(HOUSEHOLD_PATH, "");

        mToDoListRef = mFireStore.document(householdPath).collection(COLLECTION_PATH_TO_DO_LIST);
        mToDoListRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Add all tasks from DB that are not in the todoList yet to the todoList
                    Task task = document.toObject(Task.class);
                    task.setTaskId(document.getId());
                    if (!toDoList.contains(task)) {
                        toDoList.add(task);
                    }
                }
                listAdapter.notifyDataSetChanged();
                loadingLayout.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to retrieve to-do list collection: " + e);
            }
        });
    }

    private void addTaskDialog() {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.setListener(this);

        // Hide to do list action bar and fab
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        fab.hide();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // DialogFragment is removed
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    onCloseNewTaskDialog();
                }
            }
        });

        // Add DialogFragment with transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.to_do_list_root_view, addTaskDialogFragment).addToBackStack(null).commit();
    }

    private void addTask(final Task newTask) {
        listAdapter.add(newTask);

        // Add new task to database
        mToDoListRef.add(newTask).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Set auto generated FireStore id as task id
                newTask.setTaskId(documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add task to database: " + e);
            }
        });
    }

    @Override
    public void onAddNewTask(Task newTask) {
        addTask(newTask);
    }

    @Override
    public void onCloseNewTaskDialog() {
        // Hide keyboard after closing the dialog
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Return toolbar and fab
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        fab.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle result of ViewTaskActivity
        if (requestCode == VIEW_TASK) {
            switch (resultCode) {
                case RESULT_DELETE:
                    // Remove task from list
                    final Task taskToDelete = data.getParcelableExtra(TASK_MESSAGE);
                    lastDeleted = new Task(taskToDelete);
                    listAdapter.remove(taskToDelete);

                    // Remove task from database
                    mToDoListRef.document(taskToDelete.getTaskId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Show snackbar with option to undo removal
                            Snackbar deleteSnackbar = Snackbar.make(findViewById(R.id.to_do_list_root_view), getResources().getQuantityString(R.plurals.task_deleted, 1), Snackbar.LENGTH_LONG);
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
                            Toast.makeText(ToDoListActivity.this, getResources().getString(R.string.delete_task_failed, taskToDelete.getTaskName()), Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                case RESULT_EDIT:
                    Task newTask = data.getParcelableExtra(TASK_MESSAGE);
                    Task originalTask = data.getParcelableExtra(TASK_MESSAGE_ORIGINAL);

                    // Replace old task
                    listAdapter.remove(originalTask);
                    addTask(newTask);
                    break;
            }
        }
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
            final int selectionSize = listAdapter.selectionSize();

            switch (menuItem.getItemId()) {
                case R.id.action_delete_multiple:

                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        builder = new AlertDialog.Builder(ToDoListActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(ToDoListActivity.this);
                    }

                    builder.setMessage(getResources().getQuantityString(R.plurals.delete_task_question, selectionSize))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    final List<Task> selection = listAdapter.getSelection();

                                    // Delete selection from adapter list, finish action mode
                                    listAdapter.deleteSelected();
                                    actionMode.finish();

                                    // Delete selection from database
                                    for (int i = 0; i < selectionSize; i++) {
                                        final Task task = selection.get(i);
                                        if (i == selectionSize - 1) {
                                            mToDoListRef.document(task.getTaskId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Show snackbar with option to undo removal
                                                    Snackbar deleteSnackbar = Snackbar.make(findViewById(R.id.to_do_list_root_view), getResources().getQuantityString(R.plurals.task_deleted, selectionSize), Snackbar.LENGTH_LONG);
                                                    deleteSnackbar.setAction(getString(R.string.action_undo), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            listAdapter.undoRemoval();

                                                            // Re-add selection to database
                                                            for (final Task task : selection) {
                                                                mToDoListRef.document(task.getTaskId()).set(task).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(LOG_NAME, "Failed to add task with id " + task.getTaskId() + ": " + e);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                    deleteSnackbar.show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(LOG_NAME, "Failed to delete task with id " + task.getTaskId() + ": " + e);
                                                    Toast.makeText(ToDoListActivity.this, getResources().getString(R.string.delete_task_failed, task.getTaskName()), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        } else {
                                            mToDoListRef.document(task.getTaskId()).delete().addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(LOG_NAME, "Failed to delete task with id " + task.getTaskId() + ": " + e);
                                                    Toast.makeText(ToDoListActivity.this, getResources().getString(R.string.delete_task_failed, task.getTaskName()), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
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
                    Toast.makeText(ToDoListActivity.this, getResources().getQuantityString(R.plurals.task_marked_completed, selectionSize), Toast.LENGTH_SHORT).show();
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

}
