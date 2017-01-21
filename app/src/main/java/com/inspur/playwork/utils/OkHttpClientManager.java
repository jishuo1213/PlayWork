package com.inspur.playwork.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

//import com.inspur.playwork.utils.loadfile.ProgressRequestBody;
//import com.inspur.playwork.utils.loadfile.ProgressRequestListener;
//import com.inspur.playwork.utils.loadfile.ProgressResponseListener;
//import com.squareup.okhttp.Call;
//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.Headers;
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.MultipartBuilder;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;
//import com.squareup.okhttp.internal.Util;


import com.inspur.playwork.utils.loadfile.ProgressRequestListener;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//import okhttp3.OkHttpClient;

/**
 * okhttp工具类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class OkHttpClientManager {

    private static final String TAG = "OkHttpClientManager";

    // 请求数据json类型
//    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

//    private OkHttpClient mOkHttpClient;


    private OkHttpClientManagerNew newManager;


    private static OkHttpClientManager clientManager = new OkHttpClientManager();

    public static OkHttpClientManager getInstance() {
        if (clientManager == null)
            clientManager = new OkHttpClientManager();
        return clientManager;
    }

    private OkHttpClientManager() {
        initHttpClient();
    }

    private void initHttpClient() {
//        if (mOkHttpClient == null) {
//            mOkHttpClient = new OkHttpClient();
//            mOkHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
//        }
        if (newManager == null)
            newManager = OkHttpClientManagerNew.getInstance();
    }

    public void clean() {
//        mOkHttpClient = null;
        newManager = null;
    }


    public void setToken(String token) {
        newManager.setToken(token);
    }

    /**
     * okhttp sync get request
     *
     * @param url API
     * @return
     */
    public String get(String url) {
        return newManager.get(url);
    }

    /**
     * okhttp sync get request
     *
     * @param url API
     * @return
     */
    public String get(String url, String param) {
        return newManager.get(url, param);
    }

    /**
     * okhttp async get request
     *
     * @param url              API
     * @param responseCallback 回调方法
     */
    public Call getAsyn(String url, Callback responseCallback) {
        Log.i(TAG, "getAsyn: " + url);
//        Request request = new Request.Builder().url(url).build();
//        mOkHttpClient.newCall(request).enqueue(responseCallback);
        return newManager.getAsyn(url, responseCallback);
    }

    public Call getAsyn(String url, Callback responseCallback, JSONObject jsonParam, String requestId) {
        return newManager.getAsyn(url, responseCallback, jsonParam, requestId);
    }

    public Call newGetAsyn(String url, Callback responseCallback, JSONObject jsonParam, String requestId) {
        return newManager.newGetAsyn(url, responseCallback, jsonParam, requestId);
    }

    public void postFormData(String url, Callback responseCallback, JSONObject params, String requestId) {
        newManager.postFormData(url, responseCallback, params, requestId);
    }


    /**
     * okhttp sync post request
     *
     * @param url     API
     * @param jsonStr 请求参数json字符串
     * @return
     */
    public String post(String url, String jsonStr) {
        return newManager.post(url, jsonStr);
    }

    /**
     * okhttp async post request
     *
     * @param url             API
     * @param jsonStr         请求参数json字符串
     * @param requestCallback 回调方法
     */
    public void post(String url, JSONObject jsonStr, Callback requestCallback, String requestId) {
        newManager.post(url, jsonStr, requestCallback, requestId);
    }

    public void newPost(String url, JSONObject jsonStr, Callback requestCallback, String requestId) {
        newManager.newPost(url, jsonStr, requestCallback, requestId);
    }


    /*下载文件
    *  @method downloadFile
    *  @param String url, String fileName String dir (文件保存在那个目录下)
    * */
    public void downloadFileUseFirst(final String url, final String dir, final @NonNull String fileName) {
//        InputStream is = null;
//        FileOutputStream fos = null;
//        try {
//            Request request = new Request.Builder().url(url).build();
//            Response response = mOkHttpClient.newCall(request).execute();
//            if (response.isSuccessful()) {
//                is = response.body().byteStream();
//                File file = FileUtil.createFileInSDCard(dir, fileName);
//
//                fos = new FileOutputStream(file);
//                byte[] b = new byte[4 * 1024];
//                int charb;
//                int count = 0;
//                while ((charb = is.read(b)) != -1) {
//                    fos.write(b, 0, charb);
//                    count += charb;
//                }
//                fos.flush();
//                Log.i(TAG, "下载文件成功了 count = " + count);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.i(TAG, "下载文件失败了");
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//                if (fos != null) {
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        newManager.downloadFileUseFirst(url, dir, fileName);
    }


    public boolean downloadFile(final String url, final String fileName) throws IOException {

//        Log.i(TAG, "downLoad url" + url);
//        if (downLoadingUrls.contains(url)) {
//            return false;
//        }
//        InputStream is = null;
//        FileOutputStream fos = null;
//
//        downLoadingUrls.add(url);
//        try {
//            Request request = new Request.Builder().url(url).build();
//            Response response = mOkHttpClient.newCall(request).execute();
//            if (response.isSuccessful()) {
//                is = response.body().byteStream();
//                File file = new File(fileName);
////                file.createNewFile();
//                fos = new FileOutputStream(file);
//                byte[] b = new byte[4 * 1024];
//                int charb;
//                while ((charb = is.read(b)) != -1) {
//                    fos.write(b, 0, charb);
//                }
//                fos.flush();
//                return true;
//            } else {
//                return false;
//            }
//        } finally {
//            downLoadingUrls.remove(url);
//            Util.closeQuietly(is);
//            Util.closeQuietly(fos);
//        }
        return newManager.downloadFile(url, fileName);
    }


    public boolean downloadFile(final String url, final String fileName, ProgressResponseListener listener) {
        return newManager.downloadFile(url, fileName, listener);
    }

    public void cancelDownload(String url, String fileName) {
        newManager.cancelDownload(url, fileName);
    }


    /**
     * 下载文件
     *
     * @param url
     * @param filePath
     * @param callback
     * @param listener
     */
    public void downLoadFile(final String url, final String filePath, final Callback callback, final ProgressResponseListener listener) {
//        ThreadPool.exec(new Runnable() {
//            @Override
//            public void run() {
//                Request request = new Request.Builder().url(url).build();
//                Call call = mOkHttpClient.newCall(request);
//                InputStream inputStream = null;
//                FileOutputStream fos = null;
//                try {
//                    Response response = call.execute();
//                    byte[] buf = new byte[1024 * 4];
//                    inputStream = response.body().byteStream();
//                    fos = new FileOutputStream(new File(filePath));
//                    long downloaded = 0;
//                    String size = response.header("File-Size");
//                    if (TextUtils.isEmpty(size)) {
//                        callback.onFailure(request, null);
//                        return;
//                    }
//                    long target = Long.parseLong(size);
//                    if (listener != null)
//                        listener.onResponseProgress(0, target, false);
//                    int readed;
//                    long reportSize = target / 100;
//                    long reportReaded = 0;
//                    while ((readed = inputStream.read(buf)) != -1) {
//                        downloaded += readed;
//                        reportReaded += readed;
//                        fos.write(buf, 0, readed);
//                        if (listener != null) {
//                            if (reportReaded >= reportSize && downloaded != target) {
//                                listener.onResponseProgress(downloaded, target, false);
//                                reportReaded = 0;
//                            } else if (downloaded == target) {
//                                listener.onResponseProgress(downloaded, target, true);
//                            }
//                        }
//                    }
//                    if (downloaded == target) {
//                        callback.onResponse(response);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    callback.onFailure(request, e);
//                } finally {
//                    Util.closeQuietly(inputStream);
//                    Util.closeQuietly(fos);
//                }
//            }
//        });
        newManager.downLoadFile(url, filePath, callback, listener);
    }


    /**
     * 异步基于post的文件上传:主方法
     */
//    private void postAsyn(String url, String[] fileKeys, File[] files, Param[] params, Callback callback, ProgressRequestListener listener) {
////        Request request = buildMultipartFormRequest(url, fileKeys, files, params, listener);
////        mOkHttpClient.newCall(request).enqueue(callback);
//        newManager.postAsyn(url,fileKeys,files,params,callback,listener);
//    }

//    private Response postSync(String url, String[] fileKeys, File[] files, Param[] params, ProgressRequestListener listener) throws IOException {
//        Request request = buildMultipartFormRequest(url, fileKeys, files, params, listener);
//        return mOkHttpClient.newCall(request).execute();
//    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     */
    public void postAsyn(String url, String fileKey, File file, Param[] params, Callback callback, ProgressRequestListener listener) {
        newManager.postAsyn(url, fileKey, file, params, callback, listener);
    }

    public Response postSync(String url, String fileKey, File file, Param[] params, ProgressRequestListener listener) throws IOException {
        return newManager.postSync(url, fileKey, file, params, listener);
    }

    /**
     * 参数类
     */
    public static class Param implements Parcelable {
        public String key;
        public String value;

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        protected Param(Parcel in) {
            key = in.readString();
            value = in.readString();
        }

        public static final Creator<Param> CREATOR = new Creator<Param>() {
            @Override
            public Param createFromParcel(Parcel in) {
                return new Param(in);
            }

            @Override
            public Param[] newArray(int size) {
                return new Param[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(key);
            dest.writeString(value);
        }
    }

    /**
     * 验证参数
     *
     * @param params
     * @return
     */
//    private Param[] validateParam(Param[] params) {
//        if (params == null) {
//            return new Param[0];
//        } else {
//            return params;
//        }
//    }

//    private String guessMimeType(String path) {
//        FileNameMap fileNameMap = URLConnection.getFileNameMap();
//        String contentTypeFor = fileNameMap.getContentTypeFor(path);
//        if (contentTypeFor == null) {
//            contentTypeFor = "application/octet-stream";
//        }
//        return contentTypeFor;
//    }

//    private Request buildMultipartFormRequest(String url, String[] fileKeys, File[] files, Param[] params, ProgressRequestListener listener) {
//        params = validateParam(params);
//
//        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
//
//        for (Param param : params) {
//            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
//                    RequestBody.create(null, param.value));
//        }
//        if (files != null) {
//            RequestBody fileBody;
//            for (int i = 0; i < files.length; i++) {
//                File file = files[i];
//                String fileName = file.getName();
//                //fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
//                fileBody = new ProgressRequestBody(file, guessMimeType(fileName), listener);
//                builder.addPart(Headers.of("Content-Disposition",
//                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
//                        fileBody);
//            }
//        }
//
//        RequestBody requestBody = builder.build();
//        return new Request.Builder().url(url).post(requestBody).build();
//    }


    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";

    /**
     * @param params       传递的普通参数
     * @param uploadFile   需要上传的文件名
     * @param fileFormName 需要上传文件表单中的名字
     * @param newFileName  上传的文件名称，不填写将为uploadFile的名称
     * @param urlStr       上传的服务器的路径
     * @throws IOException
     */
    public void uploadFromBySocket(Map<String, String> params,
                                   String fileFormName, File uploadFile, String newFileName,
                                   String urlStr) throws IOException {
        if (newFileName == null || newFileName.trim().equals("")) {
            newFileName = uploadFile.getName();
        }

        StringBuilder sb = new StringBuilder();
        /**
         * 普通的表单数据
         */

        if (params != null)
            for (String key : params.keySet()) {
                sb.append("--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\"");
                sb.append(key);
                sb.append("\"");
                sb.append("\r\n");
                sb.append("\r\n");
                sb.append(params.get(key));
                sb.append("\r\n");
            }
        else {
            sb.append("\r\n");
        }
        /**
         * 上传文件的头
         */
        sb.append("--" + BOUNDARY + "\r\n");
        sb.append("Content-Disposition: form-data; name=\"");
        sb.append(fileFormName);
        sb.append("\"; filename=\"");
        sb.append(newFileName);
        sb.append("\"");
        sb.append("\r\n");
        sb.append("Content-Type: image/jpeg" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
        sb.append("\r\n");

        byte[] headerInfo = sb.toString().getBytes("UTF-8");
        byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

        System.out.println(sb.toString());

        URL url = new URL(urlStr);
        Socket socket = new Socket(url.getHost(), url.getPort());
        socket.setSoTimeout(1000);
        OutputStream os = socket.getOutputStream();
        PrintStream ps = new PrintStream(os, true, "UTF-8");

        // 写出请求头
        ps.println("POST " + urlStr + " HTTP/1.1");
        ps.println("Content-Type: multipart/form-data; boundary=" + BOUNDARY);
        ps.println("Content-Length: "
                + String.valueOf(headerInfo.length + uploadFile.length()
                + endInfo.length));
        ps.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        InputStream in = new FileInputStream(uploadFile);
        // 写出数据
        os.write(headerInfo);

        byte[] buf = new byte[1024 * 4];
        int len;
        while ((len = in.read(buf)) != -1)
            os.write(buf, 0, len);

        os.write(endInfo);

        in.close();
        os.close();
    }

    public static boolean connect(String host, int port) {
        if (port == 0) port = 80;
        Socket connect = new Socket();
        try {
            connect.connect(new InetSocketAddress(host, port), 10 * 1000);
            return connect.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void sclean() {
        if (clientManager != null) {
            clientManager.clean();
            OkHttpClientManagerNew.sclean();
        }
        clientManager = null;
    }
}
