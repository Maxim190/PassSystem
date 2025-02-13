package com.example.facedetector.utils;

import android.util.Log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class JSONManager {

    public static String parseToStr(byte[] data) {
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
        //Log.e("PassSystem", "Parse " + str);
        String precessedStr = str.replaceAll("[\\[\\]{}\"\']", "");
        String[] pairsArray = precessedStr.split("\\s*,\\s*");

        int keyPostfix = 0;
        int keyIndex = 0;
        int valueIndex = 1;
        Map<String, String> result = new LinkedHashMap<>();
        for (String item: pairsArray) {
            String[] pair = item.split("\\s*:\\s*");
            if (pair.length != 2) {
                continue;
            }
            if (result.containsKey(pair[keyIndex])) {
                pair[keyIndex] += keyPostfix++;
            }
            result.put(pair[keyIndex], pair[valueIndex]);
        }
        //result.forEach((k, v)-> Log.e("PassSystem", "key:" + k + " value:" + v));
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
