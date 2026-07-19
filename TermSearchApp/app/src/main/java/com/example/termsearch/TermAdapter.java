package com.example.termsearch;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 词条列表适配器，支持关键词高亮
 */
public class TermAdapter extends RecyclerView.Adapter<TermAdapter.VH> {

    public interface OnTermClickListener {
        void onClick(Term term);
    }

    public interface OnFavoriteToggleListener {
        void onToggle(Term term, boolean newFav);
    }

    private final List<Term> data = new ArrayList<>();
    private String keyword = "";
    private final OnTermClickListener clickListener;
    private final OnFavoriteToggleListener favListener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public TermAdapter(OnTermClickListener clickListener, OnFavoriteToggleListener favListener) {
        this.clickListener = clickListener;
        this.favListener = favListener;
    }

    public void setData(List<Term> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void setKeyword(String kw) {
        this.keyword = kw == null ? "" : kw.trim();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Term t = data.get(position);
        h.title.setText(highlight(t.getTitle()));
        h.content.setText(highlight(t.getContent()));

        // 标签
        String tags = t.getTags();
        if (tags == null || tags.trim().isEmpty()) {
            h.tags.setVisibility(View.GONE);
        } else {
            h.tags.setVisibility(View.VISIBLE);
            h.tags.setText("#" + tags.replace(",", "  #"));
        }

        // 分类
        h.category.setText(t.getCategory());

        // 浏览数与时间
        h.views.setText(t.getViews() + " 次浏览");
        h.time.setText(sdf.format(new Date(t.getUpdatedAt())));

        // 收藏图标
        h.star.setImageResource(t.isFavorite() ? R.drawable.ic_star : R.drawable.ic_star_outline);
        h.star.setColorFilter(t.isFavorite()
                ? h.itemView.getContext().getResources().getColor(R.color.star_on)
                : h.itemView.getContext().getResources().getColor(R.color.star_off));

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(t);
        });

        h.star.setOnClickListener(v -> {
            if (favListener != null) favListener.onToggle(t, !t.isFavorite());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /** 关键词高亮（大小写不敏感） */
    private CharSequence highlight(String src) {
        if (src == null) return "";
        if (keyword.isEmpty()) return src;
        String lower = src.toLowerCase(Locale.getDefault());
        String kw = keyword.toLowerCase(Locale.getDefault());
        int idx = lower.indexOf(kw);
        if (idx < 0) return src;
        SpannableString sp = new SpannableString(src);
        while (idx >= 0) {
            sp.setSpan(new ForegroundColorSpan(0xFFFF5722),
                    idx, idx + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            idx = lower.indexOf(kw, idx + keyword.length());
        }
        return sp;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, content, tags, category, views, time;
        ImageView star;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tv_title);
            content = v.findViewById(R.id.tv_content);
            tags = v.findViewById(R.id.tv_tags);
            category = v.findViewById(R.id.tv_category);
            views = v.findViewById(R.id.tv_views);
            time = v.findViewById(R.id.tv_time);
            star = v.findViewById(R.id.iv_star);
        }
    }
}
