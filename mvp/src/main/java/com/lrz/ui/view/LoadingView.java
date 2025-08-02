package com.lrz.ui.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lrz.ui.mvp.R;


/**
 * Author And Date: liurongzhi on 2021/12/1.
 * Description: com.yilan.sdk.player.ylplayer.ui
 */
public class LoadingView extends View {

    //圆半径27px
    private int mCircleRaius = 27;
    //圆距离view中心点最小距离
    private int mCircleInterval = 8;
    private Paint paint;
    private float viewHalfWidth = 0;
    private float viewHalfHeight = 0;
    private float mAnimValue = 0f;
    private boolean autoStart = true;//是否自动执行动画

    public LoadingView(Context context) {
        super(context);
        initView(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    void initView(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);//TypedArray是一个数组容器
            autoStart = typedArray.getBoolean(R.styleable.LoadingView_autoStart, true);
            typedArray.recycle();
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.color_theme));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewHalfWidth = getMeasuredWidth() >> 1;
        viewHalfHeight = getMeasuredHeight() >> 1;

        mCircleRaius = (int) (viewHalfWidth / 3);
        mCircleInterval = mCircleRaius / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画顶部的圆
        drawCircleTop(canvas);
        //画左边的圆
        drawCircleLeft(canvas);
        //画右边的圆
        drawCircleRight(canvas);
        //画底部的圆
        drawCircleBottom(canvas);
    }

    //画顶部的圆
    private void drawCircleTop(Canvas canvas) {
        canvas.save();
        canvas.rotate(360 * mAnimValue, viewHalfHeight, viewHalfHeight);
        canvas.translate(viewHalfWidth, viewHalfHeight - mCircleInterval - ((mCircleRaius * 2 - mCircleInterval) * (0.5f - Math.abs(0.5f - mAnimValue))) * 2);
        canvas.drawCircle(0, 0, mCircleRaius, paint);
        canvas.restore();
    }

    //画右边的圆
    private void drawCircleRight(Canvas canvas) {
        canvas.save();
        canvas.rotate(360 * mAnimValue, viewHalfHeight, viewHalfHeight);
        canvas.translate(viewHalfWidth + mCircleInterval + ((mCircleRaius * 2 - mCircleInterval) * (0.5f - Math.abs(0.5f - mAnimValue))) * 2, viewHalfHeight);
        canvas.drawCircle(0, 0, mCircleRaius, paint);
        canvas.restore();
    }

    //画左边的圆
    private void drawCircleLeft(Canvas canvas) {
        canvas.save();
        canvas.rotate(360 * mAnimValue, viewHalfHeight, viewHalfHeight);
        canvas.translate(viewHalfWidth - (mCircleRaius * 2 * (0.5f - Math.abs(0.5f - mAnimValue))) * 2, viewHalfHeight);
        canvas.drawCircle(0, 0, mCircleRaius, paint);
        canvas.restore();
    }

    //画底部的圆
    private void drawCircleBottom(Canvas canvas) {
        canvas.save();
        canvas.rotate(360 * mAnimValue, viewHalfHeight, viewHalfHeight);
        canvas.translate(viewHalfWidth, viewHalfHeight + mCircleInterval + ((mCircleRaius * 2 - mCircleInterval) * (0.5f - Math.abs(0.5f - mAnimValue))) * 2);
        canvas.drawCircle(0, 0, mCircleRaius, paint);
        canvas.restore();
    }

    final ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);

    public void start() {
        if (getVisibility() != VISIBLE || getWindowVisibility() != VISIBLE) {
            return;
        }
        if (animator.isRunning()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            animator.setCurrentFraction(mAnimValue);
        }
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            mAnimValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    public void stop() {
        if (animator.isRunning()) {
            animator.cancel();
            mAnimValue = 0f;
        }
    }


    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) {
            if (autoStart) start();
        } else {
            if (autoStart) stop();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (autoStart) stop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (autoStart) start();
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * 设置当前进度，记得先调用stop停止
     *
     * @param percent
     */
    public void setPercent(@FloatRange(from = 0f, to = 1f) float percent) {
        mAnimValue = percent;
        invalidate();
    }
}
