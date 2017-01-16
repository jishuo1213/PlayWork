// EventManager.aidl
package com.inspur.playwork;

// Declare any non-default types here with import statements

import com.inspur.playwork.EventCallback;
import com.inspur.playwork.model.common.UserInfoBean;

interface EventManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void registerCallBack(EventCallback callback);

    void unRegisterCallBack(EventCallback callback);

    void setClientProcessId(int pid);

    void setCurrentUser(in UserInfoBean currentUser);

    void getUnProcessEvent();

    void setEventProcessed(String fbId);

    void sendSocketRequest(String type,String info);
}
