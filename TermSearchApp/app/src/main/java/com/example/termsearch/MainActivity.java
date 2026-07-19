package com.example.termsearch;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.List;

/**
 * 主界面：搜索、列表、抽屉菜单、导入导出、夜间模式、排序
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private MaterialToolbar toolbar;
    private EditText etSearch;
    private View btnClearSearch;
    private RecyclerView rv;
    private SwipeRefreshLayout refresh;
    private View emptyView;
    private TextView tvEmpty, tvDrawerCount;
    private View historyScroll, historyContainer;
    private FloatingActionButton fabAdd;

    private DatabaseHelper db;
    private PrefManager pref;
    private TermAdapter adapter;

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::onDetailResult);
    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::onAddEditResult);
    private final ActivityResultLauncher<Intent> categoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> refreshAll());
    private final ActivityResultLauncher<String> importLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    this::onImportFilePicked);
    private final ActivityResultLauncher<Intent> shareFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 必须在 setTheme / setContentView 之前应用夜间模式
        pref = new PrefManager(this);
        AppCompatDelegate.setDefaultNightMode(
                pref.isNightMode()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.get(this);

        bindViews();
        setupToolbar();
        setupSearch();
        setupRecyclerView();
        setupFab();
        setupDrawer();
        refreshAll();
    }

    private void bindViews() {
        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        rv = findViewById(R.id.recycler_view);
        refresh = findViewById(R.id.refresh_layout);
        emptyView = findViewById(R.id.empty_view);
        tvEmpty = findViewById(R.id.tv_empty);
        tvDrawerCount = findViewById(R.id.tv_drawer_count);
        historyScroll = findViewById(R.id.history_scroll);
        historyContainer = findViewById(R.id.history_container);
        fabAdd = findViewById(R.id.fab_add);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                refreshAll();
            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String kw = v.getText().toString().trim();
                if (!kw.isEmpty()) db.addHistory(kw);
                return true;
            }
            return false;
        });
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            refreshAll();
        });
    }

    private void setupRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TermAdapter(
                term -> {
                    db.incrementViews(term.getId());
                    // 点击搜索结果时记录关键词到历史
                    String kw = etSearch.getText().toString().trim();
                    if (!kw.isEmpty()) db.addHistory(kw);
                    Intent it = new Intent(this, DetailActivity.class);
                    it.putExtra(DetailActivity.EXTRA_ID, term.getId());
                    detailLauncher.launch(it);
                },
                (term, newFav) -> {
                    db.toggleFavorite(term.getId(), newFav);
                    term.setFavorite(newFav);
                    refreshAll();
                });
        rv.setAdapter(adapter);

        refresh.setOnRefreshListener(this::refreshAll);
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            Intent it = new Intent(this, AddEditActivity.class);
            addEditLauncher.launch(it);
        });
    }

    private void setupDrawer() {
        findViewById(R.id.nav_all).setOnClickListener(v -> {
            pref.setDrawerMode(PrefManager.MODE_ALL);
            drawer.closeDrawer(GravityCompat.START);
            refreshAll();
        });
        findViewById(R.id.nav_favorites).setOnClickListener(v -> {
            pref.setDrawerMode(PrefManager.MODE_FAV);
            drawer.closeDrawer(GravityCompat.START);
            refreshAll();
        });
        findViewById(R.id.nav_history).setOnClickListener(v -> {
            showHistory();
            drawer.closeDrawer(GravityCompat.START);
        });
        findViewById(R.id.nav_category_manage).setOnClickListener(v -> {
            Intent it = new Intent(this, CategoryActivity.class);
            categoryLauncher.launch(it);
            drawer.closeDrawer(GravityCompat.START);
        });
        findViewById(R.id.nav_about).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.about_text)
                    .setPositiveButton(R.string.dialog_confirm, null)
                    .show();
            drawer.closeDrawer(GravityCompat.START);
        });
    }

    /** 重建抽屉里的分类列表 */
    private void rebuildDrawerCategories() {
        ViewGroup container = findViewById(R.id.nav_category_container);
        container.removeAllViews();
        List<String> cats = db.getCategories();
        String activeCat = pref.getDrawerCategory();
        String activeMode = pref.getDrawerMode();

        // 「未分类」
        container.addView(buildCategoryItem(Term.CATEGORY_NONE, activeMode, activeCat));

        for (String c : cats) {
            container.addView(buildCategoryItem(c, activeMode, activeCat));
        }
    }

    private View buildCategoryItem(String name, String activeMode, String activeCat) {
        TextView tv = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.item_drawer_category, findViewById(R.id.nav_category_container), false);
        tv.setText(name);
        boolean selected = activeMode.equals(PrefManager.MODE_CATEGORY) && name.equals(activeCat);
        tv.setBackgroundColor(selected
                ? getResources().getColor(R.color.primary_light)
                : 0);
        tv.setOnClickListener(view -> {
            pref.setDrawerMode(PrefManager.MODE_CATEGORY);
            pref.setDrawerCategory(name);
            drawer.closeDrawer(GravityCompat.START);
            refreshAll();
        });
        return tv;
    }

    private void refreshAll() {
        refresh.setRefreshing(false);

        String kw = etSearch.getText().toString().trim();
        String mode = pref.getDrawerMode();
        String sort = pref.getSortMode();

        // toolbar 标题
        String title;
        switch (mode) {
            case PrefManager.MODE_FAV:
                title = getString(R.string.drawer_favorites);
                break;
            case PrefManager.MODE_CATEGORY:
                title = pref.getDrawerCategory();
                if (title == null || title.isEmpty()) title = getString(R.string.drawer_all);
                break;
            default:
                title = getString(R.string.app_name);
                break;
        }
        toolbar.setTitle(title);

        // 抽屉分类列表
        rebuildDrawerCategories();
        tvDrawerCount.setText("共 " + db.countTerms() + " 条词条");

        // 数据查询
        List<Term> list;
        if (!kw.isEmpty()) {
            list = db.search(kw, sort);
            historyScroll.setVisibility(View.GONE);
        } else {
            switch (mode) {
                case PrefManager.MODE_FAV:
                    list = db.getFavorites(sort);
                    break;
                case PrefManager.MODE_CATEGORY:
                    list = db.getByCategory(pref.getDrawerCategory(), sort);
                    break;
                default:
                    list = db.getAll(sort);
                    break;
            }
            // 显示搜索历史
            showHistoryChips();
        }

        adapter.setKeyword(kw);
        adapter.setData(list);

        if (list.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tvEmpty.setText(kw.isEmpty()
                    ? (mode.equals(PrefManager.MODE_FAV)
                        ? getString(R.string.empty_favorite)
                        : getString(R.string.empty_list))
                    : getString(R.string.empty_search));
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    /** 顶部显示搜索历史 chip */
    private void showHistoryChips() {
        LinearLayout container = (LinearLayout) historyContainer;
        container.removeAllViews();
        List<String> hist = db.getHistory();
        if (hist.isEmpty()) {
            historyScroll.setVisibility(View.GONE);
            return;
        }
        historyScroll.setVisibility(View.VISIBLE);
        for (String k : hist) {
            TextView tv = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_history_chip, container, false);
            tv.setText(k);
            tv.setOnClickListener(v -> etSearch.setText(k));
            container.addView(tv);
        }
    }

    /** 抽屉 - 历史记录列表对话框 */
    private void showHistory() {
        List<String> hist = db.getHistory();
        if (hist.isEmpty()) {
            Toast.makeText(this, R.string.empty_history, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] arr = hist.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(R.string.drawer_history)
                .setItems(arr, (d, which) -> etSearch.setText(arr[which]))
                .setNeutralButton(R.string.action_clear_history, (d, w) -> {
                    db.clearHistory();
                    refreshAll();
                    Toast.makeText(this, R.string.toast_history_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ========== 菜单 ==========

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem nightItem = menu.findItem(R.id.action_night_mode);
        if (nightItem != null) {
            nightItem.setIcon(pref.isNightMode() ? R.drawable.ic_palette : R.drawable.ic_palette);
            nightItem.setTitle(pref.isNightMode() ? "切换日间模式" : R.string.action_night_mode);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_night_mode) {
            boolean now = pref.isNightMode();
            pref.setNightMode(!now);
            AppCompatDelegate.setDefaultNightMode(
                    !now ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
            return true;
        } else if (id == R.id.action_export) {
            doExport();
            return true;
        } else if (id == R.id.action_import) {
            importLauncher.launch("application/json");
            return true;
        } else if (id == R.id.action_clear_history) {
            db.clearHistory();
            Toast.makeText(this, R.string.toast_history_cleared, Toast.LENGTH_SHORT).show();
            refreshAll();
            return true;
        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.about_text)
                    .setPositiveButton(R.string.dialog_confirm, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] opts = {
                getString(R.string.sort_by_time_desc),
                getString(R.string.sort_by_time_asc),
                getString(R.string.sort_by_title),
                getString(R.string.sort_by_views)
        };
        String[] values = {
                DatabaseHelper.SORT_TIME_DESC,
                DatabaseHelper.SORT_TIME_ASC,
                DatabaseHelper.SORT_TITLE,
                DatabaseHelper.SORT_VIEWS
        };
        String cur = pref.getSortMode();
        int checked = 0;
        for (int i = 0; i < values.length; i++) if (values[i].equals(cur)) checked = i;
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_sort)
                .setSingleChoiceItems(opts, checked, (d, which) -> {
                    pref.setSortMode(values[which]);
                    d.dismiss();
                    refreshAll();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ========== 导入导出 ==========

    private void doExport() {
        List<Term> all = db.getAll(pref.getSortMode());
        if (all.isEmpty()) {
            Toast.makeText(this, R.string.empty_list, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File out = JsonIO.exportToFile(this, all);
            Toast.makeText(this,
                    getString(R.string.toast_export_success, out.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();

            // 同时弹出系统分享
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", out);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/json");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent chooser = Intent.createChooser(share, getString(R.string.action_export));
            try {
                shareFileLauncher.launch(chooser);
            } catch (Exception ignored) { }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.toast_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void onImportFilePicked(Uri uri) {
        if (uri == null) return;
        try {
            File tmp = new File(getCacheDir(), "import_" + System.currentTimeMillis() + ".json");
            try (java.io.InputStream is = getContentResolver().openInputStream(uri);
                 java.io.OutputStream os = new java.io.FileOutputStream(tmp)) {
                if (is == null) throw new RuntimeException("无法读取文件");
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
            }
            int n = JsonIO.importFromFile(this, tmp);
            tmp.delete();
            Toast.makeText(this, getString(R.string.toast_import_success, n), Toast.LENGTH_SHORT).show();
            refreshAll();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.toast_import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ========== Activity Result 回调 ==========

    private void onDetailResult(ActivityResult result) {
        refreshAll();
    }

    private void onAddEditResult(ActivityResult result) {
        refreshAll();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
}
