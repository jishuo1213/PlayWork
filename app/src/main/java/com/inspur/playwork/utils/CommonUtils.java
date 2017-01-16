package com.inspur.playwork.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    private static Pattern urlPattern;
    private static String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";


    public static boolean isUrlVliad(String url) {
        if (urlPattern == null)
            urlPattern = Pattern.compile(urlRegex);
        Matcher matcher = urlPattern.matcher(url);
        return matcher.matches();
    }

    @SuppressLint("SimpleDateFormat")
    public static long parseDate(String source) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(source).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static long parseDate(String source, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(source).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void callNum(String num, Context context) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
        context.startActivity(intent);
    }

    public static Intent getTakePhoteIntent(Context context, String imgPath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        imgPath = FileUtil.getImageFilePath() + System.currentTimeMillis() + ".png";

        if (Build.VERSION.SDK_INT >= 24) {
            Uri uri = FileProvider.getUriForFile(context, "com.inspur.playwork.fileprovider", new File(imgPath));
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imgPath)));
        }
        return intent;
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatTime(long time) {
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
                .format(time);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatTime(long time, String format) {
        return new SimpleDateFormat(format).format(time);
    }

    public static int getCalendarField(long time, int field) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return c.get(field);
    }

    public static String getAccessoryPath() {
        File sdCardDir = Environment.getExternalStorageDirectory();

        if (sdCardDir.exists()) {
            File mPictureDir = new File(sdCardDir + File.separator + "OmAccessory" + File.separator);
            if (!mPictureDir.exists()) {
                mPictureDir.mkdir();
            }
            return mPictureDir.getAbsolutePath();
        } else {
            File dir = Environment.getDataDirectory();
            File mPictureDir = new File(dir + File.separator + "OmAccessory" + File.separator);
            if (!mPictureDir.exists()) {
                mPictureDir.mkdir();
            }
            return mPictureDir.getAbsolutePath();
        }
    }

    public static String getCachePath() {
        File sdCardDir = Environment.getExternalStorageDirectory();

        if (sdCardDir.exists()) {
            File mPictureDir = new File(sdCardDir + File.separator + "OmCache" + File.separator);
            if (!mPictureDir.exists()) {
                mPictureDir.mkdir();
            }
            return mPictureDir.getAbsolutePath();
        } else {
            File dir = Environment.getDataDirectory();
            File mPictureDir = new File(dir + File.separator + "OmCache" + File.separator);
            if (!mPictureDir.exists()) {
                mPictureDir.mkdir();
            }
            return mPictureDir.getAbsolutePath();
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }


    public static void back() {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        });
    }

    private static void finishActivity() {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            //		fragment.getFragmentManager().beginTransaction().remove(fragment).commit();
        } catch (Exception e) {
            Log.e("Exception when onBack", e.toString());
        }
    }

    public static Activity getViewActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * 通过设置Camera打开闪光灯
     *
     * @param mCamera
     */
    @SuppressWarnings("deprecation")
    public static void turnLightOn(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }

    /**
     * 通过设置Camera关闭闪光灯
     *
     * @param mCamera
     */
    @SuppressWarnings("deprecation")
    public static void turnLightOff(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        // Check if camera flash exists
        if (flashModes == null) {
            return;
        }
        if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // Turn off the flash
            if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 封装发送给服务器json数据的格式
     *
     * @param body
     * @return
     */
    public static JSONObject createRequestJson(JSONObject body) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
            requestJson.put("Body", body);
            requestJson.put("isPhone", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }

    public static String delHTMLTag(String htmlStr) {
        String regEx_html = "<[^>]+>";// 定义HTML标签的正则表达式

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");// 过滤html标签

        return htmlStr.trim();// 返回文本字符串
    }

    public static void copy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText("已复制", content));
    }
}
