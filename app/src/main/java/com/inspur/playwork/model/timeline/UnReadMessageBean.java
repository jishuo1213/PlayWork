package com.inspur.playwork.model.timeline;

import android.support.annotation.NonNull;

import com.inspur.playwork.utils.DateUtils;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Fan on 15-9-24.
 */
public class UnReadMessageBean implements Comparable<UnReadMessageBean> {

    private static final String TAG = "UnReadFan";

    public long msgSendTime;
    public long taskCreateTime;
    public String taskId;
    public String groupId;
    public String msgId;
    public String content;
    public int isNeedShowNum;
    public int year;
    public int month;
    public int day;
    public int type;//这条未读消息是任务还是微聊
    public int msgType;//此未读消息的类型，比如撤回的消息，删除的消息

    public int createYear;
    public int createMonth;
    public int createDay;
    public int createDayOfYear;
    public int createDayofWeek;

    private static Calendar calendar = Calendar.getInstance();

    public UnReadMessageBean(String groupId) {
        this.groupId = groupId;
    }

    public UnReadMessageBean(long msgSendTime, long taskCreateTime, String taskId, String groupId, int type) {
        this.msgSendTime = msgSendTime;
        this.taskCreateTime = taskCreateTime;
        this.taskId = taskId;
        this.groupId = groupId;
        this.type = type;

        calendar.setTimeInMillis(msgSendTime);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);
        calendar.clear();

        calendar.setTimeInMillis(taskCreateTime);
        createYear = calendar.get(Calendar.YEAR);
        createMonth = calendar.get(Calendar.MONTH);
        createDay = calendar.get(Calendar.DATE);
        createDayofWeek = calendar.get(Calendar.DAY_OF_WEEK);
        this.createDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    }

    public UnReadMessageBean(JSONObject unReadMessage) {
        msgSendTime = unReadMessage.optLong("sendTime");
        taskCreateTime = unReadMessage.optLong("createTime");
        type = unReadMessage.optInt("type");
        calendar.setTimeInMillis(msgSendTime);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);
        calendar.clear();
        calendar.setTimeInMillis(taskCreateTime);

        content = unReadMessage.optString("content");
        if (content.lastIndexOf("\n") != -1) {
            content = content.substring(0, content.length() - 1);
        }

        createYear = calendar.get(Calendar.YEAR);
        createMonth = calendar.get(Calendar.MONTH);
        createDay = calendar.get(Calendar.DATE);
        createDayofWeek = calendar.get(Calendar.DAY_OF_WEEK);
        this.createDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        taskId = unReadMessage.optString("taskId");
        groupId = unReadMessage.optString("groupId");
        msgId = unReadMessage.optString("mid");
//        int type = unReadMessage.optInt("type");
//        if (unReadMessage.optJSONObject("from").optString("id").equals(PreferencesHelper.getInstance().getCurrentUser().id) ||
//                type == ) {
//            isNeedShowNum = 0;
//        } else {
//            isNeedShowNum = 1;
//        }
    }

    @Override
    public int compareTo(@NonNull UnReadMessageBean another) {
        if (this.msgSendTime > another.msgSendTime) {
            return 1;
        } else if (this.msgSendTime < another.msgSendTime) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean isSelectDay(Calendar calendar) {
        return createDay == calendar.get(Calendar.DATE) &&
                createMonth == calendar.get(Calendar.MONTH) &&
                createYear == calendar.get(Calendar.YEAR);
    }

    public boolean isNeedShowUnReadNum() {
        return isNeedShowNum == 1;
    }

    @Override
    public String toString() {
        return "UnReadMessageBean{" +
                ",groupId=" + groupId +
                ",msgId=" + msgId +
                ",taskId=" + taskId +
                ",year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", createYear=" + createYear +
                ", createDay=" + createDay +
                ", createMonth=" + createMonth +
                ", type=" + type +
                ", taskCreateTime=" + taskCreateTime +
                ", msgSendTime=" + DateUtils.getCalendarAllText(msgSendTime) +
                ", createDayOfYear=" + createDayOfYear +
                ", isNeedShowNum=" + isNeedShowNum +
                '}';
    }
}
