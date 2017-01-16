package com.inspur.playwork.utils;

import android.content.Context;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.R;
import com.inspur.playwork.model.message.MyBitmapEntity;
import com.inspur.playwork.utils.loadfile.DownLoadPictureTask;
import com.inspur.playwork.utils.loadpicture.AsyncDrawable;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;
import com.inspur.playwork.utils.loadpicture.LoadBitmapWorkerTask;
import com.inspur.playwork.utils.loadpicture.LoadThumbWorkerTask;
import com.inspur.playwork.utils.loadpicture.NormalLoadBitmapTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.internal.Util;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PictureUtils {

    private static final String TAG = "PictureUtilsFan";

    private static final int VIMG_HEIGHT = 155;
    private static final int VIMG_WIDTH = 108;

    private static final int HIMG_HEIGHT = 108;
    private static final int HIMG_WIDTH = 155;


    /**
     * 缩放聊天的图片到合适的大小和高宽，其中大小限制是100kb，注意
     * 这个方法会把传进来的bitmap对象回收
     */

    public static Bitmap getChatMsgShowBitmap(String path, int degree) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;

        int destHeight, destWidth;

        if (degree == -1)
            degree = readPictureDegree(path);

        if (sourceHeight > sourceWidth) {
            destHeight = ResourcesUtil.getInstance().dpToPx(VIMG_HEIGHT);
            destWidth = ResourcesUtil.getInstance().dpToPx(VIMG_WIDTH);
        } else {
            destHeight = ResourcesUtil.getInstance().dpToPx(HIMG_HEIGHT);
            destWidth = ResourcesUtil.getInstance().dpToPx(HIMG_WIDTH);
        }
        if (sourceHeight <= destHeight)
            destHeight = sourceHeight;
        if (sourceWidth <= destWidth)
            destWidth = sourceWidth;

        options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);


        if (bitmap == null)
            return null;

        Bitmap scaledBitmap = null;

        try {
            scaledBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = destWidth / (float) options.outWidth;
        float ratioY = destHeight / (float) options.outHeight;
        float middleX = destWidth / 2.0f;
        float middleY = destHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        assert scaledBitmap != null;
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        if (degree != 0)
            return rotateBitmap(degree, scaledBitmap);
        else {
            return scaledBitmap;
        }
    }

    public static Point getImageWidthHeight(Context context, String path) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeFile(path, options);
//
//        options.inJustDecodeBounds = false;
//        int sourceHeight = options.outHeight;
//        int sourceWidth = options.outWidth;
//
//        int destWidth, destHeight;
//
//        if (sourceHeight > sourceWidth) {
//            destWidth = DeviceUtil.dpTopx(context, 108);
//            destHeight = (int) (sourceHeight * ((float) destWidth / sourceWidth));
//        } else {
//            destHeight = DeviceUtil.dpTopx(context, 108);
//            destWidth = (int) (sourceWidth * ((float) destHeight / sourceHeight));
//        }
//        return new Point(destWidth, destHeight);
        return getImageWidthHeight(path, DeviceUtil.dpTopx(context, 108));
    }

    public static Point getImageWidthHeight(String path, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;

        int destWidth, destHeight;

        Log.i(TAG, "getImageWidthHeight: " + sourceWidth + "-----" + sourceHeight);

        if (sourceHeight > sourceWidth) {
            destWidth = size > sourceWidth ? sourceWidth : size;
            destHeight = (int) (sourceHeight * ((float) destWidth / sourceWidth));
        } else {
            destHeight = size > sourceHeight ? sourceHeight : size;
            destWidth = (int) (sourceWidth * ((float) destHeight / sourceHeight));
        }
        return new Point(destWidth, destHeight);
    }

    public static Point getSourceImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;
        int[] imgSize = new int[2];
        imgSize[0] = sourceWidth;
        imgSize[1] = sourceHeight;
        int width = 0, height = 0;
        int shortSide = 960;
        int longSide = 1280;
        if (imgSize[0] <= imgSize[1]) {
            double scale = (double) imgSize[0] / (double) imgSize[1];
            if (scale <= 1.0 && scale > 0.5625) {
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
            } else if (scale <= 0.5625) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
            }
        } else {
            double scale = (double) imgSize[1] / (double) imgSize[0];
            if (scale <= 1.0 && scale > 0.5625) {
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
            } else if (scale <= 0.5625) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
            }
        }

        return new Point(width, height);
    }

    public static Bitmap getPhonePictureBitmap(Context context, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;
        int degree = readPictureDegree(path);

        options.inJustDecodeBounds = false;

        int destHeight, destWidth;
        int requieSize = DeviceUtil.getDeviceScreenWidth(context) / 3;

        Bitmap bitmap;
        Bitmap dstBmp;

        if (sourceHeight > sourceWidth) {
            destWidth = requieSize;
            destHeight = (int) (sourceHeight * ((float) requieSize / sourceWidth));
            options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);
            bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            dstBmp = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2 - bitmap.getWidth() / 2, bitmap.getWidth(), bitmap.getWidth());
        } else {
            destHeight = requieSize;
            destWidth = (int) (sourceWidth * ((float) requieSize / sourceHeight));
            options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);
            bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            dstBmp = /*bitmap.getHeight() >= destHeight ?
                    Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0, destHeight, destHeight) :*/
                    Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0, bitmap.getHeight(), bitmap.getHeight());
        }


        Bitmap scaledBitmap = resizeBitmap(requieSize, requieSize, dstBmp);
//        Bitmap scaledBitmap = dstBmp;

        if (degree != 0)
            return rotateBitmap(degree, scaledBitmap);
        return scaledBitmap;
    }

    public static Bitmap resizeBitmap(int destHeight, int destWidth, Bitmap bitmap) {
        Bitmap scaledBitmap;

        if (destHeight == bitmap.getHeight() && destWidth == bitmap.getWidth()) {
            return bitmap;
        }


        try {
            scaledBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            return null;
        }

        float ratioX = destWidth / (float) bitmap.getWidth();
        float ratioY = destHeight / (float) bitmap.getHeight();
        float middleX = destWidth / 2.0f;
        float middleY = destHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }


    public static int compressBitmap(String sourceFilePath, long byteCount, String destFileName) {
        Bitmap bitmap = BitmapFactory.decodeFile(sourceFilePath);
        File file = new File(sourceFilePath);
        Log.i(TAG, "compressBitmap: " + file.length() + "============" + byteCount);
        int options = byteCount > file.length() ? 100 : (int) (((float) byteCount / file.length()) * 100);
        int degree = readPictureDegree(sourceFilePath);

/*        if (degree != 0) {
            bitmap = rotateBitmap(degree, bitmap);
        }*/
        compressBitmap(bitmap, byteCount, destFileName, options);
        return degree;
    }

    public static void compressBitmap(Context context, String sourceFilePath, final String destFileName) {
//        Log.i(TAG, "compressBitmap: " + sourceFilePath);
//        Luban.get(context)
//                .load(new File(sourceFilePath))
//                .putGear(Luban.THIRD_GEAR)
//                .asObservable()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnError(new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        throwable.printStackTrace();
//                    }
//                })
//                .onErrorResumeNext(new Func1<Throwable, rx.Observable<? extends File>>() {
//                    @Override
//                    public rx.Observable<? extends File> call(Throwable throwable) {
//                        return rx.Observable.empty();
//                    }
//                })
//                .subscribe(new Action1<File>() {
//                    @Override
//                    public void call(File file) {
//                        // TODO 压缩成功后调用，返回压缩后的图片文件
//                        Log.i(TAG, "call: " + file.getAbsolutePath());
////                        FileUtil.renameFileNew(file.getAbsolutePath(), destFileName);
//                        try {
//                            FileOutputStream fos = new FileOutputStream(destFileName);
//                            FileInputStream fis = new FileInputStream(file);
//                            byte[] buf = new byte[4096];
//                            int read;
//                            while ((read = fis.read(buf)) > 0) {
//                                fos.write(buf, 0, read);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
        Point point = PictureUtils.getSourceImageWidthHeight(sourceFilePath);
        new Compressor.Builder(context.getApplicationContext())
                .setMaxWidth(point.x)
                .setMaxHeight(point.y).build()
                .compressToFileAsObservable(new File(sourceFilePath))
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        Log.i(TAG, "call: " + file.getAbsolutePath());
                        try {
                            FileOutputStream fos = new FileOutputStream(destFileName);
                            FileInputStream fis = new FileInputStream(file);
                            byte[] buf = new byte[4096];
                            int read;
                            while ((read = fis.read(buf)) > 0) {
                                fos.write(buf, 0, read);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private static void compressBitmap(Bitmap afterZoom, long byteCount, String destFileName, int startOptions) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        afterZoom.compress(Bitmap.CompressFormat.JPEG, startOptions, baos);
        while (baos.toByteArray().length > byteCount) {
            startOptions -= 5;
            if (startOptions == 0 || startOptions <= 0) {
                break;
            } else {
                baos.reset();
            }
            afterZoom.compress(Bitmap.CompressFormat.JPEG, startOptions, baos);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFileName);
            baos.writeTo(fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(fos);
        }
    }

    public static int readPictureDegree(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public static Bitmap rotateBitmap(int degree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        if (bm != bitmap)
            bitmap.recycle();
        return bm;
    }

/*    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, width, height);
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        return bitmap;
    }*/

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;

       /* final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.i(TAG, "inSampleSize" + inSampleSize);
        return inSampleSize;*/
    }


/*

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }
*/


    public static void saveBitmapToFile(Bitmap photoBitmap, String path) {
        //    BufferedOutputStream bos = null;
        //      try {
   /*         File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.flush();*/
        long pictureSize = 100 * 1024;
        compressBitmap(photoBitmap, pictureSize, path, 90);
/*        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(bos);
        }*/
    }

    public static AsyncTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


    public static boolean cancelPotentialWork(String filePath, ImageView imageView) {
        final AsyncTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData;
            if (bitmapWorkerTask instanceof LoadBitmapWorkerTask) {
                bitmapData = ((LoadBitmapWorkerTask) bitmapWorkerTask).imagePath;
            } else if (bitmapWorkerTask instanceof NormalLoadBitmapTask) {
                bitmapData = ((NormalLoadBitmapTask) bitmapWorkerTask).filePath;
            } else {
                bitmapData = ((LoadThumbWorkerTask) bitmapWorkerTask).imagePath;
            }
            if (bitmapData == null || !bitmapData.equals(filePath)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

/*    public static Bitmap getAvatar(String avaterPath) {
        return getAvatar(, avaterPath);
        //return BitmapFactory.decodeFile(avaterPath);
    }*/

    public static Bitmap getAvatar(Context context, String avaterPath) {
        int length = DeviceUtil.dpTopx(context, 45);
        return createRoundConerImage(context, getCertainSizeBitmap(avaterPath, length, length));
    }

    /**
     * 根据图片总数得到坐标列表
     *
     * @param context
     * @param count
     * @return
     */
    public static List<MyBitmapEntity> getBitmapEntitys(Context context, int count) {
        List<MyBitmapEntity> mList = new LinkedList<>();
        String value = PropertiesUtil.readData(context, String.valueOf(count), R.raw.nine_rect);
        assert value != null;
        String[] arr1 = value.split(";");
        for (String content : arr1) {
            String[] arr2 = content.split(",");
            MyBitmapEntity entity = null;
            for (String ignored : arr2) {
                entity = new MyBitmapEntity();
                entity.x = Float.valueOf(arr2[0]);
                entity.y = Float.valueOf(arr2[1]);
                entity.width = Float.valueOf(arr2[2]);
                entity.height = Float.valueOf(arr2[3]);
            }
            mList.add(entity);
        }
        return mList;
    }

    /**
     * 合成九宫格图片
     *
     * @param mEntityList
     * @param bitmaps
     * @return
     */
    public static Bitmap getCombineBitmaps(List<MyBitmapEntity> mEntityList, Bitmap... bitmaps) {
        Bitmap newBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < mEntityList.size(); i++) {
            Bitmap first = newBitmap;
            Bitmap second = bitmaps[i];
            PointF fromPoint = new PointF(mEntityList.get(i).x, mEntityList.get(i).y);
            if (first == null || second == null) {
                continue;
            }
            newBitmap = mixtureBitmap(first, second, fromPoint);
        }
        return newBitmap;
    }

    /**
     * Mix two Bitmap as one.
     *
     * @param first
     * @param second
     * @param fromPoint where the second bitmap is painted.
     * @return
     */
    public static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
//        if (first == null || second == null || fromPoint == null) {
//            return null;
//        }
        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
                first.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        return newBitmap;
    }

    /**
     * 头像下载
     *
     * @param filePath
     * @param url
     * @param userAvatar
     */
    public static void downLoadAvatar(String filePath, String url,
                                      ImageView userAvatar, BitmapCacheManager cacheManager) {
        DownLoadPictureTask downLoadPictureTask = new DownLoadPictureTask(userAvatar);
        downLoadPictureTask.execute(filePath, url);
    }

    /**
     * 加载头像
     *
     * @param path
     * @param imageView
     * @param arrayMap
     */
    public static void loadAvatarBitmap(String path, ImageView imageView, BitmapCacheManager arrayMap) {
//        if (PictureUtils.cancelPotentialWork(path, imageView)) {
//            NormalLoadBitmapTask task = new NormalLoadBitmapTask(imageView, arrayMap);
//            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//            imageView.setImageDrawable(drawable);
//            task.execute(path);
//        }
        Glide.with(imageView.getContext()).
                load(new File(path)).
                placeholder(R.drawable.icon_chat_default_avatar).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                into(imageView);
    }

    private static Bitmap defaultBitmap;

    public static Bitmap getDefaultAvatar(Context context) {
        if (defaultBitmap == null)
            defaultBitmap = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.icon_chat_default_avatar);
        return defaultBitmap;
    }


    private static Bitmap createRoundConerImage(Context context, Bitmap source) {
        if (source == null)
            return null;
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        //Bitmap target = Bitmap.createBitmap(DeviceUtil.dpTopx(context, 45), DeviceUtil.dpTopx(context, 45), Bitmap.Config.ARGB_8888);
        Bitmap target = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rect, DeviceUtil.dpTopx(context, 5), DeviceUtil.dpTopx(context, 5), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }


    /**
     * 将指定路径的图片按照传入的宽度缩放到合适的大小，
     * 本方法会根据传入的宽度
     *
     * @param src
     * @param num 当图片是横向时候，width将是图片的高度
     *            当图片是纵向时候，width是图片宽度
     * @return
     */
    public static Bitmap getScaleBitmap(String src, int num) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(src, options);
        int degree = readPictureDegree(src);

        options.inJustDecodeBounds = false;

        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;
        int destHeight, destWidth;

        if (sourceHeight > sourceWidth) {
            destWidth = num;
            destHeight = (int) (sourceHeight * ((float) num / sourceWidth));
        } else {
            destHeight = num;
            destWidth = (int) (sourceWidth * ((float) num / sourceHeight));
        }

        options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);

/*        if (options.inSampleSize < 2)
            options.inSampleSize = 2;*/
        Bitmap bitmap = BitmapFactory.decodeFile(src, options);

        if (bitmap == null)
            return null;

        Bitmap scaledBitmap = resizeBitmap(destHeight, destWidth, bitmap);

        if (degree != 0)
            return rotateBitmap(degree, scaledBitmap);
        return scaledBitmap;
    }


    public static Bitmap getShowBitmap(String src, int num) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(src, options);
        int degree = readPictureDegree(src);

        options.inJustDecodeBounds = false;

        int sourceHeight = options.outHeight;
        int sourceWidth = options.outWidth;
        int destHeight, destWidth;

        if (sourceHeight > sourceWidth) {
            destWidth = num;
            destHeight = (int) (sourceHeight * ((float) num / sourceWidth));
        } else {
            destHeight = num;
            destWidth = (int) (sourceWidth * ((float) num / sourceHeight));
        }

        options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);

   /*     if (options.inSampleSize < 2)
            options.inSampleSize = 2;*/
        Bitmap bitmap = BitmapFactory.decodeFile(src, options);

        if (bitmap == null)
            return null;

        Bitmap scaledBitmap = resizeBitmap(destHeight, destWidth, bitmap);

        if (degree != 0)
            return rotateBitmap(degree, scaledBitmap);
        return scaledBitmap;
    }


    private static Bitmap getCertainSizeBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;

        options.inSampleSize = calculateInSampleSize(options, width, height);

        return BitmapFactory.decodeFile(path, options);
    }


    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }
}
