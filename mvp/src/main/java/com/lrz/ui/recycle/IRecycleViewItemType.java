package com.lrz.ui.recycle;

/**
 * Author And Date: liurongzhi on 2019/11/27.
 * Description: com.yilan.sdk.common.ui.recycle
 */
public interface IRecycleViewItemType<D> {
    int getItemTypeForDataPosition(D data,int position);
}
