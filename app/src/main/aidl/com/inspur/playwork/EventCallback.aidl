// EventCallback.aidl
package com.inspur.playwork;

// Declare any non-default types here with import statements

import com.inspur.playwork.model.common.SocketEvent;

oneway interface EventCallback {
    void notifyEvent(in SocketEvent socketEvent);
}
