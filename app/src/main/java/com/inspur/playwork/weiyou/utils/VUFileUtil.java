package com.inspur.playwork.weiyou.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by 孙 on 2016/2/15 0015.
 */
public class VUFileUtil {

    private static final String TAG = "VUFileUtil";
    public static DecimalFormat df = new DecimalFormat("#.00");

    public static String convertFileSize(long am_size){
        return ((am_size > 1024 * 1024) ? df.format((double)am_size / 1024 / 1024) + "MB" : df.format((double)am_size / 1024) + "KB");
    }

    public static long getFileSize(String filePath) throws IOException {
        return new FileInputStream(filePath).available();
    }
    public static long getFileSize(File file) throws IOException {
        return new FileInputStream(file).available();
    }

    public static Intent openFile(String filePath){

        File file = new File(filePath);
        Log.d(TAG, "filePath = [" + filePath + "] " + file.exists());
        if(!file.exists()) return null;
		/* 取得扩展名 */
        String end=file.getName().substring(file.getName().lastIndexOf(".") + 1,file.getName().length()).toLowerCase();
		/* 依扩展名的类型决定MimeType */
        switch (end) {
            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                return getAudioFileIntent(filePath);
            case "3gp":
            case "mp4":
                return getVideoFileIntent(filePath);
            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                return getImageFileIntent(filePath);
            case "apk":
                return getApkFileIntent(filePath);
            case "ppt":
            case "pptx":
                return getPptFileIntent(filePath);
            case "xls":
            case "xlsx":
                return getExcelFileIntent(filePath);
            case "doc":
            case "docx":
                return getWordFileIntent(filePath);
            case "pdf":
                return getPdfFileIntent(filePath);
            case "chm":
                return getChmFileIntent(filePath);
            case "txt":
                return getTextFileIntent(filePath, false);
            case "html":
            case "htm":
            case "xhtml":
                return getHtmlFileIntent(filePath);
            default:
                return getAllIntent(filePath);
        }
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent( String param ) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri,"*/*");
        return intent;
    }
    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent( String param ) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent( String param ) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent( String param ){

        Uri uri = Uri.parse(param ).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent( String param ) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    //Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent( String param, boolean paramBoolean){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean){
            Uri uri1 = Uri.parse(param );
            intent.setDataAndType(uri1, "text/plain");
        }else{
            Uri uri2 = Uri.fromFile(new File(param ));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }
    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }
}
