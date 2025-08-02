package com.lrz.ui.memory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/4/10
 * Description:内存缓存
 */
public class MemoryData {
    private static final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

    /**
     * 通过 字符串key 来获取 对象
     *
     * @param key 字符串
     * @param <T> 类型
     * @return object
     */
    public @Nullable
    static <T> T get(String key) {
        Object obj = data.get(key);
        if (obj != null) {
            try {
                return (T) obj;
            } catch (Exception e) {
                return null;
            }
        } else return null;
    }

    public @NotNull
    static <T> T get(String key, T defaultValue) {
        T t = get(key);
        if (t == null) {
            t = defaultValue;
            put(key, t);
        }
        return t;
    }

    /**
     * 将字符串的key 和value 存到内存
     *
     * @param key   字符串
     * @param value 值
     */
    public static void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 移除某个key
     */
    public static void remove(String key) {
        data.remove(key);
    }

    /**
     * 通过class 将唯一变量从内存中取出来
     *
     * @param tClass 类型
     * @param <T>    类型
     * @return obj
     */
    public @Nullable
    static <T> T get(Class<T> tClass) {
        Object obj = data.get(tClass.getName());
        if (obj != null && obj.getClass() == tClass) {
            return (T) obj;
        } else return null;
    }

    /**
     * 获取对应class 的单例对象，要保证该class 要有空参构造<p>
     * 如果没有,请使用{@link MemoryData#get(Class, Object)}
     *
     * @param tClass Class
     * @param <T>    范型
     * @return 返回实例对象
     */
    public @NonNull
    static <T> T getNotNull(Class<T> tClass) {
        T t = get(tClass);
        if (t == null) {
            try {
                t = tClass.newInstance();
                put(tClass.getName(), t);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return t;
    }

    public @NonNull
    static <T> T get(Class<T> tClass, T defaultValue) {
        T t = get(tClass);
        if (t == null) {
            t = defaultValue;
            put(tClass.getName(), t);
        }
        return t;
    }

    /**
     * 通过class 将唯一变量保存到内存中
     *
     * @param tClass 类型
     * @param value  值
     */
    public static void put(Class<?> tClass, Object value) {
        data.put(tClass.getName(), value);
    }

    /**
     * 移除某个key
     */
    public static void remove(Class<?> tClass) {
        data.remove(tClass.getName());
    }
}
