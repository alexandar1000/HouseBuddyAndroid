package allurosi.housebuddy.shoppingList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import allurosi.housebuddy.R;

public class ShoppingListFragment extends Fragment implements AddShoppingListItemDialogFragment.AddShoppingListItemListener,
        AddShoppingItemAdapter.ShowShoppingListItemListener, ViewShoppingListItemFragment.ViewShoppingListItemFragmentListener {

    private Context mContext;
    private ActionBar mActionBar;

    private ArrayList<ShoppingItem> shoppingItems = new ArrayList<>();

    private FloatingActionButton mNewItemBtn;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        mRecyclerView = rootView.findViewById(R.id.shopping_list_container);

        // Add dividers between recyclerView items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

//      TODO: retrieve shoping list data from firestore

        /*
         * Just for testing purposes, delete afterwards
         * */
        shoppingItems.clear();
        shoppingItems.add(new ShoppingItem("sponge", "for dishes"));
        shoppingItems.add(new ShoppingItem("dish liquid", "for dishes"));
        shoppingItems.add(new ShoppingItem("food", "for everyone"));


        mAdapter = new AddShoppingItemAdapter(shoppingItems, this);
        mRecyclerView.setAdapter(mAdapter);

        mNewItemBtn = rootView.findViewById(R.id.add_shopping_list_item);

        mNewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
            }
        });

        return rootView;
    }

    public void addNewItem() {
        AddShoppingListItemDialogFragment addShoppingListItemDialogFragment = new AddShoppingListItemDialogFragment();
        addShoppingListItemDialogFragment.setListener(this);

        updateActionBar("Add Item", R.drawable.ic_arrow_back_white);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.flContent, addShoppingListItemDialogFragment).addToBackStack(null).commit();
    }

    private void updateActionBar(String title, int backDrawableId) {
        mActionBar.setTitle(title);
        mActionBar.setHomeAsUpIndicator(backDrawableId);
    }

    @Override
    public void onFinishAddShoppingListItem(ShoppingItem shoppingItem) {
        shoppingItems.add(shoppingItem);
//      TODO: add the item to the database
    }

    @Override
    public void onDeleteShoppingItem(int itemToDelete) {
        shoppingItems.remove(itemToDelete);
    }

    @Override
    public void onEditShoppingItem(ShoppingItem editedItem, int position, ViewShoppingListItemFragment fragment) {
        shoppingItems.set(position, editedItem);
        for (ShoppingItem item :shoppingItems) {
            System.out.println(item.getName());
        }
        mAdapter.notifyDataSetChanged();
        //TODO: Update in the DB
    }

    @Override
    public void onCloseViewShoppingItem() {
        updateActionBar(mContext.getResources().getString(R.string.app_name), R.drawable.ic_menu_white);
        getFragmentManager().popBackStack();
    }

    @Override
    public void onCloseNewShoppingItemDialog() {
        updateActionBar(mContext.getResources().getString(R.string.app_name), R.drawable.ic_menu_white);
    }

    @Override
    public void showShoppingListItem(String name, String info, int position) {
        ViewShoppingListItemFragment viewShoppingListItemFragment = ViewShoppingListItemFragment.newInstance("Edit Item Item", name, info, position);
        viewShoppingListItemFragment.setListener(this);

        updateActionBar("Shopping Item", R.drawable.ic_arrow_back_white);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.flContent, viewShoppingListItemFragment).addToBackStack(null).commit();
    }

    public ArrayList<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    // Deprecated method to support lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mActionBar = ((AppCompatActivity) activity).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActionBar = ((AppCompatActivity) context).getSupportActionBar();
    }
}
