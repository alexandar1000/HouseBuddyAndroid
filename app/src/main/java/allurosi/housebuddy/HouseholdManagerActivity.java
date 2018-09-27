package allurosi.housebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import allurosi.housebuddy.authentication.LogInActivity;
import allurosi.housebuddy.expensetracker.ExpensesActivity;
import allurosi.housebuddy.expensetracker.ExpensesListAdapter;
import allurosi.housebuddy.todolist.ToDoListActivity;

public class HouseholdManagerActivity extends Activity {
    private FirebaseAuth mAuth;

    private Button mChoreScheduleBtn;
    private Button mToDoListBtn;
    private Button mShoppingListBtn;
    private Button mUtilityReservationBtn;
    private Button mExpenseTrackerBtn;
    private Button mLogOutBtn;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_manager);

        mAuth = FirebaseAuth.getInstance();

        mChoreScheduleBtn = (Button) findViewById(R.id.choreScheduleBtn);
        mToDoListBtn = (Button) findViewById(R.id.toDoListBtn);
        mShoppingListBtn = (Button) findViewById(R.id.shoppingListBtn);
        mUtilityReservationBtn = (Button) findViewById(R.id.utilityReservationBtn);
        mExpenseTrackerBtn = (Button) findViewById(R.id.expenseTrackerBtn);
        mLogOutBtn = (Button) findViewById(R.id.logoutBtn);


        mLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        mToDoListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonToDoList();
            }
        });
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        moveToLogInpage();
                    }
                });
    }

    private void moveToLogInpage() {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }


    public void buttonToDoList() {

//    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

    public void buttonExpensesList(View view) {
        Intent intent = new Intent(this, ExpensesActivity.class);
        startActivity(intent);
    }

}
