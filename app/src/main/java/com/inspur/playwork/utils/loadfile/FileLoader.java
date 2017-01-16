package com.inspur.playwork.utils.loadfile;

import com.inspur.playwork.model.common.DownloadRequest;

/**
 * Created by fan on 16-11-14.
 */

public interface FileLoader {


    /**
     * 初始化
     */
    void init();

    /**
     * 对应view onStart()生命周期
     *
     * @param uuid
     */
    void onStart(String uuid);


    /**
     * 对应view onStop()生命周期
     *
     * @param uuid
     */
    void onStop(String uuid);


    /**
     * 对应view onDesotry()生命周期
     *
     * @param uuid
     */
    void onDestory(String uuid);


    /**
     * 一个新的 LoadFileManger 被创建，此时要创建属于它的消息队列
     *
     * @param uuid
     */
    void addNewManager(String uuid);


    /**
     * 下载请求
     *
     * @param uuid
     * @param request
     */
    void downloadFile(String uuid, DownloadRequest request);

    /**
     * 上传请求
     *
     * @param uuid
     * @param request
     */
    void uploadFile(String uuid, DownloadRequest request);

    /**
     *清理
     */
    void clean();
}
