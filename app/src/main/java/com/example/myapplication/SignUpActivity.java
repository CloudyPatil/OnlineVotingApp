package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    // UI Elements
    private EditText etFullName, etUsername, etPassword;
    private TextView tvSelectedDate, tvSelectedTime;
    private Button btnPickDate, btnPickTime, btnSignUp;
    private TextView tvGoToLogin;

    // Variables to store selected date and time
    private String selectedDate = "";
    private String selectedTime = "";

    // SharedPreferences constants
    private static final String PREFS_NAME = "VotingAppPrefs";
    private static final String KEY_USER_DATA = "user_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // ==========================================
        // Step 1: Find all views by their IDs
        // ==========================================
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // ==========================================
        // Step 2: MODULE 8 — DatePicker
        // When user clicks "Pick" button, a calendar dialog opens
        // ==========================================
        btnPickDate.setOnClickListener(v -> {
            // Get today's date as default
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create and show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignUpActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Month is 0-indexed so add 1
                        selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        tvSelectedDate.setText(selectedDate);
                        tvSelectedDate.setTextColor(getResources().getColor(R.color.black));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // ==========================================
        // Step 3: MODULE 9 — TimePicker
        // When user clicks "Pick" button, a clock dialog opens
        // ==========================================
        btnPickTime.setOnClickListener(v -> {
            // Get current time as default
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create and show TimePickerDialog
            // false = 12-hour format with AM/PM
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    SignUpActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        // Convert to 12-hour format
                        String amPm;
                        int displayHour;
                        if (selectedHour >= 12) {
                            amPm = "PM";
                            displayHour = (selectedHour == 12) ? 12 : selectedHour - 12;
                        } else {
                            amPm = "AM";
                            displayHour = (selectedHour == 0) ? 12 : selectedHour;
                        }
                        selectedTime = String.format("%02d:%02d %s",
                                displayHour, selectedMinute, amPm);
                        tvSelectedTime.setText(selectedTime);
                        tvSelectedTime.setTextColor(getResources().getColor(R.color.black));
                    },
                    hour, minute, false
            );
            timePickerDialog.show();
        });

        // ==========================================
        // Step 4: MODULE 1 — Button (Sign Up)
        // Validates all inputs and saves new user
        // ==========================================
        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // --- Validation checks one by one ---
            if (fullName.isEmpty()) {
                etFullName.setError(getString(R.string.error_enter_fullname));
                etFullName.requestFocus();
                return;
            }
            if (username.isEmpty()) {
                etUsername.setError(getString(R.string.error_enter_username));
                etUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.error_enter_password));
                etPassword.requestFocus();
                return;
            }
            if (password.length() < 4) {
                etPassword.setError(getString(R.string.error_password_short));
                etPassword.requestFocus();
                return;
            }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_dob),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_time),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Check if username already exists ---
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String existingUsers = prefs.getString(KEY_USER_DATA, "");

            if (!existingUsers.isEmpty()) {
                String[] userPairs = existingUsers.split(",");
                for (String pair : userPairs) {
                    String[] credentials = pair.split(":");
                    if (credentials.length >= 1 && credentials[0].equals(username)) {
                        Toast.makeText(this, getString(R.string.error_username_exists),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            // --- Save new user to SharedPreferences ---
            String newUserData;
            if (existingUsers.isEmpty()) {
                newUserData = username + ":" + password;
            } else {
                newUserData = existingUsers + "," + username + ":" + password;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_DATA, newUserData);
            editor.apply();

            // --- Show success message ---
            Toast.makeText(this, getString(R.string.signup_success, fullName),
                    Toast.LENGTH_LONG).show();

            // --- Navigate to LoginActivity ---
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // ==========================================
        // Step 5: Navigate to Login screen
        // ==========================================
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}