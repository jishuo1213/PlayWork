package com.inspur.playwork.actions;

import android.util.SparseArray;


/**
 * Created by Fan on 15-11-5.
 */
public class DbAction {

    private final int actionType;

    private final SparseArray<Object> data;


    public DbAction(int actionType, SparseArray<Object> data) {
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

        public DbAction build() {
            return new DbAction(type, data);
        }

    }
}
