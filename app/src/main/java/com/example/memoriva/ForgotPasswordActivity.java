package com.example.memoriva;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;
    private TextView tvBackToSignIn;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authManager = AuthManager.getInstance(this);

        initViews();
        setupToolbar();
        setupResetButton();
        setupBackToSignIn();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToSignIn = findViewById(R.id.tvBackToSignIn);
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

    private void setupResetButton() {
        btnResetPassword.setOnClickListener(v -> attemptPasswordReset());
    }

    private void setupBackToSignIn() {
        tvBackToSignIn.setOnClickListener(v -> finish());
    }

    private void attemptPasswordReset() {
        tilEmail.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            return;
        }

        btnResetPassword.setEnabled(false);

        authManager.sendPasswordResetEmail(email, task -> {
            btnResetPassword.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthInvalidUserException) {
                    tilEmail.setError("No account found with this email address.");
                } else {
                    Snackbar.make(btnResetPassword,
                            getString(R.string.error_network),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
