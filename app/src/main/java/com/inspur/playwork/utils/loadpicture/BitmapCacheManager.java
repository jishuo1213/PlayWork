package com.inspur.playwork.utils.loadpicture;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.utils.DiskLruCache;
import com.inspur.playwork.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Fan on 15-11-19.
 */
public class BitmapCacheManager extends Fragment {

    private static final String TAG = "BitmapCacheManager";


    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = (int) Runtime.getRuntime().maxMemory() / 2;

    private LruCache<String, Bitmap> bitmapLruCache;

    public static BitmapCacheManager findOrCreateRetainFragment(FragmentManager fm) {
        BitmapCacheManager fragment = (BitmapCacheManager) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new BitmapCacheManager();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File cacheDir = new File(FileUtil.getDiskBitmapCachePath());

        bitmapLruCache = ((PlayWorkApplication) getActivity().getApplication()).getImageBitmapCache();

        new InitDiskCacheTask().execute(cacheDir);

        setRetainInstance(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    private class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                try {
                    mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(), 1, DISK_CACHE_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }


/*    public Bitmap getBitmapFromCache(String path) {
        String md5key = hashKeyForDisk(path);
        Bitmap memoryBitmap = getBitmapFromMemoryCache(md5key);
        if (memoryBitmap == null) {
            Bitmap diskBitmap = getBitmapFromDiskCache(md5key);
            if (diskBitmap != null) {
                putBitmapIntoMemoryCache(md5key, diskBitmap);
                return diskBitmap;
            } else {
                return null;
            }
        } else {
            return memoryBitmap;
        }
    }*/

    public Bitmap getBitmapFromMemoryCache(String key) {
        return bitmapLruCache.get(key);
    }

    /**
     * 将Bitmap放入内存中，注意：这里的key是MD5编码之后的
     */
    public void putBitmapIntoMemoryCache(String key, Bitmap bitmap) {
        if (bitmap == null)
            return;
        bitmapLruCache.put(key, bitmap);
    }

    /**
     * 从硬盘缓存获取bitmap，key是MD5之后的key
     */
    public Bitmap getBitmapFromDiskCache(String key) {

        synchronized (mDiskCacheLock) {

            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            DiskLruCache.Snapshot snapShot;
            try {
                snapShot = mDiskLruCache.get(key);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if (snapShot != null) {
                InputStream inputStream = snapShot.getInputStream(0);
                return BitmapFactory.decodeStream(inputStream);
            }
        }
        return null;
    }


    public void putBitmapIntoDiskCache(String key, Bitmap bitmap) {
        if (bitmap == null)
            return;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void flushDiskCache() {
//        try {
//            if (mDiskLruCache != null)
//                mDiskLruCache.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void closeDiskCache() {
//        if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
//            try {
//                mDiskLruCache.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public int getAppVersion() {
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
