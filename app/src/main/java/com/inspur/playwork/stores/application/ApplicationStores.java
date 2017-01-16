package com.inspur.playwork.stores.application;

import android.database.MatrixCursor;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.SearchPersonInfo;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.model.weekplan.WeekPlanHeader;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.view.application.addressbook.AddressBookViewOperation;
import com.inspur.playwork.view.application.news.NewsViewOperation;
import com.inspur.playwork.view.application.weekplan.WeekPlanViewOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by fan on 17-1-6.
 */
public class ApplicationStores extends Stores {
    private static final String TAG = "ApplicationStores";

    private static final int GET_SHARE_WEEK_PLAN = 0x01;
    private static final int GET_PLAN_DETAIL = 0x02;
    private static final String GET_OTHER_PLANS = "getOtherPlans";
    private static final String GET_WEEK_PLAN_DETAIL = "getWeekPlanByWeeks";
    private static final String QUERY_NEWS = "getNews";

    private ArrayList<WeekPlanHeader> planArrayList;

    public ApplicationStores(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public static ApplicationStores getInstance() {
        return SingleRefreshManager.getInstance().getApplicationStores();
    }

    private WeakReference<AddressBookViewOperation> addressBookReference = new WeakReference<>(null);
    private WeakReference<WeekPlanViewOperation> weekPlanWeakReference = new WeakReference<>(null);
    private WeakReference<NewsViewOperation> newsWeakReference = new WeakReference<>(null);
    private ArrayList<SearchPersonInfo> resultList;


    public void searchPerson(String inputText) {
        String url = AppConfig.BASE_SERVER + "searchperson.ashx?Value=" + inputText;
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, searchPersonCallback);
    }

    public void searchPersonInfo(String text) {
        String url = AppConfig.BASE_LOG_SERVER + "getUserInfoByKey.ashx?Value=" + text;
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, searchPersonInfoCallback);
    }

    public void viewMobileNum(String email) {
        String url = AppConfig.BASE_LOG_SERVER + "sl.ashx?f=" + PreferencesHelper.getInstance().getCurrentUser().id + "&e=" + email;
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, viewPhoneCallback);
    }


    private Callback viewPhoneCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
        }
    };

    private Callback searchPersonCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() == 200) {
                String responseData = response.body().string();
                try {
                    JSONArray responseJsonArray = new JSONArray(responseData);
                    String[] columnNames = {"_id", "name", "userId", "avatar"};
                    int count = responseJsonArray.length();
                    MatrixCursor cursor = new MatrixCursor(columnNames);
                    Object[] temp = new Object[4];
                    for (int i = 0; i < count; i++) {
                        JSONObject user = responseJsonArray.getJSONObject(i);
                        temp[0] = i;
                        temp[1] = user.optString("UserName");
                        temp[2] = user.optString("UserId").toLowerCase();
                        temp[3] = user.optLong("Avatar");
                        cursor.addRow(temp);
                    }
                    if (addressBookReference.get() != null) {
                        cursor.moveToFirst();
                        addressBookReference.get().showSuggestList(cursor);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Callback searchPersonInfoCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            if (addressBookReference.get() != null) {
                addressBookReference.get().showSuggestList(null);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() == 200) {
                String responseData = response.body().string();
                try {
                    JSONArray responseJsonArray = new JSONArray(responseData);
                    int count = responseJsonArray.length();
                    Log.i(TAG, "onResponse: " + count);
                    if (resultList == null) {
                        resultList = new ArrayList<>();
                    }
                    resultList.clear();
                    for (int i = 0; i < count; i++) {
                        JSONObject user = responseJsonArray.getJSONObject(i);
                        SearchPersonInfo person = new SearchPersonInfo(user);
                        resultList.add(person);
                    }
                    if (addressBookReference.get() != null) {
                        addressBookReference.get().showSearchResult(resultList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void setAddressBookReference(AddressBookViewOperation operation) {
        this.addressBookReference = new WeakReference<>(operation);
    }

    public void setWeekPlanReference(WeekPlanViewOperation operation) {
        this.weekPlanWeakReference = new WeakReference<>(operation);
    }

    public void setNewsWeakReference(NewsViewOperation operation) {
        this.newsWeakReference = new WeakReference<>(operation);
    }

    public void getNews(int type, int page, String departMent) {
        JSONObject requsetJson = new JSONObject();
        try {
            requsetJson.put("page", page);
            requsetJson.put("type", type + 1);
            requsetJson.put("department", departMent);
            requsetJson.put("length", 10);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.QUERY_NEWS_URL,
                    getNewsCallback, requsetJson, QUERY_NEWS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getShareWeekPlanList(int weekNum) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(time);
//        int weekNum = calendar.get(Calendar.WEEK_OF_YEAR);
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
            requestJson.put("WeekNumber", weekNum);
            createHttpRequestJson(requestJson, GET_SHARE_WEEK_PLAN, "" + weekNum);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + GET_OTHER_PLANS,
                    httpCallback, requestJson, GET_OTHER_PLANS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPlanDetail(String userId, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        int weekNum = DateUtils.getDayWeekNum(time)[1] + 1;
        JSONObject requestJson = new JSONObject();
        JSONArray weekArray = new JSONArray();
//        calendar.get(Calendar.)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            calendar.add(Calendar.DATE, -6);
        } else {
            calendar.add(Calendar.DATE, -(dayOfWeek - 2));
        }
        for (int i = 0; i < 7; i++) {
            weekArray.put(calendar.getTimeInMillis());
            calendar.add(Calendar.DATE, 1);
        }
        try {
            requestJson.put("userId", userId);
            requestJson.put("weekNumber", weekNum);
            requestJson.put("weekNumbers", weekArray);
            requestJson.put("type", 2);

            createHttpRequestJson(requestJson, GET_PLAN_DETAIL, "");
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + GET_WEEK_PLAN_DETAIL,
                    httpCallback, requestJson, GET_WEEK_PLAN_DETAIL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = call.request().header("requestId");
            errorHandle(requestId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    JSONObject cleentId = new JSONObject(res.optString("ClientId"));
                    int type = cleentId.optInt("type");
                    String clientId = cleentId.optString("ClientId", "");
                    if (!TextUtils.isEmpty(clientId)) {
                        res.put("ClientId", clientId);
                    } else {
                        res.remove("ClientId");
                    }
                    Log.i(TAG, "onResponse: " + res);

                    switch (type) {
                        case GET_SHARE_WEEK_PLAN:
                            parseGetShareWeekPlanList(res);
                            break;
                        case GET_PLAN_DETAIL:
                            parseWeekPlanDetail(res);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String requestId = call.request().header("requestId");
                errorHandle(requestId);
            }
        }
    };

    private void errorHandle(String requestId) {
        switch (requestId) {
            case GET_OTHER_PLANS:
                if (weekPlanWeakReference.get() != null) {
                    weekPlanWeakReference.get().showShareWeekPlanList(null);
                }
                break;
        }
    }

    private void parseWeekPlanDetail(JSONObject res) {

    }

    private void parseGetShareWeekPlanList(JSONObject res) {
        String clientWeekNum = res.optString("ClientId");
        if (res.optBoolean("type")) {
            JSONArray planList = res.optJSONArray("data");
            int length = planList.length();
            if (planArrayList == null) {
                planArrayList = new ArrayList<>();
            } else {
                planArrayList.clear();
            }
            for (int i = 0; i < length; i++) {
                JSONObject plan = planList.optJSONObject(i);
                WeekPlanHeader weekPlanHeader = new WeekPlanHeader(plan);
                planArrayList.add(weekPlanHeader);
            }
            WeekPlanHeader myWeekPlan = new WeekPlanHeader(WeekPlanHeader.MY_PLAN);
            myWeekPlan.subject = "第" + clientWeekNum + "周计划--" + PreferencesHelper.getInstance().getCurrentUser().name;
            myWeekPlan.from = PreferencesHelper.getInstance().getCurrentUser();
            planArrayList.add(WeekPlanHeader.getInstance(WeekPlanHeader.MY_PLAN_TITLE));
            planArrayList.add(WeekPlanHeader.getInstance(WeekPlanHeader.OTHER_PLAN_TITLE));
            planArrayList.add(myWeekPlan);
            Collections.sort(planArrayList);
            if (weekPlanWeakReference.get() != null) {
                weekPlanWeakReference.get().showShareWeekPlanList(planArrayList);
            }
        } else {
            errorHandle(GET_OTHER_PLANS);
        }
    }

    private Callback getNewsCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = call.request().header("requestId");
            errorHandle(requestId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    int page = res.optInt("page");
                    JSONArray newsArray = res.optJSONArray("data");
                    int count = newsArray.length();
                    ArrayList<DepartmentNewsBean> newsBeanArrayList = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        JSONObject news = newsArray.optJSONObject(i);
                        DepartmentNewsBean newsBean = new DepartmentNewsBean(news);
                        newsBeanArrayList.add(newsBean);
                    }
                    if (newsWeakReference.get() != null) {
                        newsWeakReference.get().showNews(page, newsBeanArrayList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String requestId = call.request().header("requestId");
                errorHandle(requestId);
            }
        }
    };

    public void clean() {
    }

    private void createHttpRequestJson(JSONObject body, int type, String clientId) throws JSONException {
        JSONObject clientJson = new JSONObject();
        clientJson.put("type", type);
        if (!TextUtils.isEmpty(clientId)) {
            clientJson.put("ClientId", clientId);
        }
        body.put("isPhone", true);
        body.put("ClientId", clientJson.toString());
    }
}