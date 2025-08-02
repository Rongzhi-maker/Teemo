package com.lrz.ui.base;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.lrz.ui.mvp.YLBaseFragment;

import java.util.List;

public class BaseXFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener {

    public static final String TAG = "YL_BaseFrag";
    protected boolean isResume = false;
    //当前Fragment可见性
    protected boolean isVisible = true;
    //父Fragment可见性，都可见为true，有一个不可见为false
    //可见性的标准：
    protected boolean isParentVisible = true;

    protected boolean isShow = false;

    protected boolean isParentVisible() {
        boolean parentVisible = true;
        Fragment parent = getParentFragment();
        while (parent != null && parentVisible) {
            parentVisible = parentVisible && parent.getUserVisibleHint() && parent.isVisible();
            parent = parent.getParentFragment();
        }
        return parentVisible;
    }

    protected boolean isCurrentVisible() {
        return getUserVisibleHint() && isVisible();
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isResume) {
            isResume = true;
            checkShow();
        }
    }

    @CallSuper
    protected void checkShow() {
        isVisible = isCurrentVisible();
        isParentVisible = isParentVisible();
        if (isShow ^ (isResume && isVisible && isParentVisible)) {
            isShow = isResume && isVisible && isParentVisible;
            onShow(isShow);
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            if (!fragments.isEmpty()) {
                for (int i = fragments.size() - 1; i >= 0; i--) {
                    Fragment fragment = fragments.get(i);
                    if (fragment instanceof BaseXFragment) {
                        ((BaseXFragment) fragment).checkShow();
                    }
                }
            }
        }
    }

    public boolean isShow() {
        return isResume && isVisible && isParentVisible;
    }

    @CallSuper
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        checkShow();
    }

    @CallSuper
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        checkShow();
    }



    /**
     * fragment是否展现，统一的入口，子类可重写此方法
     *
     * @param isShow
     */
    protected void onShow(boolean isShow) {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isResume) {
            isResume = false;
            checkShow();
        }
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        isResume = false;
        isVisible = false;
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        super.onDestroyView();
    }

    public boolean onBackPressed() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof YLBaseFragment && ((YLBaseFragment<?>) fragment).onBackPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof BaseXFragment && ((BaseXFragment) fragment).isShow() && ((BaseXFragment) fragment).onKeyDown(keyCode, event)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onGlobalLayout() {
        checkShow();
    }

    public void removeSelf() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(this).commitAllowingStateLoss();
    }

    public void removeSelf(@AnimatorRes @AnimRes int animation) {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .setCustomAnimations(0, animation)
                .remove(this).commitAllowingStateLoss();
    }
}
