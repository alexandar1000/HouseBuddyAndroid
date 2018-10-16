package allurosi.housebuddy.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.logging.LogEntry;
import allurosi.housebuddy.logging.Loggable;

import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_FIRST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.FIELD_LAST_NAME;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.USERS_COLLECTION_PATH;
import static allurosi.housebuddy.todolist.ToDoListActivity.COLLECTION_PATH_CHANGE_LOG;

public class ExpensesActivity extends AppCompatActivity implements AddExpenseDialogFragment.NewExpenseDialogListener, Loggable {

    private static List<Product> productList = new ArrayList<>();
    private ExpensesListAdapter listAdapter;

    private static final String LOG_NAME = "ExpensesListActivity";
    public static final String COLLECTION_PATH_EXPENSES_LIST = "expenses_list";
    private static final String LOCATION_EXPENSE_LIST = "Expenses List";

    private String mUserId;
    private String mFirstName, mLastName;

    private FrameLayout loadingLayout;
    private TextView totalPrice;

    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private static CollectionReference mExpensesListRef;
    private CollectionReference mChangeLogRef;

    private ListenerRegistration mListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        if (mUserId == null) {
            mUserId = PreferenceManager.getDefaultSharedPreferences(this).getString(USER_ID, "");
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expenses List");
        }

        loadingLayout = findViewById(R.id.expenses_list_loading);
        ListView productListView = findViewById(R.id.product_list);
        listAdapter = new ExpensesListAdapter(this, R.layout.product_list_item, productList);

        productListView.setAdapter(listAdapter);
        totalPrice = findViewById(R.id.total_price);

        fetchExpenses();
        totalPrice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_expenses, menu);

        return true;
    }

    private void addExpense() {
        AddExpenseDialogFragment addExpenseDialogFragment = new AddExpenseDialogFragment();
        addExpenseDialogFragment.setListener(this);

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
        transaction.add(android.R.id.content, addExpenseDialogFragment).addToBackStack(null).commit();
    }

    public void logChange(final int changeActionStringResource) {
        if (mFirstName == null || mLastName == null) {
            mFireStore.collection(USERS_COLLECTION_PATH).document(mUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mFirstName = documentSnapshot.getString(FIELD_FIRST_NAME);
                    mLastName = documentSnapshot.getString(FIELD_LAST_NAME);

                    // Add change log containing the change location, change info and a timestamp
                    LogEntry logEntry = new LogEntry(LOCATION_EXPENSE_LIST, getResources().getString(changeActionStringResource), mFirstName + " " + mLastName, new Timestamp(new Date()));
                    mChangeLogRef.add(logEntry).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_NAME, "Failed to add log entry: " + e);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_NAME, "Failed to retrieve name for logging: " + e);
                }
            });
        } else {
            LogEntry logEntry = new LogEntry(LOCATION_EXPENSE_LIST, getResources().getString(changeActionStringResource), mFirstName + " " + mLastName, new Timestamp(new Date()));
            mChangeLogRef.add(logEntry).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_NAME, "Failed to add log entry: " + e);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.nut:
                addExpense();
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
    public void onAddNewExpense(final Product newProduct) {
        listAdapter.add(newProduct);
        mExpensesListRef.add(newProduct).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                logChange(R.string.action_added_product);

                newProduct.setProductId(documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add Expense to database: " + e);
            }
        });
        totalPrice();
    }

    private void fetchExpenses() {
        // Get stored household path
        String householdPath = PreferenceManager.getDefaultSharedPreferences(this).getString(HOUSEHOLD_PATH, "");
        DocumentReference householdRef = mFireStore.document(householdPath);

        mChangeLogRef = householdRef.collection(COLLECTION_PATH_CHANGE_LOG);
        mExpensesListRef = householdRef.collection(COLLECTION_PATH_EXPENSES_LIST);

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

    @Override
    public void onStart() {
        super.onStart();

        // Add listener which updates the invitation code if another user changes it
        mListenerRegistration = mExpensesListRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    List<Product> newProductList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setProductId(document.getId());
                        newProductList.add(product);
                    }
                    productList.clear();
                    productList.addAll(newProductList);
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Detach listener when it's no longer needed
        mListenerRegistration.remove();
    }

    public void remove(Product product){
        mExpensesListRef.document(product.getProductId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logChange(R.string.action_removed_product);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to delete Expense from database: " + e);
            }
        });
    }

    public void totalPrice(){
        double total = 0;
        for (int i = 0; i < productList.size(); i++) {
            total += productList.get(i).getPrice();
        }
        totalPrice.setText("€"+String.format("%.2f",total));
    }

}
