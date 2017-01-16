package com.inspur.playwork.stores.timeline;

import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.actions.timeline.TimeLineActions;
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
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.view.timeline.RecyclerTaskAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by fan on 15-8-22.
 */
public class TimeLineStores extends Stores {

    private static final String TAG = "TimeLineStoresFan";

    private static final String GET_MAIL_LIST = "getMailList";
    private static final String SEND_MAIL = "sendMail";
    private static final String UPDATE_TOPIC_SUBJECT = "updateTopicSubject";
    private static final String EXIT_TASK = "exitTask";
    private static final String GET_TASK_ATTACHMENT_LIST = "getTaskAttachmentList";
    private static final String GET_CHAT_WINDOW_INFO = "getChatWindowInfo";
    private static final String GET_UN_READ_MESSAGE = "getMcUnReadMsg";
    private static final String CHANGE_TASK_TIME = "setCustomProperty";

    private ArrayList<TaskBean> todayTaskList;
    private ArrayList<TaskBean> crossDayTaskList;

    private ArrayList<TaskAttachmentBean> attachmentList;

    private ArrayList<UnReadMessageBean> unReadMessageList;

    private Calendar selectedDay;

    private String needUuid;

    private TaskBean currentTaskBean;

    private ArrayList<String> todayTaskIds;

    private ArrayList<String> crossDayTaskIds;

    private ArrayList<String> taskIds;

    private TodayTaskBeanComparator todayTaskBeanComparator;

    private ArrayList<MessageBean> unReadMap;
    private boolean moveTime;

    private boolean isGetChatWidowInfo;


    public static TimeLineStores getInstance() {
//        return SingleRefreshManager.getInstance().getTimeLineStores();
        return null;
    }

    public TimeLineStores() {
        super(Dispatcher.getInstance());
    }

    @SuppressWarnings("unused")
    public void onEvent(StoreAction action) {
        switch (action.getActionType()) {
            case TimeLineActions.TIME_LINE_INIT_TASK_LIST:   //初始化任务列表，刚进入应用时
                getMailList((Long) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_REQUEST_TASK_LIST:  //用户点击某天时加载当天的任务列表
                getMailList((Long) action.getActiontData().get(0));
                break;
            case CommonActions.REVICE_TIMELINE_DATA_FROM_SERVER:   //从网络上获取了数据
                String type = (String) action.getActiontData().get(0);
                praseServerReturnData(action, type);
                break;
            case TimeLineActions.TIME_LINE_ADD_EDIT_TASK:
                sendMailToServer((TaskBean) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_DELETE_TASK:
//                deleteTask((String) action.getActiontData().get(0));
                quitTask((String) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_QUIT_TASK:
                quitTask((String) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST:
                getTaskAttachList((String) action.getActiontData().get(0));
                break;
            case TimeLineActions.TIME_LINE_GET_WINDOW_INFO:
                if (!isGetChatWidowInfo) {
                    isGetChatWidowInfo = true;
                    currentTaskBean = (TaskBean) action.getActiontData().get(0);
                    dispatcher.dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID, currentTaskBean.taskId);
                }
                break;
            case TimeLineActions.TIME_LINE_GET_UNREAD_MESSAGE:
                getUnReadMessage();
                break;
            case TimeLineActions.TIME_LINE_CHANGE_TASK_TIME:
                changeTaskTime((TaskBean) action.getActiontData().get(0), (CalendarDateBean) action.getActiontData().get(1));
                break;
            case MessageActions.SET_UNREAD_MSG_READ:
                setUnReadMsgRead((String) action.getActiontData().get(0), (String) action.getActiontData().get(1));
                break;
            case MessageActions.RECIVE_UNREAD_TASK_CHAT_MSG:
                praseReciveTaskChatMsg((JSONObject) action.getActiontData().get(0));
                break;
/*            case MessageActions.REMOVE_UNREAD_MESSAGE:
                getUnReadMap().remove(action.getActiontData().get(0));
                break;*/
            case CommonActions.GET_TIMELINE_DATA_FROM_SERVER_TIME_OUT:
                if (action.getActiontData().get(0).equals(GET_MAIL_LIST)) {
                    String uuid = (String) action.getActiontData().get(1);
                    if (uuid.equals(needUuid)) {
                        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_GET_TASK_TIME_OUT);
                    }
                }
                break;
            case NetWorkActions.CONNECT_TO_TIMELINE_SERVER_SUCCESS:
                isGetChatWidowInfo = false;
                break;
            case DataBaseActions.QUERY_CHAT_UNREAD_MSG_SUCCESS:
                if ((int) action.getActiontData().get(0) == 0) {
                    //noinspection unchecked
                    queryUnReadSuccess(((ArrayList<UnReadMessageBean>) action.getActiontData().get(1)));
                }
                break;
            case DataBaseActions.QUERY_CHAT_BY_ID_SUCCESS:
                ChatWindowInfoBean windowInfoBean = (ChatWindowInfoBean) action.getActiontData().get(0);
                if (!TextUtils.isEmpty(windowInfoBean.taskId) && (int) action.getActiontData().get(1) == 2) {
                    isGetChatWidowInfo = false;
                    dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_GROUP_INFO_WINDOW, windowInfoBean);
                }
                break;
            case DataBaseActions.QUERY_RESULT_IS_EMPTY:
                int emptyType = (int) action.getActiontData().get(0);
                if (emptyType == DataBaseActions.QUERY_CHAT_WINDOW_INFO_BY_ID) {
                    if (action.getActiontData().get(2) == null) {
                        String taskId = (String) action.getActiontData().get(1);
                        getChatWindowInfo(taskId);
                    }
                }
                break;
            case TimeLineActions.TIME_LINE_SET_MESSAGE_TO_READ:
                setUnReadMsgRead((String) action.getActiontData().get(0), (String) action.getActiontData().get(1));
                break;
            case TimeLineActions.TIME_LINE_SORT_TASK_NUM:
//                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).sortTask((JSONObject) action.getActionData().get(0));
                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SORT_TASK_NUM, action.getActiontData().get(0));
                break;
        }
    }

    private void queryUnReadSuccess(ArrayList<UnReadMessageBean> unReadMessageBeans) {
        if (this.unReadMessageList == null) {
            unReadMessageList = unReadMessageBeans;
        } else {
            unReadMessageList.addAll(unReadMessageBeans);
        }
    }

    private void changeTaskTime(TaskBean taskBean, CalendarDateBean calendarDateBean) {

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
    }

    /**
     * 收到了一条时间轴消息，但当前不再时间轴聊天界面
     */
    private void praseReciveTaskChatMsg(JSONObject jsonObject) {
        int type = jsonObject.optInt("type");
        initUnReadDataStructs();
        if (type == MessageActions.MESSAGE_DELETE_ONE_MSG) {
            String groupId = jsonObject.optString("groupId");
            String msgId = jsonObject.optString("mid");
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_ONE_MSG_BY_MSGID, groupId, msgId);
            if (removeUnReadMsgById(msgId)) {
                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
            }
            return;
        }
        MessageBean messageBean = new MessageBean(jsonObject);
        messageBean.sendTime = System.currentTimeMillis();
        messageBean.isSendSuccess = true;
        messageBean.isMessageNew = false;
        messageBean.id = jsonObject.optString("mid");

        if (messageBean.type == MessageBean.RECALL_MESSAGE) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 2, messageBean.groupId, messageBean.id);
            removeRecallUnRead(messageBean);
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
            return;
        }

        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, messageBean);

        if (jsonObject.has("exitUser")) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.UPDATE_WINDOW_INFO_WHEN_MEMBER_EXIT, jsonObject);
        }

        if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id) || messageBean.type == MessageBean.SYSTEM_TIP_MESSAGE)
            return;

        UnReadMessageBean unReadBean = new UnReadMessageBean(jsonObject);
        unReadBean.type = MessageBean.MESSAGE_TASK_CHAT;
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_UNREAD_MESSAGE, unReadBean);
        unReadMessageList.add(unReadBean);

        if (isCurrentToday(unReadBean)) { //当前选择的天和消息发送的是一天
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, unReadBean.taskId, 1);
        } else {
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, null, 1);
        }
    }

    private void removeRecallUnRead(MessageBean messageBean) {
        String msgId = messageBean.id;
        boolean isChanged = removeUnReadMsgById(msgId);
        if (isChanged)
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
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

    private boolean isCurrentToday(UnReadMessageBean unReadBean) {
        return unReadBean.createYear == selectedDay.get(Calendar.YEAR) && unReadBean.createMonth == selectedDay.get(Calendar.MONTH) && unReadBean.createDay == selectedDay.get(Calendar.DATE);
    }

    private void setUnReadMsgRead(String taskId, String groupId) {
        removeUnReadMsgById(taskId, groupId);
        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_SET_UNREAD_MSG, taskId, 0);
    }

    private void removeUnReadMsgById(String taskId, String groupId) {
        Iterator<UnReadMessageBean> iterator = unReadMessageList.iterator();
        while (iterator.hasNext()) {
            UnReadMessageBean mBean = iterator.next();
            if (mBean.taskId.equals(taskId) || mBean.groupId.equals(groupId))
                iterator.remove();
        }
    }

    private void getChatWindowInfo(String taskId) {
        JSONObject body = new JSONObject();
        try {
            body.put("taskId", taskId);
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_CHAT_WINDOW_INFO, createRequestJson(body, taskId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void praseServerReturnData(StoreAction action, String type) {
        JSONObject data = (JSONObject) action.getActiontData().get(1);
        switch (type) {
            case GET_MAIL_LIST:
                String uuid = data.optString("ClientId");
                if (!uuid.equals(needUuid)) {
                    return;
                }
                praseMailList(data);
                break;
            case SEND_MAIL:
                praseAddMailResult(data);
                break;
            case UPDATE_TOPIC_SUBJECT:
                praseDeleteTaskResult(data);
                break;
            case EXIT_TASK:
                praseExitTextResult(data);
                break;
            case GET_TASK_ATTACHMENT_LIST:
                praseTaskAttachmentResult(data);
                break;
            case GET_CHAT_WINDOW_INFO:
                parseWindowInfoResult(data);
                break;
            case GET_UN_READ_MESSAGE:
                parseUnReadMessage(data);
                break;
            case CHANGE_TASK_TIME:
                parseChangeTaskTime(data);
                break;
        }
    }

    private void parseChangeTaskTime(JSONObject data) {
//        JSONObject docsJson = data.optJSONObject("docs");
        if (!data.has("res")) {
            boolean res = data.optBoolean("docs");
            if (res && moveTime) {
                moveTime = false;
                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_CHANGE_TASK_TIME_SUCCESS);
            } else {
                dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_CHANGE_TASK_TIME_FAILED);
            }
        }
    }

    private ArrayList<UnReadMessageBean> netUnReadList;

    private ArrayList<String> readedTaskIds;

    private void parseUnReadMessage(JSONObject result) {

        initUnReadDataStructs();
//        unReadMessageList.clear();
        JSONArray messageArray = result.optJSONArray("docs");
        int count = messageArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject message = messageArray.optJSONObject(i);
            int type = message.optInt("type");
            if (type == 1) {
                if (message.optInt("isRead") == 0) {
                    UnReadMessageBean bean = new UnReadMessageBean(message);
                    bean.type = MessageBean.MESSAGE_TASK_CHAT;
                    netUnReadList.add(bean);
                }
                //if(bean.type  == MessageBean.)
                String content = message.optString("content");

                try {
                    if (message.optBoolean("isEncrypt") || message.optInt("isMailMsg") > 0)
                        message.put("content", EncryptUtil.aesDecrypt(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MessageBean messageBean = new MessageBean(message);
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");
                messageBean.groupId = message.optString("groupId");
                //messageBean.type = MessageBean.MESSAGE_TASK_CHAT;
      /*          if (unReadMap.get(bean.groupId) == null) {
                    unReadMap.put(bean.groupId, new ArrayList<MessageBean>());
                }*/
                unReadMap.add(messageBean);
            } else if (type == MessageActions.MESSAGE_RECALL && !TextUtils.isEmpty(message.optString("taskId"))) {
                MessageBean messageBean = new MessageBean(message);
                messageBean.groupId = message.optString("groupId");
                messageBean.isSendSuccess = true;
                messageBean.isMessageNew = false;
                messageBean.id = message.optString("mid");

                if (message.optInt("isRead") == 0) {
                    UnReadMessageBean bean = new UnReadMessageBean(message);
                    bean.type = MessageBean.MESSAGE_TASK_CHAT;
                    bean.content = messageBean.content;
                    netUnReadList.add(bean);
                }
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_MESSAGE_BY_MESSAGE_UUID, 3, messageBean);
            } else if (type == MessageActions.MESSAGE_TO_READ && !TextUtils.isEmpty(message.optString("taskId"))) {
                readedTaskIds.add(message.optString("taskId"));
            }
        }

        for (String taskId : readedTaskIds) {
            removeUnReadMsgById(taskId, null);
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, taskId);
        }

        if (netUnReadList.size() > 0) {
            unReadMessageList.addAll(netUnReadList);
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_UNREAD_MESSAGE_LIST, netUnReadList);
        }
        if (unReadMap.size() > 0)
            dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_MESSAGE_LIST, unReadMap, 1);
        Collections.sort(unReadMessageList);
        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE, unReadMessageList);
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

    private void parseWindowInfoResult(JSONObject data) {
        isGetChatWidowInfo = false;
        ChatWindowInfoBean mBean = new ChatWindowInfoBean(data.optJSONObject("docs"));
        mBean.taskId = data.optString("ClientId");
        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_RECIVE_GROUP_INFO_WINDOW, mBean);
        dispatcher.dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, mBean);
    }

    private String attachTaskId;

    private void getTaskAttachList(String taskId) {
        attachTaskId = taskId;
        JSONObject body = new JSONObject();
        try {
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("taskId", taskId);
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_TASK_ATTACHMENT_LIST, createRequestJson(body));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void praseTaskAttachmentResult(JSONObject data) {
        initAttachMentList();
        JSONArray json = data.optJSONArray("docs");
        int count = json.length();
        for (int i = 0; i < count; i++) {
            JSONObject object = json.optJSONObject(i);
            TaskAttachmentBean mBean = new TaskAttachmentBean(object, attachTaskId);
            attachmentList.add(mBean);
        }
        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST, attachmentList);
    }

/*    private void deleteTask(String taskId) {
        JSONObject body = new JSONObject();
        try {
            body.put("taskId", taskId);
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("subject", "");
            JSONObject requestJson = createRequestJson(body, taskId);
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, UPDATE_TOPIC_SUBJECT, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    private void praseDeleteTaskResult(JSONObject jsonObject) {
        if (jsonObject.optBoolean("docs")) {
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_DELETE_TASK_SUCCESS);
        } else {
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_DELETE_TASK_FAILED);
        }
    }

    private void quitTask(String taskId) {
        JSONObject body = new JSONObject();
        try {
            body.put("taskId", taskId);
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            JSONObject requestJson = createRequestJson(body, taskId);
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, EXIT_TASK, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void praseExitTextResult(JSONObject jsonObject) {
        Log.i(TAG, "praseExitTextResult: " + jsonObject.toString());
        String taskId = jsonObject.optString("ClientId");
        if (jsonObject.optBoolean("docs")) {
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, taskId);
            dispatcher.dispatchDataBaseAction(DataBaseActions.DELETE_CHAT_WINDOW_BY_ID, taskId);
            setUnReadMsgRead(taskId, null);
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_QUIT_TASK_SUCCESS);
        } else {
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_QUIT_TASK_FAILED);
        }
    }

    private void sendMailToServer(TaskBean taskBean) {
        try {
            JSONObject requestJson;
            if (TextUtils.isEmpty(taskBean.taskId))
                requestJson = createRequestJson(taskBean.toJson());
            else {
                JSONObject taskInfo = new JSONObject();
                taskInfo.put("taskId", taskBean.taskId);
                taskInfo.put("subject", taskBean.taskContent);
                requestJson = createRequestJson(taskBean.toJson(), taskInfo.toString());
            }
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, SEND_MAIL, requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void praseAddMailResult(JSONObject jsonObject) {
        JSONObject docs = jsonObject.optJSONObject("docs");
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
        if (docs.optBoolean("result")) {
            Log.i(TAG, "praseAddMailResult: " + docs.optString("taskId"));
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK_SUCCESS, docs.optString("taskId"));
        } else {
            dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK_FAIL);
        }
    }

    private void getMailList(long time) {
        try {
            JSONObject requestJson1 = new JSONObject();
            requestJson1.put("type", 2);
            requestJson1.put("dateNow", time);
            requestJson1.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            UUID uuid = UUID.randomUUID();
            needUuid = uuid.toString();

            JSONObject requestJson2 = createRequestJson(requestJson1, needUuid);
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_MAIL_LIST, requestJson2, needUuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void praseMailList(JSONObject jsonObject) {
        initCrossTaskList();
        initTaskList();
        boolean isNoon = false, isAfterNoon = false, isNight = false;
        JSONArray docJson = jsonObject.optJSONArray("docs");
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

        dispatcher.dispatchUpdateUIEvent(TimeLineActions.TIME_LINE_INIT_TASK_LIST, todayTaskList, crossDayTaskList);
    }

    public void getUnReadMessage() {
        JSONObject body = new JSONObject();
        try {
            body.put("userId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
            body.put("isPhone", true);
            dispatcher.dispatchNetWorkAction(CommonActions.GET_TIMELINE_DATA_FROM_SERVER, GET_UN_READ_MESSAGE, createRequestJson(body));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void initAttachMentList() {
        if (attachmentList == null)
            attachmentList = new ArrayList<>();
        attachmentList.clear();
    }

    public ArrayList<String> getTaskIds() {
        return taskIds;
    }

//    public ArrayMap<String, ArrayList<MessageBean>> getUnReadMap() {
//        return unReadMap;
//    }

    public TaskBean getCurrentTaskBean() {
        return currentTaskBean;
    }

    public Calendar getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Calendar selectedDay) {
        this.selectedDay = selectedDay;
    }

    public boolean isCanGetMailList() {
        return PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_TIME_LINE);
    }

    public ArrayList<UnReadMessageBean> getUnReadMessageList() {
        return unReadMessageList;
    }

    private JSONObject createRequestJson(JSONObject body) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
        requestJson.put("Body", body);
        requestJson.put("isPhone", true);
        return requestJson;
    }

    private JSONObject createRequestJson(JSONObject body, String clientId) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("ConnectionId", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.CONNECTID));
        requestJson.put("Body", body);
        requestJson.put("ClientId", clientId);
        requestJson.put("isPhone", true);
        return requestJson;
    }
}
