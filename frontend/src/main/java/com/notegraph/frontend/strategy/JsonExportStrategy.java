package com.notegraph.frontend.strategy;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class JsonExportStrategy implements ExportStrategy {
    private static final Gson GSON = new Gson();

    @Override
    public String format(String title, String content) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        return GSON.toJson(data);
    }
}
