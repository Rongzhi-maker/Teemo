package com.lrz.ui;

import com.lrz.coroutine.LLog;
import com.lrz.ui.mvp.YLPresenter;

public class MainPresenter extends YLPresenter<MainActivity,MainModel> {
    @Override
    protected void initData() {
        super.initData();
        LLog.e("MainPresenter","initData");
    }
}
