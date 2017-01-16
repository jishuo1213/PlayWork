package com.inspur.playwork.actions.network;

/**
 * Created by Fan on 15-9-1.
 * 来自网络的事件类型，2开头
 */
public interface NetWorkActions {

    /**
     * 连接到服务器
     */
    int CONNECT_TO_TIMELINE_SERVER_SUCCESS = 200;

    /**
     * 从服务器断开连接
     */
    int DISCONNECT_TO_TIMELINE_SERVER = 201;

    /**
     * 开始重连服务器
     */
    int RECONNECT_TO_TIMELINE_SERVER = 203;

    /**
     * 连接到服务器超时
     */
    int CONNECT_TO_TIMELINE_SERVER_TIME_OUT = 204;


    /**
     * 发送登录时间轴服务器请求
     */
    int SEND_CONNECT_TO_TIMELINE_SERVER = 205;

    /**
     * 网络状态发生变化
     */
    int NET_WORK_STATE_CHANGE = 206;

    /**
     * 更新头像map
     */
    int USER_AVATAR_DOWNLOADED = 207;
}
