package com.spnetgram.app.ai;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.spnetgram.app.security.SecureStorageManager;

/**
 * Handles voice note transcription via OpenAI Whisper API,
 * then summarizes the transcription using AIManager.
 */
public class AIVoiceNoteHelper {

    private static final String TAG = "AIVoiceNoteHelper";
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final Context context;
    private final AIManager aiManager;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final SecureStorageManager secureStorage;

    public AIVoiceNoteHelper(Context context) {
        this.context = context.getApplicationContext();
        this.aiManager = AIManager.getInstance(context);
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.secureStorage = SecureStorageManager.getInstance(context);
    }

    /**
     * Transcribes a voice note file, then summarizes the transcript.
     *
     * @param audioFile  The OGG/MP3/WAV file from the Telegram message
     * @param callback   Returns a short summary of the voice note
     */
    public void transcribeAndSummarize(File audioFile, VoiceNoteCallback callback) {
        executor.execute(() -> {
            String apiKey = secureStorage.getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
            if (apiKey == null || apiKey.isEmpty()) {
                mainHandler.post(() -> callback.onError("OpenAI API key not set. Configure in Settings → AI Features."));
                return;
            }

            // Step 1: Transcribe with Whisper
            String transcript = transcribeWithWhisper(audioFile, apiKey);
            if (transcript == null) {
                mainHandler.post(() -> callback.onError("Transcription failed."));
                return;
            }

            mainHandler.post(() -> callback.onTranscriptionReady(transcript));

            // Step 2: Summarize transcript
            aiManager.summarizeVoiceNote(transcript, new AIManager.AICallback() {
                @Override
                public void onSuccess(String result) {
                    callback.onSummaryReady(result);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    /**
     * Transcribe only — returns raw transcript without summarizing.
     */
    public void transcribeOnly(File audioFile, AIManager.AICallback callback) {
        executor.execute(() -> {
            String apiKey = secureStorage.getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
            if (apiKey == null || apiKey.isEmpty()) {
                mainHandler.post(() -> callback.onError("OpenAI API key not set."));
                return;
            }
            String transcript = transcribeWithWhisper(audioFile, apiKey);
            if (transcript != null) {
                mainHandler.post(() -> callback.onSuccess(transcript));
            } else {
                mainHandler.post(() -> callback.onError("Transcription failed."));
            }
        });
    }

    private String transcribeWithWhisper(File audioFile, String apiKey) {
        try {
            String mimeType = getMimeType(audioFile);
            RequestBody fileBody = RequestBody.create(audioFile, MediaType.get(mimeType));

            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("response_format", "text")
                .build();

            Request request = new Request.Builder()
                .url(WHISPER_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Whisper error: " + response.code());
                    return null;
                }
                return response.body() != null ? response.body().string().trim() : null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Whisper request failed", e);
            return null;
        }
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".ogg") || name.endsWith(".oga")) return "audio/ogg";
        if (name.endsWith(".mp3")) return "audio/mpeg";
        if (name.endsWith(".wav")) return "audio/wav";
        if (name.endsWith(".m4a")) return "audio/mp4";
        if (name.endsWith(".opus")) return "audio/opus";
        return "audio/ogg";
    }

    public interface VoiceNoteCallback {
        void onTranscriptionReady(String transcript);
        void onSummaryReady(String summary);
        void onError(String error);
    }
}
