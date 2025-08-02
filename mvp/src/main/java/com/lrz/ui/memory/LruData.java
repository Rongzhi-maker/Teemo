package com.lrz.ui.memory;

import android.util.LruCache;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/4/10
 * Description:内存缓存
 */
public class LruData {
    private static final LruCache<String, Object> data = new LruCache<>(30);

    /**
     * 通过 字符串key 来获取 对象
     * @param key 字符串
     * @param <T> 类型
     * @return object
     */
    public static <T> T get(String key) {
        Object obj = data.get(key);
        if (obj != null) {
            try {
                return (T) obj;
            } catch (Exception e) {
                return null;
            }
        } else return null;
    }

    /**
     * 将字符串的key 和value 存到内存
     * @param key 字符串
     * @param value 值
     */
    public static void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 移除某个key
     */
    public static void remove(String key){
        data.remove(key);
    }

    /**
     * 通过class 将唯一变量从内存中取出来
     * @param tClass 类型
     * @param <T> 类型
     * @return obj
     */
    public static <T> T get(Class<T> tClass) {
        Object obj = data.get(tClass.getName());
        if (obj != null && obj.getClass() == tClass) {
            return (T) obj;
        } else return null;
    }

    /**
     * 通过class 将唯一变量保存到内存中
     * @param tClass 类型
     * @param value 值
     */
    public static void put(Class<?> tClass, Object value) {
        data.put(tClass.getName(), value);
    }

    /**
     * 移除某个key
     */
    public static void remove(Class<?> tClass){
        data.remove(tClass.getName());
    }
}
