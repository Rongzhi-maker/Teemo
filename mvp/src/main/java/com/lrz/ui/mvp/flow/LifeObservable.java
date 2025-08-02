package com.lrz.ui.mvp.flow;

import com.lrz.coroutine.Dispatcher;
import com.lrz.coroutine.flow.IError;
import com.lrz.coroutine.flow.Observable;
import com.lrz.coroutine.flow.Observer;
import com.lrz.coroutine.flow.Task;
import com.lrz.coroutine.flow.net.ReqObservable;
import com.lrz.coroutine.flow.net.RequestBuilder;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/1/11
 * Description:
 */
public class LifeObservable<T> extends ReqObservable<T> {
    //监听事件流状态，及时关闭事件流
    protected OnComplete onComplete;
    protected IError<Throwable> iError;
    protected IError<Throwable> realError;

    {
        iError = throwable -> {
            IError<Throwable> realError = this.realError;
            if (realError != null) {
                realError.onError(throwable);
            } else {
                throw (RuntimeException) throwable;
            }
            onComplete();
        };
    }

    public LifeObservable(Task<T> task, OnComplete complete) {
        super(task);
        this.onComplete = complete;
    }

    public LifeObservable() {
        super();
    }


    @Override
    public synchronized LifeObservable<T> subscribe(Observer<T> result) {
        if (getTask() instanceof RequestBuilder) {
            return (LifeObservable<T>) super.subscribe(result);
        } else {
            return (LifeObservable<T>) super.subscribe(dispatcher, result);
        }
    }

    @Override
    public synchronized LifeObservable<T> execute() {
        return (LifeObservable<T>) super.execute();
    }

    @Override
    public synchronized LifeObservable<T> execute(Dispatcher dispatcher) {
        return (LifeObservable<T>) super.execute(dispatcher);
    }

    @Override
    public synchronized LifeObservable<T> executeTime(Dispatcher dispatcher, long interval) {
        return (LifeObservable<T>) super.executeTime(dispatcher, interval);
    }

    @Override
    public synchronized LifeObservable<T> executeDelay(Dispatcher dispatcher, long delay) {
        return (LifeObservable<T>) super.executeDelay(dispatcher, delay);
    }

    @Override
    protected void onSubscribe(T t) {
        super.onSubscribe(t);
        Observable<?> observable = nextObservable;
        //表示是最后一个
        if (observable == null) {
            onComplete();
        }
    }

    @Override
    protected void onError(Throwable e) {
        super.onError(e);
        onComplete();
    }

    private void onComplete() {
        LifeObservable<?> pre = LifeObservable.this;
        while (pre != null) {
            if (pre.onComplete != null) {
                // 如果是循环任务，则只有在手动调用了cancel之后，才算执行完毕
                if (getInterval() <= 0 || isCancel()) {
                    pre.onComplete.onComplete(pre);
                }
                return;
            }
            if (pre.preObservable instanceof LifeObservable) {
                pre = (LifeObservable<?>) pre.preObservable;
            } else {
                return;
            }
        }
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        onComplete();
        this.onComplete = null;
        this.iError = null;
        this.realError = null;
    }
}
