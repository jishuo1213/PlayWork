package com.inspur.playwork.view.timeline;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.timeline.CrossTaskBeanComparator;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.timeline.TodayTaskBeanComparator;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.BadgeView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Fan on 15-9-5.
 */
public class RecyclerTaskAdapter extends RecyclerView.Adapter<RecyclerTaskAdapter.ViewHolder> implements TaskRecyclerItemTouchCallBack.ItemTouchHelperAdapter {

    private static final String TAG = "RecyclerTaskAdapterFan";

    //  private static final int TODAY_TITLE_VIEW = 100;

    private static final int CROSS_DAY_TITLE_VIEW = 101;
    private static final int TASK_VIEW = 102;


    private List<TaskBean> todayTaskList;
    private List<TaskBean> crossDayTaskList;

    private LayoutInflater inflater;

    private TaskItemEventListener listener;

    private TaskItemLongClickListener itemLongClickListener;
    private TaskContentClickListener taskContentClickListener;
    private TaskMenuClickListener taskMenuClickListener;
    private TaskTimeClickListener taskTimeClickListener;
    private EmptyTaskClickListener emptyTaskClickListener;

    private RecyclerView recyclerView;

    private String currentUserId;

    private int itemHeight;

    private int oneDp;

    private long[] toggleItems;

    private int topMargin;

    private ArrayMap<String, SortObj> sortNum;

    // private ArrayList<Long> toggleItemsList;


    private static class SortObj {
        public int sortNum;
        public int unclearTime;

        public SortObj(int sortNum, int unclearTime) {
            this.sortNum = sortNum;
            this.unclearTime = unclearTime;
        }
    }


    public RecyclerTaskAdapter(Context context) {
        todayTaskList = new ArrayList<>();
        crossDayTaskList = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
        itemLongClickListener = new TaskItemLongClickListener();
        //itemClickListener = new TaskItemClickListener();
        taskContentClickListener = new TaskContentClickListener();
        taskMenuClickListener = new TaskMenuClickListener();
        emptyTaskClickListener = new EmptyTaskClickListener();
        taskTimeClickListener = new TaskTimeClickListener();
        currentUserId = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        itemHeight = DeviceUtil.dpTopx(context, 43);
        oneDp = DeviceUtil.dpTopx(context, 1);
        toggleItems = new long[2];
        //toggleItemsList = new ArrayList<>();
    }

    public boolean isInvalidPos(int pos) {
        TaskBean mBean = getItem(pos);
        return pos == todayTaskList.size() + 1 || mBean == null || mBean.isEmptyTask();
    }

    public void sortTask(JSONObject jsonObject, boolean isViable) {
        if (sortNum == null)
            sortNum = new ArrayMap<>();
        sortNum.clear();
        JSONArray nums = jsonObject.optJSONArray("data");
        int num = nums.length();
        for (int i = 0; i < num; i++) {
            JSONObject data = nums.optJSONObject(i);
            sortNum.put(data.optString("_id"), new SortObj(data.optInt("xh"), data.optInt("shijianType")));
        }

        for (TaskBean task : crossDayTaskList) {
            if (!TextUtils.isEmpty(task.taskId)) {
                task.sortNum = sortNum.get(task.taskId).sortNum;
            }
        }

        for (TaskBean task : todayTaskList) {
            if (!TextUtils.isEmpty(task.taskId)) {
                task.sortNum = sortNum.get(task.taskId).sortNum;
                int unClearTime = sortNum.get(task.taskId).unclearTime;
                if (unClearTime != task.unClearTime) {
                    task.setTimeByUnclearTime(unClearTime);
                }
            }
        }

        TaskBean empty = crossDayTaskList.remove(crossDayTaskList.size() - 1);
        Collections.sort(crossDayTaskList, new CrossTaskBeanComparator());
        crossDayTaskList.add(empty);

        Collections.sort(todayTaskList, new TodayTaskBeanComparator());
        if (isViable) {
            notifyDataSetChanged();
        }
    }

    public interface TaskItemEventListener {
        void onTaskContentClick(TaskBean taskBean, int pos, boolean isOnlyShowMenu);

        void onItemLongClick(View view, int position, TaskBean taskBean);

        void onToggleMenuClick(TaskBean taskBean, int type, int pos);

        void onEmptyViewClick(TaskBean taskBean, int pos);

        void onTaskTimeClick(TaskBean taskBean, int pos);

        void onCancelCutBean();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case CROSS_DAY_TITLE_VIEW:
                layout = R.layout.layout_task_across_day_title;
                break;
            case TASK_VIEW:
                layout = R.layout.layout_task_item;
                break;
            case TaskBean.EMPTY_CROSS_DAY_TASK:
            case TaskBean.EMPTY_NOON:
            case TaskBean.EMPTY_NIGHT:
            case TaskBean.EMPTY_AFTERNOON:
            case TaskBean.EMPTY_TODAY_TASK:
                layout = R.layout.layout_empty_task;
                break;
        }
        View v = inflater.inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    void reNameTask(String taskId, String newTitle) {
        int pos = findPositionByTaskId(taskId);
        if(pos < 0)
            return;
        TaskBean taskBean = getItem(pos);
        taskBean.taskContent = newTitle;
        notifyItemChanged(pos);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TaskBean mBean = getItem(position);
        if (mBean != null) {
            if (!mBean.isEmptyTask()) {
                bindNormalTask(holder, position, mBean);
            } else {
                bindEmptyTask(holder, mBean);
            }

            if (holder.rootView.getAlpha() < 1) {
                cutPos = -1;
                holder.rootView.setAlpha(1);
            }

            if (mBean.isTodayTask() && position < todayTaskList.size() - 1) {
                TaskBean nextTask = getItem(position + 1);
                boolean isCurrentEmpty = mBean.isEmptyTask();
                boolean isNextEmpty = nextTask.isEmptyTask();
                if (!isCurrentEmpty && !isNextEmpty) {
                    if (mBean.unClearTime != nextTask.unClearTime)
                        holder.taskAllViews.setBackgroundResource(R.drawable.bottom_red_dotted_line);
                    else
                        holder.taskAllViews.setBackgroundResource(R.drawable.bottom_dotted_line);
                } else {
                    if (mBean.isEmptyTask())
                        holder.rootView.setBackgroundResource(R.drawable.bottom_red_dotted_line);
                    else
                        holder.taskAllViews.setBackgroundResource(R.drawable.bottom_red_dotted_line);
                }
            } else if (position == todayTaskList.size() - 1) {
                holder.rootView.setBackgroundColor(ResourcesUtil.getInstance().getColor(R.color.white));
            } else {
                if (mBean.isEmptyTask()) {
                    holder.rootView.setBackgroundResource(R.drawable.empty_task_selector);
                } else {
                    holder.taskAllViews.setBackgroundResource(R.drawable.bottom_line);
                }
            }
        }
    }

    void clearListener() {
        listener = null;
        itemLongClickListener = null;
        taskContentClickListener = null;
        taskMenuClickListener = null;
        taskTimeClickListener = null;
        emptyTaskClickListener = null;
    }

    private void bindEmptyTask(ViewHolder holder, TaskBean mBean) {
        holder.taskTime.setText(mBean.startTimeString);
        holder.taskTime.setOnClickListener(taskTimeClickListener);
        holder.rootView.setOnClickListener(emptyTaskClickListener);
    }

    private void bindNormalTask(ViewHolder holder, int position, TaskBean mBean) {
        if (mBean.isTodayUnClearTask()) {
            holder.taskTime.setText(mBean.startTimeString);
        } else {
            holder.taskTime.setText(mBean.startTimeString + " ~ " + mBean.endTimeString);
        }
        holder.taskContent.setText(mBean.taskContent);
        long itemId = getItemId(position);
        holder.rootView.setTag(itemId);
        holder.taskToggleMenu.setVisibility(getItemId(position) == toggleItems[0] ? View.VISIBLE : View.GONE);
        if (!mBean.isTodayTask()) {
            holder.taskEdit.setVisibility(View.GONE);
        }

        //holder.taskToggleMenu.setVisibility(toggleItemsList.contains(itemId) ? View.VISIBLE : View.GONE);
        if (mBean.unReadMessageNum > 0) {
            if (holder.badgeView == null) {
                holder.badgeView = new BadgeView(inflater.getContext());
                holder.badgeView.setTargetView(holder.taskContent);
                holder.badgeView.setBackgroundResource(R.drawable.bageview_back);
            }
            holder.badgeView.setText(mBean.unReadMessageNum + "");
            holder.badgeView.setVisibility(View.VISIBLE);
        } else {
            if (holder.badgeView != null) {
                holder.badgeView.setVisibility(View.GONE);
            }
        }
        if (mBean.taskCreator.equals(currentUserId)) {
            holder.taskFrom.setVisibility(View.INVISIBLE);
        } else {
            holder.taskFrom.setVisibility(View.VISIBLE);
        }

        holder.contentContainer.setOnLongClickListener(itemLongClickListener);
        holder.contentContainer.setOnClickListener(taskContentClickListener);
        holder.taskTime.setOnClickListener(taskTimeClickListener);

        holder.taskChat.setOnClickListener(taskMenuClickListener);
        holder.taskEdit.setOnClickListener(taskMenuClickListener);
        holder.taskDelete.setOnClickListener(taskMenuClickListener);
    }

    @Override
    public int getItemCount() {
        return todayTaskList.size() + crossDayTaskList.size() + 1;
    }


    @Override
    public long getItemId(int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TASK_VIEW:
                TaskBean item = getItem(position);
                assert item != null;
                return item.taskId.hashCode();
            case CROSS_DAY_TITLE_VIEW:
                return -2;
            default:
                return position;
        }
    }


    public TaskBean getItem(int position) {
        if (position < todayTaskList.size() && position >= 0) {
            return todayTaskList.get(position);
        } else if (position > todayTaskList.size()) {
            return crossDayTaskList.get(position - todayTaskList.size() - 1);
        } else {
            return null;
        }
    }

/*    public void imitateCrossEmptyClick() {
        int pos = getItemCount() - 1;
        listener.onEmptyViewClick(getItem(pos), pos);
    }

    public void imitateEmptyClick(int pos) {
        listener.onEmptyViewClick(getItem(pos), pos);
    }*/

    @Override
    public int getItemViewType(int position) {
        if (position == todayTaskList.size()) {
            return CROSS_DAY_TITLE_VIEW;
        } else {
            TaskBean mBean = getItem(position);
            assert mBean != null;
            if (mBean.isEmptyTask()) {
                return mBean.unClearTime;
            } else {
                return TASK_VIEW;
            }
        }
    }

    @Override
    public void onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();

        TaskBean fromTask = getItem(from);
        TaskBean toTask = getItem(to);

        int todayTaskCount = getTodayTaskCount();
        int tempTodayTaskCount;
        List<String> taskIds = TimeLineStoresNew.getInstance().getTaskIds();
        tempTodayTaskCount = todayTaskList.get(todayTaskCount - 1).unClearTime == TaskBean.EMPTY_TODAY_TASK ?
                todayTaskCount - 1 : todayTaskCount;
        if (from < tempTodayTaskCount && to < tempTodayTaskCount && to >= 0) {
            Collections.swap(todayTaskList, from, to);
            Collections.swap(taskIds, from, to);
            if (!isSameUnClearTime(fromTask, toTask)) {
                switch (toTask.unClearTime) {
                    case TaskBean.NOON:
                    case TaskBean.EMPTY_NOON:
                        fromTask.setTimeByUnclearTime(TaskBean.NOON);
                        break;
                    case TaskBean.AFTERNOON:
                    case TaskBean.EMPTY_AFTERNOON:
                        fromTask.setTimeByUnclearTime(TaskBean.AFTERNOON);
                        break;
                    case TaskBean.NIGHT:
                    case TaskBean.EMPTY_NIGHT:
                        fromTask.setTimeByUnclearTime(TaskBean.NIGHT);
                        break;
                }
            } else {
                switch (toTask.unClearTime) {
                    case TaskBean.NOON:
                    case TaskBean.EMPTY_NOON:
//                        fromTask.setTimeByUnclearTime(TaskBean.NOON);
                        if (from < to && !isSameUnClearTime(getItem(to + 1), toTask))
                            fromTask.setTimeByUnclearTime(TaskBean.AFTERNOON);
                        break;
                    case TaskBean.AFTERNOON:
                    case TaskBean.EMPTY_AFTERNOON:
                        if (from < to && !isSameUnClearTime(getItem(to + 1), toTask)) {
                            fromTask.setTimeByUnclearTime(TaskBean.NIGHT);
                        } else if (from > to && !isSameUnClearTime(getItem(to - 1), toTask)) {
                            fromTask.setTimeByUnclearTime(TaskBean.NOON);
                        }
                        break;
                    case TaskBean.NIGHT:
                    case TaskBean.EMPTY_NIGHT:
                        if (from > to && !isSameUnClearTime(getItem(to - 1), toTask)) {
                            fromTask.setTimeByUnclearTime(TaskBean.AFTERNOON);
                        }
//                        fromTask.setTimeByUnclearTime(TaskBean.NIGHT);
                        break;
                }
            }
            notifyItemMoved(from, to);
            if (fromTask.isTodayUnClearTask()) {
                ((ViewHolder) viewHolder).taskTime.setText(fromTask.startTimeString);
            }
//            notifyItemChanged(to);
        } else if (from > todayTaskCount && to > todayTaskCount) {
            Collections.swap(crossDayTaskList, from - todayTaskCount - 1, to - todayTaskCount - 1);
            Collections.swap(taskIds, from - todayTaskCount - 1, to - todayTaskCount - 1);
            notifyItemMoved(from, to);
        }
    }

    @Override
    public void onDropItem(RecyclerView.ViewHolder viewHolder) {
        int pos = viewHolder.getAdapterPosition();
        Log.i(TAG, "onDropItem: " + pos);
        TaskBean taskBean = getItem(pos);
//        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_CHANGE_TASK_TIME, taskBean);
        if (taskBean.isTodayTask()) {
            TimeLineStoresNew.getInstance().changeTaskTime(taskBean, null);
        }
        editTask(taskBean);
    }


    private boolean isSameUnClearTime(TaskBean from, TaskBean to) {
        if (!to.isEmptyTask() && from != null && from.unClearTime == to.unClearTime)
            return true;
        else if (to.isEmptyTask() && from != null && from.unClearTime + 11 == to.unClearTime)
            return true;
        return false;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTime, taskContent;
        public View rootView;
        BadgeView badgeView;
        ImageView taskFrom;
        View taskAllViews;

        TextView taskContentInput;

        View taskToggleMenu;
        View taskEdit, taskChat, taskDelete;

        View contentContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            taskTime = (TextView) itemView.findViewById(R.id.tv_task_start_time);
            taskContentInput = (TextView) itemView.findViewById(R.id.edit_input_task_content);
            if (taskContentInput == null) {
                taskToggleMenu = itemView.findViewById(R.id.click_toggle_menu);
                taskAllViews = itemView.findViewById(R.id.task_all_views);
                taskContent = (TextView) itemView.findViewById(R.id.tv_task_content);
                taskFrom = (ImageView) itemView.findViewById(R.id.img_task_from);

                taskEdit = itemView.findViewById(R.id.img_edit_task);
                taskChat = itemView.findViewById(R.id.img_task_chat);
                taskDelete = itemView.findViewById(R.id.img_task_delete);

                contentContainer = itemView.findViewById(R.id.fram_task_content_container);
            }
        }
    }

    /**
     * 增加一个任务
     */
    public void addTask(TaskBean mBean) {
        if (mBean.isTodayTask()) {
            int index = todayTaskList.indexOf(mBean);
            Log.i(TAG, "addTask: " + mBean.unClearTime);
            TodayTaskBeanComparator comparator = new TodayTaskBeanComparator();
            if (index == -1) { //创建的新的Bean
                todayTaskList.add(mBean);
                Collections.sort(todayTaskList, comparator);
                int sameTimeIndex = -1;
                for (TaskBean taskBean : todayTaskList) {
                    if (taskBean.isEmptyTask() && taskBean.isSameTodayUnClearTime(mBean)) {
                        sameTimeIndex = todayTaskList.indexOf(taskBean);
                    }
                }
                if (sameTimeIndex != -1)
                    todayTaskList.remove(sameTimeIndex);
                notifyItemRangeChanged(0, todayTaskList.size() - 1);
            } else { //之前的bean
                if (index > 0) {
                    if (comparator.compare(getItem(index - 1), mBean) > 0 &&
                            comparator.compare(mBean, getItem(index + 1)) < 0) {
                        notifyItemChanged(index);
                        if (todayTaskNeedEmpty()) {
                            TaskBean emptyTaskBean = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_TODAY_TASK);
                            todayTaskList.add(emptyTaskBean);
                            notifyItemInserted(todayTaskList.size() - 1);
                        }
                    } else {
                        addAndRemoveEmptyBeans(comparator);
                        notifyItemRangeChanged(0, todayTaskList.size() - 1);
                    }
                } else {
                    if (!todayTaskNeedEmpty() && comparator.compare(mBean, getItem(index + 1)) < 0) {
                        notifyItemChanged(index);
                        if (todayTaskNeedEmpty()) {
                            TaskBean emptyTaskBean = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_TODAY_TASK);
                            todayTaskList.add(emptyTaskBean);
                            notifyItemInserted(todayTaskList.size() - 1);
                        }
                    } else {
                        addAndRemoveEmptyBeans(comparator);
                        notifyItemRangeChanged(0, todayTaskList.size() - 1);
                    }
                }
            }
            recyclerView.scrollToPosition(0);
        } else {
            int pos = getItemCount();
            // crossDayTaskList.add(mBean);
            crossDayTaskList.add(TaskBean.createEmptyTaskBean(TaskBean.EMPTY_CROSS_DAY_TASK));
            notifyItemRangeInserted(pos, pos + 1);
        }
    }


    /**
     * 当从点击上午下午晚上的空行添加当天任务的时候，如果是第一次
     * 则需要在最后添加上空行
     * 如果时间段不一样，需要修改这个任务的位置
     */
    private void addAndRemoveEmptyBeans(TodayTaskBeanComparator comparator) {
        boolean isNoon = false, isAfterNoon = false, isNight = false;
        boolean needAddAllEmpty = true;
        Iterator<TaskBean> iterator = todayTaskList.iterator();
        while (iterator.hasNext()) {
            TaskBean taskBean = iterator.next();
            if (taskBean.isEmptyTask()) {
                if (!(taskBean.unClearTime == TaskBean.EMPTY_TODAY_TASK)) {
                    iterator.remove();
                } else {
                    needAddAllEmpty = false;
                }
            } else {
                switch (taskBean.unClearTime) {
                    case TaskBean.NOON:
                        isNoon = true;
                        break;
                    case TaskBean.AFTERNOON:
                        isAfterNoon = true;
                        break;
                    case TaskBean.NIGHT:
                        isNight = true;
                        break;
                }
            }
        }

        if (!isNoon) {
            TaskBean noonEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_NOON);
            todayTaskList.add(noonEmpty);
        }
        if (!isAfterNoon) {
            TaskBean afterNoonEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_AFTERNOON);
            todayTaskList.add(afterNoonEmpty);
        }
        if (!isNight) {
            TaskBean nightEmpty = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_NIGHT);
            todayTaskList.add(nightEmpty);
        }
        if (needAddAllEmpty) {
            TaskBean emptyTaskBean = TaskBean.createEmptyTaskBean(TaskBean.EMPTY_TODAY_TASK);
            todayTaskList.add(emptyTaskBean);
        }
        Collections.sort(todayTaskList, comparator);
    }

    private boolean todayTaskNeedEmpty() {
        TaskBean lastBean = todayTaskList.get(todayTaskList.size() - 1);
        return lastBean.unClearTime != TaskBean.EMPTY_TODAY_TASK;
    }

    public void editTask(TaskBean taskBean) {
        if (taskBean.isTodayTask()) {
            int index = todayTaskList.indexOf(taskBean);
            TodayTaskBeanComparator comparator = new TodayTaskBeanComparator();
            if (index > 0) {
                if (comparator.compare(getItem(index - 1), taskBean) < 0 &&
                        comparator.compare(taskBean, getItem(index + 1)) > 0) {
                    notifyItemChanged(index);
                } else {
                    addAndRemoveEmptyBeans(comparator);
                    notifyDataSetChanged();
                }
            } else {
                if (comparator.compare(taskBean, getItem(index + 1)) < 0) {
                    notifyItemChanged(index);
                } else {
                    addAndRemoveEmptyBeans(comparator);
                    notifyDataSetChanged();
                }
            }
        } else {
            int index = crossDayTaskList.indexOf(taskBean);
            notifyItemChanged(index + todayTaskList.size() + 1);
        }
    }


/*    public void resetTaskList(TaskBean mBean, int pos) {
        if (pos == -1)
            return;
        if (mBean.isEmptyTask())
            switch (mBean.unClearTime) {
                case TaskBean.EMPTY_NOON:
                case TaskBean.EMPTY_AFTERNOON:
                case TaskBean.EMPTY_NIGHT:
                    break;
                case TaskBean.EMPTY_CROSS_DAY_TASK:
                    break;
            }
        else {
            switch (mBean.taskType) {
                case TaskBean.TODAY_TASK:
                case TaskBean.TODAY_TASK_TIME_UNCLEAR:
                    break;
                case TaskBean.CROSS_DAY_TASK_TIME_UNCLEAR:
                case TaskBean.CROSS_DAY_TASK:
                    break;
            }
        }
    }*/

    /**
     * 设置指定位置的未读消息个数
     * 其实就两种，一种是归零，一种是在当前的基础上加一
     */
    public void setPosUnReadNum(String taskId, int count) {
        int pos = findPositionByTaskId(taskId);
        TaskBean taskBean = getItem(pos);
        if (taskBean == null)
            return;
        taskBean.unReadMessageNum = count == 0 ? 0 : taskBean.unReadMessageNum + 1;
        notifyItemChanged(pos);
    }


    public void showUnReadMessage(ArrayList<UnReadMessageBean> unReadList) {
        if (unReadList == null) {
            return;
        }
        List<String> taskIds = TimeLineStoresNew.getInstance().getTaskIds();
        for (TaskBean mBean : todayTaskList)
            mBean.unReadMessageNum = 0;
        for (TaskBean mBean : crossDayTaskList)
            mBean.unReadMessageNum = 0;
        for (UnReadMessageBean messageBean : unReadList) {
            boolean res = isSelectDayMsg(messageBean);
//            boolean isTaskContainsToday = messageBean.taskCreateTime >= (TimeLineStoresNew.getInstance().getSelectedDay().getTimeInMillis() - 1000);

            if (res) {
                if (taskIds.contains(messageBean.taskId)) {
                    int pos = findPositionByTaskId(messageBean.taskId);
                    if (pos == -1)
                        continue;
                    TaskBean taskBean = getItem(pos);
                    assert taskBean != null;
                    taskBean.unReadMessageNum++;
                }
            }
        }
        notifyDataSetChanged();
    }

    private int findPositionByTaskId(String taskId) {
        for (int i = 0; i < getItemCount(); i++) {
            TaskBean mBean = getItem(i);
            if (mBean == null)
                continue;
            if (!mBean.isEmptyTask() && mBean.taskId.equals(taskId))
                return i;
        }
        return -1;
    }

    private boolean isSelectDayMsg(UnReadMessageBean messageBean) {
        Calendar selectedDay = TimeLineStoresNew.getInstance().getSelectedDay();
        int year = selectedDay.get(Calendar.YEAR);
        int month = selectedDay.get(Calendar.MONTH);
        int day = selectedDay.get(Calendar.DATE);
        return messageBean.type == MessageBean.MESSAGE_TASK_CHAT
                && day == messageBean.createDay &&
                month == messageBean.createMonth && year == messageBean.createYear;
    }

/*    private boolean containesSelectday(ArrayList<UnReadMessageBean> unReadList) {
        if (unReadList == null || unReadList.size() == 0) {
            return false;
        }
        Calendar selectedDay = TimeLineStores.getInstance().getSelectedDay();
        return selectedDay.getTimeInMillis() >= unReadList.get(0).taskCreateTime ||
                selectedDay.getTimeInMillis() <= unReadList.get(unReadList.size() - 1).taskCreateTime;
    }*/

    public void setCrossDayTaskList(List<TaskBean> crossDayTaskList) {
        this.crossDayTaskList = crossDayTaskList;
        cutPos = -1;
        toggleItems[0] = toggleItems[1] = 0;
    }

    public void setTodayTaskList(List<TaskBean> todayTaskList) {
        this.todayTaskList = todayTaskList;
        cutPos = -1;
        toggleItems[0] = toggleItems[1] = 0;
    }

    public void setListener(TaskItemEventListener listener) {
        this.listener = listener;
    }


    private int getTodayTaskCount() {
        return todayTaskList.size();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }


/*    private void removePosTaskBean(int position) {
        if (position <= todayTaskList.size() && position > 0) {
            todayTaskList.remove(position - 1);
        } else if (position > todayTaskList.size() + 1) {
            crossDayTaskList.remove(position - todayTaskList.size() - 2);
        }
    }*/


    private class EmptyTaskClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (cancelCutState()) {
                listener.onCancelCutBean();
                return;
            }

            if (hideToggleMenu()) return;

            int pos = recyclerView.getChildAdapterPosition(v);
            TaskBean taskBean = getItem(pos);
            if (taskBean.unClearTime != TaskBean.EMPTY_CROSS_DAY_TASK) {
                if (!isCurrentDay()) {
                    UItoolKit.showToastShort(v.getContext(), "只能在今天创建当日任务");
                } else {
                    if (taskBean.unClearTime == TaskBean.EMPTY_TODAY_TASK) {
                        taskBean = TaskBean.createEmptyTaskBean(-1);
                    }
                    listener.onEmptyViewClick(taskBean, pos);
                }
            } else {
                listener.onEmptyViewClick(taskBean, pos);
            }
        }
    }

    public int getTotalHeight() {
        return (getItemCount() - 1) * itemHeight + oneDp;
    }

 /*   public int getNeedScrollDis(int pos) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int recyViewHeight = layoutManager.getHeight();
        int canVisablePos;
        Log.i(TAG, recyViewHeight + "recyViewHeight");

        canVisablePos = pos < todayTaskList.size() ? recyViewHeight + DeviceUtil.dpTopx(recyclerView.getContext(), 49) -
                PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT) -
                DeviceUtil.dpTopx(recyclerView.getContext(), 40) : recyViewHeight + DeviceUtil.dpTopx(recyclerView.getContext(), 49) -
                PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT);
        topMargin = canVisablePos - DeviceUtil.dpTopx(recyclerView.getContext(), 40);
        int posItemBottom = layoutManager.findViewByPosition(pos).getBottom();
        Log.i(TAG, posItemBottom + "pos item bottom");
        if (posItemBottom < itemHeight) {
            return posItemBottom - itemHeight;
        }
        if (posItemBottom <= canVisablePos) {
            return 0;
        } else {
            return posItemBottom - canVisablePos;
        }
    }*/


    public int getNeedScrollDis(int pos) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int recyViewHeight = layoutManager.getHeight();
        int canVisablePos;

        canVisablePos = pos > todayTaskList.size() ? recyViewHeight + DeviceUtil.dpTopx(recyclerView.getContext(), 49)
                - PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT) :
                recyViewHeight + DeviceUtil.dpTopx(recyclerView.getContext(), 49)
                        - PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT) -
                        DeviceUtil.dpTopx(recyclerView.getContext(), 40);


        int posItemBottom = layoutManager.findViewByPosition(pos).getBottom();

        if (posItemBottom < itemHeight) {
            topMargin = DeviceUtil.dpTopx(recyclerView.getContext(), 3) - 1;
            return posItemBottom - itemHeight;
        }
        if (posItemBottom <= canVisablePos) {
            topMargin = layoutManager.findViewByPosition(pos).getTop() + DeviceUtil.dpTopx(recyclerView.getContext(), 3) - 1;
            return 0;
        } else {
            topMargin = canVisablePos - DeviceUtil.dpTopx(recyclerView.getContext(), 43);
            return posItemBottom - canVisablePos;
        }
    }


    public int getMonthModeScrollDis(int pos) {
        if (pos < todayTaskList.size()) {
            return pos * itemHeight;
        } else if (pos > todayTaskList.size()) {
            return (pos - 1) * itemHeight + oneDp;
        } else {
            return 0;
        }
    }

    public int getTopMargin() {
        return topMargin;
    }

 /*   public int getItemHeight() {
        return itemHeight;
    }*/


    public void removeTaskItem(int pos) {
        if (pos < todayTaskList.size()) {
            todayTaskList.remove(pos);
            notifyItemRemoved(pos);
            addAndRemoveEmptyBeans(new TodayTaskBeanComparator());
            notifyItemRangeChanged(0, todayTaskList.size());
        } else if (pos > todayTaskList.size()) {
            crossDayTaskList.remove(pos - todayTaskList.size() - 1);
            notifyItemRemoved(pos);
        }
    }

    public void removeTaskItem(String taskId) {
        int pos = recyclerView.getChildAdapterPosition(recyclerView.findViewHolderForItemId(taskId.hashCode()).itemView);
        if (pos >= 0) {
            removeTaskItem(pos);
        }
    }

    private boolean isCurrentDay() {
        return (Calendar.getInstance().getTimeInMillis() - TimeLineStoresNew.getInstance().getSelectedDay().getTimeInMillis() <= 86400000);
    }


    private class TaskItemLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            int pos;
            try {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            } catch (Exception e) {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent().getParent().getParent());
            }
            Log.i(TAG, "onLongClick: " + pos);
            listener.onItemLongClick(v, pos, getItem(pos));
            return true;
        }
    }

    private class TaskContentClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (cancelCutState()) {
                listener.onCancelCutBean();
                return;
            }
            int pos;
            try {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            } catch (Exception e) {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent().getParent().getParent());
            }
            TaskBean mBean = getItem(pos);
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (pos > todayTaskList.size()) {
                holder.taskEdit.setVisibility(View.GONE);
            } else {
                holder.taskEdit.setVisibility(View.VISIBLE);
            }

            if (holder.taskToggleMenu.getVisibility() == View.VISIBLE) { // 进入编辑任务状态
                holder.taskToggleMenu.setVisibility(View.GONE);
                toggleItems[0] = toggleItems[1] = 0;
                listener.onTaskContentClick(mBean, pos, false);
                return;
            }

            if (hideToggleMenu()) return;

            if (holder.taskToggleMenu.getVisibility() == View.GONE) {
                holder.taskToggleMenu.setVisibility(View.VISIBLE);
                toggleItems[0] = getItemId(pos);
                toggleItems[1] = getItemId(pos);
                listener.onTaskContentClick(mBean, pos, true);
            }
        }
    }

    public boolean hideToggleMenu() {
        if (toggleItems[1] != 0) {
            ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForItemId(toggleItems[1]);
            if (preViewHolder != null) {
                preViewHolder.taskToggleMenu.setVisibility(View.GONE);
                toggleItems[1] = 0;
                toggleItems[0] = 0;
                return true;
            }
        }
        return false;
    }

    private class TaskTimeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (cancelCutState()) {
                listener.onCancelCutBean();
                return;
            }

            if (hideToggleMenu()) return;

            int pos;
            try {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            } catch (Exception e) {
                pos = recyclerView.getChildAdapterPosition((View) v.getParent());
            }
            TaskBean taskBean = getItem(pos);
            /*if (taskBean.isEmptyTask()) {
                return;
            }*/
            listener.onTaskTimeClick(taskBean, pos);
        }
    }

    private boolean cancelCutState() {
        if (cutPos != -1) {
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(cutPos);
            viewHolder.rootView.setAlpha(1);
            cutPos = -1;
            return true;
        }
        return false;
    }

    private int cutPos = -1;

    private class TaskMenuClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            viewHolder.taskToggleMenu.setVisibility(View.GONE);
            toggleItems[0] = toggleItems[1] = 0;
            switch (v.getId()) {
                case R.id.img_task_delete:
                    listener.onToggleMenuClick(getItem(position), 5, position);
                    break;
                case R.id.img_edit_task:
                    TaskBean mBean = getItem(position);
                    if (!mBean.taskCreator.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                        UItoolKit.showToastShort(v.getContext(), "你不是该任务创建者，不能移动该任务");
                        return;
                    }
                    cutPos = position;
                    viewHolder.rootView.setAlpha(0.3f);
                    listener.onToggleMenuClick(mBean, 1, position);
                    break;
                case R.id.img_task_chat:
                    listener.onToggleMenuClick(getItem(position), 2, position);
                    break;
            }
        }
    }
}
