package allurosi.housebuddy.expensetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

public class ExpensesActivity extends AppCompatActivity {
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private List<Product> productList = new ArrayList<>();
    public TextView totalprice;
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
        totalprice = findViewById(R.id.total_price);
        initDummyData();

        totalPrice();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_expenses, menu);

        return true;
    }

    private void initDummyData() {
        productList.add(new Product("Beer", 10.0));
        productList.add(new Product("Toilet Paper", 4));
        productList.add(new Product("Air softener", 3.5));
        productList.add(new Product("Plate", 2.5));
        productList.add(new Product("Nut",23.60));
    }

    private void totalPrice(){
        double total=0;
        for(int i=0;i<productList.size();i++){
            total+=productList.get(i).getPrice();
        }
        totalprice.setText("€"+String.format("%.2f",total));
    }

}
