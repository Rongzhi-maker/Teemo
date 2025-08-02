package com.lrz.ui.recycle.inter;

import android.view.MotionEvent;
import android.view.View;

/**
 * Author:  liurongzhi
 * CreateTime:  2024/1/22
 * Description:
 */
public interface OnRecycleGestureListener<D> {
    void onCancel(View view, int position, D data, MotionEvent event);

    void onSingleClick(View view, int position, D data, MotionEvent event);

    void onDoubleClick(View view, int position, D data, MotionEvent event);

    void onPress(View view, int position, D data, MotionEvent event);

    void onLongPress(View view, int position, D data, MotionEvent event);

    void onUp(View view, int position, D data, MotionEvent event);

    void onDown(View view, int position, D data, MotionEvent event);

    void onLongClick(View view, int position, D data, MotionEvent event);
}
