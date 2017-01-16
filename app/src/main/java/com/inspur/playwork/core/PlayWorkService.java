package com.inspur.playwork.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.inspur.playwork.R;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.common.ConnectType;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.actions.network.NetAction;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.broadcastreciver.NotifyMsgReciver;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.NetWorkUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.utils.loadfile.ProgressRequestListener;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import okhttp3.Callback;

public class PlayWorkService extends Service implements Emitter.Listener {

    private static final String TAG = "PlayWorkServiceFan";

    public static final String IS_RECREATE = "is_recreate";

    private static final String NEW_GROUP_MSG = "newGroupMsg";

    private static final String NEW_TASK = "newTask";

    private static final int RECONNECT_TIME_UINT = 1000;

    private Socket socket;

    private Binder binder;

    private Dispatcher dispatcher;

    private ArrayMap<String, RequestBean> netRequestMap;

    private ServiceHandler handler;

    private boolean isProcessKilled = false;

    private ChatWindowId chatWindowId;

    private volatile int connectedAttempt;

    private boolean isAppVisable; //当前应用是否可见

    private int connectType;

    private int disconnectType;

    //private boolean isServiceCreate;
    private IO.Options options = new IO.Options();

    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;


    public PlayWorkService() {
        options.reconnection = false;
        options.timeout = 8000;
        options.forceNew = true;
        //alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private boolean needLogin = false;

    private static class ServiceHandler extends Handler {

        private WeakReference<PlayWorkService> reference;

        public ServiceHandler(WeakReference<PlayWorkService> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    UItoolKit.showToastShort(reference.get(), "当前未连接到网络");
                    break;
                case 0x02:
                    reference.get().connectedToTimeLineServer(ConnectType.CONNECT_FROM_RECONNECT);
                    break;
                case 0x03:
                    reference.get().connectedToTimeLineServer(ConnectType.CONNECT_FROM_LOGIN);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        保命处理机制。
        当API<18时，前台服务传入一个空的notification，通知栏不会显示图标。
        当API>17时，需要在前台服务中启用一个InnerService，然后stop掉。
         */
//        if (Build.VERSION.SDK_INT > 17) {
//            startService(new Intent(this, InnerService.class));
//        }
//        startForeground(1, new Notification());

        initData();
        //connectedToTimeLineServer();
        Log.i(TAG, "service onCreate");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "service start  intent == null" + (intent == null) + intent);
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (intent == null) {
            isProcessKilled = true;
            connectedToTimeLineServer(ConnectType.CONNECT_FROM_SERVICE_START);
        }
        return START_NOT_STICKY;
    }


    private void initData() {
        dispatcher = Dispatcher.getInstance();
        binder = new Binder();
        netRequestMap = new ArrayMap<>();
        handler = new ServiceHandler(new WeakReference<>(this));
        dispatcher.register(this);
        chatWindowId = new ChatWindowId();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "service destory");
        super.onDestroy();
        clear();
    }

    private void clear() {
        disconnectFromTimeLineServer(ConnectType.DISCONNECT_WHEN_SERVICE_DESTORY);
        dispatcher.unRegister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind" + intent.hasExtra(IS_RECREATE));
        if (intent.getBooleanExtra(IS_RECREATE, false)) {
            isProcessKilled = true;
            connectedToTimeLineServer(ConnectType.CONNECT_FROM_RECONNECT);
        }
        return binder;
    }

    public class Binder extends android.os.Binder {
        public Service getService() {
            return PlayWorkService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return true;
    }


    public boolean isConnectedToServer() {
        return socket != null && socket.connected();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind: ");
        if (binder == null) {
            binder = new Binder();
        }
    }

    public void connectedToTimeLineServer(int type) {
        Log.i(TAG, "send connecttoserver   type" + type);
        connectType = type;
        if (socket != null) {
            Log.i(TAG, "socket != null" + socket.connected());
            socket.disconnect();
            socket.off();
            socket = null;
        }

        switch (type) {
            case ConnectType.CONNECT_FROM_SERVICE_START:
                needLogin = false;
                break;
            default:
                needLogin = true;
                break;
        }
        /*int network = PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.NET_WORK_TYPE);
        String serverUrl = network == ConnectType.LOCAL_NET ? AppConfig.LOCAL_NET_URI : AppConfig.WIDE_NET_URI;*/
        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
        try {
            socket = IO.socket(AppConfig.LOCAL_NET_URI, options);
            socket.on(Socket.EVENT_CONNECT, onConnectListener);
            socket.on(Socket.EVENT_DISCONNECT, onDisConnectListener);
            socket.on(Socket.EVENT_RECONNECT, onReConnectListener);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeOutListener);
            socket.on(Socket.EVENT_ERROR, onErrorListener);
            socket.on(NEW_GROUP_MSG, newGroupMsgListener);
            socket.on(NEW_TASK, newTaskListener);
            socket.on(Socket.EVENT_PING, onPingListener);
            socket.on(Socket.EVENT_PONG, onPongListener);
            socket.on(Socket.EVENT_MESSAGE, this);
            socket.connect();
            Log.i(TAG, "send connect");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromTimeLineServer(int disConnectType) {
        this.disconnectType = disConnectType;
        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
        if (socket != null) {
            Log.i(TAG, "disconnected socket");
            socket.off();
            socket.disconnect();
            socket.close();
            socket = null;
        }
    }


    /**
     * 所有需要从服务器获取数据时都要走这里
     */
    @SuppressWarnings("unused")
    public void onEvent(NetAction netActions) {
        SparseArray<Object> data = netActions.getActiontData();
        switch (netActions.getActionType()) {
            case CommonActions.GET_TIMELINE_DATA_FROM_SERVER:
                String type = (String) data.get(0);
                if (type.equals("getMailList")) {
                    String uuid = (String) data.get(2);
                    RequestBean mBean = new RequestBean(type, new TimeOutRunnable(uuid));
                    netRequestMap.put(uuid, mBean);
                    handler.postDelayed(mBean.runnable, 5000);
                }
                if (socket.connected())
                    sendSocketRequest(type, data.get(1));
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
            case NetWorkActions.SEND_CONNECT_TO_TIMELINE_SERVER:
                connectedToTimeLineServer((Integer) data.get(0));
                break;
            case NetWorkActions.NET_WORK_STATE_CHANGE:
                if (isNetWorkAvailable()) {
                    connectedAttempt = 0;
                    connectedToTimeLineServer(ConnectType.CONNECT_FROM_NETWORK_CHANGE);
                } else {
                    disconnectFromTimeLineServer(ConnectType.DISCONNECT_WHEN_NET_TYPE_CHANGE);
                }
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
        OkHttpClientManager.getInstance().post(url, null, callback,"");
    }


    @Override
    public void call(Object... args) {
//        Log.i(TAG, "dispatchNetEvents: before convert" + args[0].toString());
        dispatchNetEvents(args[0].toString());
    }

    private void sendSocketRequest(String type, Object data) {
//            JSONObject jsonObject = new JSONObject(CharsetCoder.getInstance().conventRequestCharSet(data.toString()));
        Log.i(TAG, "Request Method: " + type);
        Log.i(TAG, "Request Data: " + data.toString());
        socket.emit(type, data);
    }

    public void getUnReadMessage() {
        JSONObject body = new JSONObject();
        try {
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("isPhone", true);
            sendSocketRequest("getMcUnReadMsg", createRequestJson(body));
        } catch (JSONException e) {
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
        if (type.equals("login")) {
            PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.CONNECTID, json.optString("ConnectionId"));
            if (json.optString("LoginStatus").equals("success")) {
                PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, true);
                Log.i(TAG, "login success" + connectType);
                if (connectType == ConnectType.CONNECT_FROM_RECONNECT || connectType == ConnectType.CONNECT_FROM_NETWORK_CHANGE) {
                    getUnReadMessage();
                }
            }
        } else if (type.equals("getMailList")) {
            String uuid = json.optString("ClientId");
            if (netRequestMap.containsKey(uuid)) {
                RequestBean mBean = netRequestMap.get(uuid);
                handler.removeCallbacks(mBean.runnable);
            }
        }
        Log.i(TAG, "Rsponse Data: " + json.toString());
        dispatcher.dispatchStoreActionEvent(CommonActions.REVICE_TIMELINE_DATA_FROM_SERVER, type, json);
    }

    private void reLoginTimeLineServer() {
        UserInfoBean userInfoJsonObject = PreferencesHelper.getInstance().getCurrentUser();//第一次打开应用的时候
        if (userInfoJsonObject == null) {
            return;
        }
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("name", userInfoJsonObject.name);
            requestJson.put("userId", userInfoJsonObject.id);
            requestJson.put("avatar", userInfoJsonObject.avatar);
            requestJson.put("password", userInfoJsonObject.passWord);
            requestJson.put("keepServerMail", "@inspur.com");
            requestJson.put("inUse", 1);
            requestJson.put("type", 1);
            requestJson.put("loginTime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (socket.connected()) {
            sendSocketRequest("login", requestJson);
        }
    }

    private Emitter.Listener onConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            connectedAttempt = 0;
            Log.i(TAG, "connected to server" + socket.id());
            if (PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_AD_SERVER) &&
                    !PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_TIME_LINE) ||
                    isProcessKilled) {
                if (needLogin)
                    reLoginTimeLineServer();
            }
            dispatcher.dispatchNetWorkAction(NetWorkActions.CONNECT_TO_TIMELINE_SERVER_SUCCESS);
            dispatcher.dispatchStoreActionEvent(NetWorkActions.CONNECT_TO_TIMELINE_SERVER_SUCCESS);
        }
    };

    private Emitter.Listener onDisConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Disconnected to server" + disconnectType);
            PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
            dispatcher.dispatchNetWorkAction(NetWorkActions.DISCONNECT_TO_TIMELINE_SERVER);
            if (isNetWorkAvailable()) {       //与服务器断开连接而且当前网络可用 则直接重连服务器尝试次数加一
                if (disconnectType == ConnectType.DISCONNECT_WHEN_NET_TYPE_CHANGE || disconnectType ==
                        ConnectType.DISCONNECT_WHEN_EXIT_LOGIN) {
                    disconnectType = 0;
                    return;
                }
                if (connectedAttempt == 0) {
                    connectedAttempt++;
                    handler.sendEmptyMessage(0x02);
                }
            } else {
                handler.sendEmptyMessage(0x01);
            }
        }
    };

    private Emitter.Listener onReConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "ReConnect to server");
            dispatcher.dispatchNetWorkAction(NetWorkActions.RECONNECT_TO_TIMELINE_SERVER);
        }
    };

    private Emitter.Listener onConnectTimeOutListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "connected to server TimeOut   connectedAttempt   " + connectedAttempt);
            if (isNetWorkAvailable()) { //连接到时间轴服务器超时，而且网络可用，则延迟一定的时间在重连服务器
                if ((connectType == ConnectType.CONNECT_FROM_LOGIN || connectType == ConnectType.CONNECT_FROM_WELCOME)) {
                    dispatcher.dispatchUpdateUIEvent(NetWorkActions.CONNECT_TO_TIMELINE_SERVER_TIME_OUT);
                    connectedAttempt = 0;
                    return;
                }
              /*  if (connectedAttempt > 2)
                    return;*/
                handler.sendEmptyMessageDelayed(0x02, connectedAttempt * RECONNECT_TIME_UINT);
                connectedAttempt++;
            } else {
                dispatcher.dispatchUpdateUIEvent(NetWorkActions.CONNECT_TO_TIMELINE_SERVER_TIME_OUT);
            }
        }
    };

    private Emitter.Listener onErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "onError" + args.length + args[0].toString());
            //PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
        }
    };

    private Emitter.Listener onPingListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
//            Log.i(TAG, "call: " + "==== socket ping");
        }
    };

    private Emitter.Listener onPongListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
//            Log.i(TAG, "call: " + "==== socket pong");
        }
    };

    private Emitter.Listener newTaskListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        }
    };

    private Emitter.Listener newGroupMsgListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: " + Thread.currentThread().getName());
            JSONObject msg;
            try {
//                msg = new JSONObject(CharsetCoder.getInstance().convertStringCharsetCode(msg.toString()));
                msg = new JSONObject(args[0].toString());
                int type = getMsgType(msg);
                dispatchNewMsgEvent(type, msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void dispatchNewMsgEvent(int type, JSONObject msg) {
        Log.i(TAG, "dispatchNewMsgEvent: " + msg.toString());

        if (msg.has("fbId")) {
            String _id = msg.optString("fbId");
            JSONObject body = new JSONObject();
            try {
                body.put("fbId", _id);
                body.put("isPhone", true);
                sendSocketRequest("msgFeedBack", createRequestJson(body));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (type == MessageActions.MESSAGE_SORT_TASK) {
            dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_SORT_TASK_NUM, msg);
            return;
        } else if (type == MessageActions.MESSAGE_DELETE_GROUP) {
            String groupId = msg.optString("groupId");
            if (isChatWindowShow(groupId)) {
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.MESSAGE_DELETE_GROUP, groupId);
            }
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_MESSAGE_BY_GROUPID, groupId);
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.DELETE_VCHAT_ONE_CHAT, true, groupId);
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
            return;
        } else if (type == MessageActions.MESSAGE_TO_READ) {
            String groupId = msg.optString("groupId");
            if (msg.has("taskId")) {
                dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, groupId);
                dispatcher.dispatchStoreActionEvent(TimeLineActions.TIME_LINE_SET_MESSAGE_TO_READ, msg.optString("taskId"), groupId);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.SET_UNREAD_MSG_TO_READ, groupId);
            }
            return;
        } else if (type == MessageActions.MESSAGE_ADD_MEMBER) {
            String groupId = msg.optString("groupId");
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg);
            if (isChatWindowShow(groupId)) {
                dispatcher.dispatchUpdateUIEvent(MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER, msg);
            }
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
            return;
        } else if (type == MessageActions.MESSAGE_RENAME_CHAT) {
            String groupId = msg.optString("groupId");
            String subject = msg.optString("subject");
            dispatcher.dispatchDataBaseAction(DataBaseActions.RENAME_CHAT_SUBJECT, groupId, subject);
            if (isChatWindowShow(groupId)) {

            }
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
        if (isChatWindowShow(groupId)) { //收到消息的是当前窗口
            if (type == 5)
                return;
            if (TextUtils.isEmpty(taskId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_NORMAL_CHAT_MSG, msg);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_TASK_CHAT_MSG, msg);
            }
        } else { // 收到消息但是当前不再聊天窗口
            if (type == 5)
                return;
            if (TextUtils.isEmpty(taskId)) {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_NORMAL_CHAT_MSG, msg);
            } else {
                dispatcher.dispatchStoreActionEvent(MessageActions.RECIVE_UNREAD_TASK_CHAT_MSG, msg);
            }
            if (!msg.optJSONObject("from").optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id))
                sendNotification(msg.optString("title"), msg.optJSONObject("from").optString("name"), msg.optString("content"), msg.optString("groupId"));
        }
    }

    private boolean isChatWindowShow(String groupId) {
        return !TextUtils.isEmpty(chatWindowId.groupId) && groupId.equals(chatWindowId.groupId);
    }

    private boolean isTaskChatWindowShow(String taskId) {
        return !TextUtils.isEmpty(chatWindowId.taskId) && taskId.equals(chatWindowId.taskId);
    }

    private int getMsgType(JSONObject msg) {
        return msg.optInt("type");
    }


    private class TimeOutRunnable implements Runnable {

        private String uuid;

        public TimeOutRunnable(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void run() {
            RequestBean requestType = netRequestMap.get(uuid);
            dispatchLoadTimeOutEvent(requestType, uuid);
        }
    }

    private void dispatchLoadTimeOutEvent(RequestBean requestBean, String uuid) {
        netRequestMap.remove(uuid);
        dispatcher.dispatchStoreActionEvent(CommonActions.GET_TIMELINE_DATA_FROM_SERVER_TIME_OUT, requestBean.requestType, uuid);
    }

    private static class RequestBean {

        public RequestBean(String requestType, TimeOutRunnable runnable) {
            this.requestType = requestType;
            this.runnable = runnable;
        }

        public String requestType;
        public TimeOutRunnable runnable;

    }

    public void setChatWindowId(String groupId) {
        Log.i(TAG, "groupId_____ " + groupId);
        chatWindowId.groupId = groupId;
    }

    public void setAppVisable(boolean isAppVisable) {
        this.isAppVisable = isAppVisable;
    }

    @Override
    public void onTrimMemory(int level) { //当进入不可见的状态时会调用
        super.onTrimMemory(level);
    }

    private static class ChatWindowId {
        public String taskId;
        public String groupId;
    }

    private boolean isNetWorkAvailable() {
        return NetWorkUtils.isNetWorkAvailable(this);
    }

    private JSONObject createRequestJson(JSONObject body) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
        requestJson.put("Body", body);
        return requestJson;
    }

    int notificationId = 0;

    private void sendNotification(String chatTitle, String title, String content, String groupId) {
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
}
