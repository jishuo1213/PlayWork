package com.inspur.playwork.weiyou.utils;

import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailContacts;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.utils.db.bean.MailTask;

import java.util.ArrayList;

/**
 *
 * Created by 孙 on 2016/2/18 0018.
 */
public class DBUtil {

    public static void queryMailAccountList(String userId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_ACCOUNT_LIST,userId);
    }

    public static void saveMailAccount(MailAccount mc){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.SAVE_ONE_MAIL_ACCOUNT, mc);
    }

    public static void deleteMailAccount(long maId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MAIL_ACCOUNT, maId);
    }

    public static void queryMailDirectory(String email){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_DIRECTORY_LIST, email);
    }

    public static void saveMailDetail(MailDetail mailDetail,ArrayList<MailAttachment> attachments){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_MAIL_MESSAGE, mailDetail, attachments);
    }

    public static void updateMailDetail(MailDetail md,ArrayList<MailAttachment> attachments){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MAIL_DETAIL, md, attachments);
    }

    public static void changeMailDirectory(String email, long sentDate,long dirId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.CHANGE_MAIL_DIRECTORY, email, sentDate, dirId);
    }

    public static void updateMailSendStatus(String email, long sentDate,long sendStatus){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.CHANGE_MAIL_SEND_STATUS, email, sentDate, sendStatus);
    }

    public static void updateMailSentPercentage(String email, long sentDate,long percentage){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE, email, sentDate, percentage);
    }

    public static void deleteOneMail(String email,long mailId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MAIL, email, mailId);
    }

    public static void queryMailDetail(long mailId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_DETAIL_BY_ID, mailId);
    }

    public static void queryMailUidList(String email){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_ALL_MAIL_UID_LIST, email);
    }

    public static void queryUnreadMailList(String email){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_UNREAD_MAIL_LIST, email);
    }

    public static void queryMarkedMailList(String email){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MARKED_MAIL_LIST, email);
    }

    public static void queryMailListByDirId(String email,long dirId){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID, email, dirId);
    }

    public static void queryOutBoxMailList(String email){
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_OUT_BOX_MAIL_LIST, email);
    }

    public static void syncContactToDB(MailContacts mc) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.SAVE_ONE_MAIL_CONTACTS, mc);
    }

    public static void sarchContactsByStr(String searchKey) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_CONTACTS_LIST,searchKey);
    }

    public static void saveMailAttachment(MailAttachment ma) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT, ma);
    }

    public static void queryMailAttachmentList(String email) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST, email);
    }

    public static void searchPerson(String inputText) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEARCH_PERSON, inputText);
        GroupStores.getInstance().searchPerson(inputText);
    }

    public static void saveMailTask(MailTask mailTask) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.SAVE_ONE_MAIL_TASK,mailTask);
    }

    public static void getAllMailTask() {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.GET_ALL_MAIL_TASK);
    }

    public static void deleteMailTaskById(long taskId) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_MAIL_TASK_BY_ID,taskId);
    }

    public static void updateMailTask(MailTask mailTask) {
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MAIL_TASK_RCPTS,mailTask);
    }
}
