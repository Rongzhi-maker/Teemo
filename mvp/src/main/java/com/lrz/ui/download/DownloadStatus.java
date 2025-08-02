package com.lrz.ui.download;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/10
 * Description: -1还未开始，0暂停，1正在下载，2完成,3取消
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {DownloadStatus.DEFAULT, DownloadStatus.PAUSE, DownloadStatus.DOWNLOADING, DownloadStatus.SUCCESS, DownloadStatus.CANCEL})
public @interface DownloadStatus {
    int DEFAULT = -1;
    int PAUSE = 0;
    int DOWNLOADING = 1;
    int SUCCESS = 2;
    int CANCEL = 3;
}
