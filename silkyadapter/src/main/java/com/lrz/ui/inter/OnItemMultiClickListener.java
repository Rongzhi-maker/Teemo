package com.lrz.ui.inter;

import android.view.View;

/**
 * Author And Date: liurongzhi on 2020/6/30.
 * Description: com.yilan.sdk.common.ui.inter
 */
public  interface OnItemMultiClickListener<D> {
    void onSingleClick(View view, int position, D data);
    void onDoubleClick(View view, int position, D data);
}
