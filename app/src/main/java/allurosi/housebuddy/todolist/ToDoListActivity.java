package allurosi.housebuddy.todolist;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import allurosi.housebuddy.R;

public class ToDoListActivity extends AppCompatActivity implements AddTaskDialogFragment.NewTaskDialogListener {

    public static final String TASK_MESSAGE = "Task";
    public static final int DELETE_TASK = 1;

    private static List<Task> toDoList = new ArrayList<>();
    private ToDoListAdapter listAdapter;

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
                startActivityForResult(intent, DELETE_TASK);
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
        // Remove task if ViewTaskActivity returns RESULT_OK
        if (requestCode == DELETE_TASK) {
            if (resultCode == RESULT_OK) {
                // TODO: implement undo + add Snackbar
                Task task = data.getParcelableExtra(TASK_MESSAGE);
                listAdapter.remove(task);
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
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete_multiple:
                    listAdapter.deleteSelected();
                    actionMode.finish();

                    // Show snackbar with option to undo removal
                    Snackbar deleteSnackbar = Snackbar.make(findViewById(R.id.to_do_list_root_view), "Tasks removed.", Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listAdapter.undoRemoval();
                        }
                    });
                    deleteSnackbar.show();
                    return true;

                case R.id.action_complete_multiple:
                    // TODO implement marking tasks as complete, create task class
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
