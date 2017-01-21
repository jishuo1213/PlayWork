package com.inspur.playwork;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.core.PlayWorkServiceNew;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.IMMLeaks;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.db.DBOperation;
import com.inspur.playwork.versionUpdate.VersionPlaywork;
import com.inspur.playwork.view.ApplicationFragment;
import com.inspur.playwork.view.UnReadMsgChangeListener;
import com.inspur.playwork.view.application.addressbook.AddressBookActivity;
import com.inspur.playwork.view.common.BadgeView;
import com.inspur.playwork.view.common.ChoseYearMonthAdapter;
import com.inspur.playwork.view.common.SpinerWindow;
import com.inspur.playwork.view.message.VChatFragment;
import com.inspur.playwork.view.message.chat.ChatActivityNew;
import com.inspur.playwork.view.profile.my.ProfileFragment;
import com.inspur.playwork.view.timeline.TimeLineFragmentNew2;

import java.util.Calendar;


/**
 * app主页
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class MainActivity extends AppCompatActivity implements UnReadMsgChangeListener, View.OnClickListener, ChoseYearMonthAdapter.ChoseTimeListener {

    private static final String TAG = "MainActivity";

    private static final String TAB_INDEX = "tab_index";
    private static final String KEY = "tab_key";

    private static final String TIME_LINE_TAG = "timelinetag";
    private static final String VCHAT_TAG = "vchat_tag";
    private static final String APPLICATION_TAG = "app_tag";
    private static final String SETTING_TAG = "setting_tag";

    private static final String[] TAGS = {TIME_LINE_TAG, VCHAT_TAG, APPLICATION_TAG, SETTING_TAG};

    private TextView mTvTitle; // 标题
    private Fragment[] viewFragments; // fragment容器
    private Fragment mTimeLineFragment, mMessageFragment; // 时间轴fragment
    private ImageView[] mImageViews; // 图片按钮
    private TextView[] mTextViews; // 按钮文字
    private int currentTabIndex = 0; // 记录当前fragment的index

//    private BitmapCacheManager bitmapCacheManager;

    private View timeLine, message;

    private BadgeView timeLineMsgCount, normalMsgCount;

    private boolean isAddOrEditTask;

    private Calendar today;

    private TextView todayTextView;
    private TextView monthTextView;
    private ImageButton rightImageButton;
    private ImageButton searchImageButton;

    private boolean isCrossViewShow;

    private PopupWindow choseYearPopWidow, choseMonthPopWindow;

    private boolean isMoveToBack = false;

    private boolean isUserLogOut = false;

    private View netErrorLayout;
    private boolean isCanReconnect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IMMLeaks.fixFocusedViewLeak(getApplication());
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: MainActivity savedInstanceState == null" + (savedInstanceState == null));
        setContentView(R.layout.activity_main);
        initViews(savedInstanceState);
        today = Calendar.getInstance();
        DateUtils.trimCalendarDate(today);
        MessageStores.getInstance().queryLocalChatList();
        initTabView(savedInstanceState);
        Dispatcher.getInstance().register(this);
//        Log.i(TAG, "onCreate: " + EncryptUtil.aesDecrypt("n6tykwhelMpiFWrL/F4atjvUol4YrDK+CtTvM2dZ0Vg="));
//        (((PlayWorkApplication) getApplicationContext()).getDbOperation()).printAllMessageHistory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this, PlayWorkServiceNew.class);
        bindService(bindIntent, connection, 0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMoveToBack) {
            ((PlayWorkServiceNew) binder.getService()).setAppVisable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_INDEX, currentTabIndex);
        outState.putString(KEY, PreferencesHelper.getInstance().key);
        Log.i(TAG, "onSaveInstanceState: ===================");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState: =========================");
        PreferencesHelper.getInstance().key = savedInstanceState.getString(KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ============");
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
//        if (!isUserLogOut)
//            startActivity(new Intent(this, DummyActivity.class));
        Log.i(TAG, "onDestroy: ----------");
        super.onDestroy();
        if (Dispatcher.getInstance() != null)
            Dispatcher.getInstance().unRegister(this);

        for (Fragment fragment : viewFragments) {
            fragment = null;
        }
        viewFragments = null;
        mTimeLineFragment = null;
        mMessageFragment = null;
        if (isUserLogOut) {
            ((PlayWorkApplication) getApplication()).unRegisterDispather();
            disconnect();
        } else {
            Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.CANCEL_ALL_NOTIFICATION);
        }
    }

    /**
     * 初始化view组件
     *
     * @param savedInstanceState
     */
    private void initViews(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            currentTabIndex = savedInstanceState.getInt(TAB_INDEX);
        mTvTitle = (TextView) findViewById(R.id.tv_title); // 实例化标题
        timeLine = findViewById(R.id.iv_timeline);
        message = findViewById(R.id.iv_message);
        todayTextView = (TextView) findViewById(R.id.tv_today);
        todayTextView.setOnClickListener(this);

        monthTextView = (TextView) findViewById(R.id.tv_addition);
        monthTextView.setVisibility(View.VISIBLE);
        monthTextView.setOnClickListener(this);

        rightImageButton = (ImageButton) findViewById(R.id.iv_right);
        rightImageButton.setOnClickListener(this);
        mTvTitle.setOnClickListener(this);

        netErrorLayout = findViewById(R.id.error_layout);
        searchImageButton = (ImageButton) findViewById(R.id.iv_search);
        searchImageButton.setOnClickListener(this);
    }

    /**
     * 初始化tab组件
     *
     * @param savedInstanceState
     */
    private void initTabView(Bundle savedInstanceState) {
        ApplicationFragment mApplicationFragment = null;
        ProfileFragment mProfileFragment = null;
        if (savedInstanceState == null) {
            mTimeLineFragment = new TimeLineFragmentNew2();
            /*mMessageFragment = new VChatFragment();
            mApplicationFragment = new ApplicationFragment();
            mProfileFragment = new ProfileFragment();*/
        } else {
            mTimeLineFragment = getFragmentManager().findFragmentByTag(TIME_LINE_TAG);
            mMessageFragment = getFragmentManager().findFragmentByTag(VCHAT_TAG);
            mApplicationFragment = (ApplicationFragment) getFragmentManager().findFragmentByTag(APPLICATION_TAG);
            mProfileFragment = (ProfileFragment) getFragmentManager().findFragmentByTag(SETTING_TAG);
        }

        viewFragments = new Fragment[]{mTimeLineFragment, mMessageFragment,
                mApplicationFragment, mProfileFragment};

        // 实例化按钮
        mImageViews = new ImageView[4];
        mImageViews[0] = (ImageView) findViewById(R.id.iv_timeline);
        mImageViews[1] = (ImageView) findViewById(R.id.iv_message);
        mImageViews[2] = (ImageView) findViewById(R.id.iv_application);
        mImageViews[3] = (ImageView) findViewById(R.id.iv_profile);
        // 实例化按钮文字
        mTextViews = new TextView[4];
        mTextViews[0] = (TextView) findViewById(R.id.tv_timeline); // 时间轴
        mTextViews[1] = (TextView) findViewById(R.id.tv_message); // 微聊
        mTextViews[2] = (TextView) findViewById(R.id.tv_application); // 应用
        mTextViews[3] = (TextView) findViewById(R.id.tv_profile); // 我
        currentTabIndex = 0;
        mImageViews[currentTabIndex].setSelected(true);
        mTextViews[currentTabIndex].setTextColor(getResources().getColor(R.color.toolbar_text_pressed_color));
        // 添加显示第一个fragment
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mTimeLineFragment, TIME_LINE_TAG)
                    .commit();
            Log.i(TAG, "initTabView: update version");
            new VersionPlaywork(MainActivity.this, PreferencesHelper.getInstance().getVersionInfo(), new Handler());
        } else {
            int index = 0;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Log.i(TAG, "initTabView: fragment array length" + viewFragments.length + currentTabIndex);
            for (Fragment fragment : viewFragments) {
                Log.i(TAG, "initTabView: index" + index + "fragment == null" + (fragment == null));
                if (fragment != null && index != currentTabIndex) {
                    ft.hide(fragment);
                }
                index++;
            }
            if (currentTabIndex != 0) {
//                ft.remove(mTimeLineFragment);
                ((TimeLineFragmentNew2) viewFragments[0]).setNeedShowViews(true);
            }
            ft.commit();
        }
    }

    long firstClickTabTime;

    public void onTabClicked(View view) {
        int index = 0;
        switch (view.getId()) {
            case R.id.rl_timeline:
                index = 0;
                if (index != currentTabIndex) {
                    showTodayTextView();
                    monthTextView.setVisibility(View.VISIBLE);
                    setYearMonth(((TimeLineFragmentNew2) mTimeLineFragment).selectedDay);
                    rightImageButton.setVisibility(View.GONE);
                    searchImageButton.setVisibility(View.GONE);
                } else {
                    if (System.currentTimeMillis() - firstClickTabTime < 500) {//双击触发
                        ((TimeLineFragmentNew2) mTimeLineFragment).showNetUnReadDay();
                    } else {
                        firstClickTabTime = System.currentTimeMillis();
                    }
                }
                if (viewFragments[index] == null)
                    viewFragments[0] = new TimeLineFragmentNew2();
                break;
            case R.id.rl_message:
                index = 1;
                mTvTitle.setText(R.string.message);
                todayTextView.setVisibility(View.GONE);
                monthTextView.setVisibility(View.GONE);
                rightImageButton.setVisibility(View.VISIBLE);
                searchImageButton.setVisibility(View.VISIBLE);
                if (viewFragments[index] == null) {
                    viewFragments[1] = new VChatFragment();
                    mMessageFragment = viewFragments[1];
                }
                break;
            case R.id.rl_application:
                index = 2;
                mTvTitle.setText(R.string.application);
                todayTextView.setVisibility(View.GONE);
                monthTextView.setVisibility(View.GONE);
                rightImageButton.setVisibility(View.GONE);
                searchImageButton.setVisibility(View.GONE);
                if (viewFragments[index] == null)
                    viewFragments[2] = new ApplicationFragment();
                break;
            case R.id.rl_profile:
                index = 3;
                mTvTitle.setText(R.string.profile);
                todayTextView.setVisibility(View.GONE);
                monthTextView.setVisibility(View.GONE);
                rightImageButton.setVisibility(View.GONE);
                searchImageButton.setVisibility(View.GONE);
                if (viewFragments[index] == null)
                    viewFragments[3] = new ProfileFragment();
                break;
        }

        if (currentTabIndex != index) {
            /*
            切换显示内容
             */
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.hide(viewFragments[currentTabIndex]);
            if (!viewFragments[index].isAdded()) {
                Log.i(TAG, "onTabClicked: add fragment to container");
                fragmentTransaction.add(R.id.fragment_container, viewFragments[index], TAGS[index]);
            }
            fragmentTransaction.show(viewFragments[index]).commit();
            mImageViews[currentTabIndex].setSelected(false);
            mTextViews[currentTabIndex].setTextColor(getResources().getColor(R.color.toolbar_text_normal_color));
            /*
            设置当前tab为选中状态
             */
            mImageViews[index].setSelected(true);
            mTextViews[index].setTextColor(getResources().getColor(R.color.toolbar_text_pressed_color));
            currentTabIndex = index;
        }
    }

    private void showTodayTextView() {
        if (!DateUtils.isSameYearMonth(((TimeLineFragmentNew2) mTimeLineFragment).selectedDay))
            todayTextView.setVisibility(View.VISIBLE);
        else {
            todayTextView.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setYearMonth(Calendar selectedDay) {
        mTvTitle.setText(selectedDay.get(Calendar.YEAR) + " 年 ");
        int month = selectedDay.get(Calendar.MONTH) + 1;
        monthTextView.setText(month >= 10 ? (month) + " 月" : ("0" + month) + " 月");
        showTodayTextView();
    }

    public void setIsCrossViewShow(boolean isCrossViewShow) {
        this.isCrossViewShow = isCrossViewShow;
    }

    private PlayWorkServiceNew.Binder binder;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (PlayWorkServiceNew.Binder) service;
            PlayWorkServiceNew playWorkService = (PlayWorkServiceNew) binder.getService();
//            playWorkService.setAppVisable(true);
            // 主动调用更新版本的方法
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            PlayWorkServiceNew playWorkService = (PlayWorkServiceNew) binder.getService();
//            playWorkService.setAppVisable(false);
        }
    };

    public void disconnect() {
        PlayWorkServiceNew playWorkService = (PlayWorkServiceNew) binder.getService();
        playWorkService.logout();
//        playWorkService.disconnectFromTimeLineServer(ConnectType.DISCONNECT_WHEN_EXIT_APP);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMsgCountChange(int type, int count) {
        switch (type) {
            case 0:
                if (timeLineMsgCount == null) {
                    timeLineMsgCount = new BadgeView(this);
                    timeLineMsgCount.setTargetView(timeLine);
                    timeLineMsgCount.setBackgroundResource(R.drawable.bageview_back);
                }
//                int count = TimeLineStores.getInstance().getUnReadMessageList().size();
                if (count == 0) {
                    timeLineMsgCount.setVisibility(View.GONE);
                } else {
                    timeLineMsgCount.setVisibility(View.VISIBLE);
                    timeLineMsgCount.setText(count + "");
                }
                break;
            case 1:
                if (normalMsgCount == null) {
                    normalMsgCount = new BadgeView(this);
                    normalMsgCount.setTargetView(message);
                    normalMsgCount.setBackgroundResource(R.drawable.bageview_back);
                }
                int normalCount = MessageStores.getInstance().getvChatUnReadMsg() == null ? 0 :
                        MessageStores.getInstance().getvChatUnReadMsg().size();
                Log.i(TAG, "onMsgCountChange: 微聊未读个数" + normalCount);
                if (normalCount == 0) {
                    normalMsgCount.setVisibility(View.GONE);
                } else {
                    normalMsgCount.setVisibility(View.VISIBLE);
                    normalMsgCount.setText(normalCount + "");
                }
                break;
            case 2:
                break;
        }
    }

    public void setIsAddOrEditTask(boolean isAddOrEditTask) {
        this.isAddOrEditTask = isAddOrEditTask;
    }

/*    public void setAddEditDialog(DialogFragment addEditDialog) {
        this.addEditDialog = addEditDialog;
    }*/

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (isAddOrEditTask) {
            //addEditDialog.getDialog().cancel();
            ((TimeLineFragmentNew2) mTimeLineFragment).hideAddTodayView();
            isAddOrEditTask = false;
            return;
        }

        if (isCrossViewShow) {
            ((TimeLineFragmentNew2) mTimeLineFragment).hideCrossDayView();
            return;
        }

        long lastBackPressTime = 0;
        if (currentTime - lastBackPressTime < 1000) {
            super.onBackPressed();
        } else {
//            lastBackPressTime = currentTime;
//            UItoolKit.showToastShort(this, "再按一次退出");
            moveTaskToBack(false);
            ((PlayWorkServiceNew) binder.getService()).setAppVisable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_today:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(today.getTimeInMillis());
                ((TimeLineFragmentNew2) mTimeLineFragment).setCalendarToDate(calendar);
                break;
            case R.id.iv_right:
                Intent intent = new Intent(this, ChatActivityNew.class);
                startActivity(intent);
                break;
            case R.id.tv_title:
                if (currentTabIndex != 0)
                    return;
                if (choseYearPopWidow == null) {
                    ChoseYearMonthAdapter yearAdapter = new ChoseYearMonthAdapter(90, 2010, ChoseYearMonthAdapter.YEAR_TYPE);
                    yearAdapter.setListener(this);
                    choseYearPopWidow = new SpinerWindow(this, yearAdapter);
                }
                ((SpinerWindow) choseYearPopWidow).showPopWindow(v);
                break;
            case R.id.tv_addition:
                if (currentTabIndex != 0)
                    return;
                if (choseMonthPopWindow == null) {
                    ChoseYearMonthAdapter monthAdapter = new ChoseYearMonthAdapter(12, 1, ChoseYearMonthAdapter.MONTH_TYPE);
                    monthAdapter.setListener(this);
                    choseMonthPopWindow = new SpinerWindow(this, monthAdapter);
                }
                ((SpinerWindow) choseMonthPopWindow).showPopWindow(v);
                break;
            case R.id.error_layout:
                if (isCanReconnect) {
                    PlayWorkServiceNew playWorkService = (PlayWorkServiceNew) binder.getService();
                    playWorkService.reconnectToServer();
                }
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, AddressBookActivity.class));

                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: ====================" + requestCode + (mMessageFragment == null));
//        if (mMessageFragment != null && requestCode == VChatFragment.UPDATE_VCHAT_LIST) {
//            Log.i(TAG, "onActivityResult: dispatch");
//            mMessageFragment.onActivityResult(requestCode, resultCode, data);
//        }
    }

    @Override
    public void onYearChose(int year, int type) {
        switch (type) {
            case ChoseYearMonthAdapter.YEAR_TYPE:
                ((TimeLineFragmentNew2) mTimeLineFragment).setYear(year);
                choseYearPopWidow.dismiss();
                break;
            case ChoseYearMonthAdapter.MONTH_TYPE:
                ((TimeLineFragmentNew2) mTimeLineFragment).setMonth(year - 1);
                choseMonthPopWindow.dismiss();
                break;
        }
    }

    public void onEventMainThread(UpdateUIAction action) {
        int type = action.getActionType();
        switch (type) {
            case MessageActions.SET_VCHAT_UNREAD_NUM:
                onMsgCountChange(1, 1);
                break;
            case CommonActions.DISCONNECT_FROM_SERVER:
                netErrorLayout.setVisibility(View.VISIBLE);
                netErrorLayout.setOnClickListener(this);
                isCanReconnect = true;
                break;
            case CommonActions.CONNECT_SERVER:
                netErrorLayout.setVisibility(View.GONE);
                isCanReconnect = true;
                break;
        }
    }

    public void setUserLogOut(boolean isUserLogOut) {
        this.isUserLogOut = isUserLogOut;
    }
}
