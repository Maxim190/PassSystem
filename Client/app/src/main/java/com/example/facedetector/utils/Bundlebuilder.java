package com.example.facedetector.utils;

import android.os.Bundle;

import java.util.Map;

public class Bundlebuilder {

    public static Bundle build(Map<String, byte[]> data) {
        Bundle bundle = new Bundle();
        for (String key: data.keySet()) {
            if (Consts.DATA_TYPE_PHOTO.equals(key)) {
                bundle.putByteArray(key, data.get(key));
            } else {
                Map<String, String> parsed = JSONManager.parse(new String(data.get(key)));
                for (String parsedKey: parsed.keySet()) {
                    bundle.putString(parsedKey, parsed.get(parsedKey));
                }
            }
        }
        return bundle;
    }
}
