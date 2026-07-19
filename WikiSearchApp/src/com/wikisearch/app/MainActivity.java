package com.wikisearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.wikisearch.app.adapter.CategoryGridAdapter;
import com.wikisearch.app.adapter.EntryAdapter;
import com.wikisearch.app.db.DatabaseHelper;
import com.wikisearch.app.db.DataInitializer;
import com.wikisearch.app.model.Category;
import com.wikisearch.app.model.Entry;
import com.wikisearch.app.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private View btnMenu;
    private View searchBar;

    private ChipGroup hotChipGroup;
    private ChipGroup historyChipGroup;
    private GridView categoryGrid;
    private RecyclerView entryList;
    private LinearLayout historySection;
    private TextView btnClearHistory;

    private DatabaseHelper dbHelper;
    private EntryAdapter entryAdapter;
    private CategoryGridAdapter categoryAdapter;
    private List<Entry> allEntries = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private int[] categoryColors = {
            R.color.category_tech,
            R.color.category_history,
            R.color.category_science,
            R.color.category_culture,
            R.color.category_geography,
            R.color.category_biology,
            R.color.category_medicine,
            R.color.category_art
    };

    private String[] categoryNames = {
            "科技", "历史", "科学", "文化",
            "地理", "生物", "医学", "艺术"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);
        DataInitializer.initSampleData(this);

        initViews();
        setupListeners();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        btnMenu = findViewById(R.id.btn_menu);
        searchBar = findViewById(R.id.search_bar);

        hotChipGroup = findViewById(R.id.hot_chip_group);
        historyChipGroup = findViewById(R.id.history_chip_group);
        categoryGrid = findViewById(R.id.category_grid);
        entryList = findViewById(R.id.entry_list);
        historySection = findViewById(R.id.history_section);
        btnClearHistory = findViewById(R.id.btn_clear_history);

        entryList.setLayoutManager(new LinearLayoutManager(this));
        entryAdapter = new EntryAdapter(this, allEntries);
        entryAdapter.setOnEntryClickListener(new EntryAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(Entry entry) {
                openDetail(entry);
            }
        });
        entryList.setAdapter(entryAdapter);

        initCategories();
        categoryAdapter = new CategoryGridAdapter(this, categoryList);
        categoryAdapter.setOnCategoryClickListener(new CategoryGridAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                openCategory(category.getName());
            }
        });
        categoryGrid.setAdapter(categoryAdapter);
    }

    private void initCategories() {
        categoryList.clear();
        for (int i = 0; i < categoryNames.length; i++) {
            int count = dbHelper.getCategoryCount(categoryNames[i]);
            categoryList.add(new Category(categoryNames[i], 0, categoryColors[i], count));
        }
    }

    private void setupListeners() {
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.bottom_home) {
                    return true;
                } else if (id == R.id.bottom_favorite) {
                    openFavorite();
                    return false;
                } else if (id == R.id.bottom_settings) {
                    openSettings();
                    return false;
                }
                return false;
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                drawerLayout.closeDrawers();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_favorite) {
                    openFavorite();
                    return true;
                } else if (id == R.id.nav_settings) {
                    openSettings();
                    return true;
                }
                return false;
            }
        });

        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearSearchHistory();
                loadSearchHistory();
            }
        });
    }

    private void loadData() {
        loadHotSearches();
        loadSearchHistory();
        loadAllEntries();
        initCategories();
        categoryAdapter.notifyDataSetChanged();
    }

    private void loadHotSearches() {
        List<Entry> hotEntries = dbHelper.getHotEntries(10);
        hotChipGroup.removeAllViews();
        for (int i = 0; i < hotEntries.size() && i < 8; i++) {
            Entry entry = hotEntries.get(i);
            Chip chip = new Chip(this);
            chip.setText(entry.getTitle());
            chip.setTextSize(13);
            final String keyword = entry.getTitle();
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.addSearchHistory(keyword);
                    Entry entry = dbHelper.searchEntries(keyword).get(0);
                    openDetail(entry);
                }
            });
            hotChipGroup.addView(chip);
        }
    }

    private void loadSearchHistory() {
        List<String> historyList = dbHelper.getSearchHistory(10);
        historySection.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
        historyChipGroup.removeAllViews();
        for (final String keyword : historyList) {
            Chip chip = new Chip(this);
            chip.setText(keyword);
            chip.setTextSize(13);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.addSearchHistory(keyword);
                    List<Entry> results = dbHelper.searchEntries(keyword);
                    if (!results.isEmpty()) {
                        openDetail(results.get(0));
                    }
                }
            });
            historyChipGroup.addView(chip);
        }
    }

    private void loadAllEntries() {
        allEntries = dbHelper.getAllEntries();
        entryAdapter.updateData(allEntries);
    }

    private void openSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void openDetail(Entry entry) {
        dbHelper.incrementViews(entry.getId());
        dbHelper.addSearchHistory(entry.getTitle());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("entry", entry);
        startActivity(intent);
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void openFavorite() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
