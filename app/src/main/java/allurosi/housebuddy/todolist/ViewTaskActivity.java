package allurosi.housebuddy.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        Toolbar actionToolBar = findViewById(R.id.toolbar_view_task);
        actionToolBar.setTitle("");
        TextView textTaskDesc = findViewById(R.id.view_task_description);

        // Add back button to toolbar
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
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    builder = new AlertDialog.Builder(ViewTaskActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ViewTaskActivity.this);
                }

                builder.setMessage("Delete this task?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Set result to RESULT_OK and add intent with task
                        Intent returnIntent = new Intent(ViewTaskActivity.this, ToDoListActivity.class);
                        returnIntent.putExtra(TASK_MESSAGE, mTask);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
