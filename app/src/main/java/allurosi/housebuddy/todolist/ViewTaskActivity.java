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

import static allurosi.housebuddy.todolist.ToDoListActivity.RESULT_DELETE;
import static allurosi.housebuddy.todolist.ToDoListActivity.RESULT_EDIT;
import static allurosi.housebuddy.todolist.ToDoListActivity.TASK_MESSAGE;

public class ViewTaskActivity extends AppCompatActivity {

    public static final String TASK_MESSAGE_ORIGINAL = "OriginalTask";

    public static final int EDIT_TASK = 1;

    private Task mTask;
    private Task originalTask;
    private int mResultCode;

    private Toolbar actionToolBar;
    private TextView textTaskDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        actionToolBar = findViewById(R.id.toolbar_view_task);
        actionToolBar.setTitle("");
        textTaskDesc = findViewById(R.id.view_task_description);

        // Add back button to toolbar
        setSupportActionBar(actionToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get passed task
        Intent intent = getIntent();
        mTask = intent.getParcelableExtra(TASK_MESSAGE);

        // Set values in layout
        actionToolBar.setTitle(mTask.getTaskName());
        textTaskDesc.setText(mTask.getTaskDesc());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_TASK) {
            if (resultCode == RESULT_OK) {
                mTask = data.getParcelableExtra(TASK_MESSAGE);
                originalTask = data.getParcelableExtra(TASK_MESSAGE_ORIGINAL);
                actionToolBar.setTitle(mTask.getTaskName());
                textTaskDesc.setText(mTask.getTaskDesc());

                Intent returnIntent = new Intent(ViewTaskActivity.this, ToDoListActivity.class);
                returnIntent.putExtra(TASK_MESSAGE, mTask);
                returnIntent.putExtra(TASK_MESSAGE_ORIGINAL, originalTask);
                setResult(RESULT_EDIT, returnIntent);
                mResultCode = RESULT_EDIT;

                Toast.makeText(this, getString(R.string.task_saved), Toast.LENGTH_SHORT).show();
            }
        }
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
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_complete:
                originalTask = new Task(mTask);
                mTask.setIsCompleted(true);

                Intent returnIntent = new Intent(ViewTaskActivity.this, ToDoListActivity.class);
                returnIntent.putExtra(TASK_MESSAGE, mTask);
                returnIntent.putExtra(TASK_MESSAGE_ORIGINAL, originalTask);
                setResult(RESULT_EDIT, returnIntent);
                mResultCode = RESULT_EDIT;

                Toast.makeText(this, getResources().getQuantityString(R.plurals.task_marked_completed, 1), Toast.LENGTH_SHORT).show();
                finish();
                return true;

            case R.id.action_edit:
                Intent intent = new Intent(ViewTaskActivity.this, EditTaskActivity.class);
                intent.putExtra(TASK_MESSAGE, mTask);
                startActivityForResult(intent, EDIT_TASK);
                return true;

            case R.id.action_delete:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    builder = new AlertDialog.Builder(ViewTaskActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ViewTaskActivity.this);
                }

                builder.setMessage(getResources().getQuantityString(R.plurals.delete_task_question, 1))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Set result to RESULT_DELETE and add intent with task
                        Intent returnIntent = new Intent(ViewTaskActivity.this, ToDoListActivity.class);
                        if (mResultCode == RESULT_EDIT) {
                            returnIntent.putExtra(TASK_MESSAGE, originalTask);
                            setResult(RESULT_DELETE, returnIntent);
                            mResultCode = RESULT_DELETE;
                        } else {
                            returnIntent.putExtra(TASK_MESSAGE, mTask);
                            setResult(RESULT_DELETE, returnIntent);
                            mResultCode = RESULT_DELETE;
                        }
                        finish();
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

}
