package com.lrz.ui.mvp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.lrz.coroutine.flow.Observable;
import com.lrz.coroutine.flow.Task;
import com.lrz.coroutine.flow.net.CommonRequest;
import com.lrz.coroutine.flow.net.RequestBuilder;
import com.lrz.ui.utils.Util;
import com.lrz.ui.mvp.flow.LifeObservable;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Author And Date: liurongzhi on 2020/6/22.
 */
public abstract class YLPresenter<U extends YLBaseUI, M extends YLModel> {
    protected U ui;
    protected M model;

    void init(U ui) {
        this.ui = ui;
        try {
            Constructor<M> constructor = ((Class<M>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]).getDeclaredConstructor();
            constructor.setAccessible(true);
            model = constructor.newInstance();
            model.setPresenter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initIntentData(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {

    }

    protected void initData() {
    }

    @CallSuper
    protected void onPause() {

    }

    @CallSuper
    protected void onResume() {
        if (burnables != null) {
            Iterator<Runnable> iterable = burnables.iterator();
            while (iterable.hasNext()) {
                Runnable runnable = iterable.next();
                runnable.run();
                iterable.remove();
            }
        }
    }

    @CallSuper
    protected void onDestroy() {
        if (model != null) {
            model.onDestroy();
        }
        if (burnables != null) {
            burnables.clear();
        }
        ui = null;
    }


    public void doUI(Runnable runnable) {
        if (ui != null) {
            runnable.run();
        }
    }


    private LinkedList<Runnable> burnables;

    /**
     * 同一个任务在队列中只会被执行一次，执行完后可再次执行
     *
     * @param runnable {@link UIRunnable}
     */
    public final <R extends Runnable> void doUIOnShow(R runnable) {
        if (ui != null) {
            if (ui.isShow()) {
                runnable.run();
            } else {
                if (burnables == null) {
                    burnables = new LinkedList<>();
                }
                if (!burnables.contains(runnable)) {
                    burnables.add(runnable);
                }
            }
        }
    }

    /**
     * 创建异步任务 且跟随生命周期来管理
     *
     * @param task 任务
     * @return Observable
     */
    public final <T> Observable<T> create(Task<T> task) {
        LifeObservable<T> observable = new LifeObservable<>(task, ob -> {
            if (model != null) {
                model.removeCloseable(ob);
            }
        });
        task.setObservable(observable);
        model.addCloseable(observable);
        return observable;
    }

    /**
     * 创建异步请求 且跟随生命周期来管理
     *
     * @param requestBuilder 网络请求任务
     * @return Observable
     */
    public final <B> LifeObservable<B> create(RequestBuilder<B> requestBuilder) {
        requestBuilder.setRequest(CommonRequest.request);
        LifeObservable<B> reqObservable;
        reqObservable = new LifeObservable<>(requestBuilder, ob -> {
            if (model != null) {
                model.removeCloseable(ob);
            }
        });
        requestBuilder.setObservable(reqObservable);
        model.addCloseable(reqObservable);
        return reqObservable;
    }

    /**
     * 获取上下文 当前页面如果已经销毁，则返回null
     *
     * @return context
     */
    public Context getContext() {
        if (ui == null) return Util.getApp();
        return ui.getContext();
    }
}
