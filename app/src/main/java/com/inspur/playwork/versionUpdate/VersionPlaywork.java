package com.inspur.playwork.versionUpdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.VersionInfoBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;

import java.io.File;
import java.lang.ref.WeakReference;


public class VersionPlaywork {


    private double newVerCode = 0;//新版本号
    private String newVerName = "";

//    private ProgressDialog pd = null;

    //    private Activity activity = null;

    private boolean checkByHand = false;

    private VersionInfoBean jsonObj;

    private NotificationManager mNotificationManager;

    private static final String TAG = "VersionPlaywork";

    private Handler handler;

    private UpdateProgressRunnable runnable;

    private WeakReference<Activity> viewReference;


    public VersionPlaywork(Activity activity, VersionInfoBean jsonObj, Handler handler) {
//        this.activity = activity;
        this.jsonObj = jsonObj;
        this.initNotify(activity);
        this.handler = handler;
        viewReference = new WeakReference<>(activity);
        updateVersion();
    }

    public VersionPlaywork(Activity activity, boolean byHand, VersionInfoBean jsonObj, Handler handler) {
//        this.activity = activity;
        this.checkByHand = byHand;//手动检查更新
        this.jsonObj = jsonObj;
        this.handler = handler;
        this.initNotify(activity);
        viewReference = new WeakReference<>(activity);
        updateVersion();
    }

    private void initNotify(Context context) {
        NotificationCompat.Builder mNotificationCompatBuilder = new NotificationCompat.Builder(context.getApplicationContext()).setOngoing(false).setSmallIcon(R.drawable.waner_logo);
        mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        runnable = new UpdateProgressRunnable(mNotificationCompatBuilder, mNotificationManager);
    }

    private void updateVersion() {
        if (getServerVerCode() && getServerVerName()) {
            int verCode = this.getVerCode(viewReference.get());
            double new_verCode = verCode / 100.0;//将版本生成浮点数 进行比较
            Log.i(TAG, "newVerCode" + newVerCode + "<===new_verCode===>" + new_verCode);
            if (newVerCode > new_verCode) {
                doNewVersionUpdate();//更新版本
            } else if (checkByHand) {
                // notNewVersionUpdate();//提示已是最新版本
                UItoolKit.showToastShort(viewReference.get(), "已是最新版本");
            }
        }
    }

    /**
     * 获得版本号
     */
    private int getVerCode(Context context) {
        int verCode = -1;
        try {
            String packName = context.getPackageName();
            verCode = context.getPackageManager().getPackageInfo(packName, 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e("版本号获取异常", e.getMessage());
        }
        return verCode;
    }

    /**
     * 获得版本名称
     */
    private String getVerName(Context context) {
        String verName = "";
        try {
            String packName = context.getPackageName();
            verName = context.getPackageManager().getPackageInfo(packName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e("版本名称获取异常", e.getMessage());
        }
        return verName;
    }

    /**
     * 从服务器端获得版本号与版本名称
     *
     * @return
     */
    private boolean getServerVerCode() {
        try {
            newVerCode = Double.parseDouble(jsonObj.Version);
            Log.i(TAG, "newVerCode: " + newVerCode);
            return true;
        } catch (Exception e) {
            return false;//如果这里改为false 则不更新会退出程序
        }

    }

    /**
     * 从服务器端获得版本号与版本名称
     *
     * @return
     */
    private boolean getServerVerName() {
        try {
            newVerName = jsonObj.VersionName;
            Log.i(TAG, "onResponse====》 newVerName: " + newVerName);
            return true;
        } catch (Exception e) {
            return false;//如果这里改为false 则不更新会退出程序
        }

    }

    /**
     * 不更新版本
     */
    public void notNewVersionUpdate() {
        int verCode = this.getVerCode(viewReference.get());
        String verName = this.getVerName(viewReference.get());
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本：");
        sb.append(verName);
        sb.append("\n已是最新版本，无需更新");
        Dialog dialog = new AlertDialog.Builder(viewReference.get())
                .setTitle("软件更新")
                .setMessage(sb.toString())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //activity.finish();
                    }
                }).create();
        dialog.show();
    }

    /**
     * 更新版本
     */
    private void doNewVersionUpdate() {
        String verName = this.getVerName(viewReference.get());
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本：（V");
        sb.append(verName);
        sb.append("）\n");
        sb.append("发现版本：(V");
        sb.append(newVerName);
        sb.append("）, 是否更新\n");
        sb.append("更新内容：\n");
        sb.append(jsonObj.Updatecontent);
        Dialog dialog = new AlertDialog.Builder(viewReference.get())
                .setTitle("软件更新")
                .setMessage(sb.toString())
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
//                        downloadFileByBrowser(AppConfig.DOWNLOAD_APK_FILE);
//                        dialog.dismiss();
                        UItoolKit.showToastShort(viewReference.get(), "正在下载更新文件");
                        ThreadPool.exec(new Runnable() {
                            @Override
                            public void run() {
                                autoUpdate(jsonObj.VURL, FileUtil.getDownloadPath() + "Waner.apk");
                            }
                        });
                    }
                })
                .setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
                    }
                }).create();
        //显示更新框
        dialog.show();
    }

    /**
     * 安装应用
     */
    private void update() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
        String update_apkName = "Waner.apk";
//        intent.setDataAndType(Uri.fromFile(new File(FileUtil.getDownloadPath(), update_apkName))
//                , "application/vnd.android.package-archive");
        if (viewReference.get() != null)
            viewReference.get().startActivity(FileUtil.getOpenFileIntent(viewReference.get(), FileUtil.getDownloadPath() + update_apkName));
    }

    /**
     * 自动下载安装文件
     *
     * @param url
     * @param fileName
     */
    private void autoUpdate(String url, String fileName) {
        if (!url.contains("user_id")) {
            url += "&user_id=" + PreferencesHelper.getInstance().getCurrentUser().id;
        }
        Log.d(TAG, "autoUpdate() called with: " + "url = [" + url + "], fileName = [" + fileName + "]");

        boolean res = OkHttpClientManager.getInstance().downloadFile(url, fileName, new ProgressResponseListener() {
            @Override
            public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                if (!done) {
                    VersionPlaywork.this.setNotify((int) bytesRead, (int) contentLength, "正在下载");
                } else {
                    VersionPlaywork.this.setNotify((int) bytesRead, (int) contentLength, "下载完成");
                }
            }
        });
        if (res) {
            this.update();
            mNotificationManager.cancel(101);
        }
//        try {
//            Request request = new Request.Builder().url(url).build();
//            Response response = new OkHttpClient().newCall(request).execute();
//            if (response.isSuccessful()) {
////                int length = Integer.parseInt(response.header("Content-Length"));
//                int length = Integer.parseInt(response.header("File-Size"));
//                inputStream = response.body().byteStream();
//                File file = new File(fileName);
//                fileOutputStream = new FileOutputStream(file);
//                byte[] buffer = new byte[4 * 1024];
//                int notifySize = 0;
//                int readSize;
//                int downCount = 0;
//                this.setNotify(downCount, length, "正在下载");
//                while ((readSize = inputStream.read(buffer)) != -1) {
//                    fileOutputStream.write(buffer, 0, readSize);
//                    downCount += readSize;
//                    notifySize += readSize;
//                    if (downCount < length) {
//                        if (notifySize > NOTIFY_NUM) {
//                            this.setNotify(downCount, length, "正在下载");
//                            notifySize = 0;
//                        }
//                    } else {
//                        this.setNotify(downCount, length, "下载完成");
//                    }
//                }
//                fileOutputStream.flush();
//                // 自动更新
//                this.update();
//                mNotificationManager.cancel(101);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            Util.closeQuietly(inputStream);
//            Util.closeQuietly(fileOutputStream);
//        }
    }

    /**
     * 更新进度
     *
     * @param downCount
     * @param length
     * @param downTitle
     */
    private void setNotify(int downCount, int length, String downTitle) {
      /*  mNotificationCompatBuilder.setContentText(downTitle);
        mNotificationCompatBuilder.setProgress(length, downCount, false);
        mNotificationManager.notify(101, mNotificationCompatBuilder.build());*/
        runnable.setProgress(downCount, length, downTitle);
        handler.post(runnable);
    }

    private static class UpdateProgressRunnable implements Runnable {

        private NotificationCompat.Builder mNotificationCompatBuilder;
        private NotificationManager mNotificationManager;

        private int count, length;
        private String downTitle;


        UpdateProgressRunnable(NotificationCompat.Builder mNotificationCompatBuilder, NotificationManager mNotificationManager) {
            this.mNotificationCompatBuilder = mNotificationCompatBuilder;
            this.mNotificationManager = mNotificationManager;
        }

        void setProgress(int count, int length, String downTitle) {
            this.count = count;
            this.length = length;
            this.downTitle = downTitle;
        }

        @Override
        public void run() {
            mNotificationCompatBuilder.setContentText(downTitle);
            mNotificationCompatBuilder.setProgress(length, count, false);
            mNotificationManager.notify(101, mNotificationCompatBuilder.build());
        }
    }
}