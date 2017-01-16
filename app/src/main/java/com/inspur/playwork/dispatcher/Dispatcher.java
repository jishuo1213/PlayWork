package com.inspur.playwork.dispatcher;


import android.util.Log;

import com.inspur.playwork.actions.DbAction;
import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.network.NetAction;
import com.inspur.playwork.utils.SingleRefreshManager;

import java.util.ArrayList;
import java.util.Objects;

import de.greenrobot.event.EventBus;

/**
 * Created by fan on 15-8-21.
 */
public class Dispatcher {
    // private static Dispatcher dispatcher;

    private static final String TAG = "Dispatcher";

    private EventBus eventBus;

    private ArrayList<Object> registerList;

    public static Dispatcher getInstance() {
/*        if (dispatcher == null) {
            dispatcher = new Dispatcher();
        }*/
        return SingleRefreshManager.getInstance().getDispatcher();
    }

    public Dispatcher() {
        eventBus = EventBus.getDefault();
        registerList = new ArrayList<>();
    }

    public void register(final Object cls) {
        if (eventBus != null) {
            if (!eventBus.isRegistered(cls)) {
                registerList.add(cls);
                eventBus.register(cls);
            }
        }
    }

    public void register(final Object cls, int priority) {
        eventBus.register(cls, priority);
    }

    public void unRegister(Object cls) {
        if (eventBus != null) {
            eventBus.unregister(cls);
            registerList.remove(cls);
        }
    }

    public void registerSticky(Object cls) {
        eventBus.registerSticky(cls);
    }

    public void registerSticky(Object cls, int priority) {
        eventBus.registerSticky(cls, priority);
    }

    public void cancelEventDelivery(Object event) {
        eventBus.cancelEventDelivery(event);
    }

    public boolean isRegistered(Object subscriber) {
        if (eventBus != null)
            return eventBus.isRegistered(subscriber);
        return false;
    }

    public boolean hasSubscriberForEvent(Class<?> cls) {
        return eventBus.hasSubscriberForEvent(cls);
    }

    public void removeAllStickyEvents() {
        eventBus.removeAllStickyEvents();
    }

    public void removeStickyEvent(Object event) {
        eventBus.removeStickyEvent(event);
    }

    public <T> T removeStickyEvent(Class<T> eventType) {
        return eventBus.removeStickyEvent(eventType);
    }

    public <T> T getStickyEvent(Class<T> eventType) {
        return eventBus.getStickyEvent(eventType);
    }


    public void dispatchStickyEvent(int type, Object... data) {
        StoreAction.Builder builder = StoreAction.buildType(type);
        for (int i = 0; i < data.length; i++) {
            builder.bundle(i, data[i]);
        }
        postStickyEvent(builder.build());
    }

    /**
     * 分发业务处里的事件
     */
    public void dispatchStoreActionEvent(int type, Object... data) {
        Log.i(TAG, "dispatchStoreActionEvent: " + type);
        StoreAction.Builder builder = StoreAction.buildType(type);
        for (int i = 0; i < data.length; i++) {
            builder.bundle(i, data[i]);
        }
        postEvent(builder.build());
    }


    /**
     * 分发更新界面的事件
     */
    public void dispatchUpdateUIEvent(int type, Object... data) {
        Log.i(TAG, "dispatchUpdateUIEvent: " + type);
        UpdateUIAction.Builder builder = UpdateUIAction.buildType(type);
        for (int i = 0; i < data.length; i++) {
            builder.bundle(i, data[i]);
        }
        postEvent(builder.build());
    }

    public void dispatchNetWorkAction(int type, Object... data) {
        Log.i(TAG, "dispatchNetWorkAction: " + type);
        NetAction.Builder builder = NetAction.buildType(type);
        for (int i = 0; i < data.length; i++) {
            builder.bundle(i, data[i]);
        }
        postEvent(builder.build());
    }

    public void dispatchDataBaseAction(int type, Object... data) {
        Log.i(TAG, "dispatchDataBaseAction: " + type);
        DbAction.Builder builder = DbAction.buildType(type);
        for (int i = 0; i < data.length; i++) {
            builder.bundle(i, data[i]);
        }
        postEvent(builder.build());
    }


    private void postEvent(final Object event) {
        if (eventBus != null)
            eventBus.post(event);
    }

    private void postStickyEvent(final Object event) {
        if (eventBus != null)
            eventBus.postSticky(event);
    }

    public void clean() {
//        EventBus.clearCaches();
        for (Object cls : registerList) {
            eventBus.unregister(cls);
        }
        registerList.clear();
    }
}
