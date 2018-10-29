package allurosi.housebuddy;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

public class HouseBuddy extends MultiDexApplication {

    private static final String TAG = "HouseBuddy";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }
}
