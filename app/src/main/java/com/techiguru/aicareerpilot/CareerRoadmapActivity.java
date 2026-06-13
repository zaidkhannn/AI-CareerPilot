package com.techiguru.aicareerpilot;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class CareerRoadmapActivity extends AppCompatActivity {

    private static final String TAG = "CareerRoadmapActivity";

    // Selectors
    private Spinner roleSpinner;
    private Spinner skillSpinner;
    private Spinner hoursSpinner;
    private Button generateBtn;
    private View loadingLayout;

    // Summary Metric Card
    private View summaryCard;
    private TextView phasesVal;
    private TextView timeVal;
    private TextView difficultyVal;

    // Career Insights Card
    private View insightsCard;
    private TextView salaryVal;
    private TextView demandVal;
    private TextView competitionVal;
    private TextView growthVal;

    // Phase Container
    private LinearLayout cardsContainer;

    // Final Outcome Card
    private View outcomeCard;
    private TextView readinessPct;
    private ProgressBar readinessProgress;
    private TextView outcomeSkillsText;

    // Job Roles Card
    private View rolesCard;
    private TextView jobRolesVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_career_roadmap);

        // Adjust for Edge to Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        roleSpinner = findViewById(R.id.roadmap_role_spinner);
        skillSpinner = findViewById(R.id.roadmap_skill_spinner);
        hoursSpinner = findViewById(R.id.roadmap_hours_spinner);
        generateBtn = findViewById(R.id.roadmap_generate_btn);
        loadingLayout = findViewById(R.id.roadmap_loading_layout);
        
        // Summary Card
        summaryCard = findViewById(R.id.roadmap_summary_card);
        phasesVal = findViewById(R.id.roadmap_phases_val);
        timeVal = findViewById(R.id.roadmap_time_val);
        difficultyVal = findViewById(R.id.roadmap_difficulty_val);
        
        // Insights Card
        insightsCard = findViewById(R.id.roadmap_insights_card);
        salaryVal = findViewById(R.id.roadmap_salary_val);
        demandVal = findViewById(R.id.roadmap_demand_val);
        competitionVal = findViewById(R.id.roadmap_competition_val);
        growthVal = findViewById(R.id.roadmap_growth_val);
        
        // Container
        cardsContainer = findViewById(R.id.roadmap_cards_container);

        // Outcome Card
        outcomeCard = findViewById(R.id.roadmap_outcome_card);
        readinessPct = findViewById(R.id.roadmap_readiness_pct);
        readinessProgress = findViewById(R.id.roadmap_readiness_progress);
        outcomeSkillsText = findViewById(R.id.roadmap_outcome_skills);

        // Roles Card
        rolesCard = findViewById(R.id.roadmap_roles_card);
        jobRolesVal = findViewById(R.id.roadmap_job_roles_val);

        // Back action
        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Set up Career Role spinner Choices
        String[] roles = {
            "Frontend Developer",
            "Full Stack Developer",
            "Android Developer",
            "Data Scientist",
            "AI Engineer"
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Set up Skill Level spinner Choices
        String[] skillLevels = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> skillAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skillLevels);
        skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skillSpinner.setAdapter(skillAdapter);

        // Set up Study Time spinner Choices
        String[] studyTimes = {"1 Hour", "2 Hours", "4 Hours", "6+ Hours"};
        ArrayAdapter<String> hoursAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studyTimes);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(hoursAdapter);

        // Action button
        generateBtn.setOnClickListener(v -> handleGenerateRoadmap());
    }

    private void handleGenerateRoadmap() {
        String selectedRole = roleSpinner.getSelectedItem().toString();
        String selectedLevel = skillSpinner.getSelectedItem().toString();
        String selectedHours = hoursSpinner.getSelectedItem().toString();

        // Prompt Groq strictly for structured JSON representation integrating new params
        String prompt = "You are an expert career advisor. Generate a learning roadmap for becoming a " + selectedRole + ".\n" +
                "Adjust the path complexity based on user's current skill level: " + selectedLevel + ".\n" +
                "Adjust durations based on user's daily study time: " + selectedHours + " per day.\n\n" +
                "You must output the roadmap strictly in JSON format. Do not include any explanation, intro/outro paragraphs, or markdown code fences outside the JSON object.\n\n" +
                "The JSON structure must match this format exactly:\n" +
                "{\n" +
                "  \"total_phases\": 4,\n" +
                "  \"estimated_time\": \"6 Months\",\n" +
                "  \"difficulty\": \"Intermediate\",\n" +
                "  \"career_insights\": {\n" +
                "    \"avg_salary\": \"$80k - $120k\",\n" +
                "    \"market_demand\": \"High\",\n" +
                "    \"competition_level\": \"Medium\",\n" +
                "    \"growth_potential\": \"Excellent\"\n" +
                "  },\n" +
                "  \"phases\": [\n" +
                "    {\n" +
                "      \"phase_name\": \"Phase Name\",\n" +
                "      \"duration\": \"Duration\",\n" +
                "      \"skills\": [\"Skill A\", \"Skill B\"],\n" +
                "      \"weekly_milestones\": [\"Week 1 Goal\", \"Week 2 Goal\"],\n" +
                "      \"mini_project\": {\n" +
                "        \"project_name\": \"Name\",\n" +
                "        \"description\": \"Description\",\n" +
                "        \"expected_outcome\": \"Outcome\"\n" +
                "      },\n" +
                "      \"resources\": {\n" +
                "        \"youtube\": \"Channel / Creator\",\n" +
                "        \"documentation\": \"Doc Resource\",\n" +
                "        \"practice\": \"Practice Site\"\n" +
                "      },\n" +
                "      \"interview_topics\": [\"Topic 1\", \"Topic 2\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"final_outcome\": {\n" +
                "    \"skills_learned\": [\"Outcome 1\", \"Outcome 2\"],\n" +
                "    \"job_readiness_pct\": 85\n" +
                "  },\n" +
                "  \"job_roles\": [\"Job Role 1\", \"Job Role 2\"]\n" +
                "}";

        // Enable loading indicator state
        generateBtn.setEnabled(false);
        loadingLayout.setVisibility(View.VISIBLE);
        
        // Hide details cards
        summaryCard.setVisibility(View.GONE);
        insightsCard.setVisibility(View.GONE);
        outcomeCard.setVisibility(View.GONE);
        rolesCard.setVisibility(View.GONE);
        cardsContainer.removeAllViews();

        GroqApiService.sendPrompt(prompt, new GroqApiService.GroqCallback() {
            @Override
            public void onSuccess(String responseText) {
                generateBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);
                
                try {
                    // Sanitize JSON Response
                    String cleanJson = responseText.trim();
                    if (cleanJson.startsWith("```json")) {
                        cleanJson = cleanJson.substring(7);
                    } else if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.substring(3);
                    }
                    if (cleanJson.endsWith("```")) {
                        cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                    }
                    cleanJson = cleanJson.trim();

                    Log.d(TAG, "Parsing JSON: " + cleanJson);

                    // Parse JSON root object
                    JSONObject responseJson = new JSONObject(cleanJson);

                    // Update Top Summary metrics
                    int totalPhases = responseJson.getInt("total_phases");
                    String estimatedTime = responseJson.getString("estimated_time");
                    String difficulty = responseJson.getString("difficulty");

                    phasesVal.setText(String.valueOf(totalPhases));
                    timeVal.setText(estimatedTime);
                    difficultyVal.setText(difficulty);
                    summaryCard.setVisibility(View.VISIBLE);

                    // Update Career Insights Card
                    JSONObject insightsObj = responseJson.getJSONObject("career_insights");
                    salaryVal.setText(insightsObj.getString("avg_salary"));
                    demandVal.setText(insightsObj.getString("market_demand"));
                    competitionVal.setText(insightsObj.getString("competition_level"));
                    growthVal.setText(insightsObj.getString("growth_potential"));
                    insightsCard.setVisibility(View.VISIBLE);

                    // Fetch and render list of phases
                    JSONArray phasesArray = responseJson.getJSONArray("phases");
                    LayoutInflater inflater = LayoutInflater.from(CareerRoadmapActivity.this);

                    for (int i = 0; i < phasesArray.length(); i++) {
                        JSONObject phaseObj = phasesArray.getJSONObject(i);
                        
                        // Inflate item layout
                        View cardView = inflater.inflate(R.layout.item_phase_card, cardsContainer, false);

                        // Bind item widgets
                        TextView nameText = cardView.findViewById(R.id.phase_name);
                        TextView durationText = cardView.findViewById(R.id.phase_duration);
                        TextView arrowText = cardView.findViewById(R.id.phase_expand_arrow);
                        LinearLayout detailsLayout = cardView.findViewById(R.id.phase_details_container);
                        LinearLayout skillsChecklistContainer = cardView.findViewById(R.id.phase_skills_checklist_container);
                        TextView milestonesText = cardView.findViewById(R.id.phase_milestones);
                        
                        // Mini Project widgets
                        TextView projNameText = cardView.findViewById(R.id.phase_project_name);
                        TextView projDescText = cardView.findViewById(R.id.phase_project_desc);
                        TextView projOutcomeText = cardView.findViewById(R.id.phase_project_outcome);
                        
                        // Resource widgets
                        TextView resYoutubeText = cardView.findViewById(R.id.phase_res_youtube);
                        TextView resDocsText = cardView.findViewById(R.id.phase_res_docs);
                        TextView resPracticeText = cardView.findViewById(R.id.phase_res_practice);
                        
                        TextView topicsText = cardView.findViewById(R.id.phase_topics);
                        LinearLayout headerLayout = cardView.findViewById(R.id.phase_header_layout);

                        // Populate item data
                        nameText.setText(phaseObj.getString("phase_name"));
                        durationText.setText(phaseObj.getString("duration"));

                        // Dynamic Checkboxes for Skills
                        skillsChecklistContainer.removeAllViews();
                        JSONArray skillsArray = phaseObj.getJSONArray("skills");
                        for (int j = 0; j < skillsArray.length(); j++) {
                            CheckBox checkBox = new CheckBox(CareerRoadmapActivity.this);
                            checkBox.setText(skillsArray.getString(j));
                            checkBox.setTextColor(ContextCompat.getColor(CareerRoadmapActivity.this, R.color.text_secondary));
                            checkBox.setTextSize(13);
                            checkBox.setPadding(0, 8, 0, 8);
                            skillsChecklistContainer.addView(checkBox);
                        }

                        // Weekly Milestones
                        JSONArray milestonesArray = phaseObj.getJSONArray("weekly_milestones");
                        StringBuilder milestonesBuilder = new StringBuilder();
                        for (int m = 0; m < milestonesArray.length(); m++) {
                            milestonesBuilder.append("• ").append(milestonesArray.getString(m));
                            if (m < milestonesArray.length() - 1) {
                                milestonesBuilder.append("\n");
                            }
                        }
                        milestonesText.setText(milestonesBuilder.toString());

                        // Enhanced Mini Project data
                        JSONObject projObj = phaseObj.getJSONObject("mini_project");
                        projNameText.setText("Project: " + projObj.getString("project_name"));
                        projDescText.setText(projObj.getString("description"));
                        projOutcomeText.setText("Expected Outcome: " + projObj.getString("expected_outcome"));

                        // Resources data
                        JSONObject resObj = phaseObj.getJSONObject("resources");
                        resYoutubeText.setText("YouTube: " + resObj.getString("youtube"));
                        resDocsText.setText("Docs: " + resObj.getString("documentation"));
                        resPracticeText.setText("Practice: " + resObj.getString("practice"));

                        // Build Interview Topics list
                        JSONArray topicsArray = phaseObj.getJSONArray("interview_topics");
                        StringBuilder topicsBuilder = new StringBuilder();
                        for (int k = 0; k < topicsArray.length(); k++) {
                            topicsBuilder.append("• ").append(topicsArray.getString(k));
                            if (k < topicsArray.length() - 1) {
                                topicsBuilder.append("\n");
                            }
                        }
                        topicsText.setText(topicsBuilder.toString());

                        // Expand/Collapse click interactions
                        headerLayout.setOnClickListener(v -> {
                            if (detailsLayout.getVisibility() == View.VISIBLE) {
                                detailsLayout.setVisibility(View.GONE);
                                arrowText.setText("▼");
                            } else {
                                detailsLayout.setVisibility(View.VISIBLE);
                                arrowText.setText("▲");
                            }
                        });

                        // Add card view to container list
                        cardsContainer.addView(cardView);
                    }

                    // Render Final Outcome Card
                    JSONObject finalOutcomeObj = responseJson.getJSONObject("final_outcome");
                    int readiness = finalOutcomeObj.getInt("job_readiness_pct");
                    readinessPct.setText(readiness + "%");
                    readinessProgress.setProgress(readiness);

                    JSONArray outcomeSkillsArray = finalOutcomeObj.getJSONArray("skills_learned");
                    StringBuilder outcomeSkillsBuilder = new StringBuilder();
                    for (int s = 0; s < outcomeSkillsArray.length(); s++) {
                        outcomeSkillsBuilder.append("• ").append(outcomeSkillsArray.getString(s));
                        if (s < outcomeSkillsArray.length() - 1) {
                            outcomeSkillsBuilder.append("\n");
                        }
                    }
                    outcomeSkillsText.setText(outcomeSkillsBuilder.toString());
                    outcomeCard.setVisibility(View.VISIBLE);

                    // Render Job Roles Card
                    JSONArray jobRolesArray = responseJson.getJSONArray("job_roles");
                    StringBuilder jobRolesBuilder = new StringBuilder();
                    for (int jr = 0; jr < jobRolesArray.length(); jr++) {
                        jobRolesBuilder.append("• ").append(jobRolesArray.getString(jr));
                        if (jr < jobRolesArray.length() - 1) {
                            jobRolesBuilder.append("\n");
                        }
                    }
                    jobRolesVal.setText(jobRolesBuilder.toString());
                    rolesCard.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    Log.e(TAG, "JSON Parsing failed", e);
                    showParsingErrorCard(responseText);
                }
            }

            @Override
            public void onFailure(int statusCode, String errorMessage) {
                generateBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(CareerRoadmapActivity.this, "Request failed: Status " + statusCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showParsingErrorCard(String rawResponse) {
        summaryCard.setVisibility(View.GONE);
        insightsCard.setVisibility(View.GONE);
        outcomeCard.setVisibility(View.GONE);
        rolesCard.setVisibility(View.GONE);
        cardsContainer.removeAllViews();

        TextView errorText = new TextView(this);
        errorText.setPadding(32, 32, 32, 32);
        errorText.setText("AI output parsing error. Tap 'Generate Roadmap' again to retry.\n\nRaw Output:\n" + rawResponse);
        errorText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        errorText.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_bg));
        errorText.setTextIsSelectable(true);
        cardsContainer.addView(errorText);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
