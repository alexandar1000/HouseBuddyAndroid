package allurosi.housebuddy.todolist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import allurosi.housebuddy.R;

import static android.view.Window.FEATURE_NO_TITLE;

public class AddTaskDialogFragment extends DialogFragment {

    private Task newTask;

    private NewTaskDialogListener listener;

    public interface NewTaskDialogListener {
        void onFinishNewTaskDialog(Task newTask);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);
        getDialog().requestWindowFeature(FEATURE_NO_TITLE);

        final EditText newTaskNameInput = view.findViewById(R.id.new_task_name);
        final EditText newTaskDescInput = view.findViewById(R.id.new_task_description);
        Button cancelButton = view.findViewById(R.id.button_cancel);
        Button createButton = view.findViewById(R.id.button_create);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createTask(newTaskNameInput.getText().toString(), newTaskDescInput.getText().toString())) {
                    // Notify the listener that a new task has to be added
                    listener.onFinishNewTaskDialog(newTask);
                    dismiss();
                }
            }
        });

        return view;
    }

    public void setListener(ToDoListActivity parent) {
        listener = (NewTaskDialogListener) parent;
    }

    private boolean createTask(String newTaskName, String newTaskDescription) {
        // Notify the user if no name is supplied before pressing create
        if (newTaskName.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        newTask = new Task(newTaskName);
        newTask.setDescription(newTaskDescription);

        return true;
    }

}
