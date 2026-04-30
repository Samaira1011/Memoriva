package com.example.memoriva;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.memoriva.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private MaterialButton btnGoogle;
    private TextView tvForgotPassword;
    private TextView tvSignUpLink;

    private AuthManager authManager;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    authManager.handleGoogleSignInResult(result.getData(), task -> {
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().getUser() != null) {
                            authManager.saveUserToPrefs(this, task.getResult().getUser());
                            navigateToMap();
                        } else {
                            String errorMsg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Google sign-in failed. Please try again.";
                            Snackbar.make(btnSignIn, errorMsg, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        authManager = AuthManager.getInstance(this);

        initViews();
        setupForgotPassword();
        setupSignUpLink();
        setupSignInButton();
        setupGoogleButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authManager.isSignedIn()) {
            navigateToMap();
        }
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
    }

    private void setupForgotPassword() {
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void setupSignUpLink() {
        String fullText = "Don't have an account? Sign Up";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf("Sign Up");
        int end = start + "Sign Up".length();

        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignUpLink.setText(spannable);
        tvSignUpLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupSignInButton() {
        btnSignIn.setOnClickListener(v -> attemptSignIn());
    }

    private void setupGoogleButton() {
        btnGoogle.setOnClickListener(v -> authManager.signInWithGoogle(googleSignInLauncher));
    }

    private void attemptSignIn() {
        clearErrors();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required.");
            valid = false;
        }

        if (!valid) return;

        btnSignIn.setEnabled(false);

        authManager.signInWithEmail(email, password, task -> {
            btnSignIn.setEnabled(true);
            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().getUser() != null) {

                // Check email verification
                if (!task.getResult().getUser().isEmailVerified()) {
                    // Sign out and prompt to verify
                    authManager.signOut();
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Email not verified")
                            .setMessage("Please verify your email before signing in. Check your inbox for the verification link.")
                            .setPositiveButton("Resend Email", (dialog, which) -> {
                                task.getResult().getUser().sendEmailVerification();
                                Snackbar.make(btnSignIn, "Verification email resent.", Snackbar.LENGTH_LONG).show();
                            })
                            .setNegativeButton("OK", null)
                            .show();
                    return;
                }

                authManager.saveUserToPrefs(this, task.getResult().getUser());
                navigateToMap();
            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthInvalidCredentialsException
                        || exception instanceof FirebaseAuthInvalidUserException) {
                    Snackbar.make(btnSignIn, getString(R.string.error_invalid_credentials),
                            Snackbar.LENGTH_LONG).show();
                } else {
                    // Likely a network error
                    new AlertDialog.Builder(this)
                            .setTitle("Connection Error")
                            .setMessage(getString(R.string.error_network))
                            .setPositiveButton("Retry", (dialog, which) -> attemptSignIn())
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void navigateToMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
