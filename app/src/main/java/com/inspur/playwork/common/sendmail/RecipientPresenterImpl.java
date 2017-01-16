package com.inspur.playwork.common.sendmail;

import android.util.SparseArray;

import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.utils.PreferencesHelper;

import java.util.ArrayList;

/**
 * Created by Bugcode on 2016/3/24.
 */
public class RecipientPresenterImpl implements RecipientPresenter {

//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    private RecipientView recipientView;
    private ArrayList<UserInfoBean> recentUserList, departmentUserList;
    private ArrayList<GroupInfoBean> recentGroupList;
    private boolean isRecentComplete, isDepartmentComplete;
    private ArrayList<UserInfoBean> recipientList;

    public RecipientPresenterImpl(RecipientView recipientView, ArrayList<UserInfoBean> recipientList) {
        this.recipientView = recipientView;
        isRecentComplete = false;
        isDepartmentComplete = false;
        this.recipientList = recipientList;
    }

    @Override
    public void register() {
//        GroupStores.getInstance().register();
        Dispatcher.getInstance().register(this);
    }

    @Override
    public void unRegister() {
//        GroupStores.getInstance().unRegister();
        Dispatcher.getInstance().unRegister(this);
    }

    @Override
    public void initContactsList() {
        recipientView.showDialog();
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEARCH_USER_BY_DEPT);
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.GET_ALL_RECENT_CONTACTS);
        GroupStores.getInstance().getAllRecentContacts();
        GroupStores.getInstance().searchByDept();
    }

    @Override
    public void searchUserByKeyWord(String keyWord) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEARCH_PERSON, keyWord);
        GroupStores.getInstance().searchPerson(keyWord);
    }

    @Override
    public void initRecipientList() {
        recipientView.initRecipientListView(recipientList);
    }

    @Override
    public void delRecipient(UserInfoBean userInfo) {
        recipientList.remove(userInfo);
        recipientView.notifyRecipientListAdapter(recipientList);
    }

    @Override
    public void updateRecipient() {
        recipientView.notifyRecipient(recipientList);
    }

    @Override
    public void addRecipient(UserInfoBean userInfo) {
        if (PreferencesHelper.getInstance().getCurrentUser().id.equals(userInfo.id))
            return;
        if (isIdExisted(userInfo.id, recipientList) == null) {
            recipientList.add(userInfo);
            recipientView.notifyRecipientListAdapter(recipientList);
        }
    }

    private UserInfoBean isIdExisted(String id, ArrayList<UserInfoBean> userList) {
        for (UserInfoBean userInfo : userList) {
            if (id.equals(userInfo.id))
                return userInfo;
        }
        return null;
    }

    /**
     * 更新UI线程
     */
    public void onEventMainThread(UpdateUIAction updateUIAction) {
        switch (updateUIAction.getActionType()) {
            // 更新搜索联系人列表
            case MessageActions.SEARCH_PERSON:
                this.searchResult(updateUIAction.getActionData());
                break;
            // 初始化最近联系人、群列表
            case MessageActions.GET_ALL_RECENT_CONTACTS:
                this.setRecentList(updateUIAction.getActionData());
                break;
            // 初始化部门人员列表
            case MessageActions.SEARCH_USER_BY_DEPT:
                this.setDepartmentList(updateUIAction.getActionData());
                break;
            default:
                break;
        }
    }

    /**
     * 更新搜索联系人列表
     */
    private void searchResult(SparseArray<Object> data) {
        ArrayList<UserInfoBean> searchResultList = (ArrayList<UserInfoBean>) data.get(0);
        recipientView.notifySearchUserList(searchResultList);
    }

    /**
     * 初始化最近联系人、群列表
     */
    private void setRecentList(SparseArray<Object> data) {
        recentGroupList = (ArrayList<GroupInfoBean>) data.get(0);
        recentUserList = (ArrayList<UserInfoBean>) data.get(1);
        recipientView.initRecentList(recentUserList, recentGroupList);
        isRecentComplete = true;
        if (isDepartmentComplete) {
            isRecentComplete = false;
            isDepartmentComplete = false;
            recipientView.dismissDialog();
        }
    }

    /**
     * 初始化部门人员列表
     */
    private void setDepartmentList(SparseArray<Object> data) {
        departmentUserList = (ArrayList<UserInfoBean>) data.get(0);
        recipientView.initDepartmentList(departmentUserList);
        isDepartmentComplete = true;
        if (isRecentComplete) {
            isRecentComplete = false;
            isDepartmentComplete = false;
            recipientView.dismissDialog();
        }
    }
}
