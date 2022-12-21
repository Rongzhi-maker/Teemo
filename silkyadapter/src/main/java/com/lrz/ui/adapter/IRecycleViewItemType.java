package com.lrz.ui.adapter;

/**
 * Author And Date: liurongzhi on 2019/11/27.
 * Description: 定义viewHolder类型
 */
public interface IRecycleViewItemType<D> {
    int getItemTypeForDataPosition(D data,int position);
}
