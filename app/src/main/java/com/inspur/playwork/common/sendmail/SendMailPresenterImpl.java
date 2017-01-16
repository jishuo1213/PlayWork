package com.inspur.playwork.common.sendmail;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.sendmail.CustomInfo;
import com.inspur.playwork.model.sendmail.SendMailRequest;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.utils.json.GsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Bugcode on 2016/3/16.
 */
public class SendMailPresenterImpl implements SendMailPresenter {

    private static final String TAG = "sendmailPresenter";

    private static final int SEND_MAIL = 0x01;

    private SendMailView sendMailView;
    private SmallMailBean smallMail;
    private ChatWindowInfoBean chatInfo;
    private TaskBean taskInfo;
    private ArrayList<UserInfoBean> tempChatList, tempHideList, tempExitList, tempAllList, tempAddList;
    private boolean isSendComplete, isSaveComplete;

    public SendMailPresenterImpl(SendMailView sendMailView, ChatWindowInfoBean chatInfo, TaskBean taskInfo) {
        this.sendMailView = sendMailView;
        this.chatInfo = chatInfo;
        this.taskInfo = taskInfo;
        isSendComplete = false;
        isSaveComplete = false;

        tempChatList = new ArrayList<>();
        tempHideList = new ArrayList<>();
        tempExitList = new ArrayList<>();
        tempAllList = new ArrayList<>();
        tempAddList = new ArrayList<>();

        tempChatList.addAll(chatInfo.chatMemberList);
        tempHideList.addAll(chatInfo.hideMemberList);
        tempExitList.addAll(chatInfo.exitMemberList);
        tempAllList.addAll(chatInfo.allMemberList);
    }

    @Override
    public void register() {
//        GroupStores.getInstance().register();
        Dispatcher.getInstance().register(this);
    }

    @Override
    public void unregister() {
//        GroupStores.getInstance().unRegister();
        Dispatcher.getInstance().unRegister(this);
    }

    @Override
    public void sendMail(SmallMailBean smallMail, String bodyContent, String historyContent) {
//        sendMailView.showDialog();
        this.smallMail = smallMail;
        this.saveContactsRequest(smallMail.toUserList);
        this.sendMailRequest(smallMail, bodyContent, historyContent);
    }

    @Override
    public String getHistoryContent(SmallMailBean smallMail) {
        return "<div class=\"original_mail\" style=\"font-size: 12px;\">" +
                "<hr style=\"border-top: #c2cee5 1px solid;\"/>" +
                "<div class=\"old_mail_header\">" +
                "<p>发件人: " + smallMail.sendUser.name + "<span class=\"old_mail_header_time\">" + DateUtils.getCalendarAllText(smallMail.sendTime) + "</span></p>" +
                "<p>收件人: " + smallMail.toUserNames + "</p>" +
                "</div>" + smallMail.content + "</div>";
    }

    private void saveContactsRequest(ArrayList<UserInfoBean> recipientList) {

        for (UserInfoBean userInfo : recipientList) {
            UserInfoBean userInfoInAll = this.isIdExisted(userInfo.id, tempAllList);
            if (userInfoInAll == null) {
                tempAddList.add(userInfo);
                tempChatList.add(userInfo);
                tempAllList.add(userInfo);
                continue;
            }

            UserInfoBean userInfoInHide = this.isIdExisted(userInfo.id, tempHideList);
            if (userInfoInHide != null) {
                tempChatList.add(userInfoInHide);
                tempHideList.remove(userInfoInHide);
                continue;
            }

            UserInfoBean userInfoInExit = this.isIdExisted(userInfo.id, tempExitList);
            if (tempExitList.contains(userInfoInExit)) {
                tempChatList.add(userInfoInExit);
                tempExitList.remove(userInfoInExit);
            }
        }

        if (tempAddList.size() > 0) {
//            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.ADD_MEMBER, taskInfo, chatInfo, tempAddList);
            GroupStores.getInstance().addMember(taskInfo, chatInfo, tempAddList);
        }
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SAVE_CONTACT_GROUP, taskInfo, chatInfo, tempChatList);
        GroupStores.getInstance().saveContactGroup(taskInfo, chatInfo, tempChatList, true);
    }

    private void saveContactsResponse(SparseArray<Object> data) {
        boolean result = (boolean) data.get(0);
        if (result) {
            chatInfo.chatMemberList = tempChatList;
            chatInfo.hideMemberList = tempHideList;
            chatInfo.exitMemberList = tempExitList;
            chatInfo.allMemberList = tempAllList;

            chatInfo.calculateChangeMember();
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_CHAT_WINDOW_BY_GROUP_ID, 1, chatInfo);
        }
        isSaveComplete = true;
        if (isSendComplete) {
            sendMailView.dismissDialog();
            isSaveComplete = false;
            sendMailView.finished();
        }
    }

    private void sendMailRequest(SmallMailBean smallMail, String bodyContent, String historyContent) {
        if ("".equals(historyContent)) {
            smallMail.content = "<p>" + bodyContent + "</p>";
        } else {
            smallMail.content = "<p>" + bodyContent + "<br/><br/><br/><br/></p>" + historyContent;
        }

        ArrayList<UserInfoBean> toList = smallMail.toUserList;
        smallMail.initSendToString();
        UserInfoBean[] to = new UserInfoBean[toList.size()];
        for (int i = 0; i < toList.size(); i++) {
            UserInfoBean toUser = toList.get(i);
            to[i] = new UserInfoBean();
            to[i].avatar = toUser.avatar;
            to[i].id = toUser.id;
            to[i].name = toUser.name;
            to[i].email = toUser.email;
        }

        SendMailRequest sendMailRequest = new SendMailRequest();
        sendMailRequest.subject = smallMail.subject;
        sendMailRequest.from = smallMail.sendUser;
        sendMailRequest.to = to;
        sendMailRequest.isDel = 0;
        sendMailRequest.attachment = new String[0];
        sendMailRequest.custom = new CustomInfo();
        sendMailRequest.content = EncryptUtil.encrypt2aes(smallMail.content);
        if (bodyContent.length() > 10) {
            sendMailRequest.summary = EncryptUtil.encrypt2aes(bodyContent.substring(0, 10));
        } else {
            sendMailRequest.summary = EncryptUtil.encrypt2aes(bodyContent);
        }
        sendMailRequest.type = 2;
        sendMailRequest.taskId = smallMail.taskId;
        sendMailRequest.chatId = smallMail.chatId;
        sendMailRequest.createTime = System.currentTimeMillis();
        sendMailRequest.isEDCrypts = true;

        JSONObject requestJson;
        try {
            requestJson = new JSONObject(GsonUtils.bean2Json(sendMailRequest));
            if (smallMail.attchments != null) {
                JSONArray jsonArray = new JSONArray();
                int index = 0;
                for (TaskAttachmentBean attachmentBean : smallMail.attchments) {
                    jsonArray.put(index, attachmentBean.toJson());
                    ++index;
                }
                requestJson.put("Attachment", jsonArray);
                smallMail.attchmentString = jsonArray.toString();
            } else {
                smallMail.attchmentString = "[]";
            }
            createHttpRequestJson(requestJson, SEND_MAIL, "SendMail");

            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "addNewTask", requestJson, httpCallback, "");

//            Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SEND_MAIL, JsonUtils.createRequestJson(requestJson, "SendMail"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMailResponse(JSONObject responseData) {
        String clientId = responseData.optString("ClientId");
        if ("SendMail".equals(clientId)) {
            if (responseData.optBoolean("type")) {
                smallMail.mailId = responseData.optString("mailId");
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SEND_MAIL_SUCCESS);
            } else {
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SEND_MAIL_FAIL);
            }
        }
    }

    private UserInfoBean isIdExisted(String id, ArrayList<UserInfoBean> userList) {
        for (UserInfoBean userInfo : userList) {
            if (id.equals(userInfo.id))
                return userInfo;
        }
        return null;
    }

//    private void serverResponse(String actionType, JSONObject actionData) {
//        switch (actionType) {
//            case SEND_MAIL:
//                this.sendMailResponse(actionData);
//                break;
//            default:
//                break;
//        }
//    }

    private void createHttpRequestJson(JSONObject body, int type, String clientId) throws JSONException {
        JSONObject clientJson = new JSONObject();
        clientJson.put("type", type);
        if (!TextUtils.isEmpty(clientId)) {
            clientJson.put("ClientId", clientId);
        }
        body.put("isPhone", true);
        body.put("ClientId", clientJson.toString());
    }


    public void onEventMainThread(UpdateUIAction updateUIAction) {
        Log.i(TAG, "onEventMainThread: ");
        switch (updateUIAction.getActionType()) {
            case MessageActions.SEND_MAIL_SUCCESS:
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_SMALL_MAIL, smallMail);
                isSendComplete = true;
                if (isSaveComplete) {
                    sendMailView.dismissDialog();
                    isSendComplete = false;
                    sendMailView.finished();
                }
                break;
            case MessageActions.SEND_MAIL_FAIL:
                break;
            case MessageActions.SAVE_CONTACT_GROUP:
                this.saveContactsResponse(updateUIAction.getActionData());
                break;
            default:
                break;
        }
    }

    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {

                try {
                    JSONObject res = new JSONObject(response.body().string());
                    JSONObject cleentId = new JSONObject(res.optString("ClientId"));
                    int type = cleentId.optInt("type");
                    String clientId = cleentId.optString("ClientId", "");
                    Log.i(TAG, "onResponse: type:" + type + res.toString());
                    if (!TextUtils.isEmpty(clientId)) {
                        res.put("ClientId", clientId);
                    } else {
                        res.remove("ClientId");
                    }
                    switch (type) {
                        case SEND_MAIL:
                            sendMailResponse(res);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    };
}
