package com.spnetgram.app.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.spnetgram.app.R;
import com.spnetgram.app.ai.AIManager;

public class RewriteStyleSheet extends BottomSheetDialogFragment {
    public interface OnStyleSelectedListener { void onStyleSelected(AIManager.RewriteStyle style); }
    private OnStyleSelectedListener listener;

    public void setOnStyleSelected(OnStyleSelectedListener l) { this.listener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.bottom_sheet_rewrite, c, false);
        int[] btnIds = {R.id.btn_formal, R.id.btn_casual, R.id.btn_shorter,
            R.id.btn_longer, R.id.btn_polite, R.id.btn_assertive, R.id.btn_funny};
        AIManager.RewriteStyle[] styles = AIManager.RewriteStyle.values();
        for (int i = 0; i < btnIds.length && i < styles.length; i++) {
            final AIManager.RewriteStyle style = styles[i];
            Button btn = v.findViewById(btnIds[i]);
            if (btn != null) btn.setOnClickListener(vv -> { if (listener != null) listener.onStyleSelected(style); dismiss(); });
        }
        return v;
    }
}
