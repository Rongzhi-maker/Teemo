package com.lrz.ui.view.jelly;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.lrz.ui.mvp.R;
import com.lrz.ui.utils.Util;
import com.lrz.ui.view.LoadingView;


/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.widget.jelly
 */
public class RefreshLayout extends JellyLayout {
    private OnRefreshListener listener;
    private LoadingView headProgressBar;
    private LoadingView footProgressBar;
    private ViewGroup rootHeader;
    private ViewGroup rootFooter;
    private View head, foot;

    public RefreshLayout(Context context) {
        super(context);
        initView();
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        setRefreshEnable(true);
        setLoadMoreEnable(true);
        headCreator(parent -> {
            head = LayoutInflater.from(parent.getContext()).inflate(R.layout.refresh_head_layout, parent, false);
            rootHeader = head.findViewById(R.id.root_layout);
            headProgressBar = head.findViewById(R.id.loading);
            headProgressBar.setAutoStart(false);
            return head;
        }).footCreator(parent -> {
            foot = LayoutInflater.from(parent.getContext()).inflate(R.layout.refresh_head_layout, parent, false);
            rootFooter = foot.findViewById(R.id.root_layout);
            rootFooter.setPadding(0, Util.Size.dp2px(10), 0, Util.Size.dp2px(55));
            footProgressBar = foot.findViewById(R.id.loading);
            footProgressBar.setAutoStart(false);
            return foot;
        }).setOnJellyListener(new OnJellyListener() {
            @Override
            public void onRefresh() {
                if (listener != null) {
                    listener.onRefresh();
                }
            }

            @Override
            public void onLoadMore() {
                if (listener != null) {
                    listener.onLoadMore();
                }
            }

            @Override
            public void onStateChange(final JellyState state, final JellyState old) {
                post(() -> setRefreshState(state, old));

            }
        });
        addOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY < 0) {
                headProgressBar.setPercent(1 - (-scrollY * 0.5f / head.getHeight() % 1));
                headProgressBar.setAlpha(-scrollY * 1f / head.getHeight() % 1);
            } else if (scrollY > 0) {
                footProgressBar.setPercent(1 - (scrollY * 0.5f / foot.getHeight() % 1));
                headProgressBar.setAlpha(scrollY * 1f / foot.getHeight() % 1);
            }
        });
    }


    public RefreshLayout setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
        return this;
    }

    private void setRefreshState(JellyState state, JellyState old) {
        if (state == JellyState.REFRESH) {
            headProgressBar.start();
        } else if (state == JellyState.LOAD_MORE) {
            footProgressBar.start();
        } else {
            footProgressBar.stop();
            headProgressBar.stop();
        }
    }

    @Override
    public JellyLayout setRefreshEnable(boolean refreshEnable) {
        if (headProgressBar != null) {
            if (!refreshEnable) {
                headProgressBar.setVisibility(INVISIBLE);
            } else {
                headProgressBar.setVisibility(VISIBLE);
            }
        }
        return super.setRefreshEnable(refreshEnable);
    }

    @Override
    public JellyLayout setLoadMoreEnable(boolean loadMoreEnable) {
        if (footProgressBar != null) {
            if (!loadMoreEnable) {
                footProgressBar.setVisibility(INVISIBLE);
            } else {
                footProgressBar.setVisibility(VISIBLE);
            }
        }
        return super.setLoadMoreEnable(loadMoreEnable);
    }

    public void setHeaderBackGround(@ColorRes int color) {
        if (rootHeader != null) {
            rootHeader.setBackgroundResource(color);
        }
    }


    public void setFooterBackGround(@ColorRes int color) {
        if (rootFooter != null) {
            rootFooter.setBackgroundResource(color);
        }
    }
}
