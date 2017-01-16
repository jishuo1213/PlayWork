package com.inspur.playwork.utils.db;

import java.util.Calendar;

public class SQLSentence {

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_BOOLEAN = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String CREATE_TABLE_PREFIX = "create table if not exists ";

    private static final String PRIMARY_KEY = "_id integer primary key autoincrement";

    private static final String LEFT_BRACKETS = "(";

    private static final String RIGHT_BRACKETS = ")";

    public static final String TABLE_USERINFO = "userInfo";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USER_UID = "uid";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_TEL = "tel";
    public static final String COLUMN_USER_DEPARTMENT = "department";
    public static final String COLUMN_USER_DEPARTMENTID = "departmentId";
    public static final String COLUMN_USER_COMPANY = "company";
    public static final String COLUMN_USER_COMPANYID = "companyId";
    public static final String COLUMN_USER_AVATAR = "avatar";


    public static String getCreateTableUserInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_USERINFO).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(COLUMN_USER_ID).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_UID).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_NAME).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_EMAIL).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_TEL).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_DEPARTMENT).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_DEPARTMENTID).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_COMPANY).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_COMPANYID).append(TYPE_TEXT).append(COMMA_SEP).
                append(COLUMN_USER_AVATAR).append(TYPE_INTEGER).append(RIGHT_BRACKETS);
        return sb.toString();
    }


    public static final String TABLE_CHAT_MESSAGE = "chat_message";
    public static final String MESSAGE_CONTENT = "content";
    public static final String MESSAGE_CREATETIME = "create_time";
    public static final String MESSAGE_FROM = "message_from";
    public static final String MESSAGE_ISENCRYPT = "is_encrypt";
    public static final String MESSAGE_TO = "message_to";
    public static final String MESSAGE_SENDTIME = "send_time";
    public static final String MESSAGE_MAILID = "mail_id";

    public static final String MESSAGE_ISSEND = "is_send"; //消息是否已经发送成功
    public static final String MESSAGE_TYPE = "message_type";//消息类型
    public static final String MESSAGE_UUID = "message_uuid";
    public static final String MESSAGE_GROUP_ID = "group_id";
    public static final String MESSAGE_SMALL_MAIL_ID = "small_mail_id";

    private static final String[] MESSAGE_COLUMNS = {MESSAGE_CONTENT, MESSAGE_CREATETIME, MESSAGE_FROM,
            MESSAGE_ISENCRYPT, MESSAGE_TO, MESSAGE_SENDTIME, MESSAGE_MAILID, MESSAGE_ISSEND, MESSAGE_TYPE,
            MESSAGE_UUID, MESSAGE_GROUP_ID, MESSAGE_SMALL_MAIL_ID};

    public static String getCreateTableMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_CHAT_MESSAGE).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(MESSAGE_CONTENT).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_CREATETIME).append(TYPE_INTEGER).append(COMMA_SEP).
                append(MESSAGE_FROM).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_ISENCRYPT).append(TYPE_BOOLEAN).append(COMMA_SEP).
                append(MESSAGE_TO).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_SENDTIME).append(TYPE_INTEGER).append(COMMA_SEP).
                append(MESSAGE_MAILID).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_TYPE).append(TYPE_INTEGER).append(COMMA_SEP).
                append(MESSAGE_ISSEND).append(TYPE_BOOLEAN).append(COMMA_SEP).
                append(MESSAGE_UUID).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_SMALL_MAIL_ID).append(TYPE_TEXT).append(COMMA_SEP).
                append(MESSAGE_GROUP_ID).append(TYPE_TEXT).
                append(RIGHT_BRACKETS);
        return sb.toString();
    }


    public static String getInsertMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_MESSAGE);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : MESSAGE_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }


    public static String getUpsertMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_MESSAGE);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : MESSAGE_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" select ");
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(" where not exists ").append(LEFT_BRACKETS);
        sb.append("select ").append(MESSAGE_CONTENT).append(" from ");
        sb.append(TABLE_CHAT_MESSAGE).append(" where ").append(MESSAGE_GROUP_ID).
                append("= ? and ").append(MESSAGE_MAILID).
                append(" = ").append(" ? ").append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getUpsertReCallMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_MESSAGE);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : MESSAGE_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }

        sb.append(RIGHT_BRACKETS);
        sb.append(" select ");
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(" where not exists ").append(LEFT_BRACKETS);
        sb.append("select ").append(MESSAGE_CONTENT).append(" from ");
        sb.append(TABLE_CHAT_MESSAGE).append(" where ").append(MESSAGE_GROUP_ID).
                append("= ? and ").append(MESSAGE_MAILID).
                append(" = ").append(" ? ").append(RIGHT_BRACKETS);
        return sb.toString();
    }

    static String getUpdateMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(TABLE_CHAT_MESSAGE).append(" set ");
        sb.append(MESSAGE_ISSEND).append(" = ? ").append(COMMA_SEP);
        sb.append(MESSAGE_MAILID).append(" = ? ");
        sb.append(" where ").append(MESSAGE_GROUP_ID).append("= ? and ")
                .append(MESSAGE_UUID).append(" = ? ");
        return sb.toString();
    }

    static String getUpdateImageMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(TABLE_CHAT_MESSAGE).append(" set ");
        sb.append(MESSAGE_CONTENT).append(" = ? ");
        sb.append(" where ").append(MESSAGE_GROUP_ID).append("= ? and ")
                .append(MESSAGE_UUID).append(" = ? ");
        return sb.toString();
    }


    /**
     * 未读消息表
     */
    static final String TABLE_CHAT_UNREAD_MESSAGE = "chat_unread_message";

    static final String UNREAD_MSG_GROUPID = "unread_groupId";
    static final String UNREAD_MSG_TASKID = "unread_taskId";
    static final String UNREAD_MSG_CREATETIME = "create_time";
    static final String UNREAD_MSG_SENDTIME = "unread_sendtime";
    static final String UNREAD_TYPE = "unread_type";
    static final String UNREAD_MSG_ID = "msg_id";
    static final String UNREAD_IS_NEED_SHOW_NUMBER = "is_need_show_num";

    private static final String[] UNREAD_MESSAGE_COLUMNS = {UNREAD_MSG_GROUPID,
            UNREAD_MSG_TASKID, UNREAD_MSG_CREATETIME, UNREAD_MSG_SENDTIME, UNREAD_TYPE, UNREAD_MSG_ID, UNREAD_IS_NEED_SHOW_NUMBER};

    static String getQueryUnReadString(int type) {
        return "SELECT " + "a" + ".*, " + "b" + "." + MESSAGE_CONTENT + " FROM " + TABLE_CHAT_UNREAD_MESSAGE + " as a," + TABLE_CHAT_MESSAGE + " as b where " + "a" + "." + UNREAD_TYPE + " = " + type + " AND " +
                "a" + "." + UNREAD_MSG_ID + " = " + "b" + "." + MESSAGE_MAILID;
    }

    static String getQueryAllUnReadString() {
        return "SELECT " + "a" + ".*, " + "b" + "." + MESSAGE_CONTENT + " FROM " + TABLE_CHAT_UNREAD_MESSAGE + " as a," + TABLE_CHAT_MESSAGE + " as b where " +
                "a" + "." + UNREAD_MSG_ID + " = " + "b" + "." + MESSAGE_MAILID;
    }

    static String getInsertUnReadMessageString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_UNREAD_MESSAGE);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : UNREAD_MESSAGE_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    static String getCreateUnReadMsgTableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_CHAT_UNREAD_MESSAGE).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(UNREAD_MSG_GROUPID).append(TYPE_TEXT).append(COMMA_SEP).
                append(UNREAD_MSG_TASKID).append(TYPE_TEXT).append(COMMA_SEP).
                append(UNREAD_MSG_CREATETIME).append(TYPE_INTEGER).append(COMMA_SEP).
                append(UNREAD_MSG_SENDTIME).append(TYPE_INTEGER).append(COMMA_SEP).
                append(UNREAD_IS_NEED_SHOW_NUMBER).append(TYPE_INTEGER).append(COMMA_SEP).
                append(UNREAD_TYPE).append(TYPE_INTEGER).
                append(RIGHT_BRACKETS);
//        Log.i("createTableString", sb.toString());
        return sb.toString();
    }


    static final String TABLE_CHAT_WINDWO_INFO = "table_chat_window_info";

    static final String CHAT_WINDOW_GROUPID = "chat_window_groupId";
    static final String CHAT_WINDOW_MAILID = "chat_window_mailId";
    static final String CHAT_WINDOW_TASKID = "chat_window_taskId";
    static final String CHAT_WINDOW_CREATE_USER = "chat_window_create_user";
    static final String CHAT_WINDOW_TITLE = "chat_window_title";
    static final String CHAT_WINDOW_IS_SINGLE = "chat_window_is_single";
    static final String CHAT_WINDOW_TASK_PLACE = "chat_window_task_place";
    static final String CHAT_WINDOW_MEMBER_NAMES = "chat_window_member_names";
    static final String CHAT_WINDOW_MEMBER = "chat_window_member";
    static final String CHAT_WINDOW_LAST_TO = "chat_window_last_to";
    static final String CHAT_WINDOW_EXIT_MEMBER = "chat_window_exit_member";

    private static final String[] CHAT_WINDOW_COLUMNS = {CHAT_WINDOW_GROUPID,
            CHAT_WINDOW_MAILID, CHAT_WINDOW_TASKID, CHAT_WINDOW_CREATE_USER, CHAT_WINDOW_TITLE,
            CHAT_WINDOW_IS_SINGLE, CHAT_WINDOW_TASK_PLACE, CHAT_WINDOW_MEMBER_NAMES, CHAT_WINDOW_MEMBER, CHAT_WINDOW_LAST_TO,
            CHAT_WINDOW_EXIT_MEMBER};

    public static String getCreateChatWindowInfoTableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_CHAT_WINDWO_INFO).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(CHAT_WINDOW_GROUPID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_MAILID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_TASKID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_CREATE_USER).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_TITLE).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_IS_SINGLE).append(TYPE_INTEGER).append(COMMA_SEP).
                append(CHAT_WINDOW_TASK_PLACE).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_MEMBER_NAMES).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_MEMBER).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_LAST_TO).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW_EXIT_MEMBER).append(TYPE_TEXT).
                append(RIGHT_BRACKETS);
//        Log.i("createTableString", sb.toString());
        return sb.toString();
    }

    public static String getInsertChatWindowString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_WINDWO_INFO);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : CHAT_WINDOW_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static final String TABLE_CHAT_WINDWO_SUMMARY = "table_chat_window_summary";

    public static final String CHAT_WINDOW_SUMMARY_GROUPID = "chat_window_summary_groupId";
    public static final String CHAT_WINDOW__SUMMARY_TASKID = "chat_window_summary_taskId";
    public static final String CHAT_WINDOW__SUMMARY_TITLE = "chat_window_summary_title";
    public static final String CHAT_WINDOW__SUMMARY_MEMBER = "chat_window_summary_member_list";
    public static final String CHAT_WINDOW__SUMMARY_ISGROUP = "chat_window_summary_isgroup";
    public static final String CHAT_WINDOW__SUMMARY_LAST_MSG = "chat_window_summary_lastmsg";
    public static final String CHAT_WINDOW__SUMMARY_LAST_MSG_ID = "chat_window_summary_msgid";
    public static final String CHAT_WINDOW__SUMMARY_SEND_TIME = "chat_window_summary_sendtime";

    private static final String[] CHAT_WINDOW_SUMMARY_COLUMNS = {CHAT_WINDOW_SUMMARY_GROUPID,
            CHAT_WINDOW__SUMMARY_TASKID, CHAT_WINDOW__SUMMARY_TITLE,
            CHAT_WINDOW__SUMMARY_MEMBER, CHAT_WINDOW__SUMMARY_ISGROUP, CHAT_WINDOW__SUMMARY_LAST_MSG,
            CHAT_WINDOW__SUMMARY_LAST_MSG_ID, CHAT_WINDOW__SUMMARY_SEND_TIME};

    public static String getCreateChatSummarySql() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_CHAT_WINDWO_SUMMARY).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(CHAT_WINDOW_SUMMARY_GROUPID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_TASKID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_TITLE).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_ISGROUP).append(TYPE_BOOLEAN).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_MEMBER).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_LAST_MSG).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_LAST_MSG_ID).append(TYPE_TEXT).append(COMMA_SEP).
                append(CHAT_WINDOW__SUMMARY_SEND_TIME).append(TYPE_INTEGER).
                append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getInsertChatSummarySql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_WINDWO_SUMMARY);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : CHAT_WINDOW_SUMMARY_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getUpsertChatSummarySql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_CHAT_WINDWO_SUMMARY);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : CHAT_WINDOW_SUMMARY_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" SELECT ");
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(" WHERE NOT EXISTS ").append(LEFT_BRACKETS).append("SELECT ").
                append(CHAT_WINDOW__SUMMARY_TITLE).append(" FROM ").append(TABLE_CHAT_WINDWO_SUMMARY)
                .append(" WHERE ").append(CHAT_WINDOW_SUMMARY_GROUPID).append(" = ?");
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    /**
     * SELECT table_chat_window_summary.chat_window_summary_groupId,
     * table_chat_window_summary.chat_window_summary_member_list,
     * table_chat_window_summary.chat_window_summary_title,
     * table_chat_window_summary.chat_window_summary_isgroup,
     * new_table.send_time,new_table.mail_id,new_table.content from table_chat_window_summary
     * LEFT OUTER JOIN (SELECT  chat_message.group_id,chat_message.send_time,chat_message.mail_id,chat_message.content,max(chat_message.send_time)
     * from chat_message GROUP BY chat_message.group_id) new_table ON
     * table_chat_window_summary.chat_window_summary_groupId = new_table.group_id
     *
     * @return
     */

    static String getQueryChatListSql() {
        return "SELECT " + TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW_SUMMARY_GROUPID + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_MEMBER + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_TITLE + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_ISGROUP + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_LAST_MSG + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_LAST_MSG_ID + "," +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW__SUMMARY_SEND_TIME + "," +
                "new_table" + "." + MESSAGE_SENDTIME + "," +
                "new_table" + "." + MESSAGE_MAILID + "," +
                "new_table" + "." + MESSAGE_CONTENT + "," +
                "new_table" + "." + MESSAGE_TYPE + "," +
                "new_table" + "." + MESSAGE_FROM +
                " from " + TABLE_CHAT_WINDWO_SUMMARY + " LEFT OUTER JOIN (SELECT " +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_GROUP_ID + "," +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_SENDTIME + "," +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_MAILID + "," +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_CONTENT + "," +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_TYPE + "," +
                TABLE_CHAT_MESSAGE + "." + MESSAGE_FROM + "," +
                "MAX ( " + TABLE_CHAT_MESSAGE + "." + MESSAGE_SENDTIME + ") FROM " + TABLE_CHAT_MESSAGE +
                " GROUP BY " + TABLE_CHAT_MESSAGE + "." + MESSAGE_GROUP_ID + ") new_table ON " +
                TABLE_CHAT_WINDWO_SUMMARY + "." + CHAT_WINDOW_SUMMARY_GROUPID + " = " +
                "new_table" + "." + MESSAGE_GROUP_ID;
    }

    static String getUpdateWindowInfoSql(int type) {
        if (type == 1)
            return "update " + TABLE_CHAT_WINDWO_INFO + " set " + CHAT_WINDOW_MEMBER + " = ? " + COMMA_SEP + CHAT_WINDOW_EXIT_MEMBER + " = ? " + COMMA_SEP + CHAT_WINDOW_LAST_TO + " = ? " + COMMA_SEP + CHAT_WINDOW_MEMBER_NAMES + " = ? " + " where " + CHAT_WINDOW_GROUPID + " = ? ";
        else if (type == 2)
            return "update " + TABLE_CHAT_WINDWO_INFO + " set " + CHAT_WINDOW_TITLE + " = ? " + " where " + CHAT_WINDOW_GROUPID + " = ? ";
        return null;
    }

    static String getUpdateWindowSummarySql(int type) {
        if (type == 1)
            return "update " + TABLE_CHAT_WINDWO_SUMMARY + " set " + CHAT_WINDOW__SUMMARY_MEMBER + " = ? " + COMMA_SEP + CHAT_WINDOW__SUMMARY_TITLE + " = ? " + " where " + CHAT_WINDOW_SUMMARY_GROUPID + " = ? ";
        else if (type == 2)
            return "update " + TABLE_CHAT_WINDWO_SUMMARY + " set " + CHAT_WINDOW__SUMMARY_TITLE + " = ? " + " where " + CHAT_WINDOW_SUMMARY_GROUPID + " = ? ";
        return null;
    }


    static final String TABLE_SMALL_MAIL = "small_mail";

    static final String SMALL_MAIL_MESSAGE_ID = "message_id";
    static final String SMALL_MAIL_SUBJECT = "mail_subject";
    static final String SMALL_MAIL_FROM = "mail_from";
    static final String SMALL_MAIL_SEND_TO = "mail_to";
    static final String SMALL_MAIL_CONTENT = "mail_content";
    static final String SMALL_MAIL__ID = "mail_id";
    static final String SMALL_MAIL__SEND_TIME = "mail_send_time";
    static final String SMALL_MAIL_ATTACHMENTS = "small_mail_attchments";


    private static final String[] SMALL_MAIL_COLUMNS = {
            SMALL_MAIL_MESSAGE_ID, SMALL_MAIL_SUBJECT,
            SMALL_MAIL_FROM, SMALL_MAIL_SEND_TO, SMALL_MAIL_CONTENT,
            SMALL_MAIL__ID, SMALL_MAIL__SEND_TIME, SMALL_MAIL_ATTACHMENTS};

    static String getCreateSmallMailSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_TABLE_PREFIX).append(TABLE_SMALL_MAIL).append(LEFT_BRACKETS).append(PRIMARY_KEY).append(COMMA_SEP).
                append(SMALL_MAIL_MESSAGE_ID).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL_FROM).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL_SUBJECT).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL_SEND_TO).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL_CONTENT).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL__ID).append(TYPE_TEXT).append(COMMA_SEP).
                append(SMALL_MAIL__SEND_TIME).append(TYPE_INTEGER).
                append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getInsertSmallMailSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_SMALL_MAIL);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : SMALL_MAIL_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getQuerySmallMailSql() {
        return "SELECT * FROM " + TABLE_SMALL_MAIL + " WHERE " + SMALL_MAIL_MESSAGE_ID + " = ?";
    }


    public static final String TABLE_SERVER_SOCKET_ENVENT = "server_socket_event";
    public static final String EVENT_FEED_BACK_ID = "feed_back_id";
    public static final String EVENT_CODE = "event_code";
    public static final String EVENT_INFO = "event_info";
    public static final String IS_SERVER_EVENT_DELETE = "is_server_event_delete";
    public static final String IS_CLIENT_HAS_PROCESS = "is_client_has_process";
    public static final String EVENT_RECIVE_TIME = "event_recive_time";


    private static final String[] SOCKET_EVENT_COLUMNS = {EVENT_FEED_BACK_ID, EVENT_CODE, EVENT_INFO,
            IS_SERVER_EVENT_DELETE, IS_CLIENT_HAS_PROCESS, EVENT_RECIVE_TIME};

    public static String getCreateSocketEventSql() {
        return CREATE_TABLE_PREFIX + TABLE_SERVER_SOCKET_ENVENT + LEFT_BRACKETS + PRIMARY_KEY + COMMA_SEP +
                EVENT_FEED_BACK_ID + TYPE_TEXT + COMMA_SEP +
                EVENT_CODE + TYPE_INTEGER + COMMA_SEP +
                EVENT_INFO + TYPE_TEXT + COMMA_SEP +
                IS_SERVER_EVENT_DELETE + TYPE_BOOLEAN + COMMA_SEP +
                IS_CLIENT_HAS_PROCESS + TYPE_BOOLEAN + COMMA_SEP +
                EVENT_RECIVE_TIME + TYPE_INTEGER +
                RIGHT_BRACKETS;
    }


    public static String getInsertSocketEventSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_SERVER_SOCKET_ENVENT);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : SOCKET_EVENT_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" VALUES ").append(LEFT_BRACKETS);
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(RIGHT_BRACKETS);
        return sb.toString();
    }

    public static String getUpsertSocketEventSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(TABLE_SERVER_SOCKET_ENVENT);
        sb.append(LEFT_BRACKETS);
        int i = 0;
        for (String columnName : SOCKET_EVENT_COLUMNS) {
            sb.append(i > 0 ? COMMA_SEP : "");
            sb.append(columnName);
            i++;
        }
        sb.append(RIGHT_BRACKETS);
        sb.append(" select ");
        for (; i > 0; i--) {
            sb.append("?");
            if (i > 1)
                sb.append(COMMA_SEP);
        }
        sb.append(" where not exists ").append(LEFT_BRACKETS);
        sb.append("select ").append(EVENT_CODE).append(" from ");
        sb.append(TABLE_SERVER_SOCKET_ENVENT).append(" where ").append(EVENT_FEED_BACK_ID).
                append(" = ? ").append(RIGHT_BRACKETS);
        return sb.toString();
    }


    public static void main(String[] argc) {
//        double  res = 0.7 + 0.1;
//        double num = res * 1000000;
//        System.out.println(num);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2006, 11, 31);
        calendar1.setTimeInMillis(calendar.getTimeInMillis());
        int dateNums = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println(dateNums);
//        int weekNum = dateNums / 7;
//        System.out.println(weekNum);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
        System.out.println(dayofweek);

        if (dayofweek <= 5 && dayofweek != 1) {
//            weekNum++;
            dateNums += (dayofweek - 2);
        } else {
            if (dayofweek == 1) {
                dateNums -= 1;
            } else {
                dateNums -= (7 - dayofweek + 2);
            }
        }

        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DATE, 31);

        int lastDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int weekNum = dateNums / 7;
        if (dateNums % 7 != 0) {
            weekNum++;
        } else {
            System.out.println(weekNum);
            return;
        }
//        calendar.add(Calendar.YEAR,1);

        System.out.println(weekNum);
//        if (weekNum == 0) {
//            weekNum = 52;
//        }
//        System.out.println(weekNum);

    }
}
