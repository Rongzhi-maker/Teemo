package com.lrz.ui.view.jelly;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/4/28
 * Description: 缺省页面类型
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {JellyStyle.LINEAR, JellyStyle.FRAME})
public @interface JellyStyle {
    int LINEAR = 0;
    int FRAME = 1;
}
