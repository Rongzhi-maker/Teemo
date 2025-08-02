package com.lrz.ui.recycle.inter;

import android.view.View;

/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.inter
 */
public  interface OnItemClickListener<D> {
    void onClick(View view, int position,D data);
}
