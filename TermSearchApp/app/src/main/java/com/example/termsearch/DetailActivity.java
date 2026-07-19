package com.example.termsearch;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 词条详情：查看 / 收藏 / 复制 / 分享 / 编辑 / 删除
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "term_id";

    private MaterialToolbar toolbar;
    private TextView tvTitle, tvContent, tvCategory, tvViews, tvTime, tvTags;
    private ImageButton btnFavorite;
    private View btnEdit, btnCopy, btnShare, btnDelete;

    private DatabaseHelper db;
    private Term term;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::onEditResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = DatabaseHelper.get(this);

        bindViews();
        setupToolbar();
        setupBottomButtons();

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id <= 0) {
            finish();
            return;
        }
        term = db.getTerm(id);
        if (term == null) {
            Toast.makeText(this, "词条不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindTerm(term);
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvCategory = findViewById(R.id.tv_category);
        tvViews = findViewById(R.id.tv_views);
        tvTime = findViewById(R.id.tv_time);
        tvTags = findViewById(R.id.tv_tags);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnEdit = findViewById(R.id.btn_edit);
        btnCopy = findViewById(R.id.btn_copy);
        btnShare = findViewById(R.id.btn_share);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        // 导航按钮使用平台 close 图标
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
    }

    private void setupBottomButtons() {
        btnEdit.setOnClickListener(v -> {
            Intent it = new Intent(this, AddEditActivity.class);
            it.putExtra(AddEditActivity.EXTRA_ID, term.getId());
            editLauncher.launch(it);
        });
        btnCopy.setOnClickListener(v -> copyToClipboard());
        btnShare.setOnClickListener(v -> shareTerm());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void bindTerm(Term t) {
        tvTitle.setText(t.getTitle());
        tvContent.setText(t.getContent());
        tvCategory.setText(t.getCategory());
        tvViews.setText(t.getViews() + " 次浏览");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvTime.setText("更新：" + sdf.format(new Date(t.getUpdatedAt())));

        if (t.getTags() == null || t.getTags().trim().isEmpty()) {
            tvTags.setVisibility(View.GONE);
        } else {
            tvTags.setVisibility(View.VISIBLE);
            tvTags.setText("#" + t.getTags().replace(",", "  #"));
        }
        updateFavoriteIcon();
        toolbar.setTitle(t.getTitle());
    }

    private void updateFavoriteIcon() {
        boolean fav = term != null && term.isFavorite();
        btnFavorite.setImageResource(fav ? R.drawable.ic_star : R.drawable.ic_star_outline);
        btnFavorite.setColorFilter(fav
                ? getResources().getColor(R.color.star_on)
                : getResources().getColor(R.color.star_off));
    }

    private void toggleFavorite() {
        boolean now = term.isFavorite();
        db.toggleFavorite(term.getId(), !now);
        term.setFavorite(!now);
        updateFavoriteIcon();
        Toast.makeText(this, !now ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
    }

    private void copyToClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String text = term.getTitle() + "\n\n" + term.getContent();
        cm.setPrimaryClip(ClipData.newPlainText("term", text));
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show();
    }

    private void shareTerm() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, term.getTitle());
        share.putExtra(Intent.EXTRA_TEXT,
                term.getTitle() + "\n\n" + term.getContent()
                        + (term.getTags().isEmpty() ? "" : "\n\n标签：" + term.getTags()));
        try {
            startActivity(Intent.createChooser(share, getString(R.string.action_share)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_no_share_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.toast_confirm_delete)
                .setPositiveButton(R.string.dialog_confirm, (d, w) -> {
                    db.deleteTerm(term.getId());
                    Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void onEditResult(ActivityResult result) {
        if (term == null) return;
        term = db.getTerm(term.getId());
        if (term == null) {
            finish();
            return;
        }
        bindTerm(term);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent it = new Intent(this, AddEditActivity.class);
            it.putExtra(AddEditActivity.EXTRA_ID, term.getId());
            editLauncher.launch(it);
            return true;
        } else if (id == R.id.action_share) {
            shareTerm();
            return true;
        } else if (id == R.id.action_copy) {
            copyToClipboard();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
