package com.lrz.ui.mvp;

import java.util.Objects;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/6/8
 * Description:用于刷新ui的任务，通过tag来排重
 */
public abstract class UIRunnable implements Runnable {
    private final String TAG;

    public UIRunnable(String tag) {
        TAG = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UIRunnable that = (UIRunnable) o;
        return Objects.equals(TAG, that.TAG);
    }

    @Override
    public int hashCode() {
        return TAG.hashCode();
    }
}
