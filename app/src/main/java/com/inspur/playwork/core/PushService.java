package com.inspur.playwork.core;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.EventCallback;
import com.inspur.playwork.model.common.SocketEvent;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.EventManager;
import com.inspur.playwork.utils.NetWorkUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.db.PushSeviceDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;

/**
 * Socket 连接策略：
 * 总共分为两种状态，又由于手机的是否锁屏状态总共有四种状态
 * 1、app前台状态，手机未锁屏
 * 此时是app的正常运行状态，需要保持socket连接，即需要时刻监听socket状态，在socket断掉时进行重连
 * 2、app前台状态，手机锁屏
 * 此时的处理和手机未锁屏时一致
 * 3、app后台状态，手机未锁屏
 * 此时处理和手机未锁屏时一致
 * 4、app后台状态，手机锁屏
 * 此时可以适当不保持socket连接状态，待从此状态恢复时，要第一时间检查socket状态，若已经断开要及时重连。
 */

public class PushService extends Service implements Emitter.Listener {

    private static final String TAG = "PushServiceFan";


    private static final String NEW_GROUP_MSG = "newGroupMsg";
    private static final String NEW_TASK = "newTask";

    //    public static final String HTTP_SERVER_IP = "http://10.110.6.64:6382/";
//    public static final String HTTP_SERVER_IP = "http://218.57.135.45:55166/";
//    public static final String HTTP_SERVER_IP = "http://218.57.135.45:9080/";
    public static final String HTTP_SERVER_IP = "http://htime.inspur.com:6380/";
//    public static final String HTTP_SERVER_IP = "http://htime.inspur.com:55166/";
//    public static final String HTTP_SERVER_IP = "http://10.47.0.181:6382/";

    private static final int SOCKET_CONNECT_TO_SERVER = 0;
    private static final int SOCKET_DISCONNECT_TO_SERVER = -1;
    private static final int SOCKET_LOGIN_SERVER = 2;
    private static final int SOCKET_COMMON_EVENT = 1;
    private static final int SOCKET_CONNECT_TIME_OUT = 3;

    private static final int INIT_SOCKET_EVENT_DB = 0x01;
    private static final int GET_UNPROCESSED_EVENT = 0x02;
    private static final int SET_EVENT_PROCESSED = 0x03;
    private static final int RECIVE_SOCKET_EVENT = 0x04;
    private static final int SERVER_DELETE_SOCKET_EVENT = 0x05;
    private static final int GET_SERVER_UNREAD_MSG = 0x06;
    private static final int SEND_SOCKET_REQUEST = 0x07;
    private static final int GET_SERVER_RESPONSE = 0x08;
    private static final int LOGIN_TIME_LINE_SUCCESS = 0x09;
    private static final int LOG_OUT = 0x0A;
    private static final int CONNECT_TIME_LINE = 0x0B;
    private static final int CANCEL_CONNECT = 0x0C;
    private static final int APP_MOVE_TO_BACK = 0x0D;
    private static final int APP_RESUME_FROM_BACk = 0x0E;


    private byte APP_VISABLE_MASK = 0x01;
    private byte APP_BACK_MASK = 0x02;

    private byte SCREEN_ON_MASK = 0x04;
    private byte SCREEN_OFF_MASK = 0x08;

    private byte appStatus = 0;

    int clientProcessId;

    EventCallback eventCallback;
    private Socket socket;
    private IO.Options options = new IO.Options();
    private UserInfoBean currentUser;
    private ArrayMap<String, SocketEvent> eventMap;//存放当前正在处理的事件
    private ArrayList<SocketEvent> needNotifyEventList;
    PushServiceHandler handler;

    PushSeviceDB pushSeviceDB;
    String connectId;

//    private boolean isLogin = false;

    private String token;

    private int netWorkState = -1;

    private boolean isConnecting = false;

    private ArrayList<NetEventInfo> needSendEvent;

    public PushService() {
        options.reconnection = false;
        options.timeout = 8000;
        options.forceNew = true;
        handler = new PushServiceHandler(new WeakReference<>(this));
        pushSeviceDB = PushSeviceDB.getInstance();
        eventMap = new ArrayMap<>();
        needNotifyEventList = new ArrayList<>();
        needSendEvent = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(64329, new Notification());
        } else {
            startForeground(64329, new Notification());
            startService(new Intent(this, InnerService.class));
        }

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkReceiver, mFilter);
        startScreenBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        unregisterReceiver(netWorkReceiver);
        unregisterReceiver(mScreenReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    /**
     * thread=binder-1
     */
    private EventManager.Stub binder = new EventManager.Stub() {

        @Override
        public void registerCallBack(EventCallback callback) throws RemoteException {
            Log.i(TAG, "registerCallBack: " + (callback == null));
            eventCallback = callback;
            if (needNotifyEventList.size() > 0) {
                notifyEvent(needNotifyEventList.get(0));
                needNotifyEventList.clear();
            }
        }

        @Override
        public void unRegisterCallBack(EventCallback callback) throws RemoteException {
            Log.i(TAG, "unRegisterCallBack: ");
            eventCallback = null;
        }

        @Override
        public void setClientProcessId(int pid) throws RemoteException {
            clientProcessId = pid;
        }

        @Override
        public void setCurrentUser(UserInfoBean userInfoBean) throws RemoteException {
//            isLogin = true;
            Log.i(TAG, "setCurrentUser: " + userInfoBean + "=========" + (currentUser == null));
            if (currentUser == null || !currentUser.id.equals(userInfoBean.id)) {
                currentUser = userInfoBean;
                handler.sendEmptyMessage(INIT_SOCKET_EVENT_DB);
                reLoginTimeLineServer();
            } else if (currentUser.id.equals(userInfoBean.id)) {
                handler.sendMessage(handler.obtainMessage(LOGIN_TIME_LINE_SUCCESS, token));
            }
        }

        @Override
        public void getUnProcessEvent() throws RemoteException {
            handler.sendMessage(handler.obtainMessage(GET_UNPROCESSED_EVENT));
        }

        @Override
        public void setEventProcessed(String fbId) throws RemoteException {
            handler.sendMessage(handler.obtainMessage(SET_EVENT_PROCESSED, fbId));
        }

        @Override
        public void sendSocketRequest(String type, String info) throws RemoteException {
//            handler.sendMessage(handler.obtainMessage());
            switch (type) {
                case "-9999":
                    handler.sendMessage(handler.obtainMessage(LOG_OUT));
                    return;
                case "-9998":
                    if (TextUtils.isEmpty(info))
                        handler.sendEmptyMessage(CONNECT_TIME_LINE);
                    else
                        handler.sendMessage(handler.obtainMessage(CONNECT_TIME_LINE, info));
                    return;
                case "-9997": //从后台恢复
                    handler.sendMessage(handler.obtainMessage(APP_RESUME_FROM_BACk));
                    return;
                case "-9996": //进入到后台
                    handler.sendMessage(handler.obtainMessage(APP_MOVE_TO_BACK));
                    return;
            }
            String[] args = new String[]{type, info};
            handler.sendMessage(handler.obtainMessage(SEND_SOCKET_REQUEST, args));
        }
    };

    /**
     * process = mainthread
     */
    private void queryAndNotifyEvent() {
        ArrayList<SocketEvent> eventList = pushSeviceDB.querySocketEvent();
        if (eventList != null)
            needNotifyEventList.addAll(eventList);
//        getUnReadMessage();
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        if (intent != null) {
            isAppExit = false;
            connectedToTimeLineServer();
            return binder;
        } else {
            return null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        clientProcessId = 0;
        return super.onUnbind(intent);
    }

    @Override
    public void call(Object... args) {
        JSONObject json = null;
        try {
            json = new JSONObject(args[0].toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert json != null;
        String type = json.optString("type");
        Log.i(TAG, "response: " + args[0].toString());
        switch (type) {
            case "login":
                if (json.optString("LoginStatus").equals("success")) {
                    connectId = json.optString("ConnectionId");
//                    SocketEvent socketEvent = new SocketEvent("", SOCKET_LOGIN_SERVER, "");
                    token = json.optString("token");
                    handler.sendMessage(handler.obtainMessage(LOGIN_TIME_LINE_SUCCESS, token));
//                    getUnReadMessage();
                } else if (json.optJSONObject("docs").optInt("res") == 2) {
                    connectId = json.optJSONObject("docs").optString("connectId");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("isLogout", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendSocketRequest("login_out", jsonObject);
                }
                break;

            default:
                SocketEvent socketEvent = new SocketEvent("", SOCKET_COMMON_EVENT, args[0].toString());
                handler.sendMessage(handler.obtainMessage(GET_SERVER_RESPONSE, socketEvent));
                break;
        }
    }

    private void doAfterServerDeleteEvent(String fbId, boolean isSuccess) {
        SocketEvent event;
        event = getProcessedSocketEvent(fbId);

        if (event == null)
            return;
        if (isSuccess) {
            event.isServerDelete = true;
            if (event.isClientProcess) {
                removeAndNotifyNext(fbId, event);
            } else {
                pushSeviceDB.updateAfterServerDelete(fbId);
            }
        } else {
            if (event.isClientProcess) {
                eventMap.remove(fbId);
                needNotifyEventList.remove(event);

                if (needNotifyEventList.size() > 0) {
                    notifyEvent(needNotifyEventList.get(0));
                }
            }
        }
    }

    public void connectedToTimeLineServer() {
        Log.i(TAG, "connectedToTimeLineServer: isConnecting=" + isConnecting);
        if (isConnecting || connectAttemps > 5 || !isNetWorkAvailable()) {
            if ((socket != null && socket.connected())) {
                if (eventCallback != null) {
                    if (netWorkState != SOCKET_LOGIN_SERVER) {
                        try {
                            eventCallback.notifyEvent(new SocketEvent("", SOCKET_DISCONNECT_TO_SERVER, ""));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return;
            } else {
                if (eventCallback != null)
                    try {
                        eventCallback.notifyEvent(new SocketEvent("", SOCKET_DISCONNECT_TO_SERVER, ""));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                return;
            }
        }
        isConnecting = true;
        if (socket != null) {
            Log.i(TAG, "socket != null" + socket.connected());
            socket.disconnect();
            socket.off();
            socket = null;
        }

        try {
//            socket = IO.socket("http://10.110.6.64:5849", options);
//            socket = IO.socket("http://218.57.135.45:55165", options);
//            socket = IO.socket("http://218.57.135.45:9090", options);
//            socket = IO.socket("http://htime.inspur.com:5848", options);
            socket = IO.socket("http://htime.inspur.com:5848", options);
//            socket = IO.socket("http://10.47.0.181:5849", options);
            socket.on(Socket.EVENT_CONNECT, onConnectListener);
            socket.on(Socket.EVENT_DISCONNECT, onDisConnectListener);
            socket.on(Socket.EVENT_RECONNECT, onReConnectListener);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeOutListener);
            socket.on(Socket.EVENT_ERROR, onErrorListener);
            socket.on(NEW_GROUP_MSG, newGroupMsgListener);
            socket.on(NEW_TASK, newTaskListener);
            socket.on(Socket.EVENT_MESSAGE, this);
            socket.connect();
            Log.i(TAG, "send connect");
            handler.sendEmptyMessageDelayed(CANCEL_CONNECT, 10 * 1000);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //自己新建任務監聽
    private Emitter.Listener newTaskListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject msg = null;
            try {
                msg = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "new task:" + args[0].toString());


            if (msg != null) {
//                int type = msg.optInt("type");
                String fbId = msg.optString("fbId", "");
                SocketEvent socketEvent = new SocketEvent(fbId, 0xFF, args[0].toString());
                socketEvent.reciveTime = msg.optLong("sendTime");
                handler.sendMessage(handler.obtainMessage(RECIVE_SOCKET_EVENT, socketEvent));
            }
        }
    };

    /**
     * thread=EventThread
     */
    private Emitter.Listener newGroupMsgListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject msg = null;
            try {
                msg = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "newGroupMsg:" + args[0].toString());


            if (msg != null) {
//                int type = msg.optInt("type");
                String fbId = msg.optString("fbId", "");
                SocketEvent socketEvent = new SocketEvent(fbId, 0xFF, args[0].toString());
                socketEvent.reciveTime = msg.optLong("sendTime");
                handler.sendMessage(handler.obtainMessage(RECIVE_SOCKET_EVENT, socketEvent));
            }
        }
    };

    private void reciveNewSocketEvent(SocketEvent socketEvent) {
        needNotifyEventList.add(socketEvent);
        Collections.sort(needNotifyEventList);
        if (pushSeviceDB.insertSocketEvent(socketEvent) > -1) {

            Log.i(TAG, "reciveNewSocketEvent: " + needNotifyEventList.size());
            for (SocketEvent event : needNotifyEventList) {
                Log.i(TAG, "reciveNewSocketEvent: " + event.toString());
            }
            if (needNotifyEventList.size() == 1) {
                notifyEvent(socketEvent);
            }
//            sendFeedBackToServer(socketEvent.fbId);
        }
    }

    private void sendFeedBackToServer(String fbId) {
//        JSONObject body = new JSONObject();
//        try {
//            body.put("fbId", fbId);
//            body.put("userId", currentUser.id);
//            sendSocketRequest("msgFeedBack", createRequestJson(body, fbId));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", currentUser.id);
            jsonObject.put("fbId", fbId);
            jsonObject.put("isPhone", true);
            jsonObject.put("token", token);
            jsonObject.put("ClientId", fbId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClientManager.getInstance().post(HTTP_SERVER_IP + "msgFeedBack", jsonObject, msgFeedBackCallback, fbId);
    }

    private Callback msgFeedBackCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = call.request().header("requestId");
            handler.sendMessage(handler.obtainMessage(SERVER_DELETE_SOCKET_EVENT, -1, 0, requestId));
        }

        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: msg feed back" + res.toString());
                if (res.optBoolean("type")) {
                    handler.sendMessage(handler.obtainMessage(SERVER_DELETE_SOCKET_EVENT, res.optString("ClientId")));
                } else {
                    handler.sendMessage(handler.obtainMessage(SERVER_DELETE_SOCKET_EVENT, -1, 0, res.optString("ClientId")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onReConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "ReConnect to server");
        }
    };

    private int connectAttemps = 0;

    private Emitter.Listener onConnectTimeOutListener = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: onSocketConnectTimeOut");
            connectId = null;
            isConnecting = false;
            handler.removeMessages(CANCEL_CONNECT);
            if (eventCallback != null) {
                try {
                    eventCallback.notifyEvent(new SocketEvent("", SOCKET_CONNECT_TIME_OUT, ""));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if ((appStatus & SCREEN_OFF_MASK) > 0)
                connectAttemps++;

            if (!handler.hasMessages(CONNECT_TIME_LINE))
                handler.sendEmptyMessage(CONNECT_TIME_LINE);
//            connectedToTimeLineServer();
        }
    };

    private Emitter.Listener onErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            connectId = null;
            Log.i(TAG, "call: onErrorListener");
            handler.removeMessages(CANCEL_CONNECT);
            if (eventCallback != null) {
                try {
                    eventCallback.notifyEvent(new SocketEvent("", SOCKET_DISCONNECT_TO_SERVER, ""));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
//            connectedToTimeLineServer();
        }
    };

    private Emitter.Listener onConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnecting = false;
            handler.removeMessages(CANCEL_CONNECT);
            Log.i(TAG, "call: connect to timelineserver");
            netWorkState = SOCKET_CONNECT_TO_SERVER;
            SocketEvent socketEvent = new SocketEvent("", SOCKET_CONNECT_TO_SERVER, "");
            if (eventCallback != null)
                try {
                    eventCallback.notifyEvent(socketEvent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            else {
                needNotifyEventList.add(socketEvent);
            }
            connectAttemps = 0;
            reLoginTimeLineServer();
        }
    };

    private Emitter.Listener onDisConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: onDisConnectListener");
            connectId = null;
            isConnecting = false;
            handler.removeMessages(CANCEL_CONNECT);
            netWorkState = SOCKET_DISCONNECT_TO_SERVER;
            if (eventCallback != null)
                try {
                    eventCallback.notifyEvent(new SocketEvent("", SOCKET_DISCONNECT_TO_SERVER, ""));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
//            connectedToTimeLineServer();
            if (!handler.hasMessages(CONNECT_TIME_LINE))
                handler.sendEmptyMessage(CONNECT_TIME_LINE);
        }
    };

//    private boolean isNetWorkAvailable() {
//        return NetWorkUtils.isNetWorkAvailable(this);
//    }

    private void reLoginTimeLineServer() {
        if (currentUser == null) {
            return;
        }
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("name", currentUser.name);
            requestJson.put("userId", currentUser.id);
            requestJson.put("avatar", currentUser.avatar);
            requestJson.put("password", "");
            requestJson.put("keepServerMail", "@inspur.com");
            requestJson.put("inUse", 1);
            requestJson.put("type", 1);
            requestJson.put("macAddress", getUniqueId());
            requestJson.put("loginTime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (socket.connected()) {
            sendSocketRequest("login", requestJson);
        }
    }

    private String getUniqueId() {
        SharedPreferences sp = getSharedPreferences("playwork_config.xml", Context.MODE_PRIVATE);
        return sp.getString("unqiue_id", "");
    }

    private void sendSocketRequest(String type, Object data) {
        Log.i(TAG, "Request Method: " + type);

        if (!isCanUseSocket() && !type.equals("login") && !type.equals("login_out")) {
            needSendEvent.add(new NetEventInfo(data, type));
            if (!socket.connected())
                connectedToTimeLineServer();
            return;
        }

        try {
            JSONObject args = new JSONObject(data.toString());
            if (!TextUtils.isEmpty(connectId))
                args.put("ConnectionId", connectId);
            Log.i(TAG, "Request Data: " + args.toString());
            socket.emit(type, args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private boolean isCanUseSocket() {
        return netWorkState == SOCKET_LOGIN_SERVER;
    }

    private static class PushServiceHandler extends Handler {

        private WeakReference<PushService> weakReference;

        PushServiceHandler(WeakReference<PushService> serviceReference) {
            this.weakReference = serviceReference;
        }

        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case INIT_SOCKET_EVENT_DB:
                    if (weakReference.get() != null) {
                        weakReference.get().initSocketEventDb();
                    }
                    break;
                case GET_UNPROCESSED_EVENT:
                    if (weakReference.get() != null) {
                        weakReference.get().queryAndNotifyEvent();
                    }
                    break;
                case SET_EVENT_PROCESSED:
                    if (weakReference.get() != null) {
                        weakReference.get().setEventProcessed((String) msg.obj);
                    }
                    break;
                case RECIVE_SOCKET_EVENT:
                    if (weakReference.get() != null) {
                        weakReference.get().reciveNewSocketEvent((SocketEvent) msg.obj);
                    }
                    break;
                case SERVER_DELETE_SOCKET_EVENT:
                    if (weakReference.get() != null) {
                        weakReference.get().doAfterServerDeleteEvent((String) msg.obj, !(msg.arg1 == -1));
                    }
                    break;
                case GET_SERVER_UNREAD_MSG:
                    if (weakReference.get() != null) {
                        //noinspection unchecked
                        weakReference.get().getServerUnProcessMsg((ArrayList<SocketEvent>) msg.obj);
                    }
                    break;
                case SEND_SOCKET_REQUEST:
                    if (weakReference.get() != null) {
                        String[] args = (String[]) msg.obj;
                        weakReference.get().sendSocketRequest(args[0], args[1]);
                    }
                    break;
                case GET_SERVER_RESPONSE:
                    if (weakReference.get() != null) {
                        weakReference.get().notifyEvent((SocketEvent) msg.obj);
                    }
                    break;
                case LOGIN_TIME_LINE_SUCCESS:
                    if (weakReference.get() != null) {
                        weakReference.get().loginTimeLineSuccess((String) msg.obj);
                    }
                    break;
                case LOG_OUT:
                    if (weakReference.get() != null) {
                        weakReference.get().logout();
                    }
                    break;
                case CONNECT_TIME_LINE:
                    Log.i(TAG, "dispatchMessage: CONNECT_TIME_LINE");
                    if (weakReference.get() != null) {
                        if (!TextUtils.isEmpty((String) msg.obj) && msg.obj.equals("resume"))
                            weakReference.get().isAppExit = false;
                        weakReference.get().connectedToTimeLineServer();
                    }
                    break;
                case CANCEL_CONNECT:
                    if (weakReference.get() != null) {
                        weakReference.get().cancelConnect();
                    }
                    break;
                case APP_MOVE_TO_BACK:
                    if (weakReference.get() != null) {
                        weakReference.get().appMoveToBack();
                    }
                    break;
                case APP_RESUME_FROM_BACk:
                    if (weakReference.get() != null) {
                        weakReference.get().resumeFromBack();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void cancelConnect() {
        isConnecting = false;
        if (socket == null || !socket.connected()) {
            Log.i(TAG, "cancelConnect: ");
            connectedToTimeLineServer();
        }
    }

    private void resumeFromBack() {
        appStatus &= ~APP_BACK_MASK;
        appStatus |= APP_VISABLE_MASK;
        if (socket == null || !socket.connected()) {
            connectedToTimeLineServer();
        }
    }

    private void appMoveToBack() {
        appStatus &= ~APP_VISABLE_MASK;
        appStatus |= APP_BACK_MASK;
    }

    private boolean isAppExit = false;

    /**
     * 如果此方法被调用，说明用户在应用中点击了退出登录
     * 或者在登录页直接退出了
     * 在这种情况下，用户已经看不到应用了，此时socket不用保持连接
     * 网络变化时也不用重新连接
     */
    private void logout() {
        handler.removeMessages(CANCEL_CONNECT);
        if (socket != null) {
            Log.i(TAG, "socket != null" + socket.connected());
            socket.disconnect();
            socket.off();
            socket = null;
        }
        currentUser = null;
        connectId = "";
//        isLogin = false;
        token = "";
        isConnecting = false;
        isAppExit = true;
        needSendEvent.clear();
//        needNotifyEventList.clear();
//        pushSeviceDB = null;
        pushSeviceDB.logout();

        Log.i(TAG, "logout: " + currentUser);

//        unregisterReceiver(netWorkReceiver);
//        unregisterReceiver(mScreenReceiver);
    }

    private void loginTimeLineSuccess(String token) {
        netWorkState = SOCKET_LOGIN_SERVER;
        SocketEvent event = new SocketEvent("", SOCKET_LOGIN_SERVER, token);
        Log.i(TAG, "loginTimeLineSuccess: " + (eventCallback == null));
        notifyEvent(event);
        if (needSendEvent.size() > 0) {
            Iterator<NetEventInfo> it = needSendEvent.iterator();
            while (it.hasNext()) {
                NetEventInfo eventInfo = it.next();
                sendSocketRequest(eventInfo.type, eventInfo.arg);
                it.remove();
            }
        }
//        if (!isLogin) {
//            getUnReadMessage();
//            isLogin = false;
//        }
    }

    private void getServerUnProcessMsg(ArrayList<SocketEvent> unProcessEvent) {
        pushSeviceDB.insertSocketEventList(unProcessEvent);
        for (SocketEvent event : unProcessEvent) {
            if (!needNotifyEventList.contains(event)) {
                needNotifyEventList.add(event);
            }
        }

        Collections.sort(needNotifyEventList);

        if (needNotifyEventList.size() > 0) {
            notifyEvent(needNotifyEventList.get(0));
        }
    }

    private void setEventProcessed(String fbId) {
        Log.i(TAG, "setEventProcessed: " + fbId);
        isAppExit = false;
        SocketEvent event;
        event = getProcessedSocketEvent(fbId);
        Log.i(TAG, "setEventProcessed: " + (event == null));
        if (event == null)
            return;
        event.isClientProcess = true;

        Log.i(TAG, "setEventProcessed: " + event.toString());
        if (event.isServerDelete) {
            removeAndNotifyNext(fbId, event);
        } else {
            pushSeviceDB.updateAfterClentProcessed(fbId);
        }

    }

    private void removeAndNotifyNext(String fbId, SocketEvent event) {
        pushSeviceDB.deleteSocketEventByFbId(fbId);
        eventMap.remove(fbId);
        boolean res = needNotifyEventList.remove(event);

        Log.i(TAG, "removeAndNotifyNext: " + res);
        if (needNotifyEventList.size() > 0) {
            notifyEvent(needNotifyEventList.get(0));
        }
    }

    private SocketEvent getProcessedSocketEvent(String fbId) {
        SocketEvent event;
        if (!eventMap.containsKey(fbId)) {
            //noinspection SuspiciousMethodCalls
            int index = getEventIndex(fbId);
            if (index > -1)
                event = needNotifyEventList.get(index);
            else {
                return null;
            }
        } else {
            event = eventMap.get(fbId);
        }
        return event;
    }

    private int getEventIndex(String fbId) {
        int i = 0;
        for (SocketEvent event : needNotifyEventList) {
            if (event.fbId.equals(fbId))
                return i;
            i++;
        }
        return -1;
    }

    private boolean isClentProcessExists(int pID) {
        Log.i(TAG, "isClentProcessExists: " + pID);
        if (pID == 0)
            return false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : l) {
            if (info.pid == pID) {
                return true;
            }
        }
        this.clientProcessId = 0;
        return false;
    }

    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(64329, new Notification());
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

    private BroadcastReceiver netWorkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "Net Work CHANGE onReceive: ");
            NetworkInfo info = intent.getParcelableExtra("networkInfo");
            if (info != null) {
                if (info.isConnected()) {
                    Log.i(TAG, "onReceive: info.isConnected() true" + isAppExit);
                    if (!isAppExit) {
                        if (!handler.hasMessages(CONNECT_TIME_LINE)) {
                            isConnecting = false;
                            handler.sendEmptyMessage(CONNECT_TIME_LINE);
                        }
                    }
                }
            }
        }
    };

    private boolean isNetWorkAvailable() {
        boolean res = NetWorkUtils.isNetWorkAvailable(this);
        Log.i(TAG, "isNetWorkAvailable: " + res);
        return res;
    }

    private void initSocketEventDb() {
        pushSeviceDB.init(this, currentUser.id);
    }


    private int notifyEvent(SocketEvent event) {
        Log.i(TAG, "notifyEvent: " + event.toString());
        if (isClentProcessExists(clientProcessId)) {
            if (TextUtils.isEmpty(event.fbId)) {
                if (eventCallback != null) {
                    try {
                        eventCallback.notifyEvent(event);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return 4;
            } else {
                if (!event.isClientProcess) {
                    eventMap.put(event.fbId, event);
                    if (!event.isServerDelete) {
                        sendFeedBackToServer(event.fbId);
                    }
                    try {
                        if (eventCallback != null)
                            eventCallback.notifyEvent(event);
                        return 1;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return 0;
                    }
                } else {
                    if (event.isServerDelete) {
                        removeAndNotifyNext(event.fbId, event);
                        return 2;
                    } else {
                        sendFeedBackToServer(event.fbId);
                        return 3;
                    }
                }
            }

        } else {//唤醒主进程
            return -1;
        }
    }

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        private String action = null;


        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // 开屏
                Log.i(TAG, "onReceive: ACTION_SCREEN_ON");

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 锁屏
                Log.i(TAG, "onReceive: ACTION_SCREEN_OFF");
                appStatus &= ~SCREEN_ON_MASK;
                appStatus |= SCREEN_OFF_MASK;

            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                // 解锁
                Log.i(TAG, "onReceive: ACTION_USER_PRESENT");
                connectAttemps = 0;
                appStatus &= ~SCREEN_OFF_MASK;
                appStatus |= SCREEN_ON_MASK;
//                if ((appStatus & APP_BACK_MASK) > 0) {//此时app处于后台
//
//                }

                Log.i(TAG, "onReceive: " + isAppExit);
                if ((socket == null || !socket.connected()) && !isAppExit) {
//                    connectedToTimeLineServer();
                    if (isConnecting) {
                        isConnecting = false;
                    }
                    if (!handler.hasMessages(CONNECT_TIME_LINE))
                        handler.sendEmptyMessage(CONNECT_TIME_LINE);
                }
            }
        }
    };

    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenReceiver, filter);
    }

    private static class NetEventInfo {
        public String type;
        Object arg;
        public String uuid;

        NetEventInfo(Object arg, String type) {
            this.arg = arg;
            this.type = type;
        }
    }
}
