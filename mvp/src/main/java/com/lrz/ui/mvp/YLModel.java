package com.lrz.ui.mvp;

import androidx.annotation.CallSuper;

import com.lrz.coroutine.flow.Observable;
import com.lrz.coroutine.flow.Task;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Author And Date: liurongzhi on 2020/6/22.
 * Description: com.yilan.sdk.common.ui.mvp
 */
public class YLModel<P extends YLPresenter> {
    protected P presenter;
    private LinkedList<Closeable> closeables;

    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @CallSuper
    public void onDestroy() {
        synchronized (this) {
            if (closeables != null) {
                for (Closeable closeable : closeables) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void addCloseable(Closeable closeable) {
        synchronized (this) {
            if (closeable == null) {
                closeables = new LinkedList<>();
            }
            closeables.add(closeable);
        }
    }


    public <T> Observable<T> create(Task<T> task){
        return presenter.create(task);
    }
}
