package allurosi.housebuddy.shoppingList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

import allurosi.housebuddy.R;

public class ShoppingListActivity extends AppCompatActivity {

    private ArrayList<ShoppingItem> shoppingItems = new ArrayList<>();

    private FloatingActionButton mNewItemBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

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
}
