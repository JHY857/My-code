package com.example.termsearch;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * 词条数据模型
 */
public class Term {
    public static final String CATEGORY_NONE = "未分类";

    private long id;
    private String title;
    private String content;
    private String category;
    private String tags;        // 逗号分隔
    private boolean favorite;
    private long createdAt;
    private long updatedAt;
    private int views;

    public Term() {
        this.category = CATEGORY_NONE;
        this.tags = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category == null || category.isEmpty() ? CATEGORY_NONE : category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags == null ? "" : tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    /** 把 tag 字符串切分为列表 */
    public List<String> tagList() {
        List<String> result = new ArrayList<>();
        if (tags == null || tags.trim().isEmpty()) return result;
        for (String t : tags.split(",")) {
            String s = t.trim();
            if (!s.isEmpty()) result.add(s);
        }
        return result;
    }

    public static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(tags.get(i).trim());
        }
        return sb.toString();
    }
}
