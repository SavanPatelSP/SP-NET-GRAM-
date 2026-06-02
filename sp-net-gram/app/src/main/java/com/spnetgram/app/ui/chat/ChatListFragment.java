package com.spnetgram.app.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.ui.premium.SPDiamondActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Main chat list screen. Shows conversations, folders, search, and account switcher.
 */
public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private TabLayout folderTabs;
    private FloatingActionButton fabNewChat;
    private ImageView ivAccountAvatar;
    private TextView tvAccountName;
    private View diamondBadgeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        bindViews(view);
        setupSearch();
        setupFolderTabs();
        setupFab();
        setupAccountSwitcher();
        setupDiamondBadge();
        return view;
    }

    private void bindViews(View view) {
        recyclerView   = view.findViewById(R.id.rv_chats);
        searchBar      = view.findViewById(R.id.et_search);
        folderTabs     = view.findViewById(R.id.tab_folders);
        fabNewChat     = view.findViewById(R.id.fab_new_chat);
        ivAccountAvatar = view.findViewById(R.id.iv_account_avatar);
        tvAccountName  = view.findViewById(R.id.tv_account_name);
        diamondBadgeView = view.findViewById(R.id.view_diamond_badge);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterChats(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFolderTabs() {
        folderTabs.addTab(folderTabs.newTab().setText("All"));
        folderTabs.addTab(folderTabs.newTab().setText("Personal"));
        folderTabs.addTab(folderTabs.newTab().setText("Work"));
        folderTabs.addTab(folderTabs.newTab().setText("Channels"));
        folderTabs.addTab(folderTabs.newTab().setText("Bots"));

        folderTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                filterByFolder(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFab() {
        fabNewChat.setOnClickListener(v -> {
            // Open new chat / contact selector
            startActivity(new Intent(requireContext(), NewChatActivity.class));
        });
    }

    private void setupAccountSwitcher() {
        com.spnetgram.app.utils.AccountManager.Account active =
            SPNetGramApp.getInstance().getAccountManager().getActiveAccount();
        if (active != null) {
            tvAccountName.setText(active.displayName);
        }

        ivAccountAvatar.setOnClickListener(v -> {
            AccountSwitcherBottomSheet sheet = new AccountSwitcherBottomSheet();
            sheet.show(getChildFragmentManager(), "account_switcher");
        });
    }

    private void setupDiamondBadge() {
        boolean hasDiamond = SPNetGramApp.getInstance().getDiamondManager().isDiamondActive();
        diamondBadgeView.setVisibility(hasDiamond ? View.VISIBLE : View.GONE);
        diamondBadgeView.setOnClickListener(v ->
            startActivity(new Intent(requireContext(), SPDiamondActivity.class)));
    }

    private void filterChats(String query) {
        // Delegate to adapter
    }

    private void filterByFolder(int folderIndex) {
        // Delegate to adapter
    }
}
