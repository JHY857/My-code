package com.wikisearch.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.wikisearch.app.R;
import com.wikisearch.app.model.Entry;
import com.wikisearch.app.util.PrefsManager;
import com.wikisearch.app.util.ThemeUtils;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;
    private OnEntryClickListener listener;

    public interface OnEntryClickListener {
        void onEntryClick(Entry entry);
    }

    public EntryAdapter(Context context, List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Entry> entries) {
        this.entryList = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entryList.get(position);
        holder.tvTitle.setText(entry.getTitle());
        holder.tvSummary.setText(entry.getSummary());
        holder.tvCategory.setText(entry.getCategory());

        holder.tvTitle.setTextColor(ThemeUtils.getTextPrimaryColor(context));
        holder.tvSummary.setTextColor(ThemeUtils.getTextSecondaryColor(context));
        holder.cardView.setCardBackgroundColor(ThemeUtils.getCardBackgroundColor(context));

        int categoryColor = getCategoryColor(entry.getCategory());
        holder.tvCategory.setBackgroundColor(categoryColor);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEntryClick(entry);
                }
            }
        });
    }

    private int getCategoryColor(String category) {
        if (category == null) return context.getResources().getColor(R.color.primary);
        switch (category) {
            case "科技":
                return context.getResources().getColor(R.color.category_tech);
            case "历史":
                return context.getResources().getColor(R.color.category_history);
            case "科学":
                return context.getResources().getColor(R.color.category_science);
            case "文化":
                return context.getResources().getColor(R.color.category_culture);
            case "地理":
                return context.getResources().getColor(R.color.category_geography);
            case "生物":
                return context.getResources().getColor(R.color.category_biology);
            case "医学":
                return context.getResources().getColor(R.color.category_medicine);
            case "艺术":
                return context.getResources().getColor(R.color.category_art);
            default:
                return context.getResources().getColor(R.color.primary);
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvSummary;
        TextView tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }
}
