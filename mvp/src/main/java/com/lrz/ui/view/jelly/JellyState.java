package com.lrz.ui.view.jelly;

/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.widget.jelly
 */
public enum JellyState {
    NORMAL(0),
    SCROLL(1),
    REFRESH(2),
    LOAD_MORE(3),
    RESET(4);
    final int value;

    JellyState(int value) {
        this.value = value;
    }
}
