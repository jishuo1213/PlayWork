package com.inspur.playwork.stores.login;


import android.util.Log;
import android.util.SparseArray;

import com.inspur.playwork.BuildConfig;
import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.login.LoginActions;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 登录Stores
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class LoginStores extends Stores {

    private static final String TAG = "LoginStoresFan";

    private static LoginStores mLoginStores;

    private String userName;

    private String password;


    private LoginStores() {
        super(Dispatcher.getInstance());
    }

    public static LoginStores getInstance() {
        if (mLoginStores == null) {
            mLoginStores = new LoginStores();
        }
        return mLoginStores;
    }

    public static void clean() {
        if (mLoginStores != null)
            mLoginStores.unRegister();
        mLoginStores = null;
    }

    @SuppressWarnings("unused")
    public void onEvent(StoreAction storeAction) {
        SparseArray<Object> data = storeAction.getActiontData();
        switch (storeAction.getActionType()) {
            case LoginActions.LOGIN_TO_ADSERVER:
                Log.i(TAG, "onEvent: LOGIN_TO_ADSERVER");
                loginToADServer((String) data.get(0), (String) data.get(1), (boolean) data.get(2));
                break;
//            case CommonActions.REVICE_TIMELINE_DATA_FROM_SERVER:
//                String type = (String) storeAction.getActiontData().get(0);
//                if (type.equals("login")) {
//                    JSONObject json = (JSONObject) storeAction.getActiontData().get(1);
//                    if (json.optString("LoginStatus").equals("success")) {
//                        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.CONNECTID, json.optString("ConnectionId"));
//                        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, true);
//                        dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_TIMELINE_SUCCESS);
//                    } else {
//                        dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_FAILED);
//                    }
//                }
//                break;
            case LoginActions.LOGIN_TIME_LINE_SERVER:
                if (!PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_TIME_LINE)) {
                    loginTimeLineServer();
                } else {
                    dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_TIMELINE_SUCCESS);
                }
                break;
        }
    }

    /**
     * 登录外网服务器
     */
    private void loginToADServer(String userName, String password, boolean needEncypt) {
        this.userName = userName.toLowerCase();
/*        if (PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_AD_SERVER)) {
            dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_AD_SERVER_SUCCESS);
            return;
        }*/
        if (needEncypt) {
            this.password = EncryptUtil.encrypt2aes(password, AppConfig.AD_KEY);
        } else {
            this.password = password;
        }
        String loginPath = AppConfig.AD_SERVER_URI + "version=" + BuildConfig.VERSION_CODE + "&loginName=" + userName + "&password=" + this.password;
//        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, loginPath, loginCallBack);
        OkHttpClientManager.getInstance().getAsyn(loginPath, loginCallBack);
    }


    /**
     * 登录时间轴服务器
     */
    private void loginTimeLineServer() {
        UserInfoBean userInfoJsonObject = PreferencesHelper.getInstance().getCurrentUser();
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("name", userInfoJsonObject.name);
            requestJson.put("userId", userInfoJsonObject.id);
            requestJson.put("avatar", userInfoJsonObject.avatar);
            requestJson.put("password", userInfoJsonObject.passWord);
            requestJson.put("keepServerMail", "@inspur.com");
            requestJson.put("inUse", 1);
            requestJson.put("type", 1);
            requestJson.put("loginTime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, "login", requestJson);
    }


    private Callback loginCallBack = new Callback() {

        @Override
        public void onFailure(Call request, IOException e) {
            Log.i(TAG, "onFailure====》error" + e.getLocalizedMessage());
            dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_FAILED, 1);

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String backData = response.body().string();
                Log.i(TAG, "onResponse====》onResponse+" + backData);
                JSONArray result = null;
                try {
                    result = new JSONArray(backData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    JSONObject infoJson = (JSONObject) result.opt(0);
                    if (infoJson.opt("status").equals("success")) {  //外网登录成功
                        JSONObject infoObj = infoJson.optJSONObject("info");
                        try {
                            infoObj.put("Password", password);
                            JSONObject versionJson = new JSONObject();

                            versionJson.put("version", infoJson.optString("version"));
                            versionJson.put("versioname", infoJson.optString("versioname"));
                            versionJson.put("updatecontent", infoJson.optString("updatecontent"));
                            versionJson.put("VURL", infoJson.optString("VURL"));
                            PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.VERSION_INFO, versionJson.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        PreferencesHelper.getInstance().key = infoObj.optString("ClientKEY");
                        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.USER_INFO, infoObj.toString());
                        MessageStores.USER_ID = userName;
                        GroupStores.USER_ID = userName;
                        PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_AD_SERVER, true);
                        String fileName = infoObj.optString("UserId") + "-" + infoObj.optLong("Avatar") + ".png";
                        if (!FileUtil.isFileExist(AppConfig.AVATAR_DIR, fileName)) {
                            OkHttpClientManager.getInstance().downloadFileUseFirst(AppConfig.AVATAR_ROOT_PATH + infoObj.opt("Avatar"), AppConfig.AVATAR_DIR, fileName);
                            Dispatcher.getInstance().dispatchNetWorkAction(NetWorkActions.USER_AVATAR_DOWNLOADED, userName, infoObj.optLong("Avatar"));
                        }
                        dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_AD_SERVER_SUCCESS);
                    } else {
                        dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_FAILED, 2);
                    }
                } else {
                    dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_FAILED, 4);
                }
            } else {
                Log.i(TAG, "登录失败了 code = " + response.code());
                dispatcher.dispatchUpdateUIEvent(LoginActions.LOGIN_FAILED, 3);
            }
        }
    };
}
