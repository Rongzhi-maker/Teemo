package com.lrz.ui.base;

import java.util.HashMap;
import java.util.Map;

/**
 * 兼容intent传递大数据，防止数据过大造成崩溃
 */
public class IntentDataContainer {

    private final Map<String, Object> mCachedMap = new HashMap<>();
    private static volatile IntentDataContainer instance = null;

    private IntentDataContainer() {
    }

    public static IntentDataContainer getInstance() {
        if (instance == null) {
            synchronized (IntentDataContainer.class) {
                if (instance == null) {
                    instance = new IntentDataContainer();
                }
            }
        }
        return instance;
    }

    public void saveData(String key, Object data) {
        mCachedMap.put(key, data);
    }

    public void removeData(String key) {
        mCachedMap.remove(key);
    }

    public Object getData(String key) {
        return mCachedMap.get(key);
    }

    public Object getAndRemoveData(String key) {
        return mCachedMap.remove(key);
    }

}
