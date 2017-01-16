package com.inspur.playwork.view.timeline;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.timeline.CalendarDateBean;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.view.common.BadgeView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Fan on 15-9-2.
 */
class RecycleCalendarAdapter extends RecyclerView.Adapter<RecycleCalendarAdapter.ViewHolder> {

    private static final String TAG = "CalendarAdapterFan";

    static final int MONTH_ADAPTER = 1;
    static final int WEEK_ADAPTER = 2;

    private static int[] backgrounds = {R.drawable.num1, R.drawable.num2, R.drawable.num3,
            R.drawable.num4, R.drawable.num5, R.drawable.num6, R.drawable.num7
            , R.drawable.num8, R.drawable.num9, R.drawable.num10, R.drawable.num11, R.drawable.num12};


    interface DateClickListener {
        void onOneDayClick(CalendarDateBean mBean);
    }

    private ArrayList<CalendarDateBean> calendarDateList;

    private Calendar selectDay;

    private int count;

    private RecyclerView calendarRecyclerView;

    private DateClickListener dateClickListener;

    private CalendarClickListener calendarClickListener;

    private int mode;

    private View preSelectedView;
    private ViewGroup calendarScrollView;

    RecycleCalendarAdapter(int mode) {
        calendarDateList = new ArrayList<>();
        selectDay = Calendar.getInstance();
        calendarClickListener = new CalendarClickListener();
        this.mode = mode;
        initCalendarList();
        unReadPos = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_date, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CalendarDateBean mBean = calendarDateList.get(position);

        addZeroToDate(holder, mBean);

        setDateTextColor(holder, mBean);

        holder.lunarDate.setText(mBean.getFestival());

        setSelectedDayBg(holder, mBean);

        setUnRead(holder, mBean);
    }

    private void setUnRead(ViewHolder holder, CalendarDateBean mBean) {
        if (mBean.unReadMessageNum == 0) {
            if (holder.badgeView != null) {
                holder.badgeView.setVisibility(View.GONE);
            }
            return;
        }
        if (holder.badgeView == null) {
            holder.badgeView = new BadgeView(calendarRecyclerView.getContext());
            holder.badgeView.setTargetView(holder.backView);
            holder.badgeView.setBackgroundResource(R.drawable.bageview_back);
        }
        holder.badgeView.setText(mBean.unReadMessageNum + "");
        holder.badgeView.setVisibility(View.VISIBLE);
    }

    private void setSelectedDayBg(ViewHolder holder, CalendarDateBean mBean) {
        if (mBean.isSelectDay(selectDay)) {
            holder.backView.setBackgroundResource(R.drawable.calendar_date_selector);
            holder.backView.setSelected(true);
            preSelectedView = holder.backView;
            holder.calendarDate.setTextColor(Color.WHITE);
            holder.lunarDate.setTextColor(Color.WHITE);
        } else if (mBean.isSelectDay(tempCalendar) && mBean.isCurrentMonth()) {
            holder.backView.setBackgroundResource(R.drawable.today_date_background);
//            preSelectedView = holder.backView;
            holder.backView.setSelected(false);
            holder.calendarDate.setTextColor(Color.WHITE);
            holder.lunarDate.setTextColor(Color.WHITE);
        } else {
            holder.backView.setBackgroundResource(R.drawable.calendar_date_selector);
            holder.backView.setSelected(false);
        }
    }

    private void setDateTextColor(ViewHolder holder, CalendarDateBean mBean) {
        if (mode == MONTH_ADAPTER) {
            if (!mBean.isCurrentMonth()) {
                holder.calendarDate.setTextColor(ResourcesUtil.getInstance().getColor(R.color.text_gray));
            } else {
                holder.calendarDate.setTextColor(ResourcesUtil.getInstance().getColor(R.color.black_lower));
                holder.backView.setOnClickListener(calendarClickListener);
            }
        } else {
            holder.calendarDate.setTextColor(ResourcesUtil.getInstance().getColor(R.color.black_lower));
            holder.lunarDate.setTextColor(ResourcesUtil.getInstance().getColor(R.color.text_gray));
            holder.backView.setOnClickListener(calendarClickListener);
        }
    }

    private void addZeroToDate(ViewHolder holder, CalendarDateBean mBean) {
        if (mBean.getDay() < 10) {
            holder.calendarDate.setText("0" + mBean.getDay());
        } else {
            holder.calendarDate.setText(mBean.getDay() + "");
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    void setCalendarRecyclerView(RecyclerView calendarRecyclerView, ViewGroup calendarScrollView) {
        this.calendarRecyclerView = calendarRecyclerView;
        this.calendarScrollView = calendarScrollView;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView calendarDate, lunarDate;
        View backView;
        BadgeView badgeView;

        public ViewHolder(View itemView) {
            super(itemView);
            backView = itemView.findViewById(R.id.lin_date_back);
            calendarDate = (TextView) itemView.findViewById(R.id.tv_date);
            lunarDate = (TextView) itemView.findViewById(R.id.tv_lunar_date);
        }
    }

    private void initCalendarList() {

        count = mode == MONTH_ADAPTER ? 42 : 7;

        for (int i = 0; i < count; i++) {
            CalendarDateBean mBean = new CalendarDateBean();
            calendarDateList.add(mBean);
        }
    }

    void setMonthDayCalendar(Calendar oneDaycalendar) {
        selectDay.setTimeInMillis(oneDaycalendar.getTimeInMillis());
        calendarRecyclerView.setBackgroundResource(backgrounds[oneDaycalendar.get(Calendar.MONTH)]);
        getMonth(oneDaycalendar);
        notifyItemRangeChanged(0, 42);
        //notifyDataSetChanged();
        //notifyItemRangeRemoved(0,42);
    }

    void setWeekDayCalendar(Calendar oneDayCalendar) {
        selectDay.setTimeInMillis(oneDayCalendar.getTimeInMillis());
        getWeek(oneDayCalendar);
        notifyItemRangeChanged(0, 7);
        //notifyDataSetChanged();
        // notifyItemRangeRemoved(0,7);
    }

    void setCurrentDay() {
        if (!DateUtils.isSameDayOfMillis(tempCalendar.getTimeInMillis(), System.currentTimeMillis())) {
            tempCalendar = Calendar.getInstance();
            notifyDataSetChanged();
        }
    }

    private int currentFirstDayPos;

    private void getWeek(Calendar calendar) {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH);
        c.setTimeInMillis(calendar.getTimeInMillis());
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            c.add(Calendar.DATE, -6);
        } else {
            c.add(Calendar.DATE, -(c.get(Calendar.DAY_OF_WEEK) - 2));//获取本周的周一
        }
        for (int i = 0; i < count; i++) {
            CalendarDateBean mBean = calendarDateList.get(i);
            //CalendarDateBean mBean = new CalendarDateBean();
            if (c.get(Calendar.MONTH) != currentMonth) {
                mBean.setIsCurrentMonth(false);
            } else {
                mBean.setIsCurrentMonth(true);
            }
            mBean.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), c.get(Calendar.DAY_OF_YEAR));
            c.add(Calendar.DATE, 1);
            calendarDateList.add(mBean);
        }
    }

    private void getMonth(Calendar calendar) {
        currentFirstDayPos = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(calendar.getTimeInMillis());
        calendarDateList = new ArrayList<>();
        int currentMonth = c.get(Calendar.MONTH);
        int weeks = c.get(Calendar.WEEK_OF_MONTH);
        int currentYear = c.get(Calendar.YEAR);

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            c.add(Calendar.DATE, -6);
            weeks--;
        } else {
            c.add(Calendar.DATE, -(c.get(Calendar.DAY_OF_WEEK) - 2));//获取本周的周一
        }

        while (true) {
            c.add(Calendar.DATE, 7);
            weeks++;//获取本月的星期数
            if (c.get(Calendar.MONTH) != currentMonth || c.get(Calendar.YEAR) > currentYear) {
                break;
            }
        }

        c.add(Calendar.DATE, -(weeks - 1) * 7);
        if (c.get(Calendar.DATE) == Calendar.MONDAY) {
            c.add(Calendar.DATE, -7);
        }
        for (int i = 0; i < count; i++) {
            CalendarDateBean mBean = new CalendarDateBean();
            if (c.get(Calendar.MONTH) != currentMonth) {
                mBean.setIsCurrentMonth(false);
            } else {
                currentFirstDayPos = currentFirstDayPos == -1 ? i : currentFirstDayPos;
                mBean.setIsCurrentMonth(true);
            }
            mBean.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), c.get(Calendar.DAY_OF_YEAR));
            calendarDateList.add(mBean);
            c.add(Calendar.DATE, 1);
        }
    }

    private CalendarDateBean getItem(int postion) {
        return calendarDateList.get(postion);
    }

    void setDateClickListener(DateClickListener dateClickListener) {
        this.dateClickListener = dateClickListener;
    }

    void clearListener() {
        dateClickListener = null;
        calendarClickListener = null;
    }

    private class CalendarClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (dateClickListener != null) {
                CalendarDateBean mBean = getItem(calendarRecyclerView.getChildAdapterPosition((View) v.getParent()));
                dateClickListener.onOneDayClick(mBean);
                selectDay.set(mBean.getYear(), mBean.getMonth(), mBean.getDay());
                if (preSelectedView != v) {
                    setUnSelectedView(preSelectedView);
                    setSelectedView(v);
                    preSelectedView = v;
                    if (calendarScrollView instanceof CalendarScrollViewNew)
                        ((CalendarScrollViewNew) calendarScrollView).setLeftAndRightSelectedDay(selectDay);
                }
            }
        }
    }

    private ArrayList<Integer> unReadPos;

    void showUnReadMsgCount() {

        if (unReadPos != null) {
            for (int pos : unReadPos) {
                calendarDateList.get(pos).unReadMessageNum = 0;
            }
        }

        ArrayList<UnReadMessageBean> unReadMessageList = TimeLineStoresNew.getInstance().getUnReadMessageList();
        if (unReadMessageList == null || unReadMessageList.size() == 0) {
            assert unReadPos != null;
            if (unReadPos.size() == 1) {
                notifyItemChanged(unReadPos.get(0));
                unReadPos.clear();
            }
            return;
        }
        for (UnReadMessageBean messageBean : unReadMessageList) {
            if (messageBean.type != MessageBean.MESSAGE_TASK_CHAT) {
                return;
            }
//            Log.i(TAG, "showUnReadMsgCount: " + messageBean.taskCreateTime);
            switch (mode) {
                case MONTH_ADAPTER:
                    if (isCurrentMonthMsg(messageBean)) {//当前月的未读消息
                        Log.i(TAG, "showUnReadMsgCount: current month msg");
                        setPosShowUnRead(messageBean);
                    }
                    break;
                case WEEK_ADAPTER:
                    if (isCurrentWeekMsg(messageBean)) {//当前周的未读消息
                        setPosShowUnRead(messageBean);
                    }
                    break;
            }
        }
        if (unReadPos != null) {
            Iterator<Integer> iterator = unReadPos.iterator();
            while (iterator.hasNext()) {
                int pos = iterator.next();
                notifyItemChanged(pos);
                if (calendarDateList.get(pos).unReadMessageNum == 0) {
                    iterator.remove();
                }
            }
        }
    }

    void showUnReadMsgCount(ArrayList<UnReadMessageBean> unReadMessageList) {

        if (unReadPos != null) {
            for (int pos : unReadPos) {
                calendarDateList.get(pos).unReadMessageNum = 0;
            }
        }

//        ArrayList<UnReadMessageBean> unReadMessageList = TimeLineStores.getInstance().getUnReadMessageList();
        if (unReadMessageList == null || unReadMessageList.size() == 0) {
            assert unReadPos != null;
            if (unReadPos.size() == 1) {
                notifyItemChanged(unReadPos.get(0));
                unReadPos.clear();
            }
            return;
        }
        for (UnReadMessageBean messageBean : unReadMessageList) {
            if (messageBean.type != MessageBean.MESSAGE_TASK_CHAT) {
                continue;
            }
//            Log.i(TAG, "showUnReadMsgCount: " + messageBean.taskCreateTime);
            switch (mode) {
                case MONTH_ADAPTER:
                    if (isCurrentMonthMsg(messageBean)) {//当前月的未读消息
                        setPosShowUnRead(messageBean);
                    }
                    break;
                case WEEK_ADAPTER:
                    if (isCurrentWeekMsg(messageBean)) {//当前周的未读消息
                        setPosShowUnRead(messageBean);
                    }
                    break;
            }
        }
        if (unReadPos != null) {
            Iterator<Integer> iterator = unReadPos.iterator();
            while (iterator.hasNext()) {
                int pos = iterator.next();
                notifyItemChanged(pos);
                if (calendarDateList.get(pos).unReadMessageNum == 0) {
                    iterator.remove();
                }
            }
        }
    }

    private void setPosShowUnRead(UnReadMessageBean messageBean) {

        int pos = getOneDayPosition(messageBean.createDay, messageBean.createDayofWeek);
        Log.i(TAG, "setPosShowUnRead: " + pos + "mode = " + mode + "messageBean.createDayOfWeek" + messageBean.createDayofWeek);
        if (pos < 0)
            return;
        CalendarDateBean calendarDateBean = calendarDateList.get(pos);
        calendarDateBean.unReadMessageNum++;
        if (!unReadPos.contains(pos)) {
            unReadPos.add(pos);
        }
    }

    private boolean isCurrentMonthMsg(UnReadMessageBean messageBean) {
        int currentMonth = selectDay.get(Calendar.MONTH);
        int currentYear = selectDay.get(Calendar.YEAR);
        return messageBean.createMonth == currentMonth && messageBean.createYear == currentYear;
    }

    private boolean isCurrentWeekMsg(UnReadMessageBean messageBean) {
        CalendarDateBean firstDay = calendarDateList.get(0);
        CalendarDateBean lastDay = calendarDateList.get(6);
        boolean res = false;
        if (messageBean.createYear == firstDay.getYear() && messageBean.createYear == lastDay.getYear()) {
            res = messageBean.createDayOfYear >= firstDay.dayOfYear && messageBean.createDayOfYear <= lastDay.dayOfYear;
        } else if (messageBean.createDayOfYear >= firstDay.dayOfYear && messageBean.createYear == firstDay.getYear()) {
            res = true;
        } else if (messageBean.createDayOfYear <= lastDay.dayOfYear && messageBean.createYear == lastDay.getYear()) {
            res = true;
        }
//        Log.i(TAG, "isCurrentWeekMsg: firstDay"+firstDay.toString());
//        Log.i(TAG, "isCurrentWeekMsg: lastDay"+lastDay.toString());
//        Log.i(TAG, "isCurrentWeekMsg: " + res + messageBean.toString());
        return res;
    }

    boolean isCurrentWeekDay(Calendar calendar) {
        CalendarDateBean firstDay = calendarDateList.get(0);
        CalendarDateBean lastDay = calendarDateList.get(6);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
//        Log.i(TAG, firstDay.toString() + "" + lastDay.toString() + "day of year" + dayOfYear);
        if (year == firstDay.getYear() && year == lastDay.getYear()) {
            return dayOfYear >= firstDay.dayOfYear && dayOfYear <= lastDay.dayOfYear;
        } else if (dayOfYear >= firstDay.dayOfYear && year == firstDay.getYear()) {
            return true;
        } else if (dayOfYear <= lastDay.dayOfYear && year == lastDay.getYear()) {
            return true;
        }
        return false;
    }

    boolean isCurrentMonthDay(Calendar calendar) {
        int currentMonth = selectDay.get(Calendar.MONTH);
        int currentYear = selectDay.get(Calendar.YEAR);
        return calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear;
    }

    private void setSelectedView(View v) {
        int pos = calendarRecyclerView.getChildAdapterPosition((View) v.getParent());
        CalendarDateBean mBean = getItem(pos);
        if (mBean.isSelectDay(tempCalendar)) {
            Log.i(TAG, "setSelectedView: set selected view" + mBean.toString());
            Log.i(TAG, "setSelectedView: set today selected view");
            v.setBackgroundResource(R.drawable.calendar_date_selector);
        }
        v.setSelected(true);
        TextView dateView = (TextView) v.findViewById(R.id.tv_date);
        TextView lunarView = (TextView) v.findViewById(R.id.tv_lunar_date);
        dateView.setTextColor(Color.WHITE);
        lunarView.setTextColor(Color.WHITE);
    }

    private void setUnSelectedView(View preSelectedView) {
        int pos = calendarRecyclerView.getChildAdapterPosition((View) preSelectedView.getParent());
        CalendarDateBean mBean = getItem(pos);
        TextView dateView = (TextView) preSelectedView.findViewById(R.id.tv_date);
        TextView lunarView = (TextView) preSelectedView.findViewById(R.id.tv_lunar_date);
        if (mBean.isSelectDay(tempCalendar)) {
            preSelectedView.setBackgroundResource(R.drawable.today_date_background);
            Log.i(TAG, "setUnSelectedView: set today un selected view");
            dateView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
            lunarView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
            return;
        }
        preSelectedView.setSelected(false);
        dateView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.black_lower));
        lunarView.setTextColor(ResourcesUtil.getInstance().getColor(R.color.text_gray));
    }

    void setSelectDay(Calendar calendar) {
        if (calendar.get(Calendar.DATE) == selectDay.get(Calendar.DATE) &&
                calendar.get(Calendar.MONTH) == selectDay.get(Calendar.MONTH) &&
                calendar.get(Calendar.YEAR) == selectDay.get(Calendar.YEAR))
            return;
        selectDay.setTimeInMillis(calendar.getTimeInMillis());
        int pos = getOneDayPosition(calendar);
        ViewHolder viewHolder = (ViewHolder) calendarRecyclerView.findViewHolderForAdapterPosition(pos);
        setUnSelectedView(preSelectedView);
        setSelectedView(viewHolder.backView);
        preSelectedView = viewHolder.backView;
    }

    private int getOneDayPosition(Calendar calendar) {
        int date = calendar.get(Calendar.DATE);
        return mode == MONTH_ADAPTER ? currentFirstDayPos + date - 1 : calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2;
    }

    private int getOneDayPosition(int date, int createDayofWeek) {
//        int firstDay = calendarDateList.get(0).getDay();
        return mode == MONTH_ADAPTER ? currentFirstDayPos + date - 1 : (createDayofWeek == 1 ? 6 : createDayofWeek - 2);
    }

    private Calendar tempCalendar = Calendar.getInstance();
}
