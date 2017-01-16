package com.inspur.playwork.utils.loadpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PictureUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-10-12.
 */
public class LoadBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> imageReference;
    public String imagePath;
    private Context context;

    private int clipPictureSize;

    private BitmapCacheManager cacheManager;

    public LoadBitmapWorkerTask(ImageView imageView, BitmapCacheManager bitmapCacheManager) {
        this.imageReference = new WeakReference<>(imageView);
        cacheManager = bitmapCacheManager;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context = imageReference.get().getContext();
        clipPictureSize = DeviceUtil.getDeviceScreenWidth(context) - DeviceUtil.dpTopx(context, 40);

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageReference != null && bitmap != null) {
            ImageView imageView = imageReference.get();
            AsyncTask task = PictureUtils.getBitmapWorkerTask(imageView);
            if (this == task) {
                //messageBean.imgBitMap = bitmap;
                //cache.put(imagePath, bitmap);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imagePath = params[0];
        if (cacheManager == null) {
            //return PictureUtils.getPhonePictureBitmap(context, imagePath);
            return PictureUtils.getScaleBitmap(imagePath, clipPictureSize);
        }
        String key = cacheManager.hashKeyForDisk(imagePath);
        Bitmap bitmap = cacheManager.getBitmapFromDiskCache(key);
        if (bitmap == null) {
            if (params.length > 1) {
                bitmap = PictureUtils.getPhonePictureBitmap(context, imagePath);
            } else {
                bitmap = PictureUtils.getChatMsgShowBitmap(imagePath,-1);
            }
            if (bitmap != null) {
                cacheManager.putBitmapIntoMemoryCache(imagePath, bitmap);
                cacheManager.putBitmapIntoDiskCache(key, bitmap);
                return bitmap;
            }
        }
        cacheManager.putBitmapIntoMemoryCache(imagePath, bitmap);
        return bitmap;
    }
}
