package com.lrz.ui.recycle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lrz.coroutine.LLog;
import com.lrz.ui.recycle.inter.OnGestureViewListener;

/**
 * Author And Date: liurongzhi on 2021/7/15.
 * Description: com.yilan.sdk.player.ylplayer.ui
 */
public class GestureView extends FrameLayout {
    private GestureDetector gestureDetector;
    private OnGestureViewListener simpleOnGestureListener;

    public GestureView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return super.onTouchEvent(event);
        if (simpleOnGestureListener == null) return false;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (simpleOnGestureListener != null) {
                simpleOnGestureListener.onUp(this, event);
                if (isLongPress) {
                    simpleOnGestureListener.onLongClick(this, event);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (simpleOnGestureListener != null) {
                simpleOnGestureListener.onCancel(this, event);
            }
        }
        return gestureDetector.onTouchEvent(event);
    }

    public void setOnGestureListener(OnGestureViewListener simpleOnGestureListener) {
        this.simpleOnGestureListener = simpleOnGestureListener;
    }

    boolean isLongPress;

    public void initView(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                LLog.e("GestureDetector", "onDown");
                isLongPress = false;
                if (simpleOnGestureListener != null) {
                    simpleOnGestureListener.onDown(GestureView.this, e);
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                LLog.e("GestureDetector", "onSingleTapConfirmed");
                if (simpleOnGestureListener != null) {
                    simpleOnGestureListener.onSingleClick(GestureView.this, e);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                LLog.e("GestureDetector", "onDoubleTap");
                if (simpleOnGestureListener != null) {
                    simpleOnGestureListener.onDoubleClick(GestureView.this, e);
                }
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                LLog.e("GestureDetector", "onShowPress");
                if (simpleOnGestureListener != null) {
                    simpleOnGestureListener.onPress(GestureView.this, e);
                }
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                isLongPress = true;
                LLog.e("GestureDetector", "onLongPress");
                if (simpleOnGestureListener != null) {
                    simpleOnGestureListener.onLongPress(GestureView.this, e);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }
}
