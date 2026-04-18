package com.clean.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.UnsupportedEncodingException;

public final class JsonProcessor {
    private JsonProcessor() {}

    public static Object parseDeep(String body) {
        Object json = JSON.parse(body);
        if (json instanceof JSONObject) {
            urlDecodeObject((JSONObject) json);
            expandNestedObject((JSONObject) json);
        } else if (json instanceof JSONArray) {
            urlDecodeArray((JSONArray) json);
            expandNestedArray((JSONArray) json);
        }
        return json;
    }

    public static String pretty(Object json) {
        return JSON.toJSONString(
                json,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty
        );
    }

    public static boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.length() < 2) return false;
        char f = t.charAt(0);
        char l = t.charAt(t.length() - 1);
        return (f == '{' && l == '}') || (f == '[' && l == ']');
    }

    private static void urlDecodeObject(JSONObject obj) {
        try {
            for (String key : obj.keySet()) {
                Object value = obj.get(key);
                if (value instanceof String) {
                    obj.put(key, UrlDecoder.decodeFully((String) value));
                } else if (value instanceof JSONObject) {
                    urlDecodeObject((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    urlDecodeArray((JSONArray) value);
                }
            }
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    private static void urlDecodeArray(JSONArray arr) {
        try {
            for (int i = 0; i < arr.size(); i++) {
                Object value = arr.get(i);
                if (value instanceof String) {
                    arr.set(i, UrlDecoder.decodeFully((String) value));
                } else if (value instanceof JSONObject) {
                    urlDecodeObject((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    urlDecodeArray((JSONArray) value);
                }
            }
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    private static void expandNestedObject(JSONObject obj) {
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            Object expanded = tryExpand(value);
            if (expanded != value) {
                obj.put(key, expanded);
                value = expanded;
            }
            if (value instanceof JSONObject) {
                expandNestedObject((JSONObject) value);
            } else if (value instanceof JSONArray) {
                expandNestedArray((JSONArray) value);
            }
        }
    }

    private static void expandNestedArray(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            Object value = arr.get(i);
            Object expanded = tryExpand(value);
            if (expanded != value) {
                arr.set(i, expanded);
                value = expanded;
            }
            if (value instanceof JSONObject) {
                expandNestedObject((JSONObject) value);
            } else if (value instanceof JSONArray) {
                expandNestedArray((JSONArray) value);
            }
        }
    }

    private static Object tryExpand(Object value) {
        if (!(value instanceof String)) return value;
        String s = ((String) value).trim();
        if (!looksLikeJson(s)) return value;
        try {
            Object parsed = JSON.parse(s);
            if (parsed instanceof JSONObject || parsed instanceof JSONArray) return parsed;
        } catch (Exception ignored) {
        }
        return value;
    }
}
