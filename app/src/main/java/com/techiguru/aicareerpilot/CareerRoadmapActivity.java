package com.techiguru.aicareerpilot;

import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.Heading;

public class CareerRoadmapActivity extends AppCompatActivity {

    private static final String TAG = "CareerRoadmapActivity";

    // Selectors
    private Spinner roleSpinner;
    private Spinner skillSpinner;
    private Spinner hoursSpinner;
    private Button generateBtn;
    private View loadingLayout;

    // Response area
    private ScrollView responseScroll;
    private TextView markdownText;

    // Markwon instance
    private Markwon markwon;

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

        // Initialize Markwon with premium theming
        initMarkwon();

        // Initialize UI Elements
        roleSpinner = findViewById(R.id.roadmap_role_spinner);
        skillSpinner = findViewById(R.id.roadmap_skill_spinner);
        hoursSpinner = findViewById(R.id.roadmap_hours_spinner);
        generateBtn = findViewById(R.id.roadmap_generate_btn);
        loadingLayout = findViewById(R.id.roadmap_loading_layout);
        responseScroll = findViewById(R.id.roadmap_response_scroll);
        markdownText = findViewById(R.id.roadmap_markdown_text);

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

    /**
     * Initialize Markwon with custom theme to match the app's dark premium design.
     */
    private void initMarkwon() {
        int accentColor = ContextCompat.getColor(this, R.color.colorAccent);     // #00D4FF
        int primaryText = ContextCompat.getColor(this, R.color.text_primary);    // #FFFFFF
        int secondaryText = ContextCompat.getColor(this, R.color.text_secondary); // #B3BDD1
        int cardBg = ContextCompat.getColor(this, R.color.ai_bar_surface);       // #16233A
        int strokeColor = ContextCompat.getColor(this, R.color.glass_card_stroke); // #1E293B

        markwon = Markwon.builder(this)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(this))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder
                            // Headings
                            .headingBreakHeight(0)
                            .headingTextSizeMultipliers(new float[]{1.6f, 1.35f, 1.15f, 1.05f, 1.0f, 0.9f})
                            // Code
                            .codeTextColor(accentColor)
                            .codeBackgroundColor(cardBg)
                            .codeBlockTextColor(secondaryText)
                            .codeBlockBackgroundColor(cardBg)
                            .codeTextSize((int) (getResources().getDisplayMetrics().scaledDensity * 13))
                            // Block quote
                            .blockQuoteColor(accentColor)
                            .blockQuoteWidth((int) (getResources().getDisplayMetrics().density * 3))
                            // Thematic break (---)
                            .thematicBreakColor(strokeColor)
                            .thematicBreakHeight((int) (getResources().getDisplayMetrics().density * 1))
                            // List items
                            .bulletListItemStrokeWidth((int) (getResources().getDisplayMetrics().density * 1))
                            .listItemColor(accentColor)
                            // Links
                            .linkColor(accentColor);
                    }

                    @Override
                    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
                        // Make headings use accent color
                        builder.addFactory(Heading.class, (configuration, props) ->
                                new ForegroundColorSpan(accentColor));

                        // Make block quotes use secondary text color
                        builder.addFactory(BlockQuote.class, (configuration, props) ->
                                new ForegroundColorSpan(secondaryText));
                    }
                })
                .build();
    }

    private void handleGenerateRoadmap() {
        String selectedRole = roleSpinner.getSelectedItem().toString();
        String selectedLevel = skillSpinner.getSelectedItem().toString();
        String selectedHours = hoursSpinner.getSelectedItem().toString();

        // Markdown-based prompt — asks for rich, formatted text output (NOT JSON)
        String prompt = "/no_think\n" +
                "You are CareerPilot AI, a premium career advisor.\n\n" +
                "Generate a detailed, beautifully formatted career roadmap for becoming a **" + selectedRole + "**.\n" +
                "Current skill level: **" + selectedLevel + "**\n" +
                "Daily study time: **" + selectedHours + "/day**\n\n" +
                "CRITICAL RULES:\n" +
                "- Output ONLY Markdown text. NO JSON. NO code fences wrapping the entire response.\n" +
                "- Use rich formatting: headings (#, ##, ###), bold (**text**), bullet points, numbered lists, emojis.\n" +
                "- Make it look like a premium, magazine-quality career guide.\n\n" +
                "Use this EXACT structure:\n\n" +
                "# \uD83D\uDE80 Your [Role] Career Roadmap\n\n" +
                "**Current Level:** [Level]\n" +
                "**Study Time:** [Hours]/Day\n" +
                "**Estimated Duration:** [Duration]\n" +
                "**Difficulty:** [Difficulty]\n\n" +
                "---\n\n" +
                "## \uD83D\uDCCA Career Overview\n" +
                "[2-3 sentence overview of this career path]\n\n" +
                "### \uD83D\uDCB0 Career Insights\n" +
                "- **Average Salary:** [salary range]\n" +
                "- **Market Demand:** [level]\n" +
                "- **Competition:** [level]\n" +
                "- **Growth Potential:** [level]\n\n" +
                "---\n\n" +
                "# \uD83D\uDDFA\uFE0F Learning Roadmap\n\n" +
                "[For each phase use ## Phase N — Title, include duration, difficulty, ### What You'll Learn (bullet list), ### Skills to Master, ### Practice Projects (numbered), ### Recommended Resources]\n\n" +
                "Include 3 phases with --- between each.\n\n" +
                "---\n\n" +
                "# \uD83D\uDE80 Final Projects\n" +
                "[2-3 capstone project ideas with tech stack]\n\n" +
                "---\n\n" +
                "# \uD83D\uDCBC Career Path\n" +
                "[Show career progression]\n\n" +
                "---\n\n" +
                "# \u2705 Final Checklist\n" +
                "[Checklist of things to complete before applying]\n\n" +
                "---\n\n" +
                "## \uD83C\uDFAF Final Advice\n" +
                "[Motivational closing paragraph with a blockquote tip]\n";

        // Enable loading indicator state
        generateBtn.setEnabled(false);
        loadingLayout.setVisibility(View.VISIBLE);
        responseScroll.setVisibility(View.GONE);

        GroqApiService.sendPrompt(prompt, new GroqApiService.GroqCallback() {
            @Override
            public void onSuccess(String responseText) {
                generateBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);

                // Sanitize response — strip thinking tags and code fences
                String cleanMarkdown = sanitizeMarkdownResponse(responseText);

                // Render Markdown into the TextView
                markwon.setMarkdown(markdownText, cleanMarkdown);

                // Show the response area and scroll to top
                responseScroll.setVisibility(View.VISIBLE);
                responseScroll.post(() -> responseScroll.scrollTo(0, 0));
            }

            @Override
            public void onFailure(int statusCode, String errorMessage) {
                generateBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(CareerRoadmapActivity.this,
                        "Request failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clean up the AI response — remove thinking blocks, outer code fences, etc.
     */
    private String sanitizeMarkdownResponse(String response) {
        if (response == null) return "";
        String cleaned = response.trim();

        // Strip <think>...</think> blocks
        cleaned = cleaned.replaceAll("(?s)<think>.*?</think>", "").trim();

        // If the entire response is wrapped in ```markdown ... ``` or ``` ... ```, strip it
        if (cleaned.startsWith("```markdown")) {
            cleaned = cleaned.substring(11);
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
        }

        return cleaned;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
