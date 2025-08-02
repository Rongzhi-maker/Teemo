package com.lrz.ui.download;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by lihu on 2018/9/3.
 */
public class Download implements Serializable {

    public Download() {
    }

    private String fileName = "";
    private int intID;
    private String taskID;
    private String requestUrl = "";//下载地址
    private int progress;//进度
    private volatile long currentFileSize;//已经下载的大小
    private volatile long totalFileSize;//总大小
    private String absolutePath = "";//保存路径
    private String tmpPath = "";//临时保存路径
    @DownloadStatus
    private volatile int state = DownloadStatus.DEFAULT;//-1还未开始，0，暂停，1，正在下载，2，完成,3取消
    private String realUrl = "";//重定向后的真实url

    public String getRealUrl() {
        return realUrl;
    }

    public void setRealUrl(String realUrl) {
        this.realUrl = realUrl;
    }

    public String getTaskID() {
        if (TextUtils.isEmpty(taskID)) {
            if (TextUtils.isEmpty(requestUrl)) return "";
            taskID = Integer.toHexString(requestUrl.hashCode());
        }
        return taskID;
    }

    void setTaskID(String taskID){
        this.taskID = taskID;
    }

    public int getIntID() {
        if (intID == 0) {
            if (TextUtils.isEmpty(requestUrl)) return 0;
            intID = requestUrl.hashCode();
        }
        return requestUrl.hashCode();
    }

    public int getState() {
        return state;
    }

    public void setState(@DownloadStatus int state) {
        this.state = state;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCurrentFileSize() {
        return currentFileSize;
    }

    public void setCurrentFileSize(long currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public long getTotalFileSize() {
        if (totalFileSize < 1) totalFileSize = 1;
        return totalFileSize;
    }

    public void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getTmpPath() {
        return tmpPath;
    }

    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

    @Override
    public String toString() {
        return "Download{" +
                ", requestUrl='" + requestUrl + '\'' +
                ", progress=" + progress +
                ", currentFileSize=" + currentFileSize +
                ", totalFileSize=" + totalFileSize +
                ", absolutePath='" + absolutePath + '\'' +
                ", tmpPath='" + tmpPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Download)) return false;
        return ((Download) o).getTaskID().equals(getTaskID());
    }
}
