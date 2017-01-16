package com.inspur.playwork.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.utils.loadfile.ProgressRequestListener;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.FileNameMap;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;

/**
 * okhttp工具类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class OkHttpClientManagerNew {

    private static final String TAG = "OkHttpClientManager";

    // 请求数据json类型
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient mOkHttpClient;

    private ArrayList<String> downLoadingUrls;

    private String token;


    private static OkHttpClientManagerNew clientManager = new OkHttpClientManagerNew();

    public static OkHttpClientManagerNew getInstance() {
        if (clientManager == null)
            clientManager = new OkHttpClientManagerNew();
        return clientManager;
    }

    private OkHttpClientManagerNew() {
        initHttpClient();
        downLoadingUrls = new ArrayList<>();
        callArrayMap = new ArrayMap<>();
    }

    private void initHttpClient() {
        if (mOkHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            mOkHttpClient = builder.connectTimeout(8, TimeUnit.SECONDS)
                    .build();
        }
    }

    public void clean() {
        mOkHttpClient = null;
    }


    public void setToken(String token) {
        this.token = token;
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

    /**
     * okhttp sync get request
     *
     * @param url API
     * @return
     */
    public String get(String url, String param) {
        String result = null;
        try {
            RequestBody body = RequestBody.create(JSON_TYPE, param);
            Request request = new Request.Builder().url(url).put(body).build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * okhttp async get request
     *
     * @param url              API
     * @param responseCallback 回调方法
     */
    public void getAsyn(String url, Callback responseCallback) {
        Log.i(TAG, "getAsyn: " + url);
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public void getAsyn(String url, Callback responseCallback, JSONObject jsonParam, String requestId) {
        Iterator<String> keys = jsonParam.keys();
        url += "?";
        Log.i(TAG, "getAsyn: " + jsonParam.toString());
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonParam.optString(key);
            url = url + "&" + key + "=" + value;
        }

        if (!TextUtils.isEmpty(token)) {
            url = url + "&token=" + token;
        }
//        RequestBody body = RequestBody.create(JSON_TYPE, jsonParam.toString());
        Request.Builder builder = new Request.Builder();
        if (!TextUtils.isEmpty(token)) {
            builder.header("token", token);
        }
        if (!TextUtils.isEmpty(requestId))
            builder.header("requestId", requestId);
        Log.i(TAG, "getAsyn: " + url);
        Request request = builder.get().url(url).build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }


    public OkHttpClient getmOkHttpClient() {
        return mOkHttpClient;
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

    /**
     * okhttp async post request
     *
     * @param url             API
     * @param jsonStr         请求参数json字符串
     * @param requestCallback 回调方法
     */
    public void post(String url, JSONObject jsonStr, Callback requestCallback, String requestId) {
        Log.i(TAG, "post: " + url + jsonStr.toString());
        if (!TextUtils.isEmpty(token)) {
            try {
                jsonStr.put("token", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonStr.toString());
        Log.i(TAG, "post: " + jsonStr.toString());
        Request.Builder builder = new Request.Builder();
        if (!TextUtils.isEmpty(requestId))
            builder.header("requestId", requestId);
        Request request = builder.url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(requestCallback);
    }


    /*下载文件
    *  @method downloadFile
    *  @param String url, String fileName String dir (文件保存在那个目录下)
    * */
    void downloadFileUseFirst(final String url, final String dir, final @NonNull String fileName) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i(TAG, "下载文件失败了");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    if (response.isSuccessful()) {
                        is = response.body().byteStream();
                        File file = FileUtil.createFileInSDCard(dir, fileName);

                        fos = new FileOutputStream(file);
                        byte[] b = new byte[4 * 1024];
                        int charb;
                        int count = 0;
                        while ((charb = is.read(b)) != -1) {
                            fos.write(b, 0, charb);
                            count += charb;
                        }
                        fos.flush();
                        Log.i(TAG, "下载文件成功了 count = " + count);

                    }
                } finally {
                    Util.closeQuietly(is);
                    Util.closeQuietly(fos);
                }
            }
        });
    }


    boolean downloadFile(final String url, final String fileName) throws IOException {

        Log.i(TAG, "downLoad url" + url);
        if (downLoadingUrls.contains(url)) {
            return false;
        }
        InputStream is = null;
        FileOutputStream fos = null;

        downLoadingUrls.add(url);
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                is = response.body().byteStream();
                File file = new File(fileName);
//                file.createNewFile();
                fos = new FileOutputStream(file);
                byte[] b = new byte[4 * 1024];
                int charb;
                while ((charb = is.read(b)) != -1) {
                    fos.write(b, 0, charb);
                }
                fos.flush();
                return true;
            } else {
                return false;
            }
        } finally {
            downLoadingUrls.remove(url);
            Util.closeQuietly(is);
            Util.closeQuietly(fos);
        }
    }

    private ArrayMap<String, Call> callArrayMap;

    boolean downloadFile(final String url, final String fileName, ProgressResponseListener listener) {

        Log.d(TAG, "downloadFile() called with: url = [" + url + "], fileName = [" + fileName + "], listener = [" + listener + "]");
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Request request = new Request.Builder().url(url).build();
            Call downloadCall = mOkHttpClient.newCall(request);
            callArrayMap.put(url + fileName, downloadCall);
            Response response = downloadCall.execute();
            if (response.isSuccessful()) {
                String headerFileSize = response.header("File-Size");
                if (headerFileSize == null) {
                    headerFileSize = response.header("Content-length");
                    if (headerFileSize == null)
                        headerFileSize = "-1";
                }
                Log.i(TAG, "downloadFile: " + headerFileSize);
                long fileSize = Long.parseLong(headerFileSize);
                long downLoaded = 0;

                if (listener != null)
                    listener.onResponseProgress(0, fileSize, false);
                is = response.body().byteStream();
                File file = new File(fileName + ".temp");
                fos = new FileOutputStream(file);
                byte[] b = new byte[4 * 1024];
                int charb;
                long notifyTime = System.currentTimeMillis();
//                long readedNotify = 0;
                while ((charb = is.read(b)) != -1) {
                    downLoaded += charb;
//                    readedNotify += charb;
                    fos.write(b, 0, charb);
                    if (listener != null) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - notifyTime >= 500) {
                            listener.onResponseProgress(downLoaded, fileSize, downLoaded == fileSize);
                            notifyTime = currentTime;
                        } else if (downLoaded == fileSize) {
                            listener.onResponseProgress(downLoaded, fileSize, true);
                        }
                    }
                }
                fos.flush();
                callArrayMap.remove(url + fileName);
                FileUtil.renameFileNew(fileName + ".temp", fileName);
                return true;
            } else {
                Log.i(TAG, "downloadFile: " + response.code());
                File file = new File(fileName);
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                callArrayMap.remove(url + fileName);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            File file = new File(fileName + ".temp");
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            callArrayMap.remove(url + fileName);
            return false;
        } finally {
            Util.closeQuietly(is);
            Util.closeQuietly(fos);
        }
    }

    void cancelDownload(String url, String fileName) {
        Call call = callArrayMap.get(url + fileName);
        if (call != null)
            call.cancel();
        callArrayMap.remove(url + fileName);
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
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                Call call = mOkHttpClient.newCall(request);
                InputStream inputStream = null;
                FileOutputStream fos = null;
                try {
                    Response response = call.execute();
                    byte[] buf = new byte[1024 * 4];
                    inputStream = response.body().byteStream();
                    fos = new FileOutputStream(new File(filePath));
                    long downloaded = 0;
                    String size = response.header("File-Size");
                    if (TextUtils.isEmpty(size)) {
                        callback.onFailure(call, null);
                        return;
                    }
                    long target = Long.parseLong(size);
                    if (listener != null)
                        listener.onResponseProgress(0, target, false);
                    int readed;
                    long reportSize = target / 100;
                    long reportReaded = 0;
                    while ((readed = inputStream.read(buf)) != -1) {
                        downloaded += readed;
                        reportReaded += readed;
                        fos.write(buf, 0, readed);
                        if (listener != null) {
                            if (reportReaded >= reportSize && downloaded != target) {
                                listener.onResponseProgress(downloaded, target, false);
                                reportReaded = 0;
                            } else if (downloaded == target) {
                                listener.onResponseProgress(downloaded, target, true);
                            }
                        }
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
     * 异步基于post的文件上传:主方法
     */
    private void postAsyn(String url, String[] fileKeys, File[] files, OkHttpClientManager.Param[] params, Callback callback, ProgressRequestListener listener) {
        Request request = buildMultipartFormRequest(url, fileKeys, files, params, listener);
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    private Response postSync(String url, String[] fileKeys, File[] files, OkHttpClientManager.Param[] params, ProgressRequestListener listener) throws IOException {
        Request request = buildMultipartFormRequest(url, fileKeys, files, params, listener);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     */
    void postAsyn(String url, String fileKey, File file, OkHttpClientManager.Param[] params, Callback callback, ProgressRequestListener listener) {
        postAsyn(url, new String[]{fileKey}, new File[]{file}, params, callback, listener);
    }

    Response postSync(String url, String fileKey, File file, OkHttpClientManager.Param[] params, ProgressRequestListener listener) throws IOException {
        return postSync(url, new String[]{fileKey}, new File[]{file}, params, listener);
    }

    /**
     * 参数类
     */
//    public static class Param implements Parcelable {
//        public String key;
//        public String value;
//
//        public Param(String key, String value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        protected Param(Parcel in) {
//            key = in.readString();
//            value = in.readString();
//        }
//
//        public static final Creator<Param> CREATOR = new Creator<Param>() {
//            @Override
//            public Param createFromParcel(Parcel in) {
//                return new Param(in);
//            }
//
//            @Override
//            public Param[] newArray(int size) {
//                return new Param[size];
//            }
//        };
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeString(key);
//            dest.writeString(value);
//        }
//    }

    /**
     * 验证参数
     *
     * @param params
     * @return
     */
    private OkHttpClientManager.Param[] validateParam(OkHttpClientManager.Param[] params) {
        if (params == null) {
            return new OkHttpClientManager.Param[0];
        } else {
            return params;
        }
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    private Request buildMultipartFormRequest(String url, String[] fileKeys, File[] files, OkHttpClientManager.Param[] params, ProgressRequestListener listener) {
        params = validateParam(params);

//        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);

        for (OkHttpClientManager.Param param : params) {

            bodyBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null, param.value));
        }
        if (files != null) {
            RequestBody fileBody;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                //fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                fileBody = new ProgressRequestBody(file, guessMimeType(fileName), listener);
                bodyBuilder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }

        RequestBody requestBody = bodyBuilder.build();
        return new Request.Builder().url(url).post(requestBody).build();
    }


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
        if (clientManager != null)
            clientManager.clean();
        clientManager = null;
    }


    private static class ProgressRequestBody extends RequestBody {

        private static final String TAG = "ProgressRequestBody";

        private static final int SEGMENT_SIZE = 4096; // okio.Segment.SIZE

        private final File file;

        private final String contentType;

        private final ProgressRequestListener progressListener;

        private long contentLength;

        public ProgressRequestBody(File file, String contentType, ProgressRequestListener listener) {
            this.file = file;
            this.contentType = contentType;
            this.progressListener = listener;
        }

        @Override
        public long contentLength() {
            contentLength = file.length();
            return contentLength;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(contentType);
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(file);
                long total = 0;
                long read;

//                long reportedSize = contentLength / 50;
//                long writeBytes = 0;

                long notifyTime = System.currentTimeMillis();

                while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                    total += read;
                    sink.flush();
//                    writeBytes += read;
                    if (progressListener != null) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - notifyTime >= 500) {
                            progressListener.onRequestProgress(total, contentLength, false);
                            notifyTime = currentTime;
//                            writeBytes = 0;
                        } else if (total == contentLength) {
                            progressListener.onRequestProgress(total, contentLength, true);
                        }
                    }
                }
            } finally {
                Util.closeQuietly(source);
            }
        }
    }
}
