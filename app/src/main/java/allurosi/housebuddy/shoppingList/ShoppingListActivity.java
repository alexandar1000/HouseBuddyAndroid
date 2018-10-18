package allurosi.housebuddy.shoppingList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import allurosi.housebuddy.R;

import static allurosi.housebuddy.authentication.LogInActivity.USER_ID;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.HOUSEHOLD_PATH;
import static allurosi.housebuddy.householdmanager.HouseholdManagerActivity.LOG_NAME;

public class ShoppingListActivity extends AppCompatActivity implements AddShoppingListItemDialogFragment.AddShoppingItemDialogueListener, AddShoppingItemAdapter.EditShoppingListItemListener {

    private ArrayList<ShoppingItem> shoppingItems = new ArrayList<>();

    public static final String COLLECTION_PATH_SHOPPING_LIST = "shopping_list";
    public static final String COLLECTION_PATH_CHANGE_LOG = "change_log";
    public static final String LOCATION_SHOPPING_LIST = "Shopping List";

    private FloatingActionButton mNewItemBtn;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String mUserId;
    private FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
    private CollectionReference mShoppingListListRef;
    private CollectionReference mChangeLogRef;

    private SharedPreferences mSharedPreferences;

    private ListenerRegistration mListenerRegistration;

    private String householdPath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.shopping_list_container);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

//      TODO: retrieve shoping list data from firestore

        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID);

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


    @Override
    public void onStart() {
        super.onStart();

        // Get stored household path
        String householdPath = mSharedPreferences.getString(HOUSEHOLD_PATH, "");
        DocumentReference householdRef = mFireStore.document(householdPath);

        mChangeLogRef = householdRef.collection(COLLECTION_PATH_CHANGE_LOG);
        mShoppingListListRef = householdRef.collection(COLLECTION_PATH_SHOPPING_LIST);

        // Add listener which updates the todoList if another user changes it
        // Is called once when addSnapshotListener is called
        mListenerRegistration = mShoppingListListRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_NAME, "Listen error: ", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<ShoppingItem> newShoppingList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ShoppingItem shoppingItem = document.toObject(ShoppingItem.class);
                        shoppingItem.setShoppingItemId(document.getId());
                        shoppingItems.add(shoppingItem);
                    }
                    shoppingItems.clear();
                    shoppingItems.addAll(newShoppingList);
                    mAdapter.notifyDataSetChanged();
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
    public void onFinishAddDialog(final ShoppingItem shoppingItem) {
        shoppingItems.add(shoppingItem);
        mAdapter.notifyDataSetChanged();
//      TODO: add the item to the database

        // Add new task to database
        mShoppingListListRef.add(shoppingItem).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Set auto generated FireStore id as task id
                shoppingItem.setShoppingItemId(documentReference.getId());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(LOG_NAME, "Failed to add task to database: " + e);
            }
        });


    }

    @Override
    public void onFinishEditDialog(ShoppingItem shoppingItem, int position) {
        shoppingItems.get(position).setName(shoppingItem.getName());
        shoppingItems.get(position).setInfo(shoppingItem.getInfo());
        mAdapter.notifyDataSetChanged();
//      TODO: edit item in the database
    }

    @Override
    public void editShoppingListItem(String name, String info, int position) {
        editExistingItem(name, info, position);
    }

    public ArrayList<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }
}
