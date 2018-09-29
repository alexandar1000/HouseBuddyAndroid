package allurosi.housebuddy.shoppingList;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import allurosi.housebuddy.R;

public class ShoppingListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
    }

    public void buttonAddItem() {

//    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, AddShoppingListItemDialogFragment.class);
        startActivity(intent);
    }
}
