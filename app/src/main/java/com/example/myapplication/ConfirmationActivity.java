package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class ConfirmationActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvConfirmVoter, tvConfirmBranch, tvConfirmEvents, tvConfirmCandidate;
    private TextView tvConverterResult, tvConverterDetail;
    private ProgressBar progressBarVote;
    private TextView tvProgressPercent, tvCountdownConfirm;
    private TextView tvSuccessMessage;
    private Button btnVisitWebsite, btnExit;

    // Data from previous screen
    private String branch, events, candidate, username;

    // SharedPreferences
    private static final String PREFS_NAME = "VotingAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // ==========================================
        // Step 1: Find all views
        // ==========================================
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

        // ==========================================
        // Step 2: Get data from CandidateVoteActivity
        // ==========================================
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

        // ==========================================
        // Step 3: MODULE 6 — Vote Converter
        // ==========================================
        performVoteConversion();

        // ==========================================
        // Step 4: MODULE 7 — Countdown Timer (5 seconds)
        // ==========================================
        startSubmissionCountdown();

        // ==========================================
        // Step 5: MODULE 10 — Implicit Intent
        // Opens college website in external browser
        // ==========================================
        btnVisitWebsite.setOnClickListener(v -> {
            String url = getString(R.string.college_website);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));

            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this,
                        getString(R.string.no_browser),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ==========================================
        // Step 6: Exit Button — closes entire app
        // ==========================================
        btnExit.setOnClickListener(v -> {
            Toast.makeText(this,
                    getString(R.string.thank_you),
                    Toast.LENGTH_SHORT).show();
            finishAffinity();
        });

        // ==========================================
        // Step 7: Handle BACK button press (MODERN WAY)
        // This replaces the old onBackPressed() method
        // Prevents user from going back after voting
        // ==========================================
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show message instead of going back
                Toast.makeText(ConfirmationActivity.this,
                        getString(R.string.vote_already_submitted),
                        Toast.LENGTH_SHORT).show();
                // Do NOT call setEnabled(false) — that would allow going back
            }
        });
    }

    /**
     * MODULE 6 — Convertor Application
     * Converts raw vote count into percentage
     */
    private void performVoteConversion() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalVoters = prefs.getInt("total_votes", 100);

        int yourVote = 1;
        double percentage = ((double) yourVote / totalVoters) * 100;

        tvConverterResult.setText(getString(R.string.converter_result,
                yourVote, totalVoters, percentage));

        tvConverterDetail.setText(getString(R.string.converter_detail,
                yourVote, totalVoters, percentage));

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("total_votes", totalVoters + 1);
        editor.apply();
    }

    /**
     * MODULE 7 — Countdown Timer
     * 5-second countdown with animated progress bar
     */
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
                        getString(R.string.vote_success_toast),
                        Toast.LENGTH_LONG).show();
            }
        }.start();
    }
}