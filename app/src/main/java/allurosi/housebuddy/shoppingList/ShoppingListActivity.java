package allurosi.housebuddy.shoppingList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import allurosi.housebuddy.R;

public class ShoppingListActivity extends AppCompatActivity implements AddShoppingListItemDialogFragment.AddShoppingItemDialogueListener, AddShoppingItemAdapter.EditShoppingListItemListener {

    private ArrayList<ShoppingItem> shoppingItems = new ArrayList<>();

    private FloatingActionButton mNewItemBtn;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.shopping_list_container);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

//      TODO: retrieve shoping list data from firestore


        /*
        * Just for testing purposes, delete afterwards
        * */
        shoppingItems.add(new ShoppingItem("sponge", "for dishes"));
        shoppingItems.add(new ShoppingItem("dish liquid", "for dishes"));
        shoppingItems.add(new ShoppingItem("food", "for everyone"));



        mAdapter = new AddShoppingItemAdapter(shoppingItems, this);
        mRecyclerView.setAdapter(mAdapter);

        mNewItemBtn = findViewById(R.id.add_shopping_list_item);
    }

    public void addNewItem(View view) {
        FragmentManager fm = getSupportFragmentManager();
        AddShoppingListItemDialogFragment addShoppingListItemDialogFragment = AddShoppingListItemDialogFragment.newInstance("Add Item");
        addShoppingListItemDialogFragment.show(fm, "new_shopping_list_item");
    }

    public void editExistingItem(String name, String info, int position) {
        FragmentManager fm = getSupportFragmentManager();
        AddShoppingListItemDialogFragment addShoppingListItemDialogFragment = AddShoppingListItemDialogFragment.newInstance("Edit Item Item", name, info, position);
        addShoppingListItemDialogFragment.show(fm, "new_shopping_list_item");
    }


    @Override
    public void onFinishAddDialog(ShoppingItem shoppingItem) {
        shoppingItems.add(shoppingItem);
        mAdapter.notifyDataSetChanged();
//      TODO: add the item to the database
    }

    @Override
    public void onFinishEditDialog(ShoppingItem shoppingItem, int position) {
        shoppingItems.get(position).setName(shoppingItem.getName());
        shoppingItems.get(position).setInfo(shoppingItem.getInfo());
        mAdapter.notifyDataSetChanged();
//      TODO: edit item in the database
    }

    @Override
    public void editShoppingListItem(String name, String info, int position, int operation) {
        editExistingItem(name, info, position);
    }
}
