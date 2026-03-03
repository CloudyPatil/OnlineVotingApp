package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword;
    private TextView tvSelectedDate, tvSelectedTime;
    private Button btnPickDate, btnPickTime, btnSignUp;
    private TextView tvGoToLogin;

    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // DatePicker
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = day + "/" + (month + 1) + "/" + year;
                        tvSelectedDate.setText(selectedDate);
                        tvSelectedDate.setTextColor(getResources().getColor(R.color.black));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // TimePicker
        btnPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        String amPm = hour >= 12 ? "PM" : "AM";
                        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
                        selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
                        tvSelectedTime.setText(selectedTime);
                        tvSelectedTime.setTextColor(getResources().getColor(R.color.black));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            ).show();
        });

        // Sign Up Button — saves to Firebase
        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullName.setError(getString(R.string.error_enter_fullname));
                return;
            }
            if (username.isEmpty()) {
                etUsername.setError(getString(R.string.error_enter_username));
                return;
            }
            if (password.isEmpty() || password.length() < 4) {
                etPassword.setError(getString(R.string.error_password_short));
                return;
            }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_dob), Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_time), Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username exists in Firebase
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(SignUpActivity.this,
                                getString(R.string.error_username_exists),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Create new user in Firebase
                        User newUser = new User(fullName, password, false, "", false);
                        db.child("users").child(username).setValue(newUser)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(SignUpActivity.this,
                                            getString(R.string.signup_success, fullName),
                                            Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this,
                                            getString(R.string.firebase_error),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SignUpActivity.this,
                            getString(R.string.firebase_error),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Go to Login
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }
}