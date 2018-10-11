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

public class ShoppingListActivity extends AppCompatActivity implements AddShoppingListItemDialogFragment.AddShoppingItemDialogueListener {

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

        mAdapter = new AddShoppingItemAdapter(shoppingItems);
        mRecyclerView.setAdapter(mAdapter);

        mNewItemBtn = findViewById(R.id.add_shopping_list_item);




        mNewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
            }
        });
    }

    public void addNewItem() {
        FragmentManager fm = getSupportFragmentManager();
        AddShoppingListItemDialogFragment addShoppingListItemDialogFragment = AddShoppingListItemDialogFragment.newInstance("Add Item");
        addShoppingListItemDialogFragment.show(fm, "new_shopping_list_item");
    }

    @Override
    public void onFinishEditDialog(ShoppingItem shoppingItem) {
        shoppingItems.add(shoppingItem);
//      TODO: add the item to the database
    }


}
