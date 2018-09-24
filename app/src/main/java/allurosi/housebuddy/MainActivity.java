package allurosi.housebuddy;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import allurosi.housebuddy.authentication.LogInActivity;
import allurosi.housebuddy.todolist.ToDoListActivity;

public class MainActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        setContentView(R.layout.log_in_layout);
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
//        Intent intent = new Intent(this, LogInActivity.class);
//        startActivity(intent);
    }

    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

}
