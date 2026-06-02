package com.spnetgram.app.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.spnetgram.app.BuildConfig;
import com.spnetgram.app.security.SecureStorageManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Central AI Manager for all SP NET GRAM AI features.
 * Coordinates summarization, translation, rewriting, voice note analysis, and smart replies.
 */
public class AIManager {

    private static final String TAG = "AIManager";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private static volatile AIManager instance;

    private final Context context;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final SecureStorageManager secureStorage;

    private AIManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static AIManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AIManager.class) {
                if (instance == null) {
                    instance = new AIManager(context);
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    public void summarizeMessages(String messages, String language, AICallback callback) {
        executor.execute(() -> {
            String prompt = String.format(
                "You are a concise summarizer. Summarize the following Telegram chat messages in %s. " +
                "Be brief (3-5 sentences max). Focus on key topics and decisions.\n\n%s",
                language, messages
            );
            callOpenAI(prompt, callback);
        });
    }

    public void translateMessage(String message, String targetLanguage, AICallback callback) {
        executor.execute(() -> {
            String prompt = String.format(
                "Translate the following message to %s. Return only the translated text, no explanations.\n\n%s",
                targetLanguage, message
            );
            callOpenAI(prompt, callback);
        });
    }

    public void rewriteMessage(String message, RewriteStyle style, AICallback callback) {
        executor.execute(() -> {
            String styleDesc = getRewriteStyleDescription(style);
            String prompt = String.format(
                "Rewrite the following message to be %s. Keep the same meaning. Return only the rewritten message.\n\n%s",
                styleDesc, message
            );
            callOpenAI(prompt, callback);
        });
    }

    public void summarizeVoiceNote(String transcription, AICallback callback) {
        executor.execute(() -> {
            String prompt = String.format(
                "You received a voice note transcription. Summarize it in 1-3 short bullet points. " +
                "Keep it very brief and to the point.\n\nTranscription: %s",
                transcription
            );
            callOpenAI(prompt, callback);
        });
    }

    public void generateSmartReplies(String conversationContext, int replyCount, AICallback callback) {
        executor.execute(() -> {
            String prompt = String.format(
                "Based on this conversation, suggest %d short, natural reply options. " +
                "Format as a JSON array of strings. Keep each reply under 10 words.\n\nConversation:\n%s",
                replyCount, conversationContext
            );
            callOpenAI(prompt, result -> {
                // Parse JSON array from result
                try {
                    JSONArray arr = new JSONArray(result);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arr.length(); i++) {
                        if (i > 0) sb.append("||");
                        sb.append(arr.getString(i));
                    }
                    mainHandler.post(() -> callback.onSuccess(sb.toString()));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onSuccess(result));
                }
            });
        });
    }

    public void detectMessageLanguage(String message, AICallback callback) {
        executor.execute(() -> {
            String prompt = String.format(
                "Detect the language of this text and return ONLY the ISO 639-1 language code (e.g. 'en', 'es', 'fr').\n\n%s",
                message
            );
            callOpenAI(prompt, callback);
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void callOpenAI(String userPrompt, AICallback callback) {
        String apiKey = secureStorage.getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
        if (apiKey == null || apiKey.isEmpty()) {
            mainHandler.post(() -> callback.onError("AI API key not configured. Set it in Settings → AI Features."));
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", DEFAULT_MODEL);
            requestBody.put("max_tokens", 512);
            requestBody.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", userPrompt);
            messages.put(message);
            requestBody.put("messages", messages);

            Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "OpenAI error: " + response.code() + " " + errBody);
                    mainHandler.post(() -> callback.onError("AI request failed: " + response.code()));
                    return;
                }

                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                String content = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

                mainHandler.post(() -> callback.onSuccess(content));
            }

        } catch (Exception e) {
            Log.e(TAG, "AI call failed", e);
            mainHandler.post(() -> callback.onError("AI error: " + e.getMessage()));
        }
    }

    private String getRewriteStyleDescription(RewriteStyle style) {
        switch (style) {
            case FORMAL:       return "more formal and professional";
            case CASUAL:       return "more casual and friendly";
            case SHORTER:      return "shorter and more concise";
            case LONGER:       return "more detailed and elaborate";
            case POLITE:       return "more polite and respectful";
            case ASSERTIVE:    return "more assertive and direct";
            case FUNNY:        return "funnier and more humorous";
            default:           return "clearer";
        }
    }

    public enum RewriteStyle {
        FORMAL, CASUAL, SHORTER, LONGER, POLITE, ASSERTIVE, FUNNY
    }

    public interface AICallback {
        void onSuccess(String result);
        void onError(String error);
    }
}
