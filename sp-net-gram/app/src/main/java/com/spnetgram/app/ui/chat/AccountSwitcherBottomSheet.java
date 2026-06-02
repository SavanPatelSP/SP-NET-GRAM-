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

public class AccountSwitcherBottomSheet extends BottomSheetDialogFragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.bottom_sheet_account_switcher, c, false);
        RecyclerView rv = v.findViewById(R.id.rv_accounts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        return v;
    }
}
