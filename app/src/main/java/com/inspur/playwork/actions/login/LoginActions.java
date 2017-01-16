package com.inspur.playwork.actions.login;

/**
 * 登录actions 4开头
 * @author 笑面V客(bugcode@foxmail.com)
 */
public interface LoginActions {

    /**
     * 用户点击登录时的事件
     */
    int LOGIN_TO_ADSERVER = 400;

    /**
     * 登录成功
     */
   // int LOGIN_SUCCESS = 401;

    /**
     * 登录失败
     */
    int LOGIN_FAILED = 402;


    /**
     * 登录到外网服务器成功
     */
    int LOGIN_AD_SERVER_SUCCESS = 403;

    /**
     * 登录到时间轴服务器
     */
    int LOGIN_TIME_LINE_SERVER = 404;

    /**
     * 登录时间轴成功
     */
    int LOGIN_TIMELINE_SUCCESS = 405;
}
