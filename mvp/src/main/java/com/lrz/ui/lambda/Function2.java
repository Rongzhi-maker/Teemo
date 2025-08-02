package com.lrz.ui.lambda;

import java.io.Serializable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/11
 * Description:
 */
public interface Function2<P,P1> extends Serializable {
    void call(P p,P1 p1);
}
