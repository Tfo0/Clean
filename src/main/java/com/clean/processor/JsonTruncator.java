package com.clean.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public final class JsonTruncator {
    private JsonTruncator() {}

    public static String truncate(String json, int maxLength, RSyntaxTextArea textArea) {
        try {
            Object parsed = JSON.parse(json);
            if (parsed instanceof JSONObject) {
                truncateObject((JSONObject) parsed, maxLength, textArea);
                return JSON.toJSONString(parsed, SerializerFeature.PrettyFormat);
            } else if (parsed instanceof JSONArray) {
                truncateArray((JSONArray) parsed, maxLength, textArea);
                return JSON.toJSONString(parsed, SerializerFeature.PrettyFormat);
            }
        } catch (Exception ignored) {
        }
        return json;
    }

    private static void truncateObject(JSONObject obj, int maxLength, RSyntaxTextArea textArea) {
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof String && ((String) value).length() > maxLength) {
                String original = (String) value;
                obj.put(key, original.substring(0, maxLength - 3) + "...");
                textArea.setToolTipText(original);
            } else if (value instanceof JSONObject) {
                truncateObject((JSONObject) value, maxLength, textArea);
            } else if (value instanceof JSONArray) {
                truncateArray((JSONArray) value, maxLength, textArea);
            }
        }
    }

    private static void truncateArray(JSONArray arr, int maxLength, RSyntaxTextArea textArea) {
        for (int i = 0; i < arr.size(); i++) {
            Object value = arr.get(i);
            if (value instanceof String && ((String) value).length() > maxLength) {
                String original = (String) value;
                arr.set(i, original.substring(0, maxLength - 3) + "...");
                textArea.setToolTipText(original);
            } else if (value instanceof JSONObject) {
                truncateObject((JSONObject) value, maxLength, textArea);
            } else if (value instanceof JSONArray) {
                truncateArray((JSONArray) value, maxLength, textArea);
            }
        }
    }
}
