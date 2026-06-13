package com.techiguru.aicareerpilot;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AiTestActivity extends AppCompatActivity {

    private EditText promptInput;
    private Button sendBtn;
    private View loadingLayout;
    private TextView responseOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_test);

        // Adjust for Edge to Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        promptInput = findViewById(R.id.ai_prompt_input);
        sendBtn = findViewById(R.id.ai_send_btn);
        loadingLayout = findViewById(R.id.ai_loading_layout);
        responseOutput = findViewById(R.id.ai_response_output);

        // Back navigation
        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Send action
        sendBtn.setOnClickListener(v -> handleSendPrompt());
    }

    private void handleSendPrompt() {
        String prompt = promptInput.getText().toString().trim();

        if (TextUtils.isEmpty(prompt)) {
            promptInput.setError("Please enter a prompt first");
            promptInput.requestFocus();
            return;
        }

        // Dismiss Keyboard
        dismissKeyboard();

        // Enable Loading States
        sendBtn.setEnabled(false);
        loadingLayout.setVisibility(View.VISIBLE);
        responseOutput.setVisibility(View.VISIBLE);
        responseOutput.setAlpha(1.0f);
        responseOutput.setText("Thinking...");
        responseOutput.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        // Call Groq Async service with simplified formatting instruction appended
        String apiPrompt = prompt + "\n\n(Provide a direct, clear response in simple formatting. Use paragraphs, bold key terms where helpful, and standard bullet lists. No complex headers, symbols, or multiple structured sections.)";
        GroqApiService.sendPrompt(apiPrompt, new GroqApiService.GroqCallback() {
            @Override
            public void onSuccess(String responseText) {
                sendBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);
                responseOutput.setVisibility(View.VISIBLE);
                responseOutput.setAlpha(1.0f);

                String finalResult = responseText;
                if (finalResult == null || finalResult.trim().isEmpty()) {
                    finalResult = "No response received";
                }
                Log.d("AiTestActivity", "Groq answer received. Length: " + finalResult.length());

                try {
                    responseOutput.setText(formatApiResponse(AiTestActivity.this, finalResult));
                } catch (Exception e) {
                    Log.e("AiTestActivity", "Failed to format Groq answer", e);
                    responseOutput.setText(finalResult);
                }
                responseOutput.setTextColor(ContextCompat.getColor(AiTestActivity.this, R.color.text_primary));

                responseOutput.requestLayout();
                responseOutput.invalidate();

                View scrollView = findViewById(R.id.ai_response_scroll);
                if (scrollView instanceof android.widget.ScrollView) {
                    ((android.widget.ScrollView) scrollView).post(
                            () -> ((android.widget.ScrollView) scrollView).fullScroll(View.FOCUS_UP)
                    );
                }

                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(500);
                fadeIn.setInterpolator(new android.view.animation.DecelerateInterpolator());
                responseOutput.startAnimation(fadeIn);
            }

            @Override
            public void onFailure(int statusCode, String errorMessage) {
                // Restore UI State & Stop Loading indicator immediately
                sendBtn.setEnabled(true);
                loadingLayout.setVisibility(View.GONE);
                responseOutput.setVisibility(View.VISIBLE);
                responseOutput.setAlpha(1.0f);

                // Format friendly user error based on status codes
                String uiErrorMsg;
                if (statusCode == 401) {
                    uiErrorMsg = "API Authentication Failed (Status 401).\nThe configured API Key is invalid or expired. Please verify your credentials.";
                } else if (statusCode == 429) {
                    uiErrorMsg = "Rate Limit Exceeded (Status 429).\nToo many requests are being sent. Please wait a moment and try again.";
                } else if (statusCode >= 500) {
                    uiErrorMsg = "Groq Server Error (Status " + statusCode + ").\nThe server encountered an internal error. Please try again later.";
                } else if (statusCode == -2) {
                    uiErrorMsg = "Connection Timeout.\nThe request exceeded the 15-second response limit. Please check your internet connectivity.";
                } else {
                    uiErrorMsg = "API Connection Failed.\nDetails: " + (errorMessage != null ? errorMessage : "Unknown network error");
                }

                // Show error details in chat UI
                responseOutput.setText(uiErrorMsg);
                responseOutput.setTextColor(ContextCompat.getColor(AiTestActivity.this, R.color.error_red));
                
                Toast.makeText(AiTestActivity.this, "Request failed: Status " + statusCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dismissKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private SpannableStringBuilder formatApiResponse(Context context, String rawText) {
        if (rawText == null) return new SpannableStringBuilder("");

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] lines = rawText.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Normalize paragraph spacing: if previous line wasn't empty and this is empty
            if (line.trim().isEmpty()) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
                    builder.append("\n");
                }
                builder.append("\n");
                continue;
            }

            // Handle Horizontal Dividers
            String lineTrimmed = line.trim();
            if (lineTrimmed.equals("---") || lineTrimmed.equals("***") || lineTrimmed.matches("^\\-{3,}$") || lineTrimmed.matches("^\\*{3,}$")) {
                int start = builder.length();
                builder.append("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯").append("\n\n");
                int end = builder.length() - 2; // exclude newlines
                builder.setSpan(new android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_CENTER), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_muted)), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            // Handle Headings
            if (line.startsWith("#")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') {
                    level++;
                }
                if (level > 0 && level < line.length() && line.charAt(level) == ' ') {
                    String headingText = line.substring(level + 1).trim();
                    if (!headingText.isEmpty()) {
                        int start = builder.length();
                        builder.append(headingText).append("\n\n");
                        int end = builder.length() - 2; // exclude newlines

                        // Style Heading: Bold + Slightly Larger Text + Premium White color
                        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        float sizeMultiplier = level == 1 ? 1.2f : (level == 2 ? 1.15f : 1.1f);
                        builder.setSpan(new RelativeSizeSpan(sizeMultiplier), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        continue;
                    }
                }
            }

            // Handle Bullet Lists
            boolean isBullet = false;
            String bulletPrefix = "";
            if (lineTrimmed.startsWith("- ") || lineTrimmed.startsWith("* ")) {
                isBullet = true;
                int idx = line.indexOf(lineTrimmed.startsWith("- ") ? "-" : "*");
                bulletPrefix = line.substring(0, idx);
                line = lineTrimmed.substring(2);
            } else if (lineTrimmed.startsWith("• ")) {
                isBullet = true;
                int idx = line.indexOf("•");
                bulletPrefix = line.substring(0, idx);
                line = lineTrimmed.substring(2);
            }

            if (isBullet) {
                // Add sentence spacing to bullet text
                line = line.replaceAll("\\. +", ".  ");
                
                int start = builder.length();
                builder.append(bulletPrefix).append("  •  ").append(line).append("\n");
                int end = builder.length() - 1;

                // Color the bullet symbol white for a clean look
                int bulletIndex = start + bulletPrefix.length() + 2;
                builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), bulletIndex, bulletIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new StyleSpan(Typeface.BOLD), bulletIndex, bulletIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Apply padding margin
                builder.setSpan(new LeadingMarginSpan.Standard(16, 32), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            // Handle Numbered Lists
            boolean isNumbered = false;
            String numPrefix = "";
            String numPart = "";
            int dotIndex = lineTrimmed.indexOf(". ");
            if (dotIndex > 0 && dotIndex < 5) {
                String possibleNum = lineTrimmed.substring(0, dotIndex);
                if (possibleNum.matches("\\d+")) {
                    isNumbered = true;
                    int idx = line.indexOf(possibleNum + ". ");
                    numPrefix = line.substring(0, idx);
                    line = lineTrimmed.substring(dotIndex + 2);
                    numPart = possibleNum + ". ";
                }
            }

            if (isNumbered) {
                // Add sentence spacing to numbered list text
                line = line.replaceAll("\\. +", ".  ");
                
                int start = builder.length();
                builder.append(numPrefix).append("  ").append(numPart).append("  ").append(line).append("\n");
                int end = builder.length() - 1;

                // Style the number prefix in white for consistency
                int numberIndex = start + numPrefix.length() + 2;
                builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), numberIndex, numberIndex + numPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new StyleSpan(Typeface.BOLD), numberIndex, numberIndex + numPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Apply padding margin
                builder.setSpan(new LeadingMarginSpan.Standard(16, 32), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            // Normal text line
            line = line.replaceAll("\\. +", ".  ");
            builder.append(line).append("\n");
        }

        // Apply inline formatting like bold (**text**)
        applyInlineFormatting(builder, context);

        return builder;
    }

    private void applyInlineFormatting(SpannableStringBuilder builder, Context context) {
        int boldStart = -1;
        for (int i = 0; i < builder.length() - 1; i++) {
            if (builder.charAt(i) == '*' && builder.charAt(i + 1) == '*') {
                if (boldStart == -1) {
                    boldStart = i;
                    i++; // skip second '*'
                } else {
                    int start = boldStart;
                    int end = i;
                    
                    // Remove ending '**'
                    builder.delete(end, end + 2);
                    // Remove starting '**'
                    builder.delete(start, start + 2);
                    
                    int actualStart = start;
                    int actualEnd = end - 2;
                    
                    // Style with Bold and primary clean white text color
                    builder.setSpan(new StyleSpan(Typeface.BOLD), actualStart, actualEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), actualStart, actualEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    
                    i = actualEnd - 1; // update pointer
                    boldStart = -1;
                }
            }
        }
    }
}
