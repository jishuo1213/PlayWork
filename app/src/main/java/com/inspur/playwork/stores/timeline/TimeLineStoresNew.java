package com.inspur.playwork.stores.timeline;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.timeline.CalendarDateBean;
import com.inspur.playwork.model.timeline.CrossTaskBeanComparator;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.TodayTaskBeanComparator;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.view.timeline.TimeLineViewOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 时间轴业务逻辑处理类，注意
 * 这个类中的所有get方法返回的都是各种数据结构的拷贝，即，界面层
 * 的数据和这个类中没有关联
 * Created by Fan on 2016/4/19.
 */
public class TimeLineStoresNew extends Stores {

    private static final String TAG = "TimeLineStoresNew";


    private byte GET_LOCAL_SUCCESS_MASK = 0x01;
    private byte GET_NET_SUCCESS_MASK = 0x02;

    private String needUuid;

    private Calendar selectedDay;

    private ArrayList<TaskBean> todayTaskList;

    private ArrayList<TaskBean> crossDayTaskList;

    private ArrayList<TaskAttachmentBean> attachmentList;

    private ArrayList<UnReadMessageBean> unReadMessageList;

    private TaskBean currentTaskBean;

    private ArrayList<String> todayTaskIds;

    private ArrayList<String> crossDayTaskIds;

    private ArrayList<String> taskIds;

    private TodayTaskBeanComparator todayTaskBeanComparator;

    private ArrayList<MessageBean> unReadMap;

    private ArrayList<UnReadMessageBean> netUnReadList;

    private ArrayList<String> readedTaskIds;

    private boolean moveTime;

    private boolean isGetChatWidowInfo;

    private WeakReference<TimeLineViewOperation> viewOperationWeakReference;

    private boolean isAddCrossDay;

    private byte unreadStatus = 0;

    public TimeLineStoresNew() {
        super(Dispatcher.getInstance());
    }

    public static TimeLineStoresNew getInstance() {
        return SingleRefreshManager.getInstance().getTimeLineStoresNew();
    }


    @SuppressWarnings("unused")
    public void onEvent(StoreAction action) {
        switch (action.getActionType()) {
            case DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS://database->stores
                if ((int) action.getActiontData().get(0) == 0) {
                    //noinspection unchecked
                    queryUnReadSuccess(((ArrayList<UnReadMessageBean>) action.getActiontData().get(1)));
                }
                break;
            case MessageActions.RECIVE_UNREAD_TASK_CHAT_MSG://notification
                praseReciveTaskChatMsg((JSONObject) action.getActiontData().get(0));
                break;
            case DataBaseActions.QUERY_CHAT_BY_ID_SUCCESS://database->stores
                ChatWindowInfoBean windowInfoBean = (ChatWindowInfoBean) action.getActiontData().get(0);
                if (!TextUtils.isEmpty(windowInfoBean.taskId) && (int) action.getActiontData().get(1) == 2) {
                    isGetChatWidowInfo = false;
                    if (viewOperationWeakReference.get() != null) {
                        viewOperationWeakReference.get().getChatWindowInfoSuccess(true, windowInfoBean, currentTaskBean);
                    }
                }
                break;
            case DataBaseActions.QUERY_RESULT_IS_EMPTY://database->stores
                int emptyType = (int) action.getActiontData().get(0);
                if (emptyType == DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID) {
                    if (action.getActiontData().get(2) == null) {
                        String taskId = (String) action.getActiontData().get(1);
                        getChatWindowInfo(taskId);
                    }
                } else if (emptyType == DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE) {
                    unreadStatus |= GET_LOCAL_SUCCESS_MASK;
                    if (viewOperationWeakReference.get() != null && (unreadStatus & GET_NET_SUCCESS_MASK) > 0) {
                        Collections.sort(unReadMessageList);
                        //noinspection unchecked
                        viewOperationWeakReference.get().showUnReadMsg();
                    }
                }
                break;
            case TimeLineActions.TIME_LINE_SORT_TASK_NUM://notification
                if (viewOperationWeakReference.get() != null) {
                    viewOperationWeakReference.get().sortTaskList((JSONObject) action.getActiontData().get(0));
                }
                break;
/*            case MessageActions.SET_UNREAD_MSG_READ:
                setUnReadMsgRead((String) action.getActiontData().get(0), (String) action.getActiontData().get(1));
                break;*/
            case TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST://view->stores
                getTaskAttachList((String) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_SET_MESSAGE_TO_READ://notification
                setUnReadMsgRead((String) action.getActiontData().get(0), (String) action.getActiontData().get(1));
                break;
            case TimeLineActions.TIME_LINE_GET_UNREAD_MESSAGE:
                getNetUnReadMessage();
                break;
            case TimeLineActions.TIME_LINE_UPDATE_TASK_NAME:
                if (viewOperationWeakReference.get() != null) {
                    viewOperationWeakReference.get().updateTaskSubject((String) action.getActiontData().get(0), (String) action.getActiontData().get(1));
                }
                break;
            case TimeLineActions.TIME_LINE_DELETE_TASK:
                if (viewOperationWeakReference.get() != null) {
                    viewOperationWeakReference.get().deleteTask((String) action.getActiontData().get(0));
                }
                break;
            case TimeLineActions.TIME_LINE_ADD_NEW_TASK:
                TaskBean taskBean = new TaskBean((JSONObject) action.getActiontData().get(0));
                if (taskBean.isTodayTask()) {
                    if (DateUtils.isSameDayOfMillis(taskBean.startTime, selectedDay.getTimeInMillis())) {
//                        addTodayTask(taskBean);
                        getMailList(selectedDay.getTimeInMillis());
                    }
                } else {
                    if (taskBean.taskType == TaskBean.CROSS_DAY_TASK_TIME_UNCLEAR) {
                        if (taskBean.startTime <= selectedDay.getTimeInMillis()) {
                            getMailList(selectedDay.getTimeInMillis());
                        }
                    } else if (taskBean.taskType == TaskBean.CROSS_DAY_TASK) {
                        if (taskBean.startTime <= selectedDay.getTimeInMillis() && taskBean.endTime >= selectedDay.getTimeInMillis()) {
                            getMailList(selectedDay.getTimeInMillis());
                        }
                    }
                }
                break;
        }
    }

/*
    private void praseServerReturnData(StoreAction action, String type) {
//        JSONObject data = (JSONObject) action.getActiontData().get(1);
        switch (type) {
            case GET_MAIL_LIST:
//                String uuid = data.optString("ClientId");
//                if (!uuid.equals(needUuid)) {
//                    return;
//                }
//                praseMailList(data);
                break;
            case GET_UN_READ_MESSAGE:
//                parseUnReadMessage(data);
                break;
            case EXIT_TASK:
//                praseExitTaskResult(data);
                break;
            case GET_CHAT_WINDOW_INFO:
//                parseWindowInfoResult(data);
                break;
            case CHANGE_TASK_TIME:
//                parseChangeTaskTime(data);
                break;
            case SEND_MAIL:
//                praseAddMailResult(data);
                break;
            case GET_TASK_ATTACHMENT_LIST:
//                praseTaskAttachmentResult(data);
                break;
        }
    }
*/

    public void with(AppCompatActivity appCompatActivity) {

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initAttachMentList() {
        if (attachmentList == null)
            attachmentList = new ArrayList<>();
        attachmentList.clear();
    }

    private void praseTaskAttachmentResult(JSONObject data) {
        initAttachMentList();
        JSONArray json = data.optJSONArray("data");
        int count = json.length();
        for (int i = 0; i < count; i++) {
            JSONObject object = json.optJSONObject(i);
            TaskAttachmentBean mBean = new TaskAttachmentBean(object, attachTaskId);
            attachmentList.add(mBean);
        }
        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST, attachmentList);
    }

    private void praseAddMailResult(JSONObject jsonObject) {
//        JSONObject docs = jsonObject.optJSONObject("docs");
        if (jsonObject.has("ClientId")) {
            try {
                JSONObject taskInfo = new JSONObject(jsonObject.optString("ClientId"));
                dispatcher.dispatchDataBaseAction(DataBaseActions.RENAME_CHAT_SUBJECT,
                        taskInfo.optString("taskId"), taskInfo.optString("subject"));
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        if (jsonObject.optBoolean("type")) {
            Log.i(TAG, "praseAddMailResult: " + jsonObject.optString("taskId"));
            if (viewOperationWeakReference.get() != null)
                viewOperationWeakReference.get().sendMailResult(true, isAddCrossDay, jsonObject.optString("taskId"));
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK_SUCCESS, docs.optString("taskId"));
        } else {
            if (viewOperationWeakReference.get() != null)
                viewOperationWeakReference.get().sendMailResult(false, isAddCrossDay, null);
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK_FAIL);
        }
    }

    private void parseChangeTaskTime(JSONObject data) {
        boolean res = data.optBoolean("type");
        if (res && moveTime) {
            moveTime = false;
        }
        if (viewOperationWeakReference.get() != null) {
            viewOperationWeakReference.get().changeTaskTimeResult(res);
        }
    }

    //TODO:
    private void parseWindowInfoResult(JSONObject data) {
        isGetChatWidowInfo = false;
        ChatWindowInfoBean mBean = new ChatWindowInfoBean(data.optJSONObject("data"));
        mBean.taskId = data.optString("ClientId");
//        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_GROUP_INFO_WINDOW, mBean);
        if (viewOperationWeakReference.get() != null) {
            viewOperationWeakReference.get().getChatWindowInfoSuccess(true, mBean, currentTaskBean);
        }
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, mBean);
    }


    private void getChatWindowInfo(String taskId) {
        JSONObject body = new JSONObject();
        try {
            body.put("taskId", taskId);
            body.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
            Log.d(TAG, "getChatWindowInfo() called with: " + "taskId = [" + taskId + "]");
            createRequestJson(body, taskId);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getChatWindowInfo", getChatWindowInfoCallback, body, null);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_CHAT_WINDOW_INFO, createRequestJson(body, taskId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void praseReciveTaskChatMsg(JSONObject jsonObject) {
        int type = jsonObject.optInt("type");
        initUnReadDataStructs();
        if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {
            String groupId = jsonObject.optString("groupId");
            String msgId = jsonObject.optString("mid");
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, groupId, msgId);
            if (removeUnReadMsgById(msgId)) {
//                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
                if (viewOperationWeakReference.get() != null) {
                    viewOperationWeakReference.get().showUnReadMsg();
                }
            }
            return;
        }
        MessageBean messageBean = new MessageBean(jsonObject);
        messageBean.sendTime = System.currentTimeMillis();
        messageBean.isSendSuccess = true;
        messageBean.isMessageNew = false;
        if (jsonObject.has("mid"))
            messageBean.id = jsonObject.optString("mid");

        if (messageBean.type == MessageBean.RECALL_MESSAGE) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 2, messageBean.groupId, messageBean.id);
            removeRecallUnRead(messageBean);
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
            if (viewOperationWeakReference.get() != null) {
                viewOperationWeakReference.get().showUnReadMsg();
            }
            return;
        }

        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);

        if (jsonObject.has("exitUser")) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT,
                    jsonObject.optString("exitUser"), jsonObject.optString("groupId"));
        }

        if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id) || messageBean.type == MessageBean.SYSTEM_TIP_MESSAGE
                || jsonObject.optInt("isRead") == 1)
            return;

        UnReadMessageBean unReadBean = new UnReadMessageBean(jsonObject);
        unReadBean.type = MessageBean.MESSAGE_TASK_CHAT;
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_UNREAD_MESSAGE, unReadBean);
        unReadMessageList.add(unReadBean);

        if (isCurrentToday(unReadBean)) { //当前选择的天和消息发送的是一天
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, unReadBean.taskId, 1);
            if (viewOperationWeakReference.get() != null) {
                viewOperationWeakReference.get().setTimeLineMsgCount(unReadBean.taskId, 1);
            }
        } else {
            if (viewOperationWeakReference.get() != null) {
                viewOperationWeakReference.get().setTimeLineMsgCount(null, 1);
            }
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, null, 1);
        }
    }

    private void praseExitTaskResult(JSONObject jsonObject) {
        String taskId = jsonObject.optString("ClientId");
        boolean result = jsonObject.optBoolean("type");

        if (result) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, taskId);
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_WINDOW_BY_ID, taskId);
            setUnReadMsgRead(taskId, null);
        }

        if (viewOperationWeakReference.get() != null) {
            viewOperationWeakReference.get().quitTaskResult(result, taskId);
        }
    }

    private void parseUnReadMessage(JSONObject result) {
        initUnReadDataStructs();
        JSONArray messageArray;
        if (result == null) {
            messageArray = new JSONArray();
        } else {
            messageArray = result.optJSONArray("data");
        }
//        JSONArray messageArray = result.optJSONArray("data");
        int count = messageArray.length();
        Log.i(TAG, "parseUnReadMessage: + messageArray length" + count);
        for (int i = 0; i < count; i++) {
            JSONObject message = messageArray.optJSONObject(i);
            if (message.optBoolean("notShowMe") && message.optJSONObject("from").
                    optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                continue;
            }
            int type = message.optInt("type");
            if (type == 1) { //任务聊天未读消息
                boolean isNeedNotify = false;
                if (message.optInt("isRead") == 0) {
                    UnReadMessageBean bean = new UnReadMessageBean(message);
                    bean.type = MessageBean.MESSAGE_TASK_CHAT;
                    netUnReadList.add(bean);
                    isNeedNotify = true;
                }
                String content = message.optString("content");

                try {
                    if (message.optBoolean("isEncrypt") || message.optInt("isMailMsg") > 0)
                        message.put("content", EncryptUtil.aesDecrypt(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String title = message.optString("title");
                MessageBean messageBean = new MessageBean(message);
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");
                messageBean.groupId = message.optString("groupId");
                unReadMap.add(messageBean);
                if (!messageBean.isCurrentUserMsg() && isNeedNotify) {
                    if (messageBean.isEmojiMessage()) {
                        Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.SEND_NOTIFICATION, title, messageBean);
                    } else {
                        Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.SEND_NOTIFICATION, title, messageBean);
                    }
                }
            } else if (type == MessageActions.MESSAGE_RECALL && !TextUtils.isEmpty(message.optString("taskId"))) {//任务聊天的撤回消息
                MessageBean messageBean = new MessageBean(message);
                messageBean.groupId = message.optString("groupId");
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");

                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 3, messageBean);
            } else if (type == MessageActions.MESSAGE_TO_READ && !TextUtils.isEmpty(message.optString("taskId"))) {//设置某任务未读消息为已读
                readedTaskIds.add(message.optString("taskId"));
            }
        }


        if (netUnReadList.size() > 0) {
            unReadMessageList.addAll(netUnReadList);
            Log.i(TAG, "parseUnReadMessage: " + unReadMessageList.size());
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_UNREAD_MESSAGE_LIST, netUnReadList);
        }

        for (String taskId : readedTaskIds) {
            removeUnReadMsgById(taskId, null);
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, taskId);
        }


        if (unReadMap.size() > 0)
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_MESSAGE_LIST, unReadMap, 1);
        unreadStatus |= GET_NET_SUCCESS_MASK;

        Log.i(TAG, "parseUnReadMessage: " + netUnReadList.size());
        if (viewOperationWeakReference != null && viewOperationWeakReference.get() != null && ((unreadStatus & GET_LOCAL_SUCCESS_MASK) > 0)) {
            Collections.sort(unReadMessageList);
            //noinspection unchecked
            viewOperationWeakReference.get().showUnReadMsg();
        }
    }

    private void initUnReadDataStructs() {
        if (netUnReadList == null)
            netUnReadList = new ArrayList<>();

        netUnReadList.clear();
        if (unReadMessageList == null) {
            unReadMessageList = new ArrayList<>();
        }
        if (unReadMap == null)
            unReadMap = new ArrayList<>();
        unReadMap.clear();

        if (readedTaskIds == null)
            readedTaskIds = new ArrayList<>();
        readedTaskIds.clear();
    }

    private void sendMailToServer(TaskBean taskBean) {
        try {
            JSONObject requestJson;
            if (TextUtils.isEmpty(taskBean.taskId)) {
                requestJson = taskBean.toJson();
            } else {
                JSONObject taskInfo = new JSONObject();
                taskInfo.put("taskId", taskBean.taskId);
                taskInfo.put("subject", taskBean.taskContent);
                requestJson = taskBean.toEditJson();
                createRequestJson(requestJson, taskInfo.toString());
//                requestJson = createRequestJson(taskBean.toJson(), taskInfo.toString());
                OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "modifyTaskSubjectOrCustoms", requestJson, editTaskCallback, null);
                return;
            }
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SEND_MAIL, requestJson);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "addNewTask", requestJson, addTaskCallback, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int removeUnReadMsgById(String taskId, String groupId) {
        Iterator<UnReadMessageBean> iterator = unReadMessageList.iterator();
        int deleteNum = 0;
        while (iterator.hasNext()) {
            UnReadMessageBean mBean = iterator.next();
            if (mBean.taskId.equals(taskId) || mBean.groupId.equals(groupId)) {
                iterator.remove();
                ++deleteNum;
            }
        }
        return deleteNum;
    }

    private boolean removeUnReadMsgById(String msgId) {
        Iterator<UnReadMessageBean> iterator = unReadMessageList.iterator();
        boolean isChanged = false;
        while (iterator.hasNext()) {
            UnReadMessageBean unReadMessageBean = iterator.next();
            if (unReadMessageBean.msgId.equals(msgId)) {
                iterator.remove();
                isChanged = true;
            }
        }
        return isChanged;
    }

/*    private void changeTaskTime(TaskBean taskBean, CalendarDateBean calendarDateBean) {

        if (calendarDateBean != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(calendarDateBean.getYear(), calendarDateBean.getMonth(), calendarDateBean.getDay());
            calendar.set(Calendar.SECOND, 1);
            long firstTime = calendar.getTimeInMillis();
            long startOffSet = taskBean.startTime - DateUtils.getStartPointOfDay(taskBean.startTime);
            long endOffSet = taskBean.endTime - taskBean.startTime;
            taskBean.startTime = firstTime + startOffSet;
            taskBean.endTime = taskBean.startTime + endOffSet;
            moveTime = true;
        } else {
            moveTime = false;
        }

        try {
            JSONObject requestJson = createRequestJson(taskBean.getChangeTimeBean());
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, CHANGE_TASK_TIME, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/


    private void removeRecallUnRead(MessageBean messageBean) {
        String msgId = messageBean.id;
        boolean isChanged = removeUnReadMsgById(msgId);
        if (isChanged) {
            if (viewOperationWeakReference.get() != null)
                viewOperationWeakReference.get().showUnReadMsg();
        }
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
    }

    private boolean isCurrentToday(UnReadMessageBean unReadBean) {
        return unReadBean.createYear == selectedDay.get(Calendar.YEAR) && unReadBean.createMonth == selectedDay.get(Calendar.MONTH) && unReadBean.createDay == selectedDay.get(Calendar.DATE);
    }

    public void setSelectedDay(Calendar selectedDay) {
        this.selectedDay = selectedDay;
    }


    public void setViewOperationWeakReference(WeakReference<TimeLineViewOperation> viewOperationWeakReference) {
        this.viewOperationWeakReference = viewOperationWeakReference;
    }

    public ArrayList<TaskBean> getTodayTaskList() {
        //noinspection unchecked
        return todayTaskList;
    }

    public ArrayList<TaskBean> getCrossDayTaskList() {
        //noinspection unchecked
        return crossDayTaskList;
    }

    public ArrayList<UnReadMessageBean> getUnReadMessageList() {
        //noinspection unchecked
        return unReadMessageList;
    }

    public boolean isHaveUnReadMsg() {
        return unReadMessageList != null && unReadMessageList.size() > 0;
    }

    /**
     * 获取某天的任务列表
     *
     * @param time
     */
    public void getMailList(long time) {
        try {

            JSONObject requestJson1 = new JSONObject();
            requestJson1.put("type", 2 + "");
            requestJson1.put("dateNow", time + "");
            requestJson1.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            UUID uuid = UUID.randomUUID();
            needUuid = uuid.toString();

            requestJson1.put("ClientId", needUuid);
            createRequestJson(requestJson1, needUuid);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_MAIL_LIST, requestJson2, needUuid);
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getTaskList", getMailListCallBack, requestJson1, needUuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getWindownInfoByTaskId(TaskBean taskBean) {
        if (!isGetChatWidowInfo) {
            isGetChatWidowInfo = true;
            currentTaskBean = taskBean;
            dispatcher.dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID, currentTaskBean.taskId);
        }
    }


    public void changeTaskTime(TaskBean taskBean, CalendarDateBean calendarDateBean) {
        if (calendarDateBean != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(calendarDateBean.getYear(), calendarDateBean.getMonth(), calendarDateBean.getDay());
            calendar.set(Calendar.SECOND, 1);
            long firstTime = calendar.getTimeInMillis();
            long startOffSet = taskBean.startTime - DateUtils.getStartPointOfDay(taskBean.startTime);
            long endOffSet = taskBean.endTime - taskBean.startTime;
            taskBean.startTime = firstTime + startOffSet;
            taskBean.endTime = taskBean.startTime + endOffSet;
            moveTime = true;
        } else {
            moveTime = false;
        }

        try {
            JSONObject requestJson = createRequestJson(taskBean.getChangeTimeBean());
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "setCustomProperty", requestJson, changeTaskTimeCallback, null);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, CHANGE_TASK_TIME, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析返回的任务数据
     *
     * @param jsonObject
     */
    private void praseMailList(JSONObject jsonObject) {
        initCrossTaskList();
        initTaskList();
        boolean isNoon = false, isAfterNoon = false, isNight = false;
//        JSONArray docJson = jsonObject.optJSONArray("docs");
        JSONArray docJson = jsonObject.optJSONArray("data");
        int count = docJson.length();
        for (int i = 0; i < count; i++) {
            JSONObject jsonObject1 = docJson.optJSONObject(i);
            TaskBean mBean = new TaskBean(jsonObject1);
            if (mBean.isTodayTask()) {
                todayTaskList.add(mBean);
                todayTaskIds.add(mBean.taskId);
                switch (mBean.unClearTime) {
                    case TaskBean.NOON:
                        isNoon = true;
                        break;
                    case TaskBean.AFTERNOON:
                        isAfterNoon = true;
                        break;
                    case TaskBean.NIGHT:
                        isNight = true;
                        break;
                }
            } else {
                crossDayTaskList.add(mBean);
                crossDayTaskIds.add(mBean.taskId);
            }
        }
        Collections.sort(crossDayTaskList, new CrossTaskBeanComparator());
        crossDayTaskList.add(TaskBean.createEmptyTaskBean(TaskBean.EMPTY_CROSS_DAY_TASK));
        if (!isNoon) {
            TaskBean noonEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_NOON);
            todayTaskList.add(noonEmpty);
        }
        if (!isAfterNoon) {
            TaskBean afterNoonEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_AFTERNOON);
            todayTaskList.add(afterNoonEmpty);
        }
        if (!isNight) {
            TaskBean nightEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_NIGHT);
            todayTaskList.add(nightEmpty);
        }
        if (todayTaskBeanComparator == null)
            todayTaskBeanComparator = new TodayTaskBeanComparator();
        Collections.sort(todayTaskList, todayTaskBeanComparator);
        if (isNight || isNoon || isAfterNoon) {
            TaskBean emptyTodayBean = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_TODAY_TASK);
            todayTaskList.add(emptyTodayBean);
        }
        taskIds.addAll(todayTaskIds);
        taskIds.addAll(crossDayTaskIds);

        if (viewOperationWeakReference.get() != null)
            //noinspection unchecked
            viewOperationWeakReference.get().showTaskList(todayTaskList, crossDayTaskList);
    }

    private void initTaskList() {
        if (todayTaskList == null)
            todayTaskList = new ArrayList<>();
        todayTaskList.clear();

        if (todayTaskIds == null)
            todayTaskIds = new ArrayList<>();
        todayTaskIds.clear();

        if (taskIds == null)
            taskIds = new ArrayList<>();
        taskIds.clear();

        if (crossDayTaskIds == null)
            crossDayTaskIds = new ArrayList<>();
        crossDayTaskIds.clear();
    }

    private void initCrossTaskList() {
        if (crossDayTaskList == null)
            crossDayTaskList = new ArrayList<>();
        crossDayTaskList.clear();
    }

    /**
     * 查询本地的未读消息
     */
    public void getLocalUnReadMsg() {
        dispatcher.dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE);
    }

    /**
     * 删除一个任务根据任务ID
     *
     * @param taskId
     */
    public void quitTaskByTaskId(String taskId) {
        JSONObject body = new JSONObject();
        try {
            body.put("taskId", taskId);
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
//            body.put("exitDay", System.currentTimeMillis());
            createRequestJson(body, taskId);
            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "exitTask", body, exitTaskCallback, null);
//            OkHttpClientManager.getInstance().post(AppConfig.HTTP_SERVER_IP + "deleteTask", body, exitTaskCallback, null);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, EXIT_TASK, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从本地数据库查询未读消息成功
     *
     * @param unReadMessageBeans
     */
    private void queryUnReadSuccess(ArrayList<UnReadMessageBean> unReadMessageBeans) {
        if ((unreadStatus & GET_NET_SUCCESS_MASK) > 0) {
            if (this.unReadMessageList == null) {
                unReadMessageList = unReadMessageBeans;
            } else {
                unReadMessageList.clear();
                unReadMessageList.addAll(unReadMessageBeans);
            }
        } else {
            unReadMessageList = unReadMessageBeans;
        }
        unreadStatus |= GET_LOCAL_SUCCESS_MASK;
        if (viewOperationWeakReference.get() != null && (unreadStatus & GET_NET_SUCCESS_MASK) > 0) {
            Collections.sort(unReadMessageList);
            //noinspection unchecked
            viewOperationWeakReference.get().showUnReadMsg();
        }
    }

    /**
     * 查询网络的未读消息
     */
    private void getNetUnReadMessage() {
        JSONObject body = new JSONObject();
        try {
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("isPhone", true);
            body.put("version", 132);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_UN_READ_MESSAGE, createRequestJson(body));
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getMcUnReadMsg", getUnReadCallback, createRequestJson(body), "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String attachTaskId;

    private void getTaskAttachList(String taskId) {
        attachTaskId = taskId;
        JSONObject body = new JSONObject();
        try {
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("taskId", taskId);
//            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_TASK_ATTACHMENT_LIST, createRequestJson(body));
            OkHttpClientManager.getInstance().getAsyn(AppConfig.HTTP_SERVER_IP + "getTaskAttachmentList", getAttachmentCallback, body, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int setUnReadMsgRead(String taskId, String groupId) {
        int removeNum = removeUnReadMsgById(taskId, groupId);
        if (removeNum > 0) {
//            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, taskId, 0);
            if (viewOperationWeakReference.get() != null)
                viewOperationWeakReference.get().setTimeLineMsgCount(taskId, 0);
        }
        return removeNum;
    }


    private JSONObject createRequestJson(JSONObject body, String clientId) throws JSONException {
        body.put("ClientId", clientId);
        return body;
    }

    private JSONObject createRequestJson(JSONObject body) throws JSONException {
        return body;
    }

    public boolean isCanGetMailList() {
        return true;
    }

    public ArrayList<String> getTaskIds() {
        return taskIds;
    }

    public Calendar getSelectedDay() {
        return selectedDay;
    }

    public void addCrossDayTask(TaskBean taskBean) {
        isAddCrossDay = true;
        sendMailToServer(taskBean);
    }

    public void addTodayTask(TaskBean taskBean) {
        isAddCrossDay = false;
        sendMailToServer(taskBean);
    }

    private Callback getMailListCallBack = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            Log.i(TAG, "onFailure: " + e.getMessage());
            String requestId = request.request().header("requestId");
            if (requestId.equals(needUuid)) {
                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_GET_TASK_TIME_OUT);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    if (res.has("code")) {
                        return;
                    }
                    Log.i(TAG, "onResponse: getMailList" + res.toString());
                    String uuid = res.optString("ClientId");
                    if (!uuid.equals(needUuid)) {
                        return;
                    }
                    if (res.optBoolean("type"))
                        praseMailList(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Callback addTaskCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            Log.i(TAG, "onFailure: " + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    if (res.has("code")) {
                        return;
                    }
                    Log.i(TAG, "onResponse: addTask" + res.toString());
                    praseAddMailResult(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Callback getChatWindowInfoCallback = new Callback() {

        @Override
        public void onFailure(Call request, IOException e) {
            isGetChatWidowInfo = false;
            if (viewOperationWeakReference.get() != null) {
                viewOperationWeakReference.get().getChatWindowInfoSuccess(false, null, null);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: getChatWindowInfo" + res.toString());
                if (res.has("code")) {
                    return;
                }
                parseWindowInfoResult(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback exitTaskCallback = new Callback() {

        @Override
        public void onFailure(Call request, IOException e) {
            Log.i(TAG, "onFailure: " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: exitTask" + res.toString());
                if (res.has("code")) {
                    return;
                }
                praseExitTaskResult(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback changeTaskTimeCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: changeTaskTime" + res.toString());
                if (res.has("code")) {
                    return;
                }
                parseChangeTaskTime(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback getUnReadCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {
            parseUnReadMessage(null);
            MessageStores.getInstance().praseUnReadMsgList(null);
//            getLocalUnReadMsg();
//            getNetUnReadMessage();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: getUnReadMsg" + res.toString());

                if (!res.optBoolean("type")) {
                    parseUnReadMessage(null);
                    return;
                }
                if (res.has("code")) {
                    return;
                }
                parseUnReadMessage(res);
                MessageStores.getInstance().praseUnReadMsgList(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback editTaskCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: editTask" + res.toString());
                if (res.has("code")) {
                    return;
                }
                praseAddMailResult(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback getAttachmentCallback = new Callback() {
        @Override
        public void onFailure(Call request, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject res = new JSONObject(response.body().string());
                Log.i(TAG, "onResponse: getAttachment:" + res.toString());
                if (res.has("code")) {
                    return;
                }
                praseTaskAttachmentResult(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void clean() {
        unRegister();
    }
}
