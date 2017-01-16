package com.inspur.playwork.view.timeline;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.UnReadMessageBean;
import com.inspur.playwork.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Fan on 15-12-25.
 */
public class CalendarScrollViewNew extends ViewPager implements CalendarViewPagerAdapter.InitItemListener {


    private static final String TAG = "CalendarScrollViewNew";


    private static final int MONTH_MODE = 0;
    private static final int WEEK_MODE = 1;

    private int mode;

    private ArrayList<View> recyclerViews;


    private Calendar selectedDay;

    private int middleViewPagerPos;

    private OnCalendarChangeListener dateChangeListener;

    private boolean needNotifyDateChange = true;

    private PageChangeListener listener;

    public interface OnCalendarChangeListener {
        void onDateChanged(Calendar calendar);
    }

    public CalendarScrollViewNew(Context context) {
        this(context, null);
    }

    public CalendarScrollViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CalendarScrollViewNew, 0, 0);
        getCustomAttrs(a);
        a.recycle();

        setWillNotDraw(true);

        initData();
        if (mode == MONTH_MODE) {
            initMonthRecyclerViews();
        } else {
            initWeekRecyclerViews();
        }
//        initViewPager(getContext());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) height = h;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void initData() {
        selectedDay = Calendar.getInstance();
        recyclerViews = new ArrayList<>();
        listener = new PageChangeListener();
    }

    public void init(long time) {
        selectedDay.setTimeInMillis(time);
        initViewPagerNew();
    }

    private void initWeekRecyclerViews() {
        for (int i = 0; i < 5; i++) {
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_reclyerview, this, false);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            RecycleCalendarAdapter adapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.WEEK_ADAPTER);
            adapter.setCalendarRecyclerView(recyclerView, this);
            recyclerViews.add(recyclerView);
            recyclerView.setAdapter(adapter);
        }
    }

    private void initMonthRecyclerViews() {
        for (int i = 0; i < 5; i++) {
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_reclyerview, this, false);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            RecycleCalendarAdapter adapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.MONTH_ADAPTER);
            adapter.setCalendarRecyclerView(recyclerView, this);
            recyclerViews.add(recyclerView);
            recyclerView.setAdapter(adapter);
        }
    }

    public void initViewPagerNew() {
        CalendarViewPagerAdapter adapter = new CalendarViewPagerAdapter();
        adapter.setViewList(recyclerViews);
        adapter.setListener(this);
        addOnPageChangeListener(listener);
        setAdapter(adapter);
        middleViewPagerPos = (Integer.MAX_VALUE - 2) / 2;
        Log.i(TAG, "initViewPagerNew: " + middleViewPagerPos);
        setCurrentItem(middleViewPagerPos);
    }

    public void setDateChangeListener(OnCalendarChangeListener dateChangeListener) {
        this.dateChangeListener = dateChangeListener;
    }

    private boolean isSameWeek(Calendar c) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(selectedDay.getTimeInMillis());
        int year = c.get(Calendar.YEAR);
        int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek == 1 ? 8 : dayOfWeek;
        day.add(Calendar.DATE, 2 - dayOfWeek);
        int mondayYear = day.get(Calendar.YEAR);
        int mondatyDayOfYear = day.get(Calendar.DAY_OF_YEAR);
        day.add(Calendar.DATE, 6);
        int sundayDayOfYear = day.get(Calendar.DAY_OF_YEAR);
        int sundayYear = day.get(Calendar.YEAR);

        if (year == mondayYear && year == sundayYear) {
            return dayOfYear >= mondatyDayOfYear && dayOfYear <= sundayDayOfYear;
        } else if (dayOfYear >= mondatyDayOfYear && year == mondayYear) {
            return true;
        } else if (dayOfYear <= sundayDayOfYear && year == sundayYear) {
            return true;
        }
        return false;
    }

    private void getCustomAttrs(TypedArray a) {
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CalendarScrollViewNew_calendar_mode:
                    int val = a.getInt(attr, 0);
                    switch (val) {
                        case 0:
                            mode = MONTH_MODE;
                            break;
                        case 1:
                            mode = WEEK_MODE;
                            break;
                    }
                    break;
            }
        }
    }


    public void showUnReadMessage(ArrayList<UnReadMessageBean> unReadMessageList) {
        int currentIndxe = middleViewPagerPos % 5;
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get(currentIndxe)).getAdapter()).showUnReadMsgCount(unReadMessageList);
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5)).getAdapter()).showUnReadMsgCount(unReadMessageList);
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5)).getAdapter()).showUnReadMsgCount(unReadMessageList);
    }

    public void showUnReadMessage() {
        int currentIndxe = middleViewPagerPos % 5;
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get(currentIndxe)).getAdapter()).showUnReadMsgCount();
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5)).getAdapter()).showUnReadMsgCount();
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5)).getAdapter()).showUnReadMsgCount();
    }

    public void setCurrentDay() {
        int currentIndxe = middleViewPagerPos % 5;
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get(currentIndxe)).getAdapter()).setCurrentDay();
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5)).getAdapter()).setCurrentDay();
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5)).getAdapter()).setCurrentDay();
    }

    public void setDateToSomeDay(Calendar calendar) {

        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        if (isSameSelectDay(calendar)) {
            return;
        }
        if (mode == MONTH_MODE) {
            if (isSameMonth(calendar)) {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);
            } else {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, -1);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, 2);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, -1);

                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);

                showUnReadMessage();
            }
        } else {
            if (isSameWeek(calendar)) {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);
            } else {
                setWeekCalendarWhenMonthClick(calendar);
            }
        }
    }

    public void setWeekCalendarWhenMonthClick(Calendar day) {

        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day.getTimeInMillis());
        if (isSameMonth(day) && isSameWeek(day)) {
            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(c);
            setLeftAndRightSelectedDay(c);
        } else {
            ((RecycleCalendarAdapter) middleView.getAdapter()).setWeekDayCalendar(c);
            c.add(Calendar.DATE, -7);
            ((RecycleCalendarAdapter) leftView.getAdapter()).setWeekDayCalendar(c);
            c.add(Calendar.DATE, 14);
            ((RecycleCalendarAdapter) rightView.getAdapter()).setWeekDayCalendar(c);
            c.add(Calendar.DATE, -7);

            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(c);
            setLeftAndRightSelectedDay(c);
            showUnReadMessage();
        }
    }


    public void setMonthCalendarWhenWeekClick(Calendar day) {
        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        if (isSameMonth(day)) {
            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(day);
            setLeftAndRightSelectedDay(day);
        } else {
            Log.i(TAG, "setMonthCalendarWhenWeekClick:  before" + System.currentTimeMillis());
            ((RecycleCalendarAdapter) middleView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, -1);
            ((RecycleCalendarAdapter) leftView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, 2);
            ((RecycleCalendarAdapter) rightView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, -1);
            Log.i(TAG, "setMonthCalendarWhenWeekClick: after" + System.currentTimeMillis());

            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(day);
            setLeftAndRightSelectedDay(day);
            showUnReadMessage();
        }
    }

    public void setLeftAndRightSelectedDay(Calendar day) {
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        Calendar selectedDay = Calendar.getInstance();
        selectedDay.setTimeInMillis(day.getTimeInMillis());
        this.selectedDay.setTimeInMillis(day.getTimeInMillis());
        if (mode == MONTH_MODE) {
            selectedDay.add(Calendar.MONTH, -1);
            ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(selectedDay);
            selectedDay.add(Calendar.MONTH, 2);
            ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(selectedDay);
        } else {
            selectedDay.add(Calendar.DATE, -7);
            ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(selectedDay);
            selectedDay.add(Calendar.DATE, 14);
            ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(selectedDay);
        }
    }

    private boolean isSameSelectDay(Calendar calendar) {
        return selectedDay.get(Calendar.DATE) == calendar.get(Calendar.DATE) &&
                selectedDay.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                selectedDay.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    }

    private boolean isSameMonth(Calendar day) {
        return selectedDay.get(Calendar.MONTH) == day.get(Calendar.MONTH) && selectedDay.get(Calendar.YEAR) == day.get(Calendar.YEAR);
    }


    private void onDateChanged(Calendar selectedDay) {
        if (dateChangeListener != null) {
            Log.e(TAG, "onDateChanged() called with: " + "selectedDay = [" + DateUtils.getCalendarAllText(selectedDay) + "]");
            dateChangeListener.onDateChanged(selectedDay);
        }
    }

    private Calendar tempCalendar = Calendar.getInstance();

    public void moveToNext() {

        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        tempCalendar.setTimeInMillis(selectedDay.getTimeInMillis());
        tempCalendar.add(Calendar.DATE, 1);
        if (mode == WEEK_MODE) {
            RecycleCalendarAdapter adapter = (RecycleCalendarAdapter) middleView.getAdapter();
            if (adapter.isCurrentWeekDay(tempCalendar)) {
                adapter.setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.DATE, -7);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.DATE, 14);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(tempCalendar);
                selectedDay.add(Calendar.DATE, 1);
            } else {
//                needNotifyDateChange = false;
                needNotifyDateChange = false;
                setCurrentItem(middleViewPagerPos + 1, true);
            }
        } else {
            RecycleCalendarAdapter adapter = (RecycleCalendarAdapter) middleView.getAdapter();
            if (adapter.isCurrentMonthDay(tempCalendar)) {
                adapter.setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.MONTH, -1);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.MONTH, 2);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(tempCalendar);
                selectedDay.add(Calendar.DATE, 1);
            } else {
                needNotifyDateChange = false;
                setCurrentItem(middleViewPagerPos + 1, true);
            }
        }
    }

    public void moveToPrevious() {
//        int middleIndex = middleViewPagerPos % 5;
        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);

        Log.e(TAG, "moveToPrevious: " + DateUtils.getCalendarAllText(selectedDay));
        tempCalendar.setTimeInMillis(selectedDay.getTimeInMillis());
        tempCalendar.add(Calendar.DATE, -1);
        if (mode == WEEK_MODE) {
            RecycleCalendarAdapter adapter = (RecycleCalendarAdapter) middleView.getAdapter();
            if (adapter.isCurrentWeekDay(tempCalendar)) {
                adapter.setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.DATE, -7);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.DATE, 14);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(tempCalendar);
                selectedDay.add(Calendar.DATE, -1);
            } else {
                needNotifyDateChange = false;
                setCurrentItem(middleViewPagerPos - 1, true);
            }
        } else {
            RecycleCalendarAdapter adapter = (RecycleCalendarAdapter) middleView.getAdapter();
            if (adapter.isCurrentMonthDay(tempCalendar)) {
                adapter.setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.MONTH, -1);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setSelectDay(tempCalendar);
                tempCalendar.add(Calendar.MONTH, 2);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setSelectDay(tempCalendar);
                selectedDay.add(Calendar.DATE, -1);
            } else {
                needNotifyDateChange = false;
                setCurrentItem(middleViewPagerPos - 1, true);
            }
        }
    }

    private void setMiddleViewSelect() {
        selectedDay.setTimeInMillis(tempCalendar.getTimeInMillis());
        ((RecycleCalendarAdapter) ((RecyclerView) recyclerViews.get(middleViewPagerPos % 5)).getAdapter()).setSelectDay(tempCalendar);
        needNotifyDateChange = true;
    }


    @Override
    public void onInitItem(View view, int pos) {
        int currentIndex = middleViewPagerPos;
        RecycleCalendarAdapter adapter = (RecycleCalendarAdapter) ((RecyclerView) view).getAdapter();
        if (mode == WEEK_MODE) {
            selectedDay.add(Calendar.DATE, (pos - currentIndex) * 7);
            adapter.setWeekDayCalendar(selectedDay);
            selectedDay.add(Calendar.DATE, (currentIndex - pos) * 7);
        } else {
            selectedDay.add(Calendar.MONTH, pos - currentIndex);
            adapter.setMonthDayCalendar(selectedDay);
            selectedDay.add(Calendar.MONTH, currentIndex - pos);
        }
        adapter.showUnReadMsgCount();
    }

    private class PageChangeListener extends SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            int offset = position - middleViewPagerPos;
            if (mode == MONTH_MODE) {
                selectedDay.add(Calendar.MONTH, offset);
            } else {
                selectedDay.add(Calendar.DATE, offset * 7);
            }

            Log.i(TAG, "onPageSelected: " + position);
            middleViewPagerPos = position;

            if (needNotifyDateChange) {
                Calendar tempCalendar = Calendar.getInstance();
                tempCalendar.setTimeInMillis(selectedDay.getTimeInMillis());
                onDateChanged(tempCalendar);
            } else {
                setMiddleViewSelect();
            }
        }
    }

    public void setCalendarClickListener(RecycleCalendarAdapter.DateClickListener listener) {
        Log.i(TAG, "setCalendarClickListener: " + middleViewPagerPos);
        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);
        RecyclerView rrightView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 2) % 5);
        RecyclerView lleftView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 2) % 5);

        ((RecycleCalendarAdapter) middleView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) leftView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) rightView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) rrightView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) lleftView.getAdapter()).setDateClickListener(listener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTimeInMillis(selectedDay.getTimeInMillis());
        onDateChanged(tempCalendar);
//        initViewPager(getContext());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState: ----");
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Log.i(TAG, "onRestoreInstanceState: =====");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    public void clearListener() {
        dateChangeListener = null;
        RecyclerView middleView = (RecyclerView) recyclerViews.get(middleViewPagerPos % 5);
        RecyclerView leftView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 1) % 5);
        RecyclerView rightView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 1) % 5);
        RecyclerView rrightView = (RecyclerView) recyclerViews.get((middleViewPagerPos - 2) % 5);
        RecyclerView lleftView = (RecyclerView) recyclerViews.get((middleViewPagerPos + 2) % 5);

        ((RecycleCalendarAdapter) middleView.getAdapter()).clearListener();
        ((RecycleCalendarAdapter) leftView.getAdapter()).clearListener();
        ((RecycleCalendarAdapter) rightView.getAdapter()).clearListener();
        ((RecycleCalendarAdapter) rrightView.getAdapter()).clearListener();
        ((RecycleCalendarAdapter) lleftView.getAdapter()).clearListener();
    }
    /*
    public Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState: ========");
        Parcelable superState = super.onSaveInstanceState();
        CalendarScrollViewState savedState = new CalendarScrollViewState(superState);
        savedState.selectDayTime = selectedDay.getTimeInMillis();
        return savedState;
    }

    public void onRestoreInstanceState(Parcelable state) {
        CalendarScrollViewState savedState = (CalendarScrollViewState) state;
        Log.i(TAG, "onRestoreInstanceState: " + savedState);
        super.onRestoreInstanceState(savedState.getSuperState());
        if (selectedDay == null)
            selectedDay = Calendar.getInstance();
        selectedDay.setTimeInMillis(savedState.selectDayTime);
    }

    private static class CalendarScrollViewState extends BaseSavedState {


        public long selectDayTime;

        public CalendarScrollViewState(Parcelable superState) {
            super(superState);
        }


        public static final Parcelable.Creator<CalendarScrollViewState> CREATOR = new Creator<CalendarScrollViewState>() {
            public CalendarScrollViewState createFromParcel(Parcel source) {
                return new CalendarScrollViewState(source);
            }

            @Override
            public CalendarScrollViewState[] newArray(int size) {
                return new CalendarScrollViewState[size];
            }
        };


        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectDayTime);
        }

        public CalendarScrollViewState(Parcel source) {
            super(source);
            selectDayTime = source.readLong();
        }

        public String toString() {
            return "CalendarScrollViewState{" +
                    "selectDayTime=" + selectDayTime +
                    "superState=" + getSuperState().toString() +
                    '}';
        }
    }*/
}
