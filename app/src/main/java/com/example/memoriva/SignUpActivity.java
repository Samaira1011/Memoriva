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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.memoriva.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnCreateAccount;
    private TextView tvLoginLink;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        authManager = AuthManager.getInstance(this);

        initViews();
        setupToolbar();
        setupLoginLink();
        setupCreateAccountButton();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupLoginLink() {
        String fullText = "Already have an account? Login";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf("Login");
        int end = start + "Login".length();

        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                navigateToSignIn();
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginLink.setText(spannable);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupCreateAccountButton() {
        btnCreateAccount.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptSignUp() {
        clearErrors();

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (!validateInputs(fullName, email, password, confirmPassword)) {
            return;
        }

        btnCreateAccount.setEnabled(false);

        authManager.signUpWithEmail(email, password, task -> {
            btnCreateAccount.setEnabled(true);
            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().getUser() != null) {

                // Send verification email
                task.getResult().getUser().sendEmailVerification()
                        .addOnCompleteListener(verifyTask -> {
                            // Show confirmation dialog
                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Verify your email")
                                    .setMessage("A verification email has been sent to " + email
                                            + ". Please verify your email before signing in.")
                                    .setPositiveButton("OK", (dialog, which) -> navigateToSignIn())
                                    .setCancelable(false)
                                    .show();
                        });

            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthUserCollisionException) {
                    tilEmail.setError(getString(R.string.error_email_in_use));
                } else {
                    String errorMsg = exception != null ? exception.getMessage()
                            : "Sign up failed. Please try again.";
                    Snackbar.make(btnCreateAccount, errorMsg, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String password, String confirmPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required.");
            valid = false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.length() < 8) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            valid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void navigateToMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignIn() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
