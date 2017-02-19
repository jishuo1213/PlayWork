package com.inspur.playwork.core;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.Log;

import com.inspur.playwork.BuildConfig;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * Created by fan on 17-1-9.
 */
class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;

    private static final String FILE_PATH = FileUtil.getLogPath();
    //log文件的后缀名
    private static CrashHandler sInstance = new CrashHandler();

    //系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

//    private Context mContext;

    //构造方法私有，防止外部构造多个实例，即采用单例模式
    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    //这里主要完成初始化工作
    public void init() {
        //获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        //获取Context，方便内部使用
//        mContext = context.getApplicationContext();

        File filePath = new File(FILE_PATH);
        File[] files = filePath.listFiles();
        Log.i(TAG, "init: " + files.length);
        for (File file : files) {
            uploadExceptionToServer(file);
        }
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //导出异常信息到SD卡中
        dumpExceptionToSDCard(ex);
        //这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug

        //打印出当前调用栈信息
        ex.printStackTrace();

        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultCrashHandler != null) {
            Process.killProcess(Process.myPid());
//            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }

    }

    private void dumpExceptionToSDCard(Throwable ex) {
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }

        CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
        File file = new File(FILE_PATH + PreferencesHelper.getInstance().getCurrentUser().id + "_" + timestamp + ".txt");
//        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //以当前时间创建log文件
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //导出发生异常的时间
//            pw.println(time);

            //导出手机信息
            dumpPhoneInfo(pw);

            pw.println();
            //导出异常的调用栈信息
            ex.printStackTrace(pw);

//            pw.close();
//            uploadExceptionToServer(file);
        } catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(pw);
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        //应用的版本名称和版本号
//        PackageManager pm = mContext.getPackageManager();
//        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(BuildConfig.VERSION_NAME);
        pw.print('_');
        pw.println(BuildConfig.VERSION_CODE);

        //android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //cpu架构
        pw.print("CPU ABI: ");
        if (Build.VERSION.SDK_INT > 20)
            pw.println(Build.SUPPORTED_ABIS[0]);
        else
            pw.println(Build.CPU_ABI);
    }

    private void uploadExceptionToServer(final File f) {
        if (f.exists()) {
            OkHttpClientManager.getInstance().postAsyn("http://218.57.135.45:55166/feedback/uploadFiles", "uploadFile", f, null, new Callback() {
                @Override
                public void onFailure(Call request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i(TAG, "onResponse: " + f.getAbsolutePath());
                    Log.i(TAG, "onResponse: " + response.toString());
                    if (response.code() == 200) {
                        f.delete();
                    }
                }
            }, null);
        }
    }

//    private void deleteFile(String jsonData) {
//        try {
//            JSONObject jsonObject = new JSONObject(jsonData);
//            String fileName = jsonObject.optString("fileName");
//            File file = new File(FILE_PATH + fileName);
//            file.delete();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}