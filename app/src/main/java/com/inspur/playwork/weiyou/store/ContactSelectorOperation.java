package com.inspur.playwork.weiyou.store;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;

import java.util.ArrayList;

/**
 * Created by sunyuan on 2016/11/23 0023 11:18.
 * Email: sunyuan@inspur.com
 */
public interface ContactSelectorOperation {
    void refreshAllListView(ArrayList<UserInfoBean> mRecentChatList, ArrayList<GroupInfoBean> mGroupChatList);

    void refreshDepartmentListView(ArrayList<UserInfoBean> mDepartmentList);

    void refreshSearchResultListView(ArrayList<UserInfoBean> mSearchPersonResultArrayList);

}
