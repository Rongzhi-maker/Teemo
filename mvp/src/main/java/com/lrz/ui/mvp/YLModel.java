package com.lrz.ui.mvp;

import android.content.Context;

import androidx.annotation.CallSuper;

import com.lrz.coroutine.flow.Observable;
import com.lrz.coroutine.flow.Task;
import com.lrz.coroutine.flow.net.ReqObservable;
import com.lrz.coroutine.flow.net.RequestBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Author And Date: liurongzhi on 2020/6/22.
 */
public class YLModel<P extends YLPresenter> {
    protected P presenter;
    protected volatile LinkedList<Closeable> closeables;

    void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @CallSuper
    public void onDestroy() {
        if (closeables != null) {
            synchronized (this) {
                if (closeables != null) {
                    LinkedList<Closeable> closeables = new LinkedList<>(this.closeables);
                    for (Closeable closeable : closeables) {
                        try {
                            closeable.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    this.closeables.clear();
                }
            }
        }
    }

    protected void addCloseable(Closeable closeable) {
        if (closeable == null) return;
        synchronized (this) {
            if (closeables == null) {
                closeables = new LinkedList<>();
            }
            closeables.add(closeable);
        }
    }

    /**
     * 移除 已经执行完毕的任务
     *
     * @param closeable 任务
     */
    protected void removeCloseable(Closeable closeable) {
        if (closeable == null || closeables == null) return;
        synchronized (this) {
            closeables.remove(closeable);
        }
    }


    public <T> Observable<T> create(Task<T> task) {
        return presenter.create(task);
    }

    public <B> ReqObservable<B> create(RequestBuilder<B> requestBuilder) {
        return presenter.create(requestBuilder);
    }

    public Context getContext() {
        return presenter.getContext();
    }
}
