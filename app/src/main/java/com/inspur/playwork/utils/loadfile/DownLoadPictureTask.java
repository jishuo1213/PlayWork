package com.inspur.playwork.utils.loadfile;

import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.R;
import com.inspur.playwork.core.ActivityLifecycleListener;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Fan on 15-10-13.
 */
public class DownLoadPictureTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "DownLoadPictureTask";

    public String filePath, downLoadUrl;

//    private ImageView imageView;

    private WeakReference<ImageView> imageViewWeakReference;

    private ArrayList<String> downloadingUrls;

    public DownLoadPictureTask(ImageView imageView, BitmapCacheManager bitmapCacheManage, ArrayList<String> downloadingUrls) {
//        this.imageView = imageView;
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
        this.downloadingUrls = downloadingUrls;
    }

    public DownLoadPictureTask(ImageView imageView) {
//        this.imageView = imageView;
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
//            if (PictureUtils.cancelPotentialWork(filePath, imageViewWeakReference.get())) {
//                NormalLoadBitmapTask task = new NormalLoadBitmapTask(imageView, bitmapCacheManager);
//                AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//                imageView.setImageDrawable(drawable);
//                task.execute(filePath);
//            }
//            AppCompatActivity activity = (AppCompatActivity) imageViewWeakReference.get().getContext();
//            activity.isDestroyed();
            if (imageViewWeakReference.get() != null && ActivityLifecycleListener.getInstance().isActivityVisable(CommonUtils.getViewActivity(imageViewWeakReference.get()))) {
                Glide.with(imageViewWeakReference.get().getContext()).
                        load(new File(filePath)).
                        placeholder(R.drawable.icon_chat_default_avatar).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        into(imageViewWeakReference.get());
            }
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        filePath = params[0];
        downLoadUrl = params[1];
        try {
            boolean result = OkHttpClientManager.getInstance().downloadFile(downLoadUrl, filePath);
            if (downloadingUrls != null)
                downloadingUrls.remove(downLoadUrl);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            File file = new File(filePath);
            file.delete();
            return false;
        }
    }
}
