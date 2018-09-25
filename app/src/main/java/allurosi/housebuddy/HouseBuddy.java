package allurosi.housebuddy;

import android.app.Application;

import allurosi.housebuddy.todolist.ToDoListActivity;

// TODO: REMOVE THIS CLASS IF DB IS DONE
public class HouseBuddy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToDoListActivity.initDummyData();
    }
}
