package com.inspur.playwork.utils;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Fan on 15-12-11.
 */
public class O {
    public static void i(String tag, String info) {
        Log.i(tag, getCurrentThreadName() + info);
    }

    @NonNull
    private static String getCurrentThreadName() {
        return "currentthread====>" + Thread.currentThread().getName() + "\n";
    }

    public static void d(String tag, String info) {
        Log.d(tag, getCurrentThreadName() + info);
    }

    public static void e(String tag, String info) {
        Log.e(tag, getCurrentThreadName() + info);
    }

    public static void v(String tag, String info) {
        Log.v(tag, getCurrentThreadName() + info);
    }

    public static void w(String tag, String info) {
        Log.w(tag, getCurrentThreadName() + info);
    }
}
