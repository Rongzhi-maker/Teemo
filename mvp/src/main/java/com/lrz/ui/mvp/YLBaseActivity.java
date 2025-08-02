package com.lrz.ui.mvp;

import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;

import com.lrz.ui.base.BaseActivity;

import java.lang.reflect.ParameterizedType;

/**
 * Author And Date: liurongzhi on 2020/2/16.
 */
public abstract class YLBaseActivity<P extends YLPresenter> extends BaseActivity implements YLBaseUI {
    protected P presenter;
    private boolean isResume = false;
    public View viewRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            presenter = ((Class<P>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
            presenter.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        View viewRoot = onCreateContentView(LayoutInflater.from(this));
        setContentView(viewRoot);
        this.viewRoot = viewRoot;
        presenter.initIntentData(getIntent().getExtras(), savedInstanceState);
        initView(viewRoot);
        presenter.initData();
    }


    @Override
    protected void onPause() {
        super.onPause();
        isResume = false;
        presenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public boolean isShow() {
        return isResume;
    }

    @Override
    public Context getContext() {
        return this;
    }
}

