package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout candidatesContainer;
    private LinearLayout votersContainer;
    private TextView tvTotalVotes;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        candidatesContainer = findViewById(R.id.candidatesContainer);
        votersContainer = findViewById(R.id.votersContainer);
        tvTotalVotes = findViewById(R.id.tvTotalVotes);
        btnLogout = findViewById(R.id.btnLogout);

        // Load LIVE candidate rankings
        loadCandidateRankings();

        // Load LIVE voter log
        loadVoterLog();

        // Logout button
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Back button → go to login
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void loadCandidateRankings() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("candidates");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Candidate> candidates = new ArrayList<>();
                int totalVotes = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Candidate c = child.getValue(Candidate.class);
                    if (c != null) {
                        candidates.add(c);
                        totalVotes += c.voteCount;
                    }
                }

                // Sort by votes — HIGHEST FIRST
                Collections.sort(candidates, (a, b) -> b.voteCount - a.voteCount);

                // Update total votes
                tvTotalVotes.setText(String.valueOf(totalVotes));

                // Clear and rebuild UI
                candidatesContainer.removeAllViews();
                int maxVotes = candidates.isEmpty() ? 1 :
                        Math.max(candidates.get(0).voteCount, 1);

                for (int i = 0; i < candidates.size(); i++) {
                    addCandidateRow(i + 1, candidates.get(i), maxVotes);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        getString(R.string.firebase_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCandidateRow(int rank, Candidate candidate, int maxVotes) {
        // Card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card_bg);
        card.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        // Row 1: Rank + Name + Votes
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        // Rank badge
        TextView tvRank = new TextView(this);
        tvRank.setText("#" + rank);
        tvRank.setTextSize(22);
        tvRank.setTypeface(null, Typeface.BOLD);
        tvRank.setTextColor(rank == 1 ? Color.parseColor("#FF6F00") :
                getResources().getColor(R.color.primary));
        tvRank.setPadding(0, 0, 16, 0);

        // Name
        TextView tvName = new TextView(this);
        tvName.setText(candidate.name);
        tvName.setTextSize(16);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvName.setLayoutParams(nameParams);

        // Vote count
        TextView tvVotes = new TextView(this);
        tvVotes.setText(candidate.voteCount + " votes");
        tvVotes.setTextSize(14);
        tvVotes.setTypeface(null, Typeface.BOLD);
        tvVotes.setTextColor(getResources().getColor(R.color.accent));

        row1.addView(tvRank);
        row1.addView(tvName);
        row1.addView(tvVotes);

        // Row 2: Description
        TextView tvDesc = new TextView(this);
        tvDesc.setText(candidate.description);
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(getResources().getColor(R.color.text_secondary));
        tvDesc.setPadding(0, 4, 0, 8);

        // Row 3: Progress bar showing vote proportion
        ProgressBar progressBar = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(maxVotes);
        progressBar.setProgress(candidate.voteCount);
        progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(
                        rank == 1 ? Color.parseColor("#FF6F00") :
                                getResources().getColor(R.color.primary)));
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 16);
        progressBar.setLayoutParams(pbParams);

        card.addView(row1);
        card.addView(tvDesc);
        card.addView(progressBar);
        candidatesContainer.addView(card);
    }

    private void loadVoterLog() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("votes");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                votersContainer.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    TextView tvEmpty = new TextView(AdminDashboardActivity.this);
                    tvEmpty.setText(getString(R.string.no_votes_yet));
                    tvEmpty.setTextSize(14);
                    tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
                    tvEmpty.setPadding(16, 16, 16, 16);
                    votersContainer.addView(tvEmpty);
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    VoteRecord vote = child.getValue(VoteRecord.class);
                    if (vote != null) {
                        addVoterRow(vote);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void addVoterRow(VoteRecord vote) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.card_bg);
        row.setPadding(24, 16, 24, 16);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 8);
        row.setLayoutParams(rowParams);

        // Username
        TextView tvUser = new TextView(this);
        tvUser.setText(vote.username);
        tvUser.setTextSize(14);
        tvUser.setTypeface(null, Typeface.BOLD);
        tvUser.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams userParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvUser.setLayoutParams(userParams);

        // Arrow
        TextView tvArrow = new TextView(this);
        tvArrow.setText(" → ");
        tvArrow.setTextSize(14);
        tvArrow.setTextColor(getResources().getColor(R.color.text_secondary));

        // Candidate + Branch
        TextView tvInfo = new TextView(this);
        tvInfo.setText(vote.candidateName + "\n(" + vote.branch + ")");
        tvInfo.setTextSize(12);
        tvInfo.setTextColor(getResources().getColor(R.color.accent));

        row.addView(tvUser);
        row.addView(tvArrow);
        row.addView(tvInfo);
        votersContainer.addView(row);
    }
}