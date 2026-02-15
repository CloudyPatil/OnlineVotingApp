package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private ProgressBar progressBarSplash;

    private static final String PREFS_NAME = "VotingAppPrefs";
    private static final String KEY_FIRST_RUN = "isFirstRun";
    private static final String KEY_USER_DATA = "user_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Step 1: Find Views
        tvCountdown = findViewById(R.id.tvCountdown);
        progressBarSplash = findViewById(R.id.progressBarSplash);

        // Step 2: Pre-load default users on first run
        loadDefaultUsers();

        // Step 3: Start Countdown Timer (3 seconds)
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;

                // FIX: Use getString with placeholder instead of concatenation
                tvCountdown.setText(getString(R.string.countdown_format, secondsLeft));

                int progress = (int) ((3000 - millisUntilFinished) * 100 / 3000);
                progressBarSplash.setProgress(progress);
            }

            @Override
            public void onFinish() {
                progressBarSplash.setProgress(100);

                // FIX: Use string resource instead of hardcoded text
                tvCountdown.setText(getString(R.string.countdown_go));

                // Explicit Intent → navigate to LoginActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void loadDefaultUsers() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

        if (isFirstRun) {
            String defaultUsers = "admin:admin123,"
                    + "student1:pass1234,"
                    + "student2:pass5678,"
                    + "student3:pass9012,"
                    + "student4:pass3456";

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_DATA, defaultUsers);
            editor.putBoolean(KEY_FIRST_RUN, false);
            editor.putInt("total_votes", 100);
            editor.apply();
        }
    }
}