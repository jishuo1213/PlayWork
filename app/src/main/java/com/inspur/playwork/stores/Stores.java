package com.inspur.playwork.stores;

import com.bumptech.glide.manager.LifecycleListener;
import com.inspur.playwork.dispatcher.Dispatcher;

/**
 * 处理业务逻辑的类，注意，在需要发送事件的时候，一定要先
 * 调用register方法将该类注册到事件总线中，在不需要的时候，
 * 一定要调用unregister解除注册
 * Created by fan on 15-8-22.
 */
public class Stores implements LifecycleListener {

    protected Dispatcher dispatcher;

    public Stores(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void register() {
        if (!dispatcher.isRegistered(this))
            dispatcher.register(this);
    }

    public void unRegister() {
        if (dispatcher.isRegistered(this))
            dispatcher.unRegister(this);
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }
}
