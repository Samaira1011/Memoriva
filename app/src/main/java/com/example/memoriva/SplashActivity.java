package com.example.memoriva;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.memoriva.auth.AuthManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 3000L;
    private static final long PROGRESS_DURATION_MS = 2500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Animate progress bar from 0 to 100 over 2500ms
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(PROGRESS_DURATION_MS);
        progressAnimator.start();

        // Disable back navigation on splash screen
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing — back is disabled on splash
            }
        });

        // Navigate after 3000ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthManager authManager = AuthManager.getInstance(SplashActivity.this);
            Intent intent;
            if (authManager.isSignedIn()) {
                intent = new Intent(SplashActivity.this, MapActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
