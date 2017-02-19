package com.inspur.playwork.view.application.weekplan;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.weekplan.WeekPlanHeader;
import com.inspur.playwork.utils.DateUtils;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-12.
 */
class WeekPlanListAdapter extends RecyclerView.Adapter<WeekPlanListAdapter.ViewHolder> {
    private static final String TAG = "WeekPlanListAdapter";

    private ArrayList<WeekPlanHeader> weekPlanHeaders;

    private RecyclerView weekPlanList;

    private WeekPlanListListener listEventListener;

    WeekPlanListAdapter(RecyclerView weekPlanList) {
        this.weekPlanList = weekPlanList;
    }

    interface WeekPlanListListener {
        void onWeekPlanClick(int pos);
    }

    public void setListEventListener(WeekPlanListListener listEventListener) {
        this.listEventListener = listEventListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resId = -1;
        switch (viewType) {
            case WeekPlanHeader.MY_PLAN_TITLE:
                resId = R.layout.layout_week_plan_list_title;
                break;
            case WeekPlanHeader.OTHER_PLAN_TITLE:
                resId = R.layout.layout_week_plan_list_title;
                break;
            case WeekPlanHeader.OTHER_PLAN:
                resId = R.layout.layout_week_plan_other_item;
                break;
            case WeekPlanHeader.MY_PLAN:
                resId = R.layout.layout_week_plan_other_item;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeekPlanHeader item = getItem(position);
        if (item.type == WeekPlanHeader.MY_PLAN_TITLE) {
            holder.titleView.setText("我的计划");
            return;
        }
        if (item.type == WeekPlanHeader.OTHER_PLAN_TITLE) {
            holder.titleView.setText("他人计划");
            return;
        }
        if (item.type != WeekPlanHeader.MY_PLAN) {
            holder.planTime.setText(DateUtils.getCalendarText(item.updateTime));
        } else {
            holder.planTime.setText("");
        }
        holder.planName.setText(item.subject);
        holder.department.setText(item.from.department);
        holder.itemView.setOnClickListener(weekPlanClickListener);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public int getItemCount() {
        return weekPlanHeaders.size();
    }

    public WeekPlanHeader getItem(int pos) {
        return weekPlanHeaders.get(pos);
    }

    void setWeekPlanHeaders(ArrayList<WeekPlanHeader> weekPlanHeaders) {
        this.weekPlanHeaders = weekPlanHeaders;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView planName;
        TextView department;
        TextView planTime;
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            planName = (TextView) itemView.findViewById(R.id.tv_week_plan_name);
            department = (TextView) itemView.findViewById(R.id.tv_from_department);
            planTime = (TextView) itemView.findViewById(R.id.tv_share_time);
            titleView = (TextView) itemView.findViewById(R.id.week_plan_title);
        }
    }

    private View.OnClickListener weekPlanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = weekPlanList.getChildLayoutPosition(v);
//            WeekPlanHeader weekPlanHeader = getItem(pos);
            if (listEventListener != null) {
                listEventListener.onWeekPlanClick(pos);
            }
        }
    };
}
