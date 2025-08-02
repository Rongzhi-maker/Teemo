package com.lrz.ui.lambda;

import java.io.Serializable;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/5/11
 * Description:
 */
public interface FunctionGet<R> extends Serializable {
    R get();
}
