package com.inspur.playwork.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * Created by fan on 15-8-7.
 */
public class DeviceUtil {

    private static String TAG = "DeviceUtilFan";


    public static int getFlipDistance(Context context) {
        return ViewConfiguration.get(context.getApplicationContext()).getScaledTouchSlop();
    }

    public static int getMinFlingVelocity(Context context) {
        return ViewConfiguration.get(context.getApplicationContext()).getScaledMinimumFlingVelocity();
    }

    public static int getMaxFlingVelocity(Context context) {

        return ViewConfiguration.get(context.getApplicationContext()).getScaledMaximumFlingVelocity();
    }

    public static int dpTopx(Context context, int dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dpTopx(Context context, float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getDeviceScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static int getDeviceScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point.y;
    }

    public static int pxTodp(Context context, int pxValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /*
    * 手机震动
    * */
    public static void vibrate(Context context, long milliseconds) {

        if (milliseconds == 0L) {
            milliseconds = 500L;
        }
        Vibrator vib = (Vibrator) context.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /*
    * 手机提示音
    * */
    public static void beep(Context context, int count) {
        Uri ringtone = RingtoneManager.getDefaultUri(2);
        Ringtone notification = RingtoneManager.getRingtone(context.getApplicationContext(), ringtone);

        if (notification == null)
            notification = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(4));
        if (notification != null)
            for (long i = 0L; i < count; i += 1L) {
                notification.play();
                long timeout = 5000L;
                while ((notification.isPlaying()) && (timeout > 0L)) {
                    timeout -= 100L;
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    public static boolean getPermission(Activity context, String premisson, int requestCode) {
        int hasPermission = ContextCompat.checkSelfPermission(context,
                premisson);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(context, premisson)) {
                ActivityCompat.requestPermissions(context, new String[]{premisson}, requestCode);
                return false;
            }
            ActivityCompat.requestPermissions(context, new String[]{premisson},
                    requestCode);
            return false;
        }
        return true;
    }

}
