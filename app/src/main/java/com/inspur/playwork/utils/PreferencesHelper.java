package com.inspur.playwork.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.common.VersionInfoBean;
import com.inspur.playwork.utils.json.GsonUtils;
import com.inspur.playwork.versionUpdate.UserInfoJsonObject;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Fan on 15-9-11.
 */
public class PreferencesHelper {

    private static final String TAG = "PreferencesHelperFan";

    private static final String PREFERENCE_NAME = "playwork_config.xml";

    public static final String HAVE_LOGIN_TIME_LINE = "have_login";

    public static final String USER_NAME = "user_name";

    public static final String CONNECTID = "connectid";

    public static final String USER_INFO = "userInfo";
    public static final String VERSION_INFO = "versionInfo";

    public static final String HAVE_LOGIN_AD_SERVER = "loginadserver";

    public static final String INPUT_HEIGHT = "input_method_height";

    public static final String NET_WORK_TYPE = "net_work_type";

    public static final String UNIQUE_ID = "unqiue_id";

    public static final String SERVICE_NUM_GROUP_ID = "service_num_groupId";

    public static final String IS_GUIDE_PAGE_SHOW = "isguildpageshow";


    private SharedPreferences sp;

    private Context context;

    private UserInfoBean currentUser;

    public String key;

    public String serviceGroupId;


    public static PreferencesHelper getInstance() {
        return SingleRefreshManager.getInstance().getPreferencesHelper();
    }

    public PreferencesHelper() {
    }

    public void init(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void writeToPreferences(String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void writeToPreferencesFree(String key, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getServiceNumGroupId() {
        if (TextUtils.isEmpty(serviceGroupId)) {
            serviceGroupId = sp.getString(SERVICE_NUM_GROUP_ID, "");
        }
        return serviceGroupId;
    }

    public String readStringPreference(String key) {
        if (key.equals(USER_NAME)) {
            return getCurrentUser().id;
        }
        return sp.getString(key, "");
    }

    public void writeToPreferences(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void writeToPreferences2Exit(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Context getContext() {
        return context;
    }

    public boolean readBooleanPreference(String key) {
        return sp.getBoolean(key, false);
    }

    /*获取存储的登录信息*/
    private UserInfoJsonObject getUserInfoToNative() {
        UserInfoJsonObject userInfoJsonObject = GsonUtils.json2Bean(readStringPreference(USER_INFO), UserInfoJsonObject.class);
        if (userInfoJsonObject != null && TextUtils.isEmpty(userInfoJsonObject.getUserId())) {
            return null;
        }
        return userInfoJsonObject;
    }

    public UserInfoBean getCurrentUser() {
        Log.i(TAG, "getCurrentUser: " + (currentUser == null));
        if (currentUser == null) {
            UserInfoJsonObject user = getUserInfoToNative();
            Log.i(TAG, "getCurrentUser: " + user);
            if (user == null)
                return null;
            currentUser = new UserInfoBean();
            currentUser.id = user.getUserId().toLowerCase();
            currentUser.uid = user.getEId();
            currentUser.avatar = user.getAvatar();
            currentUser.email = user.getUserId().toLowerCase() + AppConfig.EMAIL_SUFFIX;
            currentUser.name = user.getUserName();
            currentUser.passWord = user.getPassword();
            currentUser.department = user.getDepartment();
            currentUser.subDepartment = user.getSubdepartment();
            currentUser.subDepartmentId = user.getSubdepartmentId();
            currentUser.company = user.getCompany();
        }
        Log.i(TAG, currentUser.avatar + "" + currentUser.id);
        return currentUser;
    }

    public UserInfoBean getCurrentUser(boolean forseNew) {
        if (forseNew) {
            Log.i(TAG, "getCurrentUser: " + (currentUser == null));
            UserInfoJsonObject user = getUserInfoToNative();
            Log.i(TAG, "getCurrentUser: " + (user == null));
            if (user == null)
                return null;
            currentUser = new UserInfoBean();
            currentUser.id = user.getUserId().toLowerCase();
            currentUser.uid = user.getEId();
            currentUser.avatar = user.getAvatar();
            currentUser.email = user.getUserId().toLowerCase() + AppConfig.EMAIL_SUFFIX;
            currentUser.name = user.getUserName();
            currentUser.passWord = user.getPassword();
            currentUser.department = user.getDepartment();
            currentUser.subDepartment = user.getSubdepartment();
            currentUser.subDepartmentId = user.getSubdepartmentId();
            currentUser.company = user.getCompany();
//            Log.i(TAG, currentUser.avatar + "" + currentUser.id);
            return currentUser;
        } else {
            return getCurrentUser();
        }
    }

    public VersionInfoBean getVersionInfo() {
        try {
            JSONObject jsonObject = new JSONObject(readStringPreference(VERSION_INFO));
            Log.i(TAG, "getVersionInfo: " + jsonObject.toString());
            return new VersionInfoBean(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToPreferences(String key, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int readIntPreference(String key) {
        return sp.getInt(key, -1);
    }


    public void writeToPreferences(String s, long timeValue) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(s, timeValue);
        editor.apply();
    }

    public void clean() {
        sp = null;
        currentUser = null;
        key = null;
    }
}
