package com.example.termsearch;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 词条数据的 JSON 导入/导出
 * 格式：{ "version":1, "terms":[{title,content,category,tags,favorite,...}] }
 */
public class JsonIO {

    public static File exportToFile(Context ctx, List<Term> terms) throws Exception {
        File dir = ctx.getExternalFilesDir(null);
        if (dir == null) dir = ctx.getFilesDir();
        File out = new File(dir, "term_export_" + System.currentTimeMillis() + ".json");

        JSONArray arr = new JSONArray();
        for (Term t : terms) {
            JSONObject o = new JSONObject();
            o.put("title", t.getTitle());
            o.put("content", t.getContent());
            o.put("category", t.getCategory());
            o.put("tags", t.getTags());
            o.put("favorite", t.isFavorite());
            o.put("views", t.getViews());
            arr.put(o);
        }
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("exported_at", System.currentTimeMillis());
        root.put("terms", arr);

        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8);
        w.write(root.toString(2));
        w.flush();
        w.close();
        return out;
    }

    /** 读取文件并导入到数据库，返回导入条数 */
    public static int importFromFile(Context ctx, File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();

        JSONObject root = new JSONObject(sb.toString());
        JSONArray arr = root.optJSONArray("terms");
        if (arr == null) return 0;

        DatabaseHelper db = DatabaseHelper.get(ctx);
        int n = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Term t = new Term();
            t.setTitle(o.optString("title", ""));
            t.setContent(o.optString("content", ""));
            t.setCategory(o.optString("category", Term.CATEGORY_NONE));
            t.setTags(o.optString("tags", ""));
            t.setFavorite(o.optBoolean("favorite", false));
            t.setViews(o.optInt("views", 0));
            if (t.getTitle().isEmpty() || t.getContent().isEmpty()) continue;
            db.insertTerm(t);
            n++;
        }
        return n;
    }
}
