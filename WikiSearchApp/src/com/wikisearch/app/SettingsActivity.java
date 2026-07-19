package com.wikisearch.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wikisearch.app.db.DatabaseHelper;
import com.wikisearch.app.util.PrefsManager;
import com.wikisearch.app.util.ThemeUtils;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Switch switchNight;
    private TextView tvNightStatus;
    private RadioGroup fontSizeGroup;
    private RadioButton rbSmall;
    private RadioButton rbMedium;
    private RadioButton rbLarge;
    private TextView clearCacheItem;
    private TextView tvVersion;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_settings);

        prefsManager = PrefsManager.getInstance(this);

        initViews();
        setupListeners();
        loadSettings();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        switchNight = findViewById(R.id.switch_night);
        tvNightStatus = findViewById(R.id.tv_night_status);
        fontSizeGroup = findViewById(R.id.font_size_group);
        rbSmall = findViewById(R.id.rb_small);
        rbMedium = findViewById(R.id.rb_medium);
        rbLarge = findViewById(R.id.rb_large);
        clearCacheItem = findViewById(R.id.clear_cache_item);
        tvVersion = findViewById(R.id.tv_version);

        findViewById(R.id.toolbar).setBackgroundColor(ThemeUtils.getPrimaryColor(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switchNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefsManager.setNightMode(isChecked);
                tvNightStatus.setText(isChecked ? "开启" : "关闭");
                recreate();
            }
        });

        fontSizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int fontSize;
                if (checkedId == R.id.rb_small) {
                    fontSize = PrefsManager.FONT_SMALL;
                } else if (checkedId == R.id.rb_large) {
                    fontSize = PrefsManager.FONT_LARGE;
                } else {
                    fontSize = PrefsManager.FONT_MEDIUM;
                }
                prefsManager.setFontSize(fontSize);
                Toast.makeText(SettingsActivity.this, "字体大小已更新", Toast.LENGTH_SHORT).show();
            }
        });

        clearCacheItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearCacheDialog();
            }
        });
    }

    private void loadSettings() {
        boolean isNightMode = prefsManager.isNightMode();
        switchNight.setChecked(isNightMode);
        tvNightStatus.setText(isNightMode ? "开启" : "关闭");

        int fontSize = prefsManager.getFontSize();
        switch (fontSize) {
            case PrefsManager.FONT_SMALL:
                rbSmall.setChecked(true);
                break;
            case PrefsManager.FONT_LARGE:
                rbLarge.setChecked(true);
                break;
            case PrefsManager.FONT_MEDIUM:
            default:
                rbMedium.setChecked(true);
                break;
        }

        tvVersion.setText("版本 1.0.0");
    }

    private void showClearCacheDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清除缓存");
        builder.setMessage("确定要清除搜索历史吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseHelper.getInstance(SettingsActivity.this).clearSearchHistory();
                Toast.makeText(SettingsActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
