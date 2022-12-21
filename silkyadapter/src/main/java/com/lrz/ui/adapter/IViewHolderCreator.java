package com.lrz.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Author And Date: liurongzhi on 2019/11/26.
 * Description: viewHolder创建器
 */
public interface IViewHolderCreator<D> {
    BaseViewHolder<D> createViewHolder(Context context, ViewGroup parent, int type);
}
