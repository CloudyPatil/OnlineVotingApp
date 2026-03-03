package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private ProgressBar progressBarSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tvCountdown = findViewById(R.id.tvCountdown);
        progressBarSplash = findViewById(R.id.progressBarSplash);

        // Seed default data to Firebase on first run
        seedFirebaseData();

        // Countdown Timer
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                tvCountdown.setText(getString(R.string.countdown_format, secondsLeft));
                int progress = (int) ((3000 - millisUntilFinished) * 100 / 3000);
                progressBarSplash.setProgress(progress);
            }

            @Override
            public void onFinish() {
                progressBarSplash.setProgress(100);
                tvCountdown.setText(getString(R.string.countdown_go));
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void seedFirebaseData() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // Check if admin already exists
        db.child("users").child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create default admin
                    User admin = new User("Admin User", "admin123", false, "", true);
                    db.child("users").child("admin").setValue(admin);

                    // Create default students
                    db.child("users").child("student1")
                            .setValue(new User("Student One", "pass1234", false, "", false));
                    db.child("users").child("student2")
                            .setValue(new User("Student Two", "pass5678", false, "", false));
                    db.child("users").child("student3")
                            .setValue(new User("Student Three", "pass9012", false, "", false));

                    // Create candidates
                    db.child("candidates").child("RahulSharma")
                            .setValue(new Candidate("Rahul Sharma", "Sports Captain, 3rd Year", 0));
                    db.child("candidates").child("PriyaPatel")
                            .setValue(new Candidate("Priya Patel", "Cultural Secretary, 2nd Year", 0));
                    db.child("candidates").child("AmitKumar")
                            .setValue(new Candidate("Amit Kumar", "Tech Lead, 3rd Year", 0));
                    db.child("candidates").child("SnehaDesai")
                            .setValue(new Candidate("Sneha Desai", "Academic Topper, 2nd Year", 0));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}