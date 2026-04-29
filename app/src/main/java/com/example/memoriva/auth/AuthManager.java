package com.example.memoriva.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthManager {

    private static final String WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID";
    private static final String PREFS_NAME = "memoriva_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";

    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;

    private AuthManager(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context.getApplicationContext(), gso);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    /**
     * Register a new user with email and password.
     */
    public void signUpWithEmail(String email, String password, OnCompleteListener<com.google.firebase.auth.AuthResult> listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    /**
     * Sign in an existing user with email and password.
     */
    public void signInWithEmail(String email, String password, OnCompleteListener<com.google.firebase.auth.AuthResult> listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    /**
     * Launch the Google Sign-In intent via the provided ActivityResultLauncher.
     */
    public void signInWithGoogle(ActivityResultLauncher<Intent> launcher) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        launcher.launch(signInIntent);
    }

    /**
     * Process the result returned from the Google Sign-In flow and authenticate with Firebase.
     */
    public void handleGoogleSignInResult(Intent data, OnCompleteListener<com.google.firebase.auth.AuthResult> listener) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(listener);
        } catch (ApiException e) {
            // Wrap the exception so the caller's OnCompleteListener receives a failed task
            com.google.android.gms.tasks.TaskCompletionSource<com.google.firebase.auth.AuthResult> tcs =
                    new com.google.android.gms.tasks.TaskCompletionSource<>();
            tcs.setException(e);
            tcs.getTask().addOnCompleteListener(listener);
        }
    }

    /**
     * Send a password-reset email to the given address.
     */
    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    /**
     * Sign out from both Firebase and Google.
     */
    public void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut();
    }

    /**
     * Returns the currently authenticated FirebaseUser, or null if not signed in.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Returns true if a user is currently signed in.
     */
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Persist basic user info to SharedPreferences.
     */
    public void saveUserToPrefs(Context context, FirebaseUser user) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_ID, user.getUid())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_DISPLAY_NAME, user.getDisplayName())
                .apply();
    }

    /**
     * Remove all stored user info from SharedPreferences.
     */
    public void clearUserFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Returns the Firebase UID stored in SharedPreferences, or null if not found.
     */
    public String getUserIdFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }
}
