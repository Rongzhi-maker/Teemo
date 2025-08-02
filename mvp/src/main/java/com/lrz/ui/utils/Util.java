package com.lrz.ui.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresPermission;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.GsonBuilder;
import com.lrz.coroutine.BuildConfig;
import com.lrz.coroutine.LLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Util {
    private static Application application;

    /**
     * 获取 Application
     *
     * @return return Application
     */
    public static Application getApp() {
        if (application == null) {
            try {
                @SuppressLint("PrivateApi") Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Object thread = getActivityThread();
                Object app = activityThreadClass.getMethod("getApplication").invoke(thread);
                application = (Application) app;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return application;
    }

    private static Object getActivityThread() {
        Object activityThread = getActivityThreadInActivityThreadStaticField();
        if (activityThread != null) return activityThread;
        activityThread = getActivityThreadInActivityThreadStaticMethod();
        if (activityThread != null) return activityThread;
        return getActivityThreadInLoadedApkField();
    }


    private static Object getActivityThreadInLoadedApkField() {
        try {
            @SuppressLint("DiscouragedPrivateApi") Field mLoadedApkField = Application.class.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mLoadedApk = mLoadedApkField.get(Util.getApp());
            if (mLoadedApk == null) return null;
            Field mActivityThreadField = mLoadedApk.getClass().getDeclaredField("mActivityThread");
            mActivityThreadField.setAccessible(true);
            return mActivityThreadField.get(mLoadedApk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static Object getActivityThreadInActivityThreadStaticMethod() {
        try {
            @SuppressLint("PrivateApi") Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            return activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getActivityThreadInActivityThreadStaticField() {
        try {
            @SuppressLint("PrivateApi") Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            @SuppressLint("DiscouragedPrivateApi") Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            return sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Code {
        public static ArrayList<Field> getAllFields(Class<?> clazz) {
            // 取所有字段（包括基类的字段）
            Field[] fields = clazz.getDeclaredFields();
            ArrayList<Field> allFields = new ArrayList<>(Arrays.asList(fields));
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null) {
                Field[] superFields = superClass.getDeclaredFields();
                allFields.addAll(Arrays.asList(superFields));
                superClass = superClass.getSuperclass();
                if (superClass == Object.class) break;
            }

            return allFields;
        }
    }

    /**
     * ui相关工具类
     */
    public static class UI {
        public static Activity getActivityByContext(Context context) {
            if (context == null)
                return null;
            else if (context instanceof Activity)
                return (Activity) context;
            else if (context instanceof ContextWrapper)
                return getActivityByContext(((ContextWrapper) context).getBaseContext());

            return null;
        }

        public static Activity getActivityByView(View view) {
            if (view == null) return null;
            Context context = view.getContext();
            if (context == null)
                return null;
            else if (context instanceof Activity)
                return (Activity) context;
            else if (context instanceof ContextWrapper)
                return getActivityByContext(((ContextWrapper) context).getBaseContext());

            return null;
        }
    }


    /**
     * 网络相关工具类
     */
    public static class Network {
        public enum NetworkType {
            NETWORK_ETHERNET,
            NETWORK_WIFI,
            NETWORK_5G,
            NETWORK_4G,
            NETWORK_3G,
            NETWORK_2G,
            NETWORK_UNKNOWN,
            NETWORK_NO
        }

        /**
         * 1-移动，2-联通,3-电信
         */
        public interface Operator {
            int UNKNOWN = -1;
            int CHINA_MOBILE = 1;
            int CHINA_UNICOM = 2;
            int CHINA_TELECOM = 3;
        }

        /**
         * 是否有网络链接
         *
         * @return If there is no network, return false，otherwise return true;
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        public static boolean isConnected() {
            NetworkInfo info = getActiveNetworkInfo();
            return info != null && info.isConnected();
        }

        /**
         * 获取当前网速
         *
         * @return 字节
         */
        public static long getNowRxBytes() {
            Context context = getApp();
            if (context == null) return 0;
            return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes());
        }

        /**
         * 获取当前网络是否是mobile
         *
         * @return if network type is mobile return ture，otherwise return false
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        public static boolean isMobile() {
            NetworkInfo info = getActiveNetworkInfo();
            return null != info
                    && info.isAvailable()
                    && info.getType() == ConnectivityManager.TYPE_MOBILE;
        }

        /**
         * 获取手机运营商名称
         *
         * @return 手机运营商名称
         */
        public static String getNetworkOperatorName() {
            TelephonyManager tm =
                    (TelephonyManager) Util.getApp().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) return "";
            return tm.getNetworkOperatorName();
        }


        private static int operator;

        /**
         * 获取运营商类型
         *
         * @return Operator
         */
        public static int getOperation(Context context) {
            if (operator != -1) return operator;
            if (context == null) return Operator.UNKNOWN;
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telManager.getSimOperator();
            if (operator != null) {
                if (operator.equals("46000")
                        || operator.equals("46002")
                        || operator.equals("46004")
                        || operator.equals("46007")
                        || operator.equals("46008")) {
                    //中国移动
                    Network.operator = Operator.CHINA_MOBILE;
                    return Network.operator;
                }
                if (operator.equals("46001")
                        || operator.endsWith("46006")
                        || operator.endsWith("46009")) {
                    //中国联通
                    Network.operator = Operator.CHINA_UNICOM;
                    return Network.operator;
                }
                if (operator.equals("46003")
                        || operator.endsWith("46005")
                        || operator.endsWith("46011")) {
                    //中国电信
                    Network.operator = Operator.CHINA_TELECOM;
                    return Network.operator;
                }
            }
            return Operator.UNKNOWN;
        }

        /**
         * 当前网络是否是wifi
         *
         * @return if network type is wifi return true，otherwise return false
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        public static boolean isWifiConnected() {
            NetworkInfo ni = getActiveNetworkInfo();
            return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
        }


        /**
         * 获取网络链接类型
         *
         * @return return the type of network
         * @see NetworkType
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        private static NetworkType getNetworkType() {
            if (isEthernet()) {
                return NetworkType.NETWORK_ETHERNET;
            }
            NetworkInfo info = getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    return NetworkType.NETWORK_WIFI;
                } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    switch (info.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GSM:
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NetworkType.NETWORK_2G;

                        case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NetworkType.NETWORK_3G;

                        case TelephonyManager.NETWORK_TYPE_IWLAN:
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NetworkType.NETWORK_4G;

                        case TelephonyManager.NETWORK_TYPE_NR:
                            return NetworkType.NETWORK_5G;
                        default:
                            String subtypeName = info.getSubtypeName();
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                    || subtypeName.equalsIgnoreCase("WCDMA")
                                    || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                return NetworkType.NETWORK_3G;
                            } else {
                                return NetworkType.NETWORK_UNKNOWN;
                            }
                    }
                } else {
                    return NetworkType.NETWORK_UNKNOWN;
                }
            }
            return NetworkType.NETWORK_NO;
        }

        /**
         * 是否是以太网
         *
         * @return
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        private static boolean isEthernet() {
            final ConnectivityManager cm =
                    (ConnectivityManager) Util.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            final NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (info == null) return false;
            NetworkInfo.State state = info.getState();
            if (null == state) return false;
            return state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING;
        }

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        private static NetworkInfo getActiveNetworkInfo() {
            ConnectivityManager cm =
                    (ConnectivityManager) Util.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return null;
            return cm.getActiveNetworkInfo();
        }

        /**
         * 获取网络类型
         *
         */
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        public static String getNetType() {
            NetworkType networkType = Network.getNetworkType();
            switch (networkType) {
                case NETWORK_2G:
                    return "2g";
                case NETWORK_3G:
                    return "3g";
                case NETWORK_4G:
                    return "4g";
                case NETWORK_WIFI:
                    return "wifi";
            }
            return "未知";
        }
    }

    /**
     * 时间相关工具类
     */
    public static class Time {

        /**
         * 传入日期字符串例如2023-10-13，计算出距离当前时间还有多少天
         *
         * @param inputDateStr 目标日期
         * @return 距离目标日期还有多少天
         */
        public static int calculateDaysUntilDate(String inputDateStr) {
            // 创建日期格式化器
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                // 将输入的日期字符串解析为 Date 对象
                Date inputDate = dateFormat.parse(inputDateStr);

                // 获取当前日期
                Date currentDate = new Date();

                // 计算两个日期之间的毫秒差
                long timeDiff = inputDate.getTime() - currentDate.getTime();

                // 将毫秒差转换为天数差
                int daysDiff = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);

                return daysDiff + 1;
            } catch (ParseException e) {
                e.printStackTrace();
                return -1; // 解析日期字符串出错时返回-1表示错误
            }
        }

        /**
         * 将毫秒转换成分秒
         *
         * @param mils 毫秒
         * @return 00：00
         */
        public static String convertTime(int mils) {
            @SuppressLint("DefaultLocale") String hms = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(mils),
                    TimeUnit.MILLISECONDS.toSeconds(mils) % TimeUnit.MINUTES.toSeconds(1));
            return hms;
        }

        /**
         * 将秒值转换成 00:00:00
         *
         * @param duration 时间秒
         * @return 00:00:00
         */
        public static String formatDuration(long duration) {
            long secondVal = duration % 60;
            long minute = duration / 60;

            String ssecond;
            if (secondVal < 10) {
                ssecond = "0" + secondVal;
            } else {
                ssecond = "" + secondVal;
            }

            if (minute <= 0) {
                if (secondVal < 10) {
                    return "00:" + "0" + secondVal;
                } else {
                    return "00:" + secondVal;
                }
            }
            long minuteVal = minute % 60;
            String minuteS;
            if (minute < 10) {
                minuteS = "0" + minuteVal;
            } else {
                minuteS = String.valueOf(minuteVal);
            }
            long hour = minute / 60;
            //不到一小时
            if (hour <= 0) {
                return minuteS + ":" + ssecond;
            } else {
                return hour + ":" + minuteS + ":" + ssecond;
            }
        }

        /**
         * 将毫秒值转换成 yyyy-MM-dd HH:mm:ss
         *
         * @param millis 毫秒值
         * @return String
         */
        public static String millis2String(long millis) {
            return millis2String(millis, getSafeDateFormat("yyyy-MM-dd HH:mm:ss"));
        }

        /**
         * 按照pattern将毫秒值转换成字符串
         *
         * @param millis  毫秒值
         * @param pattern 字符串格式 例：yyyy-MM-dd HH:mm:ss
         * @return if pattern ==null or pattern == "", return yyyy-MM-dd HH:mm:ss
         */
        public static String millis2String(long millis, String pattern) {
            return millis2String(millis, getSafeDateFormat(pattern));
        }

        /**
         * 按照DateFormat将毫秒值转换成字符串
         *
         * @param millis 毫秒值
         * @param format DateFormat
         * @return if format is null, return string format by yyyy-MM-dd HH:mm:ss
         */
        public static String millis2String(long millis, DateFormat format) {
            if (format == null) {
                format = getSafeDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            return format.format(new Date(millis));
        }

        /**
         * 将 yyyy-MM-dd HH:mm:ss 日期字符串转换成毫秒值
         *
         * @param time 毫秒值
         * @return if time is null or empty,return 0;
         */
        public static long string2Millis(final String time) {
            if (TextUtils.isEmpty(time)) {
                return 0;
            }
            return string2Millis(time, getSafeDateFormat("yyyy-MM-dd HH:mm:ss"));
        }

        /**
         * 将 yyyy-MM-dd HH:mm:ss 日期字符串转换成毫秒值
         *
         * @param time 毫秒值
         * @return if time is null or empty,return 0;
         */
        public static long string2Millis(final String time, String patten) {
            if (TextUtils.isEmpty(time)) {
                return 0;
            }
            return string2Millis(time, getSafeDateFormat(patten));
        }

        /**
         * 将给定格式的日期字符串转换成毫秒值
         *
         * @param time   日期字符串
         * @param format DateFormat
         * @return if time is null or empty or format is null,return 0;
         */
        public static long string2Millis(final String time, DateFormat format) {
            if (TextUtils.isEmpty(time) || format == null) {
                return 0;
            }
            try {
                return format.parse(time).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return -1;
        }

        /**
         * 获取指定类型的SimpleDateFormat
         *
         * @param pattern 例：yyyy-MM-dd HH:mm:ss
         * @return SimpleDateFormat
         */
        @SuppressLint("SimpleDateFormat")
        public static SimpleDateFormat getSafeDateFormat(String pattern) {
            if (TextUtils.isEmpty(pattern)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
            Map<String, SimpleDateFormat> sdfMap = SDF_THREAD_LOCAL.get();
            SimpleDateFormat simpleDateFormat = sdfMap.get(pattern);
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat(pattern);
                sdfMap.put(pattern, simpleDateFormat);
            }
            return simpleDateFormat;
        }

        /**
         * 将时间从 yyyy-MM-dd HH:mm:ss 格式转换为 yyyy-MM-dd 格式
         */
        @SuppressLint("SimpleDateFormat")
        public static String getVipTime(String vipEndTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date endTime = null;
            try {
                endTime = sdf.parse(vipEndTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (endTime != null) {
                return new SimpleDateFormat("yyyy-MM-dd").format(endTime.getTime());
            } else {
                return "";
            }
        }

        /**
         * 当前日期 yyyy-MM-dd格式
         */
        @SuppressLint("SimpleDateFormat")
        public static String getCurDate(String format) {
            String curTime;
            SimpleDateFormat formatter;
            Date currentDate;
            formatter = new SimpleDateFormat(format);
            currentDate = Calendar.getInstance().getTime();
            curTime = formatter.format(currentDate);
            return curTime;
        }

        /**
         * ThreadLocal缓存常用的SimpleDateFormat对象
         */
        private static final ThreadLocal<Map<String, SimpleDateFormat>> SDF_THREAD_LOCAL
                = new ThreadLocal<Map<String, SimpleDateFormat>>() {
            @Override
            protected Map<String, SimpleDateFormat> initialValue() {
                return new HashMap<>();
            }
        };

        /**
         * @param timestamp 当前时间戳
         * @return 返回 yyyy/MM/dd HH:mm 时间格式
         */
        public static String formatTimestamp(long timestamp) {
            // 创建SimpleDateFormat对象，指定输出的时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

            // 使用Date对象格式化时间戳
            Date date = new Date(timestamp);

            // 返回格式化后的时间字符串
            return sdf.format(date);
        }
    }

    /**
     * 设备参数相关工具类
     */
    public static class Device {

        private static RomInfo bean = null;
        // rom 字段类型
        private static final String[] ROM_HUAWEI = {"huawei"};
        private static final String[] ROM_VIVO = {"vivo"};
        private static final String[] ROM_XIAOMI = {"xiaomi"};
        private static final String[] ROM_OPPO = {"oppo"};
        private static final String[] ROM_LEECO = {"leeco", "letv"};
        private static final String[] ROM_360 = {"360", "qiku"};
        private static final String[] ROM_ZTE = {"zte"};
        private static final String[] ROM_ONEPLUS = {"oneplus"};
        private static final String[] ROM_NUBIA = {"nubia"};
        private static final String[] ROM_COOLPAD = {"coolpad", "yulong"};
        private static final String[] ROM_LG = {"lg", "lge"};
        private static final String[] ROM_GOOGLE = {"google"};
        private static final String[] ROM_SAMSUNG = {"samsung"};
        private static final String[] ROM_MEIZU = {"meizu"};
        private static final String[] ROM_LENOVO = {"lenovo"};
        private static final String[] ROM_SMARTISAN = {"smartisan"};
        private static final String[] ROM_HTC = {"htc"};
        private static final String[] ROM_SONY = {"sony"};
        private static final String[] ROM_GIONEE = {"gionee", "amigo"};
        private static final String[] ROM_MOTOROLA = {"motorola"};
        // 不同系统的property 对应的字段
        public static final String VERSION_PROPERTY_HUAWEI = "ro.build.version.emui";
        private static final String VERSION_PROPERTY_VIVO = "ro.vivo.os.build.display.id";
        private static final String VERSION_PROPERTY_XIAOMI = "ro.build.version.incremental";
        private static final String VERSION_PROPERTY_OPPO = "ro.build.version.opporom";
        private static final String VERSION_PROPERTY_LEECO = "ro.letv.release.version";
        private static final String VERSION_PROPERTY_360 = "ro.build.uiversion";
        private static final String VERSION_PROPERTY_ZTE = "ro.build.MiFavor_version";
        private static final String VERSION_PROPERTY_ONEPLUS = "ro.rom.version";
        private static final String VERSION_PROPERTY_NUBIA = "ro.build.rom.id";

        private final static String UNKNOWN = "unknown";

        /**
         * 获取rom 信息
         *
         * @return
         */
        public static RomInfo getRomInfo() {
            if (bean != null) return bean;
            bean = new RomInfo();
            final String brand = getBrand();
            final String manufacturer = getManufacturer();
            if (isRightRom(brand, manufacturer, ROM_HUAWEI)) {
                bean.name = ROM_HUAWEI[0];
                String version = getRomVersion(VERSION_PROPERTY_HUAWEI);
                String[] temp = version.split("_");
                if (temp.length > 1) {
                    bean.version = temp[1];
                } else {
                    bean.version = version;
                }
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_VIVO)) {
                bean.name = ROM_VIVO[0];
                bean.version = getRomVersion(VERSION_PROPERTY_VIVO);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_XIAOMI)) {
                bean.name = ROM_XIAOMI[0];
                bean.version = getRomVersion(VERSION_PROPERTY_XIAOMI);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_OPPO)) {
                bean.name = ROM_OPPO[0];
                bean.version = getRomVersion(VERSION_PROPERTY_OPPO);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_LEECO)) {
                bean.name = ROM_LEECO[0];
                bean.version = getRomVersion(VERSION_PROPERTY_LEECO);
                return bean;
            }

            if (isRightRom(brand, manufacturer, ROM_360)) {
                bean.name = ROM_360[0];
                bean.version = getRomVersion(VERSION_PROPERTY_360);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_ZTE)) {
                bean.name = ROM_ZTE[0];
                bean.version = getRomVersion(VERSION_PROPERTY_ZTE);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_ONEPLUS)) {
                bean.name = ROM_ONEPLUS[0];
                bean.version = getRomVersion(VERSION_PROPERTY_ONEPLUS);
                return bean;
            }
            if (isRightRom(brand, manufacturer, ROM_NUBIA)) {
                bean.name = ROM_NUBIA[0];
                bean.version = getRomVersion(VERSION_PROPERTY_NUBIA);
                return bean;
            }

            if (isRightRom(brand, manufacturer, ROM_COOLPAD)) {
                bean.name = ROM_COOLPAD[0];
            } else if (isRightRom(brand, manufacturer, ROM_LG)) {
                bean.name = ROM_LG[0];
            } else if (isRightRom(brand, manufacturer, ROM_GOOGLE)) {
                bean.name = ROM_GOOGLE[0];
            } else if (isRightRom(brand, manufacturer, ROM_SAMSUNG)) {
                bean.name = ROM_SAMSUNG[0];
            } else if (isRightRom(brand, manufacturer, ROM_MEIZU)) {
                bean.name = ROM_MEIZU[0];
            } else if (isRightRom(brand, manufacturer, ROM_LENOVO)) {
                bean.name = ROM_LENOVO[0];
            } else if (isRightRom(brand, manufacturer, ROM_SMARTISAN)) {
                bean.name = ROM_SMARTISAN[0];
            } else if (isRightRom(brand, manufacturer, ROM_HTC)) {
                bean.name = ROM_HTC[0];
            } else if (isRightRom(brand, manufacturer, ROM_SONY)) {
                bean.name = ROM_SONY[0];
            } else if (isRightRom(brand, manufacturer, ROM_GIONEE)) {
                bean.name = ROM_GIONEE[0];
            } else if (isRightRom(brand, manufacturer, ROM_MOTOROLA)) {
                bean.name = ROM_MOTOROLA[0];
            } else {
                bean.name = manufacturer;
            }
            bean.version = getRomVersion("");
            return bean;
        }

        public static String getRomVersion(final String propertyName) {
            String ret = "";
            if (!TextUtils.isEmpty(propertyName)) {
                ret = getSystemProperty(propertyName);
            }
            if (TextUtils.isEmpty(ret) || ret.equals(UNKNOWN)) {
                try {
                    String display = Build.DISPLAY;
                    if (!TextUtils.isEmpty(display)) {
                        ret = display.toLowerCase();
                    }
                } catch (Throwable ignore) {/**/}
            }
            if (TextUtils.isEmpty(ret)) {
                return UNKNOWN;
            }
            return ret;
        }

        private static String getSystemProperty(final String name) {
            String prop = getSystemPropertyByShell(name);
            if (!TextUtils.isEmpty(prop)) return prop;
            prop = getSystemPropertyByStream(name);
            if (!TextUtils.isEmpty(prop)) return prop;
            if (Build.VERSION.SDK_INT < 28) {
                return getSystemPropertyByReflect(name);
            }
            return prop;
        }

        private static String getSystemPropertyByShell(final String propName) {
            String line;
            BufferedReader input = null;
            try {
                Process p = Runtime.getRuntime().exec("getprop " + propName);
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                String ret = input.readLine();
                if (ret != null) {
                    return ret;
                }
            } catch (IOException ignore) {
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ignore) {/**/}
                }
            }
            return "";
        }

        private static String getSystemPropertyByStream(final String key) {
            try {
                Properties prop = new Properties();
                FileInputStream is = new FileInputStream(
                        new java.io.File(Environment.getRootDirectory(), "build.prop")
                );
                prop.load(is);
                return prop.getProperty(key, "");
            } catch (Exception ignore) {/**/}
            return "";
        }

        private static boolean isRightRom(final String brand, final String manufacturer, final String... names) {
            for (String name : names) {
                if (brand.contains(name) || manufacturer.contains(name)) {
                    return true;
                }
            }
            return false;
        }

        private static String getBrand() {
            try {
                String brand = Build.BRAND;
                if (!TextUtils.isEmpty(brand)) {
                    return brand.toLowerCase();
                }
            } catch (Throwable ignore) {/**/}
            return UNKNOWN;
        }

        /**
         * 获取手机亮度
         *
         * @param context 上下文
         * @return 亮度 0-255
         */
        public static int getSystemBrightness(Context context) {
            int systemBrightness = 0;
            try {
                systemBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return systemBrightness;
        }

        /**
         * 获取设备meid
         *
         * @return string
         */
        public static String getMEID() {
            return getImeiOrMeid(false);
        }

        @SuppressLint("MissingPermission")
        public static String getIMSI() {
            try {
                TelephonyManager tm = (TelephonyManager) getApp().getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    String imsi = tm.getSubscriberId();
                    if (imsi == null) {
                        imsi = "";
                    }
                    return imsi;
                }
            } catch (SecurityException var1) {
                var1.printStackTrace();
            }
            return "";
        }

        /**
         * 获取设备imei
         *
         * @return
         */
        public static String getIMEI() {
            return getImeiOrMeid(true);
        }

        /**
         * 获取 设备Serial
         *
         * @return
         */
        @SuppressLint("MissingPermission")
        public static String getSerial() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    return Build.getSerial();
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return "";
                }
            }
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Build.getSerial() : Build.SERIAL;
        }

        /**
         * 获取Android
         *
         * @return string
         */
        @SuppressLint("HardwareIds")
        public static String getAndroidID() {
            String id = Settings.Secure.getString(
                    Util.getApp().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            if ("9774d56d682e549c".equals(id)) return "";
            return id == null ? "" : id;
        }

        static String udid;
        static String bootId;

        public static String getBootId() {
            if (bootId != null) return bootId;
            char[] chars = new char[37];
            try {
                FileReader reader = new FileReader("/proc/sys/kernel/random/boot_id");
                reader.read(chars);
            } catch (Exception e) {
                e.printStackTrace();
                chars[0] = '\0';
            }
            bootId = new String(chars);
            return bootId.replace("\n", "");
        }


        private static String getUdid(String prefix, String id) {
            if (id.equals("")) {
                return prefix + UUID.randomUUID().toString().replace("-", "");
            }
            return prefix + UUID.nameUUIDFromBytes(id.getBytes()).toString().replace("-", "");
        }


        /**
         * 获取 系统版本的 version name
         *
         * @return string
         */
        public static String getVersionName() {
            return Build.VERSION.RELEASE;
        }

        /**
         * 获取 系统版本的 version code
         *
         * @return int
         */
        public static int getVersionCode() {
            return Build.VERSION.SDK_INT;
        }


        /**
         * 获取设备型号
         *
         * @return
         */
        public static String getModel() {
            String model = Build.MODEL;
            if (model != null) {
                model = model.trim().replaceAll("\\s*", "");
            } else {
                model = "";
            }
            return model;
        }

        /**
         * 获取设备制造商
         *
         * @return
         */
        public static String getManufacturer() {
            String manufacturer = Build.MANUFACTURER;
            if (Build.MANUFACTURER == null) {
                manufacturer = "";
            }
            return manufacturer;
        }


        /**
         * 是否是新荣耀设备 (新荣耀设备应该需要调用不同的sdk获取oaid)
         *
         * @return 是true 不是false
         */
        public static boolean isNewHonor() {
            if (isHonorManufacturer() && !isContainsEmuiOrMagic()) {
                // 带荣耀标识，同时不是老荣耀设备。23年11月底产品 (不带HMS预装)
                return true;
            }

            // 非新荣耀设备 (也可能不是荣耀)
            return false;
        }

        /**
         * 手机是否包含荣耀标识
         *
         * @return 是true 不是false
         */
        private static boolean isHonorManufacturer() {
            return Build.MANUFACTURER.equalsIgnoreCase("HONOR");
        }

        /**
         * 是否包含emui或者Magic
         *
         * @return 是true 不是false
         */
        private static boolean isContainsEmuiOrMagic() {
            try {
                @SuppressLint("PrivateApi")
                Class<?> programClass = Class.forName("android.os.SystemProperties");
                Method method = programClass.getMethod("get", String.class);
                String emuiVersion = (String) method.invoke(null, "ro.build.version.emui");
                if (null != emuiVersion && !TextUtils.isEmpty(emuiVersion)) {
                    // 确认有emui版本
                    if (emuiVersion.contains("MagicUI") || emuiVersion.contains("MagicOS")) {
                        // 包含MagicUI或者MagicOS的设备认为是旧荣耀设备
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }


        /**
         * 判断设备是否是平板
         *
         * @return boolean, if the device is tablet return true,otherwise false;
         */
        public static boolean isTablet() {
            return (Resources.getSystem().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }

        static int phoneType = -1;

        public static boolean isPhone() {
            if (phoneType >= 0) return phoneType != 0;
            if (getApp() != null) {
                TelephonyManager tm = (TelephonyManager) getApp().getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    phoneType = tm.getPhoneType();
                }
            }
            return phoneType != 0;
        }

        /**
         * 获取手机熄屏时间
         *
         * @return int, if error return -1;
         */
        public static int getSleepDuration() {
            try {
                return Settings.System.getInt(
                        Util.getApp().getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT
                );
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @SuppressLint("MissingPermission")
        private static String getImeiOrMeid(boolean isImei) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return "";
            }
            TelephonyManager tm = (TelephonyManager) getApp().getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isImei) {
                    return getMinOne(tm.getImei(0), tm.getImei(1));
                } else {
                    return getMinOne(tm.getMeid(0), tm.getMeid(1));
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String ids = getSystemPropertyByReflect(isImei ? "ril.gsm.imei" : "ril.cdma.meid");
                if (!TextUtils.isEmpty(ids)) {
                    String[] idArr = ids.split(",");
                    if (idArr.length == 2) {
                        return getMinOne(idArr[0], idArr[1]);
                    } else {
                        return idArr[0];
                    }
                }

                String id0 = tm.getDeviceId();
                String id1 = "";
                try {
                    Method method = tm.getClass().getMethod("getDeviceId", int.class);
                    id1 = (String) method.invoke(tm,
                            isImei ? TelephonyManager.PHONE_TYPE_GSM
                                    : TelephonyManager.PHONE_TYPE_CDMA);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (isImei) {
                    if (id0 != null && id0.length() < 15) {
                        id0 = "";
                    }
                    if (id1 != null && id1.length() < 15) {
                        id1 = "";
                    }
                } else {
                    if (id0 != null && id0.length() == 14) {
                        id0 = "";
                    }
                    if (id1 != null && id1.length() == 14) {
                        id1 = "";
                    }
                }
                return getMinOne(id0, id1);
            } else {
                String deviceId = tm.getDeviceId();
                if (isImei) {
                    if (deviceId != null && deviceId.length() >= 15) {
                        return deviceId;
                    }
                } else {
                    if (deviceId != null && deviceId.length() == 14) {
                        return deviceId;
                    }
                }
            }
            return "";
        }

        private static String getSystemPropertyByReflect(String key) {
            try {
                @SuppressLint("PrivateApi")
                Class<?> clz = Class.forName("android.os.SystemProperties");
                Method getMethod = clz.getMethod("get", String.class, String.class);
                return (String) getMethod.invoke(clz, key, "");
            } catch (Exception e) {/**/}
            return "";
        }

        private static String getMinOne(String s0, String s1) {
            boolean empty0 = TextUtils.isEmpty(s0);
            boolean empty1 = TextUtils.isEmpty(s1);
            if (empty0 && empty1) return "";
            if (!empty0 && !empty1) {
                if (s0.compareTo(s1) <= 0) {
                    return s0;
                } else {
                    return s1;
                }
            }
            if (!empty0) return s0;
            return s1;
        }

        /**
         * 判断设备是否是华为
         *
         * @return
         */
        public static boolean isHuaWei() {
            String phoneBrand = getPhoneBrand();
            return phoneBrand.contains("HUAWEI") || phoneBrand.contains("OCE")
                    || phoneBrand.toLowerCase().contains("huawei") || phoneBrand.toLowerCase().contains("honor");
        }

        /**
         * 是否小米机型
         *
         * @return
         */
        public static boolean isXiaoMiDevice() {
            String tmpBrand = TextUtils.isEmpty(Build.BRAND) ? Build.MANUFACTURER : Build.BRAND;
            String brand = tmpBrand == null ? "" : tmpBrand.toLowerCase();
            return "xiaomi".equals(brand) || "redmi".equals(brand) || "blackshark".equals(brand);
        }

        /**
         * 获取设备厂商
         *
         * @return 厂商名称
         */
        public static String getPhoneBrand() {
            String manufacturer = Build.MANUFACTURER;
            if (manufacturer != null && manufacturer.length() > 0) {
                return manufacturer.toLowerCase();
            } else {
                return "unknown";
            }
        }

        public static class RomInfo {
            private String name;
            private String version;

            public String getName() {
                return name;
            }

            public String getVersion() {
                return version;
            }

            @Override
            public String toString() {
                return "RomInfo{name=" + name +
                        ", version=" + version + "}";
            }
        }
    }

    /**
     * ui,应用 等相关工具类
     */
    public static class App {

        /**
         * 获取系统local，进而获取到国家语言等
         *
         * @return
         */

        public static Locale getCurrentLocale() {
            return getApp().getResources().getConfiguration().locale;
        }

        /**
         * 设置 status bar 为 LIGHT模式
         *
         * @param activity
         * @param isLightMode 是否是 light 模式
         */
        public static void setStatusBarLightMode(final Activity activity,
                                                 final boolean isLightMode) {
            setStatusBarLightMode(activity.getWindow(), isLightMode);
        }

        /**
         * 获取status bar 高度
         *
         * @return int
         */
        public static int getStatusBarHeight() {
            Resources resources = Resources.getSystem();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            return resources.getDimensionPixelSize(resourceId);
        }

        /**
         * 获取status 是否显示
         *
         * @param activity 当前activity
         * @return boolean
         */
        public static boolean isStatusBarVisible(final Activity activity) {
            int flags = activity.getWindow().getAttributes().flags;
            return (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
        }

        /**
         * 根据包名启动app
         *
         * @param packageName
         */
        public static void launchApp(final String packageName) {
            if (TextUtils.isEmpty(packageName)) return;
            Intent intent;
            String launcherActivity = getLauncherActivity(packageName);
            if (TextUtils.isEmpty(launcherActivity)) return;
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(packageName, launcherActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Util.getApp().startActivity(intent);
        }

        /**
         * 获取某pkg 的启动页面
         *
         * @param pkg
         * @return
         */
        public static String getLauncherActivity(final String pkg) {
            if (TextUtils.isEmpty(pkg)) return "";
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(pkg);
            PackageManager pm = Util.getApp().getPackageManager();
            List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
            if (info == null || info.size() == 0) {
                return "";
            }
            return info.get(0).activityInfo.name;
        }

        public static void setStatusBarLightMode(final Window window,
                                                 final boolean isLightMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int vis = decorView.getSystemUiVisibility();
                if (isLightMode) {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }

        /**
         * 是否是青橙剧场app
         */
        public static boolean isQCApp() {
            return Util.getApp().getPackageName().equals("com.shuchen.qingcheng");
        }

        public static boolean isWBApp() {
            return Util.getApp().getPackageName().equals("com.sgswh.wbmovie");
        }
    }

    /**
     * 字符串转换工具类
     */
    public static class Str {
        /**
         * 从Resources中获取字符串并格式化输出
         *
         * @param id         资源id
         * @param formatArgs 替换字符串中占位符的参数
         * @return string 结果
         */
        public static String getString(@StringRes int id, Object... formatArgs) {
            try {
                return Util.getApp().getString(id, formatArgs);
            } catch (Resources.NotFoundException ignore) {
                return "";
            }
        }

        /**
         * 将字节数转换成mb或者kb
         *
         * @param b 字节俗
         * @return 如果b>1024*1024,则输出mb，否则输出kb
         */
        public static String byte2String(int b) {
            StringBuilder sb = new StringBuilder();
            if (b > 1024 * 1024) {
                sb.append(b * 100 / 1024 / 1024 / 100f);
                sb.append("M");
            } else {
                sb.append(b * 100 / 1024 / 100f);
                sb.append("K");
            }
            return sb.toString();
        }


        /**
         * 将二进制数组 转换成十六进制字符串
         *
         * @param bytes 二进制数组
         * @return 字符串
         */
        public static String bytes2HexString(final byte[] bytes) {
            return bytes2HexString(bytes, true);
        }

        private static final char[] HEX_DIGITS_UPPER =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        private static final char[] HEX_DIGITS_LOWER =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        /**
         * 将二进制数组 转换成十六进制字符串
         *
         * @param bytes       二进制数组
         * @param isUpperCase 是否转换成大写字母
         * @return 字符串
         */
        public static String bytes2HexString(final byte[] bytes, boolean isUpperCase) {
            if (bytes == null) return "";
            char[] hexDigits = isUpperCase ? HEX_DIGITS_UPPER : HEX_DIGITS_LOWER;
            int len = bytes.length;
            if (len <= 0) return "";
            char[] ret = new char[len << 1];
            for (int i = 0, j = 0; i < len; i++) {
                ret[j++] = hexDigits[bytes[i] >> 4 & 0x0f];
                ret[j++] = hexDigits[bytes[i] & 0x0f];
            }
            return new String(ret);
        }

        /**
         * 格式化字符串
         *
         * @param format 格式
         * @param args   字符串
         * @return 格式化后内容
         */
        public static String format(String format, Object... args) {
            return new Formatter(Locale.getDefault()).format(format, args).toString();
        }

        /**
         * 转换钱数量
         *
         * @param cost
         * @return
         */
        public static String formatMoney(int cost) {
            if (cost % 100 == 0) {
                return String.valueOf(cost / 100);
            } else if (cost % 10 == 0) {
                return new DecimalFormat("0.0").format(cost / 100f);
            } else {
                return new DecimalFormat("0.00").format(cost / 100f);
            }
        }

        /**
         * 纯数字转化加单位（w）
         */
        public static String formatFollowNum(int num) {
            String result;
            if (num >= 100000) {
                //10万
                result = num / 10000 + "w";
            } else if (num >= 10000) {
                DecimalFormat df = new DecimalFormat("#.0");
                result = df.format(num / 10000f) + "w";
            } else {
                result = String.valueOf(num);
            }
            return result;
        }

        /**
         * 数字转换
         */
        public static String formatFollowStr(int num) {
            String result;
            if (num >= 100000) {
                //10万
                result = num / 10000 + "万";
            } else if (num >= 10000) {
                DecimalFormat df = new DecimalFormat("#.0");
                result = df.format(num / 10000f) + "万";
            } else if (num >= 1000) {
                DecimalFormat df = new DecimalFormat("#.0");
                result = df.format(num / 1000f) + "千";
            } else {
                result = String.valueOf(num);
            }
            return result;
        }
    }

    /**
     * 尺寸转换工具类
     */
    public static class Size {
        /**
         * dp转px
         *
         * @param dpValue
         * @return
         */
        public static int dp2px(final float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        public static int getDPI() {
            return Resources.getSystem().getDisplayMetrics().densityDpi;
        }

        public static float getDensity() {
            return Resources.getSystem().getDisplayMetrics().density;
        }

        /**
         * px转dp
         *
         * @param pxValue
         * @return
         */
        public static int px2dp(final float pxValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }

        /**
         * 获得设备屏幕宽度
         */
        static int screenWidth = 0;

        public static int getScreenWidth() {
            if (screenWidth != 0) return screenWidth;
            DisplayMetrics dm = getDisplayMetrics();
            return (screenWidth = dm.widthPixels);
        }

        /**
         * 获得设备屏幕高度
         * According to phone resolution height
         */
        static int screenHeight = 0;

        public static int getScreenHeight() {
            if (screenHeight != 0) return screenHeight;
            DisplayMetrics dm = getDisplayMetrics();
            return (screenHeight = dm.heightPixels);
        }

        public static DisplayMetrics getDisplayMetrics() {
            try {
                return Resources.getSystem().getDisplayMetrics();
            } catch (NullPointerException e) {
                return new NullDisplayMetrics();
            }

        }

        /**
         * @ClassName: NullDisplayMetrics
         * @Description: 防止获取DisplayMetrics对象失败而导致的nullpointer异常
         */
        static class NullDisplayMetrics extends DisplayMetrics {
            public NullDisplayMetrics() {
                widthPixels = 0;
                heightPixels = 0;
                density = 0.0f;
                densityDpi = DisplayMetrics.DENSITY_LOW;
                scaledDensity = 0.0f;
                xdpi = 0.0f;
                ydpi = 0.0f;
            }
        }
    }

    /**
     * gson序列化工具类
     */
    public static class Gson {
        private static final com.google.gson.Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        /**
         * 将json字符串序列化成对象
         *
         * @param json 待转换Json
         * @param type 转换对象Class
         * @param <T>  转换对象类
         * @return 转换成的对象, 如果解析失败，返回null，要判断空
         */
        public static <T> T fromJson(final String json, final Class<T> type) {
            return fromJson(json, type, null);
        }

        /**
         * 将json字符串序列化成对象
         *
         * @param json         待转换Json
         * @param type         转换对象Class
         * @param <T>          转换对象类
         * @param defaultValue 默认值
         * @return 转换成的对象, 如果解析失败返回默认值
         */
        public static <T> T fromJson(final String json, final Class<T> type, T defaultValue) {
            try {
                if (TextUtils.isEmpty(json)) {
                    return defaultValue;
                }
                return gson.fromJson(json, type);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    LLog.e("Util.Gson", e.getMessage(), e);
                }
                return defaultValue;
            }
        }

        /**
         * 将json字符串序列化成对象
         *
         * @param json         待转换Json
         * @param typeOfT      转换对象类型
         * @param <T>          转换对象类
         * @param defaultValue 默认值
         * @return 转换成的对象, 如果解析失败返回默认值
         */
        public static <T> T fromJson(final String json, final Type typeOfT, T defaultValue) {
            try {
                return gson.fromJson(json, typeOfT);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    LLog.e("Util.Gson", e.getMessage(), e);
                }
                return defaultValue;
            }
        }

        /**
         * 将json字符串序列化成对象
         *
         * @param json    待转换Json
         * @param typeOfT 转换对象类型
         * @param <T>     转换对象类
         * @return 转换成的对象, 如果解析失败，返回null，要判断空
         */

        public static <T> T fromJson(final String json, final Type typeOfT) {
            return fromJson(json, typeOfT, null);
        }

        /**
         * 将对象转换成json
         *
         * @param object 对象
         * @return
         */
        public static String toJson(final Object object) {
            return gson.toJson(object);
        }

        public static String toJson(final Object object, Type type) {
            return gson.toJson(object, type);
        }


        private static final byte TYPE_BOOLEAN = 0x00;
        private static final byte TYPE_INT = 0x01;
        private static final byte TYPE_LONG = 0x02;
        private static final byte TYPE_DOUBLE = 0x03;
        private static final byte TYPE_STRING = 0x04;
        private static final byte TYPE_JSON_OBJECT = 0x05;
        private static final byte TYPE_JSON_ARRAY = 0x06;

        /**
         * 获取jsonObject 中的对应key 的jsonObject
         *
         * @param jsonObject   主体jb
         * @param key          键
         * @param defaultValue 默认值
         * @return JSONObject
         */
        public static JSONObject getJSONObject(final JSONObject jsonObject,
                                               final String key,
                                               final JSONObject defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_JSON_OBJECT);
        }

        /**
         * 从字符串json 中获取对应key 的object
         *
         * @param json         json串
         * @param key          键
         * @param defaultValue 默认值
         * @return JSONObject
         */
        public static JSONObject getJSONObject(final String json,
                                               final String key,
                                               final JSONObject defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_JSON_OBJECT);
        }

        /**
         * 获取jsonObject 中的对应key 的jsonArray
         *
         * @param jsonObject   主体jb
         * @param key          键
         * @param defaultValue 默认值
         * @return JSONArray
         */
        public static JSONArray getJSONArray(final JSONObject jsonObject,
                                             final String key,
                                             final JSONArray defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_JSON_ARRAY);
        }

        /**
         * json 中的对应key 的jsonArray
         *
         * @param json         json串
         * @param key          键
         * @param defaultValue 默认值
         * @return JSONArray
         */
        public static JSONArray getJSONArray(final String json,
                                             final String key,
                                             final JSONArray defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_JSON_ARRAY);
        }

        public static boolean getBoolean(final JSONObject jsonObject,
                                         final String key) {
            return getBoolean(jsonObject, key, false);
        }

        public static boolean getBoolean(final JSONObject jsonObject,
                                         final String key,
                                         final boolean defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_BOOLEAN);
        }

        public static boolean getBoolean(final String json,
                                         final String key) {
            return getBoolean(json, key, false);
        }

        public static boolean getBoolean(final String json,
                                         final String key,
                                         final boolean defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_BOOLEAN);
        }

        public static int getInt(final JSONObject jsonObject,
                                 final String key) {
            return getInt(jsonObject, key, -1);
        }

        public static int getInt(final JSONObject jsonObject,
                                 final String key,
                                 final int defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_INT);
        }

        public static int getInt(final String json,
                                 final String key) {
            return getInt(json, key, -1);
        }

        public static int getInt(final String json,
                                 final String key,
                                 final int defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_INT);
        }

        public static long getLong(final JSONObject jsonObject,
                                   final String key) {
            return getLong(jsonObject, key, -1);
        }

        public static long getLong(final JSONObject jsonObject,
                                   final String key,
                                   final long defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_LONG);
        }

        public static long getLong(final String json,
                                   final String key) {
            return getLong(json, key, -1);
        }

        public static long getLong(final String json,
                                   final String key,
                                   final long defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_LONG);
        }

        public static double getDouble(final JSONObject jsonObject,
                                       final String key) {
            return getDouble(jsonObject, key, -1);
        }

        public static double getDouble(final JSONObject jsonObject,
                                       final String key,
                                       final double defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_DOUBLE);
        }

        public static double getDouble(final String json,
                                       final String key) {
            return getDouble(json, key, -1);
        }

        public static double getDouble(final String json,
                                       final String key,
                                       final double defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_DOUBLE);
        }

        public static String getString(final JSONObject jsonObject,
                                       final String key) {
            return getString(jsonObject, key, "");
        }

        public static String getString(final JSONObject jsonObject,
                                       final String key,
                                       final String defaultValue) {
            return getValueByType(jsonObject, key, defaultValue, TYPE_STRING);
        }

        public static String getString(final String json,
                                       final String key) {
            return getString(json, key, "");
        }

        public static String getString(final String json,
                                       final String key,
                                       final String defaultValue) {
            return getValueByType(json, key, defaultValue, TYPE_STRING);
        }

        private static <T> T getValueByType(final String json,
                                            final String key,
                                            final T defaultValue,
                                            final byte type) {
            if (json == null || json.length() == 0
                    || key == null || key.length() == 0) {
                return defaultValue;
            }
            try {
                return getValueByType(new JSONObject(json), key, defaultValue, type);
            } catch (JSONException e) {
                LLog.e("Util.Gson", "getValueByType: ", e);
                return defaultValue;
            }
        }

        private static <T> T getValueByType(final JSONObject jsonObject,
                                            final String key,
                                            final T defaultValue,
                                            final byte type) {
            if (jsonObject == null || key == null || key.length() == 0) {
                return defaultValue;
            }
            try {
                Object ret;
                if (type == TYPE_BOOLEAN) {
                    ret = jsonObject.getBoolean(key);
                } else if (type == TYPE_INT) {
                    ret = jsonObject.getInt(key);
                } else if (type == TYPE_LONG) {
                    ret = jsonObject.getLong(key);
                } else if (type == TYPE_DOUBLE) {
                    ret = jsonObject.getDouble(key);
                } else if (type == TYPE_STRING) {
                    ret = jsonObject.getString(key);
                } else if (type == TYPE_JSON_OBJECT) {
                    ret = jsonObject.getJSONObject(key);
                } else if (type == TYPE_JSON_ARRAY) {
                    ret = jsonObject.getJSONArray(key);
                } else {
                    return defaultValue;
                }
                return (T) ret;
            } catch (JSONException e) {
                LLog.e("Util.Gson", "getValueByType: ", e);
                return defaultValue;
            }
        }
    }

    /**
     * 文件操作工具类
     */

    public static class File {

        /**
         * 读取文件
         *
         * @param fpath 文件路径
         * @return string
         * @throws Exception
         */
        public static String read(String fpath) throws Exception {
            return read(fpath, Charset.defaultCharset().name());
        }

        /**
         * 读取文件
         *
         * @param fpath   文件路径
         * @param charset 字符集 例如:UTF-16
         * @return string
         * @throws Exception
         */
        public static String read(String fpath, String charset) throws Exception {
            return read(new java.io.File(fpath), charset);
        }

        /**
         * 读取文件
         *
         * @param file 文件对象
         * @return string
         * @throws Exception
         */
        public static String read(java.io.File file) throws Exception {
            return read(file, Charset.defaultCharset().name());
        }

        /**
         * 读取文件
         *
         * @param file    文件对象
         * @param charset 字符集
         * @return string
         * @throws Exception
         */
        public static String read(java.io.File file, String charset) throws Exception {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4 * 1024];
                int rdsz = fis.read(buffer);
                while (rdsz != -1) {
                    baos.write(buffer, 0, rdsz);
                    rdsz = fis.read(buffer);
                }
                fis.close();
                return new String(baos.toByteArray(), charset);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static String read(InputStream stream) {
            InputStreamReader inputReader = null;
            BufferedReader bufReader = null;
            try {
                inputReader = new InputStreamReader(stream);
                bufReader = new BufferedReader(inputReader);
                String line = "";
                StringBuffer result = new StringBuffer();
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                    result.append("\n");
                }
                return result.toString();
            } catch (Exception e) {
            } finally {
                try {
                    bufReader.close();
                } catch (IOException e) {
                }
                try {
                    inputReader.close();
                } catch (IOException e) {
                }
            }
            return "";
        }

        /**
         * 写文件
         *
         * @param fpath   文件路径
         * @param content 写入内容
         */
        public static void write(String fpath, String content) {
            write(fpath, content, Charset.defaultCharset().name());
        }

        /**
         * 写入文件
         *
         * @param fpath   文件路径
         * @param content 写入内容
         * @param charset 字符集
         */
        public static void write(String fpath, String content, String charset) {
            write(new java.io.File(fpath), content, charset);
        }

        /**
         * 写入文件
         *
         * @param file    文件对象
         * @param content 写入内容
         */
        public static void write(java.io.File file, String content) {
            write(file, content, Charset.defaultCharset().name());
        }

        /**
         * 写入文件
         *
         * @param file    文件对象
         * @param content 写入内容
         * @param charset 字符集
         */
        public static void write(java.io.File file, String content, String charset) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(content.getBytes(charset));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static void append(java.io.File file, String content) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, true);
                fos.write(content.getBytes(Charset.defaultCharset().name()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 重命名文件
         *
         * @param from 原名
         * @param to   新名称
         */
        public static void rename(String from, String to) {
            try {
                java.io.File fFrom = new java.io.File(from);
                java.io.File fTo = new java.io.File(to);
                if (fFrom.exists() && !fTo.exists()) {
                    fFrom.renameTo(fTo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /**
         * 压缩文件
         *
         * @param from 源文件
         * @param to   产出文件
         * @return 压缩失败 return false
         */
        public static boolean zip(java.io.File from, java.io.File to) {
            FileInputStream fis = null;
            ZipOutputStream zos = null;
            try {
                fis = new FileInputStream(from);
                zos = new ZipOutputStream(new FileOutputStream(to));
                zos.putNextEntry(new ZipEntry(from.getName()));

                byte[] buffer = new byte[16 * 1024];
                int rdsz = fis.read(buffer);
                while (rdsz != -1) {
                    zos.write(buffer, 0, rdsz);
                    rdsz = fis.read(buffer);
                }
                zos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (zos != null) {
                        zos.close();
                    }
                } catch (Exception e) {
                }
            }
            return false;
        }


        /**
         * 解压文件
         *
         * @param zipFile 源文件
         * @param desDir  输出路径
         * @throws Exception
         */
        public static void unzip(java.io.File zipFile, java.io.File desDir) throws Exception {
            // 读入流
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            // 遍历每一个文件
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null && !zipEntry.getName().contains("../")) {
                if (zipEntry.isDirectory()) { // 文件夹
                    String unzipFilePath = desDir.getAbsolutePath() + java.io.File.separator + zipEntry.getName();
                    // 直接创建
                    new java.io.File(unzipFilePath).mkdirs();
                } else { // 文件
                    String unzipFilePath = desDir.getAbsolutePath() + java.io.File.separator + zipEntry.getName();
                    // 写出文件流
                    BufferedOutputStream bufferedOutputStream =
                            new BufferedOutputStream(new FileOutputStream(unzipFilePath));
                    byte[] bytes = new byte[1024];
                    int readLen;
                    while ((readLen = zipInputStream.read(bytes)) != -1) {
                        bufferedOutputStream.write(bytes, 0, readLen);
                    }
                    bufferedOutputStream.close();
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        }

        /**
         * 根据路径获取File
         *
         * @param context  上下文
         * @param fileName 文件名称
         * @return 文件File
         */
        public static java.io.File getFile(Context context, String fileName) {
            java.io.File file = null;
            try {
                file = context.getExternalFilesDir(fileName.substring(0, fileName.lastIndexOf("/")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (file == null) {
                return null;
            }

            return new java.io.File(file.getPath() + "/" + fileName.substring(fileName.lastIndexOf("/") + 1));
        }

        /**
         * 修改文件名称，文件名称: bilog_update.txt
         *
         * @param normalFile 正常Bi文件
         * @param updateFile 需要修改到的Bi文件
         */
        public static void renameFile(java.io.File normalFile, java.io.File updateFile) {
            try {
                normalFile.renameTo(updateFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 按行读文件,返回集合
         *
         * @param file 需要读的文件
         * @return 文件内容
         */
        public static List<String> readFile(java.io.File file) {
            List<String> content = new ArrayList<>();
            BufferedReader buffReader = null;
            try {
                InputStream inStream = new FileInputStream(file);
                InputStreamReader inputReader = new InputStreamReader(inStream);
                buffReader = new BufferedReader(inputReader);
                String line;
                while ((line = buffReader.readLine()) != null) {
                    content.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    buffReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return content;
        }

        /**
         * 获取文件夹大小
         *
         * @param file 文件夹
         * @return
         */
        public static double getDirSize(java.io.File file) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    java.io.File[] children = file.listFiles();
                    if (children == null || children.length == 0) return 0.0;
                    double size = 0;
                    for (java.io.File f : children)
                        size += getDirSize(f);
                    return size;
                }
                double size = (double) file.length() / 1024 / 1024;
                return size;
            }
            return 0.0;
        }

        /**
         * 删除文件
         *
         * @param file
         */
        public static void deleteFile(java.io.File file) {
            try {
                if (file != null && file.exists()) {
                    if (file.isDirectory()) {
                        java.io.File[] files = file.listFiles();
                        for (java.io.File f : files) {
                            deleteFile(f);
                        }
                    }
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 拷贝文件
         *
         * @param src 源文件
         * @param des 输出文件
         */
        public static void copyFile(java.io.File src, java.io.File des) {
            if (src == null || !src.exists()) {
                return;
            }
            if (des.exists()) {
                des.delete();
            }
            try {
                des.createNewFile();

                FileChannel input = null;
                FileChannel output = null;

                try {
                    input = new FileInputStream(src).getChannel();
                    output = new FileOutputStream(des).getChannel();
                    output.transferFrom(input, 0, input.size());
                } catch (Exception e) {
                } finally {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 拷贝文件夹
         *
         * @param src
         * @param des
         */
        public static void copyDir(java.io.File src, java.io.File des) {
            if (src == null || !src.exists() || !src.isDirectory()) {
                return;
            }

            if (des.exists()) {
                deleteFile(des);
            }
            try {
                des.mkdir();

                java.io.File[] files = src.listFiles();
                for (java.io.File f : files) {
                    if (f.isDirectory()) {
                        copyDir(f, new java.io.File(des, f.getName()));
                    } else {
                        copyFile(f, new java.io.File(des, f.getName()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 软件盘相关
     */
    public static class Keyboard {
        /**
         * 隐藏输入法
         *
         * @param view 输入editText
         */
        public static void hideKeyBoard(View view) {
            InputMethodManager manager =
                    (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        /**
         * 显示输入法
         *
         * @param view 输入editText
         */
        public static void showKeyBoard(View view) {
            InputMethodManager manager =
                    (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            view.requestFocus();
            manager.showSoftInput(view, 0);
        }
    }

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.applicationInfo.loadLabel(packageManager).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final HashMap<String, Boolean> installCache = new HashMap<>();

    /**
     * 判断app是否安装
     *
     * @param packageName 包名
     * @return true：已经安装 false：未安装
     * done 已优化实现方式（liurongzhi）
     */
    public static boolean isAppInstalled(String packageName) {
        if (installCache.containsKey(packageName) && installCache.get(packageName) != null) {
            return Boolean.TRUE.equals(installCache.get(packageName));
        }
        try {
            getApp().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            installCache.put(packageName, false);
            return false;
        }
        installCache.put(packageName, true);
        return true;
    }


    /**
     * 获取随机数（10000000 ~ 99999999）
     *
     * @param min 最小区间
     * @param max 最大区间
     * @return 随机数
     */
    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    /**
     * 权限工具类
     */
    public static class Permission {
        /**
         * 判断通知是否开启（非单个消息渠道）
         *
         * @param context 上下文
         * @return true 开启
         * API19 以上可用
         */
        public static boolean checkNotificationsEnabled(Context context) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (null == notificationManagerCompat) {
                return false;
            }
            notificationManagerCompat.cancel(1);
            return notificationManagerCompat.areNotificationsEnabled();
        }
    }

    /**
     * money相关工具类
     */
    public static class Money {
        /**
         * 将分转化为元，保留2位小数，最多4位小数
         *
         * @param cents 需要转化的金额（分）
         * @return 转化后的金额（元）
         */
        public static String convertToYuan(int cents) {
            // 将分转换为元
            double yuan = (double) cents / 100.0;

            // 使用DecimalFormat来格式化保留2位小数，最多4位小数
            DecimalFormat decimalFormat = new DecimalFormat("#.####");
            String formattedYuan = decimalFormat.format(yuan);

            return formattedYuan;
        }

        /**
         * 将厘转化为元，保留2位小数，最多4位小数
         *
         * @param millis 需要转化的金额（厘）
         * @return 转化后的金额（元）
         */
        public static String convertMillisToYuan(long millis) {
            // 将厘（百分之一的分）转换为元
            double yuan = (double) millis / 1000.0;

            // 使用DecimalFormat来格式化保留2位小数，最多4位小数
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            // 直接舍去后面的值
            decimalFormat.setRoundingMode(RoundingMode.DOWN);

            return decimalFormat.format(yuan);
        }

        /**
         * 将厘转化为元，保留3位小数，最多4位小数
         *
         * @param millis 需要转化的金额（厘）
         * @return 转化后的金额（元）
         */
        public static String convertMillisToYuan3(long millis) {
            // 将厘（百分之一的分）转换为元
            double yuan = (double) millis / 1000.0;

            // 使用DecimalFormat来格式化保留2位小数，最多4位小数
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            // 直接舍去后面的值
            decimalFormat.setRoundingMode(RoundingMode.DOWN);

            return decimalFormat.format(yuan);
        }
    }

    /**
     * 安全判断相关
     */
    public static class Safe {

        /**
         * usb调试是否开启
         * @return true 开启
         */
        public static boolean isUsbDebuggingEnable() {
            return Settings.Global.getInt(Util.getApp().getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        }

        /**
         * 判断设备 是否使用代理上网
         */
        public static boolean isWifiProxy() {
            String proxyAddress;
            int proxyPort;
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
            return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
        }

        /**
         * 是否有hook框架
         * @return
         */
        public static boolean isHasHook() {
            try {
                Class xposedClass = ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge");
                return xposedClass != null;
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            }
            return false;
        }

        /**
         * 安全标识
         */
        public static int getSafeFlag() {
            int safe = 0b0 ;
            // 手机是否debug模式 1是 2否
            if (Safe.isUsbDebuggingEnable()) {
                safe |= 0b0001;
            }
            // 手机是否root     1是 2否
            if (CheckRoot.isDeviceRooted()) {
                safe |= 0b0010;
            }
            // 网络是否有代理    1是 2否
            if (Safe.isWifiProxy()) {
                safe |= 0b0100;
            }
            // 是否有hook框架   1是 2否
            if (Safe.isHasHook()) {
                safe |= 0b1000;
            }
            return safe;
        }
    }
}
