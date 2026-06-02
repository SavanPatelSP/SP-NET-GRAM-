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

public class ScheduleMessageBottomSheet extends BottomSheetDialogFragment {
    public interface OnScheduleSetListener { void onScheduleSet(long timestamp); }
    private OnScheduleSetListener listener;

    public void setOnScheduleSet(OnScheduleSetListener l) { this.listener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.bottom_sheet_schedule, c, false);
        Button btn = v.findViewById(R.id.btn_confirm_schedule);
        btn.setOnClickListener(vv -> {
            if (listener != null) listener.onScheduleSet(System.currentTimeMillis() + 60000);
            dismiss();
        });
        return v;
    }
}
