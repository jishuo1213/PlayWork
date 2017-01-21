package com.inspur.playwork.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.inspur.playwork.EventCallback;
import com.inspur.playwork.EventManager;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.login.LoginActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.actions.network.NetAction;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.broadcastreciver.NotifyMsgReciver;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.SocketEvent;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.EmojiHandler;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.utils.loadfile.ProgressRequestListener;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;
//import com.squareup.okhttp.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

import okhttp3.Callback;

public class PlayWorkServiceNew extends Service {

    private static final String TAG = "PlayWorkServiceNewFan";
    public static final String IS_RECREATE = "is_recreate";

    private static final String SOCKET_LOG_OUT = "-9999";
    private static final String SOCKET_CONNECT = "-9998";
    private static final String SET_APP_RESUME = "-9997";
    private static final String SET_APP_BACK = "-9996";

    private static final int DISPATCH_NET_RESPONSE = 0x02;

    private static final int DISPATCH_NEW_MSG = 0x03;

    private static final int LOGIN_TIME_LINE = 0x05;

    private static final int CONNECT_FROM_SERVER = 0x07;

    private static final int DISCONNECT_FROM_SERVER = 0x08;

    EventManager pushService;

    private int netWorkState = -1;

    private Binder binder;

    private Dispatcher dispatcher;

//    private ArrayMap<String, RequestBean> netRequestMap;

    private ServiceHandler handler;

    private ChatWindowId chatWindowId;

    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;

    public PlayWorkServiceNew() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(64330, new Notification());
        } else {
            startForeground(64330, new Notification());
            startService(new Intent(this, InnerService.class));
        }
        initData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Intent bindIntent = new Intent(this, PushService.class);
            bindIntent.setAction("android.intent.action.push");
            bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);
            return super.onStartCommand(intent, flags, startId);
        }
        if (intent.getBooleanExtra("fromApp", false)) {
            Intent bindIntent = new Intent(this, PushService.class);
            bindIntent.setAction("android.intent.action.push");
            bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);
        } else if (intent.getBooleanExtra("resume", false)) {
            try {
                Log.i(TAG, "onStartCommand: resume");
                pushService.sendSocketRequest(SOCKET_CONNECT, "resume");
            } catch (RemoteException e) {
                e.printStackTrace();
                Intent bindIntent = new Intent(this, PushService.class);
                bindIntent.setAction("android.intent.action.push");
                bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);
            }
            dispatcher = Dispatcher.getInstance();
            dispatcher.register(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setAppVisable(boolean isVisable) {
        if (isVisable) {
            try {
                pushService.sendSocketRequest(SET_APP_RESUME, "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                pushService.sendSocketRequest(SET_APP_BACK, "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public class Binder extends android.os.Binder {
        public Service getService() {
            return PlayWorkServiceNew.this;
        }
    }

    private void initData() {
        dispatcher = Dispatcher.getInstance();
        binder = new Binder();
        Log.i(TAG, "initData: new binder");
        handler = new ServiceHandler(new WeakReference<>(this));
        dispatcher.register(this);
        chatWindowId = new ChatWindowId();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
    }


    private static class ChatWindowId {
        public String taskId;
        public String groupId;
    }

    private static class ServiceHandler extends Handler {

        private WeakReference<PlayWorkServiceNew> reference;

        public ServiceHandler(WeakReference<PlayWorkServiceNew> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    UItoolKit.showToastShort(reference.get(), "当前未连接到网络");
                    break;
                case DISPATCH_NET_RESPONSE:
                    if (reference.get() != null) {
                        reference.get().dispatchNetEvents((String) msg.obj);
                    }
                    break;
                case DISPATCH_NEW_MSG:
                    if (reference.get() != null) {
                        SocketEvent event = (SocketEvent) msg.obj;
                        reference.get().dispatchNewMsgEvent(event);
                    }
                    break;
                case 0x04:
                    if (reference.get() != null) {
                        reference.get().setEventProcessed((String) msg.obj);
                    }
                    break;
                case LOGIN_TIME_LINE:
                    if (reference.get() != null) {
                        reference.get().loginTimeLineSuccess((String) msg.obj);
                    }
                    break;
                case 0x06:
                    if (reference.get() != null) {
                        reference.get().getUnProcessedEvent();
                    }
                    break;
                case DISCONNECT_FROM_SERVER:
                    if (reference.get() != null) {
                        reference.get().sendDisconnected();
                    }
                    break;
            }
        }
    }

    private void sendDisconnected() {
        Dispatcher.getInstance().dispatchUpdateUIEvent(CommonActions.DISCONNECT_FROM_SERVER);
    }

    private void getUnProcessedEvent() {
        try {
            pushService.getUnProcessEvent();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void reconnectToServer() {
        try {
            pushService.sendSocketRequest(SOCKET_CONNECT, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void loginTimeLineSuccess(String token) {
        OkHttpClientManager.getInstance().setToken(token);
        dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_TIMELINE_SUCCESS);
        dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_GET_UNREAD_MESSAGE);
        dispatcher.dispatchUpdateUIEvent(CommonActions.CONNECT_SERVER);
    }

    private void setEventProcessed(String fbId) {
        try {
            pushService.setEventProcessed(fbId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pushService = EventManager.Stub.asInterface(service);
            try {
                Log.i(TAG, "onServiceConnected: bind to push service" + (callback == null));
                pushService.setClientProcessId(Process.myPid());
                pushService.registerCallBack(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected: ");
//                pushService.unRegisterCallBack(callback);
        }
    };

    private EventCallback callback = new EventCallback.Stub() {
        @Override
        public void notifyEvent(SocketEvent socketEvent) throws RemoteException {
            Log.i(TAG, "notifyEvent: " + socketEvent.toString());
            switch (socketEvent.eventCode) {
                case 0:
                    netWorkState = 0;
                    break;
                case -1:
                    netWorkState = -1;
                    handler.sendMessage(handler.obtainMessage(DISCONNECT_FROM_SERVER));
                    break;
                case 2:
                    netWorkState = 2;
                    handler.sendMessage(handler.obtainMessage(LOGIN_TIME_LINE, socketEvent.eventInfo));
                    break;
                case 1:
                    handler.sendMessage(handler.obtainMessage(DISPATCH_NET_RESPONSE, socketEvent.eventInfo));
                    break;
                case 0xFF:
                    handler.sendMessage(handler.obtainMessage(DISPATCH_NEW_MSG, socketEvent));
                    break;
            }
        }
    };

//    private EventCallback callback = new EventCallback.Stub() {
//        @Override
//        public void notifyEvent(SocketEvent socketEvent) throws RemoteException {
//            Log.i(TAG, "notifyEvent: " + socketEvent.toString());
//            switch (socketEvent.eventCode) {
//                case 0:
//                    netWorkState = 0;
//                    break;
//                case -1:
//                    netWorkState = -1;
//                    break;
//                case 2:
//                    netWorkState = 2;
//                    handler.sendMessage(handler.obtainMessage(0x05));
//                    break;
//                case 1:
//                    handler.sendMessage(handler.obtainMessage(0x02, socketEvent.eventInfo));
//                    break;
//                default:
//                    handler.sendMessage(handler.obtainMessage(0x03, socketEvent));
//                    break;
//            }
//        }
//
//        @Override
//        public IBinder asBinder() {
//            return null;
//        }
//    };

    private void dispatchNewMsgEvent(SocketEvent event) {
        JSONObject info = null;
        try {
            info = new JSONObject(event.eventInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (info != null)
            dispatchNewMsgEvent(info.optInt("type"), info, event.fbId);
    }

    private void dispatchNewMsgEvent(int type, JSONObject msg, String fbId) {
        Log.d(TAG, "dispatchNewMsgEvent() called with: " + "type = [" + type + "], msg = [" + msg.toString() + "], fbId = [" + fbId + "]");

        if (type == MessageActions.MESSAGE_SORT_TASK) {
            dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_SORT_TASK_NUM, msg);
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_DELETE_GROUP) {
            String groupId = msg.optString("groupId");
            if (isChatWindowShow(groupId)) {
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.MESSAGE_DELETE_GROUP, groupId);
            } else {
                cancelNotification(groupId);
            }
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_MESSAGE_BY_GROUPID, groupId);
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.DELETE_VCHAT_ONE_CHAT, true, groupId);
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {
            String groupId = msg.optString("groupId");
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, groupId, msg.optString("mid"));

            if (isChatWindowShow(groupId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_NORMAL_CHAT_MSG, msg);
            } else {
                if (msg.has("taskId")) {
                    dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_TASK_CHAT_MSG, msg);
                } else {
                    dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_NORMAL_CHAT_MSG, msg);
                }
            }
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_TO_READ) {
            String groupId = msg.optString("groupId");
            if (isChatWindowShow(groupId)) {
                clientSocketEventCallBack.onEventProcessed(fbId);
                return;
            } else {
                cancelNotification(groupId);
            }
            if (msg.has("taskId")) {
                dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
                dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_SET_MESSAGE_TO_READ, msg.optString("taskId"), groupId);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.SET_UNREAD_MSG_TO_READ, groupId);
                dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
            }
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_ADD_MEMBER) {
            String groupId = msg.optString("groupId");
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg);
            if (isChatWindowShow(groupId)) {
                dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg);
            }
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_REFRESH_MEMBERS) {
            if (msg.has("groupId")) {
                String groupId = msg.optString("groupId");
                dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg, groupId);
                if (isChatWindowShow(groupId)) {
                    dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg, groupId);
                }
            } else if (msg.has("taskId")) {
                String taskId = msg.optString("taskId");
                dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg, taskId);
                if (isTaskChatWindowShow(taskId)) {
                    dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg, taskId);
                }
            }
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == MessageActions.MESSAGE_RENAME_CHAT) {
            String groupId = msg.optString("groupId");
            String subject = msg.optString("subject");
            dispatcher.dispatchDataBaseAction(DataBaseActions.RENAME_CHAT_SUBJECT, groupId, subject);
            dispatcher.dispatchStoreActionEvent(MessageActions.RENAME_CHAT_SUBJECT, groupId, subject);
//            if (isChatWindowShow(groupId)) {
//            }
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == 1005) {
//            TimeLineStoresNew.getInstance().getMailList(-1);
            dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_ADD_NEW_TASK, msg);
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == 3) {
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == 1008) {
            String taskId = msg.optString("taskId");
            String subject = msg.optString("subject");
            dispatcher.dispatchDataBaseAction(DataBaseActions.RENAME_CHAT_SUBJECT, taskId, subject);
            if (isTaskChatWindowShow(taskId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RENAME_CHAT_SUBJECT, taskId, subject, "33");
            }
            dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_UPDATE_TASK_NAME, taskId, subject);
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == 1015) {
            String taskId = msg.optString("taskId");
            Log.i(TAG, "dispatchNewMsgEvent: deleteTask");

            if (isTaskChatWindowShow(taskId)) {
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.MESSAGE_DELETE_GROUP, taskId);
            }
            dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_DELETE_TASK, taskId);
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type == 1006) {
            MessageStores.getInstance().getServerGiveWindowInfo(msg.optString("groupId"));
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        } else if (type > 4 && type < 1000) {
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        }
        if (msg.optJSONObject("from").optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id) && msg.optBoolean("notShowMe")) {
            clientSocketEventCallBack.onEventProcessed(fbId);
            return;
        }

        String groupId = msg.optString("groupId");

        String content = msg.optString("content");
        try {
            if (msg.optBoolean("isEncrypt") || msg.optInt("isMailMsg") > 0)
                msg.put("content", EncryptUtil.aesDecrypt(content));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg.toString());


        String taskId = msg.optString("taskId");
        Log.i(TAG, "dispatchNewMsgEvent: " + taskId);
        if (isChatWindowShow(groupId)) { //收到消息的是当前窗口
            if (type == 5) {
                clientSocketEventCallBack.onEventProcessed(fbId);
                return;
            }
            if (TextUtils.isEmpty(taskId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_NORMAL_CHAT_MSG, msg);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_TASK_CHAT_MSG, msg);
            }
        } else { // 收到消息但是当前不再聊天窗口
            if (type == 5) {
                clientSocketEventCallBack.onEventProcessed(fbId);
                return;
            }
            if (TextUtils.isEmpty(taskId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_NORMAL_CHAT_MSG, msg);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_TASK_CHAT_MSG, msg);
            }
            if (msg.optJSONObject("from").optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id) ||
                    (msg.optInt("isRead") == 1) || (type == MessageActions.MESSAGE_SYSTEM_TIP)) {

            } else {
                String notifyContent = msg.optString("content");
                if (msg.optBoolean("hasEmotion")) {
                    notifyContent = EmojiHandler.getInstance().replaceEmoji(notifyContent);
                }
                sendNotification(msg.optString("title"), msg.optJSONObject("from").optString("name"), notifyContent, msg.optString("groupId"));

            }
        }
        clientSocketEventCallBack.onEventProcessed(fbId);
    }

    private boolean isChatWindowShow(String groupId) {
        return !TextUtils.isEmpty(chatWindowId.groupId) && groupId.equals(chatWindowId.groupId);
    }

    private boolean isTaskChatWindowShow(String taskId) {
        return !TextUtils.isEmpty(chatWindowId.taskId) && taskId.equals(chatWindowId.taskId);
    }

    private UserInfoBean currentUser;

    public void setCurrentUser(UserInfoBean userInfoBean) {
        this.currentUser = userInfoBean;
        try {
            pushService.setCurrentUser(currentUser);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void dispatchNetEvents(String result) {
        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert json != null;
        String type = json.optString("type");
        Log.i(TAG, "Rsponse Data: " + json.toString());
        dispatcher.dispatchStoreActionEvent(CommonActions.REVICE_TIMELINE_DATA_FROM_SERVER, type, json);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        dispatcher.unRegister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(NetAction netActions) {
        SparseArray<Object> data = netActions.getActiontData();
        switch (netActions.getActionType()) {
            case CommonActions.GET_TIMELINE_DATA_FROM_SERVER:
                String type = (String) data.get(0);
                try {
                    pushService.sendSocketRequest(type, data.get(1).toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case CommonActions.GET_DATA_BY_HTTP_GET:
                sendHttpGetRequest((String) data.get(0), (Callback) data.get(1));
                break;
            case CommonActions.GET_DATA_BY_HTTP_POST:
                sendHttpPostRequest((String) data.get(0), (String) data.get(1), (Callback) data.get(2));
                break;
            case CommonActions.UPLOAD_FILE_BY_HTTP_POST:
                upLoadFileByHttpPost((File) data.get(0), (OkHttpClientManager.Param[]) data.get(1), (Callback) data.get(2), data.get(3));
                break;
            case CommonActions.DOWNLOAD_FILE_BY_HTTP:
                downLoadFileByHttp((String) data.get(0), (String) data.get(1), (Callback) data.get(2), data.get(3));
                break;
            case CommonActions.SEND_NOTIFICATION:
                MessageBean messageBean = (MessageBean) data.get(1);
                if (messageBean.isEmojiMessage()) {
                    sendNotification((String) data.get(0), messageBean.sendMessageUser.name, EmojiHandler.getInstance().replaceEmoji(messageBean.content), messageBean.groupId);
                } else {
                    sendNotification((String) data.get(0), messageBean.sendMessageUser.name, messageBean.content, messageBean.groupId);
                }
//                sendNotification((String) data.get(0), (String) data.get(1), (String) data.get(2), (String) data.get(3));
                break;
            case CommonActions.CANCEL_ALL_NOTIFICATION:
                cacelAllNotification();
                break;
        }
    }

    private void downLoadFileByHttp(String url, String destPath, Callback callback, Object listener) {
        Log.d(TAG, "downLoadFileByHttp() called with: " + "url = [" + url + "], destPath = [" + destPath + "]");
        if (listener != null)
            OkHttpClientManager.getInstance().downLoadFile(url, destPath, callback, (ProgressResponseListener) listener);
        else
            OkHttpClientManager.getInstance().downLoadFile(url, destPath, callback, null);
    }

    private void upLoadFileByHttpPost(File file, OkHttpClientManager.Param[] params, Callback callback, Object listener) {
        if (listener != null)
            OkHttpClientManager.getInstance().postAsyn(AppConfig.UPLOAD_FILE_URI, "uploadfile", file, params, callback, (ProgressRequestListener) listener);
        else
            OkHttpClientManager.getInstance().postAsyn(AppConfig.UPLOAD_FILE_URI, "uploadfile", file, params, callback, null);
    }


    private void sendHttpGetRequest(String url, Callback callback) {
        OkHttpClientManager.getInstance().getAsyn(url, callback);
    }

    private void sendHttpPostRequest(String url, String params, Callback callback) {
        OkHttpClientManager.getInstance().post(url, null, callback, "");
    }

//
//    private static class RequestBean {
//
//        public RequestBean(String requestType, TimeOutRunnable runnable) {
//            this.requestType = requestType;
//            this.runnable = runnable;
//        }
//
//        public String requestType;
//        public TimeOutRunnable runnable;
//
//    }

//    private class TimeOutRunnable implements Runnable {
//
//        private String uuid;
//
//        public TimeOutRunnable(String uuid) {
//            this.uuid = uuid;
//        }
//
//        @Override
//        public void run() {
//            RequestBean requestType = netRequestMap.get(uuid);
//            dispatchLoadTimeOutEvent(requestType, uuid);
//        }
//    }

//    private void dispatchLoadTimeOutEvent(RequestBean requestBean, String uuid) {
//        netRequestMap.remove(uuid);
//        dispatcher.dispatchStoreActionEvent(CommonActions.GET_TIMELINE_DATA_FROM_SERVER_TIME_OUT, requestBean.requestType, uuid);
//    }

    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(64330, new Notification());
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

    public int getConnectedState() {
        return netWorkState;
    }

    int notificationId = 0;

    private void sendNotification(String chatTitle, String title, String content, String groupId) {
        if (content.contains("weiliao_images") && content.contains("<img")) {
            content = "[图片]";
        } else if (content.contains("attachmentDownload") && content.startsWith("<div>")) {
            content = "[文件]";
        }
        ArrayMap<String, Integer> notificationMap = ((PlayWorkApplication) getApplication()).getNotificationMap();
        Notification notification = buildNotification(chatTitle, groupId, title, content);
        Log.i(TAG, "sendNotification: " + chatTitle + title + content + groupId);
        if (notificationMap.containsKey(groupId)) {//已经有了一个消息提醒,更新当前的提醒
            int id = notificationMap.get(groupId);
            notificationManager.notify(id, notification);
        } else {//没有消息提醒，新生成一个
            notificationManager.notify(notificationId, notification);
            notificationMap.put(groupId, notificationId);
            notificationId++;
        }
    }

    public void setChatWindowId(String groupId, String taskId) {
        Log.i(TAG, "groupId_____ " + groupId);
        chatWindowId.groupId = groupId;
        chatWindowId.taskId = taskId;
    }

    private void cancelNotification(String groupId) {
        ArrayMap<String, Integer> notificationMap = ((PlayWorkApplication) getApplication()).getNotificationMap();
        if (notificationMap.containsKey(groupId)) {
            int id = notificationMap.get(groupId);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
        }
    }

    private void cacelAllNotification() {
        ArrayMap<String, Integer> notificationMap = ((PlayWorkApplication) getApplication()).getNotificationMap();
        for (String key : notificationMap.keySet()) {
            int id = notificationMap.get(key);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
        }
    }


    private Notification buildNotification(String chatTitle, String groupId, String title, String content) {
        if (TextUtils.isEmpty(chatTitle)) {
            builder.setContentTitle(title);
            builder.setContentText(content);
        } else {
            builder.setContentTitle(chatTitle);
            builder.setContentText(title + ":" + content);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
//        builder.setNumber(1);
        Intent intent = new Intent(NotifyMsgReciver.NOTIFY_MSG);
        intent.putExtra(NotifyMsgReciver.MSG_GROUP_ID, groupId);
        builder.setContentIntent(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        return builder.build();
    }

    private ClientSocketEventCallBack clientSocketEventCallBack = new ClientSocketEventCallBack() {
        @Override
        public void onEventProcessed(String fbId) {
            handler.sendMessage(handler.obtainMessage(0x04, fbId));
        }
    };

    private interface ClientSocketEventCallBack {
        void onEventProcessed(String fbId);
    }


    public void logout() {
        cacelAllNotification();
        try {
            pushService.sendSocketRequest(SOCKET_LOG_OUT, "");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        netWorkState = -1;

        ((PlayWorkApplication) getApplication()).logout();
    }
}
