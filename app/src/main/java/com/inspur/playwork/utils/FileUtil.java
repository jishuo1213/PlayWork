package com.inspur.playwork.utils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import okhttp3.internal.Util;

public class FileUtil {
    private static String SDCardRoot;

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    public static final String[][] MIME_TYPE = {
            {"application/vnd.android.package-archive", "apk"},
            {"application/octet-stream", "bin"},
            {"application/octet-stream", "class"},
            {"application/msword", "doc"},
            {"application/octet-stream", "exe"},
            {"application/x-gtar", "gtar"},
            {"application/x-gzip", "gz"},
            {"application/java-archive", "jar"},
            {"application/x-javascript", "js"},
            {"application/vnd.mpohun.certificate", "mpc"},
            {"application/vnd.ms-outlook", "msg"},
            {"application/pdf", "pdf"},
            {"application/vnd.ms-powerpoint", "pps"},
            {"application/vnd.ms-powerpoint", "ppt"},
            {"application/x-rar-compressed", "rar"},
            {"application/rtf", "rtf"},
            {"application/x-tar", "tar"},
            {"application/x-compressed", "tgz"},
            {"application/vnd.ms-works", "wps"},
            {"application/x-compress", "z"},
            {"application/zip", "zip"}
    };

    private static final String[][] MIME_MapTable = {
            //{后缀名，MIME类型} 
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".flv", "flv-application/octet-stream"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    private final static String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
    private final static String TAG = "fileUtilFan";

    public static String getSDCardRoot() {
        return SDCardRoot;
    }

    private static String cacheFilePath;

    private static String SDStateString;

    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 24) {
            File[] externalFileDirs = context.getExternalFilesDirs(null);
            File[] exiernalCacheDirs = context.getExternalCacheDirs();
            if (externalFileDirs.length > 1) {
                if (externalFileDirs[1] != null) {
                    SDCardRoot = externalFileDirs[1].getAbsolutePath() + File.separator;
                    cacheFilePath = exiernalCacheDirs[1].getAbsolutePath() + File.separator;
                } else if (externalFileDirs[0] != null) {
                    SDCardRoot = externalFileDirs[0].getAbsolutePath() + File.separator;
                    cacheFilePath = exiernalCacheDirs[0].getAbsolutePath() + File.separator;
                } else {
                    SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                    cacheFilePath = context.getCacheDir() + File.separator;
                }
            } else if (externalFileDirs.length == 1) {
                if (exiernalCacheDirs[0] != null) {
                    SDCardRoot = externalFileDirs[0].getAbsolutePath() + File.separator;
                    cacheFilePath = exiernalCacheDirs[0].getAbsolutePath() + File.separator;
                } else {
                    SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                    cacheFilePath = context.getCacheDir() + File.separator;
                }
            } else {
                SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                cacheFilePath = context.getCacheDir() + File.separator;
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            File[] externalFileDirs = context.getExternalFilesDirs(null);
            File[] exiernalCacheDirs = context.getExternalCacheDirs();
            if (externalFileDirs.length > 0) {
                SDCardRoot = externalFileDirs[0].getAbsolutePath() + File.separator;
            } else {
                SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
            }
            if (exiernalCacheDirs.length > 0) {
                cacheFilePath = exiernalCacheDirs[0].getAbsolutePath() + File.separator;
            } else {
                cacheFilePath = context.getCacheDir() + File.separator;
            }
        } else {
            SDCardRoot = System.getenv(ENV_SECONDARY_STORAGE);
            if (SDCardRoot != null) {
                SDCardRoot = SDCardRoot + File.separator + "Android" + File.separator + "data" + File.separator + context.getPackageName()
                        + File.separator + "files" + File.separator;
                cacheFilePath = SDCardRoot + File.separator + "Android" + File.separator + "data" + File.separator + context.getPackageName()
                        + File.separator + "cache" + File.separator;
            } else {
                SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                cacheFilePath = context.getCacheDir() + File.separator;
            }


            File fileRoot = new File(SDCardRoot);
            File cacheRoot = new File(cacheFilePath);
            if (!fileRoot.exists()) {
                boolean createResult = fileRoot.mkdirs();
                if (!createResult) {
                    Log.e(TAG, "创建文件夹失败");
                }
            }

            if (!cacheRoot.exists()) {
                boolean createResult = cacheRoot.mkdirs();
                if (!createResult) {
                    Log.e(TAG, "创建缓存文件夹失败");
                }
            }
        }

        Log.i(TAG, "init: " + SDCardRoot);
        Log.i(TAG, "init: " + cacheFilePath);

        File file = new File(SDCardRoot + "playWork");
        File cacheFile = new File(cacheFilePath + "playWork");
        Log.i(TAG, "init: is playwork exists" + file.getAbsolutePath() + file.exists());
        if (!file.exists()) {
            Log.i(TAG, "init: -------------");
            boolean result = file.mkdir();
            Log.i(TAG, "init: ------------- mk playwork dir" + result);
            if (!result)
                Log.e("fileUtil", "创建附件文件夹失败");
        }

        if (!cacheFile.exists()) {
            boolean result = cacheFile.mkdir();
            if (!result)
                Log.e("fileUtil", "创建缓存文件夹失败");
        }

        File fileAttachment = new File(getAttachmentPath());
        if (!fileAttachment.exists()) {
            Log.i(TAG, "init: -------------" + fileAttachment.getAbsolutePath());
            boolean result = fileAttachment.mkdir();
            Log.i(TAG, "init: " + result);
            if (!result)
                Log.e("fileUtil", "创建附件文件夹失败");
        }

        File mp = new File(getMailPath());
        if (!mp.exists()) {
            boolean result = mp.mkdir();
            if (!result)
                Log.e("fileUtil", "创建邮件文件夹失败");
        }

        File fileImage = new File(getImageFilePath());
        if (!fileImage.exists()) {
            boolean result = fileImage.mkdir();
            if (!result)
                Log.e("fileUtil", "创建图片文件夹失败");
        }
        File fileAvatar = new File(getAvatarFilePath());
        if (!fileAvatar.exists()) {
            boolean result = fileAvatar.mkdir();
            if (!result)
                Log.e("fileUtil", "创建头像文件夹失败");
        }

        File bitmapCache = new File(getDiskBitmapCachePath());
        if (!bitmapCache.exists()) {
            boolean result = bitmapCache.mkdir();
            if (!result)
                Log.e("fileUtil", "创建图片缓存文件夹失败");
        }
        File mailCache = new File(getMailCachePath());
        if (!mailCache.exists()) {
            boolean result = mailCache.mkdir();
            if (!result)
                Log.e("fileUtil", "创建邮件缓存文件夹失败");
        }
        File download = new File(getDownloadPath());
        if (!download.exists()) {
            boolean result = download.mkdir();
            if (!result)
                Log.e("fileUtil", "创建下载文件夹失败");
        }

        File log = new File(getLogPath());
        if (!log.exists()) {
            boolean result = log.mkdir();
            if (!result)
                Log.e("fileUtil", "创建日志文件夹失败");
        }

        File news = new File(getNewsFilePath());
        if (!news.exists()) {
            boolean result = news.mkdir();
            if (!result)
                Log.e("fileUtil", "创建新闻文件夹失败");
        }
    }

    public static Drawable getApkFileIcon(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(path, 0);

        if (pi == null)
            return null;
        // the secret are these two lines....
        pi.applicationInfo.sourceDir = path;
        pi.applicationInfo.publicSourceDir = path;
        //

        return pi.applicationInfo.loadIcon(pm);
    }

    public static ArrayList<String> getStorageList(Context context) {
        ArrayList<String> storages = new ArrayList<>();
        String sdCardRoot;
        if (Build.VERSION.SDK_INT >= 19) {
            File[] externalFileDirs = context.getExternalFilesDirs(null);
            if (externalFileDirs.length > 1) {
                if (externalFileDirs[1] != null) {
                    sdCardRoot = externalFileDirs[1].getAbsolutePath() + File.separator;
                    storages.add(externalFileDirs[0].getAbsolutePath() + File.separator);
                    storages.add(sdCardRoot);
                } else if (externalFileDirs[0] != null) {
                    sdCardRoot = externalFileDirs[0].getAbsolutePath() + File.separator;
                    storages.add(sdCardRoot);
                } else {
                    sdCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                    storages.add(sdCardRoot);
                }
            } else if (externalFileDirs.length == 1) {
                sdCardRoot = externalFileDirs[0].getAbsolutePath() + File.separator;
                storages.add(sdCardRoot);
            } else {
                sdCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
                storages.add(sdCardRoot);
            }
        } else {
            sdCardRoot = System.getenv(ENV_SECONDARY_STORAGE);
            if (sdCardRoot != null) {
                SDCardRoot = SDCardRoot + File.separator + "Android" + File.separator + "data" + File.separator + context.getPackageName()
                        + File.separator + "files" + File.separator;
            } else {
                SDCardRoot = context.getFilesDir().getAbsolutePath() + File.separator;
            }
        }
        return storages;
    }

    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    public static String[] getStorageDirectories(Context context) {

//        // Final set of paths
//        final Set<String> rv = new HashSet<String>();
//        // Primary physical SD-CARD (not emulated)
//        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
//        // All Secondary SD-CARDs (all exclude primary) separated by ":"
//        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
//        // Primary emulated SD-CARD
//        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
//        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
//            // Device has physical external storage; use plain paths.
//            if (TextUtils.isEmpty(rawExternalStorage)) {
//                // EXTERNAL_STORAGE undefined; falling back to default.
//                rv.add("/storage/sdcard0");
//            } else {
//                rv.add(rawExternalStorage);
//            }
//        } else {
//            // Device has emulated storage; external storage paths should have
//            // userId burned into them.
//            final String rawUserId;
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                rawUserId = "";
//            } else {
//                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//                final String[] folders = DIR_SEPORATOR.split(path);
//                final String lastFolder = folders[folders.length - 1];
//                boolean isDigit = false;
//                try {
//                    Integer.valueOf(lastFolder);
//                    isDigit = true;
//                } catch (NumberFormatException ignored) {
//                }
//                rawUserId = isDigit ? lastFolder : "";
//            }
//            // /storage/emulated/0[1,2,...]
//            if (TextUtils.isEmpty(rawUserId)) {
//                rv.add(rawEmulatedStorageTarget);
//            } else {
//                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
//            }
//        }
//        // Add all secondary storages
//        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
//            // All Secondary SD-CARDs splited into array
//            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
//            Collections.addAll(rv, rawSecondaryStorages);
//        }
//        return rv.toArray(new String[rv.size()]);

        Set<String> path = new HashSet<>();
        if (Build.VERSION.SDK_INT >= 19) {
            File[] externalFileDirs = context.getExternalFilesDirs(null);
            for (File file : externalFileDirs) {
                Log.i(TAG, "getStorageDirectories: " + (file == null ? "null" : file.getAbsolutePath()));
            }
            if (externalFileDirs.length > 1) {
                if (externalFileDirs[1] != null) {
                    path.add(getPath(externalFileDirs[1]));
                    if (externalFileDirs[0] != null) {
                        path.add(getPath(externalFileDirs[0]));
                    }
                } else if (externalFileDirs[0] != null) {
                    path.add(getPath(externalFileDirs[0]));
                }
            } else if (externalFileDirs.length == 1) {
                if (externalFileDirs[0] != null) {
                    path.add(getPath(externalFileDirs[0]));
                } else if (externalFileDirs[1] != null) {
                    path.add(getPath(externalFileDirs[1]));
                }
            }

            File file = Environment.getExternalStorageDirectory();
            if (file != null) {
                path.add(getPath(file));
            }
        } else {
            File file = Environment.getExternalStorageDirectory();
            if (file != null) {
                path.add(getPath(file));
            }
            String exterDir = System.getenv("SECONDARY_STORAGE");
            if (exterDir != null) {
                path.add(exterDir + File.separator);
            }
        }
        return path.toArray(new String[path.size()]);
    }

    private static String getPath(File file) {
        String expath0 = file.getAbsolutePath();
        int index0 = expath0.indexOf("Android");
        if (index0 > 0) {
            return expath0.substring(0, index0);
        } else {
            return expath0 + File.separator;
        }
    }

    public static String getFileType(String mimeType) {
        String type = "";
        for (String[] aMIME_MapTable : MIME_TYPE) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (mimeType.equals(aMIME_MapTable[0])) {
                type = aMIME_MapTable[1];
                break;
            }
        }
        return type;
    }

    public static String getFileSizeStr(long size) {
        float result;
        if (size <= KB) {
            return size + "B";
        } else if (size < MB) {
            result = size / KB;
            return result + "KB";
        } else if (size < GB) {
            result = size / MB;
            return result + "MB";
        } else {
            result = size / GB;
            return result + "GB";
        }
    }

    /**
     * 在SD卡上创建文件
     *
     * @param dir      目录路径
     * @param fileName
     * @return
     * @throws IOException
     */

    public static File createFileInSDCard(String dir, String fileName) throws IOException {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        file.createNewFile();
        return file;
    }


    public static String getCacheFilePath() {
        return cacheFilePath + "playWork" + File.separator;
    }

    /**
     * 在SD卡上创建目录
     *
     * @param dir 目录路径
     * @return
     */
    public static File creatSDDir(String dir) {
        File dirFile = new File(SDCardRoot + dir + File.separator);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return dirFile;
    }

    /**
     * 判断SD卡上的文件夹是否存在
     *
     * @param dir      目录路径
     * @param fileName 文件名称
     * @return
     */
    public static boolean isFileExist(String dir, String fileName) {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        return file.exists() && file.length() > 0;
    }


    public static boolean isDirExist(String dir) {
        File file = new File(dir);
        return file.exists();
    }

    /*
    * 根据文件的绝对路径 判断文件是否存在
    * */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    /**
     * 判断SD卡上的文件是否存在
     *
     * @param dir      目录路径
     * @param fileName 文件名称
     * @return File
     */
    public static File getFile(String dir, String fileName) {
        return new File(SDCardRoot + dir + File.separator + fileName);
    }


    public static String getImageFilePath() {
        return SDCardRoot + "playWork/images/";
    }

    public static String getAvatarFilePath() {
        return SDCardRoot + "playWork/avatar/";
    }

    public static String getNewsFilePath() {
        return SDCardRoot + "playWork/news/";
    }

    public static String getAttachmentPath() {
        return SDCardRoot + "playWork/attachment/";
    }

    public static String getDownloadPath() {
        return SDCardRoot + "playWork/download/";
    }

//    public static String getWYAttachmentPath(String userId) { return SDCardRoot + "playWork/"+userId+"vuAttachment/"; }
//
//    public static String getWYSaftyPath(String userId) { return SDCardRoot + "playWork/"+userId+"safty/";}

    public static String getDiskBitmapCachePath() {
        return cacheFilePath + "playWork/bitmap/";
    }

    public static String getMailCachePath() {
        return cacheFilePath + "playWork/mail/";
    }

    public static void clearMailCache() {
        File file = new File(getMailCachePath());
        File[] files = file.listFiles();
        if (files != null && files.length >= 0) {
            for (File _file : files) {
                deleteFile(_file.getPath());
            }
        }
    }

    public static String getLogPath() {
        return SDCardRoot + "playWork/log/";
    }

    public static String getMailPath() {
        return SDCardRoot + "playWork/vumail/";
    }


    private static String getCurrMailPath(String email) {
        return FileUtil.getMailPath() + (Base64.encodeToString(email.getBytes(), Base64.NO_WRAP)) + "/";
    }

    public static String getCurrMailAttachmentsPath(String email) {
        return getCurrMailPath(email) + ".attachments/";
    }

    public static String getServerMailFilePath(String email) {
        return getCurrMailPath(email) + ".serverEmail/";
    }

    public static String getLocalMailFilePath(String email) {
        return getCurrMailPath(email) + ".localEmail/";
    }

    public static String getCurrMailSafetyPath(String email) {
        return getCurrMailPath(email) + ".safety/";
    }

    public static void validateMailFolder(String cae) {

        File mf = new File(getCurrMailPath(cae));

        if (!mf.exists()) {
            boolean result = mf.mkdir();
            if (!result)
                Log.e("fileUtil", "创建" + cae + "文件夹失败");
        }

        File maf = new File(getCurrMailAttachmentsPath(cae));

        if (!maf.exists()) {
            boolean result = maf.mkdir();
            if (!result)
                Log.e("fileUtil", "创建" + cae + "-a文件夹失败");
        }

        File msfp = new File(getServerMailFilePath(cae));

        if (!msfp.exists()) {
            boolean result = msfp.mkdir();
            if (!result)
                Log.e("fileUtil", "创建" + cae + "-sf文件夹失败");
        }

        File mlfp = new File(getLocalMailFilePath(cae));

        if (!mlfp.exists()) {
            boolean result = mlfp.mkdir();
            if (!result)
                Log.e("fileUtil", "创建" + cae + "-lf文件夹失败");
        }

        File msf = new File(getCurrMailSafetyPath(cae));

        if (!msf.exists()) {
            boolean result = msf.mkdir();
            if (!result)
                Log.e("fileUtil", "创建" + cae + "-s文件夹失败");
        }
    }

    /***
     * 获取SD卡的剩余容量,单位是Byte
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDAvailableSize() {
        if (SDStateString.equals(Environment.MEDIA_MOUNTED)) {
            // 取得sdcard文件路径
            File pathFile = Environment
                    .getExternalStorageDirectory();
            android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
            // 获取SDCard上每个block的SIZE
            long nBlocSize, nAvailaBlock;
            if (Build.VERSION.SDK_INT >= 18) {
                nBlocSize = statfs.getBlockSizeLong();
                nAvailaBlock = statfs.getAvailableBlocksLong();
            } else {
                nBlocSize = statfs.getBlockSize();
                nAvailaBlock = statfs.getAvailableBlocks();
            }
            return nAvailaBlock * nBlocSize;
        }
        return 0;
    }

    /**
     * 重命名文件，第一个参数是原文件的路径
     * 第二个是新文件的名字,不带路径
     *
     * @param oldPath
     * @param newPath
     * @return
     */

    public static File renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            return null;
        }
        File newFile = new File(oldPath.replaceAll(oldFile.getName(), newPath + ".png"));
        if (oldFile.renameTo(newFile))
            return newFile;
        return null;
    }

    public static File renameFileNew(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            return null;
        }
        File newFile = new File(newPath);
        if (oldFile.renameTo(newFile))
            return newFile;
        return null;
    }

    public static boolean copyFile(String sourcePath, String destPath) {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(destPath);
            fis = new FileInputStream(sourcePath);
            byte[] buf = new byte[4096];
            int read;
            while ((read = fis.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            Util.closeQuietly(fis);
            Util.closeQuietly(fos);
        }
    }

    public static Intent getOpenFileIntent(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;
        String mime = getMimeType(Uri.fromFile(file).toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(context, "com.inspur.playwork.fileprovider", file);
            Log.i(TAG, "getOpenFileIntent: " + uri.toString());
            intent.setDataAndType(uri, mime);
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, mime);
        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    public static String getMimeType(String url) {
        String type;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url).toLowerCase();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        type = mime.getMimeTypeFromExtension(extension);
        return type;
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中 ,从网络上读取图片
     */
    public static boolean write2SDFromInput(String dir, String fileName, InputStream input) {

        File file;
        OutputStream output = null;
        try {
            int size = input.available();
            // 拥有可读可写权限，并且有足够的容量
            if (SDStateString.equals(Environment.MEDIA_MOUNTED)
                    && size < getSDAvailableSize()) {
                creatSDDir(dir);
                file = createFileInSDCard(dir, fileName);
                output = new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[4 * 1024];
                int temp;
                while ((temp = input.read(buffer)) != -1) {
                    output.write(buffer, 0, temp);
                }
                output.flush();
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        } finally {
            try {
                if (output != null) {
                    input.close();
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 保存证书文件
     *
     * @param fi
     * @param outF
     */
    public static boolean saveZSFile(FileInputStream fi, File outF) {
//        boolean cer = false;
        FileOutputStream fo;
        FileChannel in;
        FileChannel out;
        try {
            if (!outF.exists()) {//如果没有 则创建一个
                outF.createNewFile();
            }
            fo = new FileOutputStream(outF);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
            in.close();
            fo.close();
            out.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "saveZSFile error --->");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "saveZSFile error --->");
            e.printStackTrace();
            return false;
        }
//            boolean d = deleteFile("", fileName);//删除根目录下的文件
    }


    /*递归删除文件夹下的文件*/
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return file.delete();
            }
            for (File _file : files) {
                deleteFile(_file.getPath());
            }
            return file.delete();
        }
        return false;
    }

    public static void getAvatarDirFiles(ArrayMap<String, Long> avatarMap) {
        File file = new File(getAvatarFilePath());
        String[] avatars = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".jpg")
                        || filename.endsWith(".png")
                        || filename.endsWith(".jpeg");
            }
        });

        for (String name : avatars) {
//            String[] fileName = name.substring(0, name.indexOf(".")).split("-");
//            if (fileName.length < 2) {
//                deleteFile(getAvatarFilePath() + name + ".png");
//                continue;
//            }
            String[] fileName = new String[2];
            if (name.lastIndexOf("-") == -1) {
                deleteFile(getAvatarFilePath() + name + ".png");
                continue;
            } else {
                fileName[0] = name.substring(0, name.lastIndexOf("-"));
                fileName[1] = name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf("."));
            }
            long currentAvatarId = Long.parseLong(fileName[1]);
            if (avatarMap.containsKey(fileName[0])) {
                long avatarId = avatarMap.get(fileName[0]);
                if (avatarId >= currentAvatarId) {
                    deleteFile(getAvatarFilePath() + fileName[0] + "-" + currentAvatarId + ".png");
                } else if (avatarId < currentAvatarId) {
                    deleteFile(getAvatarFilePath() + fileName[0] + "-" + avatarId + ".png");
                    avatarMap.put(fileName[0], Long.parseLong(fileName[1]));
                }
            } else {
                avatarMap.put(fileName[0], Long.parseLong(fileName[1]));
            }
        }
    }

    public static void main(String[] argc) {
//        String[] avatars = {"aaa.jpg", "bbb.jpg", "ccc-124.jpg", "ddd-125.jpg"};
//        for (String name : avatars) {
//            String[] fileName = name.substring(0, name.indexOf(".")).split("-");
//            System.out.println(fileName.length);
//            System.out.println(fileName[0] + "    " + fileName[1]);
//        }
//        String res = "aaaaa/Android/aaaa";
//        int index = res.indexOf("Android");
//        System.out.println(res.substring(0, index));
//        ArrayList<String> lsit = new ArrayList<>();
//        lsit.add("aaa");
//        lsit.add("bbb");
//        Iterator<String> it = lsit.iterator();
//        while (it.hasNext()){
//            String s = it.next();
//            System.out.println(s);
//            it.remove();
//        }
//    }

//        String zhengze = "\\[[\\/][\\:].+\\]";
//        String zhengze = "\\[[/][:][^]]+\\]";
////        String zhengze =  "\\[[/:]+[^\\w]+\\]|\\[[/:]+[\\w]+\\]|\\[[/:]+[<\\w+>]+\\]";
//        Pattern patten = Pattern.compile(zhengze, Pattern.MULTILINE);
////        Matcher matcher = patten.matcher("a[/:turn][aa[/::)]aaa[/::)]aa[/:<O>]a[/::)]");
//        Matcher matcher = patten.matcher("asdfaadfasdfqwerqwesd[/:12][/:<>]][/:<o>] \n" +
//                "[/::z]df[/::`(][[[sdf [lllldjdjhjsdnihao [sdfasdf ]dsf ]ds fsd [sdfasdf] sdfsdssdf [sdfsdf]]]");
//        int i = 0 ;
//        while (matcher.find()) {
//            String key = matcher.group();
//            i++;
//            if (matcher.start() < 0) {
//                continue;
//            }
//            System.out.println(key);
//        }
//        System.out.println(i);
//
//    }

        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream("/home/fan/wechat-emoticons-master/encode_mapping.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<String> arrayList = new ArrayList<>();
        JSONObject jsonObject = null;
        char[] buffer = new char[1024 * 4];
        try {
            int count;
            while ((count = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, count);
            }
            reader.close();
            jsonObject = new JSONObject(builder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject emoji = jsonObject.optJSONObject(key);
//                emojiMap.put("[" + key + "]", emoji.optString("name"));
                arrayList.add(emoji.optString("name"));
            }
        }

        File file = new File("/home/fan/wechat-emoticons-master");
        File[] files = file.listFiles();
        ArrayList<String> no = new ArrayList<>();
        for (File t_file : files) {
            String name = t_file.getName();
            if (arrayList.contains(name))
                continue;
            no.add(name);
        }
        for (String name : no) {
            System.out.println(name);
        }
    }

    public static void writeContentToFile(String content, String filePath) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(content);
        } finally {
            Util.closeQuietly(writer);
        }
    }

    public static String readContent(String filePath) throws IOException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            Util.closeQuietly(reader);
        }

    }
}