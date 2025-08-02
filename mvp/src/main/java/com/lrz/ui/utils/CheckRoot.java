package com.lrz.ui.utils;

import com.lrz.coroutine.LLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Author:  zhaopeng
 * CreateTime:  2024/3/4
 * Description:
 */
public class CheckRoot {
    private static final String LOG_TAG = CheckRoot.class.getName();

    /**
     * 设备是否root
     *
     * @return true 已经root
     */
    public static boolean isDeviceRooted() {
        if (checkDeviceDebuggable()) {
            return true;
        }
        if (checkSuperuserApk()) {
            return true;
        }
        if (checkBusybox()) {
            return true;
        }
        if (checkAccessRootData()) {
            return true;
        }
        return checkGetRootAuth();
    }

    public static boolean checkDeviceDebuggable() {
        String buildTags = android.os.Build.TAGS;
        LLog.i(LOG_TAG, "buildTags=" + buildTags);
        return buildTags != null && buildTags.contains("test-keys");
    }

    public static boolean checkSuperuserApk() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                LLog.i(LOG_TAG, "/system/app/Superuser.apk exist");
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static synchronized boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            LLog.i(LOG_TAG, "to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            LLog.i(LOG_TAG, "exitValue=" + exitValue);
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) process.destroy();
            } catch (Exception ignored) {

            }
        }
    }

    public static synchronized boolean checkBusybox() {
        try {
            LLog.i(LOG_TAG, "to exec busybox df");
            String[] strCmd = new String[]{"busybox", "df"};
            ArrayList<String> execResult = executeCommand(strCmd);
            if (execResult != null) {
                LLog.i(LOG_TAG, "execResult=" + execResult.toString());
                return true;
            } else {
                LLog.i(LOG_TAG, "execResult=null");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static ArrayList<String> executeCommand(String[] shellCmd) {
        String line = null;
        ArrayList<String> fullResponse = new ArrayList<String>();
        Process localProcess = null;
        try {
            LLog.i(LOG_TAG, "to shell exec which for find su :");
            localProcess = Runtime.getRuntime().exec(shellCmd);
        } catch (Exception e) {
            return null;
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(localProcess.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
        try {
            while ((line = in.readLine()) != null) {
                LLog.i(LOG_TAG, "–> Line received: " + line);
                fullResponse.add(line);
            }
        } catch (Exception ignored) {

        }
        LLog.i(LOG_TAG, "–> Full response was: " + fullResponse);
        return fullResponse;
    }

    public static synchronized boolean checkAccessRootData() {
        try {
            LLog.i(LOG_TAG, "to write /data");
            String fileContent = "test_ok";
            Boolean writeFlag = writeFile("/data/su_test", fileContent);
            if (writeFlag) {
                LLog.i(LOG_TAG, "write ok");
            } else {
                LLog.i(LOG_TAG, "write failed");
            }

            LLog.i(LOG_TAG, "to read /data");
            String strRead = readFile("/data/su_test");
            LLog.i(LOG_TAG, "strRead=" + strRead);
            return fileContent.equals(strRead);
        } catch (Exception e) {
            return false;
        }
    }

    //写文件
    public static Boolean writeFile(String fileName, String message) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //读文件
    public static String readFile(String fileName) {
        File file = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(bytes)) > 0) {
                bos.write(bytes, 0, len);
            }
            String result = bos.toString();
            LLog.i(LOG_TAG, result);
            fis.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
