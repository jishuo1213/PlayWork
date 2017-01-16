package com.inspur.playwork.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties工具类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class PropertiesUtil {

    /**
     * 根据Key 读取Value
     *
     * @param key
     * @return
     */
    public static String readData(Context mContext, String key, int resId) {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(mContext.getResources().openRawResource(resId));
            props.load(in);
            in.close();
            String value = props.getProperty(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
