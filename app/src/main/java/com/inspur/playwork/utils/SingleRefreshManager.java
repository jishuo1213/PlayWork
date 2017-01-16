package com.inspur.playwork.utils;

import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.weiyou.store.VUStores;

/**
 * Created by Fan on 15-11-9.
 */
public class SingleRefreshManager {

    private static final String TAG = "SingleRefreshManager";

    private PlayWorkApplication playWorkApp;

    private static SingleRefreshManager ourInstance = new SingleRefreshManager();

    public static SingleRefreshManager getInstance() {
        if (ourInstance == null)
            ourInstance = new SingleRefreshManager();
        return ourInstance;
    }


    private SingleRefreshManager() {
    }


    public void init(PlayWorkApplication playWorkApp) {
        this.playWorkApp = playWorkApp;
    }

/*    public void setPlayWorkApp(PlayWorkApplication playWorkApp) {
        this.playWorkApp = playWorkApp;
    }*/

    public PreferencesHelper getPreferencesHelper() {
        return playWorkApp.getPreferencesHelper();
    }

    public Dispatcher getDispatcher() {
        return playWorkApp.getDispatcher();
    }

    public ResourcesUtil getResourcesUtil() {
        return playWorkApp.getResourcesUtil();
    }

//    public TimeLineStores getTimeLineStores() {
//        return playWorkApp.getTimeLineStores();
//    }

    public TimeLineStoresNew getTimeLineStoresNew() {
        return playWorkApp.getTimeLineStoresNew();
    }

    public MessageStores getMessageStores() {
        return playWorkApp.getMessageStores();
    }

    public ApplicationStores getApplicationStores() {
        return playWorkApp.getApplicationStores();
    }

    public VUStores getVUStores() {
        return playWorkApp.getVUStores();
    }


    public static void clean() {
//        ourInstance.playWorkApp = null;
//        ourInstance = null;
    }
}
