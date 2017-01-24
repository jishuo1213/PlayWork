package com.inspur.playwork.view.common.viewimage;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.viewimage.view.ZoomImageView;

import java.io.File;
import java.io.FilenameFilter;

public class ImageViewActivity extends BaseActivity {

    private static final String TAG = "ImageViewActivityFan";

    private ImageView[] mImageViews;
    String dir = FileUtil.getSDCardRoot() + AppConfig.IMAGE_DIR;
    private File[] files;
    private int screenWidth;
//    private BitmapCacheManager cacheManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String groupId = getIntent().getStringExtra("groupId");
        dir += groupId;

        String imagePath = getIntent().getStringExtra("imagePath");
        Log.i(TAG, "onCreate: " + imagePath);

        if (!TextUtils.isEmpty(imagePath)) {
            imagePath = imagePath.split(groupId)[1].substring(1);
        }
//        cacheManager = BitmapCacheManager.findOrCreateRetainFragment(getFragmentManager());

        setContentView(R.layout.layout_viewpage);
        screenWidth = DeviceUtil.getDeviceScreenWidth(this);

        files = getFiles();
        int showIndex = 0;
        if (files != null) {
            mImageViews = new ImageView[files.length];
            for (int i = 0; i < files.length; i++) {
                if (files[i].getAbsolutePath().contains(imagePath)) {
                    /*File f = files[0];
                    files[0] = files[i];
                    files[i] = f;*/
                    showIndex = i;
                    break;
                }
            }
        } else {
            mImageViews = new ImageView[3];
        }
        ViewPager mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZoomImageView imageView;
                if (mImageViews[position] == null) {
                    imageView = new ZoomImageView(getApplicationContext());
                    mImageViews[position] = imageView;
                } else {
                    imageView = (ZoomImageView) mImageViews[position];
                }
                String filePath;
                if (files.length == 1) {
                    filePath = files[0].getAbsolutePath();
                } else {
                    filePath = files[position].getAbsolutePath();
                }

                //获取处理过的 bitmap 对象
//                Bitmap afterZoom = getOrSetBitmapCache(filePath);
//                imageView.setImageBitmap(afterZoom);

                Point point = PictureUtils.getImageWidthHeight(filePath, screenWidth);

                Log.i(TAG, "instantiateItem: " + point.x + "--" + point.y);
                Glide.with(ImageViewActivity.this).
                        load(new File(filePath)).
                        override(point.x, point.y).
                        into(imageView);

                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                //PictureUtils.cleanImageView(mImageViews[position]);
                container.removeView(mImageViews[position]);
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mImageViews.length;
            }
        });
        mViewPager.setCurrentItem(showIndex);
    }

//    protected Bitmap getOrSetBitmapCache(String imagePath) {
//        if (cacheManager == null) {
//            //return PictureUtils.getPhonePictureBitmap(context, imagePath);
//            return PictureUtils.getShowBitmap(imagePath, screenWidth);
//        }
//        String imageKey = "Preview_";
//        String key = cacheManager.hashKeyForDisk(imageKey + imagePath);
//        // 获取内存中的图片缓存
//        Bitmap bitmap1 = cacheManager.getBitmapFromMemoryCache(key);
//        if (bitmap1 == null) {
//            // 获取磁盘中的缓存图片
//            bitmap1 = cacheManager.getBitmapFromDiskCache(key);
//            if (bitmap1 == null) {
//                // 根据原始路径生成图片并缓存起来
//                bitmap1 = PictureUtils.getShowBitmap(imagePath, screenWidth);
//                cacheManager.putBitmapIntoMemoryCache(key, bitmap1);
//                cacheManager.putBitmapIntoDiskCache(key, bitmap1);
//            } else {
//                // 内存中没有图片 进行设置缓存
//                cacheManager.putBitmapIntoMemoryCache(key, bitmap1);
//            }
//        }
//        return bitmap1;
//    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public File[] getFiles() {
        File file = new File(dir);
        if (file.exists()) {
            return file.listFiles(new FilenameFilter() {
                public boolean accept(File file, String name) {
                    return name.toLowerCase().endsWith(".png");
                }
            });
        } else {
            return null;
        }
    }
}
