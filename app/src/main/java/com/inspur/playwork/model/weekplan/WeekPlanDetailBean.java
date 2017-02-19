package com.inspur.playwork.model.weekplan;

import android.util.Log;

import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by fan on 17-1-21.
 */
public class WeekPlanDetailBean {
    private static final String TAG = "WeekPlanDetailBean";

    public String weekId;
    public int weekNum;
    public int status;
    public long updateTime;
    public int allTasksCount;
    public ArrayList<SimpleTaskBean>[] oneWeekTasks;
    public int[][] taskCounts;

    public long[] weekDayTime;
    public String weekPlanCreator;

    @SuppressWarnings("unchecked")
    public WeekPlanDetailBean(JSONObject res, long[] oneWeekTime) {
        oneWeekTasks = new ArrayList[7];
        taskCounts = new int[7][3];
        this.weekDayTime = oneWeekTime;
        JSONObject jsonObject = res.optJSONObject("data");
        JSONArray taskArray = jsonObject.optJSONArray("weeksData");
        int count = taskArray.length();
        weekPlanCreator = jsonObject.optString("userId");
        for (int i = 0; i < count; i++) {
            oneWeekTasks[i] = new ArrayList<>();
            JSONArray oneDayTaskArray = taskArray.optJSONArray(i);
            int oneDayCount = oneDayTaskArray.length();
            for (int j = 0; j < oneDayCount; j++) {
                JSONObject task = oneDayTaskArray.optJSONObject(j);
                SimpleTaskBean taskBean = new SimpleTaskBean(task);
                Log.i(TAG, "WeekPlanDetailBean: " + taskBean.unClearTime);
                switch (taskBean.unClearTime) {
                    case TaskBean.NOON:
                        oneWeekTasks[i].add(taskBean);
                        taskCounts[i][0]++;
                        allTasksCount++;
                        break;
                    case TaskBean.AFTERNOON:
                        oneWeekTasks[i].add(taskBean);
                        taskCounts[i][1]++;
                        allTasksCount++;
                        break;
                    case TaskBean.NIGHT:
                        oneWeekTasks[i].add(taskBean);
                        taskCounts[i][2]++;
                        allTasksCount++;
                        break;
                }
            }
            if (taskCounts[i][0] == 0) {
                SimpleTaskBean taskBean = new SimpleTaskBean(TaskBean.NOON, weekPlanCreator);
                oneWeekTasks[i].add(taskBean);
                taskCounts[i][0]++;
                allTasksCount++;
                taskBean.startTime = weekDayTime[i];
            }
            if (taskCounts[i][1] == 0) {
                SimpleTaskBean taskBean = new SimpleTaskBean(TaskBean.AFTERNOON, weekPlanCreator);
                oneWeekTasks[i].add(taskBean);
                taskCounts[i][1]++;
                allTasksCount++;
                taskBean.startTime = weekDayTime[i];
            }
            if (taskCounts[i][2] == 0) {
                SimpleTaskBean taskBean = new SimpleTaskBean(TaskBean.NIGHT, weekPlanCreator);
                oneWeekTasks[i].add(taskBean);
                taskCounts[i][2]++;
                allTasksCount++;
                taskBean.startTime = weekDayTime[i];
            }
            Collections.sort(oneWeekTasks[i]);
        }
        JSONObject submission = jsonObject.optJSONObject("submission");
//        planTitle = submission.optString("Subject");
//        JSONArray sharedUsers = submission.optJSONArray("OtherShareUsers");
//        int shareCount = sharedUsers.length();
//        this.sharedUsers = new ArrayList<>();
//        for (int i = 0; i < shareCount; i++) {
//            JSONObject user = sharedUsers.optJSONObject(i);
//            UserInfoBean bean = new UserInfoBean(user);
//            this.sharedUsers.add(bean);
//        }
//        JSONArray submitUsers = submission.optJSONArray("MainSubmitUsers");
//        this.mainSubmitUser = new ArrayList<>();
//        int submitCount = submitUsers.length();
//        for (int i = 0; i < submitCount; i++) {
//            JSONObject user = submitUsers.optJSONObject(i);
//            mainSubmitUser = new UserInfoBean(user);
//        }
//        from = new UserInfoBean(submission.optJSONObject("From"));
        if (submission != null) {
            status = submission.optInt("Status");
            updateTime = submission.optLong("UpdateTime");
            weekId = submission.optString("WeekId");
            weekNum = submission.optInt("weekNumber");
        }
    }

    public boolean isCurrentUserWeekPlan() {
        return PreferencesHelper.getInstance().getCurrentUser().id.equals(weekPlanCreator);
    }
}
