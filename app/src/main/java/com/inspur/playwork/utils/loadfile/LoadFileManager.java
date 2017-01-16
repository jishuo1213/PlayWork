package com.inspur.playwork.utils.loadfile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.model.common.DownloadRequest;
import com.inspur.playwork.utils.OkHttpClientManager;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by fan on 16-4-6.
 */
public class LoadFileManager extends Fragment {

    private static final String TAG = "LoadFileManager";

    public static final int DOWNLOAD_MSG_PICTURE = 0;
    public static final int UPLOAD_MSG_PICTURE = 1;

    public static final int DOWNLOAD_CHAT_AVATAR = 2;
    public static final int UPLOAD_AVATAR = 3;

    public static final int DOWNLOAD_MSG_ATTACHMENT = 4;
    public static final int UPLOAD_MSG_ATTACHMENT = 5;


    private FileLoader loadFileHandlerThread;

    private String uuid;


    public static LoadFileManager findOrCreateRetainFragment(FragmentManager fm) {
        LoadFileManager loadFileManager = (LoadFileManager) fm.findFragmentByTag(TAG);
        if (loadFileManager == null) {
            loadFileManager = new LoadFileManager();
            fm.beginTransaction().add(loadFileManager, TAG).commit();
        }
        return loadFileManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: =======================");
        loadFileHandlerThread = ((PlayWorkApplication) getActivity().getApplication()).getLoadFileHandlerThread();
        uuid = UUID.randomUUID().toString();
        loadFileHandlerThread.addNewManager(uuid);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ===================");
        loadFileHandlerThread.onStart(uuid);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: =====================");
        loadFileHandlerThread.onStop(uuid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: =====================");
        loadFileHandlerThread.onDestory(uuid);
    }

    /**
     * 下载文件入口
     *
     * @param downLoadUrl
     * @param saveFilePath
     * @param clientId
     * @param responesHandler
     * @param isNeedProgress
     * @param type
     */
    public void downLoadFile(String downLoadUrl, String saveFilePath, Object clientId, Handler responesHandler, boolean isNeedProgress, int type) {
        DownloadRequest request = new DownloadRequest(downLoadUrl, saveFilePath, clientId, responesHandler, isNeedProgress, type);
        loadFileHandlerThread.downloadFile(uuid, request);
    }

    public void upLoadFile(String url, String filePath, OkHttpClientManager.Param[] params, Object clientId, Handler responesHandler, boolean isNeedProgress, int type) {
        DownloadRequest request = new DownloadRequest(url, filePath, clientId, responesHandler, isNeedProgress, type);
        request.params = params;
        loadFileHandlerThread.uploadFile(uuid, request);
    }
}
