package com.lrz.ui.adapter;

/**
 * Author And Date: liurongzhi on 2020/8/20.
 * Description: viewHolder 的attach监听
 */
public interface ViewAttachedToWindowListener<VH extends BaseViewHolder> {
    void onViewAttachedToWindow(VH holder);

    void onViewDetachedFromWindow(VH holder);
}