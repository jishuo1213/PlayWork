package com.inspur.playwork.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.bumptech.glide.Glide;
import com.github.moduth.blockcanary.BlockCanary;
import com.inspur.playwork.BuildConfig;
import com.inspur.playwork.actions.network.NetAction;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.stores.login.LoginStores;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.EmojiHandler;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.utils.db.DBOperation;
import com.inspur.playwork.utils.loadfile.FileLoader;
import com.inspur.playwork.utils.loadfile.LoadFileHandlerThread;
import com.inspur.playwork.weiyou.store.VUStores;
import com.squareup.leakcanary.LeakCanary;

import java.util.UUID;


/**
 * Created by Fan on 15-9-11.
 */
public class PlayWorkApplication extends Application {

    private static final String TAG = "PlayWorkApplicationFan";

//    private AppHandler appHandler;

    private LruCache<String, Bitmap> imageBitmapCache;

    private Dispatcher dispatcher;

    private PreferencesHelper preferencesHelper;

    private ResourcesUtil resourcesUtil;

    private DBOperation dbOperation;

    private ArrayMap<String, Long> avatars;

    private ArrayMap<String, Integer> notificationMap;

    private FileLoader loadFileHandlerThread;

//    private TimeLineStores timeLineStores;

    private TimeLineStoresNew timeLineStoresNew;

    private MessageStores messageStores;

    private VUStores vuStores;

    private boolean isLogOut = false;
    private ApplicationStores applicationStores;

    //TODO:建立人员对象缓存
    private LruCache<String,UserInfoBean> usersCache;

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getCurProcessName(this);
        Log.i(TAG, "onCreate: " + processName);
        if (processName != null && processName.equals("com.inspur.playwork")) {
            Log.e(TAG, "appCreate" + processName);
            registerActivityLifecycleCallbacks(ActivityLifecycleListener.getInstance());
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
            BlockCanary.install(this, new AppBlockCanaryContext()).start();
            init(false);
            CrashHandler.getInstance().init();
        }
//        KLog.init(BuildConfig.LOG_DEBUG);
    }

    private void init(boolean isResume) {
        Log.i(TAG, "init startService");
        startService(new Intent(this, PushService.class));
        Intent startUiService = new Intent(PlayWorkApplication.this, PlayWorkServiceNew.class);
        if (!isResume)
            startUiService.putExtra("fromApp", true);
        else
            startUiService.putExtra("resume", true);
        ComponentName result = startService(startUiService);
        if (result != null) {
            Log.i(TAG, "init: service already srart" + result.toString());
        }

        avatars = new ArrayMap<>();

        notificationMap = new ArrayMap<>();

        SingleRefreshManager.getInstance().init(this);

        preferencesHelper = new PreferencesHelper();
        resourcesUtil = new ResourcesUtil();
        if (dispatcher == null)
            dispatcher = new Dispatcher();

        preferencesHelper.init(this);
        resourcesUtil.init(this);

        if (TextUtils.isEmpty(preferencesHelper.readStringPreference(PreferencesHelper.UNIQUE_ID))) {
            preferencesHelper.writeToPreferences(PreferencesHelper.UNIQUE_ID, UUID.randomUUID().toString());
        }
        dbOperation = new DBOperation();

        EmojiHandler.getInstance().initEmjiMap(this);

//        if (preferencesHelper.getCurrentUser() != null) {
//            dbOperation.init(this);
//        }

//        timeLineStores = new TimeLineStores();
//        timeLineStores.register();

        timeLineStoresNew = new TimeLineStoresNew();
        timeLineStoresNew.register();

        messageStores = new MessageStores();
        messageStores.register();


        vuStores = new VUStores();
//        vuStores.register();

        registerDispather();
//        appHandler = new AppHandler(new WeakReference<>(this));
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        imageBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        FileUtil.init(this);
        FileUtil.getAvatarDirFiles(avatars);

        messageStores.setAvatars(avatars);


        loadFileHandlerThread = new LoadFileHandlerThread("LoadFile");
//        ((LoadFileHandlerThread)loadFileHandlerThread).initLoadFileHandlerThread();
        loadFileHandlerThread.init();

        Log.i(TAG, "init: stop");
    }


    public LruCache<String, Bitmap> getImageBitmapCache() {
        return imageBitmapCache;
    }

    public ArrayMap<String, Long> getAvatars() {
        return avatars;
    }

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


    @SuppressWarnings("unused")
    public void onEvent(NetAction netAction) {
        int type = netAction.getActionType();
        switch (type) {
            case NetWorkActions.USER_AVATAR_DOWNLOADED:
                updateAvatarMap((String) netAction.getActiontData().get(0), (long) netAction.getActiontData().get(1));
                break;
        }
    }

    public void registerDispather() {
        dispatcher.register(this);
    }

    public void unRegisterDispather() {
        if (dispatcher != null)
            dispatcher.unRegister(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public PreferencesHelper getPreferencesHelper() {
        return preferencesHelper;
    }

    public ResourcesUtil getResourcesUtil() {
        return resourcesUtil;
    }

    public DBOperation getDbOperation() {
        return dbOperation;
    }


    public ArrayMap<String, Integer> getNotificationMap() {
        return notificationMap;
    }

    private void updateAvatarMap(String userId, long avatarId) {
        if (avatars.containsKey(userId)) {
            long preAvatarId = avatars.get(userId);
            if (preAvatarId >= avatarId) {
                FileUtil.deleteFile(FileUtil.getAvatarFilePath() + userId + "-" + avatarId + ".png");
            } else {
                FileUtil.deleteFile(FileUtil.getAvatarFilePath() + userId + "-" + preAvatarId + ".png");
                avatars.put(userId, avatarId);
            }
        } else {
            avatars.put(userId, avatarId);
        }
    }

    public FileLoader getLoadFileHandlerThread() {
        return loadFileHandlerThread;
    }

//    public void upLoadFile(String url, String filePath, OkHttpClientManager.Param[] params, Object clientId, Handler responesHandler, boolean isNeedProgress, int type) {
//        loadFileHandlerThread.upLoadFile(url, filePath, params, clientId, responesHandler, isNeedProgress, type);
//    }

/*    public TimeLineStores getTimeLineStores() {
        return null;
    }*/

    public TimeLineStoresNew getTimeLineStoresNew() {
        return timeLineStoresNew;
    }

    public MessageStores getMessageStores() {
        return messageStores;
    }

    public VUStores getVUStores() {
        return vuStores;
    }

    public void logout() {
        LoginStores.clean();
        dispatcher.clean();
        preferencesHelper.clean();
        resourcesUtil.clean();
        dbOperation.clean();
        avatars.clear();
        avatars = null;

        notificationMap.clear();
        notificationMap = null;
        loadFileHandlerThread.clean();
        timeLineStoresNew.clean();
        messageStores.clean();
        vuStores.clean();
        if (applicationStores != null)
            applicationStores.clean();

//        dispatcher = null;
        preferencesHelper = null;
        resourcesUtil = null;
        dbOperation = null;
        loadFileHandlerThread = null;
        timeLineStoresNew = null;
        messageStores = null;
        vuStores = null;
        applicationStores = null;
        isLogOut = true;

        SingleRefreshManager.clean();

        OkHttpClientManager.sclean();

        imageBitmapCache.evictAll();
        imageBitmapCache = null;
        Glide.get(this).clearMemory();
        EmojiHandler.getInstance().clear();
    }


    public boolean isLogOut() {
        return isLogOut;
    }

    public void resume() {
        init(true);
//        PlayWorkCrashHandler.getInstance().init(BuildConfig.UPLOAD_LOG);
        CrashHandler.getInstance().init();
        Log.i(TAG, "resume: ");
        isLogOut = false;
    }

    public ApplicationStores getApplicationStores() {
        if (applicationStores == null)
            applicationStores = new ApplicationStores(dispatcher);
        return applicationStores;
    }
}
