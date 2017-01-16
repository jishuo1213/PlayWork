package com.inspur.playwork.model.common;

import com.bumptech.glide.request.Request;

/**
 * Created by fan on 16-11-1.
 */
public class StoresRequest implements Request {
    private static final String TAG = "StoresRequest";

    private enum Status {
        /**
         * Created but not yet running.
         */
        PENDING,
        /**
         * In the process of fetching media.
         */
        RUNNING,
        /**
         * Waiting for a callback given to the Target to be called to determine target dimensions.
         */
        WAITING_FOR_SIZE,
        /**
         * Finished loading media successfully.
         */
        COMPLETE,
        /**
         * Failed to load media, may be restarted.
         */
        FAILED,
        /**
         * Cancelled by the user, may not be restarted.
         */
        CANCELLED,
        /**
         * Cleared by the user with a placeholder set, may not be restarted.
         */
        CLEARED,
        /**
         * Temporarily paused by the system, may be restarted.
         */
        PAUSED,
    }

    private Status status;

    @Override
    public void begin() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isPaused() {
        return status == Status.PAUSED;
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    @Override
    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    @Override
    public boolean isResourceSet() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    @Override
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    @Override
    public void recycle() {

    }
}
