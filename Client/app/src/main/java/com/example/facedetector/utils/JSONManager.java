package com.example.facedetector.utils;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class JSONManager {

    public static String parseToStr(byte[] data) {
        Log.e("PassSystem", "JsonManager before " + new String(data));
        Log.e("PassSystem", "JsonManager after " + new String(data).replaceAll("[\"\']", ""));
        return new String(data).replaceAll("[\"\']", "");
    }

    public static Map<String, String> parseToMap(byte[] data) {
        String str = new String(data);
        if (str.contains("{")) {
            return parse(str);
        }
        return Collections.emptyMap();
    }

    public static Map<String, String> parse(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String precessedStr = str.replaceAll("[\\[\\]{}\\s\"\']", "");
        String[] pairsArray = precessedStr.split(",");

        Map<String, String> result = new LinkedHashMap<>();
        for (String item: pairsArray) {
            String[] pair = item.split(":");
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].replaceAll("[\"']", "");
            String value = pair[1].replaceAll("[\"']", "");
            result.put(key, value);
        }
        return result;
    }

    public static String dump(Map<String, String> data) {
        StringBuilder result = new StringBuilder("{");
        String firstKey = data.keySet().iterator().next();
        for (String key: data.keySet()) {
            if (!key.equals(firstKey)) {
                result.append(',');
            }
            result.append('"')
                .append(key)
                .append('"')
                .append(':')
                .append('"')
                .append(data.get(key))
                .append('"');
        }
        result.append('}');
        return result.toString();
    }
}
