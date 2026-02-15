package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BranchSelectionActivity extends AppCompatActivity {

    // UI Elements
    private Spinner spinnerBranch;
    private TextView tvSelectedBranch, tvWelcomeUser;
    private Button btnProceed;

    // Variables
    private String selectedBranch = "";
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_selection);

        // ==========================================
        // Step 1: Find all views
        // ==========================================
        spinnerBranch = findViewById(R.id.spinnerBranch);
        tvSelectedBranch = findViewById(R.id.tvSelectedBranch);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnProceed = findViewById(R.id.btnProceed);

        // ==========================================
        // Step 2: Get username from LoginActivity
        // ==========================================
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra("username")) {
            username = receivedIntent.getStringExtra("username");
            tvWelcomeUser.setText(getString(R.string.welcome_user, username));
        }

        // ==========================================
        // Step 3: Setup Spinner with branch names
        // ==========================================
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.branches,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);

        // Handle Spinner selection
        spinnerBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedBranch = parent.getItemAtPosition(position).toString();

                if (position == 0) {
                    // First item is placeholder "Select Branch..."
                    tvSelectedBranch.setText(getString(R.string.none_selected));
                    tvSelectedBranch.setTextColor(
                            getResources().getColor(R.color.text_secondary));
                } else {
                    tvSelectedBranch.setText(selectedBranch);
                    tvSelectedBranch.setTextColor(
                            getResources().getColor(R.color.primary));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSelectedBranch.setText(getString(R.string.none_selected));
            }
        });

        // ==========================================
        // Step 4: Proceed Button
        // ==========================================
        btnProceed.setOnClickListener(v -> {
            // Make sure user selected a real branch (not placeholder)
            if (spinnerBranch.getSelectedItemPosition() == 0) {
                Toast.makeText(this,
                        getString(R.string.error_select_branch),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Go to EventTypeActivity with branch and username
            Intent intent = new Intent(BranchSelectionActivity.this,
                    EventTypeActivity.class);
            intent.putExtra("branch", selectedBranch);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }
}