package allurosi.housebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import allurosi.housebuddy.todolist.ToDoListActivity;

public class HouseholdManagerActivity extends Activity {
    private FirebaseAuth mAuth;

    private Button mChoreScheduleBtn;
    private Button mToDoListBtn;
    private Button mShoppingListBtn;
    private Button mUtilityReservationBtn;
    private Button mExpenseTrackerBtn;
    private Button mLogOutBtn;

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
                mAuth.signOut();
                setContentView(R.layout.log_in_layout);
            }
        });

        mToDoListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonToDoList();
            }
        });
    }

    public void buttonToDoList() {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }
}
