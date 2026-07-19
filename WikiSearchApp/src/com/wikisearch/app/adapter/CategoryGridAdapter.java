package com.wikisearch.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wikisearch.app.R;
import com.wikisearch.app.model.Category;

import java.util.List;

public class CategoryGridAdapter extends BaseAdapter {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryGridAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);
            holder = new ViewHolder();
            holder.layoutContainer = convertView.findViewById(R.id.layout_container);
            holder.tvIcon = convertView.findViewById(R.id.tv_icon);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvCount = convertView.findViewById(R.id.tv_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        holder.tvCount.setText(category.getCount() + "词条");
        holder.tvIcon.setText(getCategoryIcon(category.getName()));
        holder.layoutContainer.setBackgroundColor(context.getResources().getColor(category.getColorResId()));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            }
        });

        return convertView;
    }

    private String getCategoryIcon(String name) {
        if (name == null) return "?";
        switch (name) {
            case "科技":
                return "⚙";
            case "历史":
                return "📜";
            case "科学":
                return "🔬";
            case "文化":
                return "📚";
            case "地理":
                return "🌍";
            case "生物":
                return "🌿";
            case "医学":
                return "💊";
            case "艺术":
                return "🎨";
            default:
                return "📖";
        }
    }

    static class ViewHolder {
        LinearLayout layoutContainer;
        TextView tvIcon;
        TextView tvName;
        TextView tvCount;
    }
}
