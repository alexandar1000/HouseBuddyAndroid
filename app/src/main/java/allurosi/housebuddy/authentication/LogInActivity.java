package allurosi.housebuddy.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import allurosi.housebuddy.R;
import allurosi.housebuddy.householdmanager.HouseholdManagerActivity;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    public static final String USER_ID = "userId";
    public static final String USER_EMAIL = "userEmail";
    public static final String FULL_NAME = "full_name";
    private static final String TAG = "LogInActivity";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Choose authentication providers
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo2)                // Set logo drawable
                        .build(),
                RC_SIGN_IN);
    }

    //This class sends the application towards the main part of the app
    private void enterManager(String userId, String userEmail, String fullName) {
        Log.d(TAG, "enterManager()");
        Intent intent = new Intent(this, HouseholdManagerActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_EMAIL, userEmail);
        intent.putExtra(FULL_NAME, fullName);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                assert user != null;
                String userId = user.getUid();
                String userEmail = user.getEmail();
                String fullName = user.getDisplayName();

                enterManager(userId, userEmail, fullName);
                saveUserInfo(userId, userEmail, fullName);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                //TODO: fix this part if the user doesn't complete the sign-in properly
            }
        }
    }

    private void saveUserInfo(String userId, String userEmail, String fullName) {
        Log.d(TAG, "saveUserInfo()");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the user information to device storage
        editor.putString(USER_ID, userId);
        editor.putString(USER_EMAIL, userEmail);
        editor.putString(FULL_NAME, fullName);
        editor.apply();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            String userEmail = user.getEmail();
            String fullName = user.getDisplayName();

            enterManager(userId, userEmail, fullName);
            saveUserInfo(userId, userEmail, fullName);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        // Empty to catch back presses to MainActivity from the login screen
    }

}
