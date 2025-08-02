package com.lrz.ui.download;

import java.io.Serializable;

/**
 * Author And Date: liurongzhi on 2019/12/30.
 * Description: com.yilan.sdk.common.download
 */
public class DownloadListener implements Serializable {
    public void onSuccess(ITask download) {
    }

    public void onError(ITask download, String message) {
    }

    public void onProgress(ITask download) {
    }


    public void onStart(ITask download) {
    }


    public void onPause(ITask download) {
    }

    public void onCancel(ITask download) {
    }

    private int intId;

    public DownloadListener(int intId) {
        this.intId = intId;
    }

    public DownloadListener() {

    }

    public int getIntId() {
        return intId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }
}
