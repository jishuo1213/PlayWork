package com.inspur.playwork.utils.loadpicture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;


import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-10-12.
 */
public class AsyncDrawable extends BitmapDrawable {
    private WeakReference<AsyncTask> workerTaskWeakReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, AsyncTask workerTaskWeakReference) {
        super(res, bitmap);
        this.workerTaskWeakReference = new WeakReference<>(workerTaskWeakReference);
    }

    public AsyncTask getBitmapWorkerTask() {
        return workerTaskWeakReference.get();
    }
}
