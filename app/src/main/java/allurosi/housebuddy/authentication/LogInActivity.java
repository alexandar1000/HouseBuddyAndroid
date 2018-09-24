package allurosi.housebuddy.authentication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import allurosi.housebuddy.R;

import static android.content.ContentValues.TAG;

public class LogInActivity extends Activity {
    private FirebaseAuth mAuth;

    private EditText mEmailInput;
    private EditText mPasswordInput;
    private TextView mLoggedInUser;

    private Button mLogInButton;
    private Button mSignUpButton;

    private Button mLogOutButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_layout);

        mAuth = FirebaseAuth.getInstance();

        mEmailInput = (EditText) findViewById(R.id.emailInput);
        mPasswordInput = (EditText) findViewById(R.id.passwordInput);

        mLogInButton = (Button) findViewById(R.id.logInButton);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mLogOutButton = (Button) findViewById(R.id.logOutButton);

//        mLogInButton.setVisibility(View.VISIBLE);
//        mSignUpButton.setVisibility(View.VISIBLE);

        mLoggedInUser = (TextView) findViewById(R.id.loggedInUser);


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser();
            }
        });

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutUser();
            }
        });

        populateCurrentUser(mLoggedInUser);

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mLogInButton.setVisibility(View.GONE);
            mSignUpButton.setVisibility(View.GONE);
            populateCurrentUser(mLoggedInUser);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            populateCurrentUser(mLoggedInUser);
        }
    }

    private void signUpUser() {
        String email = mEmailInput.getText().toString();
        String password = mPasswordInput.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void logInUser() {
        String email = mEmailInput.getText().toString();
        String password = mPasswordInput.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void populateCurrentUser(TextView v) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            v.setText(user.getEmail());
        } else {
            v.setText(R.string.userLoggedOut);
        }
    }

    private void logOutUser() {
        mAuth.signOut();
        populateCurrentUser(mLoggedInUser);
        mLogInButton.setVisibility(View.VISIBLE);
        mSignUpButton.setVisibility(View.VISIBLE);
    }


}
