package com.techiguru.aicareerpilot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InterviewSimulatorActivity extends AppCompatActivity {

    private static final String TAG = "InterviewSimulator";
    private static final int REQUEST_RECORD_AUDIO = 200;

    // Chat / Voice layout views
    private View chatLayout;
    private TextView statusText;
    private TextView statusBadgeText;
    private View aiPulse1;
    private View aiPulse2;
    private TextView questionText;
    private TextView transcriptionText;
    
    // Mic views
    private View micPulse1;
    private View micPulse2;
    private ImageButton micBtn;
    private TextView endBtn;

    // Evaluation layout views
    private View evaluationLayout;
    private TextView scoreValText;
    private TextView verdictBadgeText;
    private TextView summaryText;
    private TextView strengthsText;
    private TextView weaknessesText;
    private TextView recommendationsText;
    private Button returnBtn;

    // Session Data
    private String role;
    private String difficulty;
    private String topic;
    private String voiceType;   // "male" | "female" | "default"
    private String personaName; // Always "Black Widow"
    private String currentQuestion = ""; // Last question spoken to the user
    private boolean isInHelpMode = false; // True while a help-mode response is being processed
    private JSONArray conversationHistory;
    private boolean isProcessing = false;

    // Voice Engine Core
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isTtsReady = false;
    private boolean isAiSpeaking = false;
    private boolean isRecording = false;
    private String currentTranscribedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_interview_simulator);

        // Adjust for Edge to Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load intent parameters
        Intent intent = getIntent();
        role = intent.getStringExtra("role");
        difficulty = intent.getStringExtra("difficulty");
        topic = intent.getStringExtra("topic");
        voiceType = intent.getStringExtra("voice_type");

        if (role == null) role = "Software Engineer";
        if (difficulty == null) difficulty = "Mid-Level";
        if (topic == null) topic = "Coding & Algorithms";
        if (voiceType == null) voiceType = "male";

        // Persona is always "Black Widow" — fixed brand identity
        personaName = "Black Widow";

        // Bind Console Views
        chatLayout = findViewById(R.id.chat_container_layout);
        statusText = findViewById(R.id.sim_status_text);
        statusBadgeText = findViewById(R.id.sim_status_badge);
        aiPulse1 = findViewById(R.id.sim_ai_pulse_1);
        aiPulse2 = findViewById(R.id.sim_ai_pulse_2);
        questionText = findViewById(R.id.sim_question_text);
        transcriptionText = findViewById(R.id.sim_transcription_text);

        // Bind Mic Views
        micPulse1 = findViewById(R.id.sim_mic_pulse_1);
        micPulse2 = findViewById(R.id.sim_mic_pulse_2);
        micBtn = findViewById(R.id.sim_mic_btn);
        endBtn = findViewById(R.id.sim_end_btn);

        // Bind Evaluation Screen Views
        evaluationLayout = findViewById(R.id.evaluation_container_layout);
        scoreValText = findViewById(R.id.eval_score_val);
        verdictBadgeText = findViewById(R.id.eval_verdict_badge);
        summaryText = findViewById(R.id.eval_summary_text);
        strengthsText = findViewById(R.id.eval_strengths_text);
        weaknessesText = findViewById(R.id.eval_weaknesses_text);
        recommendationsText = findViewById(R.id.eval_recommendations_text);
        returnBtn = findViewById(R.id.back_to_prep_btn);

        // Set top status details
        statusText.setText(role + " (" + difficulty + ") • " + topic);

        // Header actions
        ImageButton backBtn = findViewById(R.id.sim_back_btn);
        backBtn.setOnClickListener(v -> showExitConfirmationDialog());
        endBtn.setOnClickListener(v -> showEndInterviewConfirmationDialog());
        
        // Return button on Evaluation screen
        returnBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Mic Button toggle handler
        micBtn.setOnClickListener(v -> handleMicClick());

        // Initialize TTS and STT engines
        initVoiceEngines();
    }

    private void initVoiceEngines() {
        // Initialize Text-To-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Apply gender-specific voice, pitch, and speed settings
                applyTtsVoice(voiceType);
                isTtsReady = true;
                // Trigger permission check and begin after TTS is loaded
                checkAudioPermissions();
            } else {
                Log.e(TAG, "Initialization of TTS failed.");
                Toast.makeText(this, "Text to speech failed to load", Toast.LENGTH_SHORT).show();
            }
        });

        // Set progress listener to auto-trigger mic once speaking ends
        textToSpeech.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isAiSpeaking = true;
                runOnUiThread(() -> {
                    statusBadgeText.setText("AI is speaking...");
                    statusBadgeText.setTextColor(ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.electric_cyan));
                    startAiPulseAnimation();
                });
            }

            @Override
            public void onDone(String utteranceId) {
                isAiSpeaking = false;
                runOnUiThread(() -> {
                    aiPulse1.setVisibility(View.INVISIBLE);
                    aiPulse2.setVisibility(View.INVISIBLE);

                    if ("intro".equals(utteranceId)) {
                        // Step 2: chain immediately into first fixed question
                        speakFirstQuestion();
                    } else {
                        // All other utterances (first_question, interviewer_speak):
                        // hand over to user mic
                        startListening();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                isAiSpeaking = false;
                runOnUiThread(() -> {
                    aiPulse1.setVisibility(View.INVISIBLE);
                    aiPulse2.setVisibility(View.INVISIBLE);
                    statusBadgeText.setText("Ready - Tap Mic to answer");
                    statusBadgeText.setTextColor(ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.text_secondary));
                });
            }
        });

        // Initialize SpeechRecognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());

            // Prepare voice listener intents
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        } else {
            Toast.makeText(this, "Speech recognition is not available on this device", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Selects the best available TTS Voice for the requested gender using a
     * 4-tier priority chain so the experience degrades gracefully on any device.
     *
     * Priority:
     *   1. Voice whose name contains a gender keyword AND locale is en-US or en-GB
     *   2. Voice whose name contains a known Google TTS gender code (male/female pool)
     *   3. First non-default en-US/en-GB voice (for female, avoids the male default)
     *   4. Plain setLanguage(Locale.US) — last-resort system default
     */
    private void applyTtsVoice(String type) {
        // Speech rate: slightly slower for calm professional pacing
        textToSpeech.setSpeechRate(0.85f);

        // Gender-specific pitch tweak
        switch (type) {
            case "female":  textToSpeech.setPitch(1.10f); break;
            case "default": textToSpeech.setPitch(1.00f); break;
            default:        textToSpeech.setPitch(0.88f); break; // male — slightly deeper
        }

        if (type.equals("default")) {
            // Just use the locale default — no explicit Voice object needed
            textToSpeech.setLanguage(Locale.US);
            Log.d(TAG, "TTS: using system default voice");
            return;
        }

        try {
            Set<Voice> voices = textToSpeech.getVoices();
            if (voices == null || voices.isEmpty()) {
                Log.w(TAG, "TTS: no voices available — falling back to setLanguage");
                textToSpeech.setLanguage(Locale.US);
                return;
            }

            // Known Google TTS internal voice name fragments for each gender.
            // Male codes:   iom, sfg, col, iob, tpc, n3, aue
            // Female codes: lfn, sfn, wfd, rjs, iof, wnu, wol
            List<String> maleCodes   = java.util.Arrays.asList("iom", "sfg", "col", "iob", "tpc", "n3", "aue");
            List<String> femaleCodes = java.util.Arrays.asList("lfn", "sfn", "wfd", "rjs", "iof", "wnu", "wol");

            boolean wantMale = type.equals("male");

            // --- Priority 1: name contains explicit gender keyword ---
            for (Voice v : voices) {
                String name = v.getName().toLowerCase(Locale.US);
                Locale loc = v.getLocale();
                boolean isEnglish = loc != null &&
                        (loc.getLanguage().equals("en") &&
                                (loc.getCountry().equals("US") || loc.getCountry().equals("GB")));
                if (!isEnglish) continue;

                boolean nameMatchesMale   = name.contains("male") && !name.contains("female");
                boolean nameMatchesFemale = name.contains("female");

                if (wantMale && nameMatchesMale) {
                    textToSpeech.setVoice(v);
                    Log.d(TAG, "TTS P1 male: " + v.getName());
                    return;
                }
                if (!wantMale && nameMatchesFemale) {
                    textToSpeech.setVoice(v);
                    Log.d(TAG, "TTS P1 female: " + v.getName());
                    return;
                }
            }

            // --- Priority 2: name contains a known Google TTS gender code ---
            List<String> targetCodes = wantMale ? maleCodes : femaleCodes;
            for (Voice v : voices) {
                String name = v.getName().toLowerCase(Locale.US);
                Locale loc = v.getLocale();
                boolean isEnglish = loc != null &&
                        loc.getLanguage().equals("en") &&
                        (loc.getCountry().equals("US") || loc.getCountry().equals("GB"));
                if (!isEnglish) continue;

                for (String code : targetCodes) {
                    if (name.contains(code)) {
                        textToSpeech.setVoice(v);
                        Log.d(TAG, "TTS P2 (" + type + ") matched code '" + code + "': " + v.getName());
                        return;
                    }
                }
            }

            // --- Priority 3: first non-default en-US voice ---
            Voice defaultVoice = textToSpeech.getDefaultVoice();
            for (Voice v : voices) {
                Locale loc = v.getLocale();
                boolean isEnglish = loc != null &&
                        loc.getLanguage().equals("en") &&
                        (loc.getCountry().equals("US") || loc.getCountry().equals("GB"));
                if (!isEnglish) continue;
                if (defaultVoice != null && v.getName().equals(defaultVoice.getName())) continue;

                textToSpeech.setVoice(v);
                Log.d(TAG, "TTS P3 non-default fallback: " + v.getName());
                return;
            }

            // --- Priority 4: absolute fallback ---
            textToSpeech.setLanguage(Locale.US);
            Log.w(TAG, "TTS P4: all priority tiers failed — using setLanguage fallback");

        } catch (Exception e) {
            Log.e(TAG, "applyTtsVoice exception — falling back to setLanguage", e);
            textToSpeech.setLanguage(Locale.US);
        }
    }

    private void checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        } else {
            // Already approved -> begin interview
            initializeConversation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
                initializeConversation();
            } else {
                Toast.makeText(this, "Microphone permission is required for the Voice Simulator.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeConversation() {
        conversationHistory = new JSONArray();

        try {
            // System prompt — Black Widow persona, short feedback format
            String systemInstructions =
                    "You are Black Widow, an elite FAANG-level AI Interviewer conducting a professional spoken interview.\n\n" +
                    "Session parameters:\n" +
                    "  Role being interviewed for: " + role + "\n" +
                    "  Difficulty: " + difficulty + "\n" +
                    "  Focus Topic: " + topic + "\n\n" +
                    "STRICT RULES:\n" +
                    "1. The introduction and the first question ('Introduce yourself') are already handled by the app. " +
                    "   Do NOT repeat them.\n" +
                    "2. After the user answers, give SHORT feedback — exactly ONE of these two phrases:\n" +
                    "   - 'Good answer.' — if the response was solid\n" +
                    "   - 'It could be better.' — if the response was weak or incomplete\n" +
                    "   Then add ONE concise improvement sentence (max 15 words) if applicable.\n" +
                    "3. After feedback, ask ONE adaptive follow-up question based on the user's previous answer and performance.\n" +
                    "4. Keep total spoken output under 3 sentences. No long paragraphs.\n" +
                    "5. Maintain a strict, professional, FAANG-style tone throughout.\n" +
                    "6. ALWAYS format your response exactly like this:\n" +
                    "Feedback: [Good answer. | It could be better. <optional one improvement sentence>]\n" +
                    "Next Question: [Your adaptive question here]";

            JSONObject systemObj = new JSONObject();
            systemObj.put("role", "system");
            systemObj.put("content", systemInstructions);
            conversationHistory.put(systemObj);

            // Log the first question into history so AI knows it was asked
            JSONObject firstQObj = new JSONObject();
            firstQObj.put("role", "assistant");
            firstQObj.put("content", "Feedback: None\nNext Question: Introduce yourself.");
            conversationHistory.put(firstQObj);

            // Begin fixed intro sequence (no API call needed)
            speakIntroSequence();

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize conversation", e);
        }
    }

    /**
     * Step 1 of the fixed start sequence.
     * Speaks: "Hello, I am Black Widow. Let's start your interview."
     * On completion, the UtteranceProgressListener chains to speakFirstQuestion().
     */
    private void speakIntroSequence() {
        runOnUiThread(() -> {
            statusBadgeText.setText("AI is speaking...");
            statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.electric_cyan));
            questionText.setText("Hello, I am Black Widow. Let's start your interview.");
            transcriptionText.setText("Preparing your interview session...");
        });

        if (textToSpeech == null || !isTtsReady) return;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro");
        textToSpeech.speak(
                "Hello, I am Black Widow. Let's start your interview.",
                TextToSpeech.QUEUE_FLUSH, params, "intro");
    }

    /**
     * Step 2 of the fixed start sequence.
     * Speaks the hardcoded first question: "Introduce yourself."
     * Also seeds currentQuestion so Help Mode can re-ask it if triggered.
     * On completion, the UtteranceProgressListener calls startListening().
     */
    private void speakFirstQuestion() {
        currentQuestion = "Introduce yourself."; // seed for Help Mode
        runOnUiThread(() -> {
            questionText.setText("Introduce yourself.");
            transcriptionText.setText("Awaiting your response...");
        });

        if (textToSpeech == null || !isTtsReady) return;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "first_question");
        textToSpeech.speak(
                "Introduce yourself.",
                TextToSpeech.QUEUE_ADD, params, "first_question");
    }

    // fetchFirstQuestion() removed — intro sequence is now handled locally by
    // speakIntroSequence() → speakFirstQuestion() → startListening()

    private void saveAssistantMessage(String content) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("role", "assistant");
            obj.put("content", content);
            conversationHistory.put(obj);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save assistant message", e);
        }
    }

    private void handleInterviewerResponse(String responseText) {
        String feedback = "";
        String nextQuestion = responseText;

        if (responseText.contains("Feedback:") && responseText.contains("Next Question:")) {
            int feedbackIndex = responseText.indexOf("Feedback:");
            int nextQuestionIndex = responseText.indexOf("Next Question:");
            if (feedbackIndex < nextQuestionIndex) {
                feedback = responseText.substring(feedbackIndex + 9, nextQuestionIndex).trim();
                nextQuestion = responseText.substring(nextQuestionIndex + 14).trim();
            }
        }

        // Track the current question for Help Mode
        currentQuestion = nextQuestion;
        isInHelpMode = false; // resume normal mode after a standard response

        final String finalFeedback = feedback;
        final String finalQuestion = nextQuestion;

        runOnUiThread(() -> {
            questionText.setText(finalQuestion);

            // Display feedback with visual label
            boolean hasFeedback = !finalFeedback.isEmpty()
                    && !finalFeedback.equalsIgnoreCase("None")
                    && !finalFeedback.equalsIgnoreCase("N/A");

            if (hasFeedback) {
                String display = finalFeedback.toLowerCase(Locale.US).startsWith("good")
                        ? "✅  " + finalFeedback
                        : "💡  " + finalFeedback;
                transcriptionText.setText(display);
            } else {
                transcriptionText.setText("Awaiting your response...");
            }

            // Speak feedback (if any) then the next question
            String speechString = hasFeedback ? finalFeedback + "  " : "";
            speechString += finalQuestion;
            speakText(speechString);
        });
    }

    private void speakText(String text) {
        if (textToSpeech == null || !isTtsReady) return;

        if (isRecording) {
            stopListening();
        }

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "interviewer_speak");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "interviewer_speak");
    }

    private void startListening() {
        if (speechRecognizer == null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkAudioPermissions();
            return;
        }

        runOnUiThread(() -> {
            isRecording = true;
            currentTranscribedText = "";
            micPulse1.setVisibility(View.VISIBLE);
            micPulse2.setVisibility(View.VISIBLE);
            micBtn.setImageResource(R.drawable.ic_mic);
            statusBadgeText.setText("Listening...");
            statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.emerald_green));
            transcriptionText.setText("Listening...");
            
            try {
                speechRecognizer.startListening(recognizerIntent);
            } catch (Exception e) {
                Log.e(TAG, "SpeechRecognizer start failed", e);
                statusBadgeText.setText("STT Error");
            }
        });
    }

    private void stopListening() {
        if (speechRecognizer == null) return;
        runOnUiThread(() -> {
            isRecording = false;
            micPulse1.setVisibility(View.INVISIBLE);
            micPulse2.setVisibility(View.INVISIBLE);
            micBtn.setImageResource(R.drawable.ic_mic_off);
            speechRecognizer.stopListening();
        });
    }

    private void handleMicClick() {
        if (isProcessing) return;

        if (isRecording) {
            stopListening();
            // Trigger processing immediately with whatever was transcribed
            if (!TextUtils.isEmpty(currentTranscribedText) && !currentTranscribedText.equals("Listening...")) {
                sendUserAnswer(currentTranscribedText);
            } else {
                statusBadgeText.setText("Ready - Tap Mic to answer");
                statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                transcriptionText.setText("No speech captured. Tap Mic to talk.");
            }
        } else {
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
                isAiSpeaking = false;
                aiPulse1.setVisibility(View.INVISIBLE);
                aiPulse2.setVisibility(View.INVISIBLE);
            }
            startListening();
        }
    }

    private void sendUserAnswer(String answerText) {
        // ── Priority 1: Repeat Question ("pardon") ────────────────────────────
        // Pure local TTS repeat — zero API call.
        if (detectPardon(answerText)) {
            repeatCurrentQuestion();
            return;
        }

        // ── Priority 2: Help / Target-Answer Mode ─────────────────────────────
        // Confusion signals: AI gives a direct concept explanation then moves
        // to the NEXT question (does NOT re-ask the same one).
        if (!isInHelpMode && detectConfusion(answerText)) {
            sendHelpModeRequest(answerText);
            return;
        }
        isInHelpMode = false; // clear flag — normal answer received
        // ─────────────────────────────────────────────────────────────────────

        try {
            isProcessing = true;
            runOnUiThread(() -> {
                statusBadgeText.setText("Processing answer...");
                statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.soft_orange));
                transcriptionText.setText("Answer: \"" + answerText + "\"");
            });

            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", answerText);
            conversationHistory.put(userObj);

            GroqApiService.sendChatHistory(conversationHistory, new GroqApiService.GroqCallback() {
                @Override
                public void onSuccess(String responseText) {
                    isProcessing = false;
                    saveAssistantMessage(responseText);
                    handleInterviewerResponse(responseText);
                }

                @Override
                public void onFailure(int statusCode, String errorMessage) {
                    isProcessing = false;
                    runOnUiThread(() -> {
                        statusBadgeText.setText("Error - Tap Mic to retry");
                        statusBadgeText.setTextColor(ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.error_red));
                        Toast.makeText(InterviewSimulatorActivity.this, "Request failed. Status " + statusCode, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error sending user response", e);
            isProcessing = false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  COMMAND DETECTION
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns true if the user said "pardon" (or similar).
     * Triggers an exact repeat of the current question — no API call.
     */
    private boolean detectPardon(String answer) {
        if (TextUtils.isEmpty(answer)) return false;
        String lower = answer.toLowerCase(Locale.US).trim();
        return lower.equals("pardon")
                || lower.equals("pardon me")
                || lower.contains("repeat the question")
                || lower.contains("say that again")
                || lower.contains("can you repeat");
    }

    /**
     * Speaks the current question again verbatim.
     * No API call, no modification of question text.
     */
    private void repeatCurrentQuestion() {
        if (TextUtils.isEmpty(currentQuestion)) return;
        runOnUiThread(() -> {
            statusBadgeText.setText("🔁  Repeating question...");
            statusBadgeText.setTextColor(
                    ContextCompat.getColor(this, R.color.electric_cyan));
        });
        speakText(currentQuestion);
    }

    /**
     * Returns true when the transcribed answer contains a recognised confusion
     * signal. Matching is case-insensitive.
     *
     * Triggers: Help / Target-Answer Mode (AI explains concept, then moves to
     * the NEXT question — does NOT re-ask the same one).
     */
    private boolean detectConfusion(String answer) {
        if (TextUtils.isEmpty(answer)) return false;
        String lower = answer.toLowerCase(Locale.US).trim();
        return lower.contains("sorry")
                || lower.contains("i don't know")
                || lower.contains("i dont know")
                || lower.contains("no idea")
                || lower.contains("i am not sure")
                || lower.contains("i'm not sure")
                || lower.contains("not sure")
                || lower.contains("i have no idea")
                || lower.contains("i don't understand")
                || lower.contains("i dont understand")
                || lower.contains("i'm confused")
                || lower.contains("i am confused");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELP MODE  (Target-Answer + Next Question)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sends a focused target-answer request to Groq.
     *
     * Rules enforced in the prompt:
     *   • Direct concept explanation only — 2–4 lines MAX.
     *   • No examples, no analogies, no storytelling.
     *   • After the target answer, AI MUST move to the NEXT adaptive question.
     *   • The same question MUST NOT be repeated.
     *
     * Expected AI response format:
     *   Target Answer: <direct concept explanation, 2–4 lines>
     *   Next Question: <next adaptive question, NOT the same one>
     */
    private void sendHelpModeRequest(String confusedAnswer) {
        isInHelpMode = true;
        isProcessing = true;

        runOnUiThread(() -> {
            statusBadgeText.setText("🎯  Target Answer loading...");
            statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.soft_orange));
            transcriptionText.setText("Fetching target answer...");
        });

        try {
            // Record confused answer into main history for context
            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", confusedAnswer);
            conversationHistory.put(userObj);

            // Build a standalone help call with its own focused system prompt
            JSONArray helpHistory = new JSONArray();

            JSONObject helpSystem = new JSONObject();
            helpSystem.put("role", "system");
            helpSystem.put("content",
                    "You are Black Widow, an elite technical interviewer.\n" +
                    "The candidate could not answer the question: \"" + currentQuestion + "\"\n\n" +
                    "Your task:\n" +
                    "1. Give a TARGET ANSWER for that question.\n" +
                    "   - Direct concept explanation only. 2–4 lines MAXIMUM.\n" +
                    "   - NO examples. NO analogies. NO storytelling. NO code.\n" +
                    "2. After the target answer, ask the NEXT interview question.\n" +
                    "   - Do NOT repeat the same question (\"" + currentQuestion + "\").\n" +
                    "   - Follow the adaptive difficulty progression: " + difficulty + ".\n" +
                    "   - Base the next question on the interview topic: " + topic + ".\n\n" +
                    "ALWAYS use this EXACT format (no deviation):\n" +
                    "Target Answer: [Concise concept explanation here]\n" +
                    "Next Question: [Your next adaptive interview question here]");
            helpHistory.put(helpSystem);

            JSONObject trigger = new JSONObject();
            trigger.put("role", "user");
            trigger.put("content", "I don't know the answer. Please explain and continue.");
            helpHistory.put(trigger);

            GroqApiService.sendChatHistory(helpHistory, new GroqApiService.GroqCallback() {
                @Override
                public void onSuccess(String responseText) {
                    isProcessing = false;
                    saveAssistantMessage(responseText);
                    handleHelpModeResponse(responseText);
                }

                @Override
                public void onFailure(int statusCode, String errorMessage) {
                    isProcessing = false;
                    isInHelpMode = false;
                    runOnUiThread(() -> {
                        statusBadgeText.setText("Error - Tap Mic to retry");
                        statusBadgeText.setTextColor(
                                ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.error_red));
                        Toast.makeText(InterviewSimulatorActivity.this,
                                "Help Mode request failed. Status " + statusCode, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in sendHelpModeRequest", e);
            isProcessing = false;
            isInHelpMode = false;
        }
    }

    /**
     * Parses Target Answer: / Next Question: labels from Help Mode response.
     * Displays a 🎯 target answer badge + next question, speaks both,
     * then auto-starts mic for the user to answer the next question.
     */
    private void handleHelpModeResponse(String responseText) {
        String targetAnswer  = "";
        String nextQuestion  = currentQuestion; // safe fallback

        // Parse Target Answer:
        if (responseText.contains("Target Answer:")) {
            int start = responseText.indexOf("Target Answer:") + 14;
            int end   = responseText.contains("Next Question:")
                    ? responseText.indexOf("Next Question:")
                    : responseText.length();
            targetAnswer = responseText.substring(start, end).trim();
        }

        // Parse Next Question: (must be a NEW question, not the same one)
        if (responseText.contains("Next Question:")) {
            String parsed = responseText
                    .substring(responseText.indexOf("Next Question:") + 14).trim();
            if (!parsed.isEmpty()) {
                nextQuestion = parsed;
                currentQuestion = nextQuestion; // advance tracker
            }
        }

        isInHelpMode = false; // fully exit help mode now that next Q is set

        final String finalTarget   = targetAnswer;
        final String finalNextQ    = nextQuestion;

        runOnUiThread(() -> {
            // Badge: clear help-mode colour, show next question
            statusBadgeText.setText("🎯  Target Answer — Moving on");
            statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.soft_orange));

            // Question area shows the NEXT question
            questionText.setText(finalNextQ);

            // Info area shows the target concept answer
            String display = "🎯  Target Answer:\n" + finalTarget;
            transcriptionText.setText(display);

            // TTS: speak target answer then next question
            String speech = "";
            if (!finalTarget.isEmpty()) speech += finalTarget + "  ";
            speech += "Moving to the next question.  " + finalNextQ;
            speakText(speech);
        });
    }

    // AI speaking pulsating ring animator
    private void startAiPulseAnimation() {
        if (!isAiSpeaking) return;
        runOnUiThread(() -> {
            aiPulse1.setVisibility(View.VISIBLE);
            aiPulse2.setVisibility(View.VISIBLE);

            aiPulse1.animate().scaleX(1.4f).scaleY(1.4f).alpha(0.0f).setDuration(1200).withEndAction(() -> {
                aiPulse1.setScaleX(1.0f); aiPulse1.setScaleY(1.0f); aiPulse1.setAlpha(1.0f);
                if (isAiSpeaking) startAiPulseAnimation();
            }).start();

            aiPulse2.animate().scaleX(1.6f).scaleY(1.6f).alpha(0.0f).setDuration(1600).withEndAction(() -> {
                aiPulse2.setScaleX(1.0f); aiPulse2.setScaleY(1.0f); aiPulse2.setAlpha(1.0f);
                if (isAiSpeaking) startAiPulseAnimation();
            }).start();
        });
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Interview")
                .setMessage("Are you sure you want to exit? Your progress will not be saved.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEndInterviewConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("End Interview Session")
                .setMessage("Would you like to finish the interview and view your score evaluation report?")
                .setPositiveButton("End Session", (dialog, which) -> generateEvaluationReport())
                .setNegativeButton("Keep Practicing", null)
                .show();
    }

    private void generateEvaluationReport() {
        isProcessing = true;
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        if (isRecording) {
            stopListening();
        }

        runOnUiThread(() -> {
            statusBadgeText.setText("Generating Report...");
            statusBadgeText.setTextColor(ContextCompat.getColor(this, R.color.soft_orange));
            transcriptionText.setText("Compiling overall performance score...");
        });

        try {
            String evalPrompt = "The interview is now complete. Please generate a comprehensive evaluation and performance summary of my performance during this interview.\n\n" +
                    "You must output the evaluation strictly in JSON format. Do not include any explanation, intro/outro paragraphs, or markdown code fences outside the JSON object.\n\n" +
                    "The JSON structure must match this format exactly:\n" +
                    "{\n" +
                    "  \"score\": 85,\n" +
                    "  \"verdict\": \"Strong Hire\",\n" +
                    "  \"summary\": \"Overall qualitative summary of the candidate's performance...\",\n" +
                    "  \"strengths\": [\n" +
                    "    \"Strength 1...\",\n" +
                    "    \"Strength 2...\"\n" +
                    "  ],\n" +
                    "  \"weaknesses\": [\n" +
                    "    \"Weakness 1...\",\n" +
                    "    \"Weakness 2...\"\n" +
                    "  ],\n" +
                    "  \"recommendations\": [\n" +
                    "    \"Actionable study recommendation 1...\",\n" +
                    "    \"Actionable study recommendation 2...\"\n" +
                    "  ]\n" +
                    "}";

            JSONObject evalObj = new JSONObject();
            evalObj.put("role", "user");
            evalObj.put("content", evalPrompt);
            conversationHistory.put(evalObj);

            GroqApiService.sendChatHistory(conversationHistory, new GroqApiService.GroqCallback() {
                @Override
                public void onSuccess(String responseText) {
                    isProcessing = false;
                    parseAndDisplayEvaluation(responseText);
                }

                @Override
                public void onFailure(int statusCode, String errorMessage) {
                    isProcessing = false;
                    runOnUiThread(() -> {
                        statusBadgeText.setText("Report Error");
                        statusBadgeText.setTextColor(ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.error_red));
                        Toast.makeText(InterviewSimulatorActivity.this, "Failed to compile report. Status " + statusCode, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error generating report", e);
            isProcessing = false;
        }
    }

    private void parseAndDisplayEvaluation(String responseText) {
        try {
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

            JSONObject root = new JSONObject(cleanJson);
            int score = root.optInt("score", 75);
            String verdict = root.optString("verdict", "Hire");
            String summary = root.optString("summary", "Done");

            scoreValText.setText(String.valueOf(score));
            verdictBadgeText.setText(verdict);

            if (verdict.toLowerCase().contains("strong hire") || verdict.toLowerCase().contains("hire")) {
                verdictBadgeText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            } else {
                verdictBadgeText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
            }

            summaryText.setText(summary);

            // Strengths list
            JSONArray strengthsArr = root.optJSONArray("strengths");
            StringBuilder strengthsBuilder = new StringBuilder();
            if (strengthsArr != null && strengthsArr.length() > 0) {
                for (int i = 0; i < strengthsArr.length(); i++) {
                    strengthsBuilder.append("• ").append(strengthsArr.getString(i));
                    if (i < strengthsArr.length() - 1) strengthsBuilder.append("\n\n");
                }
            } else {
                strengthsBuilder.append("No specific strengths noted.");
            }
            strengthsText.setText(strengthsBuilder.toString());

            // Weaknesses list
            JSONArray weaknessesArr = root.optJSONArray("weaknesses");
            StringBuilder weaknessesBuilder = new StringBuilder();
            if (weaknessesArr != null && weaknessesArr.length() > 0) {
                for (int i = 0; i < weaknessesArr.length(); i++) {
                    weaknessesBuilder.append("• ").append(weaknessesArr.getString(i));
                    if (i < weaknessesArr.length() - 1) weaknessesBuilder.append("\n\n");
                }
            } else {
                weaknessesBuilder.append("No specific weaknesses noted.");
            }
            weaknessesText.setText(weaknessesBuilder.toString());

            // Recommendations
            JSONArray recommendationsArr = root.optJSONArray("recommendations");
            StringBuilder recommendationsBuilder = new StringBuilder();
            if (recommendationsArr != null && recommendationsArr.length() > 0) {
                for (int i = 0; i < recommendationsArr.length(); i++) {
                    recommendationsBuilder.append("• ").append(recommendationsArr.getString(i));
                    if (i < recommendationsArr.length() - 1) recommendationsBuilder.append("\n\n");
                }
            } else {
                recommendationsBuilder.append("Continue voice practice session.");
            }
            recommendationsText.setText(recommendationsBuilder.toString());

            // Toggle screens
            chatLayout.setVisibility(View.GONE);
            evaluationLayout.setVisibility(View.VISIBLE);
            
            TextView title = findViewById(R.id.sim_header_title);
            title.setText("Performance Report");
            endBtn.setVisibility(View.GONE);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse report", e);
            // Fallback display
            scoreValText.setText("80");
            verdictBadgeText.setText("Session Completed");
            summaryText.setText(responseText);
            strengthsText.setText("• Session successfully completed.\n• Answered questions using Voice commands.");
            weaknessesText.setText("• JSON parsing exception occurred on assistant response.");
            recommendationsText.setText("• Review the spoken history for evaluation insights.");

            chatLayout.setVisibility(View.GONE);
            evaluationLayout.setVisibility(View.VISIBLE);
            TextView title = findViewById(R.id.sim_header_title);
            title.setText("Performance Report");
            endBtn.setVisibility(View.GONE);
        }
    }

    private class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // Dynamic scale wave animations matching speaker decibels
            runOnUiThread(() -> {
                float scale = 1.0f + Math.max(0, rmsdB) / 8.0f;
                micPulse1.setScaleX(scale);
                micPulse1.setScaleY(scale);
                micPulse1.setAlpha(Math.max(0.1f, 0.8f - (scale - 1.0f)));

                float scale2 = 1.0f + Math.max(0, rmsdB) / 5.0f;
                micPulse2.setScaleX(scale2);
                micPulse2.setScaleY(scale2);
                micPulse2.setAlpha(Math.max(0.05f, 0.5f - (scale2 - 1.0f)));
            });
        }

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
            runOnUiThread(() -> {
                micPulse1.setVisibility(View.INVISIBLE);
                micPulse2.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "Speech Recognizer Error code: " + error);
            
            // Handle silence or recognize timeouts
            runOnUiThread(() -> {
                isRecording = false;
                micPulse1.setVisibility(View.INVISIBLE);
                micPulse2.setVisibility(View.INVISIBLE);
                micBtn.setImageResource(R.drawable.ic_mic_off);

                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    statusBadgeText.setText("Speech Unclear");
                    transcriptionText.setText("Speech was not detected or was unclear. Let's try again.");
                    
                    // Polite voice prompt retry
                    speakText("I didn't catch that. Could you please repeat your answer?");
                } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    statusBadgeText.setText("Ready - Tap Mic to answer");
                } else {
                    statusBadgeText.setText("Error - Tap Mic to retry");
                    statusBadgeText.setTextColor(ContextCompat.getColor(InterviewSimulatorActivity.this, R.color.error_red));
                }
            });
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String finalAnswer = matches.get(0);
                currentTranscribedText = finalAnswer;
                stopListening();
                sendUserAnswer(finalAnswer);
            } else {
                stopListening();
                statusBadgeText.setText("Ready - Tap Mic to answer");
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String partial = matches.get(0);
                currentTranscribedText = partial;
                runOnUiThread(() -> transcriptionText.setText("Transcribing: \"" + partial + "...\""));
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        if (isRecording) {
            stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (evaluationLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            showExitConfirmationDialog();
        }
    }
}
