package com.inspur.playwork.config;

/**
 * APP配置类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class AppConfig {

    /*htime server port*/
    private static final int PORT = 5848;
//    public static final int PORT = 5849;
//    public static final int PORT = 55555;

    /*htime host*/
    private static final String BASE_URI_HOST = "http://htime.inspur.com";
    private static final String WP_BASE_URI_HOST = "http://htime.inspur.com";
//    public static final String WP_BASE_URI_HOST = "http://10.110.9.46";

    //        public static final String HTTP_SERVER_IP = "http://10.47.1.20:6382/";
//    public static final String HTTP_SERVER_IP = "http://218.57.135.45:55166/";
    public static final String HTTP_SERVER_IP = "http://218.57.135.45:9080/";

    /* 微知微盘 登录*/
    public static final String APP_CONTAINER_ENTRANCE = "http://dcp.inspur.com" + ":88/common/appContainer.html";
    public static final String APP_KW_LOGIN = "http://dcp.inspur.com" + ":88/kw/mobile";
    public static final String APP_DISK_LOGIN = "http://dcp.inspur.com" + ":88/disk/mobile";

    /* 集团周计划、集团MBO */
    public static String getAPP_INSPUR_WEEKPLAN(String userId) {
        return "http://218.57.135.49:8082/io/LoginSSO.do?userName=xxx&department=xxx&company=xxx&toPage=2&userId=" + userId + "&mail=" + userId + "@inspur.com";
    }

    public static String getAPP_INSPUR_MBO(String userId) {
        return "http://218.57.146.212/cwbase/web/singlelogin.aspx?AuthType=UserCode&AppCode=0001&frameType=phone&StartUri=/cwbase/webapp/ehr/hrpf/index.html&UserCode=" + userId;
    }

    /*微邮 host*/
    private static final String WY_PUBLIC_URL_BASE = BASE_URI_HOST + ":82/mailio";

    /*时间轴 host*/
    public static final String LOCAL_NET_URI = BASE_URI_HOST + ":" + PORT;
//    public static final String LOCAL_NET_URI = "http://10.110.6.64:5849";

    /*ad host*/
//    public static final String BASE_SERVER = "http://218.57.146.202/PlayWorkWCF/";
    public static final String BASE_SERVER = "http://htime.inspur.com/m/";
    public static final String BASE_LOG_SERVER = "http://htime.inspur.com/l/";

    public static final String NEWS_URL = "http://office8.inspur.com:8082/inspur/news/";
//    public static final String BASE_SERVER = "http://htime.inspur.com/m/";

    // 登陆ad认证服务
    public static final String AD_SERVER_URI = BASE_SERVER + "ADLoginForMobileV1.ashx?";
//    public static final String AD_SERVER_URI = BASE_SERVER + "ADLoginForMobileV1.ashx?";

    // 下载更新文件的服务地址
//    public static final String DOWNLOAD_APK_FILE = BASE_SERVER + "mobile/update.htm";
//    public static final String DOWNLOAD_APK_FILE = BASE_SERVER + "mobile/getNewMobileVersionHandler.ashx";

    private static final String SHARE_APK_FILE = "http://htime.inspur.com/m";
    public static String SHARE_MESSAGE_TO_OTHERS = "好时光可以帮助你把事管起来，很方便，推荐您用一下，下载地址：" + SHARE_APK_FILE + " ," +
            "记得在微聊中联系我哦:";

    // 检查更新版本信息
    public static final String CHECK_NEW_VERSION = BASE_SERVER + "CheckNewVersion_mobil.ashx?";

    // 查询部门成员
    public static final String SEARCH_BY_DEPT = BASE_SERVER + "search/searchbyDept.ashx";

    //存放用户信息的东西
    public static final String AD_USER_INFO = "ADuserInfo";

    /*登录密钥 key*/
    public static final String AD_KEY = "!QAZ2wsx@WSX1qaz";

    /*微盘服务ip*/
    private static final String BASE_URI2WP = WP_BASE_URI_HOST + ":81";
//    public static final String BASE_URI2WP = WP_BASE_URI_HOST + ":8380";

    public static String UPLOAD_FILE_URI_SERVICE = BASE_URI2WP + "/wpserver/";

    /*上传文件地址*/
    public static String UPLOAD_FILE_URI = UPLOAD_FILE_URI_SERVICE + "upload-api";
    // 上传崩溃日志
    public static String UPLOAD_ERROR_LOG = HTTP_SERVER_IP + "feedback/uploadFiles";

    /*头像下载地址   photo_size=50&*/
    public static String AVATAR_ROOT_PATH = UPLOAD_FILE_URI_SERVICE + "imagefile?photo_id=";

    /*更改头像地址*/
    public static String UPDATE_AVATAR_PORTRAIT = BASE_SERVER + "common/ChangeAvatar.ashx?";

    /*根据给定的字符串搜索用户*/
    public static final String SEARCH_USER_BY_STR = BASE_SERVER + "searchperson.ashx";

    public static final String CHECK_SHA1_ADDR = BASE_SERVER + "cert/IsNewBySHA1.ashx?";

    public static final String GET_PUBLIC_KEY_ADDR = BASE_SERVER + "getCERTbyUserIds.ashx";

    /*头像保存的地址*/
    public static final String AVATAR_DIR = "/playWork/avatar/";
    /*邮箱后缀*/
    public static final String EMAIL_SUFFIX = "@inspur.com";

    public static final String IMAGE_DIR = "/playWork/images/";

    public static final String ATTACHMENT_DIR = "/playWork/attachments/";

    public static class WY_CFG {
        public static String CKCDK = "A49AQ11PZ9I4U0CV2YMY2780O5CRA5";//Q37TWX11PZ9I4U0CV2YMY2780O5CRA1

        //        各种马里奥数据接口
        public static String URL_GET_MAIL_LIST = WY_PUBLIC_URL_BASE + "/getMailUIDListForMobile";
        public static String URL_GET_MAIL_BY_UID = WY_PUBLIC_URL_BASE + "/getMailByUIDForMobile";
        public static String URL_SEND_MAIL = WY_PUBLIC_URL_BASE + "/sendMail";
        public static String URL_SEND_ENCRYPT_MAIL = WY_PUBLIC_URL_BASE + "/sendSecureMail";
        public static String URL_VERIFY_MAIL_ACCOUNT = WY_PUBLIC_URL_BASE + "/verifyMailConfig";

        //        邮件大小大于它就自动存文件的
        public static final long SAVE_FILE_SIZE = 256 * 1024;
        //          邮件列表每页条数
        public static final int MAIL_NUM_PER_PAGE = 10;
        //        默认目录的ID
//        public static final long DIR_ID_DELETED_AGAIN = -1000;
        public static final long DIR_ID_INBOX = -999;
        public static final long DIR_ID_UNREAD_MAIL = -998;
        public static final long DIR_ID_SENT_MAIL = -997;
        public static final long DIR_ID_DRAFTBOX = -996;
        public static final long DIR_ID_OUTBOX = -995;
        public static final long DIR_ID_DELETED_MAIL = -994;
        public static final long DIR_ID_MARKED_MAIL = -993;
        public static final long DIR_ID_ATTACHMENT_LIST = -992;

        //加密类型
        public final static int ENC_TYPE_NO_ENC = 0;
        public final static int ENC_TYPE_MICROSOFT_SIGNED = 1;
        public final static int ENC_TYPE_MICROSOFT_ENC = 2;
        public static final int ENC_TYPE_MICROSOFT_ENC_SIGNED = 3;

        public static final String POP3_SERVER_HOST = "mail.inspur.com";
        public static final String POP3_SERVER_PORT = "995";
        public static final boolean POP3_SERVER_TLS = false;
        public static final boolean POP3_SERVER_SSL = true;
        public static final String SMTP_SERVER_HOST = "mail.inspur.com";
        public static final String SMTP_SERVER_PORT = "587";
        public static final boolean SMTP_SERVER_TLS = true;
        public static final boolean SMTP_SERVER_SSL = false;

        public static final long SEND_MAIL_INTERVAL = 10000;
    }

    public static void main(String[] argc) {
        System.out.println(UPLOAD_FILE_URI_SERVICE);
        System.out.println(UPLOAD_FILE_URI_SERVICE);
    }
}
