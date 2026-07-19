package com.wikisearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.wikisearch.app.adapter.EntryAdapter;
import com.wikisearch.app.db.DatabaseHelper;
import com.wikisearch.app.model.Entry;
import com.wikisearch.app.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnBack;
    private ImageView btnClear;
    private ChipGroup hotChipGroup;
    private ChipGroup historyChipGroup;
    private LinearLayout historySection;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private LinearLayout searchSuggestion;

    private DatabaseHelper dbHelper;
    private EntryAdapter adapter;
    private List<Entry> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_search);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupListeners();
        loadHotSearches();
        loadSearchHistory();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnClear = findViewById(R.id.btn_clear);
        hotChipGroup = findViewById(R.id.hot_chip_group);
        historyChipGroup = findViewById(R.id.history_chip_group);
        historySection = findViewById(R.id.history_section);
        recyclerView = findViewById(R.id.search_result);
        tvEmpty = findViewById(R.id.tv_empty);
        searchSuggestion = findViewById(R.id.search_suggestion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntryAdapter(this, searchResults);
        adapter.setOnEntryClickListener(new EntryAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(Entry entry) {
                openDetail(entry);
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch.requestFocus();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                btnClear.setVisibility(keyword.isEmpty() ? View.GONE : View.VISIBLE);

                if (keyword.isEmpty()) {
                    showSuggestions();
                } else {
                    performSearch(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = etSearch.getText().toString().trim();
                    if (!keyword.isEmpty()) {
                        saveSearchHistory(keyword);
                        performSearch(keyword);
                    }
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.btn_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearSearchHistory();
                loadSearchHistory();
            }
        });
    }

    private void loadHotSearches() {
        List<Entry> hotEntries = dbHelper.getHotEntries(10);
        hotChipGroup.removeAllViews();
        for (int i = 0; i < hotEntries.size() && i < 10; i++) {
            Entry entry = hotEntries.get(i);
            Chip chip = new Chip(this);
            chip.setText((i + 1) + ". " + entry.getTitle());
            chip.setTextSize(14);
            final String keyword = entry.getTitle();
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etSearch.setText(keyword);
                    etSearch.setSelection(keyword.length());
                    saveSearchHistory(keyword);
                    performSearch(keyword);
                }
            });
            hotChipGroup.addView(chip);
        }
    }

    private void loadSearchHistory() {
        List<String> historyList = dbHelper.getSearchHistory(20);
        historySection.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
        historyChipGroup.removeAllViews();
        for (final String keyword : historyList) {
            Chip chip = new Chip(this);
            chip.setText(keyword);
            chip.setTextSize(14);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etSearch.setText(keyword);
                    etSearch.setSelection(keyword.length());
                    saveSearchHistory(keyword);
                    performSearch(keyword);
                }
            });
            historyChipGroup.addView(chip);
        }
    }

    private void performSearch(String keyword) {
        searchResults = dbHelper.searchEntries(keyword);
        adapter.updateData(searchResults);

        if (searchResults.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            searchSuggestion.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            searchSuggestion.setVisibility(View.GONE);
        }
    }

    private void showSuggestions() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        searchSuggestion.setVisibility(View.VISIBLE);
    }

    private void saveSearchHistory(String keyword) {
        dbHelper.addSearchHistory(keyword);
        loadSearchHistory();
    }

    private void openDetail(Entry entry) {
        dbHelper.addSearchHistory(entry.getTitle());
        dbHelper.incrementViews(entry.getId());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("entry", entry);
        startActivity(intent);
    }
}
