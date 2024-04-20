package com.example.Dog_Manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null) {
            login();
        }
    }

    protected void onStop() {
        super.onStop();
        // Sign out the user
        FirebaseAuth.getInstance().signOut();
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    // Choose authentication providers
    private void login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d("LoginActivity", "User signed in: " + user.getDisplayName());
            Log.d("LoginActivity", "User email: " + user.getEmail());
            Log.d("LoginActivity", "User phone: " + user.getPhoneNumber());
            Log.d("LoginActivity", "User UID: " + user.getUid());
            uid = user.getUid();

            if (response != null && response.isNewUser()) {
                // This is a new user
                Log.d("LoginActivity", "This is a new user.");
                //initializeUserDataForNewUser(user.getUid());
            } else {
                Log.d("LoginActivity", "Existing user entered");
            }

            // Intent logic for all users after sign-in
            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            intent.putExtra("userName", user.getDisplayName());
            // Pass "cameFromLogin" to HomePageActivity to show a welcome message
            intent.putExtra("cameFromLogin", "true");
            if(uid != null) {
                intent.putExtra("uid", uid); // Pass the UID to HomePageActivity
                Log.d("LoginActivity", "UID is: " + user.getUid() + " passing to HomePageActivity...");
            } else {
                Log.e("LoginActivity", "UID is null");
            }
            startActivity(intent);
            finish();
        } else {
            // Handle sign-in errors
            handleSignInError(response);
        }
    }

    private void initializeUserDataForNewUser(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        usersRef.setValue(uid);
    }

    private void handleSignInError(IdpResponse response) {
        if (response == null) {
            // User pressed back button
            Log.e("LoginActivity", "Sign-in cancelled by user.");
            return;
        }
        if (response.getError() != null) {
            int errorCode = response.getError().getErrorCode();
            Log.e("LoginActivity", "Sign-in error: " + errorCode);
        }
    }
}

