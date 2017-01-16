package com.inspur.playwork.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Fan on 2016/12/23.
 */
class LifecycleCallbacksAdapter implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "LifecycleCallbacksAdapter";

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
