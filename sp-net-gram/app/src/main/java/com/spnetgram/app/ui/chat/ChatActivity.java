package com.spnetgram.app.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.ai.AIManager;
import com.spnetgram.app.ai.AIVoiceNoteHelper;
import com.spnetgram.app.analytics.AnalyticsManager;
import com.spnetgram.app.premium.SPCoinsManager;
import com.spnetgram.app.premium.SPDiamondManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual chat screen with full messaging, AI features, quick replies,
 * scheduled messages, and silent messages.
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ID    = "chat_id";
    public static final String EXTRA_CHAT_TITLE = "chat_title";
    public static final String EXTRA_IS_GROUP   = "is_group";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnAttach;
    private ImageButton btnVoice;
    private ImageButton btnAI;
    private ImageButton btnSchedule;
    private Toolbar toolbar;
    private TextView tvAISummary;
    private View aiSummaryCard;

    private AIManager aiManager;
    private AIVoiceNoteHelper voiceNoteHelper;
    private SPCoinsManager coinsManager;
    private String chatId;
    private List<String> smartReplies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        String chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);

        bindViews();
        setupToolbar(chatTitle);
        setupMessageInput();
        setupAIButton();
        setupScheduleButton();

        aiManager = AIManager.getInstance(this);
        voiceNoteHelper = new AIVoiceNoteHelper(this);
        coinsManager = SPNetGramApp.getInstance().getCoinsManager();
    }

    private void bindViews() {
        rvMessages    = findViewById(R.id.rv_messages);
        etMessage     = findViewById(R.id.et_message);
        btnSend       = findViewById(R.id.btn_send);
        btnAttach     = findViewById(R.id.btn_attach);
        btnVoice      = findViewById(R.id.btn_voice);
        btnAI         = findViewById(R.id.btn_ai);
        btnSchedule   = findViewById(R.id.btn_schedule);
        toolbar       = findViewById(R.id.toolbar);
        tvAISummary   = findViewById(R.id.tv_ai_summary);
        aiSummaryCard = findViewById(R.id.card_ai_summary);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar(String chatTitle) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(chatTitle);
        }
    }

    private void setupMessageInput() {
        btnSend.setOnClickListener(v -> sendMessage(false));
        btnSend.setOnLongClickListener(v -> {
            showSendOptions();
            return true;
        });
    }

    private void sendMessage(boolean silent) {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        // Dispatch to Telegram MTProto layer
        // TelegramMessenger.sendMessage(chatId, text, silent);
        etMessage.setText("");
        coinsManager.earnCoins(1, SPCoinsManager.TransactionType.EARN_ACTIVITY,
            "Message sent", new SPCoinsManager.CoinsCallback() {
                @Override public void onSuccess(long newBalance) {}
                @Override public void onError(String error) {}
            });
    }

    private void showSendOptions() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_send_options, null);
        sheetView.findViewById(R.id.btn_send_silent).setOnClickListener(v -> {
            sendMessage(true);
            sheet.dismiss();
        });
        sheetView.findViewById(R.id.btn_schedule_message).setOnClickListener(v -> {
            showSchedulePicker();
            sheet.dismiss();
        });
        sheet.setContentView(sheetView);
        sheet.show();
    }

    private void showSchedulePicker() {
        ScheduleMessageBottomSheet sched = new ScheduleMessageBottomSheet();
        sched.setOnScheduleSet((timestamp) -> {
            // TelegramMessenger.sendScheduled(chatId, etMessage.getText().toString(), timestamp);
            etMessage.setText("");
            Toast.makeText(this, "Message scheduled", Toast.LENGTH_SHORT).show();
        });
        sched.show(getSupportFragmentManager(), "schedule");
    }

    private void setupAIButton() {
        btnAI.setOnClickListener(v -> showAIOptions());
    }

    private void showAIOptions() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sv = getLayoutInflater().inflate(R.layout.bottom_sheet_ai_options, null);

        sv.findViewById(R.id.btn_ai_summarize).setOnClickListener(v -> {
            summarizeChat();
            sheet.dismiss();
        });
        sv.findViewById(R.id.btn_ai_translate).setOnClickListener(v -> {
            showTranslateOptions();
            sheet.dismiss();
        });
        sv.findViewById(R.id.btn_ai_smart_replies).setOnClickListener(v -> {
            fetchSmartReplies();
            sheet.dismiss();
        });
        sv.findViewById(R.id.btn_ai_rewrite).setOnClickListener(v -> {
            showRewriteOptions();
            sheet.dismiss();
        });

        sheet.setContentView(sv);
        sheet.show();
    }

    private void summarizeChat() {
        if (!coinsManager.canAfford(SPCoinsManager.COST_AI_SUMMARY)
                && !SPNetGramApp.getInstance().getDiamondManager().isDiamondActive()) {
            Toast.makeText(this, "Need " + SPCoinsManager.COST_AI_SUMMARY + " SP Coins to summarize.", Toast.LENGTH_LONG).show();
            return;
        }

        aiSummaryCard.setVisibility(View.VISIBLE);
        tvAISummary.setText("Summarizing…");

        // Collect last 50 visible messages as text
        String messagesText = ""; // build from adapter
        aiManager.summarizeMessages(messagesText, "English", new AIManager.AICallback() {
            @Override public void onSuccess(String result) {
                tvAISummary.setText(result);
                SPNetGramApp.getInstance().getAnalyticsManager()
                    .logAIFeatureUsed("chat_summarize");
                if (!SPNetGramApp.getInstance().getDiamondManager().isDiamondActive()) {
                    coinsManager.spendCoins(SPCoinsManager.COST_AI_SUMMARY,
                        SPCoinsManager.TransactionType.SPEND_AI, "Chat summary", null);
                }
            }
            @Override public void onError(String error) {
                tvAISummary.setText("Could not summarize: " + error);
            }
        });
    }

    private void showTranslateOptions() {
        TranslateLanguageSheet sheet = new TranslateLanguageSheet();
        sheet.setOnLanguageSelected(lang -> {
            String msgText = etMessage.getText().toString();
            if (!msgText.isEmpty()) {
                aiManager.translateMessage(msgText, lang, new AIManager.AICallback() {
                    @Override public void onSuccess(String result) { etMessage.setText(result); }
                    @Override public void onError(String e) {
                        Toast.makeText(ChatActivity.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        sheet.show(getSupportFragmentManager(), "translate");
    }

    private void fetchSmartReplies() {
        // Build context from last few messages
        String context = "";
        aiManager.generateSmartReplies(context, 3, new AIManager.AICallback() {
            @Override public void onSuccess(String result) {
                String[] replies = result.split("\\|\\|");
                showSmartRepliesBar(replies);
            }
            @Override public void onError(String error) {}
        });
    }

    private void showSmartRepliesBar(String[] replies) {
        // Show horizontal chip row above message input
    }

    private void showRewriteOptions() {
        RewriteStyleSheet sheet = new RewriteStyleSheet();
        sheet.setOnStyleSelected(style -> {
            String msgText = etMessage.getText().toString();
            if (!msgText.isEmpty()) {
                aiManager.rewriteMessage(msgText, style, new AIManager.AICallback() {
                    @Override public void onSuccess(String result) { etMessage.setText(result); }
                    @Override public void onError(String e) {
                        Toast.makeText(ChatActivity.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        sheet.show(getSupportFragmentManager(), "rewrite");
    }

    private void setupScheduleButton() {
        btnSchedule.setOnClickListener(v -> showSchedulePicker());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { onBackPressed(); return true; }
        if (id == R.id.action_search) { /* open search */ return true; }
        if (id == R.id.action_mute) { /* toggle mute */ return true; }
        if (id == R.id.action_clear) { /* clear history */ return true; }
        return super.onOptionsItemSelected(item);
    }
}
