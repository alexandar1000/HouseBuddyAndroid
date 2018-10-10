package allurosi.housebuddy.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.todolist.AddTaskDialogFragment;

public class ExpensesActivity extends AppCompatActivity implements AddExpenseDialogFragment.NewExpenseDialogListener{
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private List<Product> productList = new ArrayList<>();
    public TextView totalprice;
    private ExpensesListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expenses List");
        }

        ListView productListView = findViewById(R.id.product_list);
        listAdapter = new ExpensesListAdapter(this, R.layout.product_list_item, productList);
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

    private void addTask() {
        AddExpenseDialogFragment addTaskDialogFragment = new AddExpenseDialogFragment();
        addTaskDialogFragment.setListener(this);

        // Hide to do list action bar and fab
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // DialogFragment is removed
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    onCloseNewExpenseDialog();
                }
            }
        });

        // Add DialogFragment with transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, addTaskDialogFragment).addToBackStack(null).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.nut:
                addTask();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCloseNewExpenseDialog() {
        // Hide keyboard after closing the dialog
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Return toolbar and fab
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    @Override
    public void onAddNewExpense(Product newTask) {
        listAdapter.add(newTask);
        totalPrice();
    }

    private void initDummyData() {
        productList.add(new Product("Beer", 10.0));
        productList.add(new Product("Toilet Paper", 4));
        productList.add(new Product("Air softener", 3.5));
        productList.add(new Product("Plate", 2.5));
        productList.add(new Product("Nut",23.60));
        productList.add(new Product("Beer", 10.0));
        productList.add(new Product("Toilet Paper", 4));
        productList.add(new Product("Air softener", 3.5));
        productList.add(new Product("Plate", 2.5));
        productList.add(new Product("Nut",23.60));
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
