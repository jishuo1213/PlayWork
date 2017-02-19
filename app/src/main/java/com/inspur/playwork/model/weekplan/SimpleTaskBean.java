package com.inspur.playwork.model.weekplan;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONObject;

import static com.inspur.playwork.model.timeline.TaskBean.EMPTY_TODAY_TASK;
import static com.inspur.playwork.model.timeline.TaskBean.TODAY_TASK_TIME_UNCLEAR;

/**
 * Created by fan on 17-1-21.
 */
public class SimpleTaskBean implements Parcelable, Comparable<SimpleTaskBean> {
    private static final String TAG = "SimpleTaskBean";

    public String taskId;
    public String taskContent;
    public long startTime;
    public long endTime;
    public int taskType;
    public int unClearTime;
    public String taskCreator;

    public SimpleTaskBean(JSONObject json) {
        JSONObject custom = json.optJSONObject("Custom");
        int type = custom.optInt("timeType");
        taskCreator = custom.optString("creator");
        taskId = json.optString("_id");
        if (json.has("Subject")) {
            taskContent = json.optString("Subject");
        } else {
            taskContent = custom.optString("taskTitle");
        }
        if (type < 7 && type > 1) {
            startTime = custom.optLong("startTime");
            endTime = custom.optLong("endTime");
            taskType = TaskBean.TODAY_TASK;
            unClearTime = DateUtils.getTimeNoonOrAfterNoon(startTime);
        } else if (type == 9) {
            taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
            startTime = custom.optLong("startTime");
            endTime = custom.optLong("endTime");
            unClearTime = DateUtils.getTimePeriod(startTime);
        } else {
            unClearTime = -1;
        }
    }

    public SimpleTaskBean(int unClearTime,String creator) {
        this.unClearTime = unClearTime;
        taskType = EMPTY_TODAY_TASK;
        this.taskCreator = creator;
    }

    public boolean isEmptyTask() {
        return taskType == EMPTY_TODAY_TASK;
    }

    public boolean isCurrentUserTask() {
        return PreferencesHelper.getInstance().getCurrentUser().id.equals(taskCreator);
    }

    protected SimpleTaskBean(Parcel in) {
        taskId = in.readString();
        taskContent = in.readString();
        startTime = in.readLong();
        endTime = in.readLong();
        taskType = in.readInt();
        unClearTime = in.readInt();
        taskCreator = in.readString();
    }

    public static final Creator<SimpleTaskBean> CREATOR = new Creator<SimpleTaskBean>() {
        @Override
        public SimpleTaskBean createFromParcel(Parcel in) {
            return new SimpleTaskBean(in);
        }

        @Override
        public SimpleTaskBean[] newArray(int size) {
            return new SimpleTaskBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(taskContent);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeInt(taskType);
        dest.writeInt(unClearTime);
        dest.writeString(taskCreator);
    }


    @Override
    public int compareTo(SimpleTaskBean o) {
        if (o != null) {
            if (unClearTime == o.unClearTime) {
                if (this.startTime < o.startTime) {
                    return -1;
                } else if (this.startTime > o.startTime) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return unClearTime - o.unClearTime;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "SimpleTaskBean{" +
                "taskId='" + taskId + '\'' +
                ", taskContent='" + taskContent + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", taskType=" + taskType +
                ", unClearTime=" + unClearTime +
                ", taskCreator='" + taskCreator + '\'' +
                '}';
    }
}
