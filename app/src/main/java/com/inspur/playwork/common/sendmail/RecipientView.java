package com.inspur.playwork.common.sendmail;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;

import java.util.ArrayList;

/**
 * Created by Bugcode on 2016/3/24.
 */
public interface RecipientView {

    void showDialog();

    void dismissDialog();

    void initRecentList(ArrayList<UserInfoBean> recentUserList, ArrayList<GroupInfoBean> recentGroupList);

    void initDepartmentList(ArrayList<UserInfoBean> departmentUserList);

    void notifySearchUserList(ArrayList<UserInfoBean> searchUserList);

    void initRecipientListView(ArrayList<UserInfoBean> chatList);

    void notifyRecipientListAdapter(ArrayList<UserInfoBean> chatList);

    void notifyRecipient(ArrayList<UserInfoBean> recipientList);
}
