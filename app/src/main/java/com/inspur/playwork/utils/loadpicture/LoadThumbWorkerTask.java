package com.inspur.playwork.utils.loadpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PictureUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-10-12.
 */
public class LoadThumbWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> imageReference;
    public String imagePath;

    private int clipPictureSize;

    private BitmapCacheManager cacheManager;

    private Context context;

    public LoadThumbWorkerTask(ImageView imageView, BitmapCacheManager bitmapCacheManager) {
        this.imageReference = new WeakReference<>(imageView);
        cacheManager = bitmapCacheManager;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context = imageReference.get().getContext();
        clipPictureSize = DeviceUtil.dpTopx(context, 41);

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
                Log.i("onPostExecte", "onPostExecute: ");

                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imagePath = params[0];
        if (cacheManager == null) {
            //return PictureUtils.getPhonePictureBitmap(context, imagePath);
            return PictureUtils.getImageThumbnail(imagePath, clipPictureSize, clipPictureSize);
        }
        Bitmap bitmap = cacheManager.getBitmapFromMemoryCache(imagePath);
        if (bitmap == null) {
            if (imagePath.endsWith("apk")) {
                bitmap = PictureUtils.drawableToBitmap(FileUtil.getApkFileIcon(context, imagePath));
            }
//            else {
//                bitmap = PictureUtils.getImageThumbnail(imagePath, clipPictureSize, clipPictureSize);
//            }
            if (bitmap != null) {
                cacheManager.putBitmapIntoMemoryCache(imagePath, bitmap);
                return bitmap;
            }
        }
        return bitmap;
    }
}
