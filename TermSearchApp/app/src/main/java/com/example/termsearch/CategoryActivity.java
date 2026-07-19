package com.example.termsearch;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类管理：增删分类
 */
public class CategoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rv;
    private View emptyView;
    private FloatingActionButton fabAdd;

    private DatabaseHelper db;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        db = DatabaseHelper.get(this);

        toolbar = findViewById(R.id.toolbar);
        rv = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        fabAdd = findViewById(R.id.fab_add);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onClick(String name) {
                // 在分类管理界面直接退出并提示主界面去到该分类
                Toast.makeText(CategoryActivity.this, "在主界面侧边栏点选该分类可筛选", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(String name) {
                confirmDelete(name);
            }
        });
        rv.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddDialog());
        refresh();
    }

    private void refresh() {
        List<String> cats = db.getCategories();
        List<CategoryAdapter.CategoryItem> items = new ArrayList<>();
        for (String c : cats) {
            int n = db.getByCategory(c, null).size();
            items.add(new CategoryAdapter.CategoryItem(c, n));
        }
        adapter.setData(items);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        EditText et = view.findViewById(R.id.et_input);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_category)
                .setView(view)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    long r = db.insertCategory(name);
                    if (r < 0) {
                        Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
                    }
                    refresh();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDelete(final String name) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage("删除分类「" + name + "」？该分类下的词条不会被删除，会变为未分类。")
                .setPositiveButton(R.string.dialog_confirm, (d, w) -> {
                    db.deleteCategory(name);
                    refresh();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
