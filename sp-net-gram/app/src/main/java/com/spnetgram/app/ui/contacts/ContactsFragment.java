package com.spnetgram.app.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.spnetgram.app.R;

public class ContactsFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_contacts, c, false);
        RecyclerView rv = v.findViewById(R.id.rv_contacts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        return v;
    }
}
