package com.example.termsearch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类列表适配器
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnCategoryActionListener {
        void onClick(String name);
        void onDelete(String name);
    }

    private final List<CategoryItem> data = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public CategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<CategoryItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CategoryItem item = data.get(position);
        h.name.setText(item.name);
        h.count.setText(item.count + " 条词条");
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item.name);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item.name);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, count;
        ImageButton btnDelete;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            count = v.findViewById(R.id.tv_count);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }

    public static class CategoryItem {
        public final String name;
        public final int count;

        public CategoryItem(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
}
