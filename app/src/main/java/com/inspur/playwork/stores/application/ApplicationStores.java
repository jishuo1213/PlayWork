package com.inspur.playwork.stores.application;

import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.SearchPersonInfo;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.model.weekplan.WeekPlanHeader;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.view.application.addressbook.AddressBookViewOperation;
import com.inspur.playwork.view.application.news.NewListFragment;
import com.inspur.playwork.view.application.news.NewsDetailOperation;
import com.inspur.playwork.view.application.news.NewsViewOperation;
import com.inspur.playwork.view.application.weekplan.WeekPlanViewOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    private static final String GET_OTHER_PLANS = "getOtherPlans";
    private static final String GET_WEEK_PLAN_DETAIL = "getWeekPlanByWeeks";
    private static final String QUERY_NEWS = "getNews";
    private static final String QUERY_NEWS_DETAIL = "getNewsDetail";

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
    private WeakReference<NewsDetailOperation> newsDetailWeakReference = new WeakReference<>(null);
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

    public void setNewsDetailWeakReference(NewsDetailOperation operation) {
        this.newsDetailWeakReference = new WeakReference<>(operation);
    }

    public void getNews(String uuid, int type, int page, String departMent) {
        JSONObject requsetJson = new JSONObject();
        try {
            requsetJson.put("page", page);
            requsetJson.put("type", type + 1);
            requsetJson.put("department", departMent);
            requsetJson.put("length", 10);
            JSONObject headerJson = new JSONObject();
            headerJson.put("name", QUERY_NEWS);
            headerJson.put("uuid", uuid);
            OkHttpClientManager.getInstance().newGetAsyn(AppConfig.NEWS_URL + "query.page",
                    getNewsCallback, requsetJson, headerJson.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getNewsDetail(String id, String key) {
        Log.i(TAG, "getNewsDetail: exist");
        if (FileUtil.isFileExist(FileUtil.getNewsFilePath() + id + ".html")) {
            if (newsDetailWeakReference.get() != null) {
                newsDetailWeakReference.get().getDetail(id, Uri.fromFile(new File(FileUtil.getNewsFilePath() + id + ".html")).toString());
//                try {
//                    newsDetailWeakReference.get().getDetail(id, FileUtil.readContent(FileUtil.getNewsFilePath() + id + ".html"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            return;
        }
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("id", id);
            requestJson.put("key", key);
            JSONObject headerJson = new JSONObject();
            headerJson.put("name", QUERY_NEWS_DETAIL);
            headerJson.put("id", id);
            OkHttpClientManager.getInstance().newGetAsyn(AppConfig.NEWS_URL + "detail",
                    getNewsDetailCallback, requestJson, headerJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getShareWeekPlanList(int weekNum) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
            requestJson.put("WeekNumber", weekNum);
            createHttpRequestJson(requestJson);
            JSONObject headerJson = new JSONObject();
            headerJson.put("name", GET_OTHER_PLANS);
            headerJson.put("weekNum", weekNum);
            OkHttpClientManager.getInstance().newGetAsyn(AppConfig.HTTP_SERVER_IP + GET_OTHER_PLANS,
                    httpCallback, requestJson, headerJson.toString());
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

            createHttpRequestJson(requestJson);
            JSONObject headerJson = new JSONObject();
            headerJson.put("name", GET_WEEK_PLAN_DETAIL);
            OkHttpClientManager.getInstance().newGetAsyn(AppConfig.HTTP_SERVER_IP + GET_WEEK_PLAN_DETAIL,
                    httpCallback, requestJson, headerJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = (String) call.request().tag();
            errorHandle(requestId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    String requestId = (String) call.request().tag();
                    JSONObject res = new JSONObject(response.body().string());
                    JSONObject cleentId = new JSONObject(requestId);
                    String type = cleentId.optString("name");

                    Log.i(TAG, "onResponse: " + res);

                    switch (type) {
                        case GET_OTHER_PLANS:
                            parseGetShareWeekPlanList(res, cleentId);
                            break;
                        case GET_WEEK_PLAN_DETAIL:
                            parseWeekPlanDetail(res, cleentId);
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
//        if (requestId.contains("&")) {
//            requestId = requestId.split("&")[0];
//            uuid = requestId.split("&")[1];
//        }
        Log.d(TAG, "errorHandle() called with: requestId = [" + requestId + "]");
        JSONObject headerJson = null;
        try {
            headerJson = new JSONObject(requestId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert headerJson != null;
        String requsetType = headerJson.optString("name");
        switch (requsetType) {
            case GET_OTHER_PLANS:
                if (weekPlanWeakReference.get() != null) {
                    weekPlanWeakReference.get().showShareWeekPlanList(null);
                }
                break;
            case QUERY_NEWS:
                if (newsWeakReference.get() != null) {
                    newsWeakReference.get().showNewsError(headerJson.optString("uuid"), NewListFragment.NET_ERROR);
                }
                break;
        }
    }

    private void parseWeekPlanDetail(JSONObject res, JSONObject requstId) {
        try {
            FileUtil.writeContentToFile(res.toString(), FileUtil.getSDCardRoot() + "test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void parseGetShareWeekPlanList(JSONObject res, JSONObject requstId) {
        int clientWeekNum = requstId.optInt("weekNum");
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
            errorHandle(requstId.toString());
        }
    }


    private Callback getNewsDetailCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = (String) call.request().tag();
            errorHandle(requestId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String requestId = (String) call.request().tag();
//                String newsId = requestId.split("&")[1]
                try {
                    JSONObject headJson = new JSONObject(requestId);
                    JSONObject res = new JSONObject(response.body().string());
                    Log.i(TAG, "onResponse: " + res.toString());
                    JSONObject data = res.optJSONObject("data");
                    if (data != null) {
                        String content = data.optString("content");
                        String filePath = FileUtil.getNewsFilePath() + headJson.optString("id") + ".html";
                        FileUtil.writeContentToFile(content, filePath);
                        if (newsDetailWeakReference.get() != null) {
                            newsDetailWeakReference.get().getDetail(headJson.optString("id"), Uri.fromFile(new File(filePath)).toString());
                        }
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


    private Callback getNewsCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String requestId = (String) call.request().tag();
            errorHandle(requestId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    String requestId = (String) call.request().tag();
                    JSONObject header = new JSONObject(requestId);
                    String uuid = header.optString("uuid");

                    JSONObject res = new JSONObject(response.body().string());
                    JSONArray newsArray = res.optJSONArray("data");
                    int count = newsArray.length();
                    if (count == 0) {
                        if (newsWeakReference.get() != null) {
                            newsWeakReference.get().showNewsError(uuid, NewsViewOperation.QUERY_RES_EMPTY);
                        }
                        return;
                    }
                    ArrayList<DepartmentNewsBean> newsBeanArrayList = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        JSONObject news = newsArray.optJSONObject(i);
                        DepartmentNewsBean newsBean = new DepartmentNewsBean(news);
                        newsBeanArrayList.add(newsBean);
                    }


                    if (newsWeakReference.get() != null) {
                        newsWeakReference.get().showNews(uuid, newsBeanArrayList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String requestId = (String) call.request().tag();
                errorHandle(requestId);
            }
        }
    };

    public void clean() {
    }

    private void createHttpRequestJson(JSONObject body) throws JSONException {
        body.put("isPhone", true);
    }
}