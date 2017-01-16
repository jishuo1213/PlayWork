package com.inspur.playwork.view.timeline;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.bumptech.glide.manager.LifecycleListener;
import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.timeline.CalendarDateBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by Fan on 2016/4/19.
 */
public class TimeLineFragmentNew2 extends Fragment implements RecyclerTaskAdapter.TaskItemEventListener,
        CalendarScrollViewNew.OnCalendarChangeListener, RecycleCalendarAdapter.DateClickListener, TaskRootView.TaskRootViewEventListener,
        AddEditTaskResultListener, AddCrossDayRootView.AddCrossDayEventListener, TimeLineViewOperation {

    private static final String TAG = "TimeLineFragmentNew2";

    private static final String SAVE_CURRENT_MODE = "save_mode";
    private static final String SAVE_TIME = "save_time";

    private static final int SHOW_TASK_LIST_EVENT_MASK = 0x00000001;

    private static final int SHOW_UNREAD_MSG_EVENT_MASK = 0x00000002;

    private int viewEventFlag = 0x00000000;

    private static final int GET_TASK_CUSTOME_PROPERTY = 101;

    private static final int MONTH_MODE = 1;

    private static final int WEEK_MODE = 2;

    private static final int EDIT_TASK_CONTENT_MONTH_WEEK = 1;

    private static final int EDIT_CROSS_DAY_TIME_WEEK_MONTH = 2;

    private static final int ADD_TASK_MONTH_WEEK = 4;

    private static final int EDIT_TODAY_TASK_TIME_MONTH_WEEK = 5;

    private static final int CROSS_DAY_CHOSE_TIME_WEEK_MONTH = 6;


//    private TimeLineStoresNew timeLineStores; //时间轴业务逻辑处理类

    private RecyclerView taskRecyclerView;//viewpager中间的那个recyclerview

    private View weekCalendarScrollView;

    private View monthCalendarScrollView;

    private View taskRootView;

    private View fragmentContainer;

    private View addCrossDayTaskView;

    private View addTodayTaskView;

    private View currentShowView; //viewpage当前显示的view

    private ViewPager taskListViewPage;

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

    private boolean isFragmentCreate;

    private boolean isVisable;

    private TaskListPageChangeListener taskListPageChangeListener;

    private boolean isNeedShowViews;

    private LifecycleListener lifecycleListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (UnReadMsgChangeListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ------------------------");
        initData(savedInstanceState);
        isFragmentCreate = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View v = inflater.inflate(R.layout.layout_time_line_new, container, false);
        fragmentContainer = container;
        initView(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: " + (selectedDay == null));
        ((MainActivity) getActivity()).setYearMonth(selectedDay);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lifecycleListener != null) {
            lifecycleListener.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ============" + viewEventFlag);
        isVisable = true;
        loadUserData();
        viewEventFlag = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        isVisable = false;
        if (lifecycleListener != null) {
            lifecycleListener.onStop();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CURRENT_MODE, mode);
        outState.putLong(SAVE_TIME, selectedDay.getTimeInMillis());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        ((CalendarScrollViewNew) monthCalendarScrollView).clearListener();
        ((CalendarScrollViewNew) weekCalendarScrollView).clearListener();
        ((TaskRootView) taskRootView).clearListener();
        RecyclerTaskAdapter adapter = (RecyclerTaskAdapter) taskRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearListener();
        }
        handler.removeCallbacks(loadTaskListRunnable);
        loadTaskListRunnable = null;

        taskRecyclerView.removeOnItemTouchListener(disabler);
        disabler = null;


        taskListViewPage.removeOnPageChangeListener(taskListPageChangeListener);
        taskListPageChangeListener = null;

        listener = null;

        if (callBack != null) {
            callBack.clearListener();
            callBack = null;
        }

        TimeLineStoresNew.getInstance().setViewOperationWeakReference(new WeakReference<TimeLineViewOperation>(null));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lifecycleListener != null) {
            lifecycleListener.onDestroy();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setNeedShowViews(boolean needShowViews) {
        isNeedShowViews = needShowViews;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged:==========" + hidden);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    private void initData(Bundle savedInstanceState) {
//        TimeLineStoresNew. = TimeLineStoresNew.getInstance();
        selectedDay = Calendar.getInstance();

        Log.i(TAG, "initData: " + (savedInstanceState == null));
        if (savedInstanceState == null) {
            selectedDay.set(Calendar.HOUR_OF_DAY, 0);
            selectedDay.set(Calendar.MINUTE, 0);
            selectedDay.set(Calendar.SECOND, 1);
            selectedDay.set(Calendar.MILLISECOND, 0);
            mode = WEEK_MODE;
        } else {
            selectedDay.setTimeInMillis(savedInstanceState.getLong(SAVE_TIME));
            mode = savedInstanceState.getInt(SAVE_CURRENT_MODE);
        }

        Log.i(TAG, "initData: " + mode);

        callBack = new TaskRecyclerItemTouchCallBack();
        itemTouchHelper = new ItemTouchHelper(callBack);
        TimeLineStoresNew.getInstance().setSelectedDay(selectedDay);
        TimeLineStoresNew.getInstance().setViewOperationWeakReference(new WeakReference<TimeLineViewOperation>(this));

        handler = new Handler();
        rootViewList = new ArrayList<>();
        disabler = new RecyclerViewDisabler();
    }

    private void initView(View v) {
        initWeekCalendar(v);
        initTaskView(v);
        if (mode == WEEK_MODE)
            taskRootView.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "show weekCalendar: ================");
                    ((TaskRootView) taskRootView).showWeekCalendar();
                }
            });
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
                Log.i(TAG, "onGlobalLayout: ============");
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
        ((TaskRootView) taskRootView).setIsNeedIntercept(true);
    }

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
        taskListViewPage.setAdapter(adapter);
        taskListViewPage.setCurrentItem((Integer.MAX_VALUE - 2) / 2);
        adapter.setViewList(rootViewList);
        taskListPageChangeListener = new TaskListPageChangeListener();
        taskListViewPage.addOnPageChangeListener(taskListPageChangeListener);

        taskListViewPage.setSaveEnabled(false);
    }


    private void loadUserData() {
        Log.i(TAG, "loadUserData: loadNetData=============================" + isFragmentCreate);
        if (isFragmentCreate) {
            TimeLineStoresNew.getInstance().getLocalUnReadMsg();
            TimeLineStoresNew.getInstance().getMailList(selectedDay.getTimeInMillis());
//            timeLineStores.getNetUnReadMessage();
            isFragmentCreate = false;
        } else {
            ((CalendarScrollViewNew)monthCalendarScrollView).setCurrentDay();
            ((CalendarScrollViewNew)weekCalendarScrollView).setCurrentDay();
            if (viewEventFlag >= 0) {
                if ((viewEventFlag & SHOW_TASK_LIST_EVENT_MASK) > 0) {
                    showTaskList(TimeLineStoresNew.getInstance().getTodayTaskList(), TimeLineStoresNew.getInstance().getCrossDayTaskList());
                } else if ((viewEventFlag & SHOW_UNREAD_MSG_EVENT_MASK) > 0) {
                    unReadMessageList = TimeLineStoresNew.getInstance().getUnReadMessageList();
                    showUnReadMsg();
                }
            }
        }
    }

    @Override
    public void showTaskList(final ArrayList<TaskBean> todayTaskList, final ArrayList<TaskBean> crossDayTaskList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable) {
                    viewEventFlag |= SHOW_TASK_LIST_EVENT_MASK;
                    return;
                }
                if (taskRecyclerView.getAdapter() == null) {
                    RecyclerTaskAdapter taskAdapter = new RecyclerTaskAdapter(getActivity());
                    taskAdapter.setTodayTaskList(todayTaskList);
                    taskAdapter.setCrossDayTaskList(crossDayTaskList);
                    taskAdapter.setListener(TimeLineFragmentNew2.this);
                    taskAdapter.setRecyclerView(taskRecyclerView);
                    taskRecyclerView.setAdapter(taskAdapter);
                    callBack.setItemTouchHelperAdapter(taskAdapter);
                } else {
                    RecyclerTaskAdapter taskAdapter = (RecyclerTaskAdapter) taskRecyclerView.getAdapter();
                    taskAdapter.notifyDataSetChanged();
                }
                if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.progressbar)
                    ((ViewSwitcher) currentShowView).showPrevious();
                if (TimeLineStoresNew.getInstance().isHaveUnReadMsg()) {
                    showTaskUnRead();
                }
            }
        });
    }

    @Override
    public void showUnReadMsg() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unReadMessageList = TimeLineStoresNew.getInstance().getUnReadMessageList();
                Log.i(TAG, "run: " + unReadMessageList.size());
                if (!isVisable) {
                    viewEventFlag |= SHOW_UNREAD_MSG_EVENT_MASK;
                    Log.i(TAG, "showUnReadMsg: " + viewEventFlag);
                    return;
                }
                showCalendarUnRead();
                if (taskRecyclerView != null && taskRecyclerView.getAdapter() != null)
                    showTaskUnRead();

                listener.onMsgCountChange(0, unReadMessageList.size());
            }
        });
    }

    @Override
    public void quitTaskResult(final boolean result, final String taskId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable) {
                    return;
                }
                if (result) {
                    UItoolKit.showToastShort(getActivity(), "退出任务成功");
                    ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).removeTaskItem(taskId);
                    clickPos = -1;
                    clickTaskBean = null;
                } else {
                    UItoolKit.showToastShort(getActivity(), "退出任务失败");
                }
            }
        });
    }

    @Override
    public void setTimeLineMsgCount(final String taskId, final int count) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable) {
                    viewEventFlag |= SHOW_UNREAD_MSG_EVENT_MASK;
                    return;
                }
                if (taskId != null)
                    ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).setPosUnReadNum(taskId, count);
                unReadMessageList = TimeLineStoresNew.getInstance().getUnReadMessageList();
                showCalendarUnRead();

                listener.onMsgCountChange(0, unReadMessageList.size());
            }
        });
    }

    @Override
    public void getChatWindowInfoSuccess(final boolean success, final ChatWindowInfoBean windowInfoBean, final TaskBean taskBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable)
                    return;
                if (success) {
                    Intent intent = new Intent(getActivity(), ChatActivityNew.class);
                    intent.putExtra(ChatActivityNew.CHAT_WINDOW_INFO,
                            windowInfoBean);
                    intent.putExtra(ChatActivityNew.TASK_BEAN, taskBean);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, GET_TASK_CUSTOME_PROPERTY);
                } else {
                    UItoolKit.showToastShort(getActivity(), "获取聊天信息失败");
                }
            }
        });
    }

    @Override
    public void changeTaskTimeResult(final boolean result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable)
                    return;
                if (result) {
                    cutTaskBean = null;
                    loadSelectedDayTaskList();
                }
            }
        });
    }

    @Override
    public void sortTaskList(final JSONObject jsonObject) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isVisable) {
                    viewEventFlag |= SHOW_TASK_LIST_EVENT_MASK;
                }
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).sortTask(jsonObject, isVisable);
            }
        });
    }

    @Override
    public void sendMailResult(final boolean result, final boolean isCrossDay, final String taskId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCrossDay) {
                    ((AddCrossDayRootView) addCrossDayTaskView).addEditTaskResult(result, taskId);
                } else {
                    ((AddTodayRootView) addTodayTaskView).addEditTaskResult(result, taskId);
                }
            }
        });
    }

    @Override
    public void updateTaskSubject(final String taskId, final String subject) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).reNameTask(taskId, subject);
            }
        });
    }

    @Override
    public void deleteTask(final String taskId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).removeTaskItem(taskId);
            }
        });
    }

    /**
     * 以上是进入到时间轴界面时要做的事
     */
    //=============================================================================
    @Override
    public void onOneDayClick(CalendarDateBean mBean) {

        hideToggledView();

        if (cutTaskBean != null) {
            needLoadTask = true;
//            Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_CHANGE_TASK_TIME, cutTaskBean, mBean);
            TimeLineStoresNew.getInstance().changeTaskTime(cutTaskBean, mBean);
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

    @Override
    public void onItemLongClick(View view, int position, TaskBean taskBean) {
        if (hideAddTask()) return;

        RecyclerTaskAdapter.ViewHolder viewHolder;
        try {
            viewHolder = (RecyclerTaskAdapter.ViewHolder) taskRecyclerView.getChildViewHolder((View) view.getParent().getParent());
        } catch (IllegalArgumentException e) {
            viewHolder = (RecyclerTaskAdapter.ViewHolder) taskRecyclerView.getChildViewHolder((View) view.getParent().getParent().getParent());
        }
        if (taskBean.isTodayTask()) {
            if (taskBean.isCurrentUserTask() && DateUtils.isToday(selectedDay)) {
                itemTouchHelper.startDrag(viewHolder);
            }
        }
//        } else {
//            itemTouchHelper.startDrag(viewHolder);
//        }
    }

    @Override
    public void onToggleMenuClick(TaskBean taskBean, int type, int pos) {
        switch (type) {
            case 1:
                cutTaskBean = taskBean;
                filkCalendar();
                break;
            case 2:
//                Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_GET_WINDOW_INFO, taskBean);
                TimeLineStoresNew.getInstance().getWindownInfoByTaskId(taskBean);
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

    @Override
    public void onCancelCutBean() {
        cutTaskBean = null;
        if (needLoadTask) {
            loadSelectedDayTaskList();
            needLoadTask = false;
        }
    }

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

    @Override
    public void onCancelAdd() {
        ((MainActivity) getActivity()).setIsCrossViewShow(false);

        enableViewTouch();

        stateChangeType = -1;
        resetTaskView();
    }

    @Override
    public void onInputContentClick() {
        if (mode == MONTH_MODE) {
            resetTaskView();
            ((TaskRootView) taskRootView).showWeekCalendar();
            stateChangeType = EDIT_TASK_CONTENT_MONTH_WEEK;
        }
    }

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

    public void showTaskUnRead() {
        ((RecyclerTaskAdapter) taskRecyclerView.getAdapter()).showUnReadMessage(unReadMessageList);
    }


    private void showCalendarUnRead() {
        initUnReadDateList();
        ((CalendarScrollViewNew) monthCalendarScrollView).showUnReadMessage(unReadMessageList);
        ((CalendarScrollViewNew) weekCalendarScrollView).showUnReadMessage(unReadMessageList);
    }

    private void loadSelectedDayTaskList() {
        Log.e(TAG, "loadSelectedDayTaskList: " + DateUtils.getCalendarAllText(selectedDay));
        if (TimeLineStoresNew.getInstance().isCanGetMailList()) {
            handler.removeCallbacks(loadTaskListRunnable);
            handler.postDelayed(loadTaskListRunnable, 500);
        } else {
            UItoolKit.showToastShort(getActivity(), "未连接到服务器，不能获取时间轴任务列表");
        }
    }

    /**
     * 加载任务列表的runnable
     */
    private Runnable loadTaskListRunnable = new Runnable() {
        @Override
        public void run() {
            TimeLineStoresNew.getInstance().getMailList(selectedDay.getTimeInMillis());
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
            Log.i(TAG, "onPageSelected: " + prePos + "=================" + position);
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

//    private int prePos = (Integer.MAX_VALUE - 2) / 2;
//
//    private void onPageChange(int position){
//        hideToggledView();
//        currentShowView = rootViewList.get(position % 5);
//        if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.task_recycler_view) {
//            taskRecyclerView = (RecyclerView) ((ViewSwitcher) currentShowView).getCurrentView();
//            ((ViewSwitcher) currentShowView).showPrevious();
//        } else {
//            taskRecyclerView = (RecyclerView) currentShowView.findViewById(R.id.task_recycler_view);
//        }
//        if (taskRecyclerView.getLayoutManager() == null) {
//            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//            taskRecyclerView.setLayoutManager(layoutManager);
//            callBack = new TaskRecyclerItemTouchCallBack();
//            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callBack);
//            taskRecyclerView.setTag(itemTouchHelper);
//            itemTouchHelper.attachToRecyclerView(taskRecyclerView);
//        }
//        itemTouchHelper = (ItemTouchHelper) taskRecyclerView.getTag();
//        ((TaskRootView) taskRootView).setTaskRecyclerView(taskRecyclerView);
//        Log.i(TAG, "onPageSelected: " + prePos + "=================" + position);
//        if (prePos > position) { //向左滑
//            selectedDay.add(Calendar.DATE, -1);
//            ((CalendarScrollViewNew) weekCalendarScrollView).moveToPrevious();
//            ((CalendarScrollViewNew) monthCalendarScrollView).moveToPrevious();
//        } else {//向右滑
//            selectedDay.add(Calendar.DATE, 1);
//            ((CalendarScrollViewNew) weekCalendarScrollView).moveToNext();
//            ((CalendarScrollViewNew) monthCalendarScrollView).moveToNext();
//        }
//        ((MainActivity) getActivity()).setYearMonth(selectedDay);
//        loadSelectedDayTaskList();
//        prePos = position;
//    }

    /**
     * 隐藏任务弹出菜单
     */
    private void hideToggledView() {
        RecyclerTaskAdapter adapter = (RecyclerTaskAdapter) taskRecyclerView.getAdapter();
        if (adapter != null)
            adapter.hideToggleMenu();
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

    private void crossWeekToMonth() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addCrossDayTaskView.getLayoutParams();
        layoutParams.topMargin = ((TaskRootView) taskRootView).getShadowViewHeight();
        scrollMonthModeTask(clickPos);
        addCrossDayTaskView.setLayoutParams(layoutParams);
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

    private void cancelClick() {
        if (addCrossDayTaskView != null && (boolean) addCrossDayTaskView.getTag()) {
            ((AddCrossDayRootView) addCrossDayTaskView).cancelClick();
        }

        if (addTodayTaskView != null && (boolean) addTodayTaskView.getTag()) {
            ((AddTodayRootView) addTodayTaskView).cancelClick();
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

    private void setSelectedDate(Calendar calendar) {
        selectedDay.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        TimeLineStoresNew.getInstance().setSelectedDay(selectedDay);
    }

    private void setCalendarSelectedDay(Calendar calendar) {
        ((MainActivity) getActivity()).setYearMonth(selectedDay);
        if (mode == MONTH_MODE) {
            ((CalendarScrollViewNew) weekCalendarScrollView).setWeekCalendarWhenMonthClick(calendar);
        } else {
            ((CalendarScrollViewNew) monthCalendarScrollView).setMonthCalendarWhenWeekClick(calendar);
        }
    }

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

    private void setDateAndLoadTask(Calendar calendar) {
        if (((ViewSwitcher) currentShowView).getCurrentView().getId() == R.id.task_recycler_view) {
            ((ViewSwitcher) currentShowView).showNext();
        }
        setSelectedDate(calendar);
        setCalendarSelectedDay(calendar);
        loadSelectedDayTaskList();
    }

    private void filkCalendar() {
        if (mode == MONTH_MODE) {
            flickView(monthCalendarScrollView);
        } else {
            flickView(weekCalendarScrollView);
        }
    }

    private void showAddTaskDialog(TaskBean taskBean, int pos, boolean isEditTime) {
        disableViewTouch();
        Log.i(TAG, "showAddTaskDialog: add on item touch listener");
        if (taskBean.isTodayTask()) {
            showAddTodayTaskDialog(taskBean, pos, isEditTime);
        } else
            showAddCrossDayTaskDialog(taskBean, pos, isEditTime);
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

    private void initAddTodayView(int topMargin) {
        addTodayTaskView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_today_task_dialog, (ViewGroup) taskRootView, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
        ((AddTodayRootView) addTodayTaskView).setListener(this);
        addTodayTaskView.setLayoutParams(layoutParams);
    }

    private void initAddCrossDayView(int topMargin) {
        addCrossDayTaskView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_cross_task_dialog, (ViewGroup) taskRootView, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(getActivity(), 40));
        layoutParams.topMargin = topMargin + ((TaskRootView) taskRootView).getShadowViewHeight();
        ((AddCrossDayRootView) addCrossDayTaskView).setListener(this);
        ((AddCrossDayRootView) addCrossDayTaskView).setEditTaskResultListener(this);
        addCrossDayTaskView.setLayoutParams(layoutParams);
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

    private void flickView(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        objectAnimator1.setDuration(200);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        objectAnimator2.setDuration(200);
        animatorSet.playSequentially(objectAnimator1, objectAnimator2);
        animatorSet.start();
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
}
