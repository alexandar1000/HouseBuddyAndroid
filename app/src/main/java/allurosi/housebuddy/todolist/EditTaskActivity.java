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
import android.widget.EditText;
import android.widget.Toast;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.todolist.ToDoListActivity.TASK_MESSAGE;
import static allurosi.housebuddy.todolist.ViewTaskActivity.TASK_MESSAGE_ORIGINAL;

public class EditTaskActivity extends AppCompatActivity {

    private Task originalTask;
    private Task mTask;

    private EditText textTaskName;
    private EditText textTaskDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Toolbar actionToolBar = findViewById(R.id.toolbar_edit_task);
        actionToolBar.setTitle("");
        textTaskName = findViewById(R.id.edit_task_name);
        textTaskDesc = findViewById(R.id.edit_task_description);

        // Add back button to toolbar
        setSupportActionBar(actionToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);

        // Get passed task
        Intent intent = getIntent();
        originalTask = intent.getParcelableExtra(TASK_MESSAGE);
        mTask = new Task(originalTask);

        // Set values in layout
        textTaskName.setText(mTask.getName());
        textTaskDesc.setText(mTask.getDescription());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_task, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_save:
                Intent returnIntent = new Intent(EditTaskActivity.this, ViewTaskActivity.class);

                // Get new values
                String newName = textTaskName.getText().toString();
                String newDesc = textTaskDesc.getText().toString();

                // Only change the name and description if they are not empty
                if (newName.equals("")) {
                    Toast.makeText(EditTaskActivity.this, "Please enter a name.", Toast.LENGTH_SHORT).show();
                } else {
                    mTask.setName(newName);

                    if (!newDesc.equals("")) {
                        mTask.setDescription(newDesc);
                    }

                    returnIntent.putExtra(TASK_MESSAGE, mTask);
                    returnIntent.putExtra(TASK_MESSAGE_ORIGINAL, originalTask);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        String newName = textTaskName.getText().toString();
        String newDesc = textTaskDesc.getText().toString();

        mTask.setName(newName);
        if (!newDesc.equals("")) {
            mTask.setDescription(newDesc);
        }

        if (mTask.equals(originalTask)) {
            super.onBackPressed();
        } else {
            showDiscardWarning();
        }
    }

    private void showDiscardWarning() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder = new AlertDialog.Builder(EditTaskActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(EditTaskActivity.this);
        }

        builder.setMessage("Discard changes?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

}
