package com.inspur.playwork.actions.common;

/**
 * 一些通用的Actions，如获取数据超时等，-1开头
 * Created by Fan on 15-9-15.
 */
public interface CommonActions {

    /**
     * 从网络获取数据超时
     */
    int GET_TIMELINE_DATA_FROM_SERVER_TIME_OUT = -101;


    /**
     * 从服务器接收到返回数据
     */
    int REVICE_TIMELINE_DATA_FROM_SERVER = -102;

    /**
     * 向时间轴服务器发送获取数据的请求
     */
    int GET_TIMELINE_DATA_FROM_SERVER = -103;

    /**
     * 发送HTTP GET 请求
     */
    int GET_DATA_BY_HTTP_GET = -104;

    /**
     * 发送HTTP POST 请求
     */
    int GET_DATA_BY_HTTP_POST = -105;

    /**
     * 上传文件，通过httppost
     */
    int UPLOAD_FILE_BY_HTTP_POST = -106;

    /**
     * 下载文件，通过http
     */
    int DOWNLOAD_FILE_BY_HTTP = -107;


    /**
     * 发送通知
     */
    int SEND_NOTIFICATION = -108;

    /**
     * 取消所有通知
     */
    int CANCEL_ALL_NOTIFICATION = -109;

    /**
     * 与服务器断开链接
     */
    int DISCONNECT_FROM_SERVER = -110;

    /**
     * 连接到服务器
     */
    int CONNECT_SERVER = -111;
}
