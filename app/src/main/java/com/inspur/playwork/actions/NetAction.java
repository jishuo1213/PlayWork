package com.inspur.playwork.actions.network;

import android.util.SparseArray;

/**
 * 这个类代表出现事件的动作，其中getActionType返回事件的类型，
 * 各个事件的类型在Actions类中有常量设置，getActionData()返回这个动作
 * 携带的数据，是一个sprseArray，在build一个action的时候，要按照顺序
 * 0 ,1,2.....将数据放进去，在之后的处理事件方法中，要按照放入时的顺序
 * 得到放入的数据
 * Created by fan on 15-8-21.
 */
public class NetAction {

    private final int actionType;

    private final SparseArray<Object> data;


    public NetAction(int actionType, SparseArray<Object> data) {
        this.actionType = actionType;
        this.data = data;
    }

    public static Builder buildType(int type) {
        return new Builder().with(type);
    }

    public int getActionType() {
        return actionType;
    }

    public SparseArray<Object> getActiontData() {
        return data;
    }

    public static class Builder {
        private int type;
        private SparseArray<Object> data;

        Builder with(int type) {
            this.type = type;
            this.data = new SparseArray<>();
            return this;
        }

        public Builder bundle(int key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public NetAction build() {
            return new NetAction(type, data);
        }

    }
}
