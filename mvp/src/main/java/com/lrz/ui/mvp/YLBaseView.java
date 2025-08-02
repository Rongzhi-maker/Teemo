package com.lrz.ui.mvp;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.ParameterizedType;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/4/4
 * Description:
 */
public abstract class YLBaseView<P extends YLPresenter> extends FrameLayout implements YLBaseUI {
    protected View viewRoot;
    protected P presenter;
    private Bundle bundle;
    //是否已经初始化了view
    protected boolean isInitView = false;

    public YLBaseView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public YLBaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YLBaseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInitView) {
            presenter.initIntentData(getArguments(), null);
            initView(viewRoot);
            presenter.initData();
            isInitView = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.onDestroy();
    }

    /**
     * 初始化mvpview管理类的各个层级对象
     *
     * @param context 上下文
     */
    private void init(Context context) {
        try {
            presenter = ((Class<P>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
            presenter.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        viewRoot = onCreateContentView(inflater);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setArguments(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getArguments() {
        return this.bundle;
    }

    /**
     * 获取根view
     *
     * @return viewRoot
     */
    public View getView() {
        return viewRoot;
    }

    @Override
    public boolean isShow() {
        return viewRoot.isAttachedToWindow() && viewRoot.isShown();
    }
}
