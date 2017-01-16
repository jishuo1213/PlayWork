package com.inspur.playwork.model.message;

import com.google.gson.annotations.SerializedName;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 群组信息Model
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class GroupInfoBean {
    private String taskId;
    private String groupId;
    private String subject;
    @SerializedName("member")
    private ArrayList<UserInfoBean> memberList;
    private String allMemberName = "";

    public GroupInfoBean(JSONObject jsonObject) {
        groupId = jsonObject.optString("groupId");
        subject = jsonObject.optString("subject");
        String currentUser = PreferencesHelper.getInstance().getCurrentUser().id;

        JSONArray memberArray = jsonObject.optJSONArray("member");
        int length = memberArray.length();
        memberList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            JSONObject user = memberArray.optJSONObject(i);
//            String userName = user.optString("name");
            UserInfoBean userBean = new UserInfoBean(user);
            if (!userBean.id.equals(currentUser)) {
                memberList.add(userBean);
                if (i < length - 1) {
                    allMemberName += userBean.name + ",";
                } else {
                    allMemberName += userBean.name;
                }
            }
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public ArrayList<UserInfoBean> getMemberList() {
        return memberList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAllMemberName() {
        return allMemberName;
    }

}
