package com.techiguru.aicareerpilot;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GroqApiService {

    private static final String TAG = "GroqApiService";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_NAME = "llama-3.1-8b-instant";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface GroqCallback {
        void onSuccess(String responseText);
        void onFailure(int statusCode, String errorMessage);
    }

    /**
     * Send user prompt to Groq API asynchronously.
     * Invokes callback methods on the main UI thread.
     */
    public static void sendPrompt(final String prompt, final GroqCallback callback) {
        if (callback == null) {
            return;
        }

        final String apiKey = BuildConfig.GROQ_API_KEY != null ? BuildConfig.GROQ_API_KEY.trim() : "";
        if (apiKey.isEmpty()) {
            Log.e(TAG, "Groq API key is missing. Add GROQ_API_KEY to local.properties.");
            mainThreadHandler.post(() -> callback.onFailure(
                    401,
                    "Groq API key is not configured. Add GROQ_API_KEY to local.properties and rebuild the app."
            ));
            return;
        }

        Log.d(TAG, "API call started");

        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL_NAME);
                requestBody.put("max_tokens", 1024);
                requestBody.put("temperature", 0.7);

                JSONArray messagesArray = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are CareerPilot AI, a professional AI career assistant.");
                messagesArray.put(systemMessage);

                JSONObject messageObject = new JSONObject();
                messageObject.put("role", "user");
                messageObject.put("content", prompt);
                messagesArray.put(messageObject);

                requestBody.put("messages", messagesArray);

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    int statusCode = response.code();
                    ResponseBody responseBody = response.body();
                    String rawBody = responseBody != null ? responseBody.string() : "";

                    Log.d(TAG, "HTTP status: " + statusCode);
                    if (!rawBody.isEmpty()) {
                        Log.d(TAG, "Response body: " + rawBody);
                    }

                    if (response.isSuccessful()) {
                        String assistantResponse = extractAssistantContent(rawBody);
                        if (assistantResponse == null || assistantResponse.trim().isEmpty()) {
                            throw new IOException("Groq returned an empty response.");
                        }
                        mainThreadHandler.post(() -> callback.onSuccess(assistantResponse));
                        return;
                    }

                    String errorMessage = parseGroqErrorMessage(rawBody);
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = "Request failed with HTTP " + statusCode;
                    }
                    final String finalErrorMessage = errorMessage;
                    mainThreadHandler.post(() -> callback.onFailure(statusCode, finalErrorMessage));
                }
            } catch (IOException e) {
                Log.e(TAG, "Network request failed", e);
                mainThreadHandler.post(() -> callback.onFailure(
                        -2,
                        "Network error: " + (e.getMessage() != null ? e.getMessage() : "Unable to reach Groq.")
                ));
            } catch (Exception e) {
                Log.e(TAG, "API request failed", e);
                mainThreadHandler.post(() -> callback.onFailure(
                        -1,
                        e.getMessage() != null ? e.getMessage() : "Unexpected API error."
                ));
            }
        });
    }

    /**
     * Send structured conversation history to Groq API asynchronously.
     * Invokes callback methods on the main UI thread.
     */
    public static void sendChatHistory(final JSONArray messages, final GroqCallback callback) {
        if (callback == null) {
            return;
        }

        final String apiKey = BuildConfig.GROQ_API_KEY != null ? BuildConfig.GROQ_API_KEY.trim() : "";
        if (apiKey.isEmpty()) {
            Log.e(TAG, "Groq API key is missing. Add GROQ_API_KEY to local.properties.");
            mainThreadHandler.post(() -> callback.onFailure(
                    401,
                    "Groq API key is not configured. Add GROQ_API_KEY to local.properties and rebuild the app."
            ));
            return;
        }

        Log.d(TAG, "API chat history call started");

        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL_NAME);
                requestBody.put("max_tokens", 1024);
                requestBody.put("temperature", 0.7);
                requestBody.put("messages", messages);

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    int statusCode = response.code();
                    ResponseBody responseBody = response.body();
                    String rawBody = responseBody != null ? responseBody.string() : "";

                    Log.d(TAG, "HTTP status: " + statusCode);
                    if (response.isSuccessful()) {
                        String assistantResponse = extractAssistantContent(rawBody);
                        if (assistantResponse == null || assistantResponse.trim().isEmpty()) {
                            throw new IOException("Groq returned an empty response.");
                        }
                        mainThreadHandler.post(() -> callback.onSuccess(assistantResponse));
                        return;
                    }

                    String errorMessage = parseGroqErrorMessage(rawBody);
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = "Request failed with HTTP " + statusCode;
                    }
                    final String finalErrorMessage = errorMessage;
                    mainThreadHandler.post(() -> callback.onFailure(statusCode, finalErrorMessage));
                }
            } catch (IOException e) {
                Log.e(TAG, "Network request failed", e);
                mainThreadHandler.post(() -> callback.onFailure(
                        -2,
                        "Network error: " + (e.getMessage() != null ? e.getMessage() : "Unable to reach Groq.")
                ));
            } catch (Exception e) {
                Log.e(TAG, "API request failed", e);
                mainThreadHandler.post(() -> callback.onFailure(
                        -1,
                        e.getMessage() != null ? e.getMessage() : "Unexpected API error."
                ));
            }
        });
    }


    private static String extractAssistantContent(String rawBody) throws Exception {
        JSONObject responseJson = new JSONObject(rawBody);
        JSONArray choicesArray = responseJson.optJSONArray("choices");
        if (choicesArray == null || choicesArray.length() == 0) {
            throw new Exception("Malformed Groq response: choices array missing.");
        }

        JSONObject firstChoice = choicesArray.getJSONObject(0);
        JSONObject message = firstChoice.optJSONObject("message");
        if (message == null) {
            throw new Exception("Malformed Groq response: message object missing.");
        }

        String content = message.optString("content", "").trim();
        if (!content.isEmpty()) {
            return content;
        }

        // Some models may return text in an alternate field.
        String text = message.optString("text", "").trim();
        if (!text.isEmpty()) {
            return text;
        }

        return null;
    }

    private static String parseGroqErrorMessage(String rawBody) {
        if (rawBody == null || rawBody.trim().isEmpty()) {
            return null;
        }

        try {
            JSONObject errorJson = new JSONObject(rawBody);
            JSONObject errorObject = errorJson.optJSONObject("error");
            if (errorObject != null) {
                String message = errorObject.optString("message", "").trim();
                if (!message.isEmpty()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
            // Fall back to raw body below.
        }

        return rawBody.trim();
    }
}
