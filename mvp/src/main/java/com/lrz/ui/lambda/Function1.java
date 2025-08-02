package com.lrz.ui.lambda;

import java.io.Serializable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/11
 * Description:
 */
public interface Function1<P> extends Serializable {
    void call(P p);
}
