package com.lrz.ui.mvp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lrz.ui.base.BaseActivity;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Author And Date: liurongzhi on 2020/2/16.
 * Description: com.yilan.sdk.common.ui.mvp
 */
public abstract class YLBaseActivity<P extends YLPresenter> extends BaseActivity implements YLBaseUI {
    protected P presenter;
    private boolean isResume = false;
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
        presenter.initIntentData();
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
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof YLBaseFragment && ((YLBaseFragment) fragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
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
}
