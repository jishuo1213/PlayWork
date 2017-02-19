package com.inspur.playwork.view.application.weekplan;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.weekplan.SimpleTaskBean;
import com.inspur.playwork.model.weekplan.WeekPlanDetailBean;
import com.inspur.playwork.utils.DateUtils;

/**
 * Created by fan on 17-2-6.
 */
class WeekPlanDetailAdapter extends RecyclerView.Adapter<WeekPlanDetailAdapter.ViewHolder> {
    private static final String TAG = "PlanDetailAdapterFan";

    private static final int DATE_TYPE = 1;
    private static final int TASK_TYPE = 2;


    private WeekPlanDetailBean weekPlanDetailBean;
    private SparseIntArray dateIndex;//日期的位置

    private RecyclerView recyclerView;

    private int currentSelectPos;


    interface TaskClickListener {
        void onTaskClick(SimpleTaskBean taskBean, int pos);
    }

    private TaskClickListener listener;

    WeekPlanDetailAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    void setWeekPlanDetailBean(WeekPlanDetailBean weekPlanDetailBean) {
        this.weekPlanDetailBean = weekPlanDetailBean;
        dateIndex = new SparseIntArray(7);
        int index = 0;
        for (int i = 0; i < 7; i++) {
//            dateIndex[i] = index;
            dateIndex.put(index, i);
            index = index + weekPlanDetailBean.oneWeekTasks[i].size() + 1;
        }
    }


    public void setListener(TaskClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resId = -1;
        Log.i(TAG, "onCreateViewHolder: -----------------" + viewType);
        switch (viewType) {
            case DATE_TYPE:
                resId = R.layout.layout_week_plan_date;
                break;
            case TASK_TYPE:
                resId = R.layout.layout_week_plan_task_item;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int index = dateIndex.get(position, -1);
        Log.i(TAG, "onBindViewHolder: " + position + "---------" + index);
        if (index >= 0) {
            long time = weekPlanDetailBean.weekDayTime[index];
            Log.i(TAG, "onBindViewHolder: " + (holder.dateView == null));
            holder.dateView.setText(DateUtils.getCalendarDateWeek(time));
        } else {
            SimpleTaskBean taskBean = getTask(position);

            holder.taskTimeView.setText(taskBean.unClearTime == TaskBean.NOON ? "上 午:" :
                    (taskBean.unClearTime == TaskBean.AFTERNOON ? "下 午:" : "晚 上:"));
            if (!taskBean.isEmptyTask()) {
                holder.taskContentView.setText(taskBean.taskContent);
            } else {
                holder.taskContentView.setText("");
            }
            Log.i(TAG, "onBindViewHolder: taskBean.isCurrentUserTask()" + taskBean.isCurrentUserTask());
            if (weekPlanDetailBean.isCurrentUserWeekPlan()) {
                Log.i(TAG, "onBindViewHolder: " + "setWeekPlanListener");
                if (taskBean.isEmptyTask() || taskBean.isCurrentUserTask()) {
                    holder.taskContentView.setOnClickListener(taskContentClickListener);
                } else {
                    holder.taskContentView.setOnClickListener(null);
                }
            } else {
                holder.taskContentView.setOnClickListener(null);
            }
        }
    }

    private View.OnClickListener taskContentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent());
            currentSelectPos = pos;
            SimpleTaskBean taskBean = getTask(currentSelectPos);
            Log.i(TAG, "onClick: " + pos + (listener == null));
            if (listener != null) {
                listener.onTaskClick(taskBean, currentSelectPos);
            }
        }
    };

    @Override
    public int getItemViewType(int position) {
//        if (position == dateIndex)
//        dateIndex.indexOfValue(position);
        Log.i(TAG, "getItemViewType: ++++++++" + position);
        if (dateIndex.get(position, -1) >= 0) {
            return DATE_TYPE;
        } else {
            return TASK_TYPE;
        }
//        return super.getItemViewType(position);
    }

    private SimpleTaskBean getTask(int position) {
        int mid = 0;
        int first = 0;
        int end = dateIndex.size() - 1;
        while (first <= end) {
            mid = (first + end) >>> 1;
            int midValue = dateIndex.keyAt(mid);
            if (position > midValue) {
                first = mid + 1;
            } else if (position < midValue) {
                end = mid - 1;
            }
        }

        int datePosition = dateIndex.keyAt(mid);
        if (datePosition > position) {
            datePosition = dateIndex.keyAt(--mid);
        }
        int index = dateIndex.get(datePosition);
        return weekPlanDetailBean.oneWeekTasks[index].get(position - datePosition - 1);
    }

    @Override
    public int getItemCount() {
        return weekPlanDetailBean.allTasksCount + 7;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateView;
        private TextView taskTimeView;
        private TextView taskContentView;

        public ViewHolder(View itemView, int type) {
            super(itemView);
            switch (type) {
                case TASK_TYPE:
                    taskTimeView = (TextView) itemView.findViewById(R.id.tv_week_plan_time);
                    taskContentView = (TextView) itemView.findViewById(R.id.tv_week_plan_detail);
                    break;
                case DATE_TYPE:
                    dateView = (TextView) itemView.findViewById(R.id.tv_date);
                    Log.i(TAG, "ViewHolder: -------" + type + (dateView == null));
                    break;
            }
        }
    }

    SimpleTaskBean getNextTask() {
        int pos = currentSelectPos;
        do {
            pos = pos + 1;
            if (pos >= getItemCount()) {
                pos = -1;
                continue;
            }
            if (getItemViewType(pos) != DATE_TYPE) {
                break;
            }
        } while (true);
        currentSelectPos = pos;
        return getTask(pos);
    }


    SimpleTaskBean getPreviousTask() {
        int pos = currentSelectPos;
        do {
            pos = pos - 1;
            if (pos <= 0) {
                pos = getItemCount();
                continue;
            }
            if (getItemViewType(pos) != DATE_TYPE) {
                break;
            }

        } while (true);
        currentSelectPos = pos;
        return getTask(pos);
    }

    void refreshTaskBean() {
        notifyItemChanged(currentSelectPos);
    }
}
