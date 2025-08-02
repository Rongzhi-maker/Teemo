package com.lrz.ui.mvp.flow;

import com.lrz.coroutine.flow.Observable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/1/28
 * Description:
 */
public interface OnComplete {
    void onComplete(Observable<?> ob);
}
