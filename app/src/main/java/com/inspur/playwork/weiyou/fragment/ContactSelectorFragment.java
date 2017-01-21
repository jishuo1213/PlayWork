package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.weiyou.WriteMailActivity;
import com.inspur.playwork.weiyou.adapter.AddMemberListener;
import com.inspur.playwork.weiyou.adapter.ContactListPagerAdapter;
import com.inspur.playwork.weiyou.adapter.ContactListRecyclerAdapter;
import com.inspur.playwork.weiyou.adapter.ContactSelectorRecyclerAdapter;
import com.inspur.playwork.weiyou.adapter.SearchPersonAdapter;
import com.inspur.playwork.weiyou.adapter.SelectedContactAdapter;
import com.inspur.playwork.weiyou.store.ContactSelectorOperation;
import com.inspur.playwork.weiyou.utils.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 孙 on 2015/12/4 0004.
 */
public class ContactSelectorFragment extends Fragment implements AddMemberListener,ContactSelectorOperation {

    private static final String TAG = "ContactSelectorFragment";
//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    private WriteMailActivity wyma;

//    private RecyclerView contactListRecyclerView;

    private ViewPager mContactViewPager;

    private ContactListPagerAdapter mContactListPagerAdapter;

    private AutoCompleteTextView searchPersonAutoCompleteTextView;
    private TextView chooseContactTextView, chooseGroupTextView, chooseDepartmentTextView;

    private View rootView;

    private SearchPersonAdapter mSearchPersonAdapter;

    private ContactSelectorRecyclerAdapter.PersonSelectedListener personSelectedListener;

    private ContactListRecyclerAdapter mRecentListRecyclerAdapter, mGroupListRecyclerAdapter, mDepartmentListRecyclerAdapter;

    private ListView selectedContactListView;
    private SelectedContactAdapter selectedContactListAdapater;

    private ContactHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WriteMailActivity)getActivity();
        wyma.vuStores.setContactSelectOperation(this);
        wyma.vuStores.initContactSelectorData();
        handler = new ContactHandler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.wy_fragment_contact_selector, container, false);
        wyma.showProgressDialog("正在加载...");
        initView(rootView);
        return rootView;
    }

    private void initView(View v) {
        chooseContactTextView = (TextView) v.findViewById(R.id.tv_choose_contact);//联系人
        chooseGroupTextView = (TextView) v.findViewById(R.id.tv_choose_group);//联系组
        chooseDepartmentTextView = (TextView) v.findViewById(R.id.tv_choose_department);//部门

//        contactListRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_contact_list);// ?
        searchPersonAutoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.actv_search_person);
        v.findViewById(R.id.select_contact_ok_btn).setOnClickListener(choosePersonClickListener); // 确定按钮
        v.findViewById(R.id.select_contact_cancel_btn).setOnClickListener(choosePersonClickListener); // 取消按钮

        /* 通讯录 */
        chooseContactTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_press)); // 联系人tag
        chooseContactTextView.setOnClickListener(chooseContactByTypeListener);
        chooseGroupTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_normal)); // 联系组tag
        chooseGroupTextView.setOnClickListener(chooseContactByTypeListener);
        chooseDepartmentTextView.setTextColor(getResources().getColor(R.color.contact_list_tag_normal)); // 部门成员tag
        chooseDepartmentTextView.setOnClickListener(chooseContactByTypeListener);

        RecyclerView mRecentListRecyclerView = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.wy_contact_recycler_view, null);
        mRecentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecentListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mRecentListRecyclerView, wyma.vuStores.getSelectedContacts());
        mRecentListRecyclerAdapter.setDataList(1, wyma.vuStores.getMRecentChatList());
        mRecentListRecyclerAdapter.setAddMemberListener(this);
        mRecentListRecyclerView.setAdapter(mRecentListRecyclerAdapter);

        RecyclerView mGroupListRecyclerView = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.wy_contact_recycler_view, null);
        mGroupListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGroupListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mGroupListRecyclerView, wyma.vuStores.getSelectedContacts());
        mGroupListRecyclerAdapter.setDataList(2, wyma.vuStores.getMRecentChatList());
        mGroupListRecyclerAdapter.setAddMemberListener(this);
        mGroupListRecyclerView.setAdapter(mGroupListRecyclerAdapter);

        RecyclerView mDepartmentListRecyclerView = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.wy_contact_recycler_view, null);
        mDepartmentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDepartmentListRecyclerAdapter = new ContactListRecyclerAdapter(getActivity(), mDepartmentListRecyclerView, wyma.vuStores.getSelectedContacts());
        mDepartmentListRecyclerAdapter.setDataList(3, wyma.vuStores.getMDepartmentList());
        mDepartmentListRecyclerAdapter.setAddMemberListener(this);
        mDepartmentListRecyclerView.setAdapter(mDepartmentListRecyclerAdapter);

        List<View> viewList = new ArrayList<>();
        viewList.add(mRecentListRecyclerView);
        viewList.add(mGroupListRecyclerView);
        viewList.add(mDepartmentListRecyclerView);
        mContactListPagerAdapter = new ContactListPagerAdapter(viewList);
        mContactViewPager = (ViewPager) v.findViewById(R.id.vp_contact_list);
        mContactViewPager.setAdapter(mContactListPagerAdapter);
        mContactViewPager.addOnPageChangeListener(contactPageChangeListener);

        mSearchPersonAdapter = new SearchPersonAdapter(getActivity(), wyma.vuStores.getSelectedContacts(),true);
        mSearchPersonAdapter.setDataList(wyma.vuStores.getMSearchPersonResultArrayList());
        searchPersonAutoCompleteTextView.setAdapter(mSearchPersonAdapter);
        searchPersonAutoCompleteTextView.setThreshold(1); // 1个字符开始匹配
        searchPersonAutoCompleteTextView.addTextChangedListener(searchNameTextWatcher);
        searchPersonAutoCompleteTextView.setOnItemClickListener(searchResultOnItemClickListener);
        searchPersonAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && "".equals(searchPersonAutoCompleteTextView.getText().toString())) {
                    searchPersonAutoCompleteTextView.setHint("");
                } else if (!hasFocus && "".equals(searchPersonAutoCompleteTextView.getText().toString())) {
                    searchPersonAutoCompleteTextView.setHint("搜索");
                }
            }
        });
        searchPersonAutoCompleteTextView.clearFocus();

        selectedContactListView = (ListView) v.findViewById(R.id.selected_contact_list);
        selectedContactListAdapater = new SelectedContactAdapter(getActivity(),wyma.vuStores.getSelectedContacts());
        selectedContactListView.setAdapter(selectedContactListAdapater);
        selectedContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                wyma.vuStores.removeSelectedContact(i);
                selectedContactListAdapater.notifyDataSetChanged();
                mRecentListRecyclerAdapter.notifyDataSetChanged();
                mGroupListRecyclerAdapter.notifyDataSetChanged();
                mDepartmentListRecyclerAdapter.notifyDataSetChanged();
                mSearchPersonAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 通讯录page监听器
     */
    private ViewPager.OnPageChangeListener contactPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
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
        public void onPageScrollStateChanged(int state) {}
    };

    /**
     * 通讯录搜索框TextWatcher
     */
    private TextWatcher searchNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeMessages(1001);
            handler.sendMessageDelayed(handler.obtainMessage(1001,s.toString()),500);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    /**
     * 通讯录搜索结果OnItemClickListener
     */
    private AdapterView.OnItemClickListener searchResultOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            wyma.vuStores.addContactFromSearchResult(position);
            wyma.hideInputMethod(); // 隐藏软键盘
            searchPersonAutoCompleteTextView.setText(""); // 清空输入内容
            searchPersonAutoCompleteTextView.clearFocus(); // 清除焦点
//            CheckBox contact_cb = (CheckBox) view.findViewById(R.id.checkbox_mail_contact);
//            contact_cb.toggle();
        }
    };

    public void setListener(ContactSelectorRecyclerAdapter.PersonSelectedListener personSelectedListener) {
        this.personSelectedListener = personSelectedListener;
    }

    /**
     * 通讯录title标签OnClickListener
     */
    private View.OnClickListener chooseContactByTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_choose_contact:
                    changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);
                    mContactViewPager.setCurrentItem(0);
                    break;
                case R.id.tv_choose_group:
                    changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_press, R.color.contact_list_tag_normal);
                    mContactViewPager.setCurrentItem(1);
                    break;
                case R.id.tv_choose_department:
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
                case R.id.select_contact_ok_btn:
                    wyma.vuStores.selectOver();
                    break;
                case R.id.select_contact_cancel_btn:
                    break;
            }
            wyma.vuStores.setIsInContactSelector(false);
            wyma.onBackPressed();
        }
    };

    private void changeTagColor(int recentColor, int groupColor, int departmentColor) {
        chooseContactTextView.setTextColor(getResources().getColor(recentColor));
        chooseGroupTextView.setTextColor(getResources().getColor(groupColor));
        chooseDepartmentTextView.setTextColor(getResources().getColor(departmentColor));
    }

    /**
     * 增加单个人员接口回调方法
     *
     * @param userInfoBean 添加的人员信息
     */
    @Override
    public void addMember(UserInfoBean userInfoBean) {
        wyma.vuStores.addMember(userInfoBean);
        selectedContactListAdapater.notifyDataSetChanged();//刷新已选中人员列表视图
    }

    /**
     * 增加一组人员接口回调方法
     *
     * @param groupInfoBean 添加的群组信息
     */
    @Override
    public void addMember(GroupInfoBean groupInfoBean) {
        wyma.vuStores.addMember(groupInfoBean);
        selectedContactListAdapater.notifyDataSetChanged();//刷新已选中人员列表视图
    }

    @Override
    public void refreshAllListView(ArrayList<UserInfoBean> mRecentChatList, ArrayList<GroupInfoBean> mGroupChatList) {
        wyma.dismissProgressDialog();
        mRecentListRecyclerAdapter.setDataList(1, mRecentChatList);
        mRecentListRecyclerAdapter.notifyDataSetChanged();
        mGroupListRecyclerAdapter.setDataList(2, mGroupChatList);
        mGroupListRecyclerAdapter.notifyDataSetChanged();
        mContactListPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshDepartmentListView(ArrayList<UserInfoBean> mDepartmentList) {
        mDepartmentListRecyclerAdapter.setDataList(3, mDepartmentList);
        mDepartmentListRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshSearchResultListView(ArrayList<UserInfoBean> mSearchPersonResultArrayList) {
        mSearchPersonAdapter.setDataList(mSearchPersonResultArrayList);
        mSearchPersonAdapter.notifyDataSetChanged();
    }

    private static class ContactHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    DBUtil.searchPerson((String)msg.obj);
                    break;
            }
        }
    }

}
