package com.inspur.playwork.utils.db;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.model.common.SocketEvent;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.XmlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by jianggf on 2015/10/28.
 */
class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelperFan";

    private static final int VERSION = 9;
    //   private static DBHelper dbhelper;

    DBHelper(Context context, String name) {
        super(context, name, null, VERSION);
        Log.i(TAG, "DBHelper: " + name);
    }

/*    public static DBHelper getInstance(Context context) {
        if (dbhelper == null) {
            dbhelper = new DBHelper(context);
        }
        return dbhelper;
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLSentence.getCreateTableUserInfoString());
        db.execSQL(SQLSentence.getCreateTableMessageString());

        db.execSQL(SQLSentence.getCreateUnReadMsgTableString());
        db.execSQL("CREATE INDEX " + "chat_index ON " +
                SQLSentence.TABLE_CHAT_MESSAGE + " ( " + SQLSentence.MESSAGE_GROUP_ID + " ) ");

        db.execSQL("ALTER TABLE " + SQLSentence.TABLE_CHAT_UNREAD_MESSAGE + " ADD " + SQLSentence.UNREAD_MSG_ID + " TEXT " + " default '0'");
        db.execSQL(SQLSentence.getCreateChatWindowInfoTableString());
        db.execSQL(SQLSentence.getCreateChatSummarySql());

        version3to4(db);
        version5to6(db);
        version6to7(db);
        //7-8增加列不需要
        //8-9删除表数据不需要
//        version7to8(db);
//        version4to5(db);
//        version4to5(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        while (newVersion > oldVersion) {
            switch (oldVersion) {
                case 1:
                    version1to2(db);
                    break;
                case 2:
                    version2to3(db);
                    break;
                case 3:
                    version3to4(db);
                    break;
                case 4:
                    version4to5(db);
                    break;
                case 5:
                    version5to6(db);
                    break;
                case 6:
                    version6to7(db);
                    break;
                case 7:
                    version7to8(db);
                    break;
                case 8:
                    version8to9(db);
                    break;
//                case 9:
//                    version9to10(db);
//                    break;
            }
            ++oldVersion;
        }
    }

//    private void version9to10(SQLiteDatabase db) {
//        db.delete(SQLSentence.TABLE_CHAT_WINDWO_INFO, null, null);
//        db.delete(SQLSentence.TABLE_CHAT_WINDWO_SUMMARY, null, null);
//    }

    private void version8to9(SQLiteDatabase db) {
        db.delete(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, null, null);
        db.delete(SQLSentence.TABLE_CHAT_MESSAGE, null, null);
        db.delete(SQLSentence.TABLE_CHAT_WINDWO_INFO, null, null);
        db.delete(SQLSentence.TABLE_CHAT_WINDWO_SUMMARY, null, null);
        db.delete(SQLSentence.TABLE_SMALL_MAIL, null, null);
        db.delete(SQLSentence.TABLE_SERVER_SOCKET_ENVENT, null, null);
    }

    private void version7to8(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + SQLSentence.TABLE_CHAT_UNREAD_MESSAGE + " ADD " + SQLSentence.UNREAD_IS_NEED_SHOW_NUMBER + " INTEGER " + " default 0");
    }

    private void version6to7(SQLiteDatabase db) {
        db.execSQL(SQLSentence.getCreateSocketEventSql());
    }

    private void version5to6(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + SQLSentence.TABLE_SMALL_MAIL + " ADD " + SQLSentence.SMALL_MAIL_ATTACHMENTS + " TEXT " + " default '[]'");
    }

    private void version4to5(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + SQLSentence.TABLE_CHAT_MESSAGE + " ADD " + SQLSentence.MESSAGE_SMALL_MAIL_ID + " TEXT " + " default '0'");
    }

    private void version3to4(SQLiteDatabase db) {
        db.execSQL(SQLSentence.getCreateSmallMailSql());
    }

    private void version2to3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + SQLSentence.TABLE_CHAT_UNREAD_MESSAGE + " ADD " + SQLSentence.UNREAD_MSG_ID + " TEXT " + " default '0'");
        db.execSQL(SQLSentence.getCreateChatWindowInfoTableString());
        db.execSQL(SQLSentence.getCreateChatSummarySql());
    }

    private void version1to2(SQLiteDatabase db) {
        db.execSQL(SQLSentence.getCreateUnReadMsgTableString());
        db.execSQL("CREATE INDEX " + "chat_index ON " +
                SQLSentence.TABLE_CHAT_MESSAGE + " ( " + SQLSentence.MESSAGE_GROUP_ID + " ) ");
    }

/*    public static class UserInfoCursor extends CursorWrapper {

        public UserInfoCursor(Cursor cursor) {
            super(cursor);
        }

        public UserInfoBean getUserInfoBean() {
            UserInfoBean userInfoBean = new UserInfoBean();
            userInfoBean.avatar = getInt(getColumnIndex(SQLSentence.COLUMN_USER_AVATAR));
            userInfoBean.company = getString(getColumnIndex(SQLSentence.COLUMN_USER_COMPANY));
            userInfoBean.companyId = getString(getColumnIndex(SQLSentence.COLUMN_USER_COMPANYID));
            userInfoBean.department = getString(getColumnIndex(SQLSentence.COLUMN_USER_DEPARTMENT));
            userInfoBean.departmentId = getString(getColumnIndex(SQLSentence.COLUMN_USER_DEPARTMENTID));
            userInfoBean.email = getString(getColumnIndex(SQLSentence.COLUMN_USER_EMAIL));
            userInfoBean.name = getString(getColumnIndex(SQLSentence.COLUMN_USER_NAME));
            userInfoBean.id = getString(getColumnIndex(SQLSentence.COLUMN_USER_ID));
            userInfoBean.uid = getString(getColumnIndex(SQLSentence.COLUMN_USER_UID));
            userInfoBean.tel = getString(getColumnIndex(SQLSentence.COLUMN_USER_TEL));
            return userInfoBean;
        }
    }*/


    static class MessageBeanCursor extends CursorWrapper {

        MessageBeanCursor(Cursor cursor) {
            super(cursor);
        }

        MessageBean getMessageBean(String taskId) {
            MessageBean messageBean = new MessageBean();
            messageBean.content = getString(getColumnIndex(SQLSentence.MESSAGE_CONTENT));
            messageBean.createTime = getLong(getColumnIndex(SQLSentence.MESSAGE_CREATETIME));
            messageBean.sendMessageUser = new UserInfoBean(getString(getColumnIndex(SQLSentence.MESSAGE_FROM)));
            messageBean.type = getInt(getColumnIndex(SQLSentence.MESSAGE_TYPE));
            messageBean.groupId = getString(getColumnIndex(SQLSentence.MESSAGE_GROUP_ID));
            messageBean.isSendSuccess = getInt(getColumnIndex(SQLSentence.MESSAGE_ISSEND)) > 0;
            messageBean.id = getString(getColumnIndex(SQLSentence.MESSAGE_MAILID));
            messageBean.to = getString(getColumnIndex(SQLSentence.MESSAGE_TO));
            messageBean.initToList();
            messageBean.uuid = getString(getColumnIndex(SQLSentence.MESSAGE_UUID));
            messageBean.sendTime = getLong(getColumnIndex(SQLSentence.MESSAGE_SENDTIME));
            messageBean.taskId = taskId;
            if (messageBean.type == MessageBean.IMAGE_MESSAGE_SEND || messageBean.type == MessageBean.IMAGE_MESSAGE_RECIVE) {
                messageBean.imgSrc = XmlHelper.praseChatImageMsg(messageBean.content);
                if (messageBean.imgSrc == null)
                    messageBean.imagePath = messageBean.content;
                else
                    messageBean.imagePath = FileUtil.getImageFilePath() + messageBean.groupId + File.separator + messageBean.imgSrc.id + ".png";
            } else if (messageBean.isSmallMailMsg()) {
                messageBean.smallMailId = getString(getColumnIndex(SQLSentence.MESSAGE_SMALL_MAIL_ID));
            } else if (messageBean.type == MessageBean.ATTACHMENT_MESSAGE_SEND || messageBean.type == MessageBean.ATTACHMENT_MESSAGE_RECEIVE) {
                messageBean.attachmentBean = XmlHelper.getFileMsgXmlBean(messageBean.content);
                if (messageBean.attachmentBean == null) {
                    messageBean.attachmentBean = new TaskAttachmentBean(messageBean.content, TextUtils.isEmpty(taskId) ? messageBean.groupId : messageBean.taskId);
                } else {
                    messageBean.attachmentBean.taskId = TextUtils.isEmpty(taskId) ? messageBean.groupId : messageBean.taskId;
                }
            }
            return messageBean;
        }
    }

    static class UnReadMessageBeanCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        UnReadMessageBeanCursor(Cursor cursor) {
            super(cursor);
        }

        public UnReadMessageBean getMessageBean() {
            long createTime;
            String taskId;
            String groupId;
            long sendTime;
            int type;
            createTime = getLong(getColumnIndex(SQLSentence.UNREAD_MSG_CREATETIME));
            sendTime = getLong(getColumnIndex(SQLSentence.UNREAD_MSG_SENDTIME));
            taskId = getString(getColumnIndex(SQLSentence.UNREAD_MSG_TASKID));
            groupId = getString(getColumnIndex(SQLSentence.UNREAD_MSG_GROUPID));
            type = getInt(getColumnIndex(SQLSentence.UNREAD_TYPE));

            UnReadMessageBean mBean = new UnReadMessageBean(sendTime, createTime, taskId, groupId, type);
            mBean.msgId = getString(getColumnIndex(SQLSentence.UNREAD_MSG_ID));
            mBean.isNeedShowNum = getInt(getColumnIndex(SQLSentence.UNREAD_IS_NEED_SHOW_NUMBER));
            mBean.content = getString(getColumnIndex(SQLSentence.MESSAGE_CONTENT));
            return mBean;
        }
    }

    static class VChatBeanCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        VChatBeanCursor(Cursor cursor) {
            super(cursor);
        }

        VChatBean getVChatBean() {
            String groupId = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_SUMMARY_GROUPID));
            String topic = getString(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_TITLE));
            String members = getString(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_MEMBER));
            int isGroup = getInt(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_ISGROUP));
            int type = getInt(getColumnIndex(SQLSentence.MESSAGE_TYPE));
            VChatBean bean = new VChatBean();
            bean.groupId = groupId;
            bean.topic = topic;

            bean.isGroup = isGroup;
            int lastMsgIndex = getColumnIndex(SQLSentence.MESSAGE_CONTENT);
            int lastSendTimeIndex = getColumnIndex(SQLSentence.MESSAGE_SENDTIME);
            int lastIdIndex = getColumnIndex(SQLSentence.MESSAGE_MAILID);


            if (isNull(lastMsgIndex) && isNull(lastSendTimeIndex) && isNull(lastIdIndex)) {
                bean.lastMsg = getString(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_LAST_MSG));
                bean.lastChatTime = getLong(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_SEND_TIME));
                bean.msgId = getString(getColumnIndex(SQLSentence.CHAT_WINDOW__SUMMARY_LAST_MSG_ID));
            } else {
                if (type == MessageBean.RECALL_MESSAGE) {
                    String from = getString(getColumnIndex(SQLSentence.MESSAGE_FROM));
                    try {
                        JSONObject fromUser = new JSONObject(from);
                        bean.lastMsg = fromUser.optString("name") + "撤回了一条消息";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    bean.lastMsg = getString(lastMsgIndex);
                }
                long lastSendTime = getLong(lastSendTimeIndex);
                String lastId = getString(lastIdIndex);
                bean.lastChatTime = lastSendTime;
                bean.msgId = lastId;
            }
            try {
                bean.setMember(members);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            if (TextUtils.isEmpty(bean.lastMsg))
//                bean.lastMsg = "无消息内容...";
//
//            if (bean.lastMsg.contains("weiliao_images") && bean.lastMsg.startsWith("<p><img"))
//                bean.lastMsg = "[图片]";
//            Log.i(TAG, "getVChatBean: " + bean.topic + bean.isGroup);
            Log.i(TAG, "getVChatBean: " + bean.topic + "-----------" + bean.lastMsg);
            return bean;
        }
    }

    static class WindowInfoCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        WindowInfoCursor(Cursor cursor) {
            super(cursor);
        }

        ChatWindowInfoBean getChatWindowInfo() {
            ChatWindowInfoBean windowInfoBean = new ChatWindowInfoBean();
            windowInfoBean.groupId = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_GROUPID));
            windowInfoBean.taskId = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_TASKID));
            windowInfoBean.isSingle = getLong(getColumnIndex(SQLSentence.CHAT_WINDOW_IS_SINGLE)) > 0;
            windowInfoBean.createUser = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_CREATE_USER));
            windowInfoBean.mailId = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_MAILID));
            windowInfoBean.taskTitle = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_TITLE));
            windowInfoBean.taskPlace = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_TASK_PLACE));
            windowInfoBean.memberNames = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_MEMBER_NAMES));
            String members = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_MEMBER));
            Log.i(TAG, "getChatWindowInfo: " + windowInfoBean.toString());
            String lastTo = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_LAST_TO));
            String exitMembers = getString(getColumnIndex(SQLSentence.CHAT_WINDOW_EXIT_MEMBER));

            windowInfoBean.initMemberLists(members, lastTo, exitMembers);
            return windowInfoBean;
        }
    }

    static class SmallMailCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        SmallMailCursor(Cursor cursor) {
            super(cursor);
        }

        SmallMailBean getSmallMailBean() {
            SmallMailBean smallMailBean = new SmallMailBean();
            smallMailBean.mailId = getString(getColumnIndex(SQLSentence.SMALL_MAIL__ID));
            smallMailBean.messageId = getString(getColumnIndex(SQLSentence.SMALL_MAIL_MESSAGE_ID));
            smallMailBean.sendTime = getLong(getColumnIndex(SQLSentence.SMALL_MAIL__SEND_TIME));
            smallMailBean.sendUser = new UserInfoBean(getString(getColumnIndex(SQLSentence.SMALL_MAIL_FROM)));
            smallMailBean.initToUserList(getString(getColumnIndex(SQLSentence.SMALL_MAIL_SEND_TO)));
            smallMailBean.subject = getString(getColumnIndex(SQLSentence.SMALL_MAIL_SUBJECT));
            smallMailBean.content = getString(getColumnIndex(SQLSentence.SMALL_MAIL_CONTENT));
            smallMailBean.initAttachmentList(getString(getColumnIndex(SQLSentence.SMALL_MAIL_ATTACHMENTS)));
            return smallMailBean;
        }
    }

    static class SocketEventCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        SocketEventCursor(Cursor cursor) {
            super(cursor);
        }

        SocketEvent getSocketEvent() {
            String fbId = getString(getColumnIndex(SQLSentence.EVENT_FEED_BACK_ID));
            int eventCode = getInt(getColumnIndex(SQLSentence.EVENT_CODE));
            String info = getString(getColumnIndex(SQLSentence.EVENT_INFO));
            SocketEvent event = new SocketEvent(fbId, eventCode, info);
            event.isServerDelete = getInt(getColumnIndex(SQLSentence.IS_SERVER_EVENT_DELETE)) > 0;
            event.isClientProcess = getInt(getColumnIndex(SQLSentence.IS_CLIENT_HAS_PROCESS)) > 0;
            return event;
        }
    }
}
