package com.inspur.playwork.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Fan on 2016/12/29.
 */
public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "LifecycleListener";

    private ArrayList<Activity> createActivityList;
    private ArrayList<Activity> visableActivityList;
    private ArrayList<Activity> invisableActivityList;

    private static ActivityLifecycleListener ourInstance = new ActivityLifecycleListener();

    public static ActivityLifecycleListener getInstance() {
        return ourInstance;
    }

    private ActivityLifecycleListener() {
        createActivityList = new ArrayList<>();
        visableActivityList = new ArrayList<>();
        invisableActivityList = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated: " + activity.getClass().getName());
        createActivityList.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (createActivityList.contains(activity)) {
            Log.i(TAG, "onActivityResumed: " + activity.getClass().getName());
            createActivityList.remove(activity);
            visableActivityList.add(activity);
        } else if (invisableActivityList.contains(activity)) {
            Log.i(TAG, "onActivityResumed: " + activity.getClass().getName());
            Log.i(TAG, "onActivityResumed: " + activity.getClass().getName());
            invisableActivityList.remove(activity);
            visableActivityList.add(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (visableActivityList.contains(activity)) {
            Log.i(TAG, "onActivityStopped: " + activity.getClass().getName());
            visableActivityList.remove(activity);
            invisableActivityList.add(activity);
        } else if (createActivityList.contains(activity)) {
            Log.e(TAG, "onActivityStopped: an error appear" + activity.getClass().getName());
            createActivityList.remove(activity);
            invisableActivityList.add(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i(TAG, "onActivityDestroyed: " + activity.getClass().getName());
        Log.i(TAG, "onActivityDestroyed: " + invisableActivityList.contains(activity));
        if (invisableActivityList.contains(activity)) {
            invisableActivityList.remove(activity);
        } else if (visableActivityList.contains(activity)) {
            visableActivityList.remove(activity);
        } else if (createActivityList.contains(activity)) {
            createActivityList.remove(activity);
        }
    }

    public boolean isActivityVisable(Activity activity) {
        if (activity != null)
            Log.i(TAG, "isActivityVisable: " + activity.getClass().getName());
        return activity != null && visableActivityList.contains(activity);
    }
}
