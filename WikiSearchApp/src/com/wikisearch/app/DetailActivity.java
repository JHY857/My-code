package com.wikisearch.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.wikisearch.app.db.DatabaseHelper;
import com.wikisearch.app.model.Entry;
import com.wikisearch.app.util.PrefsManager;
import com.wikisearch.app.util.ThemeUtils;

public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView btnBack;
    private ImageView btnFavorite;
    private ImageView btnShare;
    private TextView tvTitle;
    private TextView tvCategory;
    private TextView tvViews;
    private TextView tvContent;

    private Entry entry;
    private DatabaseHelper dbHelper;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_detail);

        dbHelper = DatabaseHelper.getInstance(this);

        entry = (Entry) getIntent().getSerializableExtra("entry");
        if (entry == null) {
            finish();
            return;
        }

        dbHelper.incrementViews(entry.getId());
        entry = dbHelper.getEntry(entry.getId());

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnShare = findViewById(R.id.btn_share);
        tvTitle = findViewById(R.id.tv_title);
        tvCategory = findViewById(R.id.tv_category);
        tvViews = findViewById(R.id.tv_views);
        tvContent = findViewById(R.id.tv_content);

        toolbar.setBackgroundColor(ThemeUtils.getPrimaryColor(this));
        findViewById(R.id.toolbar_title).setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareEntry();
            }
        });

        tvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyContent();
                return true;
            }
        });
    }

    private void loadData() {
        PrefsManager prefs = PrefsManager.getInstance(this);
        tvTitle.setText(entry.getTitle());
        tvTitle.setTextSize(prefs.getTitleFontSize());
        tvTitle.setTextColor(ThemeUtils.getTextPrimaryColor(this));

        tvCategory.setText(entry.getCategory());
        tvCategory.setBackgroundColor(getCategoryColor(entry.getCategory()));

        tvViews.setText(entry.getViews() + " 阅读");
        tvViews.setTextColor(ThemeUtils.getTextSecondaryColor(this));

        tvContent.setText(entry.getContent());
        tvContent.setTextSize(prefs.getContentFontSize());
        tvContent.setTextColor(ThemeUtils.getTextPrimaryColor(this));

        isFavorite = dbHelper.isFavorite(entry.getId());
        updateFavoriteIcon();
    }

    private int getCategoryColor(String category) {
        if (category == null) return getResources().getColor(R.color.primary);
        switch (category) {
            case "科技":
                return getResources().getColor(R.color.category_tech);
            case "历史":
                return getResources().getColor(R.color.category_history);
            case "科学":
                return getResources().getColor(R.color.category_science);
            case "文化":
                return getResources().getColor(R.color.category_culture);
            case "地理":
                return getResources().getColor(R.color.category_geography);
            case "生物":
                return getResources().getColor(R.color.category_biology);
            case "医学":
                return getResources().getColor(R.color.category_medicine);
            case "艺术":
                return getResources().getColor(R.color.category_art);
            default:
                return getResources().getColor(R.color.primary);
        }
    }

    private void toggleFavorite() {
        if (isFavorite) {
            dbHelper.removeFromFavorites(entry.getId());
            isFavorite = false;
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addToFavorites(entry.getId());
            isFavorite = true;
            Toast.makeText(this, "已添加收藏", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(android.R.drawable.star_big_on);
        } else {
            btnFavorite.setImageResource(android.R.drawable.star_big_off);
        }
    }

    private void shareEntry() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, entry.getTitle() + "\n\n" + entry.getContent());
        startActivity(Intent.createChooser(shareIntent, "分享"));
    }

    private void copyContent() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(entry.getTitle(), entry.getContent());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
    }
}
