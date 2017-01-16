package com.inspur.playwork.actions;

import android.util.SparseArray;

/**
 * Created by fan on 15-8-22.
 */
public class UpdateUIAction {
    private final int actionType;

    private final SparseArray<Object> data;


    public UpdateUIAction(int actionType, SparseArray<Object> data) {
        this.actionType = actionType;
        this.data = data;
    }

    public static Builder buildType(int type) {
        return new Builder().with(type);
    }

    public int getActionType() {
        return actionType;
    }

    public SparseArray<Object> getActionData() {
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

        public UpdateUIAction build() {
            return new UpdateUIAction(type, data);
        }

    }
}
