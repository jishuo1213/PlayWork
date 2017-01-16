package com.inspur.playwork.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Gson工具类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class GsonUtils {

    private static Gson mGson = new GsonBuilder().create();

    public static String bean2Json(Object object) {
        return mGson.toJson(object);
    }

    public static <T> T json2Bean(String jsonStr, Class<T> objectClass) {
        try {
            return mGson.fromJson(jsonStr, objectClass);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static <T> T json2Bean(String jsonStr, Type type) {
        return mGson.fromJson(jsonStr, type);
    }


    public static String bean2Json(Object object, Type type) {
        return mGson.toJson(object, type);
    }
}
