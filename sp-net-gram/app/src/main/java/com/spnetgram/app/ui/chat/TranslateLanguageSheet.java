package com.spnetgram.app.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.spnetgram.app.R;

public class TranslateLanguageSheet extends BottomSheetDialogFragment {
    public interface OnLanguageSelectedListener { void onLanguageSelected(String language); }
    private OnLanguageSelectedListener listener;

    public void setOnLanguageSelected(OnLanguageSelectedListener l) { this.listener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.bottom_sheet_translate, c, false);
        RecyclerView rv = v.findViewById(R.id.rv_languages);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Populate language list — adapter calls listener.onLanguageSelected(lang)
        return v;
    }
}
