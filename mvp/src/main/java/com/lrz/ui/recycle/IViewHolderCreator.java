package com.lrz.ui.recycle;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Author And Date: liurongzhi on 2019/11/26.
 * Description: com.yilan.sdk.common.ui.recycle
 */
public interface IViewHolderCreator<D> {
    BaseViewHolder<D> createViewHolder(Context context, ViewGroup parent,int type);
}
