package allurosi.housebuddy.householdmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import allurosi.housebuddy.R;

public class HouseholdMembersAdapter extends RecyclerView.Adapter<HouseholdMembersAdapter.MemberViewHolder> {

    private LayoutInflater mInflater;
    private List<String> memberNames;

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView memberNameTextView;

        public MemberViewHolder(View itemView) {
            super(itemView);

            memberNameTextView = itemView.findViewById(R.id.member_name);
        }
    }

    public HouseholdMembersAdapter(Context context, List<String> memberNames) {
        mInflater = LayoutInflater.from(context);
        this.memberNames = memberNames;
    }

    @NonNull
    @Override
    public HouseholdMembersAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // Create new view
        View memberView = mInflater.inflate(R.layout.household_member_item, parent, false);

        // Return new holder instance
        return new HouseholdMembersAdapter.MemberViewHolder(memberView);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseholdMembersAdapter.MemberViewHolder holder, int position) {
        String memberName = memberNames.get(position);

        holder.memberNameTextView.setText(memberName);
    }

    @Override
    public int getItemCount() {
        return memberNames.size();
    }

}
