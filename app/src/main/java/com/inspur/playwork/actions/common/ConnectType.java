package com.inspur.playwork.actions.common;

/**
 * Created by Fan on 15-11-18.
 */
public interface ConnectType {

    int CONNECT_FROM_SERVICE_START = 1; //service 开启时候的连接socket

    int CONNECT_FROM_LOGIN = 2;//从登录页面过去的连接

    int CONNECT_FROM_WELCOME = 3;//从欢迎界面过去的连接

    int CONNECT_FROM_RECONNECT = 4;//从重练过去的连接

    int CONNECT_FROM_NETWORK_CHANGE = 5;//从网络状态变化过去的连接

    int DISCONNECT_WHEN_NET_TYPE_CHANGE = 6;

    int DISCONNECT_WHEN_SERVICE_DESTORY = 7;

    int DISCONNECT_WHEN_EXIT_LOGIN = 8;

    int DISCONNECT_WHEN_EXIT_APP = 8;

    int LOCAL_NET = 100;

    int WIDE_NET = 101;
}
