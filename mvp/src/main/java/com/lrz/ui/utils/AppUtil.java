package com.lrz.ui.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * App相关信息
 * <p>
 * 1、包名
 * 2、版本
 * 3、应用名称
 * 4、主版本号
 * 5、副版本号
 * 6、小版本号
 */
public class AppUtil {
    // 应用版本号 4.1.7.01
    private static String versionName = null;
    // 应用版本号 41701
    private static int versionCode = 0;

    // App包名
    private static String packageName = null;

    // 应用名称
    private static String appName = null;

    // 是否已经获取版本号
    private static boolean hasSplitVersion;

    // 主版本号
    private static int appMajor = 0;

    // 副版本号
    private static int appMinor = 0;

    // 小版本号
    private static int appMicro = 0;

    /**
     * 获取应用版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    public static String getAppVersionName(Context context) {
        if (!TextUtils.isEmpty(versionName)) {
            return versionName;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (!TextUtils.isEmpty(packInfo.versionName)) {
                String[] ver = packInfo.versionName.split("\\.");
                if (ver.length > 3) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < ver.length - 1; i++) {
                        stringBuilder.append(ver[i]);
                        if (i != ver.length - 2) {
                            stringBuilder.append(".");
                        }
                    }
                    versionName = stringBuilder.toString();
                    return versionName;
                }
            }
            return packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    /**
     * 获取应用版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    public static int getAppVersionCode(Context context) {
        if (0 != versionCode) {
            return versionCode;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            versionCode = packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            versionCode = 100;
        }
        return versionCode;
    }

    /**
     * 获取APP包名
     *
     * @param context 上下文
     * @return 包名
     */
    public static String getPackageName(Context context) {
        if (null != packageName) {
            return packageName;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            packageName = packInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            packageName = "com.shuchen.qingcheng";
        }

        return packageName;
    }

    /**
     * 获取应用名称
     *
     * @param context 上下文
     * @return 应用名称
     */
    public static String getAppName(Context context) {
        if (null != appName) {
            return appName;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appName;
    }

    /**
     * 获取主版本号
     *
     * @param context 上下文
     * @return 主版本号
     */
    public static int getAppMajor(Context context) {
        splitAppVersion(getAppVersionName(context));
        return appMajor;
    }

    /**
     * 获取副版本号
     *
     * @param context 上下文
     * @return 副版本号
     */
    public static int getAppMinor(Context context) {
        splitAppVersion(getAppVersionName(context));
        return appMinor;
    }

    /**
     * 获取小版本号
     *
     * @param context 上下文
     * @return 小版本号
     */
    public static int getAppMicro(Context context) {
        splitAppVersion(getAppVersionName(context));
        return appMicro;
    }

    /**
     * 切割版本号
     *
     * @param versionName 版本号
     */
    private static void splitAppVersion(String versionName) {
        if (hasSplitVersion) {
            return;
        }

        String[] split = versionName.split("\\.");
        if (split.length >= 3) {
            appMajor = Integer.parseInt(split[0]);
            appMinor = Integer.parseInt(split[1]);
            appMicro = Integer.parseInt(split[2]);
        }
        hasSplitVersion = true;
    }


    /**
     * 获取安装时间
     */
    public static long getAppInstallTime(Context context) {
        long installTime = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            //应用装时间
            installTime = packageInfo.firstInstallTime;
            if (installTime < 0 || String.valueOf(installTime).length() > 13) {
                String path = packageInfo.applicationInfo.sourceDir;
                if (!TextUtils.isEmpty(path)) {
                    installTime = new java.io.File(path).lastModified();
                }
            }
            //应用最后一次更新时间
        } catch (Exception e) {
        }
        return installTime / 1000;
    }

    /**
     * 获取运营商
     */
    public static String getNetOperator() {
        try {
            TelephonyManager manager = (TelephonyManager) Util.getApp().getSystemService(Context.TELEPHONY_SERVICE);
            String iNumeric = manager.getSimOperator();
            String netOperator = "";
            if (iNumeric.length() > 0) {
                if ("46000".equals(iNumeric) || "46002".equals(iNumeric)) {
                    // 中国移动
                    netOperator = "移动";
                } else if ("46003".equals(iNumeric)) {
                    // 中国电信
                    netOperator = "电信";
                } else if ("46001".equals(iNumeric)) {
                    // 中国联通
                    netOperator = "联通";
                } else {
                    //未知
                    netOperator = "未知";
                }
            }
            return netOperator;
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 获取当前进程名
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info:  manager.getRunningAppProcesses()) {
            if (info.pid == pid) {
                return info.processName;

            }
        }
        return "";
    }
}
