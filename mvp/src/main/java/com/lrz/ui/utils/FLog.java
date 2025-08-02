package com.lrz.ui.utils;

import android.util.Log;

import com.lrz.coroutine.Dispatcher;
import com.lrz.coroutine.LLog;
import com.lrz.coroutine.handler.HandlerLRZThread;
import com.lrz.coroutine.handler.IHandlerThread;

import java.io.File;
import java.io.IOException;

/**
 * Author:  liurongzhi
 * CreateTime:  2023/7/5
 * Description: 文件日志输出工具
 */
public class FLog {
    public static void i(String tag, String log) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.INFO) {
            Log.i(tag, log);
            writeLog(tag, log);
        }
    }

    public static void i(String tag, String log, Throwable e) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.INFO) {
            Log.i(tag, log, e);
            writeLog(tag, log);
        }
    }

    public static void d(String tag, String log) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.DEBUG) {
            Log.d(tag, log);
            writeLog(tag, log);
        }
    }

    public static void d(String tag, String log, Throwable e) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.DEBUG) {
            Log.d(tag, log, e);
            writeLog(tag, log);
        }
    }

    public static void e(String tag, String log) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.ERROR) {
            Log.e(tag, log);
            writeLog(tag, log);
        }
    }

    public static void e(String tag, String log, Throwable e) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.ERROR) {
            Log.e(tag, log, e);
            writeLog(tag, log);
        }
    }

    public static void w(String tag, String log) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.WARN) {
            Log.w(tag, log);
            writeLog(tag, log);
        }
    }

    public static void w(String tag, String log, Throwable e) {
        if (tag == null || log == null)
            return;
        if (LLog.logLevel <= LLog.WARN) {
            Log.w(tag, log, e);
            writeLog(tag, log);
        }
    }

    static IHandlerThread handlerThread;

    public static synchronized IHandlerThread getHandlerThread() {
        if (handlerThread == null || !handlerThread.isRunning()) {
            handlerThread = new HandlerLRZThread("QC_FILE", false, Dispatcher.BACKGROUND, 1000 * 30);
            handlerThread.setOnHandlerThreadListener(new IHandlerThread.OnHandlerThreadListener() {
                @Override
                public boolean onIdle(IHandlerThread iHandlerThread) {
                    iHandlerThread.tryQuitOutTime();
                    return false;
                }

                @Override
                public void onDeath(IHandlerThread iHandlerThread) {

                }
            });
        }
        return handlerThread;
    }

    private static void writeLog(String tag, String log) {
        getHandlerThread().getThreadHandler().post(() -> {
            File file = new File(Util.getApp().getExternalCacheDir().getAbsoluteFile() + File.separator + tag + ".log");
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                Util.File.append(file, Util.Time.millis2String(System.currentTimeMillis()) + "\t");
                Util.File.append(file, log + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
