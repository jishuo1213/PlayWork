package com.inspur.playwork.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;


public class UItoolKit {

    public static void showToastShort(Context context, String info) {
        if (context == null || info == null || "".equals(info))
            return;

        Toast mToast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showToastShort(Context context, String info, int durtion) {
        if (context == null || info == null || "".equals(info))
            return;

        Toast mToast = Toast.makeText(context, info, durtion);
        mToast.show();
    }

    public static void showToast(final Activity activity, final String word, final long time) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, time);
            }
        });
    }
}
