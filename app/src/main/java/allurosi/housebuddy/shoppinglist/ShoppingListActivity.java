package allurosi.housebuddy.shoppinglist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import allurosi.housebuddy.R;

public class ShoppingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        getSupportActionBar().setTitle("Shopping List");

    }
}