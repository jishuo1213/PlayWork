package com.inspur.playwork.stores.message;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.CustomProperty;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.utils.XmlHelper;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.utils.loadfile.LoadFileManager;
import com.inspur.playwork.view.VChatViewOperation;
import com.inspur.playwork.view.application.addressbook.AddressBookFragment;
import com.inspur.playwork.view.application.addressbook.AddressBookViewOperation;
import com.inspur.playwork.view.message.chat.ChatViewOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 消息store
 *
 * @author 笑面V客(bugcodeO@foxmail.com)
 */
public class MessageStores extends Stores {

    private static final String TAG = "MessageStoresFan";

    private byte GET_LOCAL_SUCCESS_MASK = 0x01;
    private byte GET_NET_SUCCESS_MASK = 0x02;
    private byte RE_GET_LOCAL_UNREAD_MASK = 0x04;

    private static final int CHANGE_TASK_TITLE = 0x01;

    private static final int GET_GROUP_CHAT_HISTORY = 0x02;

    private static final String SEND_GROUP_MSG = "sendGroupMsg";

    private static final String SEND_CHAT_MSG = "sendChatMsg";

    private static final int SET_MSG_READ = 0x05;

//    private static final int CLOSE_CHAT_WINDOW = 0x06;

    private static final int GET_FEI_HUA_TI_THIRTY = 0x07;

    private static final int GET_CHAT_WINDOW_INFO = 0x08;

//    private static final int GET_UN_READ_MESSAGE = 0x09;

    private static final int SET_CUSTOM_PROPERTY = 0x0A; // 设置任务地点
    private static final int GET_NOTES_BY_GROUP_ID = 0x0B; // 获取随手记内容
    private static final int SET_NOTES_BY_GROUP_ID = 0x0C; // 设置随手记内容
    private static final int UPDATE_GROUP_NAME = 0x0D; // 修改微聊话题

    private static final int DELETE_GROUP_CHAT = 0x0E;
    private static final int DELETE_ONE_CHAT_MSG = 0x0F;

    private static final int RECALL_ONE_CHAT_MSG = 0x10;

    private static final int GET_SMALL_BY_ID = 0x11;

    public static String USER_ID;

    private ArrayMap<String, Long> avatars;

    private ArrayList<VChatBean> vChatList;

    private ArrayList<String> chatGroupIds;

    private ArrayList<MessageBean> needToSaveMsg;

    private ArrayMap<String, CustomProperty> mPropertyArrayMap;

    private ArrayMap<String, MessageBean> sendMessageMap;

//    private String neededuuId;

    private ArrayList<String> gettingWindowInfoList;

    private Set<String> needGetWindowInfoList;

    private ArrayList<UnReadMessageBean> localNotHasWindowUnreadBeans;

    private ChatWindowInfoBean currentWindow;
    private TaskBean currentWindowTaskBean;
    private VChatBean currentVchatBean;

    private LoadFileManager loadFileManager;

    private byte unReadState = 0;

    private boolean isGetVchatListSuccess = false;

    private WeakReference<VChatViewOperation> vchatViewReference = new WeakReference<>(null);
    private WeakReference<AddressBookViewOperation> addBookViewOperation = new WeakReference<>(null);

    private WeakReference<ChatViewOperation> chatOperationReference = new WeakReference<>(null);

    public MessageStores() {
        super(Dispatcher.getInstance());
    }

    public static MessageStores getInstance() {
        if (USER_ID == null)
            USER_ID = PreferencesHelper.getInstance().getCurrentUser().id;
        return SingleRefreshManager.getInstance().getMessageStores();
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void onEvent(StoreAction storeAction) {
        switch (storeAction.getActionType()) {
            case CommonActions.REVICE_TIMELINE_DATA_FROM_SERVER:
                String type = (String) storeAction.getActiontData().get(0);
                parseServerReturnData(storeAction, type);
                break;
            case MessageActions.RECIVE_TASK_CHAT_MSG:
                praseReciveMsg((JSONObject) storeAction.getActiontData().get(0));
                break;
            case MessageActions.RECIVE_NORMAL_CHAT_MSG:
                praseReciveMsg((JSONObject) storeAction.getActiontData().get(0));
                break;
            case MessageActions.RECIVE_UNREAD_NORMAL_CHAT_MSG:
                praseReciveNormalChatUnReadMsg((JSONObject) storeAction.getActiontData().get(0));
                break;
            case DataBaseActions.QUERY_RESULT_IS_EMPTY:
                int emptyType = (int) storeAction.getActiontData().get(0);
                if (emptyType == DataBaseActions.QUERY_NORMAL_CHAT_LIST) {
                    getNormalChatList();
                    return;
                } else if (emptyType == DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID) {
                    if (storeAction.getActiontData().get(2) != null) {
                        getVchatWindowInfo((String) storeAction.getActiontData().get(1), (String) storeAction.getActiontData().get(3));
                    }
                    return;
                }
                if (emptyType == DataBaseActions.GET_GROUP_CHAT_HISTORY) {
                    long time = (long) storeAction.getActiontData().get(1);
                    if (time == 0) {
                        getLoaclDataEmpty((int) storeAction.getActiontData().get(0), (String) storeAction.getActiontData().get(2));
                    } else {
                        if (chatOperationReference.get() != null) {
                            chatOperationReference.get().onHaveNoMoreMeesage();
                        }
                    }
                }
                if (emptyType == DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE) {
                    synchronized (this) {
                        unReadState |= GET_LOCAL_SUCCESS_MASK;
                        if ((unReadState & GET_NET_SUCCESS_MASK) > 0) {
                            if (isGetVchatListSuccess) {//获取完了所有的未读消息，并且获取到了微聊列表
                                Log.i(TAG, "onEvent: data base empty DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE");
                                updateVchatUnreadCount();
                                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
                            }
                            if (vchatViewReference.get() != null) {
                                if (isGetVchatListSuccess) {
                                    vchatViewReference.get().refreshVchatList();
                                }
                            }
                        }
                    }
                }
                break;
            case DataBaseActions.QUERY_MESSAGE_HISTORY_SUCCESS:
                if (chatOperationReference.get() != null) {
                    chatOperationReference.get().showMessageHistory((ArrayList<MessageBean>) storeAction.getActiontData().get(0), (long) storeAction.getActiontData().get(1));
                }
                break;
            case MessageActions.DELETE_VCHAT_ONE_CHAT:
                if (vchatViewReference.get() != null) {
                    vchatViewReference.get().deleteOneVchat(true, (String) storeAction.getActiontData().get(1));
                } else {
                    dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
                }
                break;
            case DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS:
                Log.i(TAG, "onEvent: QUERY_CHAT_UNREAD_MSG_SUCCESS" + storeAction.getActiontData().get(0));
                if ((int) storeAction.getActiontData().get(0) == 1) {
                    //noinspection unchecked
                    queryUnReadSuccess(((ArrayList<UnReadMessageBean>) storeAction.getActiontData().get(1)));
                }
                break;
            case DataBaseActions.QUERY_NORMAL_CHAT_LIST_SUCCESS:
                vChatList = (ArrayList<VChatBean>) storeAction.getActiontData().get(0);
                queryLocalChatListSuccess();
                break;
            case DataBaseActions.QUERY_CHAT_BY_ID_SUCCESS:
                ChatWindowInfoBean windowInfoBean = (ChatWindowInfoBean) storeAction.getActiontData().get(0);

                if ((int) storeAction.getActiontData().get(1) == 1) {
//                    if (!getInfoList.contains(windowInfoBean.groupId)) {
//                        break;
//                    }
//                    Log.i(TAG, "onEvent: " + neededuuId + "" + storeAction.getActiontData().get(2));
//                    getInfoList.remove(windowInfoBean.groupId);
//                    if (!neededuuId.equals(storeAction.getActiontData().get(2))) {
//                        break;
//                    }
                    String uuid = (String) storeAction.getActiontData().get(2);
                    if (uuid.equals(AddressBookFragment.ADDRESS_BOOK_CHAT_TAG)) {//通讯录界面进入的
                        if (addBookViewOperation.get() != null) {
                            addBookViewOperation.get().getChatWindowInfoSuccess(windowInfoBean);
                        }
                        return;
                    }

                    if (TextUtils.isEmpty(windowInfoBean.taskId)) {
                        if (vchatViewReference.get() != null) {
                            vchatViewReference.get().getNormalChatWindowResult(windowInfoBean, (String) storeAction.getActiontData().get(2));
                        }
                    }
                }

                break;
            case DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID:
                String mark = (String) storeAction.getActiontData().get(1);
                Log.i(TAG, "onEvent: =========== messagestores QUERY_SMALL_MAIL_BY_MESSAGE_ID" + mark + (storeAction.getActiontData().get(0) == null));
                if (storeAction.getActiontData().get(0) == null) {
                    String id = (String) storeAction.getActiontData().get(2);
                    JSONObject custom = new JSONObject();
                    try {
                        custom.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
                        custom.put("mailId", id);
                        createHttpRequestJson(custom, GET_SMALL_BY_ID, mark);
                        OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getSingleMail", httpCallback, custom, "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (chatOperationReference.get() != null) {
                        chatOperationReference.get().onGetSmallMailDetail(mark, (SmallMailBean) storeAction.getActiontData().get(0));
                    }
                }
                break;
            case DataBaseActions.INSERT_ONT_CHAT_SUMMARY_SUCCESS:
                VChatBean vChatBean = (VChatBean) storeAction.getActiontData().get(0);
                addNewVchatBean(vChatBean);
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
                if (vchatViewReference.get() != null) {
                    Log.i(TAG, "onEvent: refreshList");
                    vchatViewReference.get().refreshVchatList();
                }
                break;
            case MessageActions.SET_UNREAD_MSG_TO_READ:
                String groupId = (String) storeAction.getActiontData().get(0);
                removeUnReadMsg(groupId);
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
                if (vchatViewReference.get() != null) {
                    vchatViewReference.get().setOnReadMsgToRead((String) storeAction.getActiontData().get(0));
                }
                break;
            case MessageActions.RENAME_CHAT_SUBJECT:
//                String groupId = (String) storeAction.getActiontData().get(0);
                Log.i(TAG, "onEvent: RENAME_CHAT_SUBJECT");
                if (storeAction.getActiontData().get(2) != null) {
                    if (currentWindow != null)
                        currentWindow.taskTitle = (String) storeAction.getActiontData().get(1);
                    if (chatOperationReference.get() != null) {
                        chatOperationReference.get().renameChatSubject((String) storeAction.getActiontData().get(1));
                    }
                    return;
                }
                renameChatSubject((String) storeAction.getActiontData().get(0), (String) storeAction.getActiontData().get(1));
                break;
        }
    }

    private void renameChatSubject(String groupId, String subject) {
        if (currentVchatBean != null && currentVchatBean.groupId.equals(groupId)) {
            currentVchatBean.topic = subject;
            currentWindow.taskTitle = subject;
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().renameChatSubject(subject);
            }
        } else {
            VChatBean vChatBean = getVchatBeanByGroupId(groupId);
            if (vChatBean != null) {
                vChatBean.topic = subject;
                if (currentVchatBean == null && vchatViewReference.get() != null) {
                    vchatViewReference.get().refreshVchatList();
                }
            }
        }
    }

    private void addNewVchatBean(VChatBean vChatBean) {
        if (needGetWindowInfoList != null)
            needGetWindowInfoList.remove(vChatBean.groupId);
        if (localNotHasWindowUnreadBeans != null) {
            Iterator<UnReadMessageBean> it = localNotHasWindowUnreadBeans.iterator();
            while (it.hasNext()) {
                UnReadMessageBean unReadMessageBean = it.next();
                if (unReadMessageBean.groupId.equals(vChatBean.groupId)) {
                    if (unReadMessageBean.isNeedShowUnReadNum()) {
                        vChatUnReadMsg.add(unReadMessageBean);
                        vChatBean.unReadMsgNum++;
                    }
                    if (vChatBean.lastChatTime < unReadMessageBean.msgSendTime) {
                        vChatBean.lastChatTime = unReadMessageBean.msgSendTime;
                        vChatBean.lastMsg = unReadMessageBean.content;
                        vChatBean.msgId = unReadMessageBean.msgId;
                    }
                    it.remove();
                }
            }
        }
        Log.i(TAG, "addNewVchatBean: contains" + vChatList.contains(vChatBean) + "=====" + vChatBean.toString());
        if (!vChatList.contains(vChatBean)) {
            vChatList.add(1, vChatBean);
        }
    }


    public void getNormalChatWindowInfo(VChatBean mBean, String uuid) {
        dispatcher.dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID, mBean.groupId, mBean.topic, uuid);
    }

    private void queryLocalChatListSuccess() {
        initGroupIds();
        for (VChatBean chatBean : vChatList) {
            chatGroupIds.add(chatBean.groupId);
            chatBean.setAvatars(avatars);
        }

        if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0 && (unReadState & GET_NET_SUCCESS_MASK) > 0) {//此时是未读消息已经获取完毕
            Log.i(TAG, "queryLocalChatListSuccess: query local chat list success" + unReadState);
            updateVchatUnreadCount();
            dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
        }

        isGetVchatListSuccess = true;
        if (vchatViewReference.get() != null) {
            vchatViewReference.get().showNoralChatList(vChatList);
        }
    }


    public void setLoadFileManager(LoadFileManager loadFileManager) {
        this.loadFileManager = loadFileManager;
    }

    private void setMessageToList(ArrayList<UserInfoBean> to, MessageBean messageBean) {
        JSONArray toArray = getSendPresonArray(to);
        messageBean.to = toArray.toString();
        messageBean.toArray = toArray;
        messageBean.initToList(toArray);
    }

    public void recallOneMessage(MessageBean messageBean) {
        JSONObject requsetJson = new JSONObject();
        try {
            requsetJson.put("_id", messageBean.id);
            requsetJson.put("userId", messageBean.sendMessageUser.id);
            createHttpRequestJson(requsetJson, RECALL_ONE_CHAT_MSG, messageBean.groupId);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "chatMsgToBack", requsetJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, RECALL_ONE_CHAT_MSG, createRequestJson(requsetJson, messageBean.groupId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteOneChatMsg(String msgId, String groupId, String taskId, boolean isMsgSendSuccess) {
        if (!isMsgSendSuccess) {
//            Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.DELETE_ONE_CHAT_MESSAGE, true, msgId);
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onDeleteOneMessage(true, msgId);
            }
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, groupId, msgId);
            return;
        }
        JSONObject body = new JSONObject();
        try {
            if (!TextUtils.isEmpty(taskId))
                body.put("taskId", taskId);
            body.put("_id", msgId);
            body.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
            body.put("groupId", groupId);
            createHttpRequestJson(body, DELETE_ONE_CHAT_MSG, groupId);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "deleteGroupRecode", body, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, DELETE_ONE_CHAT_MSG, createRequestJson(body, groupId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteOneChat(String groupId) {
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
            createHttpRequestJson(body, DELETE_GROUP_CHAT, null);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "deleteGroup", body, httpCallback, null);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, DELETE_GROUP_CHAT, createRequestJson(body, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getLoaclDataEmpty(int action, String currentGroupId) {
        if (action == DataBaseActions.GET_GROUP_CHAT_HISTORY) {
            getNetChatHistoryByGroupId(currentGroupId);
        }
    }

    public void setTaskTitle(String taskId, String taskTitle) {
        CustomProperty customProperty = new CustomProperty();
        customProperty.taskId = taskId;
        customProperty.taskTitle = taskTitle;
        customProperty.method = MessageActions.SET_CHAT_TITLE;
        String uuid = UUID.randomUUID().toString();
        if (mPropertyArrayMap == null) {
            mPropertyArrayMap = new ArrayMap<>();
        }
        mPropertyArrayMap.put(uuid, customProperty);
        JSONObject propertiesJson = new JSONObject();
        try {
            propertiesJson.put("Custom.taskTitle", customProperty.taskTitle);
            propertiesJson.put("Subject", customProperty.taskTitle);
            propertiesJson.put("SubjectToTask", customProperty.taskTitle);
            setCustomProperty(customProperty.taskId, propertiesJson, uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTaskPlace(String taskId, String taskPlace) {
        CustomProperty customProperty = new CustomProperty();
        customProperty.taskId = taskId; // 任务id
        customProperty.taskPlace = taskPlace; // 任务地点
        customProperty.method = MessageActions.SET_TASK_PLACE;
        String uuid = UUID.randomUUID().toString();
        if (mPropertyArrayMap == null) {
            mPropertyArrayMap = new ArrayMap<>();
        }
        mPropertyArrayMap.put(uuid, customProperty);
        JSONObject propertiesJson = new JSONObject();
        try {
            propertiesJson.put("Custom.taskPlace", customProperty.taskPlace);
            setCustomProperty(customProperty.taskId, propertiesJson, uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTaskPrivate(String taskId, int taskPrivate) {
        CustomProperty customProperty = new CustomProperty();
        customProperty.taskId = taskId;
        customProperty.taskPrivate = taskPrivate;
        customProperty.method = MessageActions.SET_TASK_PRIVATE;
        String uuid = UUID.randomUUID().toString();
        if (mPropertyArrayMap == null) {
            mPropertyArrayMap = new ArrayMap<>();
        }
        mPropertyArrayMap.put(uuid, customProperty);
        JSONObject propertiesJson = new JSONObject();
        try {
            propertiesJson.put("Custom.setPrivate", customProperty.taskPrivate);
            setCustomProperty(customProperty.taskId, propertiesJson, uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCustomProperty(String taskId, JSONObject propertiesJson, String uuid) {
        JSONObject mRequestJson = new JSONObject();
        try {
//            mRequestJson.put("uuid", uuid);
            mRequestJson.put("userId", USER_ID);
            mRequestJson.put("_id", taskId);
            mRequestJson.put("properties", propertiesJson);

            JSONObject clentId = new JSONObject();
            clentId.put("uuid", uuid);

            if (propertiesJson.has("Custom.taskTitle")) {
                clentId.put("type", CHANGE_TASK_TITLE);
                createHttpRequestJson(mRequestJson, SET_CUSTOM_PROPERTY, clentId.toString());
//                dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SET_CUSTOM_PROPERTY, createRequestJson(mRequestJson, CHANGE_TASK_TITLE));
                OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "setCustomProperty", mRequestJson, httpCallback, "");
            } else {
                createHttpRequestJson(mRequestJson, SET_CUSTOM_PROPERTY, clentId.toString());
                OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "setCustomProperty", mRequestJson, httpCallback, "");
//                dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SET_CUSTOM_PROPERTY, createRequestJson(mRequestJson, null));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getNotesByGroupId(String groupId) {
        JSONObject mRequestJson = new JSONObject();
        try {
            mRequestJson.put("userId", USER_ID);
            mRequestJson.put("groupId", groupId);
            createHttpRequestJson(mRequestJson, GET_NOTES_BY_GROUP_ID, null);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getNotesByGroupId", httpCallback, mRequestJson, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_NOTES_BY_GROUP_ID, createRequestJson(mRequestJson, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setNotesByGroupId(String groupId, String notes) {
        JSONObject mRequestJson = new JSONObject();
        try {
            mRequestJson.put("userId", USER_ID);
            mRequestJson.put("groupId", groupId);
            mRequestJson.put("notes", notes);
            createHttpRequestJson(mRequestJson, SET_NOTES_BY_GROUP_ID, notes);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "setNotesByGroupId", mRequestJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SET_NOTES_BY_GROUP_ID, createRequestJson(mRequestJson, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改微聊话题
     *
     * @param groupId
     * @param groupName
     */
    public void updateGroupName(String groupId, String groupName) {
        JSONObject mRequestJson = new JSONObject();
        try {
            mRequestJson.put("userId", USER_ID);
            mRequestJson.put("groupId", groupId);
            mRequestJson.put("subject", groupName);

            createHttpRequestJson(mRequestJson, UPDATE_GROUP_NAME, groupName);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "updateGroupName", mRequestJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, UPDATE_GROUP_NAME, createRequestJson(mRequestJson, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void praseReciveNormalChatUnReadMsg(JSONObject jsonObject) {
        int type = jsonObject.optInt("type");
        if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {
            String msgId = jsonObject.optString("mid");
            removeOneUnReadMsgById(msgId);
//            dispatcher.dispatchUpdateUIEvent(MessageActions.NORMAL_CHAT_RECIVE_UNREAD_MESSAGE, vChatUnReadMsg, jsonObject.optString("groupId"), msgId);
            if (vchatViewReference.get() != null) {
                vchatViewReference.get().reciveOneDeleteMsg(jsonObject.optString("groupId"), msgId);
            } else {
                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
            return;
        }

        MessageBean messageBean = new MessageBean(jsonObject);
        messageBean.sendTime = System.currentTimeMillis();
        messageBean.isSendSuccess = true;
        messageBean.isMessageNew = false;
        if (jsonObject.has("mid"))
            messageBean.id = jsonObject.optString("mid");


        if (messageBean.type == MessageBean.RECALL_MESSAGE) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 2, messageBean.groupId, messageBean.id);
            removeRecallUnRead(messageBean);
            return;
        }

        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);

        Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + jsonObject.optInt("isRead"));

        UnReadMessageBean unReadMEssage = new UnReadMessageBean(jsonObject);


        if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id) ||
                messageBean.type == MessageBean.SYSTEM_TIP_MESSAGE || jsonObject.optInt("isRead") == 1) {//此消息是已读消息
            if (chatGroupIds == null) {
                return;
            }

            unReadMEssage.type = MessageBean.MESSAGE_CHAT;
            unReadMEssage.isNeedShowNum = 0;
            unReadMEssage.msgSendTime = messageBean.sendTime;
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_UNREAD_MESSAGE, unReadMEssage);

            Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + chatGroupIds.contains(messageBean.groupId));

            if (chatGroupIds.contains(messageBean.groupId)) {//聊天列表有这个聊天

                VChatBean vChatBean = getVchatBeanByGroupId(unReadMEssage.groupId);

                assert vChatBean != null;
                Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + vChatBean.lastChatTime + "----" + unReadMEssage.msgSendTime);
                if (vChatBean.lastChatTime < unReadMEssage.msgSendTime) {
                    vChatBean.lastChatTime = unReadMEssage.msgSendTime;
                    vChatBean.lastMsg = unReadMEssage.content;
                    vChatBean.msgId = unReadMEssage.msgId;
                }
                Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + vChatBean.lastMsg);
                if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                    removeUnReadMsg(messageBean.groupId);
                    vChatBean.unReadMsgNum = 0;
                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, messageBean.groupId);
                }

                if (vchatViewReference.get() != null) {
                    vchatViewReference.get().reciveOneNormalMsg(vChatBean);
                }
//                if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
//                    if (vchatViewReference.get() != null) {
//                        vchatViewReference.get().setOnReadMsgToRead(messageBean.groupId);
//                    }
//                }
            } else { //此消息是已读消息，聊天列表没有这个聊天，这时候需要主动获取这个聊天的信息，并显示
                if (gettingWindowInfoList == null)
                    gettingWindowInfoList = new ArrayList<>();

                if (localNotHasWindowUnreadBeans == null) {
                    localNotHasWindowUnreadBeans = new ArrayList<>();
                }
                Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + unReadMEssage.msgSendTime);
                localNotHasWindowUnreadBeans.add(unReadMEssage);
                if (gettingWindowInfoList.contains(messageBean.groupId))
                    return;
                gettingWindowInfoList.add(messageBean.groupId);
                getVchatWindowInfo(messageBean.groupId);
            }
            return;
        }

        unReadMEssage.type = MessageBean.MESSAGE_CHAT;
        unReadMEssage.isNeedShowNum = 1;
        unReadMEssage.msgSendTime = messageBean.sendTime;

        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_UNREAD_MESSAGE, unReadMEssage);
        initUnreadDataStruct();
        if (chatGroupIds.contains(unReadMEssage.groupId)) {
            vChatUnReadMsg.add(unReadMEssage);

            VChatBean vChatBean = getVchatBeanByGroupId(unReadMEssage.groupId);
            assert vChatBean != null;
            if (vChatBean.lastChatTime < unReadMEssage.msgSendTime) {
                vChatBean.lastChatTime = unReadMEssage.msgSendTime;
                vChatBean.lastMsg = unReadMEssage.content;
                vChatBean.msgId = unReadMEssage.msgId;
                vChatBean.unReadMsgNum++;
            }

            if (vchatViewReference.get() != null) {
                vchatViewReference.get().reciveOneNormalMsg(vChatBean);
            } else {
                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
        } else { //此消息不是自己发送的消息，而且这个聊天不存在。
            if (gettingWindowInfoList == null)
                gettingWindowInfoList = new ArrayList<>();
            unReadMEssage.type = MessageBean.MESSAGE_CHAT;
            unReadMEssage.isNeedShowNum = 1;
            if (localNotHasWindowUnreadBeans == null) {
                localNotHasWindowUnreadBeans = new ArrayList<>();
            }
            localNotHasWindowUnreadBeans.add(unReadMEssage);
            Log.i(TAG, "praseReciveNormalChatUnReadMsg: " + gettingWindowInfoList.contains(messageBean.groupId));
            if (gettingWindowInfoList.contains(messageBean.groupId))
                return;
            gettingWindowInfoList.add(messageBean.groupId);
            getVchatWindowInfo(messageBean.groupId);
        }
    }

    private void removeRecallUnRead(MessageBean messageBean) {
        boolean isChanged = removeOneUnReadMsgById(messageBean.id);
        if (isChanged) {
            if (vchatViewReference.get() != null) {
                vchatViewReference.get().reciveOneRecallMsg(messageBean);
            } else {
                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
        }
//            dispatcher.dispatchUpdateUIEvent(MessageActions.NORMAL_CHAT_RECIVE_UNREAD_MESSAGE, vChatUnReadMsg, messageBean);
    }

    private boolean removeOneUnReadMsgById(String id) {
        Iterator<UnReadMessageBean> iterator = vChatUnReadMsg.iterator();
        boolean isChanged = false;
        while (iterator.hasNext()) {
            UnReadMessageBean unReadMessageBean = iterator.next();
            if (unReadMessageBean.msgId.equals(id)) {
                iterator.remove();
                isChanged = true;
            }
        }
        return isChanged;
    }

    private void getVchatWindowInfo(String groupId, String uuid) {
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("userId", USER_ID);
//            neededGroupId = groupId;
            createHttpRequestJson(body, GET_CHAT_WINDOW_INFO, uuid);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "initChatWindowInfo", httpCallback, body, "initChatWindowInfo" + groupId);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_CHAT_WINDOW_INFO, createRequestJson(body, topic));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getServerGiveWindowInfo(String groupId) {
        if (!chatGroupIds.contains(groupId)) {
            if (gettingWindowInfoList == null)
                gettingWindowInfoList = new ArrayList<>();
            if (gettingWindowInfoList.contains(groupId))
                return;
            gettingWindowInfoList.add(groupId);
            getVchatWindowInfo(groupId);
        }
    }

    private void getVchatWindowInfo(String groupId) {
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("userId", USER_ID);
            createHttpRequestJson(body, GET_CHAT_WINDOW_INFO, "getClientHasNoWindow");
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "initChatWindowInfo", httpCallback, body, "initChatWindowInfo");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_CHAT_WINDOW_INFO, createRequestJson(body, "getClientHasNoWindow"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearServerUnReadMsg(String taskId, String groupId) {
        JSONObject body = new JSONObject();
        JSONObject ids = new JSONObject();
        try {
            if (taskId != null)
                ids.put("taskId", taskId);
            ids.put("groupId", groupId);
            body.put("userId", USER_ID);
            body.put("ids", ids);
            body.put("isPhone", true);
            createHttpRequestJson(body, SET_MSG_READ, null);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "setMcMsgRead", body, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SET_MSG_READ, createRequestJson(body, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void praseReciveMsg(JSONObject jsonObject) {
        int type = jsonObject.optInt("type");
        if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, jsonObject.optString("groupId"), jsonObject.optString("mid"));
//            dispatcher.dispatchUpdateUIEvent(MessageActions.DELETE_ONE_CHAT_MESSAGE, true, jsonObject.optString("mid"));
            if (currentVchatBean != null) {
                if (currentVchatBean.msgId.equals(jsonObject.optString("mid"))) {
                    currentVchatBean.lastMsg = "你删除了一条消息";
                }
            }
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onDeleteOneMessage(true, jsonObject.optString("mid"));
            }
            return;
        }
        MessageBean message = new MessageBean(jsonObject);
        if (jsonObject.has("mid"))
            message.id = jsonObject.optString("mid");
        message.isSendSuccess = true;
        message.isMessageNew = false;
        if (message.type == MessageBean.RECALL_MESSAGE) {
            Log.i(TAG, "praseReciveMsg: recall msg" + message.id + "=======" + message.groupId);
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 2, message.groupId, message.id);
//            Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.RECALL_COE_CHAT_MESSAGE, true, message.id);
            if (currentVchatBean != null) {
                if (currentVchatBean.msgId.equals(message.id)) {
                    currentVchatBean.lastMsg = message.sendMessageUser.name + "撤回了一条消息";
                }
            }
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onRecallOneMessage(true, message.id);
            }
            return;
        }

        if (jsonObject.has("exitUser")) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT,
                    jsonObject.optString("exitUser"), jsonObject.optString("groupId"));
            dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, jsonObject);
        }

        message.sendTime = System.currentTimeMillis();

        if (currentVchatBean != null) {
            currentVchatBean.lastChatTime = message.sendTime;
            currentVchatBean.msgId = message.id;
            if (message.isImageMsg()) {
                currentVchatBean.lastMsg = "[图片]";
            } else if (message.isAttachmentMsg()) {
                currentVchatBean.lastMsg = "[文件]";
            } else {
                currentVchatBean.lastMsg = message.content;
            }
        }

        Log.i(TAG, "praseReciveMsg: ============message type" + message.type);
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, message);
        if (jsonObject.has("taskId")) {
//            dispatcher.dispatchUpdateUIEvent(MessageActions.RECIVE_TASK_CHAT_MSG, message.groupId, message);
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onReciveMessage(message.groupId, message);
            }
        } else {
//            dispatcher.dispatchUpdateUIEvent(MessageActions.RECIVE_NORMAL_CHAT_MSG, message.groupId, message);
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onReciveMessage(message.groupId, message);
            }
        }
    }

    private void sendTaskChatMsg(MessageBean message) {
        JSONObject body = new JSONObject();
        JSONObject msg = new JSONObject();
        try {
            if (currentUser == null) {
                currentUser = new JSONObject();
                //UserInfoJsonObject userInfoJsonObject = PreferencesHelper.getInstance().getUserInfoToNative();
                currentUser.put("id", message.sendMessageUser.id);
                currentUser.put("uid", message.sendMessageUser.uid);
                currentUser.put("avatar", message.sendMessageUser.avatar);
                currentUser.put("name", message.sendMessageUser.name);
                currentUser.put("email", message.sendMessageUser.email);
            } else {
                currentUser.put("avatar", message.sendMessageUser.avatar);
            }
            //sendMessageBean.sendMessageUser = new UserInfoBean(currentUser);

            body.put("mailId", message.mailId);
            body.put("taskId", message.taskId);
            msg.put("title", message.taskTitle);
            msg.put("createTime", message.taskCreateTime);

            msg.put("from", currentUser);
            msg.put("to", message.toArray);
            if (message.type == MessageBean.IMAGE_MESSAGE_SEND || message.type == MessageBean.ATTACHMENT_MESSAGE_SEND) {
                msg.put("content", message.content);
                msg.put("isEncrypt", false);
            } else {
                msg.put("content", EncryptUtil.encrypt2aes(message.content));
                msg.put("isEncrypt", true);
            }

            if (message.isEmojiMessage()) {
                msg.put("hasEmotion", true);
            }
            msg.put("type", MessageBean.MESSAGE_TASK_CHAT);
            msg.put("notShowMe", false);
            msg.put("isPhone", true);
            body.put("uuid", message.uuid);
            body.put("msg", msg);

            // 附件消息的附加属性
            if (message.type == MessageBean.ATTACHMENT_MESSAGE_SEND) {
                this.buildAttachmentJson(message, msg);
            } else if (message.type == MessageBean.SYSTEM_TIP_MESSAGE) {
                msg.put("type", 9999);
            }

            // 发送网络请求
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SEND_GROUP_MSG, createRequestJson(body, message.uuid));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject currentUser;

    private void sendChatMsg(MessageBean message) {
        JSONObject body = new JSONObject();
        JSONObject msg = new JSONObject();
        try {
            if (currentUser == null) {
                currentUser = new JSONObject();
                // UserInfoJsonObject userInfoJsonObject = PreferencesHelper.getInstance().getUserInfoToNative();
                currentUser.put("id", message.sendMessageUser.id);
                currentUser.put("uid", message.sendMessageUser.uid);
                currentUser.put("avatar", message.sendMessageUser.avatar);
                currentUser.put("name", message.sendMessageUser.name);
                currentUser.put("email", message.sendMessageUser.email);
            } else {
                currentUser.put("avatar", message.sendMessageUser.avatar);
            }
            //   sendMessageBean.sendMessageUser = new UserInfoBean(currentUser);

            body.put("mailId", message.mailId);
            msg.put("from", currentUser);
            msg.put("to", message.toArray);
            if (message.type == MessageBean.IMAGE_MESSAGE_SEND || message.type == MessageBean.ATTACHMENT_MESSAGE_SEND) {
                msg.put("content", message.content);
                msg.put("isEncrypt", false);
            } else {
                msg.put("content", EncryptUtil.encrypt2aes(message.content));
                msg.put("isEncrypt", true);
            }
            msg.put("type", MessageBean.MESSAGE_CHAT);
            msg.put("notShowMe", false);
            msg.put("isPhone", true);

            if (message.isEmojiMessage()) {
                msg.put("hasEmotion", true);
            }

            body.put("uuid", message.uuid);
            body.put("msg", msg);


            // 附件消息的附加属性
            if (message.type == MessageBean.ATTACHMENT_MESSAGE_SEND) {
                this.buildAttachmentJson(message, msg);
            } else if (message.type == MessageBean.SYSTEM_TIP_MESSAGE) {
                msg.put("type", 9999);
            }

            // 发送网络请求
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SEND_CHAT_MSG, createRequestJson(body, message.uuid));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildAttachmentJson(MessageBean message, JSONObject msg) {
        try {
            JSONObject attachmentJson = new JSONObject();
            attachmentJson.put("docId", message.attachmentBean.docId);
            attachmentJson.put("name", message.attachmentBean.attachmentName);
            attachmentJson.put("size", message.attachmentBean.attachSize);
            attachmentJson.put("downloadUrl", message.attachmentBean.attachPath);
            msg.put("attachment", attachmentJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void parseServerReturnData(StoreAction storeAction, String type) {
        JSONObject data = (JSONObject) storeAction.getActiontData().get(1);
        switch (type) {
            case SEND_GROUP_MSG:
                parseSendMessageResult(data);
                break;
            case SEND_CHAT_MSG:
                parseSendMessageResult(data);
                break;
        }
    }

    private void parseSmallMail(JSONObject data) {
        SmallMailBean smallMailBean = new SmallMailBean(data.optJSONObject("data"));
        String mark = data.optString("ClientId");
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_SMALL_MAIL, smallMailBean);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onGetSmallMailDetail(mark, smallMailBean);
        }
    }

    private void praseReCallOneMsg(JSONObject data) {
        String groupId = data.optString("ClientId");
        if (data.optBoolean("type")) {
            String msgId = data.optString("_id");
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onRecallOneMessage(true, msgId);
            }
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 2, groupId, msgId);
        }
    }

    private void parseDeleteOneMsg(JSONObject data) {
        boolean result = data.optBoolean("type");
        String msgId = data.optString("_id");
        String groupId = data.optString("ClientId");
//        Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.DELETE_ONE_CHAT_MESSAGE, result, msgId);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onDeleteOneMessage(result, msgId);
        }
        if (result) {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, groupId, msgId);
        }
    }

    private void parseDeleteGroupChat(JSONObject data) {
        boolean result = data.optBoolean("type");
        String groupId = data.optString("groupId");
        chatGroupIds.remove(groupId);
//        Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.DELETE_VCHAT_ONE_CHAT, result, groupId);
        if (vchatViewReference.get() != null) {
            vchatViewReference.get().deleteOneVchat(result, groupId);
        } else {
            dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
        }
        if (result) {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_MESSAGE_BY_GROUPID, groupId);
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
        }
    }


    public void setAvatars(ArrayMap<String, Long> avatars) {
        this.avatars = avatars;
    }

    private void parseSetCustomProperty(JSONObject data) {
//        JSONObject docsJson = data.optJSONObject("docs");
        JSONObject clientId = null;
        if (data.has("type")) {
            boolean res = data.optBoolean("type");
//            clientId = data.optJSONObject("ClientId");
            String result = data.optString("ClientId");
            try {
                clientId = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (clientId != null) {

                CustomProperty customProperty = mPropertyArrayMap.get(clientId.optString("uuid"));
                if (res) {
                    String type = clientId.optString("type");
                    if (!TextUtils.isEmpty(type) && type.equals("")) {
                        dispatcher.dispatchDataBaseAction(DataBaseActions.RENAME_CHAT_SUBJECT, customProperty.taskId, customProperty.taskTitle);
                    }
                }
//            dispatcher.dispatchUpdateUIEvent(MessageActions.SET_CUSTOM_PROPERTY, res, customProperty);
                if (chatOperationReference.get() != null) {
                    chatOperationReference.get().setTaskProperty(res, customProperty);
                }
            }
        }
    }

    private void parseGetNotesByGroupId(JSONObject data) {
        String notes = data.optString("notes");

        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onGetNotes(notes);
        }
    }

    /**
     * 修改微聊话题返回数据处理
     *
     * @param data {"type":"updateGroupName","docs":{"res":true}}
     */
    private void parseUpdateGroupName(JSONObject data) {
//        JSONObject docsJson = data.optJSONObject("docs");
        boolean res = data.optBoolean("type");
//        dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_GROUP_NAME, res);
        String subject = data.optString("ClientId");
        if (currentVchatBean != null) {
            currentVchatBean.topic = subject;
        }

        if (currentWindow != null) {
            currentWindow.taskTitle = subject;
        }
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onUpdateGroupName(res);
        }
    }

    /**
     * 修改随手记数据处理
     *
     * @param data {"type":"setNotesByGroupId","docs":{"res":{"res":"设置笔记成功"}}}
     */
    private void parseSetNotesByGroupId(JSONObject data) {
//        JSONObject docJson = data.optJSONObject("docs");
//        JSONObject resJson = docJson.optJSONObject("res");
        boolean resStr = data.optBoolean("type");
        String notes = data.optString("ClientId");
//        dispatcher.dispatchUpdateUIEvent(MessageActions.SET_NOTES_BY_GROUP_ID, resStr, notes);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onSetNotesResult(resStr, notes);
        }
    }


    /**
     * 本地和服务器所有的未读消息集合，需要提示的消息
     */
    private ArrayList<UnReadMessageBean> vChatUnReadMsg;

    private ArrayList<UnReadMessageBean> vChatNetUnReadMsg;

    /**
     * 电脑已读，手机未读的消息
     */
//    private ArrayList<UnReadMessageBean> netUnReadList;

    /**
     * 电脑和手机都未读的消息
     */
//    private ArrayList<UnReadMessageBean> netReadList;

    private ArrayList<String> readedGroupIds;

    public synchronized void praseUnReadMsgList(JSONObject data) {
        initUnreadDataStruct();
//        vChatUnReadMsg.clear();
        JSONArray messageArray;
        if (data != null)
            messageArray = data.optJSONArray("data");
        else
            messageArray = new JSONArray();
        int count = messageArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject message = messageArray.optJSONObject(i);
            if (message.optBoolean("notShowMe") && message.optJSONObject("from").
                    optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                continue;
            }
            int type = message.optInt("type");
            if (type == 2) {//普通微聊未读消息

                String content = message.optString("content");
                try {
                    if (message.optBoolean("isEncrypt") || message.optInt("isMailMsg") > 0) {
                        message.put("content", EncryptUtil.aesDecrypt(content));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MessageBean messageBean = new MessageBean(message);
                boolean isNeedNotify = false;
                UnReadMessageBean bean = new UnReadMessageBean(message);
                bean.type = MessageBean.MESSAGE_CHAT;
                bean.content = messageBean.content;
                bean.msgType = MessageBean.MESSAGE_CHAT;
                if (message.optInt("isRead") == 0) {
                    bean.isNeedShowNum = 1;
                    isNeedNotify = true;
                } else {
                    bean.isNeedShowNum = 0;
                }
                vChatNetUnReadMsg.add(bean);
                String title = message.optString("title");

                messageBean.groupId = message.optString("groupId");
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");
                needToSaveMsg.add(messageBean);
                if (!messageBean.isCurrentUserMsg() && isNeedNotify)
                    Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.SEND_NOTIFICATION, TextUtils.isEmpty(title) ? messageBean.sendMessageUser.name : title, messageBean);
            } else if (type == MessageActions.MESSAGE_RECALL && TextUtils.isEmpty(message.optString("taskId"))) {//微聊撤回某条消息的未读消息
                MessageBean messageBean = new MessageBean(message);
                messageBean.groupId = message.optString("groupId");
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");

                UnReadMessageBean bean = new UnReadMessageBean(message);
                bean.type = MessageBean.MESSAGE_CHAT;
                bean.content = messageBean.content;
                bean.msgType = MessageActions.MESSAGE_RECALL;
                if (message.optInt("isRead") == 0) {
                    bean.isNeedShowNum = 1;
                } else if (message.optInt("isRead") == 1) {
                    bean.isNeedShowNum = 0;
                }
                vChatNetUnReadMsg.add(bean);
                if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0) {
                    for (UnReadMessageBean localBean : vChatUnReadMsg) {
                        if (localBean.msgId != null && localBean.msgId.equals(messageBean.id)) {
                            localBean.content = messageBean.content;
                        }
                    }
                }
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 3, messageBean);
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_UNREAD_RECALL_MESSAGE_BY_MSG_ID, messageBean.id);
            } else if (type == MessageActions.MESSAGE_SYSTEM_TIP) {//系统提示未读消息
                String content = message.optString("content");
                try {
                    if (message.optBoolean("isEncrypt") || message.optInt("isMailMsg") > 0) {
                        message.put("content", EncryptUtil.aesDecrypt(content));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MessageBean messageBean = new MessageBean(message);
                messageBean.groupId = message.optString("groupId");
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");
                needToSaveMsg.add(messageBean);
                if (!message.has("taskId")) {
                    UnReadMessageBean bean = new UnReadMessageBean(message);
                    bean.type = MessageBean.MESSAGE_CHAT;
                    bean.msgType = MessageActions.MESSAGE_SYSTEM_TIP;
                    bean.content = messageBean.content;
                    bean.isNeedShowNum = 0;
                    vChatNetUnReadMsg.add(bean);
                }
                if (message.has("exitUser")) {
                    dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT,
                            message.optString("exitUser"), message.optString("groupId"));
                }
            } else if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {//删除某条消息的未读消息
                String msgId = message.optString("mid");
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, message.optString("groupId"), message.optString("mid"));
                if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0) {
                    Iterator<UnReadMessageBean> it = vChatUnReadMsg.iterator();
                    while (it.hasNext()) {
                        UnReadMessageBean bean = it.next();
                        if (bean.msgId != null && bean.msgId.equals(msgId)) {
                            it.remove();
                        }
                    }
                }
            } else if (type == MessageActions.MESSAGE_DELETE_GROUP) {//删除一个微聊的未读消息
                String groupId = message.optString("groupId");
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_MESSAGE_BY_GROUPID, groupId);
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
                if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0) {
                    Iterator<UnReadMessageBean> it = vChatUnReadMsg.iterator();
                    while (it.hasNext()) {
                        UnReadMessageBean bean = it.next();
                        if (bean.groupId != null && bean.groupId.equals(groupId)) {
                            it.remove();
                        }
                    }
                }
                if (isGetVchatListSuccess) {
                    removeChatId(groupId);
                    removeVchatByGroupId(groupId);
                }
            } else if (type == MessageActions.MESSAGE_REFRESH_MEMBERS) {//更新聊天人员的通知　
                String id = message.has("taskId") ? message.optString("taskId") : message.optString("groupId");
                dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, message, id);
            } else if (type == MessageActions.MESSAGE_ADD_MEMBER) {//聊天增加人员的通知
                dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, message);
            } else if (type == MessageActions.MESSAGE_TO_READ && TextUtils.isEmpty(message.optString("taskId"))) {//设置某个微聊为已读
                readedGroupIds.add(message.optString("groupId"));
            }
        }

        for (String groupId : readedGroupIds) {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
            if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0) {
                removeUnReadMsg(groupId);
            }
        }

        if (vChatNetUnReadMsg.size() > 0) {
            vChatUnReadMsg.addAll(vChatNetUnReadMsg);
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_UNREAD_MESSAGE_LIST, vChatNetUnReadMsg);
        }
        if (needToSaveMsg.size() > 0)
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_MESSAGE_LIST, needToSaveMsg, 1);
        Log.i(TAG, "praseUnReadMsgList: " + vChatUnReadMsg.size());

        unReadState |= GET_NET_SUCCESS_MASK;

        Log.i(TAG, "praseUnReadMsgList: " + unReadState);
        if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0) {
            if (isGetVchatListSuccess) {
                Log.i(TAG, "praseUnReadMsgList: get Net unread success");
                updateVchatUnreadCount();
                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
            if (vchatViewReference.get() != null) {
                if (isGetVchatListSuccess) {
                    vchatViewReference.get().refreshVchatList();
                }
            }
        }
    }

    private void removeVchatByGroupId(String groupId) {
        Iterator<VChatBean> it = vChatList.iterator();
        while (it.hasNext()) {
            VChatBean bean = it.next();
            if (bean.groupId.equals(groupId)) {
                it.remove();
            }
        }
    }


    private synchronized void queryUnReadSuccess(ArrayList<UnReadMessageBean> unReadMessageBeans) {
        Log.i(TAG, "queryUnReadSuccess: " + unReadState);
        ArrayList<UnReadMessageBean> vChatLocalUnReadMsg;
        if ((unReadState & GET_NET_SUCCESS_MASK) > 0) {
            if ((unReadState & RE_GET_LOCAL_UNREAD_MASK) == 0) {
                unReadState |= RE_GET_LOCAL_UNREAD_MASK;
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE, MessageBean.MESSAGE_CHAT);
                return;
            } else {
                vChatUnReadMsg.clear();
                vChatLocalUnReadMsg = unReadMessageBeans;
                vChatUnReadMsg.addAll(vChatLocalUnReadMsg);
                vChatLocalUnReadMsg.clear();
            }
        } else {
            vChatLocalUnReadMsg = unReadMessageBeans;
        }
        unReadState |= GET_LOCAL_SUCCESS_MASK;
        Log.i(TAG, "queryUnReadSuccess: " + unReadState);
        if ((unReadState & GET_NET_SUCCESS_MASK) > 0) {
            if (isGetVchatListSuccess) {
                Log.i(TAG, "queryUnReadSuccess: query local unread success");
                updateVchatUnreadCount();
                dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
            if (vchatViewReference.get() != null) {
                if (isGetVchatListSuccess) {
                    vchatViewReference.get().refreshVchatList();
                }
            }
        } else {
            if (vChatUnReadMsg == null)
                vChatUnReadMsg = new ArrayList<>();
            vChatUnReadMsg.addAll(vChatLocalUnReadMsg);
            vChatLocalUnReadMsg.clear();
        }
    }

    private void initUnreadDataStruct() {
        if (vChatUnReadMsg == null) {
            vChatUnReadMsg = new ArrayList<>();
        }

        if (vChatNetUnReadMsg == null) {
            vChatNetUnReadMsg = new ArrayList<>();
        }
        vChatNetUnReadMsg.clear();


        if (needToSaveMsg == null)
            needToSaveMsg = new ArrayList<>();
        needToSaveMsg.clear();

        if (readedGroupIds == null)
            readedGroupIds = new ArrayList<>();
        readedGroupIds.clear();
    }


    public ArrayList<UnReadMessageBean> getvChatUnReadMsg() {
        return vChatUnReadMsg;
    }


//    public ArrayList<String> getChatGroupIds() {
//        if (chatGroupIds == null)
//            chatGroupIds = new ArrayList<>();
//        return chatGroupIds;
//    }

    private void praseChatWindowInfo(JSONObject data) {
        JSONObject docs = data.optJSONObject("data");
//        if (mBean.taskTitle == null || "".equals(mBean.taskTitle)) {
//            mBean.taskTitle = memberNames;
//        }
        String clientId = data.optString("ClientId");
        if (clientId.equals("getClientHasNoWindow")) {//此时是客户端主动去服务器拉取本地没有的聊天
            String groupId = docs.optString("groupId");
            if (chatGroupIds.contains(groupId))
                return;
            ChatWindowInfoBean mBean = new ChatWindowInfoBean(docs);
            VChatBean vChatBean = getCreateChatvChatBean(docs, groupId, mBean);
            Log.i(TAG, "praseChatWindowInfo: " + mBean.lastToString);
            chatGroupIds.add(vChatBean.groupId);
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, mBean);
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_SUMMARY, vChatBean, true, true);
            return;
        } else if (clientId.equals(AddressBookFragment.ADDRESS_BOOK_CHAT_TAG)) {
//            String groupId = docs.optString("groupId");
            ChatWindowInfoBean mBean = new ChatWindowInfoBean(docs);
//            VChatBean vChatBean = getCreateChatvChatBean(docs, groupId, mBean);
            Log.i(TAG, "praseChatWindowInfo: " + mBean.lastToString);
//            chatGroupIds.add(vChatBean.groupId);
//            vChatList.add(1, vChatBean);
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, mBean);
//            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_SUMMARY, vChatBean, false, false);

            if (addBookViewOperation.get() != null) {
                addBookViewOperation.get().createChatSuccess(null, mBean);
            }
            return;
        }
        ChatWindowInfoBean mBean = new ChatWindowInfoBean(docs);

        if (vchatViewReference.get() != null) {
            vchatViewReference.get().getNormalChatWindowResult(mBean, clientId);
        }
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, mBean);
    }

    @NonNull
    private VChatBean getCreateChatvChatBean(JSONObject docs, String groupId, ChatWindowInfoBean mBean) {
        VChatBean vChatBean = new VChatBean();
        vChatBean.groupId = groupId;
        if (gettingWindowInfoList != null) {
            gettingWindowInfoList.remove(vChatBean.groupId);
        }
        JSONArray member = docs.optJSONArray("member");
        vChatBean.members = member.toString();
        if (docs.has("subject"))
            vChatBean.topic = docs.optString("subject");
        else {
            vChatBean.topic = mBean.memberNames;
        }
        vChatBean.lastChatTime = System.currentTimeMillis();
        if (!docs.optBoolean("serviceNumber", false)) {
            vChatBean.isGroup = docs.optBoolean("isSingle") ? 0 : 1;
            vChatBean.setMember(member);
            vChatBean.setAvatars(avatars);
        }
        return vChatBean;
    }


    private void praseTopicChat(JSONObject data, ArrayMap<String, Long> avatars) {
        initVChatList();
        initGroupIds();
        JSONArray chatList = data.optJSONArray("data");
        int count = chatList.length();
        for (int i = 0; i < count; i++) {
            JSONObject topicChat = chatList.optJSONObject(i);
            VChatBean vChatBean = new VChatBean(topicChat, avatars);
            vChatList.add(vChatBean);
            chatGroupIds.add(vChatBean.groupId);
        }

        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_SUMMARY_LIST, vChatList);
        if ((unReadState & GET_LOCAL_SUCCESS_MASK) > 0 && (unReadState & GET_NET_SUCCESS_MASK) > 0) {
            Log.i(TAG, "praseTopicChat: get windows info success");
            updateVchatUnreadCount();
            dispatcher.dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
        }
        isGetVchatListSuccess = true;
        if (vchatViewReference.get() != null) {
            vchatViewReference.get().showNoralChatList(vChatList);
        }
    }

//    ArrayList<String> gettingWindowInfoList = new ArrayList<>();

    /**
     * 这个方法启动时候只会调用一次，在获取完所有的未读消息和微聊列表的时候
     */
    private void updateVchatUnreadCount() {
        Log.i(TAG, "updateVchatUnreadCount: " + vChatUnReadMsg.size());
        for (VChatBean vChatBean : vChatList) {
            vChatBean.unReadMsgNum = 0;
        }
        Collections.sort(vChatUnReadMsg);
        Iterator<UnReadMessageBean> unReadIterator = vChatUnReadMsg.iterator();
        while (unReadIterator.hasNext()) {
            UnReadMessageBean unReadMessageBean = unReadIterator.next();
            if (chatGroupIds.contains(unReadMessageBean.groupId)) {
                VChatBean vChatBean = getVchatBeanByGroupId(unReadMessageBean.groupId);
                Log.i(TAG, "updateVchatUnreadCount: " + (vChatBean == null));
                if (vChatBean == null)
                    continue;
                Log.i(TAG, "updateVchatUnreadCount: " + vChatBean.lastChatTime + "--------" + unReadMessageBean.msgSendTime);
                if (vChatBean.lastChatTime < unReadMessageBean.msgSendTime &&
                        !unReadMessageBean.msgId.equals(vChatBean.msgId) /*&&
                        !vChatBean.msgId.equals(unReadMessageBean.msgId*/) {
                    vChatBean.lastChatTime = unReadMessageBean.msgSendTime;
                    vChatBean.lastMsg = unReadMessageBean.content;
                    vChatBean.msgId = unReadMessageBean.msgId;
                    Log.i(TAG, "updateVchatUnreadCount: " + vChatBean.lastMsg);
                }
                if (unReadMessageBean.isNeedShowUnReadNum()) {
                    vChatBean.unReadMsgNum++;
                } else {
                    unReadIterator.remove();
                }
            } else {
                if (localNotHasWindowUnreadBeans == null) {
                    localNotHasWindowUnreadBeans = new ArrayList<>();
                }
                localNotHasWindowUnreadBeans.add(unReadMessageBean);
                unReadIterator.remove();
                if (needGetWindowInfoList == null) {
                    needGetWindowInfoList = new HashSet<>();
                }
                needGetWindowInfoList.add(unReadMessageBean.groupId);
            }
        }
        if (needGetWindowInfoList != null && needGetWindowInfoList.size() > 0) {
            getNeededWindowInfo();
        }
    }

    private void getNeededWindowInfo() {
        for (String groupId : needGetWindowInfoList) {
            getVchatWindowInfo(groupId);
        }
    }


    private VChatBean getVchatBeanByGroupId(String groupId) {
        for (VChatBean vChatBean : vChatList) {
            if (vChatBean.groupId.equals(groupId)) {
                return vChatBean;
            }
        }
        return null;
    }

    private void initVChatList() {
        if (vChatList == null) {
            vChatList = new ArrayList<>();
        }
        vChatList.clear();
    }

    private void parseSendMessageResult(JSONObject data) {
        JSONObject res = data.optJSONObject("docs");
        String uuid = data.optString("ClientId");
        if (res.optBoolean("res")) {
            String id = res.optString("_id");
            MessageBean sendMessageBean = sendMessageMap.get(uuid);
            if (currentVchatBean != null) {
                currentVchatBean.msgId = id;
            }
            if (sendMessageBean != null) {
                sendMessageBean.id = id;
                sendMessageBean.isSendSuccess = true;
                sendMessageBean.isMessageNew = false;
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 1, sendMessageBean);
//                dispatcher.dispatchUpdateUIEvent(MessageActions.SEND_MASSAGE_SUCCESS, id, uuid);
                sendMessageMap.remove(uuid);
                if (chatOperationReference.get() != null) {
                    chatOperationReference.get().onSendMessageResult(true, uuid);
                }
            }
        } else {
            MessageBean mBean = sendMessageMap.remove(uuid);
            mBean.isSendSuccess = false;
            mBean.isMessageNew = false;
//            dispatcher.dispatchUpdateUIEvent(MessageActions.SEND_MASSAGE_FAILED, uuid);
            if (chatOperationReference.get() != null) {
                chatOperationReference.get().onSendMessageResult(false, uuid);
            }
        }
    }

    private void parseMessageHistoryList(JSONObject data) {
        JSONArray messageArray = data.optJSONArray("data");
        int count = messageArray.length();
        ArrayList<MessageBean> messageList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JSONObject message = messageArray.optJSONObject(i);
            if (message.optBoolean("notShowMe")) {
                continue;
            }
            String content = message.optString("content");
            try {
                if (message.optBoolean("isEncrypt") || message.optInt("isMailMsg") > 0)
                    message.put("content", EncryptUtil.aesDecrypt(content));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MessageBean mBean = new MessageBean(message);
            if (mBean.attachmentBean != null) {
                if (currentWindowTaskBean != null) {
                    mBean.attachmentBean.taskId = currentWindowTaskBean.taskId;
                }
            }
            Log.i(TAG, "parseMessageHistoryList: " + mBean.type);
            messageList.add(mBean);
        }
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_MESSAGE_LIST, messageList);
//        dispatcher.dispatchUpdateUIEvent(MessageActions.RECIVE_MESSAGE_LIST_BY_TASKID, messageList);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().showMessageHistory(messageList, 0L);
        }
    }

/*    private void initMessageList() {
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        messageList.clear();
    }*/

    /**
     * 获取聊天记录
     */
    public void getMessageList(ChatWindowInfoBean windowInfo, long time) {
        getLocalMessageList(windowInfo.groupId, time);
    }

    private void getNetChatHistoryByGroupId(String groupId) {
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("userId", USER_ID);
            body.put("timePoint", System.currentTimeMillis());
            body.put("isPhone", true);
            body.put("pageIndex", 1);

            createHttpRequestJson(body, GET_GROUP_CHAT_HISTORY, null);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getGroupChatHistory", httpCallback, body, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getLocalMessageList(String groupId, long time) {
        String taskId = currentWindowTaskBean == null ? "" : currentWindowTaskBean.taskId;
        dispatcher.dispatchDataBaseAction(DataBaseActions.GET_GROUP_CHAT_HISTORY, groupId, time, taskId);
    }

    private JSONObject createRequestJson(JSONObject body, String clientId) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
        requestJson.put("Body", body);
        if (!TextUtils.isEmpty(clientId)) {
            requestJson.put("ClientId", clientId);
        }
        requestJson.put("isPhone", true);
        return requestJson;
    }

    private void createHttpRequestJson(JSONObject body, int type, String clientId) throws JSONException {
        JSONObject clientJson = new JSONObject();
        clientJson.put("type", type);
        if (!TextUtils.isEmpty(clientId)) {
            clientJson.put("ClientId", clientId);
        }
        body.put("isPhone", true);
        body.put("ClientId", clientJson.toString());
    }


    public void queryLocalChatList() {
//        Log.i(TAG, "queryLocalChatList: " + isGetVchatListSuccess);
//        if (isGetVchatListSuccess) {
//            Log.i(TAG, "queryLocalChatList: " + vchatViewReference.get());
//            if (vchatViewReference.get() != null)
//                vchatViewReference.get().showNoralChatList(vChatList);
//            return;
//        }
        dispatcher.dispatchDataBaseAction(DataBaseActions.QUERY_NORMAL_CHAT_LIST);
    }

    public void getLocalChatList() {
        if (isGetVchatListSuccess) {
            Log.i(TAG, "queryLocalChatList: " + vchatViewReference.get());
            if (vchatViewReference.get() != null)
                vchatViewReference.get().showNoralChatList(vChatList);
        }
    }

    private void getNormalChatList() {
        JSONObject body = new JSONObject();
        try {
            body.put("userId", USER_ID);
            Log.i(TAG, "getNormalChatList: " + (USER_ID == null));
            body.put("isPhone", true);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_HUA_TI_THIRTY, createRequestJson(body, null));
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_FEI_HUA_TI_THIRTY, createRequestJson(body, null));
            createHttpRequestJson(body, GET_FEI_HUA_TI_THIRTY, null);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getRecentHuaTiByType", httpCallback, body, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initGroupIds() {
        if (chatGroupIds == null)
            chatGroupIds = new ArrayList<>();
        chatGroupIds.clear();
    }

    public void removeUnReadMsg(VChatBean mBean) {
        if (mBean.unReadMsgNum > 0)
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, mBean.groupId);
        removeUnReadMsg(mBean.groupId);
    }

    public void removeUnReadMsg(String groupId) {

        if (currentVchatBean != null && currentVchatBean.groupId.equals(groupId)) {
            if (currentVchatBean.unReadMsgNum > 0) {
                clearServerUnReadMsg(null, currentVchatBean.groupId);
            }
            currentVchatBean.unReadMsgNum = 0;
        }
        if (vChatUnReadMsg == null)
            return;
        Iterator<UnReadMessageBean> iterator = vChatUnReadMsg.iterator();
        while (iterator.hasNext()) {
            UnReadMessageBean unReadBean = iterator.next();
            if (unReadBean.groupId.equals(groupId))
                iterator.remove();
        }
    }

//    public void dealSingleChat(String groupId) {
//        if (chatGroupIds.contains(groupId)) {
//            getLocalMessageList(groupId, 0);
//        }
////        dispatcher.dispatchUpdateUIEvent(MessageActions.IS_SINGLE_CHAT_EXISTED, isExisted);
//    }

    public void removeChatId(String groupId) {
        chatGroupIds.remove(groupId);
    }

    private JSONArray getSendPresonArray(ArrayList<UserInfoBean> to) {
        JSONArray array = new JSONArray();
        for (UserInfoBean mBean : to) {
            JSONObject user = mBean.getUserJson();
            array.put(user);
        }
        return array;
    }

    public void sendOneMessage(String content, int type, ArrayList<UserInfoBean> to) {
//        Log.i(TAG, "sendOneMessage: " + (currentWindow.toString()));
        if (TextUtils.isEmpty(currentWindow.groupId))
            return;
        MessageBean messageBean = new MessageBean();
        messageBean.content = content;
        Log.i(TAG, "sendOneMessage: " + type);
        messageBean.type = type;
        messageBean.sendMessageUser = PreferencesHelper.getInstance().getCurrentUser();

        messageBean.sendTime = System.currentTimeMillis();
        messageBean.groupId = currentWindow.groupId;
        messageBean.isMessageNew = true;
        messageBean.isSendSuccess = false;

        UUID uuid = UUID.randomUUID();
        messageBean.uuid = uuid.toString();

        setMessageToList(to, messageBean);

        initMsgNeedInfo(messageBean);
        if (sendMessageMap == null)
            sendMessageMap = new ArrayMap<>();
        sendMessageMap.put(messageBean.uuid, messageBean);

        Log.i(TAG, "sendOneMessage: " + (currentVchatBean == null));
        if (currentVchatBean != null) {
            currentVchatBean.lastChatTime = messageBean.sendTime;
            currentVchatBean.msgId = messageBean.uuid;
            currentVchatBean.lastMsg = messageBean.content;
        }

        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);
//        Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SHOW_SEND_MSG_BEFORE_SEND_TO_SERVER, messageBean);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().showMsgBeforeSendToServer(messageBean);
        }
        sendMsg(messageBean);
    }

    private void setTaskMessageCreateTime(MessageBean messageBean) {
        Calendar currentDay = Calendar.getInstance();

        if (currentWindowTaskBean.taskType == TaskBean.CROSS_DAY_TASK_TIME_UNCLEAR) {
            if (currentWindowTaskBean.startTime > currentDay.getTimeInMillis()) {
                messageBean.taskCreateTime = currentWindowTaskBean.startTime;
            } else {
                messageBean.taskCreateTime = currentDay.getTimeInMillis();
            }
        } else if (currentWindowTaskBean.taskType == TaskBean.CROSS_DAY_TASK) {
            if (currentWindowTaskBean.endTime > currentDay.getTimeInMillis() && currentWindowTaskBean.startTime < currentDay.getTimeInMillis()) {
                messageBean.taskCreateTime = currentDay.getTimeInMillis();
            } else if (currentWindowTaskBean.startTime > currentDay.getTimeInMillis()) {
                messageBean.taskCreateTime = currentWindowTaskBean.startTime;
            } else {
                messageBean.taskCreateTime = currentWindowTaskBean.endTime;
            }
        } else {
            messageBean.taskCreateTime = currentWindowTaskBean.startTime;
        }
    }

    public void sendImageMessage(Context context, String filePath, final String destPath, ArrayList<UserInfoBean> to, final Handler viewHandler) {
        final MessageBean messageBean = new MessageBean();
        UUID uuid = UUID.randomUUID();
        messageBean.uuid = uuid.toString();
        messageBean.type = MessageBean.IMAGE_MESSAGE_SEND;
        messageBean.sendMessageUser = PreferencesHelper.getInstance().getCurrentUser();
        messageBean.imagePath = destPath;
        messageBean.isSendSuccess = false;
        messageBean.isMessageNew = true;
        messageBean.groupId = currentWindow.groupId;
        messageBean.sendTime = System.currentTimeMillis();
        messageBean.content = destPath;
        setMessageToList(to, messageBean);

        if (currentVchatBean != null) {
            currentVchatBean.lastMsg = "[图片]";
            currentVchatBean.lastChatTime = messageBean.sendTime;
            currentVchatBean.msgId = messageBean.uuid;
        }

        if (sendMessageMap == null)
            sendMessageMap = new ArrayMap<>();
        sendMessageMap.put(messageBean.uuid, messageBean);

        initMsgNeedInfo(messageBean);

        Log.d(TAG, "sendImageMessage() called with: " + "filePath = [" + filePath + "], destPath = [" + destPath + "]");
//        PictureUtils.compressBitmap(context, filePath, destPath);

        File file = new File(filePath);
        if (file.length() > 500 * 1024) {
            Point point = PictureUtils.getSourceImageWidthHeight(filePath);
            new Compressor.Builder(context.getApplicationContext())
                    .setMaxWidth(point.x)
                    .setMaxHeight(point.y).build()
                    .compressToFileAsObservable(new File(filePath))
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            Log.i(TAG, "call: " + file.getAbsolutePath());
                            if (FileUtil.copyFile(file.getAbsolutePath(), destPath)) {
                                sendImageMsg(viewHandler, messageBean);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
        } else {
            if (!filePath.equals(destPath)) {
                if (FileUtil.copyFile(file.getAbsolutePath(), destPath)) {
                    sendImageMsg(viewHandler, messageBean);
                }
            } else {
                sendImageMsg(viewHandler, messageBean);
            }
        }


//        messageBean.imgBitMap = PictureUtils.getChatMsgShowBitmap(destPath, degree);

//        dispatcher.dispatchUpdateUIEvent(MessageActions.SHOW_IMAGE_MSG_BEFORE_UPLOAD, messageBean);

    }

    private void sendImageMsg(Handler viewHandler, MessageBean messageBean) {
        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                new OkHttpClientManager.Param("user_id", messageBean.sendMessageUser.id),
                new OkHttpClientManager.Param("system_name", "weiliao"),
        };

        if (chatOperationReference.get() != null) {
            chatOperationReference.get().showMsgBeforeSendToServer(messageBean);
        }
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);

        loadFileManager.upLoadFile(AppConfig.UPLOAD_FILE_URI, messageBean.imagePath, params, messageBean.uuid, viewHandler, true, LoadFileManager.UPLOAD_MSG_PICTURE);
    }

    public void sendFileMsg(String filePath, ArrayList<UserInfoBean> to, Handler viewHandler) {

        MessageBean messageBean = new MessageBean();
        UUID uuid = UUID.randomUUID();
        messageBean.uuid = uuid.toString();
        messageBean.type = MessageBean.ATTACHMENT_MESSAGE_SEND;
        messageBean.sendMessageUser = PreferencesHelper.getInstance().getCurrentUser();
        messageBean.isSendSuccess = false;
        messageBean.isMessageNew = true;
        messageBean.groupId = currentWindow.groupId;
        messageBean.sendTime = System.currentTimeMillis();
        setMessageToList(to, messageBean);

        if (currentVchatBean != null) {
            currentVchatBean.lastMsg = "[文件]";
            currentVchatBean.lastChatTime = messageBean.sendTime;
            currentVchatBean.msgId = messageBean.uuid;
        }

        if (sendMessageMap == null)
            sendMessageMap = new ArrayMap<>();
        sendMessageMap.put(messageBean.uuid, messageBean);

        initMsgNeedInfo(messageBean);
        messageBean.attachmentBean = new TaskAttachmentBean(filePath, TextUtils.isEmpty(messageBean.taskId) ? messageBean.groupId : messageBean.taskId);
        messageBean.content = filePath;
//        dispatcher.dispatchUpdateUIEvent(MessageActions.SHOW_IMAGE_MSG_BEFORE_UPLOAD, messageBean);
        if (chatOperationReference.get() != null) {
            chatOperationReference.get().showMsgBeforeSendToServer(messageBean);
        }

        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);

        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                new OkHttpClientManager.Param("user_id", USER_ID),
                new OkHttpClientManager.Param("system_name", "weiliao"),
        };


        loadFileManager.upLoadFile(AppConfig.UPLOAD_FILE_URI, filePath, params, messageBean.uuid, viewHandler, true, LoadFileManager.UPLOAD_MSG_ATTACHMENT);
    }

    private void initMsgNeedInfo(MessageBean messageBean) {
        messageBean.mailId = currentWindow.mailId;
        if (currentWindowTaskBean == null) {
            messageBean.sendType = MessageBean.MESSAGE_CHAT;
        } else {
            messageBean.sendType = MessageBean.MESSAGE_TASK_CHAT;
            messageBean.taskId = currentWindowTaskBean.taskId;
            messageBean.taskTitle = currentWindowTaskBean.taskContent;

            setTaskMessageCreateTime(messageBean);
        }
    }

    public void upLoadPictureSuccess(String uuid, String response) {
        MessageBean mBean = sendMessageMap.get(uuid);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert jsonObject != null;
        String docid = jsonObject.optString("docid");
        mBean.content = XmlHelper.genertImgXmlMsg(jsonObject.optString("docid"), jsonObject.optString("download_url"));
        File file = FileUtil.renameFile(mBean.imagePath, "img_index_" + docid);
        if (file != null)
            mBean.imagePath = file.getAbsolutePath();
        dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 4, mBean);
        sendMsg(mBean);
    }

    private void sendMsg(MessageBean mBean) {
        if (mBean.sendType == MessageBean.MESSAGE_TASK_CHAT) {
            sendTaskChatMsg(mBean);
        } else if (mBean.sendType == MessageBean.MESSAGE_CHAT) {
            sendChatMsg(mBean);
        }
    }

    public void reSendOneMessage(MessageBean resendMessage, Handler viewHandler) {
        resendMessage.isMessageNew = true;
        resendMessage.isSendSuccess = false;

        if (resendMessage.toArray == null)
            try {
                resendMessage.toArray = new JSONArray(resendMessage.to);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        initMsgNeedInfo(resendMessage);
        resendMessage.sendType = currentWindowTaskBean == null ? MessageBean.MESSAGE_CHAT : MessageBean.MESSAGE_TASK_CHAT;
        if (sendMessageMap == null)
            sendMessageMap = new ArrayMap<>();
        sendMessageMap.put(resendMessage.uuid, resendMessage);
        if (resendMessage.type == MessageBean.IMAGE_MESSAGE_SEND) {
            if (resendMessage.imgSrc == null) {//上传未成功
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("user_id", resendMessage.sendMessageUser.id),
                        new OkHttpClientManager.Param("system_name", "weiliao"),
                };
                loadFileManager.upLoadFile(AppConfig.UPLOAD_FILE_URI, resendMessage.imagePath,
                        params, resendMessage.uuid, viewHandler, true, LoadFileManager.UPLOAD_MSG_PICTURE);
            } else {//上传成功
                sendMsg(resendMessage);
            }
        } else if (resendMessage.isAttachmentMsg()) {
            if (resendMessage.attachmentBean != null) {
                if (resendMessage.attachmentBean.docId == null) {
                    OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                            new OkHttpClientManager.Param("user_id", resendMessage.sendMessageUser.id),
                            new OkHttpClientManager.Param("system_name", "weiliao"),
                    };
                    loadFileManager.upLoadFile(AppConfig.UPLOAD_FILE_URI, resendMessage.imagePath,
                            params, resendMessage.uuid, viewHandler, true, LoadFileManager.UPLOAD_MSG_PICTURE);
                } else {
                    sendMsg(resendMessage);
                }
            }
        } else if (resendMessage.type == MessageBean.TEXT_MESSAGE_SEND) {
            sendMsg(resendMessage);
        }
    }

    public void upLoadPictureFailue(String uuid) {
        MessageBean mBean = sendMessageMap.get(uuid);
        mBean.isSendSuccess = false;
        mBean.isMessageNew = false;
    }

    public void upLoadFileSuccess(String uuid, String response) {
        MessageBean messageBean = sendMessageMap.get(uuid);

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert jsonObject != null;
        messageBean.attachmentBean.docId = jsonObject.optString("docid"); // docid
        messageBean.attachmentBean.attachPath = "doc?doc_id=" + messageBean.attachmentBean.docId + "&system_name=weiliao";

        messageBean.content = XmlHelper.genertFileXmlMsg(messageBean);

        dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 4, messageBean);

        sendMsg(messageBean);
    }


    public void upFileFailue(String uuid) {
        MessageBean mBean = sendMessageMap.get(uuid);
        mBean.isSendSuccess = false;
        mBean.isMessageNew = false;
    }

    public void setVchatViewReference(VChatViewOperation operation) {
        this.vchatViewReference = new WeakReference<>(operation);
    }

    public void setChatViewRefrence(ChatViewOperation operation) {
        this.chatOperationReference = new WeakReference<>(operation);
    }

    public void setAddressBookRefrence(AddressBookViewOperation operation) {
        this.addBookViewOperation = new WeakReference<>(operation);
    }


    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            String requestId = request.request().header("requestId");
            if (requestId != null && requestId.startsWith("initChatWindowInfo")) {
//                getInfoList.remove(requestId.substring(18));
                if (vchatViewReference.get() != null) {
                    vchatViewReference.get().getNormalChatWindowResult(null, requestId.substring(18));
                }
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    JSONObject cleentId = new JSONObject(res.optString("ClientId"));
                    int type = cleentId.optInt("type");
                    String clientId = cleentId.optString("ClientId", "");
                    if (!TextUtils.isEmpty(clientId)) {
                        res.put("ClientId", clientId);
                    } else {
                        res.remove("ClientId");
                    }
                    Log.i(TAG, "onResponse: type:" + type + res.toString());
                    if (!res.optBoolean("type")) {
                        return;
                    }

                    switch (type) {
                        case GET_FEI_HUA_TI_THIRTY:
                            praseTopicChat(res, avatars);
                            break;
                        case GET_GROUP_CHAT_HISTORY:
                            parseMessageHistoryList(res);
                            break;
                        case GET_SMALL_BY_ID:
                            parseSmallMail(res);
                            break;
                        case RECALL_ONE_CHAT_MSG:
                            praseReCallOneMsg(res);
                            break;
                        case DELETE_ONE_CHAT_MSG:
                            parseDeleteOneMsg(res);
                            break;
                        case DELETE_GROUP_CHAT:
                            parseDeleteGroupChat(res);
                            break;
                        case SET_CUSTOM_PROPERTY:
                            parseSetCustomProperty(res);
                            break;
                        case GET_NOTES_BY_GROUP_ID:
                            parseGetNotesByGroupId(res);
                            break;
                        case SET_NOTES_BY_GROUP_ID:
                            parseSetNotesByGroupId(res);
                            break;
                        case GET_CHAT_WINDOW_INFO:
                            praseChatWindowInfo(res);
                            break;
                        case UPDATE_GROUP_NAME:
                            parseUpdateGroupName(res);
                            break;
                        case SET_MSG_READ:
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void clean() {
        unRegister();
    }

    public void openWindow(ChatWindowInfoBean windowInfoBean, TaskBean taskBean) {
        currentWindow = windowInfoBean;
        if (taskBean != null) {
            currentWindowTaskBean = taskBean;
        }
    }

    public void closeWindow() {
        currentWindow = null;
        currentWindowTaskBean = null;
        currentVchatBean = null;
    }

    public VChatBean setCurrentVchatBean(VChatBean vchatBean) {
        if (vchatBean != null) {
            if (chatGroupIds.contains(vchatBean.groupId)) {
                this.currentVchatBean = getVchatBeanByGroupId(vchatBean.groupId);
            }
            return currentVchatBean;
        }
        this.currentVchatBean = null;
        return null;
    }

    /**
     * 是否从通讯录界面创建
     *
     * @param chatWindowInfoBean
     * @param isAddressBook
     */
    void createNewChat(ChatWindowInfoBean chatWindowInfoBean, boolean isAddressBook) {
        if (chatWindowInfoBean == null) {
            if (chatOperationReference.get() != null && !isAddressBook) {
                chatOperationReference.get().onCreateNewChat(null, null);
            }
            return;
        }
        VChatBean vChatBean = new VChatBean();
        vChatBean.groupId = chatWindowInfoBean.groupId;
        vChatBean.memberList = chatWindowInfoBean.allMemberList;
        String memberNames = vChatBean.setAvatars(avatars);

        if (TextUtils.isEmpty(chatWindowInfoBean.taskTitle))
            vChatBean.topic = memberNames;
        else
            vChatBean.topic = chatWindowInfoBean.taskTitle;
        Log.i(TAG, "createNewChat: " + memberNames + "+++++" + chatWindowInfoBean.taskTitle);
        // 判断是否为单聊
        if (!chatWindowInfoBean.isSingle) {
            vChatBean.isGroup = 1;
        }
        vChatBean.members = chatWindowInfoBean.memberString;
        vChatBean.lastChatTime = System.currentTimeMillis();
        chatWindowInfoBean.memberNames = memberNames;
        if (!chatGroupIds.contains(chatWindowInfoBean.groupId)) {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, chatWindowInfoBean);
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_SUMMARY, vChatBean, false, false);
            vChatList.add(1, vChatBean);
            chatGroupIds.add(chatWindowInfoBean.groupId);
        }

        if (isAddressBook) {
            if (addBookViewOperation.get() != null) {
                addBookViewOperation.get().createChatSuccess(vChatBean, chatWindowInfoBean);
            }
            return;
        }

        this.currentVchatBean = vChatBean;
        this.currentWindow = chatWindowInfoBean;

        if (chatOperationReference.get() != null) {
            chatOperationReference.get().onCreateNewChat(chatWindowInfoBean, vChatBean);
        }
    }


    public void sortVchatList() {
        Collections.sort(vChatList);
    }

    public VChatBean isSingleChatExist(String userId) {
        UserInfoBean user = new UserInfoBean();
        user.id = userId;
        for (VChatBean vChatBean : vChatList) {
            Log.i(TAG, "isSingleChatExist: " + vChatBean.isGroup);
            if (vChatBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId()))
                continue;
            if (vChatBean.isGroup == 0) {
                if (vChatBean.memberList.contains(user))
                    return vChatBean;
            }
        }
        return null;
    }
}
