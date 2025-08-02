package com.lrz.ui.download;

/**
 * Author And Date: liurongzhi on 2019/12/30.
 * Description: com.yilan.sdk.common.download
 */
public interface ITask {

    ITask fileName(String fileName);

    ITask enqueue();

    ITask force(boolean force);

    ITask pause();

    ITask goOn();

    ITask cancel();

    ITask timeUpdate(long time);

    Download getDownload();

    int getRetryNum();

    ITask setRetryNum(int num);

    ITask taskID(String id);
}
