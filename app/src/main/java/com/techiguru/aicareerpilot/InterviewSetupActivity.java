package com.techiguru.aicareerpilot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Spinner;

public class InterviewSetupActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private Spinner difficultySpinner;
    private Spinner topicSpinner;
    private Spinner voiceSpinner;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_interview_setup);

        // Adjust for Edge to Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        roleSpinner = findViewById(R.id.setup_role_spinner);
        difficultySpinner = findViewById(R.id.setup_difficulty_spinner);
        topicSpinner = findViewById(R.id.setup_topic_spinner);
        voiceSpinner = findViewById(R.id.setup_voice_spinner);
        startBtn = findViewById(R.id.start_interview_btn);

        // Back button
        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Set up Role Choice spinner Choices
        String[] roles = {
            "Software Engineer",
            "Frontend Developer",
            "Backend Developer",
            "Android Developer",
            "Machine Learning Engineer",
            "Data Scientist"
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Set up Difficulty spinner Choices
        String[] levels = {"Entry-Level / Junior", "Mid-Level", "Senior / FAANG Lead"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // Set up Topic Focus spinner Choices
        String[] topics = {
            "Coding & Algorithms",
            "System Design",
            "Behavioral / STAR Method",
            "Core Computer Science (OS, DBMS, Networks)",
            "Domain-Specific Knowledge"
        };
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(topicAdapter);

        // Set up Interviewer Voice spinner choices
        String[] voices = {
            "Male Voice (Tony) — Recommended",
            "Female Voice (Zara)",
            "System Default"
        };
        ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, voices);
        voiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voiceSpinner.setAdapter(voiceAdapter);
        // Default to Male (index 0)
        voiceSpinner.setSelection(0);

        // Action button to start simulation
        startBtn.setOnClickListener(v -> {
            String role = roleSpinner.getSelectedItem().toString();
            String difficulty = difficultySpinner.getSelectedItem().toString();
            String topic = topicSpinner.getSelectedItem().toString();

            // Map selected voice label to a type key
            int voiceIndex = voiceSpinner.getSelectedItemPosition();
            String voiceType;
            if (voiceIndex == 1) {
                voiceType = "female";
            } else if (voiceIndex == 2) {
                voiceType = "default";
            } else {
                voiceType = "male"; // index 0 — default selection
            }

            Intent intent = new Intent(InterviewSetupActivity.this, InterviewSimulatorActivity.class);
            intent.putExtra("role", role);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("topic", topic);
            intent.putExtra("voice_type", voiceType);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
