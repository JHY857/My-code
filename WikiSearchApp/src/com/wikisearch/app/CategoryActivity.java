package com.wikisearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikisearch.app.adapter.EntryAdapter;
import com.wikisearch.app.db.DatabaseHelper;
import com.wikisearch.app.model.Entry;
import com.wikisearch.app.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView toolbarTitle;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;

    private DatabaseHelper dbHelper;
    private EntryAdapter adapter;
    private List<Entry> entryList = new ArrayList<>();
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_category);

        categoryName = getIntent().getStringExtra("category");
        if (categoryName == null) {
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupListeners();
        loadEntries();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        toolbarTitle = findViewById(R.id.toolbar_title);
        recyclerView = findViewById(R.id.category_list);
        emptyView = findViewById(R.id.empty_view);

        findViewById(R.id.toolbar).setBackgroundColor(ThemeUtils.getPrimaryColor(this));
        toolbarTitle.setText(categoryName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntryAdapter(this, entryList);
        adapter.setOnEntryClickListener(new EntryAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(Entry entry) {
                openDetail(entry);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadEntries() {
        entryList = dbHelper.getEntriesByCategory(categoryName);
        adapter.updateData(entryList);

        if (entryList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void openDetail(Entry entry) {
        dbHelper.incrementViews(entry.getId());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("entry", entry);
        startActivity(intent);
    }
}
