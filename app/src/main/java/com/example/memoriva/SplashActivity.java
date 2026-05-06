package com.example.memoriva;

import androidx.activity.EdgeToEdge;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

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
