package com.inspur.playwork.core;

import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import com.inspur.playwork.BuildConfig;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Bugcode on 2016/4/19.
 */
public class PlayWorkCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = PlayWorkCrashHandler.class.getSimpleName();
    //    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
    private static final String FILE_PATH = FileUtil.getLogPath();
    private Thread.UncaughtExceptionHandler defaultUEH;
    private static PlayWorkCrashHandler crashHandler;

    private PlayWorkCrashHandler() {
    }

    public static PlayWorkCrashHandler getInstance() {
        if (crashHandler == null)
            crashHandler = new PlayWorkCrashHandler();
        return crashHandler;
    }

    public void init(boolean isOpen) {
        Log.i(TAG, "init: " + isOpen);
        if (!isOpen)
            return;
        // 获取系统默认的UncaughtException处理器
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);

        File filePath = new File(FILE_PATH);
        File[] files = filePath.listFiles();
        Log.i(TAG, "init: " + files.length);
        for (int i = 0; i < files.length; i++) {
            this.uploadFile(files[i]);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        // 获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
        StackTraceElement[] s = ex.getStackTrace();
        StackTraceElement[] st = new StackTraceElement[s.length + 3];
        System.arraycopy(s, 0, st, 0, s.length);
        st[s.length] = new StackTraceElement("Android", "MODEL", Build.MODEL, -1);
        st[s.length + 1] = new StackTraceElement("Android", "VERSION", Build.VERSION.RELEASE, -1);
        st[s.length + 2] = new StackTraceElement("Android", "FINGERPRINT", Build.FINGERPRINT, -1);
        st[s.length + 3] = new StackTraceElement("PlayWork", "FINGERPRINT", BuildConfig.VERSION_NAME + "---" +
                "" + BuildConfig.VERSION_CODE, -1);
        // 追加信息，因为后面会回调默认的处理方法
        ex.setStackTrace(st);
        ex.printStackTrace(printWriter);
        // 把上面获取的堆栈信息转为字符串，打印出来
        String stacktrace = result.toString();
        printWriter.close();
        this.writeLog(stacktrace);
        defaultUEH.uncaughtException(thread, ex);
    }

    private void writeLog(String log) {
        CharSequence timestamp = DateFormat.format("yyyy-MM-dd-kk-mm-ss", System.currentTimeMillis());
        File f = new File(FILE_PATH + PreferencesHelper.getInstance().getCurrentUser().id + "_" + timestamp + ".txt");
        try {
            FileOutputStream stream = new FileOutputStream(f);
            OutputStreamWriter output = new OutputStreamWriter(stream);
            BufferedWriter writer = new BufferedWriter(output);
            writer.write(log);
            writer.newLine();
            writer.close();
            output.close();
//            this.uploadFile(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(final File f) {
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
//                    deleteFile(response.body().string());
                }
            }
        }, null);
    }

    private void deleteFile(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String fileName = jsonObject.optString("fileName");
            File file = new File(FILE_PATH + fileName);
            file.delete();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
