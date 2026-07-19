package com.example.termsearch;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增 / 编辑词条
 */
public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "term_id";

    private MaterialToolbar toolbar;
    private EditText etTitle, etTags, etContent;
    private Spinner spCategory;
    private ImageButton btnAddCategory;
    private SwitchCompat swFavorite;

    private DatabaseHelper db;
    private long editId = -1;
    private Term editing;
    private ArrayAdapter<String> categoryAdapter;
    private final List<String> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = DatabaseHelper.get(this);

        bindViews();
        setupToolbar();
        setupCategorySpinner();
        setupAddCategoryButton();

        editId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (editId > 0) {
            editing = db.getTerm(editId);
            if (editing == null) {
                Toast.makeText(this, "词条不存在", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            fillForm(editing);
            toolbar.setTitle(R.string.action_edit);
        } else {
            toolbar.setTitle(R.string.title_add_edit);
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etTags = findViewById(R.id.et_tags);
        etContent = findViewById(R.id.et_content);
        spCategory = findViewById(R.id.sp_category);
        btnAddCategory = findViewById(R.id.btn_add_category);
        swFavorite = findViewById(R.id.sw_favorite);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        loadCategories();
        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.add(Term.CATEGORY_NONE);
        categoryList.addAll(db.getCategories());
    }

    private void setupAddCategoryButton() {
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        EditText et = view.findViewById(R.id.et_input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_category)
                .setView(view)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    db.insertCategory(name);
                    loadCategories();
                    categoryAdapter.notifyDataSetChanged();
                    // 选中刚加的分类
                    for (int i = 0; i < categoryList.size(); i++) {
                        if (categoryList.get(i).equals(name)) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void fillForm(Term t) {
        etTitle.setText(t.getTitle());
        etContent.setText(t.getContent());
        etTags.setText(t.getTags());
        swFavorite.setChecked(t.isFavorite());
        // 选中分类
        String cat = t.getCategory();
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).equals(cat)) {
                spCategory.setSelection(i);
                return;
            }
        }
        spCategory.setSelection(0);
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, R.string.toast_invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        String category = (String) spCategory.getSelectedItem();
        String tags = etTags.getText().toString().trim();
        boolean fav = swFavorite.isChecked();

        if (editing != null) {
            editing.setTitle(title);
            editing.setContent(content);
            editing.setCategory(category == null ? Term.CATEGORY_NONE : category);
            editing.setTags(tags);
            editing.setFavorite(fav);
            db.updateTerm(editing);
        } else {
            Term t = new Term();
            t.setTitle(title);
            t.setContent(content);
            t.setCategory(category == null ? Term.CATEGORY_NONE : category);
            t.setTags(tags);
            t.setFavorite(fav);
            db.insertTerm(t);
        }
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
