package com.lrz.ui.recycle.inter;

import android.view.MotionEvent;
import android.view.View;

/**
 * Author:  liurongzhi
 * CreateTime:  2024/1/22
 * Description:
 */
public interface OnGestureViewListener {
    void onCancel(View view, MotionEvent event);

    void onSingleClick(View view, MotionEvent event);

    void onDoubleClick(View view, MotionEvent event);

    void onPress(View view, MotionEvent event);

    void onLongPress(View view, MotionEvent event);

    void onUp(View view, MotionEvent event);

    void onLongClick(View view, MotionEvent event);

    void onDown(View view, MotionEvent event);
}
