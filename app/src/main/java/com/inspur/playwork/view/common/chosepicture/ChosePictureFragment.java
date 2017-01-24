package com.inspur.playwork.view.common.chosepicture;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.ImageFloderBean;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Fan on 15-11-12.
 */
public class ChosePictureFragment extends Fragment implements ChosePictureAdapter.ChosePictureListener, View.OnClickListener {

    public static final String TAG = "ChosePictureFragment";

    private static final String BUTTON_STRING = "button_string";
    private static final String MULTI_CHOSE = "multi";

    private boolean isUsedByFragment = false;//是否是fragment调用的，如果是就显示出toolbar
    private ImageButton backImageButton;
    private String buttonName = null;

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    private RecyclerView pictureRecyclerView;

    private View rootView;

    private ArrayList<ImageFloderBean> imageFloderList;

    private ImageHander hander;

    private HashSet<String> floderSet;

    private ImageFloderBean maxNumImageFloder;

    private TextView chooseDirTextView;
    private TextView sendTextView;

    private boolean isCanSend;

    private boolean multiChooseMode = false;

    private SelectedPicureListener listener;

//    private BitmapCacheManager cacheManager;

    private ArrayList<String> allImagePath;

    public ChosePictureFragment() {
    }

    public static Fragment getInstance(String btnName, boolean multiChooseMode) {
        Fragment fragment = new ChosePictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUTTON_STRING, btnName);
        bundle.putBoolean(MULTI_CHOSE, multiChooseMode);
        fragment.setArguments(bundle);
        return fragment;
    }


    public interface SelectedPicureListener {
        void onPictureSelect(String path);
    }

    public void setListener(SelectedPicureListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SelectedPicureListener) {
            listener = (SelectedPicureListener) activity;
        } else {
            isUsedByFragment = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        floderSet = new HashSet<>();
        imageFloderList = new ArrayList<>();
        allImagePath = new ArrayList<>();
        hander = new ImageHander(new WeakReference<>(this));
        Bundle args = getArguments();
        Log.i(TAG, "onCreate: " + (args == null));
        if (args != null) {
            buttonName = args.getString(BUTTON_STRING);
            multiChooseMode = args.getBoolean(MULTI_CHOSE);
            Log.i(TAG, "onCreate: " + buttonName + multiChooseMode);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                loadPictures();
                hander.sendEmptyMessage(0x01);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.layout_chose_picture_fragment, container, false);
        initView();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
//        cacheManager.flushDiskCache();
    }

    private void initView() {
        backImageButton = (ImageButton) rootView.findViewById(R.id.cp_back_btn);
        if (isUsedByFragment) {
            rootView.findViewById(R.id.cp_toolbar).setVisibility(View.VISIBLE);
        }
//        cacheManager = BitmapCacheManager.findOrCreateRetainFragment(getFragmentManager());
        pictureRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_images);
        pictureRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
        pictureRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        chooseDirTextView = (TextView) rootView.findViewById(R.id.id_choose_dir);
        chooseDirTextView.setOnClickListener(this);
        sendTextView = (TextView) rootView.findViewById(R.id.tv_send_picture);
        if (buttonName != null)
            sendTextView.setText(buttonName);
        sendTextView.setOnClickListener(this);
        backImageButton.setOnClickListener(this);
    }

    private void loadPictures() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            UItoolKit.showToastShort(getActivity(), "暂无外部存储");
            return;
        }
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (getActivity() == null)
            return;
        ContentResolver mContentResolver = getActivity().getContentResolver();

        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " DESC");
        if (mCursor != null && mCursor.getCount() > 0 && mCursor.moveToFirst()) {
            String firstImage = null;
            int columnIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                String imagePath = mCursor.getString(columnIndex);
                allImagePath.add(imagePath);
                if (firstImage == null)
                    firstImage = imagePath;
                File parentFile = new File(imagePath).getParentFile();
                if (parentFile == null || parentFile.length() <= 0)
                    continue;
                String dirPath = parentFile.getAbsolutePath();
                ImageFloderBean imageFloderBean;
                if (floderSet.contains(dirPath)) {
                    continue;
                } else {
                    floderSet.add(dirPath);
                    imageFloderBean = new ImageFloderBean();
                    imageFloderBean.firstPicturePath = firstImage;
                    imageFloderBean.floderPath = dirPath;
                    imageFloderBean.floderName = parentFile.getName();
                }

                String[] list = parentFile.list(filenameFilter);
                if (list == null) {
                    continue;
                }
                imageFloderBean.pictureName = Arrays.asList(list);
                imageFloderBean.pictureNum = imageFloderBean.pictureName.size();
                imageFloderList.add(imageFloderBean);

                if (maxNumImageFloder == null) {
                    maxNumImageFloder = imageFloderBean;
                } else {
                    maxNumImageFloder = maxNumImageFloder.pictureNum >= imageFloderBean.pictureNum ? maxNumImageFloder : imageFloderBean;
                }
            } while (mCursor.moveToNext());

            mCursor.close();

            floderSet.clear();
            floderSet = null;
        }
    }

    private void showPictures() {
        ChosePictureAdapter adapter = new ChosePictureAdapter(pictureRecyclerView);
        adapter.setListener(this);
        //adapter.setFloderBean(maxNumImageFloder);
//        adapter.setAllImageFloderList(imageFloderList);
        adapter.setAllImagePath(allImagePath);
//        adapter.setPictureCache(cacheManager);
        pictureRecyclerView.setAdapter(adapter);
    }

    private void showPictures(ImageFloderBean bean) {
        ((ChosePictureAdapter) pictureRecyclerView.getAdapter()).setFloderBean(bean);
    }

    @Override
    public void showSendText(boolean isShow) {
        if (isShow) {
            isCanSend = true;
            sendTextView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
        } else {
            isCanSend = false;
            sendTextView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.text_gray));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_picture:
                if (isCanSend) {
                    ArrayList<String> selectedImages = ((ChosePictureAdapter) pictureRecyclerView.getAdapter()).getSelectedItems();
                    if (multiChooseMode) {
                        for (String fp : selectedImages) {
                            listener.onPictureSelect(fp);
                        }
                        getActivity().onBackPressed();
                    } else {
                        listener.onPictureSelect(selectedImages.get(0));
                    }
                }
                break;
            case R.id.cp_back_btn:
                getActivity().onBackPressed();
                break;
        }
    }

    private static class ImageHander extends Handler {

        private WeakReference<ChosePictureFragment> pictureFragmentReference;

        ImageHander(WeakReference<ChosePictureFragment> pictureFragmentReference) {
            this.pictureFragmentReference = pictureFragmentReference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    pictureFragmentReference.get().showPictures();
                    break;
            }
        }
    }

    private FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File files = new File(dir, filename);
            filename = filename.toLowerCase();
            return files.length() > 0 && (filename.endsWith(".jpg")
                    || filename.endsWith(".png")
                    || filename.endsWith(".jpeg")
                    || filename.endsWith(".bmp"));
        }
    };

//    public void setMultiChooseMode(boolean multiChooseMode) {
//        this.multiChooseMode = multiChooseMode;
//    }

}
