package com.inspur.playwork.model.timeline;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by fan on 15-8-12.
 */
public class TaskBean implements Parcelable {

    private static final String TAG = "taskBeanFan";

    private static String currentUser = PreferencesHelper.getInstance().getCurrentUser().id;

    public static final int NOON = 1;//12
    public static final int AFTERNOON = 2;//13
    public static final int NIGHT = 3;//14

    public static final int EMPTY_NOON = 12;
    public static final int EMPTY_AFTERNOON = 13;//l
    public static final int EMPTY_NIGHT = 14;
    public static final int EMPTY_CROSS_DAY_TASK = 15;
    public static final int EMPTY_TODAY_TASK = 16;

    private static final int NO_END_TIME = 4;

    public static final int TODAY_TASK = 2;
    public static final int CROSS_DAY_TASK = 7;

    public static final int TODAY_TASK_TIME_UNCLEAR = 9;
    public static final int CROSS_DAY_TASK_TIME_UNCLEAR = 10;

    public String taskId;

    public String startTimeString;
    public String endTimeString;
    public String taskContent;
    public String taskPlace;
    public String taskCreator;
    public int setPrivate; // 公/私标识

    public int unReadMessageNum;

    public int taskType = -1;

    public long startTime;
    public long endTime;

    public int unClearTime;

    public int sortNum = Integer.MAX_VALUE;

    private Calendar tempCalendar;

//    public boolean isUserExit;

    public TaskBean() {
    }

    protected TaskBean(Parcel in) {
        taskId = in.readString();
        startTimeString = in.readString();
        endTimeString = in.readString();
        taskContent = in.readString();
        taskPlace = in.readString();
        taskCreator = in.readString();
        setPrivate = in.readInt();
        unReadMessageNum = in.readInt();
        taskType = in.readInt();
        startTime = in.readLong();
        endTime = in.readLong();
        unClearTime = in.readInt();
        sortNum = in.readInt();
    }

    public static final Creator<TaskBean> CREATOR = new Creator<TaskBean>() {
        @Override
        public TaskBean createFromParcel(Parcel in) {
            return new TaskBean(in);
        }

        @Override
        public TaskBean[] newArray(int size) {
            return new TaskBean[size];
        }
    };

    public static TaskBean createEmptyTaskBean(int taskType) {
        TaskBean mBean = new TaskBean();
        mBean.unClearTime = taskType;
        switch (taskType) {
            case EMPTY_NOON:
                mBean.startTimeString = "上午";
                break;
            case EMPTY_AFTERNOON:
                mBean.startTimeString = "下午";
                break;
            case EMPTY_NIGHT:
                mBean.startTimeString = "晚上";
                break;
            case EMPTY_CROSS_DAY_TASK:
                mBean.startTimeString = "";
                break;
            case EMPTY_TODAY_TASK:
                mBean.startTimeString = "";
                break;
            case -1:
                switch (DateUtils.getTimeNoonOrAfterNoon(Calendar.getInstance())) {
                    case 1:
                        return createEmptyTaskBean(EMPTY_NOON);
                    case 2:
                        return createEmptyTaskBean(EMPTY_AFTERNOON);
                    case 3:
                        return createEmptyTaskBean(EMPTY_NIGHT);
                }
        }
        return mBean;
    }

    public TaskBean(JSONObject json) {
        JSONObject custom = json.optJSONObject("Custom");
        int type = custom.optInt("timeType");
        taskId = json.optString("_id");
        taskPlace = custom.optString("taskPlace");
        taskCreator = custom.optString("creator");
        setPrivate = custom.optInt("setPrivate");
        if (json.has("SubjectToTask")) {
            taskContent = json.optString("SubjectToTask");
        } else if (json.has("Subject")) {
            taskContent = json.optString("Subject");
        } else {
            taskContent = custom.optString("taskTitle");
        }
        if (json.has("XH"))
            sortNum = json.optInt("XH");
        if (type < 7 && type > 1) {
            startTime = custom.optLong("startTime");
            endTime = custom.optLong("endTime");
            taskType = TODAY_TASK;
            this.startTimeString = DateUtils.getLongTimePointText(startTime);
            this.endTimeString = DateUtils.getLongTimePointText(endTime);
            unClearTime = DateUtils.getTimeNoonOrAfterNoon(startTime);
        } else if (type == 7) {
            startTime = custom.optLong("startDay");
            endTime = custom.optLong("endDay");
            this.startTimeString = DateUtils.getLongTimeDateText(startTime);
            this.endTimeString = DateUtils.getLongTimeDateText(endTime);
            taskType = CROSS_DAY_TASK;
        } else if (type == 10) {
            taskType = CROSS_DAY_TASK_TIME_UNCLEAR;
            startTime = custom.optLong("startDay");
            endTime = -1;
            this.startTimeString = DateUtils.getLongTimeDateText(startTime);
            this.endTimeString = "    ?    ";
            unClearTime = NO_END_TIME;
        } else if (type == 9) {
            taskType = TODAY_TASK_TIME_UNCLEAR;
            startTime = custom.optLong("startTime");
            endTime = custom.optLong("endTime");
            unClearTime = DateUtils.getTimePeriod(startTime);
            startTimeString = unClearTime == NOON ? "上午" : unClearTime == AFTERNOON ? "下午" : "晚上";
            this.endTimeString = startTimeString;
        }
    }

    public boolean isTodayTask() {
        return taskType == TODAY_TASK || taskType == TODAY_TASK_TIME_UNCLEAR || unClearTime == EMPTY_NOON
                || unClearTime == EMPTY_AFTERNOON || unClearTime == EMPTY_NIGHT || unClearTime == EMPTY_TODAY_TASK;
    }

    public boolean isTodayUnClearTask() {
        return taskType == TODAY_TASK_TIME_UNCLEAR;
    }


    public void setTimeString() {
        switch (taskType) {
            case TODAY_TASK:
                this.startTimeString = DateUtils.getLongTimePointText(startTime);
                this.endTimeString = DateUtils.getLongTimePointText(endTime);
                break;
            case TODAY_TASK_TIME_UNCLEAR:
                startTimeString = unClearTime == NOON ? "上午" : unClearTime == AFTERNOON ? "下午" : "晚上";
                this.endTimeString = startTimeString;
                break;
            case CROSS_DAY_TASK:
                this.startTimeString = DateUtils.getLongTimeDateText(startTime);
                this.endTimeString = DateUtils.getLongTimeDateText(endTime);
                break;
            case CROSS_DAY_TASK_TIME_UNCLEAR:
                this.startTimeString = DateUtils.getLongTimeDateText(startTime);
                this.endTimeString = "    ?    ";
                break;
        }
    }

    boolean isMineTask() {
        return taskCreator.equals(currentUser);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject custum = new JSONObject();
//        JSONObject from = new JSONObject();

        //String taskContent = CharsetCoder.getInstance().conventStringToUrlEncoder(this.taskContent);

        if (taskType == TODAY_TASK_TIME_UNCLEAR) {
            custum.put("shijianType", unClearTime);
            custum.put("startDay", DateUtils.getStartPointOfDay(startTime));
            custum.put("endDay", DateUtils.getEndPointOfDay(endTime));
        } else if (taskType == TODAY_TASK) {
            custum.put("shijianType", unClearTime);
            custum.put("startDay", startTime);
            custum.put("endDay", endTime);
        }

        if (taskType == CROSS_DAY_TASK) {
            custum.put("startDay", startTime);
            custum.put("endDay", endTime);
        } else if (taskType == CROSS_DAY_TASK_TIME_UNCLEAR) {
            custum.put("startDay", startTime);
            custum.put("endDay", 0);
        }

        custum.put("taskPlace", taskPlace);
        custum.put("tasKTitle", taskContent);
        custum.put("timeDay", taskPlace);
        custum.put("startTime", startTime);
        custum.put("endTime", endTime);
        custum.put("timeType", taskType);
        custum.put("setPrivate", 0);
        if (taskType == CROSS_DAY_TASK_TIME_UNCLEAR) {
            custum.put("isDuban", "true");
        }
        custum.put("setTime", 2);

//        UserInfoJsonObject userInfo = PreferencesHelper.getInstance().getUserInfoToNative();

//        from.put("avatar", userInfo.getAvatar());
//        from.put("id", PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME));
//        from.put("name", userInfo.getUserName());
//        from.put("email", userInfo.getUserId() + AppConfig.EMAIL_SUFFIX);
//        from.put("isDraft", 0);
//        from.put("uid", userInfo.getEId());

        jsonObject.put("Subject", taskContent);
        jsonObject.put("From", PreferencesHelper.getInstance().getCurrentUser().getUserJson());
        JSONArray emptyArry = new JSONArray();
        jsonObject.put("To", emptyArry);
        jsonObject.put("Cc", emptyArry);
        jsonObject.put("Sc", emptyArry);
        jsonObject.put("IsDel", 0);
        jsonObject.put("Attachment", emptyArry);
        jsonObject.put("SubjectToTask", "");
        jsonObject.put("Custom", custum);
        jsonObject.put("Content", taskContent);
        jsonObject.put("PromptContent", "");
        jsonObject.put("Type", 2);

        if (!TextUtils.isEmpty(taskId))
            jsonObject.put("TaskId", taskId);
        return jsonObject;
    }

    public JSONObject getChangeTimeBean() throws JSONException {
        JSONObject result = new JSONObject();
        JSONObject custume = new JSONObject();

        if (taskType == TODAY_TASK_TIME_UNCLEAR || taskType == TODAY_TASK) {
//            custume.put("shijianType", unClearTime);
            custume.put("Custom.startDay", startTime);
            custume.put("Custom.startTime", startTime);
            custume.put("Custom.endDay", endTime);
            custume.put("Custom.endTime", endTime);
            custume.put("Custom.shijianType", unClearTime);
            custume.put("Custom.timeType", taskType);
        }

        result.put("properties", custume);
//        result.put("_id", taskId);
        result.put("taskId", taskId);
        result.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
        return result;
    }

    public JSONObject toEditJson() throws JSONException {
        JSONObject result = new JSONObject();
        JSONObject custume = new JSONObject();

        custume.put("isDuban", "false");

        if (taskType == TODAY_TASK_TIME_UNCLEAR) {
            custume.put("shijianType", unClearTime);
            custume.put("startDay", DateUtils.getStartPointOfDay(startTime));
            custume.put("endDay", DateUtils.getEndPointOfDay(endTime));
        } else if (taskType == TODAY_TASK) {
            custume.put("shijianType", unClearTime);
            custume.put("startDay", startTime);
            custume.put("endDay", endTime);
        }

        if (taskType == CROSS_DAY_TASK) {
            custume.put("startDay", startTime);
            custume.put("endDay", endTime);
        } else if (taskType == CROSS_DAY_TASK_TIME_UNCLEAR) {
            custume.put("startDay", startTime);
            custume.put("endDay", 0);
        }

        custume.put("startTime", startTime);
        custume.put("endTime", endTime);
        custume.put("timeType", taskType);

        if (taskType == CROSS_DAY_TASK_TIME_UNCLEAR) {
            custume.put("isDuban", "true");
        }

        result.put("custom", custume);
//        result.put("_id", taskId);
        result.put("taskId", taskId);
        result.put("userId", PreferencesHelper.getInstance().getCurrentUser().id);
        result.put("subject", taskContent);
        return result;
    }

    public boolean isCurrentUserTask() {
        return taskCreator.equals(PreferencesHelper.getInstance().getCurrentUser().id);
    }

    public boolean isEmptyTask() {
        return unClearTime == EMPTY_AFTERNOON || unClearTime == EMPTY_CROSS_DAY_TASK
                || unClearTime == EMPTY_NIGHT || unClearTime == EMPTY_NOON || unClearTime == EMPTY_TODAY_TASK;
    }

    public boolean isSameTodayUnClearTime(TaskBean taskBean) {
        switch (unClearTime) {
            case EMPTY_NOON:
                if (taskBean.unClearTime == NOON) {
                    return true;
                }
                break;
            case EMPTY_AFTERNOON:
                if (taskBean.unClearTime == AFTERNOON) {
                    return true;
                }
                break;
            case EMPTY_NIGHT:
                if (taskBean.unClearTime == NIGHT) {
                    return true;
                }
                break;
            case EMPTY_TODAY_TASK:
                return false;
        }
        return false;
    }

    public void setTimeByUnclearTime(int time) {
        if (tempCalendar == null)
            tempCalendar = Calendar.getInstance();
        tempCalendar.clear();
        unClearTime = time;
        if (time == NOON) {
            tempCalendar.setTimeInMillis(startTime);
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
            tempCalendar.set(Calendar.MINUTE, 0);
            startTime = tempCalendar.getTimeInMillis();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 11);
            tempCalendar.set(Calendar.MINUTE, 59);
            endTime = tempCalendar.getTimeInMillis();
        } else if (time == AFTERNOON) {
            tempCalendar.setTimeInMillis(startTime);
            tempCalendar.set(Calendar.HOUR_OF_DAY, 12);
            tempCalendar.set(Calendar.MINUTE, 0);
            startTime = tempCalendar.getTimeInMillis();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 17);
            tempCalendar.set(Calendar.MINUTE, 59);
            endTime = tempCalendar.getTimeInMillis();
        } else if (time == NIGHT) {
            tempCalendar.setTimeInMillis(startTime);
            tempCalendar.set(Calendar.HOUR_OF_DAY, 18);
            tempCalendar.set(Calendar.MINUTE, 0);
            startTime = tempCalendar.getTimeInMillis();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tempCalendar.set(Calendar.MINUTE, 59);
            endTime = tempCalendar.getTimeInMillis();
        }
        taskType = TODAY_TASK_TIME_UNCLEAR;
        startTimeString = unClearTime == NOON ? "上午" : unClearTime == AFTERNOON ? "下午" : "晚上";
        this.endTimeString = startTimeString;
    }

    @Override
    public String toString() {
        return "TaskBean{" +
                "taskCreator='" + taskCreator + '\'' +
                ", taskContent='" + taskContent + '\'' +
                ", sortNum=" + sortNum +
                ", taskType=" + taskType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(startTimeString);
        dest.writeString(endTimeString);
        dest.writeString(taskContent);
        dest.writeString(taskPlace);
        dest.writeString(taskCreator);
        dest.writeInt(setPrivate);
        dest.writeInt(unReadMessageNum);
        dest.writeInt(taskType);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeInt(unClearTime);
        dest.writeInt(sortNum);
    }
}
