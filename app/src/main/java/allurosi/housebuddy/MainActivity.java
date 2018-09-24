package allurosi.housebuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import allurosi.housebuddy.todolist.ToDoListActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonToDoList(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

}
