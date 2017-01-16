package com.inspur.playwork.view.message.chat;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fan on 15-10-6.
 */
public class ChatChosePersonFragment extends Fragment implements ChosePersonRecyclerAdapter.ChangeMemberListener,
        ContactListRecyclerAdapter.AddMemberListener {

    private static final String TAG = "ChatChosePersonFan";

//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    private ViewPager mContactViewPager;

    private ContactListPagerAdapter mContactListPagerAdapter;

    private TextView chooseContactTextView, chooseGroupTextView, chooseDepartmentTextView;

    private ArrayList<UserInfoBean> allPersonList, chatPersonList, hidePersonList, exitPersonList, addMemberList;

    private ChosePersonRecyclerAdapter chatPersonRecyclerAdapter, hidePersonRecyclerAdapter, exitPersonRecyclerAdapter;

    private View rootView;

    private AutoCompleteTextView searchPersonAutoCompleteTextView;

    private ChosePersonRecyclerAdapter.PersonSelectedListener personSelectedListener;

    private SearchPersonAdapter mSearchPersonAdapter;

    private View leftLayout;
    private View middleLayout;
    private View bottomLayout;
    private View leftLine;


    private ChatWindowInfoBean chatWindowInfoBean;

    private TaskBean taskBean;

    private ArrayList<GroupInfoBean> mGroupChatList;

    private ArrayList<UserInfoBean> mRecentChatList, mDepartmentList;


    private ContactListRecyclerAdapter mRecentListRecyclerAdapter, mGroupListRecyclerAdapter, mDepartmentListRecyclerAdapter;

    private boolean isQuickChat; // 快速聊天标识

    private ArrayList<UserInfoBean> mSearchPersonResultArrayList;

    private CancelQuickChatListener mCancelQuickChatListener;

    private boolean okBtnClickable = true;

    private Handler handler;

    public interface CancelQuickChatListener {
        void cancelQuickChat();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        GroupStores.getInstance().register();
        Dispatcher.getInstance().register(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.layout_chat_chose_person, container, false);
        initData();
        initView(rootView);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        GroupStores.getInstance().unRegister();
        Dispatcher.getInstance().unRegister(this);
    }

    private void initData() {
        handler = new ChosePersonHander();
        this.mGroupChatList = new ArrayList<>();
        this.mRecentChatList = new ArrayList<>();
        this.mDepartmentList = new ArrayList<>();
        this.addMemberList = new ArrayList<>();
        this.mSearchPersonResultArrayList = new ArrayList<>();
        this.allPersonList = new ArrayList<>();
        this.chatPersonList = new ArrayList<>();
        this.hidePersonList = new ArrayList<>();
        this.exitPersonList = new ArrayList<>();

        if (chatWindowInfoBean.groupId != null) {
            this.allPersonList.addAll(this.chatWindowInfoBean.allMemberList);
            this.chatPersonList.addAll(this.chatWindowInfoBean.chatMemberList);
            this.hidePersonList.addAll(this.chatWindowInfoBean.hideMemberList);
            this.exitPersonList.addAll(this.chatWindowInfoBean.exitMemberList);
        }

//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEARCH_USER_BY_DEPT);
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.GET_ALL_RECENT_CONTACTS);
        GroupStores.getInstance().searchByDept();
        GroupStores.getInstance().getAllRecentContacts();
    }

    /**
     * 更新UI线程
     *
     * @param updateUIAction
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateUIAction updateUIAction) {
        boolean res;
        switch (updateUIAction.getActionType()) {
            // 获取联系人和联系组
            case MessageActions.GET_ALL_RECENT_CONTACTS:
                //noinspection unchecked
                mGroupChatList = (ArrayList<GroupInfoBean>) updateUIAction.getActionData().get(0); // 聊天组列表
                //noinspection unchecked
                mRecentChatList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(1); // 联系人列表
                /*mContactListRecyclerAdapter.setDataList(contactType, mRecentChatList);
                mContactListRecyclerAdapter.notifyDataSetChanged();*/

                mRecentListRecyclerAdapter.setDataList(1, mRecentChatList);
                mRecentListRecyclerAdapter.notifyDataSetChanged();
                mGroupListRecyclerAdapter.setDataList(2, mGroupChatList);
                mGroupListRecyclerAdapter.notifyDataSetChanged();
                mContactListPagerAdapter.notifyDataSetChanged();
                break;
            // 获取部门人员
            case MessageActions.SEARCH_USER_BY_DEPT:
                //noinspection unchecked
                mDepartmentList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(0); // 部门人员列表
                mDepartmentListRecyclerAdapter.setDataList(3, mDepartmentList);
                mDepartmentListRecyclerAdapter.notifyDataSetChanged();
                break;
            // 保存正在聊天人员
            case MessageActions.SAVE_CONTACT_GROUP:
                res = (boolean) updateUIAction.getActionData().get(0);
                if (res) {
                    this.chatWindowInfoBean.allMemberList = this.allPersonList;
                    this.chatWindowInfoBean.chatMemberList = this.chatPersonList;
                    this.chatWindowInfoBean.hideMemberList = this.hidePersonList;
                    this.chatWindowInfoBean.exitMemberList = this.exitPersonList;
                    chatWindowInfoBean.calculateChangeMember();
                    ((ChatActivityNew) getActivity()).setVChatMembers();
                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_CHAT_WINDOW_BY_GROUP_ID, 1, chatWindowInfoBean);
                }
                ((ChatActivityNew) getActivity()).exitChoosePerson();
                break;
            // 添加搜索到的联系人
            case MessageActions.SEARCH_PERSON:
                //noinspection unchecked
                mSearchPersonResultArrayList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(0);
                mSearchPersonAdapter.setDataList(mSearchPersonResultArrayList);
                mSearchPersonAdapter.notifyDataSetChanged();
                break;
            case MessageActions.CREATE_NEW_CHAT:
                if (updateUIAction.getActionData().get(0) == null) {
                    UItoolKit.showToastShort(getActivity(), "创建聊天失败");
                    break;
                }
                this.chatWindowInfoBean = (ChatWindowInfoBean) updateUIAction.getActionData().get(0);
                if (!chatWindowInfoBean.isSingle) {
                    // 发送 保存人员数据
//                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SAVE_CONTACT_GROUP, taskBean, chatWindowInfoBean, chatPersonList);
                    GroupStores.getInstance().saveContactGroup(taskBean, chatWindowInfoBean, chatPersonList, false);
                    ((ChatActivityNew) getActivity()).exitChoosePerson();
                } else {
                    ((ChatActivityNew) getActivity()).exitChoosePerson();
                }
                break;
        }
    }

    private void initView(View v) {
        chooseContactTextView = (TextView) v.findViewById(R.id.tv_choose_contact);
        chooseGroupTextView = (TextView) v.findViewById(R.id.tv_choose_group);
        chooseDepartmentTextView = (TextView) v.findViewById(R.id.tv_choose_department);
//        contactListRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_contact_list);
        RecyclerView chatPersonRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_chat_person);
        RecyclerView hidePersonRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_hide_person);
        RecyclerView exitPersonRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_exit_person);
        searchPersonAutoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.actv_search_person);
        ImageView choosePersonOkImageView = (ImageView) v.findViewById(R.id.iv_choose_person_ok); // 确定按钮
        choosePersonOkImageView.setOnClickListener(choosePersonClickListener);
        ImageView choosePersonCancelImageView = (ImageView) v.findViewById(R.id.iv_choose_person_cancel); // 取消按钮
        choosePersonCancelImageView.setOnClickListener(choosePersonClickListener);

        /* 通讯录 */
        chooseContactTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_press)); // 联系人tag
        chooseContactTextView.setOnClickListener(chooseContactByTypeListener);
        chooseGroupTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_normal)); // 联系组tag
        chooseGroupTextView.setOnClickListener(chooseContactByTypeListener);
        chooseDepartmentTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_normal)); // 部门成员tag
        chooseDepartmentTextView.setOnClickListener(chooseContactByTypeListener);
        RecyclerView mRecentListRecyclerView = (RecyclerView) getActivity().getLayoutInflater()
                .inflate(R.layout.layout_contact_list, null);
        mRecentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecentListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mRecentListRecyclerView);
        mRecentListRecyclerAdapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
        mRecentListRecyclerAdapter.setDataList(1, mRecentChatList);
        mRecentListRecyclerAdapter.setAddMemberListener(this);
//        if (arrayMap != null) {
//            mRecentListRecyclerAdapter.setArrayMap(arrayMap);
//        }
        mRecentListRecyclerView.setAdapter(mRecentListRecyclerAdapter);
        RecyclerView mGroupListRecyclerView = (RecyclerView) getActivity().getLayoutInflater()
                .inflate(R.layout.layout_contact_list, null);
        mGroupListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGroupListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mGroupListRecyclerView);
        mGroupListRecyclerAdapter.setDataList(2, mGroupChatList);
        mGroupListRecyclerAdapter.setAddMemberListener(this);
//        if (arrayMap != null) {
//            mGroupListRecyclerAdapter.setArrayMap(arrayMap);
//        }
        mGroupListRecyclerView.setAdapter(mGroupListRecyclerAdapter);
        RecyclerView mDepartmentListRecyclerView = (RecyclerView) getActivity().getLayoutInflater()
                .inflate(R.layout.layout_contact_list, null);
        mDepartmentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDepartmentListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mDepartmentListRecyclerView);
        mDepartmentListRecyclerAdapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
        mDepartmentListRecyclerAdapter.setDataList(3, mDepartmentList);
        mDepartmentListRecyclerAdapter.setAddMemberListener(this);
//        if (arrayMap != null) {
//            mDepartmentListRecyclerAdapter.setArrayMap(arrayMap);
//        }
        mDepartmentListRecyclerView.setAdapter(mDepartmentListRecyclerAdapter);
        List<View> viewList = new ArrayList<>();
        viewList.add(mRecentListRecyclerView);
        viewList.add(mGroupListRecyclerView);
        viewList.add(mDepartmentListRecyclerView);
        mContactListPagerAdapter = new ContactListPagerAdapter(viewList);
        mContactViewPager = (ViewPager) v.findViewById(R.id.vp_contact_list);
        mContactViewPager.setAdapter(mContactListPagerAdapter);
        mContactViewPager.addOnPageChangeListener(contactPageChangeListener);

        /*contactListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), chatPersonRecyclerView);
        mContactListRecyclerAdapter.setDataList(contactType, mRecentChatList);
        mContactListRecyclerAdapter.setAddMemberListener(this);
        if (arrayMap != null) {
            mContactListRecyclerAdapter.setArrayMap(arrayMap);
        }
        contactListRecyclerView.setAdapter(mContactListRecyclerAdapter);*/

        chatPersonRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        chatPersonRecyclerAdapter = new ChosePersonRecyclerAdapter(chatPersonRecyclerView);
        setAdapterParam(chatPersonRecyclerAdapter, chatPersonList, null, personSelectedListener, isQuickChat, 1);
        chatPersonRecyclerView.setAdapter(chatPersonRecyclerAdapter);

        hidePersonRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        hidePersonRecyclerAdapter = new ChosePersonRecyclerAdapter(hidePersonRecyclerView);
        setAdapterParam(hidePersonRecyclerAdapter, hidePersonList, null, personSelectedListener, isQuickChat, 2);
        hidePersonRecyclerView.setAdapter(hidePersonRecyclerAdapter);

        exitPersonRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        exitPersonRecyclerAdapter = new ChosePersonRecyclerAdapter(exitPersonRecyclerView);
        setAdapterParam(exitPersonRecyclerAdapter, exitPersonList, null, personSelectedListener, isQuickChat, 3);
        exitPersonRecyclerView.setAdapter(exitPersonRecyclerAdapter);

        mSearchPersonAdapter = new SearchPersonAdapter(getActivity());
        mSearchPersonAdapter.setDataList(mSearchPersonResultArrayList);
        searchPersonAutoCompleteTextView.setAdapter(mSearchPersonAdapter);
        searchPersonAutoCompleteTextView.setThreshold(1); // 1个字符开始匹配
        searchPersonAutoCompleteTextView.addTextChangedListener(searchNameTextWatcher);
        searchPersonAutoCompleteTextView.setOnItemClickListener(searchResultOnItemClickListener);
        searchPersonAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && searchPersonAutoCompleteTextView.getText().toString().equals("")) {
                    searchPersonAutoCompleteTextView.setHint("");
                } else if (!hasFocus && searchPersonAutoCompleteTextView.getText().toString().equals("")) {
                    searchPersonAutoCompleteTextView.setHint("搜索");
                }
            }
        });

        leftLayout = v.findViewById(R.id.layout_left);
        middleLayout = v.findViewById(R.id.layout_middle);
        View rightLayout = v.findViewById(R.id.layout_right);
        bottomLayout = v.findViewById(R.id.layout_bottom);
        leftLine = v.findViewById(R.id.line_left);
        if (chatPersonList != null && chatPersonList.size() > 0) {
            setIsQuickChat(true);
            setContactViewVisibility(View.INVISIBLE, View.GONE);
        } else {
            setIsQuickChat(false);
            setContactViewVisibility(View.VISIBLE, View.VISIBLE);
        }
        searchPersonAutoCompleteTextView.clearFocus();
        rightLayout.setOnTouchListener(rightOnTouchListener);
    }

    /**
     * 通讯录page监听器
     */
    private ViewPager.OnPageChangeListener contactPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // 修改tag标签颜色
            switch (position) {
                case 0:
                    changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);
                    break;
                case 1:
                    changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_press, R.color.contact_list_tag_normal);
                    break;
                case 2:
                    changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_normal, R.color.contact_list_tag_press);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 通讯录搜索框TextWatcher
     */
    private TextWatcher searchNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputText = s.toString();
            if (!TextUtils.isEmpty(inputText)) {
                handler.removeMessages(1);
                handler.sendMessageDelayed(handler.obtainMessage(1, inputText), 500);
            }
//            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEARCH_PERSON, inputText);

//            GroupStores.getInstance().searchPerson(inputText);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    private static class ChosePersonHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GroupStores.getInstance().searchPerson((String) msg.obj);
                    break;
            }
        }
    }

    /**
     * 通讯录搜索结果OnItemClickListener
     */
    private AdapterView.OnItemClickListener searchResultOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            UserInfoBean userInfoBean = mSearchPersonResultArrayList.get(position);
            hideInputMethod(); // 隐藏软键盘
            searchPersonAutoCompleteTextView.setText(""); // 清空输入内容
            searchPersonAutoCompleteTextView.clearFocus(); // 清除焦点
            requestAddMember(userInfoBean);
        }
    };

    /**
     * 正在聊天列表左滑手势OnTouchListener
     */
    private View.OnTouchListener rightOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                setIsQuickChat(false); // 关闭快速聊天模式
                mCancelQuickChatListener.cancelQuickChat();
                searchPersonAutoCompleteTextView.setText("");
                setContactViewVisibility(View.VISIBLE, View.VISIBLE);
                searchPersonAutoCompleteTextView.clearFocus();
            }
            return false;
        }
    };

    private void setAdapterParam(ChosePersonRecyclerAdapter adapter, ArrayList<UserInfoBean> dataList,
                                 BitmapCacheManager arrayMap, ChosePersonRecyclerAdapter.PersonSelectedListener personSelectedListener,
                                 boolean isQuickChat, int changeType) {
        adapter.setChatPersonList(dataList);
        adapter.setIsQuickChat(isQuickChat);
        adapter.setType(changeType);
        adapter.setChangeListener(this);
        adapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
        if (personSelectedListener != null) {
            adapter.setSelectedListener(personSelectedListener);
        }
    }

    public void setContactViewVisibility(int param1, int param2) {
        leftLayout.setVisibility(param1);
        middleLayout.setVisibility(param1);
        bottomLayout.setVisibility(param2);
        leftLine.setVisibility(param1);
//        rightLine.setVisibility(param1);
    }

    /**
     * 设置快速聊天模式
     *
     * @param isQuickChat
     */
    public void setIsQuickChat(boolean isQuickChat) {
        this.isQuickChat = isQuickChat;
        if (chatPersonRecyclerAdapter != null) {
            chatPersonRecyclerAdapter.setIsQuickChat(isQuickChat);
        }
        if (hidePersonRecyclerAdapter != null) {
            hidePersonRecyclerAdapter.setIsQuickChat(isQuickChat);
        }
        if (exitPersonRecyclerAdapter != null) {
            exitPersonRecyclerAdapter.setIsQuickChat(isQuickChat);
        }
    }

    public void setListener(ChosePersonRecyclerAdapter.PersonSelectedListener personSelectedListener) {
        this.personSelectedListener = personSelectedListener;
    }

    public void setChatWindowInfo(ChatWindowInfoBean chatWindowInfoBean) {
        this.chatWindowInfoBean = chatWindowInfoBean;
    }

    public void setTaskInfo(TaskBean taskBean) {
        this.taskBean = taskBean;
    }

    public void setCancelQuickChatListener(CancelQuickChatListener cancelQuickChatListener) {
        this.mCancelQuickChatListener = cancelQuickChatListener;
    }

    /**
     * 通讯录title标签OnClickListener
     */
    private View.OnClickListener chooseContactByTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_choose_contact:
                    /*if (contactType != 1) {
                        contactType = 1;
                        changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);
                        mContactListRecyclerAdapter.setDataList(contactType, mRecentChatList);
                        mContactListRecyclerAdapter.notifyDataSetChanged();
                    }*/
                    changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);
                    mContactViewPager.setCurrentItem(0);
                    break;
                case R.id.tv_choose_group:
                    /*if (contactType != 2) {
                        contactType = 2;
                        changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_press, R.color.contact_list_tag_normal);
                        mContactListRecyclerAdapter.setDataList(contactType, mGroupChatList);
                        mContactListRecyclerAdapter.notifyDataSetChanged();
                    }*/
                    changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_press, R.color.contact_list_tag_normal);
                    mContactViewPager.setCurrentItem(1);
                    break;
                case R.id.tv_choose_department:
                    /*if (contactType != 3) {
                        contactType = 3;
                        changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_normal, R.color.contact_list_tag_press);
                        mContactListRecyclerAdapter.setDataList(contactType, mDepartmentList);
                        mContactListRecyclerAdapter.notifyDataSetChanged();
                    }*/
                    changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_normal, R.color.contact_list_tag_press);
                    mContactViewPager.setCurrentItem(2);
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener choosePersonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_choose_person_cancel:
                    ((ChatActivityNew) getActivity()).exitChoosePerson();
                    break;
                case R.id.iv_choose_person_ok:
                    if (!okBtnClickable) {
                        return;
                    }
                    if (chatWindowInfoBean.groupId != null) {
                        if (chatWindowInfoBean.isSingle) {
                            // 单聊转群聊
                            if (addMemberList.size() > 0) {
//                                Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.CREATE_NEW_CHAT, chatPersonList, "");
                                GroupStores.getInstance().createNewChat(chatPersonList, "", "");
                            }
                        } else {
                            if (addMemberList.size() > 0) {
                                // 发送 添加人员数据
//                                Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.ADD_MEMBER, taskBean, chatWindowInfoBean, addMemberList);
                                GroupStores.getInstance().addMember(taskBean, chatWindowInfoBean, addMemberList);
                            }
                            // 发送 保存人员数据
//                            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SAVE_CONTACT_GROUP, taskBean, chatWindowInfoBean, chatPersonList);
                            GroupStores.getInstance().saveContactGroup(taskBean, chatWindowInfoBean, chatPersonList, true);
                        }
                    } else {
//                        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.CREATE_NEW_CHAT, addMemberList, ((ChatActivityNew) getActivity()).getEditTopic());
                        GroupStores.getInstance().createNewChat(addMemberList, ((ChatActivityNew) getActivity()).getEditTopic(), "");
                    }
                    okBtnClickable = false;
                    break;
                default:
                    break;
            }
        }
    };

    private void changeTagColor(int recentColor, int groupColor, int departmentColor) {
        chooseContactTextView.setTextColor(getResources().getColor(recentColor));
        chooseGroupTextView.setTextColor(getResources().getColor(groupColor));
        chooseDepartmentTextView.setTextColor(getResources().getColor(departmentColor));
    }

    /**
     * 人员调整接口回调方法
     *
     * @param userInfoBean 需要操作的人员对象
     * @param type         操作类型 1、正在聊天->屏蔽 / 撤销 联系人->正在聊天 / 撤销 退出->正在聊天
     *                     2、屏蔽->正在聊天 / 撤销 屏蔽->正在聊天
     *                     3、退出->正在聊天
     */
    @Override
    public void onChangeMember(UserInfoBean userInfoBean, int type) {
        /*
        修改数据
         */
        Log.d(TAG, "onChangeMember() called with: userInfoBean = [" + userInfoBean + "], type = [" + type + "]");
        switch (type) {
            case 1:
                if (chatWindowInfoBean.allMemberList != null && chatWindowInfoBean.allMemberList.contains(userInfoBean)) {
                    if (chatPersonList.size() > 1) {
                        chatPersonList.remove(userInfoBean);
                        addMemberList.remove(userInfoBean);
                        if (chatWindowInfoBean.exitMemberList.contains(userInfoBean)) {
                            exitPersonList.add(userInfoBean);
                        } else {
                            hidePersonList.add(userInfoBean);
                        }
                    } else {
                        UItoolKit.showToastShort(getActivity(), "聊天人员至少要有一个人");
                        break;
                    }
                } else {
                    if (chatPersonList.size() > 1 || TextUtils.isEmpty(chatWindowInfoBean.groupId)) {
                        chatPersonList.remove(userInfoBean);
                        allPersonList.remove(userInfoBean);
                        addMemberList.remove(userInfoBean);
                    } else {
                        UItoolKit.showToastShort(getActivity(), "聊天人员至少要有一个人");
                        break;
                    }
                }
                break;
            case 2:
                hidePersonList.remove(userInfoBean);
                chatPersonList.add(userInfoBean);
                break;
            case 3:
                exitPersonList.remove(userInfoBean);
                chatPersonList.add(userInfoBean);
                addMemberList.add(userInfoBean);
                break;
            default:
                break;
        }
        /*
        刷新适配器
         */
        notifyChosePersonView();
    }

    public void notifyChosePersonView() {
        this.chatPersonRecyclerAdapter.notifyDataSetChanged();
        this.hidePersonRecyclerAdapter.notifyDataSetChanged();
        this.exitPersonRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * 增加单个人员接口回调方法
     *
     * @param userInfoBean 添加的人员信息
     */
    @Override
    public void addMember(UserInfoBean userInfoBean) {
        requestAddMember(userInfoBean);
    }

    /**
     * 增加一组人员接口回调方法
     *
     * @param groupInfoBean 添加的群组信息
     */
    @Override
    public void addMember(GroupInfoBean groupInfoBean) {
        ArrayList<UserInfoBean> chooseMemberList = groupInfoBean.getMemberList();

        for (UserInfoBean userInfoBean : chooseMemberList) {
            requestAddMember(userInfoBean);
        }
    }

    /**
     * 聊天增加人员数据处理方法
     *
     * @param userInfoBean
     */
    private void requestAddMember(UserInfoBean userInfoBean) {
        if (PreferencesHelper.getInstance().getCurrentUser().id.equals(userInfoBean.id))
            return;

        if (chatWindowInfoBean.allMemberList == null || !chatWindowInfoBean.allMemberList.contains(userInfoBean)) {
            if (!chatPersonList.contains(userInfoBean)) {
                chatPersonList.add(userInfoBean);
                addMemberList.add(userInfoBean);
                allPersonList.add(userInfoBean);
            }
        } else {
            if (hidePersonList.contains(userInfoBean)) {
                hidePersonList.remove(userInfoBean);
                chatPersonList.add(userInfoBean);
            }
            if (exitPersonList.contains(userInfoBean)) {
                exitPersonList.remove(userInfoBean);
                addMemberList.add(userInfoBean);
                chatPersonList.add(userInfoBean);
            }
        }
        notifyChosePersonView();
    }

//    private UserInfoBean checkUserIdExisted(String userId, ArrayList<UserInfoBean> userList) {
//        for (UserInfoBean userInfo : userList) {
//            if (userId.equals(userInfo.id))
//                return userInfo;
//        }
//        return null;
//    }
//
//    private UserInfoBean checkUserIdExistedInHideList(String userId, ArrayList<UserInfoBean> userList) {
//        if (userId.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
//            return null;
//        }
//        for (UserInfoBean userInfoBean : userList) {
//            if (userInfoBean.id.equals(userId)) {
//                return userInfoBean;
//            }
//        }
//        return null;
//    }

    private void hideInputMethod() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

//    public void memberExit(String exitId) {
//        for (UserInfoBean userInfoBean : chatPersonList) {
//            if (exitId.equals(userInfoBean.id)) {
//                chatPersonList.remove(userInfoBean);
//                chatWindowInfoBean.chatMemberList.remove(userInfoBean);
//                chatPersonRecyclerAdapter.notifyDataSetChanged();
//
//                exitPersonList.add(userInfoBean);
//                chatWindowInfoBean.exitMemberList.add(userInfoBean);
//                exitPersonRecyclerAdapter.notifyDataSetChanged();
//                break;
//            }
//        }
//        for (UserInfoBean userInfoBean : hidePersonList) {
//            if (exitId.equals(userInfoBean.id)) {
//                hidePersonList.remove(userInfoBean);
//                chatWindowInfoBean.hideMemberList.remove(userInfoBean);
//                hidePersonRecyclerAdapter.notifyDataSetChanged();
//
//                exitPersonList.add(userInfoBean);
//                chatWindowInfoBean.exitMemberList.add(userInfoBean);
//                exitPersonRecyclerAdapter.notifyDataSetChanged();
//                break;
//            }
//        }
//    }

    public void setOkBtnClickable(boolean okBtnClickable) {
        this.okBtnClickable = okBtnClickable;
    }
}
