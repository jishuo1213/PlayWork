package com.inspur.playwork.utils.loadfile;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.inspur.playwork.model.common.DownloadRequest;
import com.inspur.playwork.utils.OkHttpClientManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Response;

/**
 * Created by fan on 16-4-1.
 */
public class LoadFileHandlerThread extends HandlerThread implements Handler.Callback, FileLoader {

    private static final String TAG = "LoadFileHandlerThread";

    private static final int UPLOAD_FILE = 1;
    private static final int DOWNLOAD_FILE = 2;

    public static final int UPDATE_UPLOAD_PROGRESS = 3;
    public static final int UPDATE_DOWNLOAD_PROGRESS = 8;

    public static final int UPLOAD_SUCCESS = 4;
    public static final int UPLOAD_FAILURE = 5;

    public static final int DOWNLOAD_SUCCESS = 6;
    public static final int DOWNLOAD_FAILURE = 7;

    private static final String UPLOAD_FILE_PATH = "localfilepath";
    private static final String UPLOAD_FILE_SERVER_URL = "serverurl";
    private static final String UPLOAD_FILE_PARAMS = "upload_params";

    private static final String DOWNLOAD_FILE_URL = "down_load_url";
    public static final String DOWNLOAD_FILE_SAVE_PATH = "down_load_save_path";

    public static final String UPLOAD_SUCCESS_RESPONSE = "response";

    private static final String IS_NEED_PROGRESS = "isneedprogress";

    private static final String MANAGER_UUID = "manager_uuid";

    private Handler loadFileHandler;

    private ArrayMap<Object, Handler> clientHandlerMap;

    private Set<String> downLoadFiles;

    private ArrayMap<String, ArrayList<DownloadRequest>> requstMap;

    public LoadFileHandlerThread(String name) {
        super(name);
        clientHandlerMap = new ArrayMap<>();
        downLoadFiles = new HashSet<>();
        requstMap = new ArrayMap<>();
    }


    public void init() {
        start();
        Looper looper = getLooper();
        loadFileHandler = new Handler(looper, this);
    }


//    void upLoadFile(String url, String filePath, OkHttpClientManager.Param[] params, Object clientId, Handler responesHandler, boolean isNeedProgress, int type) {
//        Message msg = loadFileHandler.obtainMessage(UPLOAD_FILE);
//        Bundle bundle = new Bundle();
//        bundle.putString(UPLOAD_FILE_SERVER_URL, url);
//        bundle.putString(UPLOAD_FILE_PATH, filePath);
//        bundle.putBoolean(IS_NEED_PROGRESS, isNeedProgress);
//        bundle.putParcelableArray(UPLOAD_FILE_PARAMS, params);
//        Log.i(TAG, "upLoadFile: " + (loadFileHandler == responesHandler));
//        clientHandlerMap.put(clientId, responesHandler);
//        msg.setData(bundle);
//        msg.obj = clientId;
//        msg.arg1 = type;
//        loadFileHandler.sendMessage(msg);
//    }

//    void downLoadFile(String downLoadUrl, String saveFilePath, Object clientId, Handler responesHandler, boolean isNeedProgress, int type) {
//        if (downLoadFiles.contains(saveFilePath)) {
//            return;
//        }
//        downLoadFiles.add(saveFilePath);
//        Message msg = loadFileHandler.obtainMessage(DOWNLOAD_FILE);
//        Bundle bundle = new Bundle();
//        bundle.putString(DOWNLOAD_FILE_URL, downLoadUrl);
//        bundle.putString(DOWNLOAD_FILE_SAVE_PATH, saveFilePath);
//        bundle.putBoolean(IS_NEED_PROGRESS, isNeedProgress);
//        clientHandlerMap.put(clientId, responesHandler);
//        Log.d(TAG, "downLoadFile() called with: " + "downLoadUrl = [" + downLoadUrl + "], saveFilePath = [" + saveFilePath + "], clientId = [" + clientId + "], isNeedProgress = [" + isNeedProgress + "]");
//        msg.setData(bundle);
//        msg.obj = clientId;
//        msg.arg1 = type;
//        loadFileHandler.sendMessage(msg);
//    }

    /**
     * 取消机制，还需完善
     */
    private void cancelMessage(int what, Object clientId) {
        loadFileHandler.removeMessages(what, clientId);
//        loadFileHandler.removeCallbacks(new Ru);
    }


    /**
     * 处理上传和下载的请求，发送消息的obj都是客户端标识,
     * 消息返回时也会在message的obj中加入客户端标识
     * 更新进度的消息上传和下载的obj都是传过来的下载和上传的标识，
     */

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case UPLOAD_FILE: {
                final Object clientId = msg.obj;
                final int loadType = msg.arg1;
                final Handler target = clientHandlerMap.get(clientId);
                Log.i(TAG, "handleMessage: " + (target == loadFileHandler));
                Bundle requestData = msg.peekData();
                String uploadUrl = requestData.getString(UPLOAD_FILE_SERVER_URL);
                String filePath = requestData.getString(UPLOAD_FILE_PATH);
                boolean isNeedProgress = requestData.getBoolean(IS_NEED_PROGRESS);
                assert filePath != null;
                OkHttpClientManager.Param[] params = (OkHttpClientManager.Param[]) msg.getData().getParcelableArray(UPLOAD_FILE_PARAMS);
                try {
                    Response response;
                    if (isNeedProgress)
                        response = OkHttpClientManager.getInstance().postSync(uploadUrl, "uploadfile", new File(filePath), params, new ProgressRequestListener() {
                            @Override
                            public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                                double percent = (double) bytesWritten / contentLength;
                                Message progressMsg = target.obtainMessage(UPDATE_UPLOAD_PROGRESS);
                                progressMsg.obj = clientId;
                                progressMsg.arg1 = (int) (percent * 100);
                                progressMsg.arg2 = loadType;
                                target.sendMessage(progressMsg);
                                Log.i(TAG, "onRequestProgress: " + progressMsg.arg1);
                            }
                        });
                    else
                        response = OkHttpClientManager.getInstance().postSync(uploadUrl, "uploadfile", new File(filePath), params, null);
                    Log.i(TAG, "handleMessage: " + response.isSuccessful());
                    if (response.isSuccessful()) {
                        Bundle bundle = new Bundle();
                        bundle.putString(UPLOAD_SUCCESS_RESPONSE, response.body().string());
                        Message res = target.obtainMessage(UPLOAD_SUCCESS);
                        res.obj = clientId;
                        res.arg2 = loadType;
                        res.setData(bundle);
                        clientHandlerMap.remove(clientId);
                        target.sendMessage(res);
                    } else {
                        Message res = target.obtainMessage(UPLOAD_FAILURE);
                        res.obj = clientId;
                        res.arg2 = loadType;
                        clientHandlerMap.remove(clientId);
                        target.sendMessage(res);
                    }
                } catch (IOException e) {
                    Message res = target.obtainMessage(UPLOAD_FAILURE);
                    res.obj = clientId;
                    res.arg2 = loadType;
                    clientHandlerMap.remove(clientId);
                    target.sendMessage(res);
                }
                return true;
            }
            case DOWNLOAD_FILE: {
                final Object clientId = msg.obj;
                final int loadType = msg.arg1;
                Bundle requestData = msg.peekData();
                String uploadUrl = requestData.getString(DOWNLOAD_FILE_URL);
                String filePath = requestData.getString(DOWNLOAD_FILE_SAVE_PATH);
                String uuid = requestData.getString(MANAGER_UUID);
                boolean isNeedProgress = requestData.getBoolean(IS_NEED_PROGRESS);
                final Handler target = clientHandlerMap.get(clientId);
                DownloadRequest request = getRequstByClientId(uuid, (String) clientId);
                Log.i(TAG, "handleMessage: " + (request == null));
                if (request == null) {
                    return true;
                }
                request.status = DownloadRequest.Status.RUNNING;
                boolean result;
                if (isNeedProgress)
                    result = OkHttpClientManager.getInstance().downloadFile(uploadUrl, filePath, new ProgressResponseListener() {
                        @Override
                        public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                            if (contentLength > 0) {
                                double percent = (double) bytesRead / contentLength;
                                Message progressMsg = target.obtainMessage(UPDATE_DOWNLOAD_PROGRESS);
                                progressMsg.obj = clientId;
                                progressMsg.arg1 = (int) (percent * 100);
                                progressMsg.arg2 = loadType;
                                target.sendMessage(progressMsg);
                            } else {
                                Message progressMsg = target.obtainMessage(UPDATE_DOWNLOAD_PROGRESS);
                                progressMsg.obj = clientId;
                                progressMsg.arg1 = -100;
                                target.sendMessage(progressMsg);
                            }
                        }
                    });
                else
                    result = OkHttpClientManager.getInstance().downloadFile(uploadUrl, filePath, null);
                if (result) {
                    request.status = DownloadRequest.Status.COMPLETE;
                    Bundle bundle = new Bundle();
                    bundle.putString(DOWNLOAD_FILE_SAVE_PATH, filePath);
                    Message res = target.obtainMessage(DOWNLOAD_SUCCESS);
                    res.setData(bundle);
                    res.obj = clientId;
                    res.arg2 = loadType;
//                    clientHandlerMap.remove(clientId);
                    target.sendMessage(res);
                } else {
                    request.status = DownloadRequest.Status.FAILED;
                    Message res = target.obtainMessage(DOWNLOAD_FAILURE);
                    res.obj = clientId;
                    res.arg2 = loadType;
                    target.sendMessage(res);
                }
                clientHandlerMap.remove(clientId);
                downLoadFiles.remove(filePath);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clean() {
        loadFileHandler.removeMessages(DOWNLOAD_FILE);
        loadFileHandler.removeMessages(UPLOAD_FILE);

        if (clientHandlerMap != null)
            clientHandlerMap.clear();
        if (downLoadFiles != null)
            downLoadFiles.clear();
        clientHandlerMap = null;
        downLoadFiles = null;

        requstMap.clear();
        requstMap = null;
    }

    @Override
    public void onStart(String uuid) {
        Log.i(TAG, "onStart: " + uuid);
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        if (requestArrayList.size() > 0) {
            for (DownloadRequest request : requestArrayList) {
                Log.i(TAG, "onStart: " + request.status);
                if (request.status == DownloadRequest.Status.PAUSE) {
                    switch (request.loadType) {
                        case DOWNLOAD_FILE:
                            request.status = DownloadRequest.Status.PENDING;
                            downloadFile(uuid, request);
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onStop(String uuid) {
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        if (requestArrayList.size() > 0) {
            for (DownloadRequest request : requestArrayList) {
                if (request.status == DownloadRequest.Status.PENDING) {
                    cancelMessage(request.loadType, request.clientId);
                    request.status = DownloadRequest.Status.PAUSE;
                    downLoadFiles.remove(request.savePath);
                }
            }
        }
    }

    @Override
    public void onDestory(String uuid) {
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        if (requestArrayList.size() > 0) {
            for (DownloadRequest request : requestArrayList) {
                if (request.status == DownloadRequest.Status.PENDING) {
                    loadFileHandler.removeMessages(request.loadType, request.clientId);
                    downLoadFiles.remove(request.savePath);
                }
                if (request.status == DownloadRequest.Status.RUNNING) {
                    OkHttpClientManager.getInstance().cancelDownload(request.downloadUrl, request.savePath);
                    downLoadFiles.remove(request.savePath);
                }
            }
        }
        requestArrayList.clear();
        requstMap.remove(uuid);
        downLoadFiles.clear();
    }

    @Override
    public void addNewManager(String uuid) {
        requstMap.put(uuid, new ArrayList<DownloadRequest>());
    }

    @Override
    public void downloadFile(String uuid, DownloadRequest request) {
        request.loadType = DOWNLOAD_FILE;
        request.uuid = uuid;
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        if (requestArrayList != null) {
            if (downLoadFiles.contains(request.savePath)) {
                return;
            }
//            if (!requestArrayList.contains(request))
            Log.i(TAG, "downloadFile: " + request.downloadUrl);
            if (!requestArrayList.contains(request))
                requestArrayList.add(request);
            downLoadFiles.add(request.savePath);
            Message msg = loadFileHandler.obtainMessage(DOWNLOAD_FILE);
            Bundle bundle = new Bundle();
            bundle.putString(DOWNLOAD_FILE_URL, request.downloadUrl);
            bundle.putString(DOWNLOAD_FILE_SAVE_PATH, request.savePath);
            bundle.putBoolean(IS_NEED_PROGRESS, request.isNeedProgress);
            bundle.putString(MANAGER_UUID, request.uuid);
            clientHandlerMap.put(request.clientId, request.responseHandler);
            Log.d(TAG, "downloadFile() called with: uuid = [" + uuid + "], request = [" + request + "]");
            msg.setData(bundle);
            msg.obj = request.clientId;
            msg.arg1 = request.type;
            loadFileHandler.sendMessage(msg);
        } else {
            throw new IllegalStateException("must call addNew manager");
        }
    }

    @Override
    public void uploadFile(String uuid, DownloadRequest request) {
        request.loadType = UPLOAD_FILE;
        request.uuid = uuid;
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        if (requestArrayList != null) {
            Message msg = loadFileHandler.obtainMessage(UPLOAD_FILE);
            Bundle bundle = new Bundle();
            bundle.putString(UPLOAD_FILE_SERVER_URL, request.downloadUrl);
            bundle.putString(UPLOAD_FILE_PATH, request.savePath);
            bundle.putBoolean(IS_NEED_PROGRESS, request.isNeedProgress);
            bundle.putParcelableArray(UPLOAD_FILE_PARAMS, request.params);
            bundle.putString(MANAGER_UUID, request.uuid);
            clientHandlerMap.put(request.clientId, request.responseHandler);
            msg.setData(bundle);
            msg.obj = request.clientId;
            msg.arg1 = request.type;
            loadFileHandler.sendMessage(msg);
        } else {
            throw new IllegalStateException("must call addNew manager");
        }
    }

//    private void removeRequestByClientId(String uuid, String clientId) {
//        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
//        Iterator<DownloadRequest> it = requestArrayList.iterator();
//        while (it.hasNext()) {
//            DownloadRequest request = it.next();
//            if (request.clientId.equals(clientId)) {
//                it.remove();
//            }
//        }
//    }

    private DownloadRequest getRequstByClientId(String uuid, String clientId) {
        ArrayList<DownloadRequest> requestArrayList = requstMap.get(uuid);
        for (DownloadRequest request : requestArrayList) {
            if (request.clientId.equals(clientId)) {
                return request;
            }
        }
        return null;
    }
}
