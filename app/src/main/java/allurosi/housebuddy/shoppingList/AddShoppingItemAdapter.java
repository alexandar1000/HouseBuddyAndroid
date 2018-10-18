package allurosi.housebuddy.shoppingList;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import allurosi.housebuddy.R;

public class AddShoppingItemAdapter extends RecyclerView.Adapter<AddShoppingItemAdapter.ShoppingItemViewHolder> {

    private ArrayList<ShoppingItem> mItems;
    private ShoppingListActivity shoppingListActivity;

    public interface EditShoppingListItemListener {
        void editShoppingListItem(String name, String info, int position);
    }


    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;
        public final ImageButton mImageButton;

        public ShoppingItemViewHolder (View v, final ShoppingListActivity shoppingListActivity) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.singleItemShoppingListText);
            mImageButton = (ImageButton) v.findViewById(R.id.shoppingListCheckButton);

            mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTextView.setPaintFlags(mTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            });

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditShoppingListItemListener listener = (EditShoppingListItemListener) shoppingListActivity;
                    String name = shoppingListActivity.getShoppingItems().get(getAdapterPosition()).getName();
                    String info = shoppingListActivity.getShoppingItems().get(getAdapterPosition()).getInfo();
                    listener.editShoppingListItem(name, info, getAdapterPosition());
                }
            });
        }

        public TextView getmTextView() {
            return mTextView;
        }
    }

    public AddShoppingItemAdapter(ArrayList<ShoppingItem> items, ShoppingListActivity shoppingListActivity) {
        this.mItems = items;
        this.shoppingListActivity = shoppingListActivity;
    }

    @NonNull
    @Override
    public AddShoppingItemAdapter.ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.shopping_list_item, viewGroup, false);
        return new ShoppingItemViewHolder(v, shoppingListActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull final ShoppingItemViewHolder holder, final int position) {
        holder.getmTextView().setText(mItems.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


}
