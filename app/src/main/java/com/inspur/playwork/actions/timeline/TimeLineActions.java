package com.inspur.playwork.actions.timeline;

/**
 * Created by fan on 15-8-22.
 * 时间轴的一些事件类型，3开头
 */
public interface TimeLineActions {


    /**
     * 初始化时间轴任务列表 用户刚进入应用时
     */
    int TIME_LINE_INIT_TASK_LIST = 301;

    /**
     * 点击某天时加载当天的任务列表
     */
    int TIME_LINE_REQUEST_TASK_LIST = 302;

    /**
     * 加载任务列表超时，显示超时的UI
     */

    int TIME_LINE_GET_TASK_TIME_OUT = 303;

    /**
     * 新建时间轴任务
     */

    int TIME_LINE_ADD_EDIT_TASK = 304;

    /**
     * 新建任务成功
     */
    int TIME_LINE_ADD_EDIT_TASK_SUCCESS = 305;


    /**
     * 新建任务失败
     */
    int TIME_LINE_ADD_EDIT_TASK_FAIL = 306;

    /**
     * 退出任务
     */
    int TIME_LINE_QUIT_TASK = 307;

    /**
     * 删除任务
     */
    int TIME_LINE_DELETE_TASK = 308;

    /**
     * 退出任务
     */
    int TIME_LINE_QUIT_TASK_SUCCESS = 309;

    /**
     * 退出任务
     */
    int TIME_LINE_QUIT_TASK_FAILED = 310;


    /**
     * 删除任务
     */
    int TIME_LINE_DELETE_TASK_SUCCESS = 311;

    /**
     * 删除任务
     */
    int TIME_LINE_DELETE_TASK_FAILED = 312;

    /**
     * 获取任务附件列表
     */
    int TIME_LINE_GET_TASK_ATTACH_LIST = 313;

    /**
     * 根据任务ID 获取聊天窗口信息
     */
    int TIME_LINE_GET_WINDOW_INFO = 314;

    /**
     * 获取聊天窗口信息成功
     */
    int TIME_LINE_RECIVE_GROUP_INFO_WINDOW = 315;

    /**
     * 获取未读消息
     */
    int TIME_LINE_GET_UNREAD_MESSAGE = 316;

    /**
     * 获取到未读消息
     */
    int TIME_LINE_RECIVE_UNREAD_MESSAGE = 317;

    /**
     * 设置时间轴任务未读消息个数
     */
    int TIME_LINE_SET_UNREAD_MSG = 318;

    /**
     * 设置任务时间
     */
    int TIME_LINE_CHANGE_TASK_TIME = 319;

    /**
     * 设置时间成功
     */
    int TIME_LINE_CHANGE_TASK_TIME_SUCCESS = 320;

    /**
     * 设置时间失败
     */
    int TIME_LINE_CHANGE_TASK_TIME_FAILED = 321;

    /**
     * 改变任务排序
     */
    int TIME_LINE_SORT_TASK_NUM = 322;

    /**
     * 时间轴一端读了消息，另一端设置为已读
     */
    int TIME_LINE_SET_MESSAGE_TO_READ = 323;

    /**
     * 时间轴修改任务名称
     */
    int TIME_LINE_UPDATE_TASK_NAME = 324 ;

    /**
     * 电脑端新加一个任务
     */
    int TIME_LINE_ADD_NEW_TASK = 325;

}
