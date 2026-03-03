package com.example.myapplication;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
        implements AirplaneModeReceiver.AirplaneModeListener {

    private EditText etLoginUsername, etLoginPassword;
    private Button btnLogin;
    private ToggleButton toggleShowPassword, toggleSoundMode;
    private TextView tvGoToSignUp, tvAirplaneStatus;
    private AirplaneModeReceiver airplaneModeReceiver;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        toggleShowPassword = findViewById(R.id.toggleShowPassword);
        toggleSoundMode = findViewById(R.id.toggleSoundMode);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        tvAirplaneStatus = findViewById(R.id.tvAirplaneStatus);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Show/Hide Password
        toggleShowPassword.setOnCheckedChangeListener((btn, isChecked) -> {
            etLoginPassword.setInputType(isChecked
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });

        // Sound Mode Toggle
        if (audioManager != null) {
            toggleSoundMode.setChecked(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL);
        }
        toggleSoundMode.setOnCheckedChangeListener((btn, isChecked) -> {
            if (audioManager == null) return;
            if (isChecked) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Toast.makeText(this, getString(R.string.sound_loud), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null && !nm.isNotificationPolicyAccessGranted()) {
                        Toast.makeText(this, getString(R.string.dnd_permission_needed), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                        toggleSoundMode.setChecked(true);
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        Toast.makeText(this, getString(R.string.sound_silent), Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    Toast.makeText(this, getString(R.string.sound_vibrate), Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkAirplaneMode();

        // LOGIN BUTTON — validates against Firebase
        btnLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            if (username.isEmpty()) {
                etLoginUsername.setError(getString(R.string.error_enter_login_username));
                return;
            }
            if (password.isEmpty()) {
                etLoginPassword.setError(getString(R.string.error_enter_login_password));
                return;
            }

            // Validate against Firebase
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                        return;
                    }

                    User user = snapshot.getValue(User.class);
                    if (user == null || !user.password.equals(password)) {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Login success
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.login_success, username),
                            Toast.LENGTH_SHORT).show();

                    if (user.isAdmin) {
                        // Admin → go to Admin Dashboard
                        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                    } else if (user.hasVoted) {
                        // Already voted → show message
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.already_voted_message),
                                Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        // Normal user → go to Branch Selection
                        Intent intent = new Intent(LoginActivity.this, BranchSelectionActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.firebase_error), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Go to Sign Up
        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void checkAirplaneMode() {
        try {
            boolean isOn = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            updateAirplaneUI(isOn);
        } catch (Exception e) {
            tvAirplaneStatus.setText(getString(R.string.airplane_off));
        }
    }

    private void updateAirplaneUI(boolean isEnabled) {
        if (isEnabled) {
            tvAirplaneStatus.setText(getString(R.string.airplane_on));
            tvAirplaneStatus.setTextColor(getResources().getColor(R.color.error_red));
        } else {
            tvAirplaneStatus.setText(getString(R.string.airplane_off));
            tvAirplaneStatus.setTextColor(getResources().getColor(R.color.success_green));
        }
    }

    @Override
    public void onAirplaneModeChanged(boolean isEnabled) {
        updateAirplaneUI(isEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        airplaneModeReceiver = new AirplaneModeReceiver(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(airplaneModeReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(airplaneModeReceiver, filter);
        }
        checkAirplaneMode();
        if (audioManager != null) {
            toggleSoundMode.setChecked(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (airplaneModeReceiver != null) {
            unregisterReceiver(airplaneModeReceiver);
            airplaneModeReceiver = null;
        }
    }
}