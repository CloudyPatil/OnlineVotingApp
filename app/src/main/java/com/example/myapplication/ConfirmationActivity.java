package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView tvConfirmVoter, tvConfirmBranch, tvConfirmEvents, tvConfirmCandidate;
    private TextView tvConverterResult, tvConverterDetail;
    private ProgressBar progressBarVote;
    private TextView tvProgressPercent, tvCountdownConfirm;
    private TextView tvSuccessMessage;
    private Button btnVisitWebsite, btnExit;

    private String branch, events, candidate, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        tvConfirmVoter = findViewById(R.id.tvConfirmVoter);
        tvConfirmBranch = findViewById(R.id.tvConfirmBranch);
        tvConfirmEvents = findViewById(R.id.tvConfirmEvents);
        tvConfirmCandidate = findViewById(R.id.tvConfirmCandidate);
        tvConverterResult = findViewById(R.id.tvConverterResult);
        tvConverterDetail = findViewById(R.id.tvConverterDetail);
        progressBarVote = findViewById(R.id.progressBarVote);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvCountdownConfirm = findViewById(R.id.tvCountdownConfirm);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnVisitWebsite = findViewById(R.id.btnVisitWebsite);
        btnExit = findViewById(R.id.btnExit);

        // Get data
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            username = receivedIntent.getStringExtra("username");
            branch = receivedIntent.getStringExtra("branch");
            events = receivedIntent.getStringExtra("events");
            candidate = receivedIntent.getStringExtra("candidate");

            tvConfirmVoter.setText(username != null ? username : getString(R.string.default_voter));
            tvConfirmBranch.setText(branch != null ? branch : getString(R.string.default_na));
            tvConfirmEvents.setText(events != null ? events : getString(R.string.default_na));
            tvConfirmCandidate.setText(candidate != null ? candidate : getString(R.string.default_na));
        }

        // Vote Converter — uses real data from Firebase
        performVoteConversion();

        // Countdown Timer
        startSubmissionCountdown();

        // Visit Website (Implicit Intent)
        btnVisitWebsite.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.college_website)));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
            }
        });

        // Exit
        btnExit.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
            finishAffinity();
        });

        // Prevent back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(ConfirmationActivity.this,
                        getString(R.string.vote_already_submitted), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performVoteConversion() {
        // Get total registered users from Firebase
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int totalUsers = (int) snapshot.getChildrenCount();
                        int yourVote = 1;
                        double percentage = ((double) yourVote / totalUsers) * 100;

                        tvConverterResult.setText(getString(R.string.converter_result,
                                yourVote, totalUsers, percentage));
                        tvConverterDetail.setText(getString(R.string.converter_detail,
                                yourVote, totalUsers, percentage));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        tvConverterResult.setText(getString(R.string.firebase_error));
                    }
                });
    }

    private void startSubmissionCountdown() {
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                tvCountdownConfirm.setText(getString(R.string.countdown_confirm_format, secondsLeft));
                int progress = (int) ((5000 - millisUntilFinished) * 100 / 5000);
                progressBarVote.setProgress(progress);
                tvProgressPercent.setText(getString(R.string.progress_format, progress));
            }

            @Override
            public void onFinish() {
                progressBarVote.setProgress(100);
                tvProgressPercent.setText(getString(R.string.progress_complete));
                tvCountdownConfirm.setText(getString(R.string.countdown_done));
                tvSuccessMessage.setVisibility(View.VISIBLE);
                btnVisitWebsite.setVisibility(View.VISIBLE);
                btnExit.setVisibility(View.VISIBLE);
                Toast.makeText(ConfirmationActivity.this,
                        getString(R.string.vote_success_toast), Toast.LENGTH_LONG).show();
            }
        }.start();
    }
}