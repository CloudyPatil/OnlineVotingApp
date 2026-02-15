package com.example.myapplication;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity
        implements AirplaneModeReceiver.AirplaneModeListener {

    // UI Elements
    private EditText etLoginUsername, etLoginPassword;
    private Button btnLogin;
    private ToggleButton toggleShowPassword, toggleSoundMode;
    private TextView tvGoToSignUp, tvAirplaneStatus;

    // BroadcastReceiver
    private AirplaneModeReceiver airplaneModeReceiver;

    // AudioManager for sound mode
    private AudioManager audioManager;

    // SharedPreferences constants
    private static final String PREFS_NAME = "VotingAppPrefs";
    private static final String KEY_USER_DATA = "user_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ==========================================
        // Step 1: Find all views
        // ==========================================
        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        toggleShowPassword = findViewById(R.id.toggleShowPassword);
        toggleSoundMode = findViewById(R.id.toggleSoundMode);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        tvAirplaneStatus = findViewById(R.id.tvAirplaneStatus);

        // Get AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // ==========================================
        // Step 2: Show/Hide Password Toggle
        // ==========================================
        toggleShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                etLoginPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide password
                etLoginPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Keep cursor at end
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });

        // ==========================================
        // Step 3: Sound Mode Toggle (FIXED - no more crash)
        // ==========================================

        // Set initial toggle state
        if (audioManager != null) {
            int currentMode = audioManager.getRingerMode();
            toggleSoundMode.setChecked(currentMode == AudioManager.RINGER_MODE_NORMAL);
        }

        toggleSoundMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (audioManager == null) return;

            if (isChecked) {
                // Switch to LOUD - this always works, no special permission needed
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Toast.makeText(this, getString(R.string.sound_loud),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Switch to SILENT - needs Do Not Disturb permission on API 24+
                try {
                    // First check if we have Do Not Disturb permission
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (notificationManager != null &&
                            !notificationManager.isNotificationPolicyAccessGranted()) {

                        // We DON'T have permission - ask user to grant it
                        Toast.makeText(this,
                                getString(R.string.dnd_permission_needed),
                                Toast.LENGTH_LONG).show();

                        // Open the permission settings screen
                        Intent intent = new Intent(
                                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);

                        // Reset toggle back to ON (loud) since we couldn't change it
                        toggleSoundMode.setChecked(true);

                    } else {
                        // We HAVE permission - switch to silent
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        Toast.makeText(this, getString(R.string.sound_silent),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    // Fallback: use VIBRATE mode (doesn't need special permission)
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    Toast.makeText(this, getString(R.string.sound_vibrate),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ==========================================
        // Step 4: Check airplane mode
        // ==========================================
        checkAirplaneMode();

        // ==========================================
        // Step 5: Login Button
        // ==========================================
        btnLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            // Validate inputs
            if (username.isEmpty()) {
                etLoginUsername.setError(getString(R.string.error_enter_login_username));
                etLoginUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etLoginPassword.setError(getString(R.string.error_enter_login_password));
                etLoginPassword.requestFocus();
                return;
            }

            // Check credentials
            if (validateLogin(username, password)) {
                Toast.makeText(this,
                        getString(R.string.login_success, username),
                        Toast.LENGTH_SHORT).show();

                // Navigate to BranchSelectionActivity
                Intent intent = new Intent(LoginActivity.this,
                        BranchSelectionActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this,
                        getString(R.string.login_failed),
                        Toast.LENGTH_LONG).show();
            }
        });

        // ==========================================
        // Step 6: Go to Sign Up
        // ==========================================
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Checks username and password against saved data
     */
    private boolean validateLogin(String username, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userData = prefs.getString(KEY_USER_DATA, "");

        if (userData.isEmpty()) {
            return false;
        }

        String[] userPairs = userData.split(",");
        for (String pair : userPairs) {
            String[] credentials = pair.split(":");
            if (credentials.length == 2) {
                String storedUsername = credentials[0].trim();
                String storedPassword = credentials[1].trim();

                if (storedUsername.equals(username) &&
                        storedPassword.equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks current airplane mode status
     */
    private void checkAirplaneMode() {
        try {
            boolean isAirplaneModeOn = Settings.Global.getInt(
                    getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            updateAirplaneUI(isAirplaneModeOn);
        } catch (Exception e) {
            tvAirplaneStatus.setText(getString(R.string.airplane_off));
        }
    }

    /**
     * Updates airplane mode text on screen
     */
    private void updateAirplaneUI(boolean isEnabled) {
        if (isEnabled) {
            tvAirplaneStatus.setText(getString(R.string.airplane_on));
            tvAirplaneStatus.setTextColor(getResources().getColor(R.color.error_red));
        } else {
            tvAirplaneStatus.setText(getString(R.string.airplane_off));
            tvAirplaneStatus.setTextColor(getResources().getColor(R.color.success_green));
        }
    }

    /**
     * Called by AirplaneModeReceiver when airplane mode changes
     */
    @Override
    public void onAirplaneModeChanged(boolean isEnabled) {
        updateAirplaneUI(isEnabled);
    }

    /**
     * Register BroadcastReceiver when screen is visible
     */
    @Override
    protected void onResume() {
        super.onResume();

        airplaneModeReceiver = new AirplaneModeReceiver(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        // API 33+ needs a flag for receiver registration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(airplaneModeReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(airplaneModeReceiver, filter);
        }

        // Refresh airplane mode status
        checkAirplaneMode();

        // Refresh sound toggle state
        if (audioManager != null) {
            int currentMode = audioManager.getRingerMode();
            toggleSoundMode.setChecked(currentMode == AudioManager.RINGER_MODE_NORMAL);
        }
    }

    /**
     * Unregister BroadcastReceiver when leaving screen
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (airplaneModeReceiver != null) {
            unregisterReceiver(airplaneModeReceiver);
            airplaneModeReceiver = null;
        }
    }
}