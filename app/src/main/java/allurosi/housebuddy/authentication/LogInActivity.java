package allurosi.housebuddy.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import allurosi.housebuddy.householdmanager.HouseholdManagerActivity;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    public static final String USER_ID = "userId";
    public static final String USER_EMAIL = "userEmail";

    private FirebaseAuth mAuth;

    private EditText mEmailInput;
    private EditText mPasswordInput;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    // Choose authentication providers
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.log_in_layout);

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
//                        .setLogo(R.drawable.my_great_logo)      // Set logo drawable
//                        .setTheme(R.style.MySuperAppTheme)      // Set theme
                        .build(),
                RC_SIGN_IN);
    }

    private void enterManager(String userId, String userEmail) {
        Intent intent = new Intent(this, HouseholdManagerActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_EMAIL, userEmail);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                enterManager(user.getUid(), user.getEmail());
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

}
