package allurosi.housebuddy.todolist;

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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import allurosi.housebuddy.R;

public class ToDoListActivity extends AppCompatActivity implements AddTaskDialogFragment.NewTaskDialogListener {

    private List<String> toDoList = new ArrayList<>();
    private ToDoListAdapter listAdapter;

    public static Boolean isActionMode = false;
    public static ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        // TODO: change to RecyclerView in the future?
        final ListView toDoListView = findViewById(R.id.to_do_list);
        FloatingActionButton fab = findViewById(R.id.add_task_fab);

        // TODO: implement description
        listAdapter = new ToDoListAdapter(this, R.layout.to_do_list_item, toDoList);
        toDoListView.setAdapter(listAdapter);

        // Allow multiple choices in selection mode and set listener
        toDoListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        toDoListView.setMultiChoiceModeListener(modeListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });

        initDummyData();
    }

    private void initDummyData() {
        // TODO: remove if testing is done
        toDoList.add("Clean kitchen");
        toDoList.add("Buy new pans");
        toDoList.add("Fix stove");
        toDoList.add("Make chore schedule");
        Collections.sort(toDoList);
    }

    private void addTask() {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.setListener(this);
        addTaskDialogFragment.show(getSupportFragmentManager(), "AddTaskFragment");
    }

    @Override
    public void onFinishNewTaskDialog(String newTask) {
        listAdapter.addTask(newTask);
    }


    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            String task = toDoList.get(i);

            // Add and remove items from selection when list item is selected
            if (b) {
                listAdapter.addToSelection(task);
            } else {
                listAdapter.removeFromSelection(task);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.todo_list_context_menu, menu);

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
                case R.id.action_delete:
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
                case R.id.action_complete:
                    // TODO implement marking tasks as complete, create task class
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
