package com.inspur.playwork.actions.db;

/**
 * 数据库操作，5开头
 * Created by Fan on 15-11-5.
 */
public interface DataBaseActions {

    /**
     * 根据groupId获取聊天记录，分页加载，还需要传入一个时间点
     * 当时间点为0时表示第一次进入聊天页面,此时将加载最后10条消息
     * 返回值是一个messagebean 的list
     */
    int GET_GROUP_CHAT_HISTORY = 500;

    /**
     * 插入一个聊天列表,参数是一个MessageBean的list
     */
    int INSERT_CHAT_MESSAGE_LIST = 501;

    /**
     * 根据groupId删除数据库中的消息列表
     */
    int DELETE_CHAT_MESSAGE_BY_GROUPID = 502;

    /**
     * 根据本地生成的消息UUID，更新一条记录，参数是一个MessageBean
     */
    int UPDATE_MESSAGE_BY_MESSAGE_UUID = 503;

    /**
     * 要查询的数据为空
     */
    int QUERY_RESULT_IS_EMPTY = 504;

    /**
     * 查询到聊天记录
     */
    int QUERY_MESSAGE_HISTORY_SUCCESS = 505;

    /**
     * 插入一条消息记录
     */
    int INSERT_ONE_CHAT_MESSAGE = 506;

    /**
     * 插入消息本地数据库成功
     */
    int INSERT_ONE_CHAT_MESSAGE_SUCCESS = 507;

    /**
     * 插入消息本地数据库失败
     */
    int INSERT_ONE_CHAT_MESSAGE_FAILED = 532;

    /**
     * 根据消息Id删除一条消息
     */
    int DELETE_ONE_MSG_BY_MSGID = 508;

    /**
     * 插入未读消息列表
     */
    int INSERT_CHAT_UNREAD_MESSAGE_LIST = 509;

    /**
     * 查询微聊的未读消息
     */
    int QUERY_CHAT_UNREAD_MESSAGE = 510;
    /**

     * 根据groupId删除未读消息
     */
    int DELETE_UNREAD_MSG_BY_GROUPID = 511;

    /**
     * 插入一条未读消息
     */
    int INSERT_ONE_CHAT_UNREAD_MESSAGE = 512;

    /**
     * 查询所有未读消息成功
     */
    int QUERY_CHAT_UNREAD_MSG_SUCCESS = 513;

    /**
     * 查询任务的未读消息
     */
//    int QUERY_TASK_UNREAD_MSG = 514;

    /**
     * 插入一条微聊
     */
    int INSERT_ONE_CHAT_WINDOW_INFO = 515;

    /**
     * 根据ID查询微聊
     */
    int QUERY_CHAT_WINDOW_INFO_BY_ID = 516;

    /**
     * 根据ID删除微聊
     */
    int DELETE_CHAT_WINDOW_BY_ID = 517;

    /**
     * 查询微聊列表
     */
    int QUERY_NORMAL_CHAT_LIST = 518;

    /**
     * 插入一条微聊摘要
     */
    int INSERT_ONE_CHAT_WINDOW_SUMMARY = 519;

    /**
     * 删除一条微聊摘要根据id
     */
    int DELETE_ONE_CHAT_SUMMARY_BY_ID = 520;

    /**
     * 插入微聊摘要list
     */
    int INSERT_CHAT_SUMMARY_LIST = 521;

    /**
     * 查询微聊列表成功
     */
    int QUERY_NORMAL_CHAT_LIST_SUCCESS = 522;

    /**
     * 查询某一条微聊成功
     */
    int QUERY_CHAT_BY_ID_SUCCESS = 523;

    /**
     * 插入一条微聊成功
     */
    int INSERT_ONT_CHAT_SUMMARY_SUCCESS = 524;


    /**
     * 更新chatwindowinfo 根据groupId
     */
    int UPDATE_CHAT_WINDOW_BY_GROUP_ID = 525;

    /**
     * 加人时更新数据库
     */
    int UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER = 526;

    /**
     * 有人退出时更新本地数据
     */
    int UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT = 527;

    /**
     * 更新微聊subject
     */
    int RENAME_CHAT_SUBJECT = 528;

    /**
     * 插入一条小邮
     */
    int INSERT_ONE_SMALL_MAIL = 529;


    /**
     * 根据一个messageId查询其对应的小邮件
     */
    int QUERY_SMALL_MAIL_BY_MESSAGE_ID = 530;

    /**
     * 根据一个id删除一条小邮
     */
    int DELETE_SMALL_MAIL_BY_ID = 531;

    /**
     * 消息撤回时更新未读消息
     */
    int UPDATE_UNREAD_RECALL_MESSAGE_BY_MSG_ID = 532;

    /**  -------------------------------------------------------------------------------------------------------------------------------*/
    /**  微邮数据库操作，7开头*/
    /**
     * 插入一条邮件信息记录
     */
    int INSERT_ONE_MAIL_MESSAGE = 701;

    /**
     * 插入邮件本地数据库：成功
     */
    int INSERT_ONE_MAIL_MESSAGE_SUCCESS = 702;

    /**
     * 插入邮件本地数据库：失败
     */
    int INSERT_ONE_MAIL_MESSAGE_FAILED = 703;

    /**
     * 获取某一目录下的邮件列表
     */
    int QUERY_MAIL_LIST_BY_DIR_ID = 704;

    /**
     * 获取某一目录下的邮件列表：成功
     */
    int QUERY_MAIL_LIST_BY_DIR_ID_SUCCESS = 705;

    /**
     * 获取某一目录下的邮件列表：失败
     */
    int QUERY_MAIL_LIST_BY_DIR_ID_FAILED = 706;

    /**
     * 获取单封邮件详情（根据主键ID）
     */
    int QUERY_MAIL_DETAIL_BY_ID = 707;

    /**
     * 获取单封邮件详情（根据主键ID）：成功
     */
    int QUERY_MAIL_DETAIL_BY_ID_SUCCESS = 708;

    /**
     * 获取单封邮件详情（根据主键ID）：失败
     */
    int QUERY_MAIL_DETAIL_BY_ID_FAILED = 709;

    /**
     * 获取单封邮件详情（根据MessageID）
     */
    int QUERY_MAIL_DETAIL_BY_MESSAGE_ID = 710;

    /**
     * 获取单封邮件详情（根据MessageID）：成功
     */
    int QUERY_MAIL_DETAIL_BY_MESSAGE_ID_SUCCESS = 711;

    /**
     * 获取单封邮件详情（根据MessageID）：失败
     */
    int QUERY_MAIL_DETAIL_BY_MESSAGE_ID_FAILED = 712;

    /**
     * 获取用户的邮箱账号列表
     */
    int QUERY_MAIL_ACCOUNT_LIST = 713;

    /**
     * 获取用户的邮箱账号列表：成功
     */
    int QUERY_MAIL_ACCOUNT_LIST_SUCCESS = 714;

    /**
     * 获取用户的邮箱账号列表：失败
     */
    int QUERY_MAIL_ACCOUNT_LIST_FAILED = 715;

    /**
     * 插入一条邮件附件信息
     */
    int SAVE_ONE_MAIL_ATTACHMENT = 716;

    /**
     * 插入一条邮件附件信息：成功
     */
    int SAVE_ONE_MAIL_ATTACHMENT_SUCCESS = 717;

    /**
     * 插入一条邮件附件信息：失败
     */
    int SAVE_ONE_MAIL_ATTACHMENT_FAILED = 718;

    /**
     * 更新邮件详情表
     */
    int UPDATE_MAIL_DETAIL = 719;

    /**
     * 插入邮件本地数据库：成功
     */
    int UPDATE_MAIL_DETAIL_SUCCESS = 720;

    /**
     * 插入邮件本地数据库：失败
     */
    int UPDATE_MAIL_DETAIL_FAILED = 721;

    /**
     * 获取某一邮箱账号下的所有邮件的UID列表
     */
    int QUERY_ALL_MAIL_UID_LIST = 722;

    /**
     * 获取某一邮箱账号下的所有邮件的UID列表：成功
     */
    int QUERY_ALL_MAIL_UID_LIST_SUCCESS = 723;

    /**
     * 获取某一邮箱账号下的所有邮件的UID列表：失败
     */
    int QUERY_ALL_MAIL_UID_LIST_FAILED = 724;

    /**
     * 保存一条邮箱目录（插入或更新）
     */
    int SAVE_ONE_MAIL_DIRECTORY = 725;

    /**
     * 保存一条邮箱目录：成功
     */
    int SAVE_ONE_MAIL_DIRECTORY_SUCCESS = 726;

    /**
     * 保存一条邮箱目录：失败
     */
    int SAVE_ONE_MAIL_DIRECTORY_FAILED = 727;

    /**
     * 获取邮箱的目录列表
     */
    int QUERY_MAIL_DIRECTORY_LIST = 728;

    /**
     * 获取邮箱的目录列表：成功
     */
    int QUERY_MAIL_DIRECTORY_LIST_SUCCESS = 729;

    /**
     * 获取邮箱的目录列表：失败
     */
    int QUERY_MAIL_DIRECTORY_LIST_FAILED = 730;

    /**
     * 删除一条邮件目录信息
     */
    int DELETE_ONE_MAIL_DIRECTORY = 731;

    /**
     * 删除一条邮件目录信息：成功
     */
    int DELETE_ONE_MAIL_DIRECTORY_SUCCESS = 732;

    /**
     * 删除一条邮件目录信息：失败
     */
    int DELETE_ONE_MAIL_DIRECTORY_FAILED = 733;

    /**
     * 保存用户的邮箱账号信息（插入或更新）
     */
    int SAVE_ONE_MAIL_ACCOUNT = 734;

    /**
     * 保存用户的邮箱账号信息（插入或更新）：成功
     */
    int SAVE_ONE_MAIL_ACCOUNT_SUCCESS = 735;

    /**
     * 保存用户的邮箱账号信息（插入或更新）：失败
     */
    int SAVE_ONE_MAIL_ACCOUNT_FAILED = 736;

    /**
     * 删除一条邮箱账号信息
     */
    int DELETE_ONE_MAIL_ACCOUNT = 737;

    /**
     * 删除一条邮箱账号信息：成功
     */
    int DELETE_ONE_MAIL_ACCOUNT_SUCCESS = 738;

    /**
     * 删除一条邮箱账号信息：失败
     */
    int DELETE_ONE_MAIL_ACCOUNT_FAILED = 739;

    /**
     * 删除一封邮件
     */
    int DELETE_ONE_MAIL = 740;

    /**
     * 删除一封邮件：成功
     */
    int DELETE_ONE_MAIL_SUCCESS = 741;

    /**
     * 删除一封邮件：失败
     */
    int DELETE_ONE_MAIL_FAILED = 742;

    /**
     * 获取某一目录下的邮件列表（分页）
     */
    int QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE = 743;

    /**
     * 获取某一目录下的邮件列表（分页）：成功
     */
    int QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE_SUCCESS = 744;

    /**
     * 获取某一目录下的邮件列表（分页）：失败
     */
    int QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE_FAILED = 745;

    /**
     * 获取未读邮件列表
     */
    int QUERY_UNREAD_MAIL_LIST = 746;

    /**
     * 获取未读邮件列表：成功
     */
    int QUERY_UNREAD_MAIL_LIST_SUCCESS = 747;

    /**
     * 获取未读邮件列表：失败
     */
    int QUERY_UNREAD_MAIL_LIST_FAILED = 748;

    /**
     * 获取某一目录下的邮件列表
     */
    int QUERY_MAIL_ATTACHMENT_LIST = 749;

    /**
     * 获取某一目录下的邮件列表：成功
     */
    int QUERY_MAIL_ATTACHMENT_LIST_SUCCESS = 750;

    /**
     * 获取某一目录下的邮件列表：失败
     */
    int QUERY_MAIL_ATTACHMENT_LIST_FAILED = 751;

    /**
     * 获取已标记的邮件列表
     */
    int QUERY_MARKED_MAIL_LIST = 752;

    /**
     * 获取已标记的邮件列表：成功
     */
    int QUERY_MARKED_MAIL_LIST_SUCCESS = 753;

    /**
     * 获取已标记的邮件列表：失败
     */
    int QUERY_MARKED_MAIL_LIST_FAILED = 754;

    /**
     * 获取用户的邮件联系人列表
     */
    int QUERY_MAIL_CONTACTS_LIST = 755;

    /**
     * 获取用户的邮件联系人列表：成功
     */
    int QUERY_MAIL_CONTACTS_LIST_SUCCESS = 756;

    /**
     * 获取用户的邮件联系人列表：失败
     */
    int QUERY_MAIL_CONTACTS_LIST_FAILED = 757;

    /**
     * 保存用户的一个邮件联系人信息（插入或更新）
     */
    int SAVE_ONE_MAIL_CONTACTS = 758;

    /**
     * 保存用户的一个邮件联系人信息（插入或更新）：成功
     */
    int SAVE_ONE_MAIL_CONTACTS_SUCCESS = 759;

    /**
     * 保存用户的一个邮件联系人信息（插入或更新）：失败
     */
    int SAVE_ONE_MAIL_CONTACTS_FAILED = 760;

    int QUERY_OUT_BOX_MAIL_LIST = 761;
    int QUERY_OUT_BOX_MAIL_LIST_SUCCESS = 762;
    int QUERY_OUT_BOX_MAIL_LIST_FAILED = 763;

    int SAVE_ONE_MAIL_TASK = 764;
    int SAVE_ONE_MAIL_TASK_SUCCESS = 765;
    int SAVE_ONE_MAIL_TASK_FAILED = 766;

    int GET_ALL_MAIL_TASK = 767;
    int GET_ALL_MAIL_TASK_RESULT = 768;

    int DELETE_MAIL_TASK_BY_ID = 769;
    int DELETE_MAIL_TASK_BY_ID_RESULT = 770;

    int UPDATE_MAIL_TASK_RCPTS = 771;
    int UPDATE_MAIL_TASK_RCPTS_RESULT = 772;

    int CHANGE_MAIL_DIRECTORY = 773;
    int CHANGE_MAIL_DIRECTORY_SUCCESS = 774;
    int CHANGE_MAIL_DIRECTORY_FAILED = 775;

    int UPDATE_MAIL_SENT_PERCENTAGE = 776;
    int UPDATE_MAIL_SENT_PERCENTAGE_SUCCESS = 777;
    int UPDATE_MAIL_SENT_PERCENTAGE_FAILED = 778;

    int CHANGE_MAIL_SEND_STATUS = 779;
    int CHANGE_MAIL_SEND_STATUS_SUCCESS = 780;
    int CHANGE_MAIL_SEND_STATUS_FAILED = 781;
}
