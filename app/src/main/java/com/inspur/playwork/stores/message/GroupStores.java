package com.inspur.playwork.stores.message;

import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.view.application.addressbook.AddressBookFragment;
import com.inspur.playwork.view.application.addressbook.AddressBookViewOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 群组stores
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class GroupStores extends Stores {

    public static String USER_ID;
    private static final String TAG = "GroupStores";
    private static final int SAVE_CONTACT_GROUP = 0x01;
    private static final int GET_ALL_RECENT_CONTACTS = 0x02;
    private static final int ADD_MEMBER = 0x03;
    private static final int CREATE_NEW_CHAT = 0x04;

    private static GroupStores mGroupStores;
    private JSONObject mRequestJson;
    private ArrayList<UserInfoBean> addMemberList;

    private WeakReference<AddressBookViewOperation> addressBookViewOperationWeakReference =
            new WeakReference<>(null);

    private GroupStores() {
        super(Dispatcher.getInstance());
    }

    public static GroupStores getInstance() {
        if (mGroupStores == null) {
            mGroupStores = new GroupStores();
        }
        return mGroupStores;
    }

    /**
     * 请求保存群组人员列表
     *
     * @param taskBean           任务信息
     * @param chatWindowInfoBean 聊天信息
     * @param tempChatPersonList 正在聊天人员列表
     */
    public void saveContactGroup(TaskBean taskBean, ChatWindowInfoBean chatWindowInfoBean, ArrayList<UserInfoBean> tempChatPersonList, boolean isNeedDataBase) {
        try {
            // 封装请求参数
            JSONArray members = new JSONArray();
            for (UserInfoBean user : tempChatPersonList) {
                members.put(user.getUserJson());
            }

            mRequestJson = new JSONObject();
            mRequestJson.put("members", members);
            if (taskBean != null && !"".equals(taskBean.taskId)) {
                mRequestJson.put("taskId", taskBean.taskId);
            } else {
                mRequestJson.put("groupId", chatWindowInfoBean.groupId);
            }
            mRequestJson.put("userId", USER_ID);
            if (!isNeedDataBase) {
                createHttpRequestJson(mRequestJson, SAVE_CONTACT_GROUP, "notNotify");
            } else {
                createHttpRequestJson(mRequestJson, SAVE_CONTACT_GROUP, "");
            }
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "saveContactGroup", mRequestJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SAVE_CONTACT_GROUP, CommonUtils.createRequestJson(mRequestJson));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加人员的request方法
     *
     * @param taskBean           任务信息
     * @param chatWindowInfoBean 聊天信息
     * @param addMemberList      需要增加的人员列表
     */
    public void addMember(TaskBean taskBean, ChatWindowInfoBean chatWindowInfoBean, ArrayList<UserInfoBean> addMemberList) {
        this.addMemberList = addMemberList;
        try {
//            JSONArray users = new JSONArray(GsonUtils.bean2Json(addMemberList)); // list to jsonArray

            JSONArray users = new JSONArray();
            for (UserInfoBean user : addMemberList) {
                users.put(user.getUserJson());
            }

            mRequestJson = new JSONObject();
            // 如果是任务聊天时，取taskId
            if (taskBean != null && !"".equals(taskBean.taskId)) {
                mRequestJson.put("taskId", taskBean.taskId);
            }
            mRequestJson.put("groupId", chatWindowInfoBean.groupId);
            mRequestJson.put("mailId", chatWindowInfoBean.mailId);
            mRequestJson.put("user", users);
            mRequestJson.put("userId", USER_ID);

            createHttpRequestJson(mRequestJson, ADD_MEMBER, null);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "addMember", mRequestJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, ADD_MEMBER, CommonUtils.createRequestJson(mRequestJson));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求最近联系人和群组列表
     */
    public void getAllRecentContacts() {
        try {
            mRequestJson = new JSONObject();
            mRequestJson.put("userId", USER_ID);
            createHttpRequestJson(mRequestJson, GET_ALL_RECENT_CONTACTS, "");
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getRecentContacts", httpCallback, mRequestJson, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求部门成员
     */
    public void searchByDept() {
        String url = AppConfig.SEARCH_BY_DEPT + "?Value=" + PreferencesHelper.getInstance().getCurrentUser().subDepartmentId;
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, searchByDeptCallBack);
    }

    private Callback searchByDeptCallBack = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                List<UserInfoBean> mDeptList = new ArrayList<>();
                // 去掉success_jsonpCallback()
                responseData = responseData.substring(22, responseData.length() - 1);
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (!USER_ID.equals(jsonObject.optString("UserId").toLowerCase())) {
                            UserInfoBean userInfoBean = new UserInfoBean();
                            userInfoBean.id = jsonObject.optString("UserId").toLowerCase();
                            userInfoBean.name = jsonObject.optString("UserName");
                            userInfoBean.avatar = jsonObject.optLong("Avatar");
                            userInfoBean.uid = jsonObject.optString("EId");
                            userInfoBean.email = userInfoBean.id + "@inspur.com";
                            mDeptList.add(userInfoBean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dispatcher.dispatchUpdateUIEvent(MessageActions.SEARCH_USER_BY_DEPT, mDeptList);
            }
        }
    };

    public void searchPerson(String inputText) {
        String url = AppConfig.BASE_SERVER + "searchperson.ashx?Value=" + inputText;
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, searchPersonCallback);
    }

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
                    ArrayList<UserInfoBean> resultUserList = new ArrayList<>();
                    for (int i = 0; i < responseJsonArray.length(); i++) {
                        UserInfoBean userInfoBean = new UserInfoBean();
                        userInfoBean.id = responseJsonArray.getJSONObject(i).optString("UserId").toLowerCase();
                        userInfoBean.uid = responseJsonArray.getJSONObject(i).optString("EId");
                        userInfoBean.name = responseJsonArray.getJSONObject(i).optString("UserName");
                        userInfoBean.avatar = responseJsonArray.getJSONObject(i).optLong("Avatar");
                        userInfoBean.email = userInfoBean.id + "@inspur.com";

                        resultUserList.add(userInfoBean);
                    }
                    Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SEARCH_PERSON, resultUserList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 创建新微聊
     *
     * @param addMemberList
     */
    public void createNewChat(ArrayList<UserInfoBean> addMemberList, String editTopic, String clientID) {
        this.addMemberList = addMemberList;
        try {
            UserInfoBean creator = PreferencesHelper.getInstance().getCurrentUser();
            int isGroup = (addMemberList.size() > 1) ? 1 : 0;
//            JSONArray members = new JSONArray(GsonUtils.bean2Json(addMemberList));

            JSONArray members = new JSONArray();
            for (UserInfoBean user : addMemberList) {
                members.put(user.getUserJson());
            }


//            Log.i(TAG, "createNewChat: " + addMemberList.get(0).toString());
            mRequestJson = new JSONObject();
            mRequestJson.put("creator", creator.getUserJson());
            mRequestJson.put("isGroup", isGroup);
            mRequestJson.put("subject", editTopic);
            mRequestJson.put("members", members);

            createHttpRequestJson(mRequestJson, CREATE_NEW_CHAT, clientID);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "createNewChat", mRequestJson, httpCallback, "");
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, CREATE_NEW_CHAT, CommonUtils.createRequestJson(mRequestJson));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * saveContactGroup接口数据解析
     *
     * @param responseJson
     */
    private void decodeSaveContactGroup(JSONObject responseJson) {
//        JSONObject docs = responseJson.optJSONObject("docs");
        boolean res = responseJson.optBoolean("type");
        if (TextUtils.isEmpty(responseJson.optString("ClientId")))
            dispatcher.dispatchUpdateUIEvent(MessageActions.SAVE_CONTACT_GROUP, res);
    }

    /**
     * 增加人员的response方法
     *
     * @param responseJson
     */
    private void decodeAddMember(JSONObject responseJson) {
//        JSONObject docs = responseJson.optJSONObject("docs");
        boolean res = responseJson.optBoolean("type");

        dispatcher.dispatchUpdateUIEvent(MessageActions.ADD_MEMBER, res, this.addMemberList);
    }

    /**
     * getAllRecentContacts接口数据解析
     *
     * @param responseJson
     */
    private void decodeGetAllRecentContacts(JSONObject responseJson) {
        JSONArray docs = responseJson.optJSONArray("data");

        // 联系组列表
        List<GroupInfoBean> groupList = new ArrayList<>();
        // 最近联系人列表
        HashSet<UserInfoBean> recentList = new HashSet<>();

        for (int i = 0; i < docs.length(); i++) {
            JSONObject jsonObject = docs.optJSONObject(i);
            if (jsonObject.optInt("isGroup") == 0)
                continue;
            GroupInfoBean groupInfo = new GroupInfoBean(jsonObject);
            // 单聊不显示在群列表中
            groupList.add(groupInfo);

            recentList.addAll(groupInfo.getMemberList());
        }
        dispatcher.dispatchUpdateUIEvent(MessageActions.GET_ALL_RECENT_CONTACTS, groupList, new ArrayList<>(recentList));
    }

    /**
     * 查询列表中是否存在传入的id
     *
     * @param id                 待检查的id
     * @param recentContactsList 查询列表
     * @return
     */
    private boolean checkIdExisted(String id, List<UserInfoBean> recentContactsList) {
        if (id.equals(USER_ID)) {
            return true;
        }
        for (int i = 0; i < recentContactsList.size(); i++) {
            if (id.equals(recentContactsList.get(i).id)) {
                return true;
            }
        }
        return false;
    }

    private void decodeCreateNewChat(JSONObject responseJson) {
        String clientId = responseJson.optString("ClientId");
        if (!TextUtils.isEmpty(clientId) && clientId.equals(AddressBookFragment.ADDRESS_BOOK_CHAT_TAG)) {
            if (responseJson.optBoolean("type")) {
                JSONObject docs = responseJson.optJSONObject("data");
                String groupId = docs.optString("groupId");
                if (!TextUtils.isEmpty(groupId)) {
                    ChatWindowInfoBean chatWindowInfoBean = new ChatWindowInfoBean(docs);
                    MessageStores.getInstance().createNewChat(chatWindowInfoBean, true);
                } else {
                    if (addressBookViewOperationWeakReference.get() != null) {
                        addressBookViewOperationWeakReference.get().createChatSuccess(null, null);
                    }
                }
            } else {
                if (addressBookViewOperationWeakReference.get() != null) {
                    addressBookViewOperationWeakReference.get().createChatSuccess(null, null);
                }
            }
            return;
        }
        if (responseJson.optBoolean("type")) {

            JSONObject docs = responseJson.optJSONObject("data");
            String groupId = docs.optString("groupId");
            if (!TextUtils.isEmpty(groupId)) {
                ChatWindowInfoBean chatWindowInfoBean = new ChatWindowInfoBean(docs);
                Log.i(TAG, "decodeCreateNewChat: " + responseJson.optBoolean("isOldSingle"));
                MessageStores.getInstance().createNewChat(chatWindowInfoBean, false);
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.CREATE_NEW_CHAT, chatWindowInfoBean);
            }
        } else {
            MessageStores.getInstance().createNewChat(null, false);
//            Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.CREATE_NEW_CHAT);
        }
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

    private Callback httpCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {

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
                    Log.i(TAG, "onResponse: type:" + type + res.toString());
                    switch (type) {
                        case SAVE_CONTACT_GROUP:
                            decodeSaveContactGroup(res);
                            break;
                        case GET_ALL_RECENT_CONTACTS:
                            decodeGetAllRecentContacts(res);
                            break;
                        case ADD_MEMBER:
                            decodeAddMember(res);
                            break;
                        case CREATE_NEW_CHAT:
                            decodeCreateNewChat(res);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void getUserInfoById(String... userIds) {
        String url = AppConfig.BASE_SERVER + "search/getSimpleInfo.ashx?Value=";
        int length = userIds.length;
        int i = 0;
        for (String id : userIds) {
            i++;
            if (i < length) {
                url += id + ",";
            } else {
                url += id;
            }
        }
        dispatcher.dispatchNetWorkAction(CommonActions.GET_DATA_BY_HTTP_GET, url, getUserInfoCallBack);
    }

    private Callback getUserInfoCallBack = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            if (addressBookViewOperationWeakReference.get() != null) {
                addressBookViewOperationWeakReference.get().searchUserInfoSuccess(null);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() == 200) {
                String responseData = response.body().string();
                Log.i(TAG, "onResponse: "+responseData);
                try {
                    JSONArray responseJsonArray = new JSONArray(responseData);
                    ArrayList<UserInfoBean> resultUserList = new ArrayList<>();
                    int length = responseJsonArray.length();
                    for (int i = 0; i < length; i++) {
                        UserInfoBean userInfoBean = new UserInfoBean();
                        JSONObject user = responseJsonArray.getJSONObject(i);
                        userInfoBean.id = user.optString("UserId").toLowerCase();
                        userInfoBean.uid = user.optString("EId");
                        userInfoBean.name = user.optString("UserName");
                        userInfoBean.avatar = user.optLong("Avatar");
                        userInfoBean.email = userInfoBean.id + "@inspur.com";

                        resultUserList.add(userInfoBean);
                    }
                    if (addressBookViewOperationWeakReference.get() != null) {
                        addressBookViewOperationWeakReference.get().searchUserInfoSuccess(resultUserList);
                    }
//                    Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SEARCH_PERSON, resultUserList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (addressBookViewOperationWeakReference.get() != null) {
                        addressBookViewOperationWeakReference.get().searchUserInfoSuccess(null);
                    }
                }
            } else {
                if (addressBookViewOperationWeakReference.get() != null) {
                    addressBookViewOperationWeakReference.get().searchUserInfoSuccess(null);
                }
            }
        }
    };


    public void setViewRefrence(AddressBookViewOperation operation) {
        addressBookViewOperationWeakReference = new WeakReference<>(operation);
    }
}
