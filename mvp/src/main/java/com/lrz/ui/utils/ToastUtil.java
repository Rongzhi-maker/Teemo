package com.lrz.ui.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lrz.ui.mvp.R;

import java.lang.reflect.Field;


/**
 * 自定义Toast
 */
public class ToastUtil {
    /**
     * 显示Toast
     *
     * @param resourcesId 资源Id
     */
    public static void show(int resourcesId) {
        Context context = Util.getApp();
        show(context.getResources().getString(resourcesId));
    }

    public static void show(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void show(String message, int duration) {
        showToast(message, duration);
    }

    /**
     * 显示Toast
     *
     * @param message 文本
     */
    private static void showToast(String message, int duration) {
        Context context = Util.getApp();
        try {
            @SuppressLint("InflateParams")
            View toastView = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            TextView text = toastView.findViewById(R.id.module_view_toast_message);
            text.setText(message);
            Toast toast = new Toast(context);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                hookToast(toast);
            }
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(duration);
            toast.setView(toastView);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookToast(Toast toast) {
        try {
            // 获取 Toast.mTN 字段对象
            Field mTNField = Toast.class.getDeclaredField("mTN");
            mTNField.setAccessible(true);
            Object mTN = mTNField.get(toast);

            // 获取 mTN 中的 mHandler 字段对象
            Field mHandlerField = mTNField.getType().getDeclaredField("mHandler");
            mHandlerField.setAccessible(true);
            Handler mHandler = (Handler) mHandlerField.get(mTN);

            // 如果这个对象已经被反射替换过了
            if (mHandler instanceof SafeHandler) {
                return;
            }

            mHandlerField.set(mTN, new SafeHandler(mHandler));

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static class SafeHandler extends Handler {
        private final Handler mHandler;

        private SafeHandler(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // 捕获这个异常，避免程序崩溃
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException exception) {
                exception.printStackTrace();
            }
        }
    }
}