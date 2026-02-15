package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CandidateVoteActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvVoteInfo;
    private ImageButton imgBtn1, imgBtn2, imgBtn3, imgBtn4;
    private RadioButton rb1, rb2, rb3, rb4;
    private GridView gridViewCandidates;
    private Button btnSubmitVote;

    // Data from previous screens
    private String branch = "";
    private String events = "";
    private String username = "";

    // Candidate data arrays
    private String[] candidateNames;
    private String[] candidateDescs;

    private final int[] candidateImages = {
            R.drawable.ic_candidate,
            R.drawable.ic_candidate,
            R.drawable.ic_candidate,
            R.drawable.ic_candidate
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_vote);

        // ==========================================
        // Step 1: Load candidate names from strings.xml
        // ==========================================
        candidateNames = new String[]{
                getString(R.string.candidate_1_name),
                getString(R.string.candidate_2_name),
                getString(R.string.candidate_3_name),
                getString(R.string.candidate_4_name)
        };

        candidateDescs = new String[]{
                getString(R.string.candidate_1_desc),
                getString(R.string.candidate_2_desc),
                getString(R.string.candidate_3_desc),
                getString(R.string.candidate_4_desc)
        };

        // ==========================================
        // Step 2: Find all views
        // ==========================================
        tvVoteInfo = findViewById(R.id.tvVoteInfo);
        imgBtn1 = findViewById(R.id.imgBtn1);
        imgBtn2 = findViewById(R.id.imgBtn2);
        imgBtn3 = findViewById(R.id.imgBtn3);
        imgBtn4 = findViewById(R.id.imgBtn4);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        gridViewCandidates = findViewById(R.id.gridViewCandidates);
        btnSubmitVote = findViewById(R.id.btnSubmitVote);

        // ==========================================
        // Step 3: Get data from EventTypeActivity
        // ==========================================
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            branch = receivedIntent.getStringExtra("branch");
            events = receivedIntent.getStringExtra("events");
            username = receivedIntent.getStringExtra("username");
            tvVoteInfo.setText(getString(R.string.vote_info_format, branch, events));
        }

        // ==========================================
        // Step 4: RadioButton — only one can be selected
        // Since RadioButtons are in separate containers,
        // we manage selection manually
        // ==========================================
        rb1.setOnClickListener(v -> {
            rb1.setChecked(true);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
        });

        rb2.setOnClickListener(v -> {
            rb1.setChecked(false);
            rb2.setChecked(true);
            rb3.setChecked(false);
            rb4.setChecked(false);
        });

        rb3.setOnClickListener(v -> {
            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(true);
            rb4.setChecked(false);
        });

        rb4.setOnClickListener(v -> {
            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(true);
        });

        // ==========================================
        // Step 5: ImageButton — show details on tap
        // ==========================================
        imgBtn1.setOnClickListener(v -> {
            Toast.makeText(this,
                    getString(R.string.candidate_detail, candidateNames[0], candidateDescs[0]),
                    Toast.LENGTH_SHORT).show();
        });

        imgBtn2.setOnClickListener(v -> {
            Toast.makeText(this,
                    getString(R.string.candidate_detail, candidateNames[1], candidateDescs[1]),
                    Toast.LENGTH_SHORT).show();
        });

        imgBtn3.setOnClickListener(v -> {
            Toast.makeText(this,
                    getString(R.string.candidate_detail, candidateNames[2], candidateDescs[2]),
                    Toast.LENGTH_SHORT).show();
        });

        imgBtn4.setOnClickListener(v -> {
            Toast.makeText(this,
                    getString(R.string.candidate_detail, candidateNames[3], candidateDescs[3]),
                    Toast.LENGTH_SHORT).show();
        });

        // ==========================================
        // Step 6: GridView with custom adapter
        // ==========================================
        CandidateAdapter adapter = new CandidateAdapter(
                this,
                candidateNames,
                candidateDescs,
                candidateImages
        );
        gridViewCandidates.setAdapter(adapter);

        // GridView item click
        gridViewCandidates.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this,
                    getString(R.string.candidate_info, candidateNames[position], candidateDescs[position]),
                    Toast.LENGTH_SHORT).show();
        });

        // ==========================================
        // Step 7: Submit Vote Button
        // ==========================================
        btnSubmitVote.setOnClickListener(v -> {
            // Find which candidate is selected
            String selectedCandidate = "";

            if (rb1.isChecked()) {
                selectedCandidate = candidateNames[0];
            } else if (rb2.isChecked()) {
                selectedCandidate = candidateNames[1];
            } else if (rb3.isChecked()) {
                selectedCandidate = candidateNames[2];
            } else if (rb4.isChecked()) {
                selectedCandidate = candidateNames[3];
            }

            // Make sure someone is selected
            if (selectedCandidate.isEmpty()) {
                Toast.makeText(this,
                        getString(R.string.error_select_candidate),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Go to ConfirmationActivity
            Intent intent = new Intent(CandidateVoteActivity.this,
                    ConfirmationActivity.class);
            intent.putExtra("branch", branch);
            intent.putExtra("events", events);
            intent.putExtra("candidate", selectedCandidate);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }
}