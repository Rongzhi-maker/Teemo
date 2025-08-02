package com.lrz.ui.view.jelly;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.ViewCompat;

import com.lrz.ui.mvp.R;
import com.lrz.ui.recycle.inter.IViewCreator;

import java.util.ArrayList;


/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.widget
 */
public class JellyLayout extends RelativeLayout implements NestedScrollingParent2, NestedScrollingChild2 {
    public RelativeLayout headLayout;
    private RelativeLayout footLayout;
    private IViewCreator headCreator;
    private IViewCreator footCreator;
    protected boolean refreshEnable = false;
    protected boolean loadMoreEnable = false;
    private OnJellyListener onJellyListener;
    private View target;
    private JellyState state = JellyState.NORMAL;//0初始状态，1正在拉动，2触发头部下拉，3触发底部上拉，4正在执行归位
    private @JellyStyle int jellyStyle = JellyStyle.FRAME;

    public void setJellyStyle(@JellyStyle int jellyStyle) {
        this.jellyStyle = jellyStyle;
    }

    public JellyLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public JellyLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public JellyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JellyLayout);//TypedArray是一个数组容器
            int style = typedArray.getInt(R.styleable.JellyLayout_uiStyle, 1);
            if (style == 0) {
                jellyStyle = JellyStyle.LINEAR;
            }
            typedArray.recycle();
        }
        headLayout = new RelativeLayout(context);
        footLayout = new RelativeLayout(context);
        LayoutParams headParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(headLayout, headParams);
        LayoutParams footParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footParams.addRule(ALIGN_PARENT_BOTTOM);
        this.addView(footLayout, footParams);

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child != headLayout || child != footLayout) {
            index = 0;
        }
        super.addView(child, index, params);
    }

    public JellyLayout headCreator(IViewCreator creator) {
        this.headCreator = creator;
        if (headCreator != null) {
            headLayout.addView(headCreator.createView(headLayout));
        }
        return this;
    }

    public JellyLayout footCreator(IViewCreator creator) {
        this.footCreator = creator;
        if (footCreator != null) {
            footLayout.addView(footCreator.createView(footLayout));
        }
        return this;
    }

    public JellyLayout setLoadMoreEnable(boolean loadMoreEnable) {
        this.loadMoreEnable = loadMoreEnable;
        return this;
    }

    public JellyLayout setRefreshEnable(boolean refreshEnable) {
        this.refreshEnable = refreshEnable;
        return this;
    }

    public JellyLayout setOnJellyListener(OnJellyListener onJellyListener) {
        this.onJellyListener = onJellyListener;
        return this;
    }

    private int maxDHead = 0;
    private int maxDFoot = 0;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        maxDHead = headLayout.getHeight();
        // footLayout.getHeight() 高度值会变化，只获取一次
        maxDFoot = footLayout.getHeight();
        if (state == JellyState.NORMAL) {
            headLayout.setTranslationY(-maxDHead);
            footLayout.setTranslationY(maxDFoot);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View view, @NonNull View target, int axes, int type) {
        this.target = target;
        return ViewCompat.SCROLL_AXIS_VERTICAL == axes;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
    }

    @Override
    public void onStopNestedScroll(@NonNull final View target, int type) {
        this.target = target;
        if (type == ViewCompat.TYPE_TOUCH) {
            //检查当前状态是否触发条件
            if (mTotalUnconsumed > maxDHead * 8f / 10f && refreshEnable) {
                //下拉刷新
                setState(JellyState.REFRESH);
            } else if (mTotalUnconsumed < -maxDFoot * 8f / 10f && loadMoreEnable) {
                //上拉加载
                setState(JellyState.LOAD_MORE);
            } else {
                returnToReset();
            }
        }
    }

    /**
     * 刷新完毕后，重置回去
     */
    public void close() {
        returnToReset();
    }

    private void returnToReset() {
        setState(JellyState.RESET);
        if (mTotalUnconsumed == 0) {
            setState(JellyState.NORMAL);
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(mTotalUnconsumed, 0f);
            animator.setDuration(140);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mTotalUnconsumed = (float) animation.getAnimatedValue();
                scrollTo(0, (int) -mTotalUnconsumed);
                if (mTotalUnconsumed == 0) {
                    setState(JellyState.NORMAL);
                }
            });
            animator.start();
        }
    }

    private float mTotalUnconsumed = 0f;

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type) {
        if (state.value > JellyState.SCROLL.value) return;
        if (type != ViewCompat.TYPE_TOUCH) return;
        float f = 1;
        if (mTotalUnconsumed > 0) f = 1 - mTotalUnconsumed / maxDHead;
        if (mTotalUnconsumed < 0) f = 1 - mTotalUnconsumed / -maxDFoot;
        mTotalUnconsumed -= (dyUnconsumed * f);
        if (mTotalUnconsumed > maxDHead) mTotalUnconsumed = maxDHead;
        if (mTotalUnconsumed < -maxDFoot) mTotalUnconsumed = -maxDFoot;
        this.scrollTo(0, (int) -mTotalUnconsumed);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (!(state.value <= JellyState.SCROLL.value)) return;
        setState(JellyState.SCROLL);
        if (mTotalUnconsumed != 0) {
            if (mTotalUnconsumed > 0 && dy > 0) {
                if (mTotalUnconsumed - dy < 0) {
                    consumed[1] = (int) mTotalUnconsumed;
                    mTotalUnconsumed = 0;
                } else {
                    consumed[1] = dy;
                    mTotalUnconsumed -= dy;
                }
            }
        }

        if (mTotalUnconsumed < 0 && dy < 0) {
            if (mTotalUnconsumed - dy < 0) {
                consumed[1] = (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                consumed[1] = dy;
                mTotalUnconsumed -= dy;
            }
        }

        if (mTotalUnconsumed > maxDHead) mTotalUnconsumed = maxDHead;
        if (mTotalUnconsumed < -maxDFoot) mTotalUnconsumed = -maxDFoot;
        this.scrollTo(0, (int) -mTotalUnconsumed);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return false;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    ArrayList<OnScrollChangeListener> onScrollChangeListeners;

    public void addOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        if (onScrollChangeListener == null) return;
        if (onScrollChangeListeners == null) onScrollChangeListeners = new ArrayList<>();
        else if (onScrollChangeListeners.contains(onScrollChangeListener)) return;
        onScrollChangeListeners.add(onScrollChangeListener);
    }

    private int mScrollX, mScrollY;

    @Override
    public void scrollTo(int x, int y) {
        if (jellyStyle == JellyStyle.LINEAR) {
            super.scrollTo(x, y);
        } else {
            if (y > 0) {
                footLayout.setTranslationY(maxDFoot - y);
            } else {
                headLayout.setTranslationY(-maxDHead - y);
            }
        }
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            if (onScrollChangeListeners != null) {
                for (OnScrollChangeListener listener : onScrollChangeListeners) {
                    listener.onScrollChange(this, mScrollX, mScrollY, oldX, oldY);
                }
            }
        }

    }

    public JellyState getState() {
        return state;
    }

    private void setState(JellyState state) {
        if (this.state != state) {
            JellyState old = this.state;
            this.state = state;
            if (onJellyListener != null) {
                onJellyListener.onStateChange(state, old);
                if (state == JellyState.REFRESH) {
                    onJellyListener.onRefresh();
                }
                if (state == JellyState.LOAD_MORE) {
                    onJellyListener.onLoadMore();
                }
            }
        }
    }
}
