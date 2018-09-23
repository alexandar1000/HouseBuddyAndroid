package allurosi.housebuddy.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import allurosi.housebuddy.R;

public class ToDoListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourceId;
    private List<String> toDoList, selection;

    ToDoListAdapter(Context context, int resourceId, List<String> toDoList) {
        super(context, resourceId, toDoList);
        this.mContext = context;
        this.mResourceId = resourceId;
        this.toDoList = toDoList;
        this.selection = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String task = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
        }

        TextView foodName = convertView.findViewById(R.id.task_name);
        foodName.setText(task);

        CheckBox checkBox = convertView.findViewById(R.id.delete_checkbox);

        // Add tag as identifier
        checkBox.setTag(position);

        if (ToDoListActivity.isActionMode) {
            // Manages adding and removing from selection when an item is directly selected
            // (When notifyDataSetInvalidated is called)
            if (selection.contains(toDoList.get(position))) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            // Add listener and make checkboxes visible in contextual action mode
            checkBox.setOnCheckedChangeListener(checkedChangeListener);
            checkBox.setVisibility(View.VISIBLE);

            // Set contextual action bar title
            ToDoListActivity.mActionMode.setTitle(selection.size() + " items selected");
        } else {
            // Remove and reset checkboxes
            checkBox.setVisibility(View.GONE);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(false);
            checkBox.jumpDrawablesToCurrentState();
        }

        return convertView;
    }

    public void addTask(String taskName) {
        toDoList.add(taskName);
        notifyDataSetChanged();
    }

    private void sortTasks() {
        Collections.sort(toDoList);
    }

    public boolean isSelected(String taskName) {
        return selection.contains(taskName);
    }

    public void addToSelection(String taskName) {
        selection.add(taskName);
        notifyDataSetInvalidated();
    }

    public void removeFromSelection(String taskName) {
        selection.remove(taskName);
        notifyDataSetInvalidated();
    }

    public void clearSelection() {
        selection.clear();
    }

    public void deleteSelected() {
        for (String task : selection) {
            toDoList.remove(task);
        }

        notifyDataSetChanged();
    }

    public void undoRemoval() {
        toDoList.addAll(selection);
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        sortTasks();
    }


    /**
     *  Change listener for the checkBoxes, manages items in the selection
     */
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int position = (int) compoundButton.getTag();
            String selectedTask = toDoList.get(position);

            // Add item if it's selected but not in the selection, vice versa for removing
            if (b) {
                if (!selection.contains(selectedTask)) {
                    selection.add(selectedTask);
                }
            } else {
                selection.remove(selectedTask);
            }

            ToDoListActivity.mActionMode.setTitle(selection.size() + " items selected");
        }
    };

}

