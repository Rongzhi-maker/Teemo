package com.lrz.ui.mvp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lrz.ui.base.BaseXFragment;

import java.lang.reflect.ParameterizedType;

/**
 * Author And Date: liurongzhi on 2020/6/22.
 * Description: com.yilan.sdk.common.ui.mvp
 */
public abstract class YLBaseFragment<P extends YLPresenter> extends BaseXFragment implements YLBaseUI {
    protected P presenter;

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            presenter = ((Class<P>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
            presenter.init(this);
        } catch (Exception e) {
//            YYLog.logE("YL_COMM_BASE_F", "presenter create error:" + this.getClass().getName());
            e.printStackTrace();
        }
        return onCreateContentView(inflater);
    }

    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.initIntentData();
        initView(view);
        presenter.initData();
    }


    @Override
    protected void checkShow() {
        super.checkShow();
        if (presenter != null) {
            if (isShow()) {
                presenter.onResume();
            } else {
                presenter.onPause();
            }
        }
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        if (presenter != null) {
            presenter.onDestroy();
        }
        super.onDestroyView();
    }
}
