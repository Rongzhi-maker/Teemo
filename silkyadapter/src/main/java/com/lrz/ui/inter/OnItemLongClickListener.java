package com.lrz.ui.inter;

import android.view.View;

/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: 长按点击
 */
public  interface OnItemLongClickListener<D> {
    boolean onLongClick(View view, int position,D data);
}
