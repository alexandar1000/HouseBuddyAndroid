package allurosi.housebuddy.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import allurosi.housebuddy.R;

public class ToDoListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourceId;
    private List<String> toDoList;

    ToDoListAdapter(Context context, int resourceId, List<String> toDoList) {
        super(context, resourceId, toDoList);
        this.mContext = context;
        this.mResourceId = resourceId;
        this.toDoList = toDoList;
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

        ImageButton buttonDeleteFood = convertView.findViewById(R.id.delete_task_button);
        buttonDeleteFood.setVisibility(View.VISIBLE);

        buttonDeleteFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTask(position);
            }
        });

        return convertView;
    }

    private void removeTask(int position) {
        // TODO: maybe add warning
        toDoList.remove(position);
        notifyDataSetChanged();
    }

}

