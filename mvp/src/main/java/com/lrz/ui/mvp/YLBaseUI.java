package com.lrz.ui.mvp;

import android.view.LayoutInflater;
import android.view.View;


/**
 * Author And Date: liurongzhi on 2020/6/22.
 * Description: com.yilan.sdk.common.ui.mvp
 */
public interface YLBaseUI {
    View onCreateContentView(LayoutInflater inflater);

    void initView(View viewRoot);

    boolean isShow();
}
