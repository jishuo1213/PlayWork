package com.inspur.playwork.weiyou.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Created by sunyuan on 2016/12/15 0015 16:00.
 * Email: sunyuan@inspur.com
 */

public class MailSendHandlerThread extends HandlerThread implements Handler.Callback{

    private Handler loadFileHandler;


    public MailSendHandlerThread(String name) {
        super(name);
    }


    public void init() {
        start();
        Looper looper = getLooper();
        loadFileHandler = new Handler(looper, this);
    }

    @Override
    public boolean handleMessage(Message msg) {

        return false;
    }
}
