package com.lrz.ui.download;

import android.text.TextUtils;

import com.lrz.coroutine.Dispatcher;
import com.lrz.coroutine.LLog;
import com.lrz.coroutine.handler.CoroutineLRZContext;
import com.lrz.ui.utils.Util;
import com.lrz.ui.utils.MD5;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Author And Date: liurongzhi.
 * Description: 下载工具类
 */
public class DownloadManager {
    private static DownloadManager manager;
    private final OkHttpClient mOkHttpClient;
    private final ConcurrentHashMap<String, DownloadTask> downloads;
    private String rootPath;
    private int retryCount = 3;
    private boolean isInit = false;
    private LinkedList<DownloadListener> listeners;

    public static DownloadManager getInstance() {
        if (manager == null) {
            manager = new DownloadManager();
        }
        return manager;
    }

    public DownloadManager retry(int num) {
        this.retryCount = num;
        return this;
    }

    public void rootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    private synchronized void init() {
        if (isInit) return;
        String path = Util.getApp().getCacheDir().getAbsolutePath();
        File exCache = Util.getApp().getExternalCacheDir();
        if (exCache != null && exCache.exists()) {
            path = Util.getApp().getExternalCacheDir().getAbsolutePath();
        }

        rootPath(path + File.separator + "download");

        File file = new File(rootPath);
        file.setExecutable(true);
        file.setReadable(true);
        file.setWritable(true);
        if (!file.exists()) {
            file.mkdirs();
        }
        isInit = true;
    }

    public synchronized void registerListener(DownloadListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void unRegisterListener(DownloadListener listener) {
        if (listeners == null) return;
        listeners.remove(listener);
    }

    private DownloadManager() {
        downloads = new ConcurrentHashMap<>();
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(20, TimeUnit.SECONDS)//设置读取超时时间
                .build();
    }


    public DownloadManager download(final ITask task) {
        if (!isInit) {
            init();
        }
        if (task == null || TextUtils.isEmpty(task.getDownload().getRequestUrl()) || task.getDownload().getRequestUrl().length() < 6) {
            LLog.e("DownloadManager", "下载失败，当前task不合法");
            if (task != null) onError(task, "下载失败，当前task不合法");
            return this;
        }
        if (task.getDownload().getState() > 1) {
            LLog.e("DownloadManager", "当前task正在下载中");
            return this;
        }

        if (!checkDown(task)) return this;
        task.getDownload().setState(DownloadStatus.DOWNLOADING);
        CoroutineLRZContext.Execute(Dispatcher.IO, () -> {
            Request request = new Request.Builder().url(task.getDownload().getRequestUrl()).addHeader("RANGE", "bytes=" + task.getDownload().getCurrentFileSize() + "-")
                    .build();
            Call call = mOkHttpClient.newCall(request);
            ((DownloadTask) task).setCall(call);
            try {
                Response response = call.execute();
                onResponse(response, task, call);
                response.close();
            } catch (IOException e) {
                onError(task, e);
            }
        });
        return this;
    }

    private void onError(ITask task, Exception e) {
        if (task.getRetryNum() < retryCount) {
            CoroutineLRZContext.ExecuteDelay(Dispatcher.MAIN, () -> {
                LLog.e("DownloadManager", e.getMessage() + "下载出错，重试中。。。", e);
                task.getDownload().setState(DownloadStatus.DEFAULT);
                task.setRetryNum(task.getRetryNum() + 1);
                DownloadManager.this.download(task);
            }, 2000L * (task.getRetryNum() + 1));
        } else {
            CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                if (task.getDownload() != null)
                    task.getDownload().setState(DownloadStatus.DEFAULT);
                onError(task, e.getMessage());
            });
        }
    }

    private void onResponse(Response response, ITask task, Call call) {
        task.getDownload().setRealUrl(response.request().url().toString());
        ResponseBody body = response.body();
        if (body == null) {
            onError(task, new IOException("source not found"));
            return;
        }
        long fileSize = body.contentLength();

        if (fileSize < 2) {
            CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                task.getDownload().setState(DownloadStatus.DEFAULT);
                onError(task, "file length error");
            });
            return;
        }
        LLog.e("DownloadManager", "有上次未完成下载：" + fileSize + "  当前：" + task.getDownload().getCurrentFileSize());

        task.getDownload().setTotalFileSize(fileSize + task.getDownload().getCurrentFileSize());
        task.getDownload().setProgress((int) (task.getDownload().getCurrentFileSize() * 100 / task.getDownload().getTotalFileSize()));

        CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
            task.getDownload().setState(DownloadStatus.DOWNLOADING);
            onStart(task);
        });

        int count;
        byte data[] = new byte[1024 * 2];

        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 4);
        RandomAccessFile outFile = null;
        try {
            outFile = new RandomAccessFile(task.getDownload().getTmpPath(), "rws");
            outFile.seek(task.getDownload().getCurrentFileSize());
            while ((count = bis.read(data)) != -1) {
                if (task.getDownload().getState() <= 0) {
                    call.cancel();
                    ((DownloadTask) task).setCall(null);
                    return;
                }
                outFile.write(data, 0, count);
                task.getDownload().setCurrentFileSize(task.getDownload().getCurrentFileSize() + count);
                task.getDownload().setProgress((int) (task.getDownload().getCurrentFileSize() * 100 / task.getDownload().getTotalFileSize()));

                CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                    onProgress(task);
                });
            }
            outFile.close();
            bis.close();
            final File file = new File(task.getDownload().getTmpPath());
            file.renameTo(new File(file.getAbsolutePath().replace(".tmp", "")));
            File newFile = new File(file.getAbsolutePath().replace(".tmp", ""));
            newFile.setWritable(true);
            newFile.setReadable(true);
            newFile.setExecutable(true);
            task.getDownload().setAbsolutePath(file.getAbsolutePath().replace(".tmp", ""));


            CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                task.getDownload().setState(DownloadStatus.SUCCESS);
                onProgress(task);
                onSuccess(task);
            });
            downloads.remove(task.getDownload().getTaskID());
        } catch (final Exception exception) {
            if (task.getDownload().getState() == 0) {//表示是正常的暂停
                CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                    onPause(task);
                });
                return;
            }
            if (task.getDownload().getState() == 3) {//表示是正常的取消
                CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                    onCancel(task);
                });
                downloads.remove(task.getDownload().getTaskID());
                return;
            }
            onError(task, exception);
        } finally {
            try {
                if (outFile != null) outFile.close();
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized <T extends DownloadTask> T url(String url, Class<T> tClass) {
        return (T) url(url, tClass, null);
    }

    public synchronized <T extends DownloadTask> T url(String url, Class<T> tClass, String taskID) {
        if (TextUtils.isEmpty(url)) return null;
        DownloadTask task = null;
        //先从内存中取
        if (taskID == null || taskID.length() < 1) {
            taskID = Integer.toHexString(url.hashCode());
        }

        if (downloads.containsKey(taskID)) {
            task = downloads.get(taskID);
            return (T) task;
        }

        final Download download = new Download();
        download.setRequestUrl(url);
        download.setFileName(MD5.getMD5(url));
        download.setTaskID(taskID);
        try {
            task = tClass.newInstance();
        } catch (Exception e) {
            task = new DownloadTask();
        }
        task.setDownload(download);
        downloads.put(download.getTaskID(), task);
        return (T) task;
    }


    public synchronized DownloadTask url(String url) {
        if (TextUtils.isEmpty(url)) return null;
        DownloadTask task = null;
        //先从内存中取
        if (downloads.containsKey(Integer.toHexString(url.hashCode()))) {
            task = downloads.get(Integer.toHexString(url.hashCode()));
            return task;
        }

        final Download download = new Download();
        download.setRequestUrl(url);
        download.setFileName(MD5.getMD5(url));
        task = new DownloadTask();
        task.setDownload(download);
        downloads.put(download.getTaskID(), task);
        return task;
    }

    private boolean checkDown(final ITask task) {
        //看是否下载完成
        final Download download = task.getDownload();
        if (download == null) return false;
        if (task.getDownload().getState() == DownloadStatus.DOWNLOADING) return false;
        if (download.getState() == DownloadStatus.SUCCESS) {
            //已经下载完成,则判断文件是否存在
            if (!TextUtils.isEmpty(download.getAbsolutePath())) {
                File file = new File(download.getAbsolutePath());
                if (file.exists()) {
                    CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                        onSuccess(task);
                    });
                    downloads.remove(task.getDownload().getTaskID());
                    return false;
                }
            }
        }
        File file = new File(rootPath, download.getFileName());
        if (file.exists() && !file.isDirectory()) {
            if (((DownloadTask) task).isForce()) {
                file.delete();
            } else {
                download.setAbsolutePath(file.getAbsolutePath());
                download.setProgress(100);
                CoroutineLRZContext.Execute(Dispatcher.MAIN, () -> {
                    download.setState(DownloadStatus.SUCCESS);
                    onSuccess(task);
                });
                downloads.remove(task.getDownload().getTaskID());
                return false;
            }
        }

        File tempFile = new File(rootPath, download.getFileName() + ".tmp");
        download.setTmpPath(tempFile.getAbsolutePath());
        if (tempFile.exists()) {
            download.setTmpPath(tempFile.getAbsolutePath());
            download.setCurrentFileSize(tempFile.length());
            download.setState(DownloadStatus.PAUSE);
        } else {
            //临时文件已被删除，重新下载
            download.setTmpPath(tempFile.getAbsolutePath());
            download.setState(DownloadStatus.DEFAULT);
            download.setProgress(0);
            download.setCurrentFileSize(0);
            download.setTotalFileSize(0);
        }
        return true;
    }

    public ITask findTaskByUrl(String url) {
        if (!downloads.isEmpty()) {
            for (Map.Entry<String, DownloadTask> entry : downloads.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getDownload() != null
                        && entry.getValue().getDownload().getRequestUrl().equals(url)) {
                    return findTask(entry.getValue().getDownload().getTaskID());
                }
            }
        }
        return findTask(url.hashCode());
    }

    public ITask findTask(int intID) {
        if (!downloads.isEmpty()) {
            for (Map.Entry<String, DownloadTask> entry : downloads.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getDownload() != null
                        && entry.getValue().getDownload().getIntID() == (intID)) {
                    return findTask(entry.getValue().getDownload().getTaskID());
                }
            }
        }
        return findTask(Integer.toHexString(intID));
    }

    public ITask findTask(String taskID) {
        return downloads.get(taskID);
    }

    public void onStart(ITask task) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0)
                    listener.onStart(task);
            }
        }
    }

    public void onProgress(ITask task) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0) {
                    if ((((DownloadTask) task).needUpdate())) {
                        listener.onProgress(task);
                    }
                }
            }
        }
    }

    public void onPause(ITask task) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0)
                    listener.onPause(task);
            }
        }
    }

    public void onCancel(ITask task) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0)
                    listener.onCancel(task);
            }
        }
    }

    public void onSuccess(ITask task) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0)
                    listener.onSuccess(task);
            }
        }
    }

    public void onError(ITask task, String msg) {
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                if (task.getDownload().getIntID() == listener.getIntId() || listener.getIntId() == 0)
                    listener.onError(task, msg);
            }
        }
    }
}
