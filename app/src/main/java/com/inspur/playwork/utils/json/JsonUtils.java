package com.inspur.playwork.utils.json;

import android.text.TextUtils;

import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bugcode on 2016/3/18.
 */
public class JsonUtils {

    public static JSONObject createRequestJson(JSONObject body) throws JSONException {
        return createRequestJson(body, null);
    }

    public static JSONObject createRequestJson(JSONObject body, String clientId) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
        requestJson.put("Body", body);
        if (!TextUtils.isEmpty(clientId))
            requestJson.put("ClientId", clientId);
        requestJson.put("isPhone", true);
        return requestJson;
    }
}
