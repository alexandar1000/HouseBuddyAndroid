package allurosi.housebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import allurosi.housebuddy.authentication.LogInActivity;
import allurosi.housebuddy.expensetracker.ExpensesActivity;
import allurosi.housebuddy.todolist.ToDoListActivity;

public class HouseholdManagerActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_manager);
    }

    public void signOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HouseholdManagerActivity.this, LogInActivity.class);
                        startActivity(intent);
                    }
                });
    }

    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

    public void buttonExpensesList(View view) {
        Intent intent = new Intent(this, ExpensesActivity.class);
        startActivity(intent);
    }

}
