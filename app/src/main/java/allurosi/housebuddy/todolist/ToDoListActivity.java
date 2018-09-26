package allurosi.housebuddy.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.todolist.ViewTaskActivity.TASK_MESSAGE_ORIGINAL;

public class ToDoListActivity extends AppCompatActivity implements AddTaskDialogFragment.NewTaskDialogListener {

    public static final String TASK_MESSAGE = "Task";

    public static final int VIEW_TASK = 1;
    public static final int RESULT_DELETE = 1;
    public static final int RESULT_EDIT = 2;

    private static List<Task> toDoList = new ArrayList<>();
    private ToDoListAdapter listAdapter;
    private Task lastDeleted;

    public static Boolean isActionMode = false;
    public static ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        getSupportActionBar().setTitle("To Do List");

        // TODO: change to RecyclerView in the future?
        final ListView toDoListView = findViewById(R.id.to_do_list);
        FloatingActionButton fab = findViewById(R.id.add_task_fab);

        listAdapter = new ToDoListAdapter(this, R.layout.to_do_list_item, toDoList);
        toDoListView.setAdapter(listAdapter);

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
                addTask();
            }
        });
    }

    public static void initDummyData() {
        // TODO: remove if testing is done
        toDoList.add(new Task("Clean kitchen"));
        toDoList.add(new Task("Buy new pans", "We need a new frying pan since the other one broke.."));
        toDoList.add(new Task("Fix stove", "The right burner isn't working atm."));
        toDoList.add(new Task("Make chore schedule"));
        Collections.sort(toDoList);
    }

    private void addTask() {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.setListener(this);
        addTaskDialogFragment.show(getSupportFragmentManager(), "AddTaskFragment");
    }

    @Override
    public void onFinishNewTaskDialog(Task newTask) {
        listAdapter.add(newTask);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle result of ViewTaskActivity
        if (requestCode == VIEW_TASK) {
            switch (resultCode) {
                case RESULT_DELETE:
                    Task taskToDelete = data.getParcelableExtra(TASK_MESSAGE);
                    lastDeleted = new Task(taskToDelete);
                    listAdapter.remove(taskToDelete);

                    // Show snackbar with option to undo removal
                    // TODO: maybe change task to the actual name
                    Snackbar deleteSnackbar = Snackbar.make(findViewById(R.id.to_do_list_root_view), "Task deleted.", Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listAdapter.add(lastDeleted);
                        }
                    });
                    deleteSnackbar.show();
                    break;

                case RESULT_EDIT:
                    Task newTask = data.getParcelableExtra(TASK_MESSAGE);
                    Task originalTask = data.getParcelableExtra(TASK_MESSAGE_ORIGINAL);

                    // Replace old task
                    listAdapter.remove(originalTask);
                    listAdapter.add(newTask);
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
            switch (menuItem.getItemId()) {
                case R.id.action_delete_multiple:
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        builder = new AlertDialog.Builder(ToDoListActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(ToDoListActivity.this);
                    }

                    builder.setMessage("Delete these tasks?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    listAdapter.deleteSelected();
                                    actionMode.finish();

                                    // Show snackbar with option to undo removal
                                    Snackbar deleteSnackbar = Snackbar.make(findViewById(R.id.to_do_list_root_view), "Tasks deleted.", Snackbar.LENGTH_LONG);
                                    deleteSnackbar.setAction("Undo", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            listAdapter.undoRemoval();
                                        }
                                    });
                                    deleteSnackbar.show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    actionMode.finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;

                case R.id.action_complete_multiple:
                    // TODO implement marking tasks as complete
                    Toast.makeText(ToDoListActivity.this, "Tasks marked as completed.", Toast.LENGTH_SHORT).show();
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
