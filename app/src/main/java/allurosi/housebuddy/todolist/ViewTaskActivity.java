package allurosi.housebuddy.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.todolist.ToDoListActivity.TASK_MESSAGE;

public class ViewTaskActivity extends AppCompatActivity {

    private Task mTask;
    private int resultCode = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        Toolbar actionToolBar = findViewById(R.id.toolbar_view_task);
        actionToolBar.setTitle("");
        TextView textTaskDesc = findViewById(R.id.view_task_description);

        setSupportActionBar(actionToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get passed task
        Intent intent = getIntent();
        mTask = intent.getParcelableExtra(TASK_MESSAGE);

        // Set values in layout
        actionToolBar.setTitle(mTask.getName());
        textTaskDesc.setText(mTask.getDescription());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_task, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_complete:
                Toast.makeText(this, "Task marked as completed.", Toast.LENGTH_SHORT).show();
                finish();
                return true;

            case R.id.action_edit:
                return true;

            case R.id.action_delete:
                // TODO: add warning dialog
                Intent returnIntent = new Intent(this, ToDoListActivity.class);
                returnIntent.putExtra(TASK_MESSAGE, mTask);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
