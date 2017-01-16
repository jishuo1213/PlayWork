package com.inspur.playwork.model.common;

import android.os.Handler;

import com.inspur.playwork.utils.OkHttpClientManager;

/**
 * Created by fan on 16-11-15.
 */
public class DownloadRequest {
    private static final String TAG = "DownloadRequest";

    public enum Status {
        /**
         * Created but not yet running.
         */
        PENDING,
        /**
         * In the process of fetching media.
         */
        RUNNING,
        /**
         * Finished loading media successfully.
         */
        COMPLETE,
        /**
         * Failed to load media, may be restarted.
         */
        FAILED,
        /**
         * Stop
         */
        PAUSE,
    }

    public String uuid;
    public String downloadUrl;
    public String savePath;
    public Object clientId;
    public Handler responseHandler;
    public boolean isNeedProgress;
    public int type;
    public int loadType;
    public Status status;
    public OkHttpClientManager.Param[] params;


    public DownloadRequest(String downloadUrl, String savePath, Object clientId, Handler responseHandler, boolean isNeedProgress, int type) {
        this.downloadUrl = downloadUrl;
        this.savePath = savePath;
        this.clientId = clientId;
        this.responseHandler = responseHandler;
        this.isNeedProgress = isNeedProgress;
        this.type = type;
        status = Status.PENDING;
    }

    @Override
    public String toString() {
        return "DownloadRequest{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", savePath='" + savePath + '\'' +
                ", clientId=" + clientId +
                ", responseHandler=" + responseHandler +
                ", isNeedProgress=" + isNeedProgress +
                ", type=" + type +
                '}';
    }
}
