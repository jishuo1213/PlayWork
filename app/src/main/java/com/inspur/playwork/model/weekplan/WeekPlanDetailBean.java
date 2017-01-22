package com.inspur.playwork.model.weekplan;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.timeline.TaskBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-21.
 */
public class WeekPlanDetailBean {
    private static final String TAG = "WeekPlanDetailBean";

    public ArrayList<SimpleTaskBean> noonTaskList;
    public ArrayList<SimpleTaskBean> afterNoonTaskList;
    public ArrayList<SimpleTaskBean> nightTaskList;
    public String weekId;
    public int weekNum;
    public int status;
    public long updateTime;
    public ArrayList<UserInfoBean> sharedUsers;
    public ArrayList<UserInfoBean> mainSubmitUsers;
    public UserInfoBean from;
    public String planTitle;


    public WeekPlanDetailBean(JSONObject jsonObject) {
        noonTaskList = new ArrayList<>();
        afterNoonTaskList = new ArrayList<>();
        nightTaskList = new ArrayList<>();
        JSONArray taskArray = jsonObject.optJSONArray("weeksData");
        int count = taskArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject task = taskArray.optJSONObject(i);
            SimpleTaskBean taskBean = new SimpleTaskBean(task);
            switch (taskBean.unClearTime) {
                case TaskBean.NOON:
                    noonTaskList.add(taskBean);
                    break;
                case TaskBean.AFTERNOON:
                    afterNoonTaskList.add(taskBean);
                    break;
                case TaskBean.NIGHT:
                    nightTaskList.add(taskBean);
                    break;
            }
        }
        JSONObject submission = jsonObject.optJSONObject("submission");
        planTitle = submission.optString("Subject");
        JSONArray sharedUsers = submission.optJSONArray("OtherShareUsers");
        int shareCount = sharedUsers.length();
        this.sharedUsers = new ArrayList<>();
        for (int i = 0; i < shareCount; i++) {
            JSONObject user = sharedUsers.optJSONObject(i);
            UserInfoBean bean = new UserInfoBean(user);
            this.sharedUsers.add(bean);
        }
        JSONArray submitUsers = submission.optJSONArray("MainSubmitUsers");
        this.mainSubmitUsers = new ArrayList<>();
        int submitCount = submitUsers.length();
        for (int i = 0; i < submitCount; i++) {
            JSONObject user = submitUsers.optJSONObject(i);
            UserInfoBean bean = new UserInfoBean(user);
            this.mainSubmitUsers.add(bean);
        }
        from = new UserInfoBean(submission.optJSONObject("From"));
        status = submission.optInt("Status");
        updateTime = submission.optLong("UpdateTime");
        weekId = submission.optString("WeekId");
        weekNum = submission.optInt("weekNumber");
    }
}
