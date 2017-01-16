package com.inspur.playwork.weiyou.utils;

import android.os.Handler;
import android.util.Base64OutputStream;
import android.util.Log;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.OkHttpClientManager.Param;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * okhttp工具类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class OkHttpClientManager {

    private static final String TAG = "OkHttpClientManager";

    // 请求数据json类型
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient mOkHttpClient;

    private Handler handler;

    private static OkHttpClientManager clientManager = new OkHttpClientManager();

    private Constructor<Base64OutputStream> constructor;

    public interface HttpOperationCallback {
        void onSuccess(String result) throws JSONException;

        void onFailed(Exception e);
    }

    public static OkHttpClientManager getInstance() {
        return clientManager;
    }

    private OkHttpClientManager() {
        try {
            constructor = Base64OutputStream.class.getConstructor(OutputStream.class, int.class, boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        initHttpClient();
    }


    public OkHttpClientManager initHandler(Handler handler) {
        this.handler = handler;
        return clientManager;
    }

    private void initHttpClient() {
        if (mOkHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            mOkHttpClient = builder.connectTimeout(8, TimeUnit.SECONDS)
                    .build();
        }
    }

    /**
     * okhttp sync get request
     *
     * @param url API
     * @return
     */
    public String get(String url) {
        String result = null;
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void get(String url, final HttpOperationCallback callback) {
        getAsyn(url, new Callback() {
            @Override
            public void onFailure(Call request, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onSuccess(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * okhttp async get request
     *
     * @param url              API
     * @param responseCallback 回调方法
     */
    public void getAsyn(String url, Callback responseCallback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }


    /**
     * okhttp sync post request
     *
     * @param url     API
     * @param jsonStr 请求参数json字符串
     * @return
     */
    public String post(String url, String jsonStr) {
        String result = null;
        try {
            RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonStr);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void post(String url, Param[] paramsr, final HttpOperationCallback callback) {
        Log.i(TAG, "url=====" + url);
        Request request = buildMultipartFormRequest(url, paramsr);
        for (Param p : paramsr) {
            Log.i(TAG, p.key + " --- " + p.value);
        }
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call request, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onSuccess(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * 下载微盘附件
     *
     * @param url
     * @param filePath
     * @param callback
     */
    public void downLoadWPFile(final String url, final String filePath, final Callback callback) {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                Call call = mOkHttpClient.newCall(request);
                InputStream inputStream = null;
                FileOutputStream fos = null;
                try {
                    Response response = call.execute();
                    byte[] buf = new byte[1024 * 2];
                    inputStream = response.body().byteStream();
                    String fileName = response.header("Content-Disposition").split("=")[1];
                    fos = new FileOutputStream(new File(filePath + fileName));
                    long downloaded = 0;
                    Log.d(TAG, "downLoadFile  response: " + response);

                    long target = Long.parseLong(response.header("File-Size"));
                    int readed;
                    while ((readed = inputStream.read(buf)) != -1) {
                        downloaded += readed;
                        fos.write(buf, 0, readed);
                    }
                    if (downloaded == target) {
                        callback.onResponse(call, response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailure(call, e);
                } finally {
                    Util.closeQuietly(inputStream);
                    Util.closeQuietly(fos);
                }
            }
        });
    }

    /**
     * 下载微邮文件
     *
     * @param url
     * @param filePath
     * @param callback
     * @param listener
     */
    public void downLoadVUFile(final String url, final String filePath, final long fileSize, final Callback callback, final ProgressResponseListener listener) {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                Call call = mOkHttpClient.newCall(request);
                InputStream inputStream = null;
                FileOutputStream fos = null;
                try {
                    Response response = call.execute();
                    byte[] buf = new byte[1024 * 2];
                    inputStream = response.body().byteStream();
                    fos = new FileOutputStream(new File(filePath));
                    long downloaded = 0;
                    if (listener != null)
                        listener.onResponseProgress(0, fileSize, false);
                    int readed;
                    while ((readed = inputStream.read(buf)) != -1) {
                        downloaded += readed;
                        fos.write(buf, 0, readed);
                        if (listener != null) {
                            listener.onResponseProgress(downloaded, fileSize, false);
                        }
                    }
                    callback.onResponse(call, response);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailure(call, e);
                } finally {
                    Util.closeQuietly(inputStream);
                    Util.closeQuietly(fos);
                }
            }
        });
    }

    public void checkAccountFromServer(MailAccount paramMailAccount, OkHttpClientManager.HttpOperationCallback paramHttpOperationCallback) {
        try {
            Param[] params = {
                    new Param("address", paramMailAccount.getEmail()),
                    new Param("sendServer", paramMailAccount.getOutGoingHost()),
                    new Param("sendPort", paramMailAccount.getOutGoingPort()),
                    new Param("receiveServer", paramMailAccount.getInComingHost()),
                    new Param("receivePort", paramMailAccount.getInComingPort()),
                    new Param("protocol", paramMailAccount.getProtocol()),
                    new Param("userId", paramMailAccount.getUserId()),
                    new Param("username", paramMailAccount.getAccount()),
                    new Param("password", paramMailAccount.getPassword())
            };
            OkHttpClientManager.getInstance().initHandler(new Handler()).post(AppConfig.WY_CFG.URL_VERIFY_MAIL_ACCOUNT, params, paramHttpOperationCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Request buildMultipartFormRequest(String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }

        FormBody.Builder builder = new FormBody.Builder();
//        FormEncodingBuilder builder = new FormEncodingBuilder();
//        RequestBody

        for (Param param : params) {
//            builder.add(param.key, param.value);
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }
}