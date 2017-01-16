package com.inspur.playwork.view.timeline;

import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Fan on 2016/4/21.
 */
public interface TimeLineViewOperation {

    void showTaskList(ArrayList<TaskBean> todayTaskList, ArrayList<TaskBean> crossDayTaskList);

    void showUnReadMsg();

    void quitTaskResult(boolean result, String taskId);

    void setTimeLineMsgCount(String taskId,int type);

    void getChatWindowInfoSuccess(boolean success,ChatWindowInfoBean windowInfoBean,TaskBean taskBean);

    void changeTaskTimeResult(boolean result);

    void sortTaskList(JSONObject jsonObject);

    void sendMailResult(boolean result,boolean isCrossDay,String taskId);

    void updateTaskSubject(String taskId,String subject);

    void deleteTask(String taskId);
}
