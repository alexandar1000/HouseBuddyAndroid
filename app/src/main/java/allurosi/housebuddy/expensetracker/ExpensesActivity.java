package allurosi.housebuddy.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;

public class ExpensesActivity extends AppCompatActivity implements AddExpenseDialogFragment.NewExpenseDialogListener{
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private static List<Product> productList = new ArrayList<>();
    public static TextView totalprice;
    private ExpensesListAdapter listAdapter;


    private static final String LOG_NAME = "ExpensesListActivity";

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    public static final String COLLECTION_PATH_EXPENSES_LIST = "expenses_list";
    private CollectionReference mExpensesListRef;
    private FrameLayout loadingLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expenses List");
        }
        loadingLayout = findViewById(R.id.expenses_list_loading);
        ListView productListView = findViewById(R.id.product_list);
        listAdapter = new ExpensesListAdapter(this, R.layout.product_list_item, productList);


        productListView.setAdapter(listAdapter);
        totalprice = findViewById(R.id.total_price);
        //initDummyData();
        fetchExpenses();
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
    public void onAddNewExpense(final Product newTask) {
        listAdapter.add(newTask);
        mExpensesListRef.add(newTask).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Set auto generated FireStore id as task id
                newTask.setProductId(documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add task to database: " + e);
            }
        });
        totalPrice();
    }

    private void fetchExpenses() {
        // Get stored household path
        String householdPath = PreferenceManager.getDefaultSharedPreferences(this).getString(HOUSEHOLD_PATH, "");

        mExpensesListRef = mFireStore.document(householdPath).collection(COLLECTION_PATH_EXPENSES_LIST);
        mExpensesListRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Add all tasks from DB that are not in the todoList yet to the todoList
                    Product product = document.toObject(Product.class);
                    product.setProductId(document.getId());
                    if (!productList.contains(product)) {
                        productList.add(product);
                    }
                }
                listAdapter.notifyDataSetChanged();
                loadingLayout.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to retrieve expense list collection: " + e);
            }
        });
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

    public static void totalPrice(){
        double total=0;
        for(int i=0;i<productList.size();i++){
            total+=productList.get(i).getPrice();
        }
        totalprice.setText("€"+String.format("%.2f",total));
    }

}
