package allurosi.housebuddy.householdmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.logging.LogEntry;

public class HouseholdFeedAdapter extends RecyclerView.Adapter<HouseholdFeedAdapter.LogItemViewHolder> {

    private List<LogEntry> mChangeLogs;
    private Context mContext;

    public class LogItemViewHolder extends RecyclerView.ViewHolder {
        public TextView changeLocationTextView;
        public TextView changeInfoTextView;
        public TextView changeDateTextView;

        public LogItemViewHolder(View itemView) {
            super(itemView);

            changeLocationTextView = itemView.findViewById(R.id.change_location);
            changeInfoTextView = itemView.findViewById(R.id.change_info);
            changeDateTextView = itemView.findViewById(R.id.change_date);
        }
    }

    public HouseholdFeedAdapter(List<LogEntry> changeLog) {
        mChangeLogs = changeLog;
    }

    @NonNull
    @Override
    public LogItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Create new view
        View logEntryView = inflater.inflate(R.layout.household_feed_item, parent, false);

        // Return new holder instance
        return new LogItemViewHolder(logEntryView);
    }

    @Override
    public void onBindViewHolder(@NonNull LogItemViewHolder holder, int position) {
        LogEntry logEntry = mChangeLogs.get(position);

        holder.changeLocationTextView.setText(logEntry.getChangeLocation());
        holder.changeInfoTextView.setText(mContext.getResources().getString(R.string.change_info, logEntry.getFullName(), logEntry.getChangeAction()));

        // Format date to string
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(logEntry.getTimeStamp().toDate());
        holder.changeDateTextView.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return mChangeLogs.size();
    }

}
