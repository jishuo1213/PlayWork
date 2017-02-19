package com.inspur.playwork.utils.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.inspur.playwork.actions.DbAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailContacts;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.utils.db.bean.MailDirectory;
import com.inspur.playwork.utils.db.bean.MailTask;
import com.inspur.playwork.utils.db.dao.DaoMaster;
import com.inspur.playwork.utils.db.dao.DaoSession;
import com.inspur.playwork.utils.db.dao.MailAccountDao;
import com.inspur.playwork.utils.db.dao.MailAttachmentDao;
import com.inspur.playwork.utils.db.dao.MailContactsDao;
import com.inspur.playwork.utils.db.dao.MailDetailDao;
import com.inspur.playwork.utils.db.dao.MailDirectoryDao;
import com.inspur.playwork.utils.db.dao.MailTaskDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.query.CloseableListIterator;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by jianggf on 2015/10/28.
 */
public class DBOperation {

    private static final String TAG = "DBOperationFan";

    private String userId = "";

    private DBHelper dbHelper;
    private SQLiteStatement insertChatWindowInfo;
    private SQLiteStatement insertMessageStatement;
    private SQLiteStatement insertUnReadMsgStatement;
    private SQLiteStatement upsertMessageStatement;
    private SQLiteStatement updateMessageStatement;
    private SQLiteStatement updateImageMessageStatement;
    private SQLiteStatement insertChatSummaryStatement;
    private SQLiteStatement upsertChatSummaryStatement;
    private SQLiteStatement insertSmallMailStatement;
//    private SQLiteStatement queryUnReadMsgStatement;

    private MailDetailDao mailDetailDao;
    private MailAccountDao mailAccountDao;
    private MailAttachmentDao mailAttachmentDao;
    private MailDirectoryDao mailDirectoryDao;
    private MailContactsDao mailContactsDao;
    private MailTaskDao mailTaskDao;
    private DaoMaster.DevOpenHelper mailDBHelper;

    public DBOperation() {

    }

    public void init(Context context) {

        this.userId = PreferencesHelper.getInstance().getCurrentUser().id;
        dbHelper = new DBHelper(context, this.userId + "_playwork_db");
        String mailDBName = userId + "_mail-db";
        mailDBHelper = new DaoMaster.DevOpenHelper(context, mailDBName, null);

        if (!Dispatcher.getInstance().isRegistered(this))
            Dispatcher.getInstance().register(this);

        //生成邮件表相关的操作对象
        SQLiteDatabase db = mailDBHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        mailDetailDao = daoSession.getMailDetailDao();
        mailAccountDao = daoSession.getMailAccountDao();
        mailAttachmentDao = daoSession.getMailAttachmentDao();
        mailDirectoryDao = daoSession.getMailDirectoryDao();
        mailContactsDao = daoSession.getMailContactsDao();
        mailTaskDao = daoSession.getMailTaskDao();
        //打印SQL日志
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    public void init(Context context, String userId) {
        Log.i(TAG, "init: " + (this.userId == null) + "userid" + userId + "this.userId" + this.userId);
        if (this.userId.
                equals(userId))
            return;
        this.userId = userId;
        init(context);
    }

    /**
     * 初始化对象
     */

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(DbAction dbAction) {
        SparseArray<Object> data = dbAction.getActiontData();
        switch (dbAction.getActionType()) {
            case DataBaseActions.GET_GROUP_CHAT_HISTORY:
                queryGroupChatHistory((String) data.get(0), (long) data.get(1), (String) data.get(2));
                break;
            case DataBaseActions.INSERT_ONE_CHAT_MESSAGE:
                insertChatMessage((MessageBean) data.get(0));
                break;
            case DataBaseActions.INSERT_CHAT_MESSAGE_LIST:
                if (data.get(0) != null) {
                    if (dbAction.getActiontData().get(1) != null)
                        //noinspection unchecked
                        insertUnReadChatMessageList((List<MessageBean>) data.get(0));
                    else
                        //noinspection unchecked
                        insertChatMessageList((List<MessageBean>) data.get(0));
                }
                break;
            case DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID:
                updateMessageByUUID((int) data.get(0), data);
                break;
            case DataBaseActions.DELETE_CHAT_MESSAGE_BY_GROUPID:
                deleteChatHistory((String) data.get(0));
                break;
            case DataBaseActions.DELETE_ONE_MSG_BY_MSGID:
                deleteOnUnReadByMsgId((String) data.get(0), (String) data.get(1));
                deleteOneMsgByMsgId((String) data.get(0), (String) data.get(1));
                break;
            case DataBaseActions.INSERT_CHAT_UNREAD_MESSAGE_LIST:
                //noinspection unchecked
                insertUnReadMessageList((List<UnReadMessageBean>) data.get(0));
                break;
            case DataBaseActions.INSERT_ONE_CHAT_UNREAD_MESSAGE:
                insertOneUnReadMessage((UnReadMessageBean) data.get(0));
                break;
            case DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE:
                if (dbAction.getActiontData().get(0) != null) {
                    queryNormalChatUnReadMsg((int) dbAction.getActiontData().get(0));
                    return;
                }
                queryNormalChatUnReadMsg();
                break;
            case DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID:
                deleteUnReadMsgByGroupId((String) data.get(0));
                break;
            case DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO:
                insertOneChatWindowInfo((ChatWindowInfoBean) data.get(0));
                break;
            case DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID:

                queryNormalChatWindowInfo((String) dbAction.getActiontData().get(0), (String) dbAction.getActiontData().get(1), (String) dbAction.getActiontData().get(2));
                break;
            case DataBaseActions.DELETE_CHAT_WINDOW_BY_ID:
                deleteChatWindowInfo((String) dbAction.getActiontData().get(0));
                break;
            case DataBaseActions.QUERY_NORMAL_CHAT_LIST:
                queryNormalChatList();
                break;
            case DataBaseActions.INSERT_ONE_CHAT_WINDOW_SUMMARY:
                insertOneChatSummary((VChatBean) dbAction.getActiontData().get(0), (boolean) dbAction.getActiontData().get(1), (boolean) dbAction.getActiontData().get(2));
                break;
            case DataBaseActions.DELETE_ONE_CHAT_SUMMARY_BY_ID:
                break;
            case DataBaseActions.INSERT_CHAT_SUMMARY_LIST:
                //noinspection unchecked
                insertChatSummaryList((ArrayList<VChatBean>) dbAction.getActiontData().get(0));
                break;
            case DataBaseActions.UPDATE_CHAT_WINDOW_BY_GROUP_ID:
                int type = (int) dbAction.getActiontData().get(0);
                updateChatWindowInfo((ChatWindowInfoBean) dbAction.getActiontData().get(1), type);
                break;
            case DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER:
                JSONObject msg = (JSONObject) dbAction.getActiontData().get(0);
                String id = (String) dbAction.getActiontData().get(1);
                if (id != null) {
                    updateAddMemberChatWindowInfo(msg.optString("members"), msg.optString("lastTo"), msg.optString("exited"), id);
                    return;
                }
                updateWhenAddMembers(msg);
                break;
            case DataBaseActions.UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT:
                updateWhenExitMember((String) dbAction.getActiontData().get(0), (String) dbAction.getActiontData().get(1));
                break;
            case DataBaseActions.RENAME_CHAT_SUBJECT:
                updateChatSubject((String) dbAction.getActiontData().get(0), (String) dbAction.getActiontData().get(1));
                break;
            case DataBaseActions.INSERT_ONE_SMALL_MAIL:
                insertOneSmallMail((SmallMailBean) dbAction.getActiontData().get(0));
                break;
            case DataBaseActions.DELETE_SMALL_MAIL_BY_ID:
                deleteOneSmallMailById((String) dbAction.getActiontData().get(0));
                break;
            case DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID:
                querySmallMailByMessageId((String) dbAction.getActiontData().get(0), (String) dbAction.getActiontData().get(1));
                break;
            /**
             * 邮件相关的数据库操作
             * */
            //获取某一邮箱账号下的所有邮件的UID列表
            case DataBaseActions.QUERY_ALL_MAIL_UID_LIST:
                getAllMailUidList((String) data.get(0));
                break;
            //获取某一目录下的邮件信息列表
            case DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID:
                getMailListByDirID((String) data.get(0), (Long) data.get(1));
                break;
            //获取某一目录下的邮件信息列表
            case DataBaseActions.QUERY_OUT_BOX_MAIL_LIST:
                getOutBoxMailList((String) data.get(0));
                break;
            //获取某一目录下的邮件信息列表（分页）
            case DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE:
                getMailListByDirIDWithPage((String) data.get(0), (Long) data.get(1), (int) data.get(2), (int) data.get(3));
                break;
            //获取未读邮件信息列表
            case DataBaseActions.QUERY_UNREAD_MAIL_LIST:
                getUnreadMailList((String) data.get(0));
                break;
            //获取已标记的邮件信息列表
            case DataBaseActions.QUERY_MARKED_MAIL_LIST:
                getMarkedMailList((String) data.get(0));
                break;
            //获取单封邮件详情（根据主键ID）
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_ID:
                getMailDetailById((Long) data.get(0));
                break;
            //获取单封邮件详情（根据MessageID）
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_MESSAGE_ID:
                getMailDetailByMessageId((String) data.get(0));
                break;
            //获取某一邮箱账号下的所有附件列表
            case DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST:
                getMailAttachmentList((String) data.get(0));
                break;
            //插入一条邮件信息记录
            case DataBaseActions.INSERT_ONE_MAIL_MESSAGE:
                //noinspection unchecked
                insertOneMailMessage((MailDetail) data.get(0), (List<MailAttachment>) data.get(1));
                break;
            //更新邮件详情表
            case DataBaseActions.UPDATE_MAIL_DETAIL:
                updateMailDetail((MailDetail) data.get(0), (ArrayList<MailAttachment>) data.get(1));
                break;
            //更新邮件目录
            case DataBaseActions.CHANGE_MAIL_DIRECTORY:
                changeMailDirectory((String) data.get(0), (Long) data.get(1), (Long) data.get(2));
                break;
            //更新邮件发送状态
            case DataBaseActions.CHANGE_MAIL_SEND_STATUS:
                updateMailSendStatus((String) data.get(0), (Long) data.get(1), (Long) data.get(2));
                break;
            //更新邮件发送进度
            case DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE:
                updateMailSentPercentage((String) data.get(0), (Long) data.get(1), (Long) data.get(2));
                break;
            //删除一封邮件
            case DataBaseActions.DELETE_ONE_MAIL:
                deleteMail((String) data.get(0), (long) data.get(1));
                break;
            //插入一条邮件附件信息
            case DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT:
                saveOneMailAttachment((MailAttachment) data.get(0));
                break;
            //获取用户的邮箱账号列表
            case DataBaseActions.QUERY_MAIL_ACCOUNT_LIST:
                getMailAccounts((String) data.get(0));
                break;
            //保存用户的一个邮箱账号信息（插入或更新）
            case DataBaseActions.SAVE_ONE_MAIL_ACCOUNT:
                saveMailAccount((MailAccount) data.get(0));
                break;
            //删除用户的一个邮箱账号
            case DataBaseActions.DELETE_ONE_MAIL_ACCOUNT:
                deleteMailAccount((long) data.get(0));
                break;
            //获取邮箱的目录列表
            case DataBaseActions.QUERY_MAIL_DIRECTORY_LIST:
                getMailDirectoryListByEmail((String) data.get(0));
                break;
            //保存一条邮箱目录（插入或更新）
            case DataBaseActions.SAVE_ONE_MAIL_DIRECTORY:
                saveMailDirectory((MailDirectory) data.get(0));
                break;
            //删除邮箱目录
            case DataBaseActions.DELETE_ONE_MAIL_DIRECTORY:
                deleteMailDirectory((String) data.get(0), (long) data.get(1));
                break;
            //获取用户的邮件联系人列表
            case DataBaseActions.QUERY_MAIL_CONTACTS_LIST:
                getMailContacts((String) data.get(0));
                break;
            //保存用户的一个邮件联系人信息（插入或更新）
            case DataBaseActions.SAVE_ONE_MAIL_CONTACTS:
                saveMailContacts((MailContacts) data.get(0));
                break;
            //保存用户的一个邮件联系人信息（插入或更新）
            case DataBaseActions.SAVE_ONE_MAIL_TASK:
                saveMailTask((MailTask) data.get(0));
                break;
            //保存用户的一个邮件联系人信息（插入或更新）
            case DataBaseActions.GET_ALL_MAIL_TASK:
                getAllMailTask();
                break;
            //保存用户的一个邮件联系人信息（插入或更新）
            case DataBaseActions.DELETE_MAIL_TASK_BY_ID:
                deleteMailTaskById((Long) data.get(0));
                break;
            //保存用户的一个邮件联系人信息（插入或更新）
            case DataBaseActions.UPDATE_MAIL_TASK_RCPTS:
                updateMailTaskRcpts((MailTask) data.get(0));
                break;
        }
    }

    public void printAllMessageHistory() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(SQLSentence.TABLE_CHAT_MESSAGE, null, SQLSentence.MESSAGE_GROUP_ID + "= ? ", new String[]{"5865daed54ffdd91066579b6"}, null, null, null);
        DBHelper.MessageBeanCursor messageBeanCursor = new DBHelper.MessageBeanCursor(cursor);
        int count = messageBeanCursor.getCount();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(FileUtil.getDownloadPath() + "history.txt")));
            if (count > 0 && messageBeanCursor.moveToFirst()) {
                writer.write("[");
                do {
                    MessageBean messageBean = messageBeanCursor.getMessageBean(null);
                    writer.write(messageBean.toJson());
                    writer.write(",");
                    writer.newLine();
                } while (cursor.moveToNext());
            }
            writer.write("]");

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cursor.close();
    }

    private void deleteOnUnReadByMsgId(String groupId, String msgId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, SQLSentence.UNREAD_MSG_GROUPID + " = ? " + "and " + "(" + SQLSentence.UNREAD_MSG_ID + " = ? " + ")", new String[]{groupId, msgId});
        Log.i(TAG, "result" + result);
    }

    private void querySmallMailByMessageId(String mark, String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(SQLSentence.TABLE_SMALL_MAIL, null, SQLSentence.SMALL_MAIL__ID + " = ? OR " +
                SQLSentence.SMALL_MAIL_MESSAGE_ID + " = ?", new String[]{id, id}, null, null, null);
        DBHelper.SmallMailCursor smallMailCursor = new DBHelper.SmallMailCursor(cursor);
        int count = smallMailCursor.getCount();
        Log.i(TAG, "querySmallMailByMessageId: " + count);
        if (count > 0 && cursor.moveToFirst())
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID,
                    smallMailCursor.getSmallMailBean(), mark);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID, null, mark, id);
        cursor.close();
    }

    private void deleteOneSmallMailById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLSentence.TABLE_SMALL_MAIL, SQLSentence.SMALL_MAIL__ID + " = ? OR " + SQLSentence.SMALL_MAIL_MESSAGE_ID + " = ?", new String[]{id, id});
    }

    private void insertOneSmallMail(SmallMailBean smallMailBean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (insertSmallMailStatement == null) {
            insertSmallMailStatement = db.compileStatement(SQLSentence.getInsertSmallMailSql());
        }
        if (TextUtils.isEmpty(smallMailBean.messageId)) {
            insertSmallMailStatement.bindNull(1);
        } else {
            insertSmallMailStatement.bindString(1, smallMailBean.messageId);
        }
        insertSmallMailStatement.bindString(2, smallMailBean.subject);
        insertSmallMailStatement.bindString(3, smallMailBean.sendUser.getUserJson().toString());
        insertSmallMailStatement.bindString(4, smallMailBean.sendToString);
        insertSmallMailStatement.bindString(5, smallMailBean.content);
        insertSmallMailStatement.bindString(6, smallMailBean.mailId);
        insertSmallMailStatement.bindLong(7, smallMailBean.sendTime);
        insertSmallMailStatement.bindString(8, smallMailBean.attchmentString);

        insertSmallMailStatement.executeInsert();
    }

    private void updateChatSubject(String groupId, String subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            SQLiteStatement statement = db.compileStatement("update " + SQLSentence.TABLE_CHAT_WINDWO_INFO + " set " +
                    SQLSentence.CHAT_WINDOW_TITLE + " = ? " +
                    " where " + SQLSentence.CHAT_WINDOW_GROUPID + " = ? OR " + SQLSentence.CHAT_WINDOW_TASKID + "= ? ");
            statement.bindString(1, subject);
            statement.bindString(2, groupId);
            statement.bindString(3, groupId);

            SQLiteStatement statement2 = db.compileStatement("update " + SQLSentence.TABLE_CHAT_WINDWO_SUMMARY + " set " +
                    SQLSentence.CHAT_WINDOW__SUMMARY_TITLE + " = ? " +
                    " where " + SQLSentence.CHAT_WINDOW_SUMMARY_GROUPID + " = ?");
            statement2.bindString(1, subject);
            statement2.bindString(2, groupId);

            if ((statement.executeUpdateDelete() > 0) || (statement2.executeUpdateDelete() > 0)) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }
    }

    private void updateWhenAddMembers(JSONObject msg) {
        String groupId = msg.optString("groupId");
        JSONArray[] result = queryMembersByGroupId(groupId);
        if (result != null) {
            JSONArray members = result[0];
            JSONArray addUsers = msg.optJSONArray("addMembers");
            int count = addUsers.length();
            for (int i = 0; i < count; i++) {
                members.put(addUsers.optJSONObject(i));
            }
            JSONArray exitUsers = msg.optJSONArray("exitedMember");
            updateAddMemberChatWindowInfo(members.toString(), exitUsers.toString(), groupId);
        }
    }

    private void updateWhenExitMember(String exitId, String groupId) {
//        String exitId = msg.optString("exitUser");
        JSONObject exitUser = null;
//        String groupId = msg.optString("groupId");
        if (exitId.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(SQLSentence.TABLE_CHAT_WINDWO_SUMMARY, SQLSentence.CHAT_WINDOW_SUMMARY_GROUPID + " = ? ", new String[]{groupId});
            db.delete(SQLSentence.TABLE_CHAT_WINDWO_INFO, SQLSentence.CHAT_WINDOW_GROUPID + " = ?", new String[]{groupId});
            db.delete(SQLSentence.TABLE_CHAT_MESSAGE, SQLSentence.MESSAGE_GROUP_ID + " = ? ", new String[]{groupId});
            db.delete(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, SQLSentence.UNREAD_MSG_GROUPID + " = ? OR " + SQLSentence.UNREAD_MSG_TASKID + " = ?", new String[]{groupId, groupId});
            return;
        }
        JSONArray[] result = queryMembersByGroupId(groupId);
        JSONArray newMembers = new JSONArray();
        JSONArray newLastTo = new JSONArray();
        if (result != null) {
            JSONArray members = result[0];
            int count = members.length();
            for (int i = 0; i < count; i++) {
                JSONObject member = members.optJSONObject(i);
                if (!member.optString("id").equals(exitId)) {
                    newMembers.put(member);
                } else {
                    exitUser = member;
                }
            }

            JSONArray lastTo = result[2];
            int lastToCount = lastTo.length();
            for (int i = 0; i < lastToCount; i++) {
                JSONObject member = lastTo.optJSONObject(i);
                if (!member.optString("id").equals(exitId)) {
                    newLastTo.put(member);
                }
            }

            JSONArray exitUsers = result[1];
            if (exitUser != null)
                exitUsers.put(exitUser);
            Log.i(TAG, "updateWhenExitMember: " + newMembers.toString() + "========" + newLastTo.toString());
            updateAddMemberChatWindowInfo(newMembers.toString(), newLastTo.toString(), exitUsers.toString(), groupId);
        }
    }

    private void updateAddMemberChatWindowInfo(String allMembers, String exitMembers, String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("update " + SQLSentence.TABLE_CHAT_WINDWO_INFO + " set " +
                SQLSentence.CHAT_WINDOW_MEMBER + " = ? ," +
                SQLSentence.CHAT_WINDOW_EXIT_MEMBER + " = ? " +
                " where " + SQLSentence.CHAT_WINDOW_GROUPID + " = ?");
        statement.bindString(1, allMembers);
        statement.bindString(2, exitMembers);
        statement.bindString(3, groupId);
        statement.executeUpdateDelete();
    }

    private void updateAddMemberChatWindowInfo(String allMembers, String lastToMembers, String exitMembers, String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("update " + SQLSentence.TABLE_CHAT_WINDWO_INFO + " set " +
                SQLSentence.CHAT_WINDOW_MEMBER + " = ? ," +
                SQLSentence.CHAT_WINDOW_LAST_TO + " = ? ," +
                SQLSentence.CHAT_WINDOW_EXIT_MEMBER + " = ? " +
                " where " + SQLSentence.CHAT_WINDOW_GROUPID + " = ? OR " + SQLSentence.CHAT_WINDOW_TASKID + " = ?");
        statement.bindString(1, allMembers);
        statement.bindString(2, lastToMembers);
        statement.bindString(3, exitMembers);
        statement.bindString(4, groupId);
        statement.bindString(5, groupId);
        statement.executeUpdateDelete();
    }

    private JSONArray[] queryMembersByGroupId(String groupId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLSentence.TABLE_CHAT_WINDWO_INFO, new String[]{SQLSentence.CHAT_WINDOW_MEMBER,
                        SQLSentence.CHAT_WINDOW_LAST_TO,
                        SQLSentence.CHAT_WINDOW_EXIT_MEMBER},
                SQLSentence.CHAT_WINDOW_GROUPID + "= ? ", new String[]{groupId}, null, null, null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            String members = cursor.getString(cursor.getColumnIndex(SQLSentence.CHAT_WINDOW_MEMBER));
            String exitMembers = cursor.getString(cursor.getColumnIndex(SQLSentence.CHAT_WINDOW_EXIT_MEMBER));
            String lastTo = cursor.getString(cursor.getColumnIndex(SQLSentence.CHAT_WINDOW_LAST_TO));
            try {
                cursor.close();
                JSONArray member = new JSONArray(members);
                JSONArray exitMember = new JSONArray(exitMembers);
                JSONArray lastToMember = new JSONArray(lastTo);
                return new JSONArray[]{member, exitMember, lastToMember};
            } catch (JSONException e) {
                return null;
            }
        }
        cursor.close();
        return null;
    }

    private void updateChatWindowInfo(ChatWindowInfoBean chatWindowInfoBean, int type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (type == 1) {
            Log.i(TAG, chatWindowInfoBean.toString());
            SQLiteStatement updateWindowInfoStatement = db.compileStatement(SQLSentence.getUpdateWindowInfoSql(1));
            updateWindowInfoStatement.bindString(1, chatWindowInfoBean.memberString);
            updateWindowInfoStatement.bindString(2, chatWindowInfoBean.exitString);
            updateWindowInfoStatement.bindString(3, chatWindowInfoBean.lastToString);
            updateWindowInfoStatement.bindString(4, chatWindowInfoBean.memberNames);
            updateWindowInfoStatement.bindString(5, chatWindowInfoBean.groupId);

            updateWindowInfoStatement.executeUpdateDelete();

            SQLiteStatement updateChatSummaryStatement = db.compileStatement(SQLSentence.getUpdateWindowSummarySql(1));
            updateChatSummaryStatement.bindString(1, chatWindowInfoBean.memberString);
            updateChatSummaryStatement.bindString(3, chatWindowInfoBean.groupId);
            Log.i(TAG, "updateChatWindowInfo: " + chatWindowInfoBean.taskTitle + "====" + chatWindowInfoBean.memberNames);
            if (TextUtils.isEmpty(chatWindowInfoBean.taskTitle)) {
                updateChatSummaryStatement.bindString(2, chatWindowInfoBean.memberNames);
            } else {
                updateChatSummaryStatement.bindString(2, chatWindowInfoBean.taskTitle);
            }
            updateChatSummaryStatement.executeUpdateDelete();
        } else if (type == 2) {
            SQLiteStatement updateWindowInfoStatement = db.compileStatement(SQLSentence.getUpdateWindowInfoSql(2));
            updateWindowInfoStatement.bindString(1, chatWindowInfoBean.taskTitle);
            updateWindowInfoStatement.bindString(2, chatWindowInfoBean.groupId);
            updateWindowInfoStatement.executeUpdateDelete();

            SQLiteStatement updateChatSummaryStatement = db.compileStatement(SQLSentence.getUpdateWindowSummarySql(2));
            updateChatSummaryStatement.bindString(1, chatWindowInfoBean.taskTitle);
            updateChatSummaryStatement.bindString(2, chatWindowInfoBean.groupId);
            updateChatSummaryStatement.executeUpdateDelete();
        }
    }

    private void insertOneChatSummary(VChatBean vChatBean, boolean isNeedSetLastChat, boolean sendResult) {
        Log.d(TAG, "insertOneChatSummary() called with: vChatBean = [" + vChatBean + "], isNeedSetLastChat = [" + isNeedSetLastChat + "], sendResult = [" + sendResult + "]");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (isNeedSetLastChat) {
            Cursor cursor = db.query(SQLSentence.TABLE_CHAT_MESSAGE, new String[]{SQLSentence.MESSAGE_MAILID,
                            SQLSentence.MESSAGE_CONTENT, SQLSentence.MESSAGE_SENDTIME}, SQLSentence.MESSAGE_GROUP_ID + " = ? ", new String[]{vChatBean.groupId},
                    null, null, SQLSentence.MESSAGE_SENDTIME + " DESC ", 1 + "");

            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                vChatBean.lastMsg = cursor.getString(cursor.getColumnIndex(SQLSentence.MESSAGE_CONTENT));
                vChatBean.lastChatTime = cursor.getLong(cursor.getColumnIndex(SQLSentence.MESSAGE_SENDTIME));
                vChatBean.msgId = cursor.getString(cursor.getColumnIndex(SQLSentence.MESSAGE_MAILID));
            }
            cursor.close();
        }
        doInsertVChatBean(vChatBean, db);
        if (sendResult)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONT_CHAT_SUMMARY_SUCCESS, vChatBean);
    }

    private void doInsertVChatBean(VChatBean vChatBean, SQLiteDatabase db) {
        if (upsertChatSummaryStatement == null)
            upsertChatSummaryStatement = db.compileStatement(SQLSentence.getUpsertChatSummarySql());
        fillVChatBeanData(upsertChatSummaryStatement, vChatBean);
        upsertChatSummaryStatement.bindString(9, vChatBean.groupId);
        long result = upsertChatSummaryStatement.executeInsert();
        Log.i(TAG, "doInsertVChatBean: result" + result);
    }

    private void insertOneChatWindowInfo(ChatWindowInfoBean chatWindowInfoBean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (insertChatWindowInfo == null)
            insertChatWindowInfo = db.compileStatement(SQLSentence.getInsertChatWindowString());
        Log.i(TAG, "insertOneChatWindowInfo: " + chatWindowInfoBean);
        fillChatWindowInfo(chatWindowInfoBean, insertChatWindowInfo);
        insertChatWindowInfo.executeInsert();
    }


    private void queryNormalChatWindowInfo(String groupId, String topic, String uuid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (topic == null) {
            cursor = new DBHelper.WindowInfoCursor(db.query(SQLSentence.TABLE_CHAT_WINDWO_INFO, null, SQLSentence.CHAT_WINDOW_TASKID + " = ? ", new String[]{groupId}, null, null, null));
        } else {
            cursor = new DBHelper.WindowInfoCursor(db.query(SQLSentence.TABLE_CHAT_WINDWO_INFO, null, SQLSentence.CHAT_WINDOW_GROUPID + " = ? ", new String[]{groupId}, null, null, null));
        }
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            ChatWindowInfoBean result = ((DBHelper.WindowInfoCursor) cursor).getChatWindowInfo();
            Log.i(TAG, result.toString());
            if (topic == null)
                Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_CHAT_BY_ID_SUCCESS, result, 2);
            else
                Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_CHAT_BY_ID_SUCCESS, result, 1, uuid);
        } else {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_RESULT_IS_EMPTY, DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID, groupId, topic, uuid);
        }
    }

    private void insertChatSummaryList(ArrayList<VChatBean> vChatBeans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (insertChatSummaryStatement == null)
            insertChatSummaryStatement = db.compileStatement(SQLSentence.getInsertChatSummarySql());
        db.beginTransaction();
        try {
            for (VChatBean vChatBean : vChatBeans) {
                fillVChatBeanData(insertChatSummaryStatement, vChatBean);
                insertChatSummaryStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void queryNormalChatList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor result = new DBHelper.VChatBeanCursor(db.rawQuery(SQLSentence.getQueryChatListSql(), null));
        if (result.getCount() > 0 && result.moveToFirst()) {
            ArrayList<VChatBean> vChatList = new ArrayList<>();
            do {
                VChatBean vChatBean = ((DBHelper.VChatBeanCursor) result).getVChatBean();
                vChatList.add(vChatBean);
            } while (result.moveToNext());
            result.close();
            Log.d(TAG, "queryNormalChatList() called with: ===========querysuccess" + vChatList.size());
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_NORMAL_CHAT_LIST_SUCCESS, vChatList);
        } else {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_RESULT_IS_EMPTY, DataBaseActions.QUERY_NORMAL_CHAT_LIST);
        }
    }

    private void queryNormalChatUnReadMsg(int type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.rawQuery(SQLSentence.getQueryUnReadString(), new String[type]);
//        Cursor cursor = new DBHelper.UnReadMessageBeanCursor(db.query(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, null, SQLSentence.UNREAD_TYPE + " = ? ", new String[]{type + ""}, null, null, null));
        Cursor cursor = new DBHelper.UnReadMessageBeanCursor(db.rawQuery(SQLSentence.getQueryUnReadString(type), null));
        ArrayList<UnReadMessageBean> taskUnReadList = new ArrayList<>();
        if (cursor.getCount() > 0 && cursor.moveToLast()) {
            do {
                UnReadMessageBean messageBean = ((DBHelper.UnReadMessageBeanCursor) cursor).getMessageBean();
                taskUnReadList.add(messageBean);
            } while (cursor.moveToPrevious());
            cursor.close();
        }

        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS,
                type == MessageBean.MESSAGE_CHAT ? 1 : 0, taskUnReadList);
    }

    private void deleteUnReadMsgByGroupId(String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, SQLSentence.UNREAD_MSG_GROUPID + " = ? OR " + SQLSentence.UNREAD_MSG_TASKID + " = ?", new String[]{groupId, groupId});
    }

    private void queryNormalChatUnReadMsg() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        Cursor cursor = new DBHelper.UnReadMessageBeanCursor(db.query(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, null, SQLSentence.UNREAD_TYPE + " = ? ", new String[]{"2"}, null, null, null));
//        Cursor cursor = new DBHelper.UnReadMessageBeanCursor(db.query(SQLSentence.TABLE_CHAT_UNREAD_MESSAGE, null, null, null, null, null, null));
        Cursor cursor = new DBHelper.UnReadMessageBeanCursor(db.rawQuery(SQLSentence.getQueryAllUnReadString(), null));
        if (cursor.getCount() > 0 && cursor.moveToLast()) {
            ArrayList<UnReadMessageBean> taskUnReadList = new ArrayList<>();
            ArrayList<UnReadMessageBean> chatUnReadList = new ArrayList<>();
            do {
                UnReadMessageBean unReadMessageBean = ((DBHelper.UnReadMessageBeanCursor) cursor).getMessageBean();
                if (unReadMessageBean.type == MessageBean.MESSAGE_TASK_CHAT)
                    taskUnReadList.add(unReadMessageBean);
                else if (unReadMessageBean.type == MessageBean.MESSAGE_CHAT)
                    chatUnReadList.add(unReadMessageBean);
                Log.i(TAG, unReadMessageBean.toString());
            } while (cursor.moveToPrevious());
            cursor.close();
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS, 0, taskUnReadList);
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS, 1, chatUnReadList);
        } else {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_RESULT_IS_EMPTY, DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE);
        }
    }

    private void insertOneUnReadMessage(UnReadMessageBean messageBean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (insertUnReadMsgStatement == null)
            insertUnReadMsgStatement = db.compileStatement(SQLSentence.getInsertUnReadMessageString());
        fillUnReadMessageData(insertUnReadMsgStatement, messageBean);
        insertUnReadMsgStatement.executeInsert();
    }

    private void insertUnReadMessageList(List<UnReadMessageBean> messageBeans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        if (insertUnReadMsgStatement == null)
            insertUnReadMsgStatement = db.compileStatement(SQLSentence.getInsertUnReadMessageString());
        try {
            for (UnReadMessageBean messageBean : messageBeans) {
                fillUnReadMessageData(insertUnReadMsgStatement, messageBean);
                Log.i(TAG, "insert msg   " + messageBean.toString());
                insertUnReadMsgStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    public void exitDb() {
        Dispatcher.getInstance().unRegister(this);
    }

    /**
     * 插入特数据库
     *
     * @param ChatMessage chatMessage
     *                    return long
     */
    private void insertChatMessage(MessageBean chatMessage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (insertMessageStatement == null)
            insertMessageStatement = db.compileStatement(SQLSentence.getInsertMessageString());
        fillMessageData(insertMessageStatement, chatMessage);
        long result = insertMessageStatement.executeInsert();
        Log.i(TAG, "insert msg   " + chatMessage.toString());
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONE_CHAT_MESSAGE_SUCCESS, chatMessage.uuid);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONE_CHAT_MESSAGE_FAILED, chatMessage.uuid);
    }

    private void insertChatMessageList(List<MessageBean> messageBeans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        if (insertMessageStatement == null)
            insertMessageStatement = db.compileStatement(SQLSentence.getInsertMessageString());
        try {
            for (MessageBean messageBean : messageBeans) {
                fillMessageData(insertMessageStatement, messageBean);
                Log.i(TAG, "insert msg   " + messageBean.toString());
                //db.insert(SQLSentence.TABLE_CHAT_MESSAGE, null, cv);
                insertMessageStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertUnReadChatMessageList(List<MessageBean> messageBeans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (upsertMessageStatement == null)
            upsertMessageStatement = db.compileStatement(SQLSentence.getUpsertMessageString());
        db.beginTransaction();
        try {
            for (MessageBean messageBean : messageBeans) {
                fillMessageData(upsertMessageStatement, messageBean);
                upsertMessageStatement.bindString(13, messageBean.groupId);
                upsertMessageStatement.bindString(14, messageBean.id);
                Log.i(TAG, "insertUnReadChatMessageList: " + messageBean.toString());
                upsertMessageStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    private void queryGroupChatHistory(String groupId, long time, String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (TextUtils.isEmpty(groupId)) {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_RESULT_IS_EMPTY, DataBaseActions.GET_GROUP_CHAT_HISTORY, time);
            return;
        }
        if (time != 0) {
            cursor = new DBHelper.MessageBeanCursor(db.query(SQLSentence.TABLE_CHAT_MESSAGE, null, SQLSentence.MESSAGE_GROUP_ID + " = ?" + " and " +
                    SQLSentence.MESSAGE_SENDTIME + "<" + " ? ", new String[]{groupId, time + ""}, null, null, SQLSentence.MESSAGE_SENDTIME + " DESC", 15 + ""));
        } else {
            cursor = new DBHelper.MessageBeanCursor(db.query(SQLSentence.TABLE_CHAT_MESSAGE, null, SQLSentence.MESSAGE_GROUP_ID + " = ?", new String[]{groupId},
                    null, null, SQLSentence.MESSAGE_SENDTIME + " DESC", 15 + ""));
        }
        if (cursor.getCount() > 0 && cursor.moveToLast()) {
            ArrayList<MessageBean> messageBeans = new ArrayList<>();
            do {
                MessageBean messageBean = ((DBHelper.MessageBeanCursor) cursor).getMessageBean(taskId);
                messageBeans.add(messageBean);
            } while (cursor.moveToPrevious());
            cursor.close();
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MESSAGE_HISTORY_SUCCESS, messageBeans, time);
        } else {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_RESULT_IS_EMPTY, DataBaseActions.GET_GROUP_CHAT_HISTORY, time, groupId);
        }
    }


    private void updateMessageByUUID(int type, SparseArray<Object> data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // fillMessageData(messageBean);
        if (type == 1) {
            MessageBean messageBean = (MessageBean) data.get(1);
            if (updateMessageStatement == null)
                updateMessageStatement = db.compileStatement(SQLSentence.getUpdateMessageString());
            fillUpdateMessageStatement(updateMessageStatement, messageBean);
            updateMessageStatement.executeUpdateDelete();
        } else if (type == 2) {
            String groupId = (String) data.get(1);
            String msgId = (String) data.get(2);
            updateReCallMsg(db, groupId, msgId);
        } else if (type == 3) {
            MessageBean messageBean = (MessageBean) data.get(1);
            if (upsertMessageStatement == null)
                upsertMessageStatement = db.compileStatement(SQLSentence.getUpsertMessageString());
            fillMessageData(upsertMessageStatement, messageBean);
            upsertMessageStatement.bindString(13, messageBean.groupId);
            upsertMessageStatement.bindString(14, messageBean.id);
            long num = upsertMessageStatement.executeInsert();
            if (num == -1) {
                updateReCallMsg(db, messageBean.groupId, messageBean.id);
            }
        } else if (type == 4) {
            MessageBean messageBean = (MessageBean) data.get(1);
            if (updateImageMessageStatement == null)
                updateImageMessageStatement = db.compileStatement(SQLSentence.getUpdateImageMessageString());
            updateImageMessageStatement.bindString(1, messageBean.content);
            updateImageMessageStatement.bindString(2, messageBean.groupId);
            updateImageMessageStatement.bindString(3, messageBean.uuid);
            updateImageMessageStatement.executeUpdateDelete();
        }
    }

    private void updateReCallMsg(SQLiteDatabase db, String groupId, String msgId) {
        SQLiteStatement statement = db.compileStatement("update " + SQLSentence.TABLE_CHAT_MESSAGE + " set " + SQLSentence.MESSAGE_TYPE + " = ? " + " where " + SQLSentence.MESSAGE_GROUP_ID + "= ? and " + SQLSentence.MESSAGE_MAILID + " = ? ");
        statement.bindLong(1, MessageBean.RECALL_MESSAGE);
        statement.bindString(2, groupId);
        statement.bindString(3, msgId);
        int res = statement.executeUpdateDelete();
        Log.i(TAG, "updateReCallMsg: " + res);
    }

    private void deleteOneMsgByMsgId(String groupId, String msgId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(SQLSentence.TABLE_CHAT_MESSAGE, SQLSentence.MESSAGE_GROUP_ID + " = ? " + "and " + "(" + SQLSentence.MESSAGE_MAILID + " = ? " + " OR " + SQLSentence.MESSAGE_UUID + " = ? " + ")", new String[]{groupId, msgId, msgId});
        Log.i(TAG, "result" + result);
    }

    private void deleteChatHistory(String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(SQLSentence.TABLE_CHAT_MESSAGE, SQLSentence.MESSAGE_GROUP_ID + " = ? ", new String[]{groupId});
        Log.i(TAG, "result" + result);
        result = db.delete(SQLSentence.TABLE_CHAT_WINDWO_SUMMARY, SQLSentence.CHAT_WINDOW_SUMMARY_GROUPID + " = ? ", new String[]{groupId});
        Log.i(TAG, "result TABLE_CHAT_WINDWO_SUMMARY" + result);
        result = db.delete(SQLSentence.TABLE_CHAT_WINDWO_INFO, SQLSentence.CHAT_WINDOW_GROUPID + " = ?", new String[]{groupId});
        Log.i(TAG, "result TABLE_CHAT_WINDWO_INFO" + result);
    }

    private void deleteChatWindowInfo(String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int res = db.delete(SQLSentence.TABLE_CHAT_WINDWO_SUMMARY, SQLSentence.CHAT_WINDOW_SUMMARY_GROUPID + " = ? OR " + SQLSentence.CHAT_WINDOW__SUMMARY_TASKID + " = ?", new String[]{groupId, groupId});
        Log.i(TAG, "result delete chat windows summary" + res);
        res = db.delete(SQLSentence.TABLE_CHAT_WINDWO_INFO, SQLSentence.CHAT_WINDOW_GROUPID + " = ? OR " + SQLSentence.CHAT_WINDOW_TASKID + " = ?", new String[]{groupId, groupId});
        Log.i(TAG, "result delete chat windows info" + res);
    }


    private void fillUpdateMessageStatement(SQLiteStatement updateMessageStatement, MessageBean messageBean) {
        updateMessageStatement.clearBindings();

        updateMessageStatement.bindLong(1, messageBean.isSendSuccess ? 1 : 0);
        updateMessageStatement.bindString(2, messageBean.id);
        updateMessageStatement.bindString(3, messageBean.groupId);
        updateMessageStatement.bindString(4, messageBean.uuid);
    }


    private void fillMessageData(SQLiteStatement statement, MessageBean chatMessage) {
        statement.clearBindings();
        statement.bindString(1, chatMessage.content);
        statement.bindLong(2, chatMessage.createTime);
        statement.bindString(3, chatMessage.sendMessageUser.getUserJson().toString());
        statement.bindLong(4, 0);
        statement.bindString(5, chatMessage.to);
        statement.bindLong(6, chatMessage.sendTime);
        if (!TextUtils.isEmpty(chatMessage.id))
            statement.bindString(7, chatMessage.id);
        else
            statement.bindNull(7);
        long isSend = chatMessage.isSendSuccess ? 1 : 0;
        statement.bindLong(8, isSend);
        statement.bindLong(9, chatMessage.type);
        if (!TextUtils.isEmpty(chatMessage.uuid))
            statement.bindString(10, chatMessage.uuid);
        else
            statement.bindNull(10);
        statement.bindString(11, chatMessage.groupId);
//        chatMessage.to = null;
        if (chatMessage.isSmallMailMsg())
            statement.bindString(12, chatMessage.smallMailId);
        else
            statement.bindNull(12);
    }


    private void fillUnReadMessageData(SQLiteStatement statement, UnReadMessageBean chatMessage) {
        statement.clearBindings();
        statement.bindString(1, chatMessage.groupId);
        statement.bindString(2, chatMessage.taskId);
        statement.bindLong(3, chatMessage.taskCreateTime);
        statement.bindLong(4, chatMessage.msgSendTime);
        statement.bindLong(5, chatMessage.type);
        statement.bindString(6, chatMessage.msgId);
        statement.bindLong(7, chatMessage.isNeedShowNum);
    }


    private void fillVChatBeanData(SQLiteStatement insertChatSummaryStatement, VChatBean vChatBean) {
        insertChatSummaryStatement.clearBindings();
        insertChatSummaryStatement.bindString(1, vChatBean.groupId);
        insertChatSummaryStatement.bindNull(2);
        insertChatSummaryStatement.bindString(3, vChatBean.topic);
        insertChatSummaryStatement.bindString(4, vChatBean.members);
        insertChatSummaryStatement.bindLong(5, vChatBean.isGroup);
        if (TextUtils.isEmpty(vChatBean.lastMsg)) {
            insertChatSummaryStatement.bindNull(6);
        } else {
            insertChatSummaryStatement.bindString(6, vChatBean.lastMsg);
        }
        if (TextUtils.isEmpty(vChatBean.msgId)) {
            insertChatSummaryStatement.bindNull(7);
        } else {
            insertChatSummaryStatement.bindString(7, vChatBean.msgId);
        }
        insertChatSummaryStatement.bindLong(8, vChatBean.lastChatTime);
        vChatBean.members = null;
    }

    private void fillChatWindowInfo(ChatWindowInfoBean chatWindowInfoBean, SQLiteStatement insertChatWindowInfo) {
        insertChatWindowInfo.bindString(1, chatWindowInfoBean.groupId);
        insertChatWindowInfo.bindString(2, chatWindowInfoBean.mailId);
        if (TextUtils.isEmpty(chatWindowInfoBean.taskId))
            insertChatWindowInfo.bindNull(3);
        else
            insertChatWindowInfo.bindString(3, chatWindowInfoBean.taskId);
        insertChatWindowInfo.bindString(4, chatWindowInfoBean.createUser);
        insertChatWindowInfo.bindString(5, chatWindowInfoBean.taskTitle);
        insertChatWindowInfo.bindLong(6, chatWindowInfoBean.isSingle ? 1 : 0);
        insertChatWindowInfo.bindString(7, chatWindowInfoBean.taskPlace);
        if (chatWindowInfoBean.memberNames == null)
            insertChatWindowInfo.bindNull(8);
        else
            insertChatWindowInfo.bindString(8, chatWindowInfoBean.memberNames);

        Log.i(TAG, "fillChatWindowInfo: " + chatWindowInfoBean.lastToString);
        insertChatWindowInfo.bindString(9, chatWindowInfoBean.memberString);
        insertChatWindowInfo.bindString(10, chatWindowInfoBean.lastToString);
        insertChatWindowInfo.bindString(11, chatWindowInfoBean.exitString);
    }

    /**
     * 插入一条邮件信息记录
     *
     * @param MailDetail           mailDetail
     * @param List<MailAttachment> attachments
     *                             return long
     */
    private void insertOneMailMessage(MailDetail mailDetail, List<MailAttachment> attachments) {
        //插入一封邮件
        String messageId = mailDetail.getMessageId();
        if (messageId != null) {//解决 邮件重复问题
            List<MailDetail> mdList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.MessageId.eq(messageId)).list();
            if (mdList != null && mdList.size() > 0) {
                Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONE_MAIL_MESSAGE_SUCCESS, mdList.get(0).getId(), null, null);
                return;
            }
        }
        mailDetail.setHasAttachment(attachments != null && attachments.size() > 0);
        long result = mailDetailDao.insert(mailDetail);
//        Log.i(TAG, "insert mail   " + mailDetail.toString());
//        Log.i(TAG, "insert result   " + result);
        List<Long> attIds = null;
        if (result > 0) {
            //插入附件
            if (attachments != null) {
//                attIds = new ArrayList<>();
                int count = attachments.size();
                MailAttachment mailAttachment;
                for (int i = 0; i < count; i++) {
                    mailAttachment = attachments.get(i);
                    if (mailAttachment.getEmail() == null) {
                        mailAttachment.setEmail(mailDetail.getEmail());
                    }
                    if (mailAttachment.getMailId() == 0) {
                        mailAttachment.setMailId(mailDetail.getId());
                    }
                    result = mailAttachmentDao.insertOrReplace(mailAttachment);
                    Log.i(TAG, "insert attachment   " + mailAttachment.toString());
                    Log.i(TAG, "insert result   " + result);
                    if (result > 0) {
//                        attIds.add(mailAttachment.getId());
                        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_SUCCESS, mailAttachment.getId());
                    } else {
                        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_FAILED, mailAttachment.getId());
                    }
                    Log.i(TAG, "Inserted new attachment, ID: " + mailAttachment.getId());
                }
            }
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONE_MAIL_MESSAGE_SUCCESS, mailDetail.getId(), mailDetail.getMessageId(), attIds);
        } else {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.INSERT_ONE_MAIL_MESSAGE_FAILED, mailDetail.getId(), mailDetail.getMessageId(), null);
        }
        Log.i(TAG, "Inserted new mail, ID: " + mailDetail.getId());
    }

    /**
     * 获取某一邮箱账号下的所有邮件的UID列表
     *
     * @param String email
     *               return List<String> uidList
     */
    private void getAllMailUidList(String email) {
        //获取某一邮箱账号下的所有邮件的UID列表
        CloseableListIterator<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email), MailDetailDao.Properties.Uid.isNotNull()).orderDesc(MailDetailDao.Properties.UpdateTime).orderDesc(MailDetailDao.Properties.SentDate).listIterator();
        List<String> uidList = new ArrayList<>();
        while (mailList.hasNext()) {
            String uid = mailList.next().getUid();
            uidList.add(uid);
        }
        Log.i(TAG, "get mail uid list, Count: " + uidList.size());
        if (uidList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_ALL_MAIL_UID_LIST_SUCCESS, uidList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_ALL_MAIL_UID_LIST_FAILED, uidList);
    }

    /**
     * 获取某一目录下的邮件列表
     *
     * @param String email
     * @param long   dirId
     *               return List<MailDetail>
     */
    private void getMailListByDirID(String email, long dirId) {
        //获取邮件列表
        List<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email), MailDetailDao.Properties.DirectoryId.eq(dirId), MailDetailDao.Properties.IsDeleted.eq(false)).orderDesc(MailDetailDao.Properties.UpdateTime).list();
        Log.i(TAG, "getMailListByDirID, Count: " + mailList.size());
        if (mailList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_SUCCESS, mailList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_FAILED, mailList);
    }

    /**
     * 获取某一目录下的邮件列表
     *
     * @param String email
     * @param long   dirId
     *               return List<MailDetail>
     */
    private void getOutBoxMailList(String email) {
        //获取邮件列表
        List<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email), MailDetailDao.Properties.DirectoryId.eq(-995), MailDetailDao.Properties.IsDeleted.eq(false)).orderDesc(MailDetailDao.Properties.UpdateTime).list();
        Log.i(TAG, "getOutBoxMailList, Count: " + mailList.size());
        if (mailList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_OUT_BOX_MAIL_LIST_SUCCESS, mailList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_OUT_BOX_MAIL_LIST_FAILED, mailList);
    }

    /**
     * 获取某一账号的未读邮件列表
     *
     * @param String email
     *               return List<MailDetail>
     */
    private void getUnreadMailList(String email) {
        //获取邮件列表
        List<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email), MailDetailDao.Properties.IsRead.eq(false), MailDetailDao.Properties.IsDeleted.eq(false), MailDetailDao.Properties.DirectoryId.notEq(AppConfig.WY_CFG.DIR_ID_DELETED_MAIL)).orderDesc(MailDetailDao.Properties.UpdateTime).list();
        Log.i(TAG, "get unread mail list, Count: " + mailList.size());
        if (mailList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_UNREAD_MAIL_LIST_SUCCESS, mailList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_UNREAD_MAIL_LIST_FAILED, mailList);
    }

    /**
     * 获取某一账号的已标记邮件列表
     *
     * @param String email
     *               return List<MailDetail>
     */
    private void getMarkedMailList(String email) {
        //获取邮件列表
        List<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email),
                MailDetailDao.Properties.IsMarked.eq(true), MailDetailDao.Properties.IsDeleted.eq(false),
                MailDetailDao.Properties.DirectoryId.notEq(AppConfig.WY_CFG.DIR_ID_DELETED_MAIL)).
                orderDesc(MailDetailDao.Properties.UpdateTime).list();
        Log.i(TAG, "get marked mail list, Count: " + mailList.size());
        if (mailList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MARKED_MAIL_LIST_SUCCESS, mailList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MARKED_MAIL_LIST_FAILED, mailList);
    }

    /**
     * 获取某一目录下的邮件列表（分页）
     *
     * @param String email
     * @param long   dirId
     * @param int    page
     * @param int    size
     *               return List<MailDetail>
     */
    private void getMailListByDirIDWithPage(String email, long dirId, int page, int size) {
        //获取邮件列表（分页）
        List<MailDetail> mailList = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.Email.eq(email), MailDetailDao.Properties.DirectoryId.eq(dirId)).orderDesc(MailDetailDao.Properties.SentDate).limit(size).offset((page - 1) * size).list();
        Log.i(TAG, "getMailListByDirIDWithPage, Count: " + mailList.size());
        if (mailList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE_SUCCESS, mailList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_WITH_PAGE_FAILED, mailList);
    }

    /**
     * 获取某一邮箱账号下的所有附件列表
     *
     * @param String email
     *               return List<MailAttachment>
     */
    private void getMailAttachmentList(String email) {
        //获取邮件列表
        List<MailAttachment> mailAttachmentList = mailAttachmentDao.queryBuilder().where(MailAttachmentDao.Properties.Email.eq(email)).orderDesc(MailAttachmentDao.Properties.CreateTime).list();
        Log.i(TAG, "get mail attachment list, Count: " + mailAttachmentList.size());
        if (mailAttachmentList.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST_SUCCESS, mailAttachmentList);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST_FAILED, mailAttachmentList);
    }

    /**
     * 获取单封邮件详情（根据主键ID）
     * return MailDetail
     */
    private void getMailDetailById(long id) {
        //获取单封邮件详情
        MailDetail mailDetail = mailDetailDao.load(id);
        Log.i(TAG, "Get one mail, ID: " + mailDetail.getId());
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_DETAIL_BY_ID_SUCCESS, mailDetail);
    }

    /**
     * 获取单封邮件详情（根据MessageID）
     * return MailDetail
     */
    private void getMailDetailByMessageId(String messageId) {
        //获取单封邮件详情
        MailDetail mailDetail = mailDetailDao.queryBuilder().where(MailDetailDao.Properties.MessageId.eq(messageId)).list().get(0);
        Log.i(TAG, "Get one mail, ID: " + mailDetail.getId());
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_DETAIL_BY_MESSAGE_ID_SUCCESS, mailDetail);
    }

    /**
     * 删除一封邮件
     *
     * @param String email
     * @param long   mailId
     */
    private void deleteMail(String email, long mailId) {
        //删除邮箱目录
        mailDetailDao.deleteByKey(mailId);
        Log.i(TAG, "Delete mail ID：   " + mailId);
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.DELETE_ONE_MAIL_SUCCESS, email, mailId);
    }

    /**
     * 获取用户的邮箱账号列表
     * return List<MailAccount>
     */
    private void getMailAccounts(String userId) {
        //获取用户的邮箱账号列表
        List<MailAccount> mailAccounts = mailAccountDao._queryMailAccountsByUserId(userId);
        Log.i(TAG, "get mail account list, Count: " + mailAccounts.size());
        if (mailAccounts.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_ACCOUNT_LIST_SUCCESS, mailAccounts);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_ACCOUNT_LIST_FAILED, mailAccounts);
    }

    /**
     * 保存用户的邮箱账号信息（插入或更新）
     *
     * @param MailAccount mailAccount
     *                    return long
     */
    private void saveMailAccount(MailAccount mailAccount) {
        //保存用户的邮箱账号信息（插入或更新）
        long result = mailAccountDao.insertOrReplace(mailAccount);
        Log.i(TAG, "Save mail account   " + mailAccount.toString());
        Log.i(TAG, "Save result   " + result);
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ACCOUNT_SUCCESS, mailAccount.getId());
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ACCOUNT_FAILED, mailAccount.getId());
        Log.i(TAG, "Saved mail account, ID: " + mailAccount.getId());
    }

    /**
     * 插入一条邮件附件信息
     *
     * @param MailDetail mailAttachment
     *                   return long
     */
    private void saveOneMailAttachment(MailAttachment mailAttachment) {
        //插入一封邮件
        long result = mailAttachmentDao.insertOrReplace(mailAttachment);
        Log.i(TAG, "Save attachment   " + mailAttachment.toString());
        Log.i(TAG, "Save result   " + result);
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_SUCCESS, mailAttachment.getId());
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_FAILED, mailAttachment.getId());
        Log.i(TAG, "Saved new attachment, ID: " + mailAttachment.getId());
    }

    /**
     * 更新邮件详情表
     *
     * @param MailDetail mailDetail
     */
    private void updateMailDetail(MailDetail mailDetail, ArrayList<MailAttachment> attachments) {
        //更新邮件详情表
        try {
            //插入附件
            if (attachments != null) {
                mailAttachmentDao._delete_mail_Attachments(mailDetail.getId());
                int count = attachments.size();
                MailAttachment mailAttachment;
                for (int i = 0; i < count; i++) {
                    mailAttachment = attachments.get(i);
                    if (mailAttachment.getEmail() == null) {
                        mailAttachment.setEmail(mailDetail.getEmail());
                    }
                    if (mailAttachment.getMailId() == 0) {
                        mailAttachment.setMailId(mailDetail.getId());
                    }
                    long rowId = mailAttachmentDao.insertOrReplace(mailAttachment);
                    Log.i(TAG, "updateMailDetail: save mailAttachment rowId = " + rowId);
                }
                mailDetail.resetAttachments();
                mailAttachmentDao.clearAttachmentsQuery();//重置Query 使下次查询邮件附件时候，能后查询出最新的数据
            }
            mailDetailDao.update(mailDetail);
            Log.i(TAG, "Update one mail, ID: " + mailDetail.getId());
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.UPDATE_MAIL_DETAIL_SUCCESS, mailDetail);
        } catch (DaoException e) {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.UPDATE_MAIL_DETAIL_FAILED, mailDetail);
        }
    }

    /**
     * 更新邮件详情表
     *
     * @param MailDetail mailDetail
     */
    private void changeMailDirectory(String email, long sentDate, long dirId) {
        //更新邮件详情表
        try {
            MailDetail _md = mailDetailDao._queryMailBySentDate(email, sentDate);
            _md.setDirectoryId(dirId);
            if (dirId == AppConfig.WY_CFG.DIR_ID_SENT_MAIL) {
                _md.setSendStatus(MailDetail.MAIL_TYPE_SENT);
            }
            mailDetailDao.update(_md);
            Log.i(TAG, "Change Mail Directory, Subject: " + _md.getSubject());
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.CHANGE_MAIL_SEND_STATUS_SUCCESS, _md);
        } catch (DaoException e) {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.CHANGE_MAIL_SEND_STATUS_FAILED);
        }
    }

    /**
     * 更新邮件发送状态
     *
     * @param MailDetail mailDetail
     */
    private void updateMailSendStatus(String email, long sentDate, long sendStatus) {
        //更新邮件详情表
        try {
            MailDetail _md = mailDetailDao._queryMailBySentDate(email, sentDate);
            _md.setSendStatus(sendStatus);
            mailDetailDao.update(_md);
            Log.i(TAG, "Change Mail SendStatus, Subject: " + _md.getSubject());
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.CHANGE_MAIL_DIRECTORY_SUCCESS, _md);
        } catch (DaoException e) {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.CHANGE_MAIL_DIRECTORY_FAILED);
        }
    }

    /**
     * 更新邮件详情表
     *
     * @param MailDetail mailDetail
     */
    private void updateMailSentPercentage(String email, long sentDate, long percentage) {
        //更新邮件详情表
        try {
            MailDetail _md = mailDetailDao._queryMailBySentDate(email, sentDate);
            _md.setSentPercentage(percentage);
            mailDetailDao.update(_md);
            Log.i(TAG, "Update Mail Sent Percentage, Subject: " + _md.getSubject());
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE_SUCCESS, _md);
        } catch (DaoException e) {
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE_FAILED);
        }
    }

    /**
     * 保存一条邮箱目录（插入或更新）
     *
     * @param MailDirectory mailDirectory
     *                      return long
     */
    private void saveMailDirectory(MailDirectory mailDirectory) {
        //保存一条邮箱目录（插入或更新）
        long result = mailDirectoryDao.insertOrReplace(mailDirectory);
        Log.i(TAG, "Save mail directory   " + mailDirectory.toString());
        Log.i(TAG, "Save result   " + result);
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_DIRECTORY_SUCCESS, mailDirectory.getId());
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_DIRECTORY_FAILED, mailDirectory.getId());
        Log.i(TAG, "Saved mail directory, ID: " + mailDirectory.getId());
    }

    /**
     * 获取邮箱的目录列表
     *
     * @param String email
     *               return List<MailDirectory>
     */
    private void getMailDirectoryListByEmail(String email) {
        //获取邮箱的目录列表
        List<MailDirectory> mailDirectories = mailDirectoryDao.queryBuilder().where(MailDirectoryDao.Properties.Email.eq(email)).list();
        Log.i(TAG, "get mail directories list, Count: " + mailDirectories.size());
        if (mailDirectories.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_DIRECTORY_LIST_SUCCESS, mailDirectories);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_DIRECTORY_LIST_FAILED, mailDirectories);
    }

    /**
     * 删除邮箱目录
     *
     * @param String email
     * @param long   dirId
     */
    private void deleteMailDirectory(String email, long dirId) {
        //删除邮箱目录
        mailDirectoryDao.deleteByKey(dirId);
        Log.i(TAG, "Delete mail directory ID：   " + dirId);
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.DELETE_ONE_MAIL_DIRECTORY_SUCCESS, email, dirId);
    }

    /**
     * 删除用户的一个邮箱账号
     *
     * @param Long id
     */
    private void deleteMailAccount(long id) {
        //删除用户的一个邮箱账号
        mailAccountDao.deleteByKey(id);
        Log.i(TAG, "Delete mail account ID：   " + id);
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.DELETE_ONE_MAIL_ACCOUNT_SUCCESS, id);
    }

    /**
     * 获取用户的邮件联系人列表
     * return List<MailAccount>
     */
    private void getMailContacts(String keyword) {
        //获取用户的邮箱账号列表
        List<MailContacts> mailContacts = mailContactsDao.queryBuilder().whereOr(MailContactsDao.Properties.Address.like("%" + keyword + "%"), MailContactsDao.Properties.Personal.like("%" + keyword + "%")).list();
        Log.i(TAG, "get mail contacts list, Count: " + mailContacts.size());
        if (mailContacts.size() > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_CONTACTS_LIST_SUCCESS, mailContacts);
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.QUERY_MAIL_CONTACTS_LIST_FAILED, mailContacts);
    }

    /**
     * 保存用户的邮件联系人信息（插入或更新）
     *
     * @param MailAccount mailAccount
     *                    return long
     */
    private void saveMailContacts(MailContacts mailContacts) {
        //保存用户的邮件联系人信息（插入或更新）
        List<MailContacts> curContacts = mailContactsDao.queryBuilder().where(MailContactsDao.Properties.Address.eq(mailContacts.getAddress())).list();
        if (curContacts.size() > 0) {
            // 如果已经存在此联系人，则更新；否则插入
            mailContacts.setId(curContacts.get(0).getId());
            return;
        }
        long result = mailContactsDao.insertOrReplace(mailContacts);
//        Log.i(TAG, "Save mail contacts   " + mailContacts.toString());
//        Log.i(TAG, "Save result   " + result);
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_CONTACTS_SUCCESS, mailContacts.getId());
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_CONTACTS_FAILED, mailContacts.getId());
//        Log.i(TAG, "Saved mail contacts, ID: " + mailContacts.getId());
    }

    /**
     * 保存邮件任务（插入或更新）
     *
     * @param MailTask mailTask
     */
    private void saveMailTask(MailTask mailTask) {
        //保存用户的邮件联系人信息（插入或更新）
        long result = mailTaskDao.insertOrReplace(mailTask);
        if (result > 0)
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_TASK_SUCCESS, mailTask.getId());
        else
            Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.SAVE_ONE_MAIL_TASK_FAILED, mailTask.getId());
    }

    /**
     * 获取所有邮件任务
     */
    private void getAllMailTask() {
        List<MailTask> mailTaskList = mailTaskDao.loadAll();
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.GET_ALL_MAIL_TASK_RESULT, mailTaskList);
    }

    /**
     * 删除邮件任务
     */
    private void deleteMailTaskById(long taskId) {
        //保存用户的邮件联系人信息（插入或更新）
        mailTaskDao.deleteByKey(taskId);
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.DELETE_MAIL_TASK_BY_ID_RESULT);
    }

    /**
     * 删除邮件任务
     */
    private void updateMailTaskRcpts(MailTask mailTask) {
        //保存用户的邮件联系人信息（插入或更新）
        mailTaskDao.update(mailTask);
        Dispatcher.getInstance().dispatchStoreActionEvent(DataBaseActions.UPDATE_MAIL_TASK_RCPTS_RESULT);
    }


    public void clean() {
        if (dbHelper != null)
            dbHelper.close();
        dbHelper = null;
        if (mailDBHelper != null)
            mailDBHelper.close();
        mailDBHelper = null;
    }
}
