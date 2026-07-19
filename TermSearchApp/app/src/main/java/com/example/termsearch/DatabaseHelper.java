package com.example.termsearch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 词条数据库
 * 表 terms：本地存储所有词条
 * 表 categories：自定义分类
 * 表 history：搜索历史
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "term_search.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_TERMS = "terms";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_HISTORY = "history";

    // terms 字段
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_CATEGORY = "category";
    public static final String COL_TAGS = "tags";
    public static final String COL_FAVORITE = "favorite";
    public static final String COL_CREATED = "created_at";
    public static final String COL_UPDATED = "updated_at";
    public static final String COL_VIEWS = "views";

    // categories 字段
    public static final String COL_CAT_NAME = "name";

    // history 字段
    public static final String COL_HIS_KEYWORD = "keyword";
    public static final String COL_HIS_TIME = "time_at";

    public static final String SORT_TIME_DESC = COL_UPDATED + " DESC";
    public static final String SORT_TIME_ASC = COL_UPDATED + " ASC";
    public static final String SORT_TITLE = COL_TITLE + " COLLATE NOCASE ASC";
    public static final String SORT_VIEWS = COL_VIEWS + " DESC";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper get(Context ctx) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TERMS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT, "
                + COL_TAGS + " TEXT, "
                + COL_FAVORITE + " INTEGER DEFAULT 0, "
                + COL_CREATED + " INTEGER, "
                + COL_UPDATED + " INTEGER, "
                + COL_VIEWS + " INTEGER DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_terms_title ON " + TABLE_TERMS + "(" + COL_TITLE + ")");
        db.execSQL("CREATE INDEX idx_terms_category ON " + TABLE_TERMS + "(" + COL_CATEGORY + ")");
        db.execSQL("CREATE INDEX idx_terms_favorite ON " + TABLE_TERMS + "(" + COL_FAVORITE + ")");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CAT_NAME + " TEXT UNIQUE NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_HIS_KEYWORD + " TEXT UNIQUE NOT NULL, "
                + COL_HIS_TIME + " INTEGER)");

        // 预置几个示例分类
        insertCategory(db, "技术");
        insertCategory(db, "学习");
        insertCategory(db, "生活");

        // 预置示例词条，让用户开箱即用
        insertSampleTerm(db, "Android", "一种基于 Linux 内核的移动操作系统，由 Google 主导开发。",
                "技术", "android,os,google");
        insertSampleTerm(db, "AIDE", "Android 上的集成开发环境，可在手机上直接编写并打包 Android 应用。",
                "技术", "ide,android,dev");
        insertSampleTerm(db, "SQLite", "一个轻量级嵌入式关系型数据库，广泛用于移动端本地存储。",
                "技术", "db,storage");
        insertSampleTerm(db, "番茄工作法", "以 25 分钟为一个工作单元、5 分钟休息的时间管理方法。",
                "学习", "效率,时间");
        insertSampleTerm(db, "Markdown", "一种轻量级标记语言，使用纯文本格式编写带格式的文档。",
                "技术", "text,doc");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // ========== 词条 CRUD ==========

    public long insertTerm(Term t) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues cv = termToCV(t);
        cv.put(COL_CREATED, now);
        cv.put(COL_UPDATED, now);
        long id = db.insert(TABLE_TERMS, null, cv);
        t.setId(id);
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        return id;
    }

    public int updateTerm(Term t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = termToCV(t);
        cv.put(COL_UPDATED, System.currentTimeMillis());
        t.setUpdatedAt(cv.getAsLong(COL_UPDATED));
        return db.update(TABLE_TERMS, cv, COL_ID + "=?", new String[]{String.valueOf(t.getId())});
    }

    public int deleteTerm(long id) {
        return getWritableDatabase().delete(TABLE_TERMS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Term getTerm(long id) {
        Cursor c = getReadableDatabase().query(TABLE_TERMS, null, COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Term t = null;
        if (c.moveToFirst()) t = cursorToTerm(c);
        c.close();
        return t;
    }

    public List<Term> getAll(String sort) {
        return query(null, null, sort);
    }

    public List<Term> getFavorites(String sort) {
        return query(COL_FAVORITE + "=1", null, sort);
    }

    public List<Term> getByCategory(String category, String sort) {
        if (category == null || category.isEmpty() || Term.CATEGORY_NONE.equals(category)) {
            return query(COL_CATEGORY + " IS NULL OR " + COL_CATEGORY + "=?",
                    new String[]{""}, sort);
        }
        return query(COL_CATEGORY + "=?", new String[]{category}, sort);
    }

    /** 关键词搜索：标题 + 内容 + 标签 */
    public List<Term> search(String keyword, String sort) {
        if (keyword == null || keyword.trim().isEmpty()) return getAll(sort);
        String kw = escapeLike(keyword.trim());
        String sel = COL_TITLE + " LIKE ? ESCAPE '\\' OR "
                + COL_CONTENT + " LIKE ? ESCAPE '\\' OR "
                + COL_TAGS + " LIKE ? ESCAPE '\\'";
        String arg = "%" + kw + "%";
        return query(sel, new String[]{arg, arg, arg}, sort);
    }

    public List<Term> query(String selection, String[] args, String sort) {
        String orderBy = sort == null ? SORT_TIME_DESC : sort;
        Cursor c = getReadableDatabase().query(TABLE_TERMS, null, selection, args, null, null, orderBy);
        List<Term> list = new ArrayList<>();
        while (c.moveToNext()) list.add(cursorToTerm(c));
        c.close();
        return list;
    }

    public void toggleFavorite(long id, boolean fav) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FAVORITE, fav ? 1 : 0);
        getWritableDatabase().update(TABLE_TERMS, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void incrementViews(long id) {
        getWritableDatabase().execSQL(
                "UPDATE " + TABLE_TERMS + " SET " + COL_VIEWS + "=" + COL_VIEWS + "+1 WHERE " + COL_ID + "=?",
                new Object[]{id});
    }

    public int countTerms() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE_TERMS, null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    private ContentValues termToCV(Term t) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, t.getTitle());
        cv.put(COL_CONTENT, t.getContent());
        String cat = t.getCategory();
        cv.put(COL_CATEGORY, Term.CATEGORY_NONE.equals(cat) ? "" : cat);
        cv.put(COL_TAGS, t.getTags());
        cv.put(COL_FAVORITE, t.isFavorite() ? 1 : 0);
        cv.put(COL_VIEWS, t.getViews());
        return cv;
    }

    private Term cursorToTerm(Cursor c) {
        Term t = new Term();
        t.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        t.setTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
        t.setContent(c.getString(c.getColumnIndexOrThrow(COL_CONTENT)));
        String cat = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY));
        t.setCategory(cat == null || cat.isEmpty() ? Term.CATEGORY_NONE : cat);
        t.setTags(c.getString(c.getColumnIndexOrThrow(COL_TAGS)));
        t.setFavorite(c.getInt(c.getColumnIndexOrThrow(COL_FAVORITE)) == 1);
        t.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_CREATED)));
        t.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(COL_UPDATED)));
        t.setViews(c.getInt(c.getColumnIndexOrThrow(COL_VIEWS)));
        return t;
    }

    private void insertSampleTerm(SQLiteDatabase db, String title, String content, String cat, String tags) {
        long now = System.currentTimeMillis();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_CONTENT, content);
        cv.put(COL_CATEGORY, cat);
        cv.put(COL_TAGS, tags);
        cv.put(COL_FAVORITE, 0);
        cv.put(COL_CREATED, now);
        cv.put(COL_UPDATED, now);
        cv.put(COL_VIEWS, 0);
        db.insert(TABLE_TERMS, null, cv);
    }

    // ========== 分类 ==========

    public long insertCategory(String name) {
        return insertCategory(getWritableDatabase(), name);
    }

    private static long insertCategory(SQLiteDatabase db, String name) {
        if (name == null || name.trim().isEmpty()) return -1;
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name.trim());
        return db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int deleteCategory(String name) {
        return getWritableDatabase().delete(TABLE_CATEGORIES, COL_CAT_NAME + "=?", new String[]{name});
    }

    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_CATEGORIES, new String[]{COL_CAT_NAME},
                null, null, null, null, COL_CAT_NAME + " ASC");
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    // ========== 搜索历史 ==========

    public void addHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HIS_KEYWORD, keyword.trim());
        cv.put(COL_HIS_TIME, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_HISTORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        // 最多保留 50 条
        db.execSQL("DELETE FROM " + TABLE_HISTORY + " WHERE " + COL_ID + " NOT IN ("
                + "SELECT " + COL_ID + " FROM " + TABLE_HISTORY
                + " ORDER BY " + COL_HIS_TIME + " DESC LIMIT 50)");
    }

    public List<String> getHistory() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_HISTORY, new String[]{COL_HIS_KEYWORD},
                null, null, null, null, COL_HIS_TIME + " DESC", "50");
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public void clearHistory() {
        getWritableDatabase().delete(TABLE_HISTORY, null, null);
    }

    // ========== 工具 ==========

    private String escapeLike(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '%' || c == '_') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }
}
