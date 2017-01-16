package com.inspur.playwork.utils.loadpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.inspur.playwork.utils.PictureUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-10-13.
 */
public class NormalLoadBitmapTask extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> imageReference;

    public String filePath;

    private Context context;

    private BitmapCacheManager cacheManager;

    public NormalLoadBitmapTask(ImageView imageView, BitmapCacheManager bitmapCacheManager) {
        this.imageReference = new WeakReference<>(imageView);
        context = imageView.getContext();
        cacheManager = bitmapCacheManager;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        } else {
            if (imageReference != null && bitmap == null)
                bitmap = PictureUtils.getDefaultAvatar(imageReference.get().getContext());
        }
        if (imageReference != null && bitmap != null) {
            ImageView imageView = imageReference.get();
            AsyncTask task = PictureUtils.getBitmapWorkerTask(imageView);
            if (this == task) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        filePath = params[0];
        String key = cacheManager.hashKeyForDisk(filePath);
        Bitmap bitmap = cacheManager.getBitmapFromDiskCache(key);
        if (bitmap == null) {
            bitmap = PictureUtils.getAvatar(context, filePath);
            if (bitmap != null) {
                cacheManager.putBitmapIntoDiskCache(key, bitmap);
                cacheManager.putBitmapIntoMemoryCache(filePath, bitmap);
                return bitmap;
            }
        }
        cacheManager.putBitmapIntoMemoryCache(filePath, bitmap);
        return bitmap;
    }
}
