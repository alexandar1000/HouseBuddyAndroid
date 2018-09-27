package allurosi.housebuddy.expensetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

public class ExpensesActivity extends AppCompatActivity {

    private List<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expenses List");
        }

        ListView productListView = findViewById(R.id.product_list);
        ExpensesListAdapter listAdapter = new ExpensesListAdapter(this, R.layout.product_list_item, productList);
        productListView.setAdapter(listAdapter);

        initDummyData();
    }

    private void initDummyData() {
        productList.add(new Product("Beer", 10.0));
        productList.add(new Product("Toilet Paper", 4));
        productList.add(new Product("Air softener", 3.5));
        productList.add(new Product("Plate", 2.5));
    }

}
