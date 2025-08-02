package com.lrz.ui.lambda;

import java.io.Serializable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/11
 * Description:
 */
public interface FunctionGet2<R, P1, P2> extends Serializable {
    R get(P1 p1, P2 p2);
}
