package allurosi.housebuddy.todolist;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

public class ToDoListActivity extends AppCompatActivity implements AddTaskDialogFragment.NewTaskDialogListener {

    List<String> toDoList = new ArrayList<>();
    ToDoListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        // TODO: change to RecyclerView in the future?
        ListView toDoListView = findViewById(R.id.to_do_list);
        FloatingActionButton fab = findViewById(R.id.add_task_fab);

        // TODO: implement description
        listAdapter = new ToDoListAdapter(this, R.layout.to_do_list_item, toDoList);
        toDoListView.setAdapter(listAdapter);

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

}
