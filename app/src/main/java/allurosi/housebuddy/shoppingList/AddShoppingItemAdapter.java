package allurosi.housebuddy.shoppingList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import allurosi.housebuddy.R;

public class AddShoppingItemAdapter extends RecyclerView.Adapter<AddShoppingItemAdapter.ShoppingItemViewHolder> {

    private ArrayList<ShoppingItem> mItems;

    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;

        public ShoppingItemViewHolder (View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.singleItemShoppingListText);
        }

        public TextView getmTextView() {
            return mTextView;
        }
    }

    public AddShoppingItemAdapter(ArrayList<ShoppingItem> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public AddShoppingItemAdapter.ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.shopping_list_item, viewGroup, false);
        return new ShoppingItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        holder.getmTextView().setText(mItems.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//    }
}
