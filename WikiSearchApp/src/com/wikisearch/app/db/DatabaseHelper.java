package com.wikisearch.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wikisearch.app.model.Entry;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wikisearch.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ENTRIES = "entries";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_SEARCH_HISTORY = "search_history";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_VIEWS = "views";
    private static final String KEY_CREATE_TIME = "create_time";
    private static final String KEY_UPDATE_TIME = "update_time";
    private static final String KEY_ENTRY_ID = "entry_id";
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_SEARCH_TIME = "search_time";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ENTRIES_TABLE = "CREATE TABLE " + TABLE_ENTRIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_CONTENT + " TEXT,"
                + KEY_SUMMARY + " TEXT,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_VIEWS + " INTEGER DEFAULT 0,"
                + KEY_CREATE_TIME + " INTEGER,"
                + KEY_UPDATE_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_ENTRIES_TABLE);

        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ENTRY_ID + " INTEGER UNIQUE,"
                + KEY_CREATE_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_SEARCH_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_KEYWORD + " TEXT UNIQUE,"
                + KEY_SEARCH_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_HISTORY_TABLE);

        db.execSQL("CREATE INDEX idx_title ON " + TABLE_ENTRIES + "(" + KEY_TITLE + ")");
        db.execSQL("CREATE INDEX idx_category ON " + TABLE_ENTRIES + "(" + KEY_CATEGORY + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY);
        onCreate(db);
    }

    public long addEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, entry.getTitle());
        values.put(KEY_CONTENT, entry.getContent());
        values.put(KEY_SUMMARY, entry.getSummary());
        values.put(KEY_CATEGORY, entry.getCategory());
        values.put(KEY_VIEWS, entry.getViews());
        long time = System.currentTimeMillis();
        values.put(KEY_CREATE_TIME, time);
        values.put(KEY_UPDATE_TIME, time);
        long id = db.insert(TABLE_ENTRIES, null, values);
        db.close();
        return id;
    }

    public Entry getEntry(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ENTRIES,
                new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_SUMMARY, KEY_CATEGORY, KEY_VIEWS, KEY_CREATE_TIME, KEY_UPDATE_TIME},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        Entry entry = null;
        if (cursor != null && cursor.moveToFirst()) {
            entry = cursorToEntry(cursor);
            cursor.close();
        }
        db.close();
        return entry;
    }

    public List<Entry> getAllEntries() {
        List<Entry> entryList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ENTRIES + " ORDER BY " + KEY_VIEWS + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Entry entry = cursorToEntry(cursor);
                entryList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entryList;
    }

    public List<Entry> searchEntries(String keyword) {
        List<Entry> entryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ENTRIES + " WHERE " + KEY_TITLE + " LIKE ? OR " + KEY_CONTENT + " LIKE ? ORDER BY " + KEY_VIEWS + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%", "%" + keyword + "%"});
        if (cursor.moveToFirst()) {
            do {
                Entry entry = cursorToEntry(cursor);
                entryList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entryList;
    }

    public List<Entry> getEntriesByCategory(String category) {
        List<Entry> entryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ENTRIES,
                new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_SUMMARY, KEY_CATEGORY, KEY_VIEWS, KEY_CREATE_TIME, KEY_UPDATE_TIME},
                KEY_CATEGORY + "=?",
                new String[]{category},
                null, null, KEY_VIEWS + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Entry entry = cursorToEntry(cursor);
                entryList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entryList;
    }

    public List<Entry> getHotEntries(int limit) {
        List<Entry> entryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ENTRIES + " ORDER BY " + KEY_VIEWS + " DESC LIMIT " + limit;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Entry entry = cursorToEntry(cursor);
                entryList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entryList;
    }

    public void incrementViews(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ENTRIES + " SET " + KEY_VIEWS + " = " + KEY_VIEWS + " + 1 WHERE " + KEY_ID + " = " + id);
        db.close();
    }

    public int getEntryCount() {
        String countQuery = "SELECT * FROM " + TABLE_ENTRIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int getCategoryCount(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ENTRIES,
                new String[]{KEY_ID},
                KEY_CATEGORY + "=?",
                new String[]{category},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public void addToFavorites(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ENTRY_ID, entryId);
        values.put(KEY_CREATE_TIME, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void removeFromFavorites(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
        db.close();
    }

    public boolean isFavorite(int entryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES,
                new String[]{KEY_ID},
                KEY_ENTRY_ID + "=?",
                new String[]{String.valueOf(entryId)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<Entry> getFavorites() {
        List<Entry> entryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.* FROM " + TABLE_ENTRIES + " e INNER JOIN " + TABLE_FAVORITES +
                " f ON e." + KEY_ID + " = f." + KEY_ENTRY_ID + " ORDER BY f." + KEY_CREATE_TIME + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Entry entry = cursorToEntry(cursor);
                entry.setFavorite(true);
                entryList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entryList;
    }

    public void addSearchHistory(String keyword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_KEYWORD, keyword);
        values.put(KEY_SEARCH_TIME, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_SEARCH_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<String> getSearchHistory(int limit) {
        List<String> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_KEYWORD + " FROM " + TABLE_SEARCH_HISTORY +
                " ORDER BY " + KEY_SEARCH_TIME + " DESC LIMIT " + limit;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                historyList.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return historyList;
    }

    public void clearSearchHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SEARCH_HISTORY, null, null);
        db.close();
    }

    private Entry cursorToEntry(Cursor cursor) {
        Entry entry = new Entry();
        entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        entry.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
        entry.setContent(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT)));
        entry.setSummary(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUMMARY)));
        entry.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY)));
        entry.setViews(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VIEWS)));
        entry.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATE_TIME)));
        entry.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_UPDATE_TIME)));
        entry.setFavorite(isFavorite(entry.getId()));
        return entry;
    }
}
