package com.inspur.playwork.view.profile.my;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.ThreadPool;

import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-11-18.
 */
public class ClipPictureFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ClipPictureFragment";

    private static final String IMAGE_PATH = "imagefilepath";

    private String imagePath;

    private View clipView;

    private ClipListener listener;

    private Handler handler;

    public interface ClipListener {
        void onClipClick(Bitmap bitmap);
    }

    public static Fragment getInstance(String filePath) {
        Fragment clipPictureFragment = new ClipPictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_PATH, filePath);
        clipPictureFragment.setArguments(bundle);
        return clipPictureFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ClipListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePath = getArguments().getString(IMAGE_PATH);
        handler = new ClipHandler(new WeakReference<>(this));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_activity_clipicture, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        ImageButton confirmBtn = (ImageButton) v.findViewById(R.id.btn_confrim);
        clipView = v.findViewById(R.id.clip_picture_layout);
        confirmBtn.setOnClickListener(this);
        new BitmapWorkerTask(((ClipImageLayout) clipView).getmZoomImageView()).execute(imagePath);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confrim) {
            ((MyInfoActivity) getActivity()).showProgressDialog();
            //listener.onClipClick(bitmap);
            ThreadPool.exec(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = ((ClipImageLayout) clipView).clip();
                    Message message = handler.obtainMessage(1, bitmap);
                    handler.sendMessage(message);
                }
            });
        }
    }

    private void doClipClick(Bitmap bitmap) {
        listener.onClipClick(bitmap);
    }


    private static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private String imagePath;

        private Context context;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
            context = imageView.getContext();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imagePath = params[0];
            return PictureUtils.getScaleBitmap(imagePath, DeviceUtil.getDeviceScreenWidth(context) - DeviceUtil.dpTopx(context, 40));
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private static class ClipHandler extends Handler {

        private WeakReference<ClipPictureFragment> reference;

        public ClipHandler(WeakReference<ClipPictureFragment> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            reference.get().doClipClick(bitmap);
        }
    }
}
