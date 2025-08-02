package com.lrz.ui.lambda;

import java.io.Serializable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/11
 * Description:
 */
public interface FunctionGet1<R, P1> extends Serializable {
    R get(P1 p1);
}
