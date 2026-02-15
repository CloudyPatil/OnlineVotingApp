package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EventTypeActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvBranchDisplay;
    private CheckBox cbSports, cbCultural, cbAcademic, cbTechnical, cbLiterary;
    private ListView listViewEvents;
    private Button btnViewCandidates;

    // Data from previous screen
    private String branch = "";
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_type);

        // ==========================================
        // Step 1: Find all views
        // ==========================================
        tvBranchDisplay = findViewById(R.id.tvBranchDisplay);
        cbSports = findViewById(R.id.cbSports);
        cbCultural = findViewById(R.id.cbCultural);
        cbAcademic = findViewById(R.id.cbAcademic);
        cbTechnical = findViewById(R.id.cbTechnical);
        cbLiterary = findViewById(R.id.cbLiterary);
        listViewEvents = findViewById(R.id.listViewEvents);
        btnViewCandidates = findViewById(R.id.btnViewCandidates);

        // ==========================================
        // Step 2: Get data from BranchSelectionActivity
        // ==========================================
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            branch = receivedIntent.getStringExtra("branch");
            username = receivedIntent.getStringExtra("username");
            tvBranchDisplay.setText(getString(R.string.branch_display, branch));
        }

        // ==========================================
        // Step 3: Setup ListView with event types
        // ==========================================
        ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.event_types,
                android.R.layout.simple_list_item_1
        );
        listViewEvents.setAdapter(listAdapter);

        // When user taps an item in ListView, show info
        listViewEvents.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            Toast.makeText(this,
                    getString(R.string.event_info, selectedItem),
                    Toast.LENGTH_SHORT).show();
        });

        // ==========================================
        // Step 4: View Candidates Button
        // ==========================================
        btnViewCandidates.setOnClickListener(v -> {
            // Collect all checked event types
            ArrayList<String> selectedEvents = new ArrayList<>();

            if (cbSports.isChecked()) selectedEvents.add("Sports");
            if (cbCultural.isChecked()) selectedEvents.add("Cultural");
            if (cbAcademic.isChecked()) selectedEvents.add("Academic");
            if (cbTechnical.isChecked()) selectedEvents.add("Technical");
            if (cbLiterary.isChecked()) selectedEvents.add("Literary");

            // Make sure at least one is selected
            if (selectedEvents.isEmpty()) {
                Toast.makeText(this,
                        getString(R.string.error_select_event),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Build comma-separated string
            StringBuilder eventsString = new StringBuilder();
            for (int i = 0; i < selectedEvents.size(); i++) {
                eventsString.append(selectedEvents.get(i));
                if (i < selectedEvents.size() - 1) {
                    eventsString.append(", ");
                }
            }

            // Show what was selected
            Toast.makeText(this,
                    getString(R.string.selected_events, eventsString.toString()),
                    Toast.LENGTH_SHORT).show();

            // Go to CandidateVoteActivity
            Intent intent = new Intent(EventTypeActivity.this,
                    CandidateVoteActivity.class);
            intent.putExtra("branch", branch);
            intent.putExtra("events", eventsString.toString());
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }
}