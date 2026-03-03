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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateVoteActivity extends AppCompatActivity {

    private TextView tvVoteInfo;
    private ImageButton imgBtn1, imgBtn2, imgBtn3, imgBtn4;
    private RadioButton rb1, rb2, rb3, rb4;
    private TextView tvVoteCount1, tvVoteCount2, tvVoteCount3, tvVoteCount4;
    private GridView gridViewCandidates;
    private Button btnSubmitVote;

    private String branch = "";
    private String events = "";
    private String username = "";

    private String[] candidateNames;
    private String[] candidateDescs;
    private final int[] candidateImages = {
            R.drawable.ic_candidate, R.drawable.ic_candidate,
            R.drawable.ic_candidate, R.drawable.ic_candidate
    };

    // Map candidate key (no spaces) to index
    private final String[] candidateKeys = {"RahulSharma", "PriyaPatel", "AmitKumar", "SnehaDesai"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_vote);

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

        // Find views
        tvVoteInfo = findViewById(R.id.tvVoteInfo);
        imgBtn1 = findViewById(R.id.imgBtn1);
        imgBtn2 = findViewById(R.id.imgBtn2);
        imgBtn3 = findViewById(R.id.imgBtn3);
        imgBtn4 = findViewById(R.id.imgBtn4);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        tvVoteCount1 = findViewById(R.id.tvVoteCount1);
        tvVoteCount2 = findViewById(R.id.tvVoteCount2);
        tvVoteCount3 = findViewById(R.id.tvVoteCount3);
        tvVoteCount4 = findViewById(R.id.tvVoteCount4);
        gridViewCandidates = findViewById(R.id.gridViewCandidates);
        btnSubmitVote = findViewById(R.id.btnSubmitVote);

        // Get data from previous screen
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            branch = receivedIntent.getStringExtra("branch");
            events = receivedIntent.getStringExtra("events");
            username = receivedIntent.getStringExtra("username");
            tvVoteInfo.setText(getString(R.string.vote_info_format, branch, events));
        }

        // RadioButton — only one selected at a time
        rb1.setOnClickListener(v -> { rb1.setChecked(true); rb2.setChecked(false); rb3.setChecked(false); rb4.setChecked(false); });
        rb2.setOnClickListener(v -> { rb1.setChecked(false); rb2.setChecked(true); rb3.setChecked(false); rb4.setChecked(false); });
        rb3.setOnClickListener(v -> { rb1.setChecked(false); rb2.setChecked(false); rb3.setChecked(true); rb4.setChecked(false); });
        rb4.setOnClickListener(v -> { rb1.setChecked(false); rb2.setChecked(false); rb3.setChecked(false); rb4.setChecked(true); });

        // ImageButton — show details on tap
        imgBtn1.setOnClickListener(v -> Toast.makeText(this, getString(R.string.candidate_detail, candidateNames[0], candidateDescs[0]), Toast.LENGTH_SHORT).show());
        imgBtn2.setOnClickListener(v -> Toast.makeText(this, getString(R.string.candidate_detail, candidateNames[1], candidateDescs[1]), Toast.LENGTH_SHORT).show());
        imgBtn3.setOnClickListener(v -> Toast.makeText(this, getString(R.string.candidate_detail, candidateNames[2], candidateDescs[2]), Toast.LENGTH_SHORT).show());
        imgBtn4.setOnClickListener(v -> Toast.makeText(this, getString(R.string.candidate_detail, candidateNames[3], candidateDescs[3]), Toast.LENGTH_SHORT).show());

        // Load LIVE vote counts from Firebase
        loadLiveVoteCounts();

        // Check if user already voted
        checkIfAlreadyVoted();

        // Submit Vote Button
        btnSubmitVote.setOnClickListener(v -> {
            String selectedCandidate = "";
            int selectedIndex = -1;

            if (rb1.isChecked()) { selectedCandidate = candidateNames[0]; selectedIndex = 0; }
            else if (rb2.isChecked()) { selectedCandidate = candidateNames[1]; selectedIndex = 1; }
            else if (rb3.isChecked()) { selectedCandidate = candidateNames[2]; selectedIndex = 2; }
            else if (rb4.isChecked()) { selectedCandidate = candidateNames[3]; selectedIndex = 3; }

            if (selectedCandidate.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_candidate), Toast.LENGTH_SHORT).show();
                return;
            }

            submitVoteToFirebase(selectedCandidate, selectedIndex);
        });
    }

    private void loadLiveVoteCounts() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("candidates");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Map to store vote counts by candidate name
                Map<String, Integer> voteMap = new HashMap<>();
                List<Candidate> candidateList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Candidate c = child.getValue(Candidate.class);
                    if (c != null) {
                        voteMap.put(c.name, c.voteCount);
                        candidateList.add(c);
                    }
                }

                // Update vote counts in GridLayout
                tvVoteCount1.setText(voteMap.getOrDefault(candidateNames[0], 0) + " votes");
                tvVoteCount2.setText(voteMap.getOrDefault(candidateNames[1], 0) + " votes");
                tvVoteCount3.setText(voteMap.getOrDefault(candidateNames[2], 0) + " votes");
                tvVoteCount4.setText(voteMap.getOrDefault(candidateNames[3], 0) + " votes");

                // Sort candidates by votes (highest first) for GridView
                Collections.sort(candidateList, (a, b) -> b.voteCount - a.voteCount);

                // Build sorted arrays for GridView adapter
                String[] sortedNames = new String[candidateList.size()];
                String[] sortedDescs = new String[candidateList.size()];
                int[] sortedVotes = new int[candidateList.size()];
                int[] sortedImages = new int[candidateList.size()];

                for (int i = 0; i < candidateList.size(); i++) {
                    sortedNames[i] = candidateList.get(i).name;
                    sortedDescs[i] = candidateList.get(i).description;
                    sortedVotes[i] = candidateList.get(i).voteCount;
                    sortedImages[i] = R.drawable.ic_candidate;
                }

                // Update GridView with ranked candidates
                CandidateAdapter adapter = new CandidateAdapter(
                        CandidateVoteActivity.this,
                        sortedNames, sortedDescs, sortedImages, sortedVotes);
                gridViewCandidates.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void checkIfAlreadyVoted() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.hasVoted) {
                    // Disable voting
                    btnSubmitVote.setEnabled(false);
                    btnSubmitVote.setText(getString(R.string.already_voted_button));
                    rb1.setEnabled(false);
                    rb2.setEnabled(false);
                    rb3.setEnabled(false);
                    rb4.setEnabled(false);
                    Toast.makeText(CandidateVoteActivity.this,
                            getString(R.string.already_voted_message),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void submitVoteToFirebase(String candidateName, int index) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String candidateKey = candidateKeys[index];

        // Double-check user hasn't voted
        db.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.hasVoted) {
                    Toast.makeText(CandidateVoteActivity.this,
                            getString(R.string.already_voted_message),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // 1. Mark user as voted
                db.child("users").child(username).child("hasVoted").setValue(true);
                db.child("users").child(username).child("votedFor").setValue(candidateName);

                // 2. Increment candidate vote count using transaction (atomic)
                db.child("candidates").child(candidateKey).child("voteCount")
                        .runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer currentVotes = mutableData.getValue(Integer.class);
                                if (currentVotes == null) {
                                    mutableData.setValue(1);
                                } else {
                                    mutableData.setValue(currentVotes + 1);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError error, boolean committed,
                                                   DataSnapshot currentData) {
                            }
                        });

                // 3. Save vote record
                VoteRecord vote = new VoteRecord(username, candidateName,
                        branch, events, System.currentTimeMillis());
                db.child("votes").push().setValue(vote);

                // 4. Navigate to Confirmation
                Intent intent = new Intent(CandidateVoteActivity.this,
                        ConfirmationActivity.class);
                intent.putExtra("candidate", candidateName);
                intent.putExtra("branch", branch);
                intent.putExtra("events", events);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CandidateVoteActivity.this,
                        getString(R.string.firebase_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}