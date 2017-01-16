package com.inspur.playwork.broadcastreciver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.inspur.playwork.core.PlayWorkApplication;

import java.util.List;

/**
 * Created by Fan on 16-3-11.
 */
public class NotifyMsgReciver extends BroadcastReceiver {

    private static final String TAG = "NotifyMsgReciverFan";

    public static final String NOTIFY_MSG = "com.inspur.playwork.broadcastreciver.recivemsgnotify";
    public static final String MSG_GROUP_ID = "recivemsggroupid";

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayMap<String, Integer> notificationMap = ((PlayWorkApplication) context.getApplicationContext()).getNotificationMap();
        String groupId = intent.getStringExtra(MSG_GROUP_ID);
        notificationMap.remove(groupId);
        Log.i(TAG, "onReceive: " + groupId);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            Log.i(TAG, "onReceive: " + tasks.size());
            for (ActivityManager.AppTask task : tasks) {
                task.moveToFront();
            }
        } else {
            final List<ActivityManager.RunningTaskInfo> recentTasks = am.getRunningTasks(Integer.MAX_VALUE);

            for (int i = 0; i < recentTasks.size(); i++) {
                Log.d("Executed app", "Application executed : "
                        + recentTasks.get(i).baseActivity.toShortString()
                        + "\t\t ID: " + recentTasks.get(i).id + "");
                // bring to front
                if (recentTasks.get(i).baseActivity.getPackageName().equals("com.inspur.playwork")) {
                    am.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                }
            }
        }
    }
}
