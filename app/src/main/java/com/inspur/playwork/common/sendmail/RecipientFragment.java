package com.inspur.playwork.common.sendmail;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.common.sendmail.adapter.ContactsListAdapter;
import com.inspur.playwork.common.sendmail.adapter.RecipientListAdapter;
import com.inspur.playwork.common.sendmail.adapter.SearchUserAdapter;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.view.common.progressbar.CommonDialog;
import com.inspur.playwork.view.message.chat.ChatActivityNew;
import com.inspur.playwork.view.message.chat.ContactListPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bugcode on 2016/3/24.
 */
public class RecipientFragment extends Fragment implements RecipientView, View.OnClickListener,
        RecipientListAdapter.OnRecipientListClickListener, ContactsListAdapter.OnContactsListClickListener {

    private Activity activity;
    private View rootView;
    private AutoCompleteTextView searchUserView;
    private SearchUserAdapter searchUserAdapter;
    private TextView recentTag, groupTag, departmentTag;
    private RecyclerView recentListView, groupListView, departmentListView;
    private ContactsListAdapter recentListAdapter, groupListAdapter, departmentListAdapter;
    private ViewPager contactsViewPager;
    private ContactListPagerAdapter contactsPagerAdapter;
    private RecyclerView recipientListView;
    private RecipientListAdapter recipientListAdapter;
    private DialogFragment progressDialog;

    private RecipientPresenter recipientPresenter;
//    private BitmapCacheManager bitmapCacheManager;
    private ArrayMap<String, Long> avatarCache;
    private RecipientFinishListener recipientFinishListener;
    private ArrayList<UserInfoBean> recipientList;
    private ArrayList<UserInfoBean> searchUserList;

    public interface RecipientFinishListener {
        void updateRecipient(ArrayList<UserInfoBean> recipientList);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        ((ChatActivityNew) activity).setSendButtonVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_smallmail_recipient, container, false);
        this.initView(rootView);
        this.initData();
        return rootView;
    }

    private void initData() {
        avatarCache = ((PlayWorkApplication) activity.getApplication()).getAvatars();
//        bitmapCacheManager = BitmapCacheManager.findOrCreateRetainFragment(getFragmentManager());
        recipientPresenter = new RecipientPresenterImpl(this, recipientList);
        recipientPresenter.register();
        recipientPresenter.initContactsList();
        recipientPresenter.initRecipientList();
    }

    private void initView(View rootView) {
        this._initSearchView(rootView);
        this._initContactsView(rootView);
        this._initRecipientView(rootView);
    }

    private void _initSearchView(View view) {
        searchUserView = (AutoCompleteTextView) view.findViewById(R.id.actv_search_person);
        searchUserView.setThreshold(1); // 1个字符开始匹配
        searchUserView.addTextChangedListener(searchUserWatcher);
        searchUserView.setOnItemClickListener(searchUserOnItemClick);
        searchUserView.setOnFocusChangeListener(searchUserOnFocusChange);
        searchUserAdapter = new SearchUserAdapter();
        searchUserView.setAdapter(searchUserAdapter);
    }

    private View.OnFocusChangeListener searchUserOnFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && searchUserView.getText().toString().equals("")) {
                searchUserView.setHint("");
            } else if (!hasFocus && searchUserView.getText().toString().equals("")) {
                searchUserView.setHint("搜索");
            }
        }
    };

    private AdapterView.OnItemClickListener searchUserOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            UserInfoBean userInfo = searchUserList.get(position);
            hideInputMethod(); // 隐藏软键盘
            searchUserView.setText(""); // 清空输入内容
            searchUserView.clearFocus(); // 清除焦点
            recipientPresenter.addRecipient(userInfo);
        }
    };

    private TextWatcher searchUserWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            recipientPresenter.searchUserByKeyWord(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void _initContactsView(View view) {
        // TAG
        recentTag = (TextView) view.findViewById(R.id.tv_choose_contact);
        recentTag.setOnClickListener(this);
        groupTag = (TextView) view.findViewById(R.id.tv_choose_group);
        groupTag.setOnClickListener(this);
        departmentTag = (TextView) view.findViewById(R.id.tv_choose_department);
        departmentTag.setOnClickListener(this);
        this.changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);

        // 最近联系人
        recentListView = (RecyclerView) activity.getLayoutInflater().inflate(R.layout.layout_contact_list, null);
        recentListView.setLayoutManager(new LinearLayoutManager(activity));
        recentListAdapter = new ContactsListAdapter(activity);

        // 群
        groupListView = (RecyclerView) activity.getLayoutInflater().inflate(R.layout.layout_contact_list, null);
        groupListView.setLayoutManager(new LinearLayoutManager(activity));
        groupListAdapter = new ContactsListAdapter(activity);

        // 部门人员
        departmentListView = (RecyclerView) activity.getLayoutInflater().inflate(R.layout.layout_contact_list, null);
        departmentListView.setLayoutManager(new LinearLayoutManager(activity));
        departmentListAdapter = new ContactsListAdapter(activity);

        List<View> viewList = new ArrayList<>();
        viewList.add(recentListView);
        viewList.add(groupListView);
        viewList.add(departmentListView);
        contactsPagerAdapter = new ContactListPagerAdapter(viewList);
        contactsViewPager = (ViewPager) view.findViewById(R.id.vp_contact_list);
        contactsViewPager.setAdapter(contactsPagerAdapter);
        contactsViewPager.addOnPageChangeListener(contactsPageChangeListener);

        progressDialog = CommonDialog.getInstance("加载中...", true);
    }

    private ViewPager.OnPageChangeListener contactsPageChangeListener = new ViewPager.OnPageChangeListener() {
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

    private void changeTagColor(int recentColor, int groupColor, int departmentColor) {
        recentTag.setTextColor(getResources().getColor(recentColor));
        groupTag.setTextColor(getResources().getColor(groupColor));
        departmentTag.setTextColor(getResources().getColor(departmentColor));
    }

    private void _initRecipientView(View view) {
        recipientListView = (RecyclerView) view.findViewById(R.id.recycler_chat_person);
        recipientListView.setLayoutManager(new LinearLayoutManager(activity));
        recipientListAdapter = new RecipientListAdapter();
        view.findViewById(R.id.iv_choose_person_ok).setOnClickListener(this);
        view.findViewById(R.id.iv_choose_person_cancel).setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recipientPresenter.unRegister();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_choose_contact:
                changeTagColor(R.color.contact_list_tag_press, R.color.contact_list_tag_normal, R.color.contact_list_tag_normal);
                contactsViewPager.setCurrentItem(0);
                break;
            case R.id.tv_choose_group:
                changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_press, R.color.contact_list_tag_normal);
                contactsViewPager.setCurrentItem(1);
                break;
            case R.id.tv_choose_department:
                changeTagColor(R.color.contact_list_tag_normal, R.color.contact_list_tag_normal, R.color.contact_list_tag_press);
                contactsViewPager.setCurrentItem(2);
                break;
            case R.id.iv_choose_person_ok:
                recipientPresenter.updateRecipient();
                break;
            case R.id.iv_choose_person_cancel:
                closeFragment();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRecipientItemClick(View v, UserInfoBean userInfo) {
        recipientPresenter.delRecipient(userInfo);
    }

    @Override
    public void onContactsItemClick(View v, UserInfoBean userInfo) {
        recipientPresenter.addRecipient(userInfo);
    }

    @Override
    public void onContactsItemClick(View v, GroupInfoBean groupInfo) {
        for (UserInfoBean userInfo : groupInfo.getMemberList()) {
            recipientPresenter.addRecipient(userInfo);
        }
    }

    @Override
    public void initRecentList(ArrayList<UserInfoBean> recentUserList, ArrayList<GroupInfoBean> recentGroupList) {
        recentListAdapter.setContactsList(recentUserList);
        this.setContactsListAdapterParam(recentListAdapter, 1);
        recentListView.setAdapter(recentListAdapter);

        groupListAdapter.setContactsList(recentGroupList);
        this.setContactsListAdapterParam(groupListAdapter, 2);
        groupListView.setAdapter(groupListAdapter);
    }

    @Override
    public void initDepartmentList(ArrayList<UserInfoBean> departmentUserList) {
        departmentListAdapter.setContactsList(departmentUserList);
        this.setContactsListAdapterParam(departmentListAdapter, 3);
        departmentListView.setAdapter(departmentListAdapter);
    }

    private void setContactsListAdapterParam(ContactsListAdapter contactsListAdapter, int contactsType) {
        contactsListAdapter.setContactsType(contactsType);
        contactsListAdapter.setAvatarCache(avatarCache);
//        if (bitmapCacheManager != null)
//            contactsListAdapter.setBitmapCacheManager(bitmapCacheManager);
        contactsListAdapter.setOnContactsListClickListener(this);
    }

    @Override
    public void notifySearchUserList(ArrayList<UserInfoBean> searchUserList) {
        this.searchUserList = searchUserList;
        searchUserAdapter.setDataList(searchUserList);
        searchUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void initRecipientListView(ArrayList<UserInfoBean> recipientList) {
        recipientListAdapter.setDataList(recipientList);
        recipientListAdapter.setAvatarCache(avatarCache);
//        if (bitmapCacheManager != null)
//            recipientListAdapter.setBitmapCacheManager(bitmapCacheManager);
        recipientListAdapter.setOnRecipientListClickListener(this);
        recipientListView.setAdapter(recipientListAdapter);
    }

    @Override
    public void notifyRecipientListAdapter(ArrayList<UserInfoBean> recipientList) {
        recipientListAdapter.setDataList(recipientList);
        recipientListAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyRecipient(ArrayList<UserInfoBean> recipientList) {
        recipientFinishListener.updateRecipient(recipientList);
        this.closeFragment();
    }

    @Override
    public void showDialog() {
        progressDialog.show(getFragmentManager(), null);
    }

    @Override
    public void dismissDialog() {
        progressDialog.dismiss();
    }

    private void closeFragment() {
        ((ChatActivityNew) activity).setSendButtonVisibility(View.VISIBLE);
        ((ChatActivityNew) activity).setIsRecipientFragmentShow(false);
        getFragmentManager().popBackStack();
    }

    private void hideInputMethod() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setRecipientFinishListener(RecipientFinishListener recipientFinishListener) {
        this.recipientFinishListener = recipientFinishListener;
    }

    public void setRecipientList(ArrayList<UserInfoBean> recipientList) {
        this.recipientList = recipientList;
    }
}
