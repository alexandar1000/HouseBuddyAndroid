package allurosi.housebuddy;

import android.support.multidex.MultiDexApplication;

import allurosi.housebuddy.todolist.ToDoListActivity;

// TODO: REMOVE THIS CLASS IF DB IS DONE
public class HouseBuddy extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ToDoListActivity.initDummyData();
    }
}
