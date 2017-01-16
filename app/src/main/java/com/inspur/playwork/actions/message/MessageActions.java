package com.inspur.playwork.actions.message;

/**
 * 消息Action
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public interface MessageActions {

    /*
    * 周計劃消息
    * */
    int MESSAGE_WEEK_PLAN = 3;

    /*
    * 任務排序
    * */
    int MESSAGE_SORT_TASK = 1002;

    /*
    * 刪除微聊
    * */
    int MESSAGE_DELETE_GROUP = 1003;

    /**
     * 自己新建任務
     */
    int MESSAGE_NEW_TASK = 1005;

    /**
     * 增加人员
     */
    int MESSAGE_ADD_MEMBER = 1007;

    /**
     * 更改任務名稱
     */
    int MESSAGE_RENAME_TASK = 1008;

    /**
     * 更改話題名稱
     */
    int MESSAGE_RENAME_CHAT = 1009;
    /*
    * 消息撤回
    * */
    int MESSAGE_RECALL = 1011;

    /*
    * 刪除消息記錄
    * */
    int MESSAGE_DELETE_ONE_MSG = 1012;

    /**
     * 聊天人员更新
     */
    int MESSAGE_REFRESH_MEMBERS = 1013;

    /**
     * 一端读了消息另一端设为已读
     */
    int MESSAGE_TO_READ = 1014;

    /*
    * 刪除任務
    * */
    int MESSAGE_DELETE_TASK = 1015;


    int MESSAGE_SYSTEM_TIP = 9999;

    /**
     * 初始化weiliao消息列表
     */
    int INIT_NORMAL_CHAT_LIST = 100;

    /**
     * 初始化群组人员列表
     */
    int INIT_CHAT_WINDOW_INFO = 101;

    /**
     * 调整群组人员列表
     */
    int SAVE_CONTACT_GROUP = 102;

    /**
     * 增加群组人员
     */
    int ADD_MEMBER = 103;

    /**
     * 根据TaskId加载消息列表
     */
    int GET_MESSAGE_LIST_BY_TASKID = 104;

    /**
     * 得到消息列表
     */
    int RECIVE_MESSAGE_LIST_BY_TASKID = 105;

    /**
     * 接收到时间轴任务聊天消息
     */
    int RECIVE_TASK_CHAT_MSG = 106;

    /**
     * 接收到普通聊天消息
     */
    int RECIVE_NORMAL_CHAT_MSG = 107;

    /**
     * 发送一条时间轴任务消息
     */
    int SEND_TASK_CHAT_MASSAGE = 108;

    /**
     * 发送一条消息
     */
    int SEND_MASSAGE_SUCCESS = 109;

    /**
     * 发送一条消息
     */
    int SEND_MASSAGE_FAILED = 110;

    /**
     * 向服务器消除未读消息
     */
    int SET_UNREAD_MSG_READ = 111;

    /**
     * 收到一条时间轴聊天消息，但是当前不在时间轴聊天界面
     */
    int RECIVE_UNREAD_TASK_CHAT_MSG = 112;

    /**
     * 收到一条微聊聊天消息，但是当前不在微聊聊天界面
     */
    int RECIVE_UNREAD_NORMAL_CHAT_MSG = 113;


    /**
     * 根据groupId获取聊天窗口信息
     */
    int GET_NORMAL_CHAT_WINDO_INFO = 115;

    /**
     * 获取到了聊天窗口信息
     */
    int GET_NORMAL_CHAT_WINDOW_RESULT = 116;

    /**
     * 获取联系人和联系组
     */
    int GET_ALL_RECENT_CONTACTS = 117;

    /**
     * 获取部门人员
     */
    int SEARCH_USER_BY_DEPT = 118;

    /**
     * 发送一条微聊消息
     */
    int SEND_NORMAL_CHAT_MASSAGE = 119;

    /**
     * 获取到微聊所有的未读消息列表
     */
    int NORMAL_CHAT_RECIVE_UNREAD_MESSAGE = 120;

    /**
     * 发送图片消息
     */
    // int SEND_IMAGE_MSG = 121;

    /**
     * 上传图片成功
     */
    int UPLOAD_IMG_SUCCESS = 122;

    /**
     * 上传图片失败
     */
    int UPLOAD_IMG_FAILED = 123;

    /**
     * 上传文件消息
     */
    int SEND_FILE_MSG = 124;

    /**
     * 上传文件成功
     */
    int UPLOAD_FILE_SUCCESS = 125;

    /**
     * 上传文件失败
     */
    int UPLOAD_FILE_FAILED = 126;

    /**
     * 发送附件消息
     */
    int SEND_ATTACHMENT_MSG = 127;

    /**
     * 搜索人员
     */
    int SEARCH_PERSON = 128;

    /**
     * 设置任务自定义属性
     */
    int SET_CUSTOM_PROPERTY = 129;

    /**
     * 获取随手记内容
     */
    int GET_NOTES_BY_GROUP_ID = 130;

    /**
     * 设置随手记内容
     */
    int SET_NOTES_BY_GROUP_ID = 131;

    /**
     * 没有更多可加载的消息了
     */
    int HAVE_NO_MORE_MESSAGE = 132;

    /**
     * 删除未读消息
     */
    int REMOVE_UNREAD_MESSAGE = 133;

    /**
     * 收到退出消息更新人员列表
     */
    int MEMBER_EXIT_MESSAGE = 134;

    /**
     * 任务共/私设置
     */
    int SET_TASK_PRIVATE = 135;

    /**
     * 任务地点设置
     */
    int SET_TASK_PLACE = 136;

    /**
     * 任务标题设置
     */
    int SET_TASK_TITLE = 137;

    /**
     * 微聊话题设置
     */
    int SET_CHAT_TITLE = 138;

    /**
     * 创建新微聊
     */
    int CREATE_NEW_CHAT = 139;

    /**
     * 修改微聊标题
     */
    int UPDATE_GROUP_NAME = 140;

    /**
     * 单聊是否存在
     */
    int IS_SINGLE_CHAT_EXISTED = 141;

    /**
     * 删除微聊某一个列表
     */
    int DELETE_VCHAT_ONE_CHAT = 142;

    /**
     * 删除某一条聊天
     */
    int DELETE_ONE_CHAT_MESSAGE = 143;

    /**
     * 撤回一条消息
     */
    int RECALL_COE_CHAT_MESSAGE = 144;

    /**
     * 一端读了消息，另一端将消息设为已读
     */
    int SET_UNREAD_MSG_TO_READ = 145;

    /**
     * 添加人员的时候更新界面
     */
    int UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER = 146;

    /**
     * 更改微聊名字
     */
    int RENAME_CHAT_SUBJECT = 147;

    /**
     * 获取到小邮详情
     */
    int GET_SMALL_MAIL_DETAIL = 148;

    // 小邮件发送成功
    int SEND_MAIL_SUCCESS = 149;

    // 小邮件发送失败
    int SEND_MAIL_FAIL = 150;

    /**
     * 打开一个聊天窗口的时候
     */
    int OPEN_ONE_CHAT_WINDOW = 151;

    /**
     * 离开一个聊天窗口
     */
    int CLOSE_ONE_CHAT_WINDOW = 152;

    /**
     * 发送消息
     */
    int SEND_ONE_MESSAGE = 153;

    /**
     * 发送到服务器之前，先显示消息
     */
    int SHOW_SEND_MSG_BEFORE_SEND_TO_SERVER = 154;

    /**
     * 发送图片参数
     */
    int SEND_PICTURE_MESSAGE = 155;

    /**
     * 上传图片之前，显示图片消息
     */
    int SHOW_IMAGE_MSG_BEFORE_UPLOAD = 156;

    /**
     * 重发消息
     */
    int RESEND_ONE_MSG = 157;

    /**
     *
     */
    int SET_VCHAT_UNREAD_NUM = 158;

}
