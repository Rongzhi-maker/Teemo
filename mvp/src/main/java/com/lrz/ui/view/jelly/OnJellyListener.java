package com.lrz.ui.view.jelly;

/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.inter
 */
public interface OnJellyListener {
    void onRefresh();
    void onLoadMore();
    void onStateChange(JellyState state,JellyState old);
}
