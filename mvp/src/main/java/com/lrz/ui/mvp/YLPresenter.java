package com.lrz.ui.mvp;

import androidx.annotation.CallSuper;

import com.lrz.coroutine.flow.Observable;
import com.lrz.coroutine.flow.Task;
import com.lrz.coroutine.handler.CoroutineLRZContext;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author And Date: liurongzhi on 2020/6/22.
 * Description: com.yilan.sdk.common.ui.mvp
 */
public abstract class YLPresenter<U extends YLBaseUI, M extends YLModel> {
    protected WeakReference<U> ui;
    protected M model;

    void init(U ui) {
        this.ui = new WeakReference<>(ui);
        try {
            Constructor<M> constructor = ((Class<M>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]).getDeclaredConstructor();
            constructor.setAccessible(true);
            model = constructor.newInstance();
            model.setPresenter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initIntentData() {

    }

    protected void initData() {
    }

    @CallSuper
    protected void onPause() {

    }

    @CallSuper
    protected void onResume() {
        if (runnables != null) {
            Iterator<Runnable> iterable = runnables.iterator();
            while (iterable.hasNext()) {
                Runnable runnable = iterable.next();
                runnable.run();
                iterable.remove();
            }
        }
    }

    protected void onDestroy() {
        if (model != null) {
            model.onDestroy();
        }
        if (runnables != null) {
            runnables.clear();
        }
    }


    public void doUITask(Runnable runnable) {
        if (ui != null && ui.get() != null) {
            runnable.run();
        }
    }

    private LinkedList<Runnable> runnables;

    //同一个任务在队列中只会被执行一次，执行完后可再次执行
    public void doUITaskOnShow(Runnable runnable) {
        if (ui.get() != null) {
            if (ui.get().isShow()) {
                runnable.run();
            } else {
                if (runnables == null) {
                    runnables = new LinkedList<>();
                }
                if (!runnables.contains(runnable)) {
                    runnables.add(runnable);
                }
            }
        }
    }

    public <T> Observable<T> create(Task<T> task) {
        if (ui.get() == null) return null;
        Observable<T> observable = CoroutineLRZContext.Create(task);
        model.addCloseable(observable);
        return observable;
    }
}
