package com.lrz.ui.recycle;

/**
 * Author And Date: liurongzhi on 2020/8/20.
 * Description: com.yilan.sdk.common.ui.recycle
 */
public interface ViewAttachedToWindowListener<D> {
    void onViewAttachedToWindow(BaseViewHolder<D> holder);

    void onViewDetachedFromWindow(BaseViewHolder<D> holder);
}