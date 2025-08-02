package com.lrz.ui.download;

import android.os.SystemClock;
import android.text.TextUtils;

import java.io.Serializable;

import okhttp3.Call;

/**
 * Author And Date: liurongzhi
 * Description: 下载任务
 */
public class DownloadTask implements ITask, Serializable {
    private Download download;
    private Call call;
    private boolean force = false;
    private int retryInt = 0;
    private long time = 60L; //每次刷新的时间间隔
    private long lastTime = SystemClock.uptimeMillis() - time;

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public boolean isForce() {
        return force;
    }

    public DownloadTask() {
    }

    public Download getDownload() {
        if (download == null) download = new Download();
        return download;
    }

    @Override
    public int getRetryNum() {
        return retryInt;
    }

    @Override
    public ITask setRetryNum(int num) {
        this.retryInt = num;
        return this;
    }

    @Override
    public ITask taskID(String id) {
        getDownload().setTaskID(id);
        return this;
    }

    @Override
    public ITask pause() {
        download.setState(DownloadStatus.PAUSE);
        if (call != null) call.cancel();
        return this;
    }

    @Override
    public ITask goOn() {
        DownloadManager.getInstance().download(this);
        return this;
    }

    @Override
    public ITask cancel() {
        download.setState(DownloadStatus.CANCEL);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        return this;
    }

    @Override
    public ITask timeUpdate(long time) {
        this.time = time;
        return this;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    @Override
    public ITask fileName(String fileName) {
        //下载过程中不可改变filename
        if (!TextUtils.isEmpty(fileName) && download.getState() < 0) {
            download.setFileName(fileName);
        }
        return this;
    }

    @Override
    public ITask enqueue() {
        DownloadManager.getInstance().download(this);
        return this;
    }

    @Override
    public ITask force(boolean force) {
        this.force = force;
        return this;
    }


    public boolean needUpdate() {
        boolean r = SystemClock.uptimeMillis() - lastTime > time;
        if (r) {
            lastTime = SystemClock.uptimeMillis();
        }
        return r;
    }
}
