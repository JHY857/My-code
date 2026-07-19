package com.wikisearch.app.model;

import java.io.Serializable;

public class Entry implements Serializable {
    private int id;
    private String title;
    private String content;
    private String summary;
    private String category;
    private int views;
    private boolean isFavorite;
    private long createTime;
    private long updateTime;

    public Entry() {
    }

    public Entry(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
        if (content != null && content.length() > 100) {
            this.summary = content.substring(0, 100) + "...";
        } else {
            this.summary = content;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        if (content != null && content.length() > 100) {
            this.summary = content.substring(0, 100) + "...";
        } else {
            this.summary = content;
        }
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
