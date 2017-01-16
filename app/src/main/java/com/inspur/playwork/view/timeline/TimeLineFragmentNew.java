package com.inspur.playwork.view.timeline;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.timeline.CalendarDateBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.timeline.TimeLineStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.UnReadMsgChangeListener;
import com.inspur.playwork.view.common.NoScrollViewPager;
import com.inspur.playwork.view.message.chat.ChatActivityNew;
import com.inspur.playwork.view.timeline.addtask.AddCrossDayRootView;
import com.inspur.playwork.view.timeline.addtask.AddEditTaskResultListener;
import com.inspur.playwork.view.timeline.addtask.AddTodayRootView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fan on 15-9-2.
 */
public class TimeLineFragmentNew extends Fragment implements RecyclerTaskAdapter.TaskItemEventListener,
        CalendarScrollViewNew.OnCalendarChangeListener, RecycleCalendarAdapter.DateClickListener, TaskRootView.TaskRootViewEventListener,
        AddEditTaskResultListener, AddCrossDayRootView.AddCrossDayEventListener {

    private static final String TAG = "TimeLineFragmentNewFan";

    private static final int GET_TASK_CUSTOME_PROPERTY = 101;

    private static final int MONTH_MODE = 1;
    private static final int WEEK_MODE = 2;

    private static final int EDIT_TASK_CONTENT_MONTH_WEEK = 1;

    private static final int EDIT_CROSS_DAY_TIME_WEEK_MONTH = 2;

    private static final int ADD_TASK_MONTH_WEEK = 4;

    private static final int EDIT_TODAY_TASK_TIME_MONTH_WEEK = 5;

    private static final int CROSS_DAY_CHOSE_TIME_WEEK_MONTH = 6;


    private TimeLineStores timeLineStores; //时间轴业务逻辑处理类

    private RecyclerView taskRecyclerView;//viewpager中间的那个recyclerview

    private View weekCalendarScrollView;

    private View monthCalendarScrollView;

    private View taskRootView;

    private View fragmentContainer;

    private View addCrossDayTaskView;

    private View addTodayTaskView;

    private View currentShowView; //viewpage当前显示的view

    private Handler handler;

    private int mode;

    public Calendar selectedDay;

    private ItemTouchHelper itemTouchHelper;

    private TaskRecyclerItemTouchCallBack callBack;

    private ArrayList<UnReadMessageBean> unReadMessageList;

    private UnReadMsgChangeListener listener;

    private TaskBean cutTaskBean;

    private boolean needLoadTask;

    private TaskBean mTaskBean = null;

    private int stateChangeType;

    private int taskRootViewHeight = -1;

    private ArrayList<View> rootViewList;

    private TaskBean clickTaskBean;

    private int clickPos;

    private RecyclerViewDisabler disabler;

    private ArrayList<UnReadMessageBean> unReadMsgIndexList;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (UnReadMsgChangeListener) activity;
        Dispatcher.getInstance().register(this);
        Log.i(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();

        handler = new Handler();
        rootViewList = new ArrayList<>();
        disabler = new RecyclerViewDisabler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_time_line_new, container, false);
        fragmentContainer = container;
        initView(v);
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Dispatcher.getInstance().unRegister(this);
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void onEventMainThread(UpdateUIAction action) {
        switch (action.getActionType()) {
            case TimeLineActions.TIME_LINE_INIT_TASK_LIST:
                Object todayTaskList = action.getActionData().get(0);
                Object crossDayTaskList = action.getActionData().get(1);
                if (taskRecyclerView.getAdapter() == null) {
                    RecyclerTaskAdapter taskAdapter = new RecyclerTaskAdapter(getActivity());
                    taskAdapter.setTodayTaskList((List<TaskBean>) todayTaskList);
                    taskAdapter.setCrossDayTaskList((List<TaskBean>) crossDayTaskList);
                    taskAdapter.setListener(this);
                    taskAdapter.setRecyclerView(taskRecyclerView);
                    taskRecyclerView.setAdapter(taskAdapter);
                    callBack.setItemTouchHelperAdapter(taskAdapter);
                } else {
                    RecyclerTaskAdapter taskAdapter = (RecyclerTaskAdapter) taskRecyclerView.getAdapter();
                    taskAdapter.notifyDataSetChanged();
                }
                if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.progressbar)
                    ((ViewSwitcher) currentShowView).showPrevious();
                if (timeLineStores.getUnReadMessageList().size() > 0) {
                    showTaskUnRead();
                }
                break;
            case TimeLineActions.TIME_LINE_GET_TASK_TIME_OUT:
                //((TaskRootView) taskRootView).showTimeOutView();
                break;
            case TimeLineActions.TIME_LINE_DELETE_TASK_SUCCESS:
                UItoolKit.showToastShort(getActivity(), "删除任务成功");
//                loadSelectedDayTaskList();
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).removeTaskItem(clickPos);
                clickPos = -1;
                clickTaskBean = null;
                break;
            case TimeLineActions.TIME_LINE_DELETE_TASK_FAILED:
                UItoolKit.showToastShort(getActivity(), "删除任务失败");
                break;
            case TimeLineActions.TIME_LINE_QUIT_TASK_SUCCESS:
                UItoolKit.showToastShort(getActivity(), "退出任务成功");
                loadSelectedDayTaskList();
                break;
            case TimeLineActions.TIME_LINE_QUIT_TASK_FAILED:
                UItoolKit.showToastShort(getActivity(), "退出任务失败");
                break;
            case TimeLineActions.TIME_LINE_RECIVE_UNREAD_MESSAGE:
                unReadMessageList = (ArrayList<UnReadMessageBean>) action.getActionData().get(0);
                showCalendarUnRead();
                if (taskRecyclerView != null && taskRecyclerView.getAdapter() != null)
                    showTaskUnRead();
                listener.onMsgCountChange(0, unReadMessageList.size());
                break;
            case TimeLineActions.TIME_LINE_RECIVE_GROUP_INFO_WINDOW:
                ChatWindowInfoBean windowInfoBean = (ChatWindowInfoBean) action.getActionData().get(0);
                Intent intent = new Intent(getActivity(), ChatActivityNew.class);
                intent.putExtra(ChatActivityNew.CHAT_WINDOW_INFO,
                        windowInfoBean);
                intent.putExtra(ChatActivityNew.TASK_BEAN, timeLineStores.getCurrentTaskBean());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, GET_TASK_CUSTOME_PROPERTY);
                break;
            case TimeLineActions.TIME_LINE_SET_UNREAD_MSG:
                String taskId = (String) action.getActionData().get(0);
                int count = (int) action.getActionData().get(1);
                if (taskId != null)
                    ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).setPosUnReadNum(taskId, count);
                showCalendarUnRead();
                listener.onMsgCountChange(0, unReadMessageList.size());
                break;
            case TimeLineActions.TIME_LINE_CHANGE_TASK_TIME_SUCCESS:
                cutTaskBean = null;
                loadSelectedDayTaskList();
                break;
            case TimeLineActions.TIME_LINE_SORT_TASK_NUM:
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).sortTask((JSONObject) action.getActionData().get(0), true);
                break;
        }
    }

    private void initData() {
        timeLineStores = TimeLineStores.getInstance();
        mode = MONTH_MODE;
        callBack = new TaskRecyclerItemTouchCallBack();
        itemTouchHelper = new ItemTouchHelper(callBack);
        selectedDay = Calendar.getInstance();
        selectedDay.set(Calendar.HOUR_OF_DAY, 0);
        selectedDay.set(Calendar.MINUTE, 0);
        selectedDay.set(Calendar.SECOND, 1);
        selectedDay.set(Calendar.MILLISECOND, 0);
        timeLineStores.setSelectedDay(selectedDay);
    }

    private void initView(View v) {
        initWeekCalendar(v);
        initTaskView(v);
        taskRootView.post(new Runnable() {
            @Override
            public void run() {
                ((TaskRootView) taskRootView).showWeekCalendar();
            }
        });
        ((MainActivity) getActivity()).setYearMonth(selectedDay);
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_CHAT_UNREAD_MESSAGE);
        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_INIT_TASK_LIST, selectedDay.getTimeInMillis());
        getUserUnReadMsg();
    }

    private void initWeekCalendar(final View v) {
        weekCalendarScrollView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_week_view, (ViewGroup) v, false);
        ((CalendarScrollViewNew) weekCalendarScrollView).init(selectedDay.getTimeInMillis());
        ((ViewGroup) v).addView(weekCalendarScrollView, 0);
        ViewTreeObserver vo = weekCalendarScrollView.getViewTreeObserver();
        vo.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                v.scrollBy(0, weekCalendarScrollView.getHeight());
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) taskRootView.getLayoutParams();
                lp.height = taskRootView.getHeight() + weekCalendarScrollView.getHeight();
                taskRootView.setLayoutParams(lp);
                weekCalendarScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        monthCalendarScrollView = v.findViewById(R.id.calendar_root_view);
        ((CalendarScrollViewNew) monthCalendarScrollView).init(selectedDay.getTimeInMillis());
        ((CalendarScrollViewNew) monthCalendarScrollView).setDateChangeListener(this);
        ((CalendarScrollViewNew) monthCalendarScrollView).setCalendarClickListener(this);
        ((CalendarScrollViewNew) weekCalendarScrollView).setDateChangeListener(this);
        ((CalendarScrollViewNew) weekCalendarScrollView).setCalendarClickListener(this);
    }

    private void initTaskView(View v) {
        initTaskViewPage(v);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        taskRecyclerView.setLayoutManager(layoutManager);
        taskRootView = v.findViewById(R.id.task_root_view);
        ((TaskRootView) taskRootView).setListener(this);
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);
        taskRecyclerView.setTag(itemTouchHelper);
        ((TaskRootView) taskRootView).setTaskRecyclerView(taskRecyclerView);
        setTaskRootViewMode();
    }

    ViewPager taskListViewPage;

    private void initTaskViewPage(View v) {
        taskListViewPage = (ViewPager) v.findViewById(R.id.task_list_view_pager);
        for (int i = 0; i < 5; i++) {
            ViewSwitcher taskListView = (ViewSwitcher) LayoutInflater.from(getActivity()).inflate(R.layout.task_list_switch_recyclerview, taskListViewPage, false);
            taskListView.showNext();
            rootViewList.add(taskListView);
        }
        currentShowView = rootViewList.get(2);
        taskRecyclerView = (RecyclerView) currentShowView.findViewById(R.id.task_recycler_view);
        TaskListViewPageAdapter adapter = new TaskListViewPageAdapter();
        adapter.setViewList(rootViewList);
        taskListViewPage.setAdapter(adapter);
        taskListViewPage.setCurrentItem((Integer.MAX_VALUE - 2) / 2);
        taskListViewPage.addOnPageChangeListener(new TaskListPageChangeListener());
    }


    private void setTaskRootViewMode() {
        if (mode == MONTH_MODE) {
            ((TaskRootView) taskRootView).setIsNeedIntercept(true);
        } else if (mode == WEEK_MODE) {
            ((TaskRootView) taskRootView).setIsNeedIntercept(false);
        }
    }


    private void getUserUnReadMsg() {
        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_GET_UNREAD_MESSAGE);
    }

    private void showCalendarUnRead() {
        Log.i(TAG, "showCalendarUnRead:  initUnReadDateList");
        initUnReadDateList();
        ((CalendarScrollViewNew) monthCalendarScrollView).showUnReadMessage();
        ((CalendarScrollViewNew) weekCalendarScrollView).showUnReadMessage();
    }


    public void showTaskUnRead() {
        ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).showUnReadMessage(unReadMessageList);
    }

    private void filkCalendar() {
        if (mode == MONTH_MODE) {
            flickView(monthCalendarScrollView);
        } else {
            flickView(weekCalendarScrollView);
        }
    }


    private void flickView(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        objectAnimator1.setDuration(200);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        objectAnimator2.setDuration(200);
        animatorSet.playSequentially(objectAnimator1, objectAnimator2);
        animatorSet.start();
    }


    private void taskTimeClick(TaskBean taskBean, int pos) {
        disableViewTouch();
        scrollMonthModeTask(pos);
        if (addCrossDayTaskView == null) {
            initAddCrossDayView(0);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addCrossDayTaskView.getLayoutParams();
            layoutParams.topMargin = ((TaskRootView) taskRootView).getShadowViewHeight();
            addCrossDayTaskView.setLayoutParams(layoutParams);
        }
        ((AddCrossDayRootView) addCrossDayTaskView).init(taskBean, selectedDay.getTimeInMillis(), true, pos);
        ((ViewGroup) taskRootView).addView(addCrossDayTaskView);
        stateChangeType = CROSS_DAY_CHOSE_TIME_WEEK_MONTH;
        ((MainActivity) getActivity()).setIsCrossViewShow(true);
        addCrossDayTaskView.setTag(true);
        filkCalendar();
    }

    private void showAddTaskDialog(TaskBean taskBean, int pos, boolean isEditTime) {
        disableViewTouch();
        Log.i(TAG, "showAddTaskDialog: add on item touch listener");
        if (taskBean.isTodayTask()) {
            showAddTodayTaskDialog(taskBean, pos, isEditTime);
        } else
            showAddCrossDayTaskDialog(taskBean, pos, isEditTime);
    }

    private void initAddCrossDayView(int topMargin) {
        addCrossDayTaskView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_cross_task_dialog, (ViewGroup) taskRootView, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(getActivity(), 40));
        layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
        ((AddCrossDayRootView) addCrossDayTaskView).setListener(this);
        ((AddCrossDayRootView) addCrossDayTaskView).setEditTaskResultListener(this);
        addCrossDayTaskView.setLayoutParams(layoutParams);
    }

    private void initAddTodayView(int topMargin) {
        addTodayTaskView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_today_task_dialog, (ViewGroup) taskRootView, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
        ((AddTodayRootView) addTodayTaskView).setListener(this);
        addTodayTaskView.setLayoutParams(layoutParams);
    }

    private void showAddTodayTaskDialog(TaskBean taskBean, int pos, boolean isEditTime) {
        if (addTodayTaskView != null && addTodayTaskView.getParent() != null) {
            return;
        }
        int topMargin = scrollTaskToPosition(pos);
        if (addTodayTaskView == null) {
            initAddTodayView(topMargin);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addTodayTaskView.getLayoutParams();
            layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
            addTodayTaskView.setLayoutParams(layoutParams);
        }
        ((AddTodayRootView) addTodayTaskView).init(taskBean, selectedDay.getTimeInMillis(), isEditTime, pos);
        ((AddTodayRootView) addTodayTaskView).setTimeLineRootView((View) fragmentContainer.getParent());
        ((ViewGroup) taskRootView).addView(addTodayTaskView);
        ((MainActivity) getActivity()).setIsAddOrEditTask(true);
        addTodayTaskView.setTag(true);
    }

    private void showAddCrossDayTaskDialog(TaskBean taskBean, int pos, boolean isEditTime) {
        if (addCrossDayTaskView != null && addCrossDayTaskView.getParent() != null) {
            return;
        }
        int topMargin = scrollTaskToPosition(pos);
        if (addCrossDayTaskView == null) {
            initAddCrossDayView(topMargin);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addCrossDayTaskView.getLayoutParams();
            layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
            addCrossDayTaskView.setLayoutParams(layoutParams);
        }
        ((AddCrossDayRootView) addCrossDayTaskView).init(taskBean, selectedDay.getTimeInMillis(), isEditTime, pos);
        ((ViewGroup) taskRootView).addView(addCrossDayTaskView);
        ((MainActivity) getActivity()).setIsCrossViewShow(true);
        addCrossDayTaskView.setTag(true);
    }

    private int scrollTaskToPosition(int pos) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) taskRecyclerView.getLayoutManager();
        int totalHeight = ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).getTotalHeight();
        int needScrollHeight = ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).getNeedScrollDis(pos);
        int recyclerViewHeight = layoutManager.getHeight();
        int topMargin = ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).getTopMargin();

        if (needScrollHeight <= 0) {
            layoutManager.scrollToPositionWithOffset(pos, topMargin);
            return topMargin/* + DeviceUtil.dpTopx(getActivity(), 2)*/;
        }
        ViewGroup.LayoutParams layoutParams = taskRecyclerView.getLayoutParams();
        layoutParams.height = totalHeight > recyclerViewHeight ? recyclerViewHeight - needScrollHeight : totalHeight - needScrollHeight;
        taskRecyclerView.setLayoutParams(layoutParams);
        if (needScrollHeight > 0) {
            layoutManager.scrollToPositionWithOffset(pos, topMargin);
        }
        return topMargin/*+ DeviceUtil.dpTopx(getActivity(), 2)*/;
    }

    private void scrollMonthModeTask(int clickPos) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) taskRecyclerView.getLayoutManager();
        int totalHeight = ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).getTotalHeight();
        int needScrollHeight = ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).getMonthModeScrollDis(clickPos);
        int recyclerViewHeight = taskRootView.getLayoutParams().height - ((TaskRootView) taskRootView).getShadowViewHeight();
        int canScrollDis = totalHeight - recyclerViewHeight;
        if (needScrollHeight >= canScrollDis) {
            int dx = needScrollHeight - canScrollDis;
            ViewGroup.LayoutParams layoutParams = taskRecyclerView.getLayoutParams();
            layoutParams.height = recyclerViewHeight - dx;
            taskRecyclerView.setLayoutParams(layoutParams);
        }
        layoutManager.scrollToPositionWithOffset(clickPos, 0);
    }

    private void resetTaskView() {
        int recyclerViewHeight = taskRootViewHeight - ((TaskRootView) taskRootView).getShadowViewHeight();
        ViewGroup.LayoutParams layoutParams = taskRecyclerView.getLayoutParams();
        int recyclerCurrentHeight = taskRecyclerView.getHeight();
        if (recyclerCurrentHeight < recyclerViewHeight) {
            layoutParams.height = recyclerViewHeight;
            taskRecyclerView.setLayoutParams(layoutParams);
            taskRecyclerView.scrollToPosition(clickPos);
        }
    }

    private void setCalendarSelectedDay(Calendar calendar) {
        ((MainActivity) getActivity()).setYearMonth(selectedDay);
        if (mode == MONTH_MODE) {
            ((CalendarScrollViewNew) weekCalendarScrollView).setWeekCalendarWhenMonthClick(calendar);
        } else {
            ((CalendarScrollViewNew) monthCalendarScrollView).setMonthCalendarWhenWeekClick(calendar);
        }
    }

    private void setSelectedDate(Calendar calendar) {
        selectedDay.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        timeLineStores.setSelectedDay(selectedDay);
    }


    private void loadSelectedDayTaskList() {
        Log.e(TAG, "loadSelectedDayTaskList: " + DateUtils.getCalendarAllText(selectedDay));
        if (timeLineStores.isCanGetMailList()) {
            handler.removeCallbacks(loadTaskListRunnable);
            handler.postDelayed(loadTaskListRunnable, 500);
        } else {
            UItoolKit.showToastShort(getActivity(), "未连接到服务器，不能获取时间轴任务列表");
        }
    }


    /**
     * 设置日历周历到某天并且去加载这一天的任务
     */
    public void setCalendarToDate(Calendar calendar) {
        setSelectedDate(calendar);
        ((MainActivity) getActivity()).setYearMonth(selectedDay);
        if (mode == MONTH_MODE) {
            ((CalendarScrollViewNew) monthCalendarScrollView).setDateToSomeDay(calendar);
            ((CalendarScrollViewNew) weekCalendarScrollView).setDateToSomeDay(calendar);
        } else {
            ((CalendarScrollViewNew) weekCalendarScrollView).setDateToSomeDay(calendar);
            ((CalendarScrollViewNew) monthCalendarScrollView).setDateToSomeDay(calendar);
        }
        loadSelectedDayTaskList();
    }

    /**
     * 加载某一天的任务，并且把日历设置成这一天的位置,
     * 此时，所传的天应该是日历和周历中显示的某一天
     */
    private void setDateAndLoadTask(Calendar calendar) {
        if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.task_recycler_view) {
            ((ViewSwitcher) currentShowView).showNext();
        }
        setSelectedDate(calendar);
        setCalendarSelectedDay(calendar);
        loadSelectedDayTaskList();
    }

    /**
     * 添加或编辑跨天任务时，从周历切换至月历后改变跨天view的位置
     */
    private void crossWeekToMonth() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addCrossDayTaskView.getLayoutParams();
        layoutParams.topMargin = ((TaskRootView) taskRootView).getShadowViewHeight();
        scrollMonthModeTask(clickPos);
        addCrossDayTaskView.setLayoutParams(layoutParams);
    }

    /**
     * 隐藏任务弹出菜单
     */
    private void hideToggledView() {
        RecyclerTaskAdapter adapter = (RecyclerTaskAdapter) taskRecyclerView.getAdapter();
        if (adapter != null)
            adapter.hideToggleMenu();
    }

    /**
     * 隐藏添加跨天任务view
     */
    public void hideCrossDayView() {
        enableViewTouch();
        ((AddCrossDayRootView) addCrossDayTaskView).dismiss();
        resetTaskView();
        ((MainActivity) getActivity()).setIsCrossViewShow(false);
    }

    /**
     * 隐藏添加当天任务view
     */
    public void hideAddTodayView() {
        enableViewTouch();
        ((AddTodayRootView) addTodayTaskView).dismiss();
        resetTaskView();
        ((MainActivity) getActivity()).setIsAddOrEditTask(false);
    }

    /**
     * 隐藏当天或跨天的增加或者编辑的view
     */
    private boolean hideAddTask() {
        enableViewTouch();
        if (addCrossDayTaskView != null && (boolean) addCrossDayTaskView.getTag()) {
            ((AddCrossDayRootView) addCrossDayTaskView).dismiss();
            ((MainActivity) getActivity()).setIsCrossViewShow(false);
            return true;
        }

        if (addTodayTaskView != null && (boolean) addTodayTaskView.getTag()) {
            ((AddTodayRootView) addTodayTaskView).dismiss();
            ((MainActivity) getActivity()).setIsAddOrEditTask(false);
            return true;
        }
        return false;
    }

    private void enableViewTouch() {
        ((NoScrollViewPager) taskListViewPage).setNotScrol(false);
        ((TaskRootView) taskRootView).setDisableTouch(false);
        taskRecyclerView.removeOnItemTouchListener(disabler);
    }

    private void disableViewTouch() {
        ((NoScrollViewPager) taskListViewPage).setNotScrol(true);
        ((TaskRootView) taskRootView).setDisableTouch(true);
        taskRecyclerView.addOnItemTouchListener(disabler);
    }

    /**
     * 修改任务地点和公私时的返回值
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_TASK_CUSTOME_PROPERTY && resultCode == Activity.RESULT_OK) {
            if (mTaskBean != null) {
                mTaskBean.taskPlace = data.getStringExtra("taskPlace");
                mTaskBean.setPrivate = data.getIntExtra("taskPrivate", 0);
            }
            mTaskBean = null;
        }
    }

    /**
     * 周历和月历切换时候的回调
     */
    @Override
    public void onStateChanged(boolean isMonthState) {
        mode = isMonthState ? MONTH_MODE : WEEK_MODE;
        int temp = taskRootView.getLayoutParams().height;

        if (temp >= taskRootViewHeight)
            taskRootViewHeight = temp;
        switch (stateChangeType) {
            case ADD_TASK_MONTH_WEEK:
                showAddTaskDialog(clickTaskBean, clickPos, false);
                break;
            case EDIT_CROSS_DAY_TIME_WEEK_MONTH:
                taskTimeClick(clickTaskBean, clickPos);
                break;
            case EDIT_TODAY_TASK_TIME_MONTH_WEEK:
                showAddTaskDialog(clickTaskBean, clickPos, true);
                break;
            case CROSS_DAY_CHOSE_TIME_WEEK_MONTH:
                filkCalendar();
                crossWeekToMonth();
                break;
            case EDIT_TASK_CONTENT_MONTH_WEEK:
                int topMargin = scrollTaskToPosition(clickPos);
                RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) addCrossDayTaskView.getLayoutParams();
                layoutParams2.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
                addCrossDayTaskView.setLayoutParams(layoutParams2);
                break;
        }
    }


    /**
     * 新增或编辑跨天任务时点击开始时间的回调
     */
    @Override
    public void onTaskStartTimeClick() {
        if (mode == WEEK_MODE) {
            resetTaskView();
            ((TaskRootView) taskRootView).showMonthCalendar();
            stateChangeType = CROSS_DAY_CHOSE_TIME_WEEK_MONTH;
        } else {
            filkCalendar();
        }
    }

    /**
     * 新增或编辑跨天任务时点击结束时间的回调
     */
    @Override
    public void onTaskEndTimeClick() {
        if (mode == WEEK_MODE) {
            resetTaskView();
            ((TaskRootView) taskRootView).showMonthCalendar();
            stateChangeType = CROSS_DAY_CHOSE_TIME_WEEK_MONTH;
        } else {
            filkCalendar();
        }
    }

    /**
     * 取消添加或编辑跨天任务的回调
     */
    @Override
    public void onCancelAdd() {
        ((MainActivity) getActivity()).setIsCrossViewShow(false);

        enableViewTouch();

        stateChangeType = -1;
        resetTaskView();
        //loadSelectedDayTaskList();
    }

    /**
     * 添加或编辑跨天任务输入框获取到焦点时的回调
     */
    @Override
    public void onInputContentClick() {
        if (mode == MONTH_MODE) {
            resetTaskView();
            ((TaskRootView) taskRootView).showWeekCalendar();
            stateChangeType = EDIT_TASK_CONTENT_MONTH_WEEK;
        }
    }

    /**
     * 编辑当天或跨天任务结果回调
     */
    @Override
    public void onEditTaskResult(TaskBean taskBean, int pos) {
        ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).editTask(taskBean);
    }

    @Override
    public void onDismiss() {
        resetTaskView();
        stateChangeType = -1;
        enableViewTouch();
        ((MainActivity) getActivity()).setIsAddOrEditTask(false);
    }


    /**
     * 添加当天或跨天任务结果回调
     */
    @Override
    public void onAddTaskResult(boolean isSuccess, TaskBean taskBean, int pos) {
        resetTaskView();
        stateChangeType = -1;
        enableViewTouch();
        if (isSuccess) {
            if (taskBean.isTodayTask() || (taskBean.startTime == selectedDay.getTimeInMillis()))
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).addTask(taskBean);
            else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(taskBean.startTime);
                setCalendarToDate(calendar);
            }
        }
    }

    /**
     * 任务标题点击时的回调
     */
    @Override
    public void onTaskContentClick(TaskBean taskBean, int pos, boolean isOnlyShowMenu) {
        if (hideAddTask()) return;

        if (!taskBean.taskCreator.equals(PreferencesHelper.getInstance().getCurrentUser().id) && !isOnlyShowMenu) {
            UItoolKit.showToastShort(getActivity(), "你不是该任务创建者，不能编辑该任务");
            return;
        }
        if (!isOnlyShowMenu)
            onEmptyViewClick(taskBean, pos);
    }

    /**
     * 任务标题被长按时的回调
     */
    @Override
    public void onItemLongClick(View view, int position, TaskBean taskBean) {
        if (hideAddTask()) return;

        RecyclerTaskAdapter.ViewHolder viewHolder;
        try {
            viewHolder = (RecyclerTaskAdapter.ViewHolder) taskRecyclerView.getChildViewHolder((View) view.getParent().getParent());
        } catch (IllegalArgumentException e) {
            viewHolder = (RecyclerTaskAdapter.ViewHolder) taskRecyclerView.getChildViewHolder((View) view.getParent().getParent().getParent());
        }
        if (taskBean.isCurrentUserTask() && DateUtils.isToday(selectedDay) && taskBean.isTodayTask()) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }

    /**
     * 任务快捷菜单被点击时的回调
     */
    @Override
    public void onToggleMenuClick(TaskBean taskBean, int type, int pos) {
        switch (type) {
            case 1:
                cutTaskBean = taskBean;
                filkCalendar();
                break;
            case 2:
                Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_GET_WINDOW_INFO, taskBean);
                mTaskBean = taskBean;
                break;
            case 5:
                clickTaskBean = taskBean;
                clickPos = pos;
                DialogFragment dialogFragment = DeleteDialogFragment.
                        getInstance(taskBean);
                dialogFragment.show(getFragmentManager(), null);
                break;
        }
    }

    /**
     * 空行任务被点击时的的回调
     */
    @Override
    public void onEmptyViewClick(TaskBean taskBean, int pos) {

        if (hideAddTask()) return;

        clickTaskBean = taskBean;
        clickPos = pos;
        if (mode == WEEK_MODE) {
            showAddTaskDialog(taskBean, pos, false);
        } else {
            stateChangeType = ADD_TASK_MONTH_WEEK;
            ((TaskRootView) taskRootView).showWeekCalendar();
        }
    }

    /**
     * 任务时间被点击时的回调
     */
    @Override
    public void onTaskTimeClick(TaskBean taskBean, int pos) {
        if (hideAddTask()) return;

        if (!taskBean.isEmptyTask() && !taskBean.taskCreator.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
            UItoolKit.showToastShort(getActivity(), "你不是该任务创建者，不能编辑该任务");
            return;
        }

        clickTaskBean = taskBean;
        clickPos = pos;

        if (taskBean.isTodayTask()) {
            if (mode == WEEK_MODE) {
                showAddTaskDialog(taskBean, pos, true);
            } else {
                stateChangeType = EDIT_TODAY_TASK_TIME_MONTH_WEEK;
                ((TaskRootView) taskRootView).showWeekCalendar();
            }
        } else {
            if (mode == WEEK_MODE) {
                stateChangeType = EDIT_CROSS_DAY_TIME_WEEK_MONTH;
                ((TaskRootView) taskRootView).showMonthCalendar();
            } else {
                taskTimeClick(taskBean, pos);
            }
        }
    }

    /**
     * 取消移动任务的回调
     */
    @Override
    public void onCancelCutBean() {
        cutTaskBean = null;
        if (needLoadTask) {
            loadSelectedDayTaskList();
            needLoadTask = false;
        }
    }

    /**
     * 滑动日历时间改变时的回调
     */
    @Override
    public void onDateChanged(Calendar calendar) {
        hideToggledView();

        if (cutTaskBean != null) {
            needLoadTask = true;
            setSelectedDate(calendar);
            setCalendarSelectedDay(calendar);
            return;
        }

        if (stateChangeType == CROSS_DAY_CHOSE_TIME_WEEK_MONTH) {
            setSelectedDate(calendar);
            setCalendarSelectedDay(calendar);
            ((AddCrossDayRootView) addCrossDayTaskView).setTime(selectedDay.getTimeInMillis());
            return;
        }
        Log.i(TAG, "onDateChanged: before setDateAndLoadTask" + System.currentTimeMillis());
        setDateAndLoadTask(calendar);
        Log.i(TAG, "onDateChanged: after setDateAndLoadTask" + System.currentTimeMillis());
    }

    /**
     * 日历某天被点击时的回调
     */
    @Override
    public void onOneDayClick(CalendarDateBean mBean) {

        hideToggledView();

        if (cutTaskBean != null) {
            needLoadTask = true;
            Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_CHANGE_TASK_TIME, cutTaskBean, mBean);
            Calendar calendar = Calendar.getInstance();
            calendar.set(mBean.getYear(), mBean.getMonth(), mBean.getDay());
            setSelectedDate(calendar);
            setCalendarSelectedDay(calendar);
            return;
        }

        if (stateChangeType == CROSS_DAY_CHOSE_TIME_WEEK_MONTH || stateChangeType == EDIT_TASK_CONTENT_MONTH_WEEK) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(mBean.getYear(), mBean.getMonth(), mBean.getDay());
            setSelectedDate(calendar);
            setCalendarSelectedDay(calendar);
            ((AddCrossDayRootView) addCrossDayTaskView).setTime(selectedDay.getTimeInMillis());
            return;
        }

        hideAddTask();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(mBean.getYear(), mBean.getMonth(), mBean.getDay());

        setDateAndLoadTask(calendar);
    }


    /**
     * 加载任务列表的runnable
     */
    private Runnable loadTaskListRunnable = new Runnable() {
        @Override
        public void run() {
            Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_REQUEST_TASK_LIST, selectedDay.getTimeInMillis());
        }
    };

    private class TaskListPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private int prePos = (Integer.MAX_VALUE - 2) / 2;

        @Override
        public void onPageSelected(int position) {
            hideToggledView();
            currentShowView = rootViewList.get(position % 5);
            if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.task_recycler_view) {
                taskRecyclerView = (RecyclerView) ((ViewSwitcher) currentShowView).getCurrentView();
                ((ViewSwitcher) currentShowView).showPrevious();
            } else {
                taskRecyclerView = (RecyclerView) currentShowView.findViewById(R.id.task_recycler_view);
            }
            if (taskRecyclerView.getLayoutManager() == null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                taskRecyclerView.setLayoutManager(layoutManager);
                callBack = new TaskRecyclerItemTouchCallBack();
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callBack);
                taskRecyclerView.setTag(itemTouchHelper);
                itemTouchHelper.attachToRecyclerView(taskRecyclerView);
            }
            itemTouchHelper = (ItemTouchHelper) taskRecyclerView.getTag();
            ((TaskRootView) taskRootView).setTaskRecyclerView(taskRecyclerView);
            if (prePos > position) { //向左滑
                selectedDay.add(Calendar.DATE, -1);
                ((CalendarScrollViewNew) weekCalendarScrollView).moveToPrevious();
                ((CalendarScrollViewNew) monthCalendarScrollView).moveToPrevious();
            } else {//向右滑
                selectedDay.add(Calendar.DATE, 1);
                ((CalendarScrollViewNew) weekCalendarScrollView).moveToNext();
                ((CalendarScrollViewNew) monthCalendarScrollView).moveToNext();
            }
            ((MainActivity) getActivity()).setYearMonth(selectedDay);
            loadSelectedDayTaskList();
            prePos = position;
        }
    }

    private class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//            hideAddTask();
            Log.i(TAG, "onTouchEvent: action up cancel click");
            if (e.getAction() == MotionEvent.ACTION_UP) {
                cancelClick();
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

    }

    private void cancelClick() {
        if (addCrossDayTaskView != null && (boolean) addCrossDayTaskView.getTag()) {
            ((AddCrossDayRootView) addCrossDayTaskView).cancelClick();
        }

        if (addTodayTaskView != null && (boolean) addTodayTaskView.getTag()) {
            ((AddTodayRootView) addTodayTaskView).cancelClick();
        }
    }


    public void setYear(int year) {
        if (year == selectedDay.get(Calendar.YEAR))
            return;
        selectedDay.set(Calendar.YEAR, year);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDay.getTimeInMillis());
        setCalendarToDate(calendar);
    }

    public void setMonth(int month) {
        if (month == selectedDay.get(Calendar.MONTH))
            return;
        selectedDay.set(Calendar.MONTH, month);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDay.getTimeInMillis());
        setCalendarToDate(calendar);
    }

    UnReadMessageBean preShowDayBean;

    public void showNetUnReadDay() {
        if (unReadMsgIndexList == null || unReadMsgIndexList.size() == 0)
            return;
        if (preShowDayBean == null) {
            int index = unReadMsgIndexList.size() - 1;
            UnReadMessageBean unReadMessageBean = unReadMsgIndexList.get(index);
            while (index > 0 && unReadMessageBean.isSelectDay(selectedDay)) {
                index--;
                unReadMessageBean = unReadMsgIndexList.get(index);
            }
            if (index >= 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(unReadMessageBean.createYear, unReadMessageBean.createMonth, unReadMessageBean.createDay);
                preShowDayBean = unReadMessageBean;
                if (!DateUtils.isSameDayOfMillis(calendar.getTimeInMillis(), selectedDay.getTimeInMillis()))
                    setCalendarToDate(calendar);
            }
        } else {
            int preindex = unReadMsgIndexList.indexOf(preShowDayBean);
            if (preindex == -1 || preindex == 0) {
                int index = unReadMsgIndexList.size() - 1;
                UnReadMessageBean unReadMessageBean = unReadMsgIndexList.get(index);
                while (index > 0 && unReadMessageBean.isSelectDay(selectedDay)) {
                    index--;
                    unReadMessageBean = unReadMsgIndexList.get(index);
                }
                if (index >= 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.clear();
                    calendar.set(unReadMessageBean.createYear, unReadMessageBean.createMonth, unReadMessageBean.createDay);
                    preShowDayBean = unReadMessageBean;
                    if (!DateUtils.isSameDayOfMillis(calendar.getTimeInMillis(), selectedDay.getTimeInMillis()))
                        setCalendarToDate(calendar);
                }
            } else {
                UnReadMessageBean unReadMessageBean = unReadMsgIndexList.get(preindex - 1);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(unReadMessageBean.createYear, unReadMessageBean.createMonth, unReadMessageBean.createDay);
                preShowDayBean = unReadMessageBean;
                if (!DateUtils.isSameDayOfMillis(calendar.getTimeInMillis(), selectedDay.getTimeInMillis()))
                    setCalendarToDate(calendar);
            }
        }
    }

    private void initUnReadDateList() {
        Collections.sort(unReadMessageList);
        if (unReadMsgIndexList == null)
            unReadMsgIndexList = new ArrayList<>();
        unReadMsgIndexList.clear();
        long currentDayTIme = 0;
        for (UnReadMessageBean unReadMessageBean : unReadMessageList) {
            if (!DateUtils.isSameDayOfMillis(currentDayTIme, unReadMessageBean.taskCreateTime)) {
                currentDayTIme = unReadMessageBean.taskCreateTime;
                unReadMsgIndexList.add(unReadMessageBean);
            }
        }
    }
}
