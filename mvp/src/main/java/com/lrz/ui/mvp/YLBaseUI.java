package com.lrz.ui.mvp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * Author And Date: liurongzhi on 2020/6/22.
 */
public interface YLBaseUI {
    View onCreateContentView(LayoutInflater inflater);

    void initView(View viewRoot);

    boolean isShow();

    @Nullable
    Context getContext();
}
