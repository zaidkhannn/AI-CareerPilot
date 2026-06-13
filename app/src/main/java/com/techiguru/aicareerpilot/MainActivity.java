package com.techiguru.aicareerpilot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTitle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Security session check: Redirect if not logged in
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // Adjust for Edge to Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        welcomeTitle = findViewById(R.id.home_welcome_title);
        ShapeableImageView avatarImg = findViewById(R.id.home_avatar_img);

        // Set greeting message
        String registeredName = sharedPreferences.getString("registered_name", "Pilot");
        welcomeTitle.setText("Hello, " + registeredName + " 👋");

        // Primary AI Assistant Bar
        View aiAssistantBar = findViewById(R.id.ai_assistant_bar);

        // Feature cards binding
        View roadmapCard = findViewById(R.id.card_career_roadmap);
        View mentorConnectCard = findViewById(R.id.card_mentor_connect);
        View interviewCard = findViewById(R.id.card_interview_prep);
        View techAssessmentCard = findViewById(R.id.card_tech_assessment);
        View resumeCard = findViewById(R.id.card_resume_analyzer);
        View dreamJobCard = findViewById(R.id.card_dream_job_planner);

        // Setup scale-on-touch listeners for AI bar and dashboard cards
        bindCardAction(aiAssistantBar, () -> navigateTo(AiTestActivity.class));
        bindCardAction(roadmapCard, () -> navigateTo(CareerRoadmapActivity.class));
        bindCardAction(mentorConnectCard, () -> navigateTo(MentorConnectActivity.class));
        bindCardAction(interviewCard, () -> navigateTo(InterviewPrepActivity.class));
        bindCardAction(techAssessmentCard, () -> navigateTo(TechAssessmentActivity.class));
        bindCardAction(resumeCard, () -> navigateTo(ResumeAnalyzerActivity.class));
        bindCardAction(dreamJobCard, () -> navigateTo(DreamJobPlannerActivity.class));

        // Profile shortcut binding
        avatarImg.setOnClickListener(v -> navigateTo(ProfileActivity.class));
    }

    /**
     * Binds a touch listener to scale cards down slightly on press and run actions on release.
     */
    private void bindCardAction(View view, final Runnable action) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
                    action.run();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
                    break;
            }
            return true;
        });
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(MainActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}