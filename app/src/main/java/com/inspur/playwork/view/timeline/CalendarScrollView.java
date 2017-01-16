package com.inspur.playwork.view.timeline;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

import java.util.Calendar;

/**
 * Created by Fan on 15-9-8.
 */
public class CalendarScrollView extends ViewGroup {

    private static final String TAG = "calendarScrollViewFan";

    private static final int MONTH_MODE = 0;
    private static final int WEEK_MODE = 1;
    private static final int MAX_SCROLL_TIME = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;

    private int mode;

    private RecyclerView middleView, leftView, rightView;

    private Calendar selectedDay;

    private Scroller scroller;

    private boolean isNeedIntercept;
    private boolean notIntercept;

    private int touchSlop;
    private int minFlingDistance;

    private int maxFlingVelocity, minFlingVelocity;

    private VelocityTracker velocityTracker;

    private int width;

    private int xScrollStart;

    private OnCalendarChangeListener dateChangeListener;

    private boolean needNotifyDateChange = true;

    public interface OnCalendarChangeListener {
        void onDateChanged(Calendar calendar);
    }

    public CalendarScrollView(Context context) {
        this(context, null);
    }

    public CalendarScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CalendarScrollView, defStyleAttr, 0);
        getCustomAttrs(a);
        a.recycle();

        setWillNotDraw(true);

        initData();
        initCalendarViews();
    }

    private void initData() {
        selectedDay = Calendar.getInstance();
        scroller = new Scroller(getContext(), sInterpolator);
        touchSlop = DeviceUtil.getFlipDistance(getContext());
        maxFlingVelocity = DeviceUtil.getMaxFlingVelocity(getContext());
        minFlingVelocity = DeviceUtil.getMinFlingVelocity(getContext());
        minFlingDistance = DeviceUtil.dpTopx(getContext(), MIN_DISTANCE_FOR_FLING);
    }

    private void initCalendarViews() {
        if (middleView == null) {
            middleView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_reclyerview, this, false);
            leftView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_reclyerview, this, false);
            rightView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_reclyerview, this, false);
            if (mode == MONTH_MODE) {
//                RecyclerView.LayoutManager layoutManager1 = new CalendarGridLayoutManager(getContext(), 7);
//                RecyclerView.LayoutManager layoutManager2 = new CalendarGridLayoutManager(getContext(), 7);
//                RecyclerView.LayoutManager layoutManager3 = new CalendarGridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager2 = new GridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager3 = new GridLayoutManager(getContext(), 7);
//                ((CalendarGridLayoutManager) layoutManager1).setCount(6);
//                ((CalendarGridLayoutManager) layoutManager2).setCount(6);
//                ((CalendarGridLayoutManager) layoutManager3).setCount(6);
                middleView.setLayoutManager(layoutManager1);
                rightView.setLayoutManager(layoutManager2);
                leftView.setLayoutManager(layoutManager3);

                middleView.setHasFixedSize(true);
                rightView.setHasFixedSize(true);
                leftView.setHasFixedSize(true);
                setMonthAdapter();
            } else if (mode == WEEK_MODE) {
//                RecyclerView.LayoutManager layoutManager1 = new CalendarGridLayoutManager(getContext(), 7);
//                RecyclerView.LayoutManager layoutManager2 = new CalendarGridLayoutManager(getContext(), 7);
//                RecyclerView.LayoutManager layoutManager3 = new CalendarGridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager2 = new GridLayoutManager(getContext(), 7);
                RecyclerView.LayoutManager layoutManager3 = new GridLayoutManager(getContext(), 7);
//                ((CalendarGridLayoutManager) layoutManager1).setCount(1);
//                ((CalendarGridLayoutManager) layoutManager2).setCount(1);
//                ((CalendarGridLayoutManager) layoutManager3).setCount(1);
                middleView.setLayoutManager(layoutManager1);
                rightView.setLayoutManager(layoutManager2);
                leftView.setLayoutManager(layoutManager3);

                /**
                 * 虽然不知道这是干什么的，但是加上就对了
                 */
                middleView.setHasFixedSize(true);
                rightView.setHasFixedSize(true);
                leftView.setHasFixedSize(true);
                setWeekAdapter();
            }
            addViewToParent();
        }
    }

    private void addViewToParent() {
        addView(leftView);
        addView(middleView);
        addView(rightView);
    }

    private void setMonthAdapter() {
        RecycleCalendarAdapter rightAdapter, middleAdapter, leftAdapter;
        rightAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.MONTH_ADAPTER);
        middleAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.MONTH_ADAPTER);
        leftAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.MONTH_ADAPTER);

        leftAdapter.setCalendarRecyclerView(leftView, this);
        middleAdapter.setCalendarRecyclerView(middleView, this);
        rightAdapter.setCalendarRecyclerView(rightView, this);

        middleAdapter.setMonthDayCalendar(selectedDay);
        selectedDay.add(Calendar.MONTH, 1);
        rightAdapter.setMonthDayCalendar(selectedDay);
        selectedDay.add(Calendar.MONTH, -2);
        leftAdapter.setMonthDayCalendar(selectedDay);
        selectedDay.add(Calendar.MONTH, 1);
        leftView.setAdapter(leftAdapter);
        middleView.setAdapter(middleAdapter);
        rightView.setAdapter(rightAdapter);

    }

    private void setWeekAdapter() {
        RecycleCalendarAdapter rightAdapter, middleAdapter, leftAdapter;
        rightAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.WEEK_ADAPTER);
        middleAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.WEEK_ADAPTER);
        leftAdapter = new RecycleCalendarAdapter(RecycleCalendarAdapter.WEEK_ADAPTER);

        leftAdapter.setCalendarRecyclerView(leftView, this);
        middleAdapter.setCalendarRecyclerView(middleView, this);
        rightAdapter.setCalendarRecyclerView(rightView, this);

        middleAdapter.setWeekDayCalendar(selectedDay);
        selectedDay.add(Calendar.DATE, 7);
        rightAdapter.setWeekDayCalendar(selectedDay);
        selectedDay.add(Calendar.DATE, -14);
        leftAdapter.setWeekDayCalendar(selectedDay);
        selectedDay.add(Calendar.DATE, 7);
        leftView.setAdapter(leftAdapter);
        middleView.setAdapter(middleAdapter);
        rightView.setAdapter(rightAdapter);

    }

    private void getCustomAttrs(TypedArray a) {
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CalendarScrollView_calendar_mode:
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
            height = Math.min(height, child.getMeasuredHeight());
        }
        this.width = width;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (changed) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int cl = -width + i * width;
                int ct = 0;
                int cr = cl + width;
                child.layout(cl, ct, cr, height);
            }
        }
    }

    private float lastXPos, lastYPos;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();

        if (!scroller.isFinished())
            return false;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isNeedIntercept = false;
            notIntercept = false;
            recycleVelocityTracker();
            return isNeedIntercept;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            if (isNeedIntercept) {
                return true;
            }
            if (notIntercept) {
                return false;
            }
        }


        switch (action) {
            case MotionEvent.ACTION_DOWN:
 /*               if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }*/
                createVelocityTracker();
                lastXPos = ev.getX();
                lastYPos = ev.getY();
                xScrollStart = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - lastXPos;
                float xDiff = Math.abs(dx);
                float dy = ev.getY() - lastYPos;
                float yDiff = Math.abs(dy);
                if (xDiff * 0.5f > yDiff && xDiff > touchSlop) {
                    isNeedIntercept = true;
                    float scrollX = getScrollX() - dx;
                    scrollTo((int) scrollX, getScrollY());
                    lastYPos = ev.getY();
                    lastXPos = ev.getX();
                    lastXPos += scrollX - (int) scrollX;
                } else if (yDiff > touchSlop) {
                    notIntercept = true;
                }
                break;
        }
        if (velocityTracker != null)
            velocityTracker.addMovement(ev);
        return isNeedIntercept;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getActionMasked();
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    return false;
                    //scroller.abortAnimation();
                }
                event.getX();
                event.getY();
                xScrollStart = getScrollX();
                createVelocityTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastXPos;
                float dy = event.getY() - lastYPos;
                float xDiff = Math.abs(dx);
                float yDiff = Math.abs(dy);
                if (xDiff > yDiff) {
                    isNeedIntercept = true;
                    float scrollX = getScrollX() - dx;
                    scrollTo((int) scrollX, getScrollY());
                    lastYPos = event.getY();
                    lastXPos = event.getX();
                    lastXPos += scrollX - (int) scrollX;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:

                if (isNeedIntercept) {
                    int xVelocity = 0;
                    if (velocityTracker != null) {
                        velocityTracker.computeCurrentVelocity(1000);
                        xVelocity = (int) velocityTracker.getXVelocity();
                        if (xVelocity != 0)
                            xVelocity = (xVelocity / Math.abs(xVelocity))
                                    * Math.min(Math.abs(xVelocity), maxFlingVelocity);
                    }

                    int xScrollDis = getScrollX() - xScrollStart;
                    int xScrollDiff = Math.abs(xScrollDis);

                    if (xScrollDiff > minFlingDistance) {
                        if (xScrollDis < 0) {//右滑
                            rightScroll(xVelocity, xScrollDiff);
                        } else {//左滑
                            leftScroll(xVelocity, xScrollDiff);
                        }
                    } else {
                        int xEnd = xScrollStart;
                        int yEnd = 0;
                        smoothScrollTo(xEnd, yEnd, 0);
                    }
                    isNeedIntercept = false;
                    notIntercept = false;
                    recycleVelocityTracker();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void leftScroll(int xVelocity, int xScrollDiff) {
        int xEnd;
        int yEnd;
        if (xVelocity < -minFlingVelocity) {
            xEnd = getScrollX() + (width - xScrollDiff);
            yEnd = 0;
        } else {
            xEnd = getScrollX() - xScrollDiff;
            yEnd = 0;
        }
        smoothScrollTo(xEnd, yEnd, xVelocity);
    }


    public void setDateToSomeDay(Calendar calendar) {

        if (isSameSelectDay(calendar)) {
            return;
        }
        if (mode == MONTH_MODE) {
            if (isSameMonth(calendar)) {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);
//                setWeekCalendarWhenMonthClick(calendar);
            } else {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, -1);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, 2);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setMonthDayCalendar(calendar);
                calendar.add(Calendar.MONTH, -1);

                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);

//                setWeekCalendarWhenMonthClick(calendar);

                showUnReadMessage();
            }
        } else {
            if (isSameWeek(calendar)) {
                ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(calendar);
                setLeftAndRightSelectedDay(calendar);
//                setMonthCalendarWhenWeekClick(calendar);
            } else {
                setWeekCalendarWhenMonthClick(calendar);
//                setMonthCalendarWhenWeekClick(calendar);
            }
        }
    }

    private boolean isSameSelectDay(Calendar calendar) {
        return selectedDay.get(Calendar.DATE) == calendar.get(Calendar.DATE) &&
                selectedDay.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                selectedDay.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    }


    public void rightScroll(int xVelocity, int xScrollDiff) {
        int xEnd;
        int yEnd;
        if (xVelocity > minFlingVelocity) {
            xEnd = getScrollX() - (width - xScrollDiff);
            yEnd = 0;
        } else {
            xEnd = getScrollX() + xScrollDiff;
            yEnd = 0;
        }
        smoothScrollTo(xEnd, yEnd, xVelocity);
    }


    private void smoothScrollTo(int x, int y, int velocity) {
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            return;
        }

        int halfWidth = width / 2;
        float distanceFactor = Math.min(1.0f, 1.0f * Math.abs(dx) / width);
        float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceFactor);

        int duration;
        if (velocity != 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            duration = (int) ((distanceFactor + 1) * 100);
        }
        duration = Math.min(duration > 250 ? duration : 250, MAX_SCROLL_TIME);
        scroller.startScroll(sx, sy, dx, dy, duration);
        invalidateView();
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset() && !scroller.isFinished()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();

            if (oldX != x || oldY != y) {
                scrollTo(scroller.getCurrX(), scroller.getCurrY());
            }
            invalidateView();
        }
    }

    private int scrollDistance = 0;

    private int lastScrollX = 0;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        scrollDistance += l - oldl;
        if (scrollDistance % width == 0) {
            if ((l - lastScrollX) == width) { //是向左跨过了一页
                doAfterLeftScrollOnePage();
            } else if ((l - lastScrollX) == -width) {//向右跨过了一页
                doAfterRightScrollOnePage();
            }
            lastScrollX = l;
        }
    }

    private void doAfterRightScrollOnePage() {
        RecyclerView temp1, temp2;
        temp1 = middleView;
        temp2 = leftView;
        middleView = temp2;
        leftView = rightView;
        rightView = temp1;
        leftView.setX(middleView.getX() - width);
        //removeView(leftView);
        //leftView.setVisibility(View.GONE);
        switch (mode) {
            case MONTH_MODE: {
                selectedDay.add(Calendar.MONTH, -2);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setMonthDayCalendar(selectedDay);
                selectedDay.add(Calendar.MONTH, 1);
//                ((RecycleCalendarAdapter) leftView.getAdapter()).showUnReadMsgCount();
                if (needNotifyDateChange) {
                    onDateChanged(selectedDay);
                } else {
                    setMiddleViewSelect();
                }
            }
            break;
            case WEEK_MODE: {
                selectedDay.add(Calendar.DATE, -14);
                ((RecycleCalendarAdapter) leftView.getAdapter()).setWeekDayCalendar(selectedDay);
                selectedDay.add(Calendar.DATE, 7);
//                ((RecycleCalendarAdapter) leftView.getAdapter()).showUnReadMsgCount();
                if (needNotifyDateChange) {
                    onDateChanged(selectedDay);
                } else {
                    setMiddleViewSelect();
                }
            }
            break;
        }
        //addView(leftView);
        //  leftView.setVisibility(View.VISIBLE);
        //  leftView.layout(middleView.getLeft() - width, 0, middleView.getLeft(), middleView.getBottom());
    }

    private void doAfterLeftScrollOnePage() {
        RecyclerView temp1, temp2;
        temp1 = middleView;
        temp2 = leftView;
        leftView = temp1;
        middleView = rightView;
        rightView = temp2;
        rightView.setX(middleView.getX() + width);
        //removeView(rightView);
        //rightView.setVisibility(View.GONE);
        switch (mode) {
            case MONTH_MODE: {
                selectedDay.add(Calendar.MONTH, 2);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setMonthDayCalendar(selectedDay);
                selectedDay.add(Calendar.MONTH, -1);
//                ((RecycleCalendarAdapter) rightView.getAdapter()).showUnReadMsgCount();
                if (needNotifyDateChange) {
                    onDateChanged(selectedDay);
                } else {
                    // tempCalendar.setTimeInMillis(selectedDay.getTimeInMillis());
                    setMiddleViewSelect();
                }
            }
            break;
            case WEEK_MODE: {
                selectedDay.add(Calendar.DATE, 14);
                ((RecycleCalendarAdapter) rightView.getAdapter()).setWeekDayCalendar(selectedDay);
                selectedDay.add(Calendar.DATE, -7);
//                ((RecycleCalendarAdapter) rightView.getAdapter()).showUnReadMsgCount();
                if (needNotifyDateChange) {
                    onDateChanged(selectedDay);
                } else {
                    setMiddleViewSelect();
                }
            }
            break;
        }
        //addView(rightView);
        // rightView.setVisibility(View.VISIBLE);
        // rightView.layout(middleView.getRight(), 0, middleView.getRight() + width, middleView.getBottom());
    }

    private void setMiddleViewSelect() {
        selectedDay.setTimeInMillis(tempCalendar.getTimeInMillis());
        ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(tempCalendar);
        needNotifyDateChange = true;
    }

    private void onDateChanged(Calendar selectedDay) {
        if (dateChangeListener != null) {
            dateChangeListener.onDateChanged(selectedDay);
        }
    }

    private Calendar tempCalendar = Calendar.getInstance();

    public void moveToNext() {
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
                needNotifyDateChange = false;
                rightScroll(0, width);
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
                rightScroll(0, width);
            }
        }
    }

    public void moveToPrevious() {
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
                leftScroll(0, width);
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
                leftScroll(0, width);
            }
        }
    }


    private void invalidateView() {
        if (Build.VERSION.SDK_INT >= 16)
            postInvalidateOnAnimation();
        else
            postInvalidate();
    }

    private void createVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    public void setDateChangeListener(OnCalendarChangeListener dateChangeListener) {
        this.dateChangeListener = dateChangeListener;
    }

    public void setLeftAndRightSelectedDay(Calendar day) {
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

    public void setWeekCalendarWhenMonthClick(Calendar day) {
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

    public void showUnReadMessage() {
//        ((RecycleCalendarAdapter) middleView.getAdapter()).showUnReadMsgCount();
//        ((RecycleCalendarAdapter) leftView.getAdapter()).showUnReadMsgCount();
//        ((RecycleCalendarAdapter) rightView.getAdapter()).showUnReadMsgCount();
    }

    private boolean isSameWeek(Calendar c) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(c.getTimeInMillis());
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek == 1 ? 8 : dayOfWeek;
        day.add(Calendar.DATE, 2 - dayOfWeek);
        int mondayDate = day.get(Calendar.DATE);
        day.add(Calendar.DATE, 6);
        int sundayDate = day.get(Calendar.DATE);
        return selectedDay.get(Calendar.DATE) >= mondayDate && selectedDay.get(Calendar.DATE) <= sundayDate;
    }

    public void setMonthCalendarWhenWeekClick(Calendar day) {
        if (isSameMonth(day)) {
            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(day);
            setLeftAndRightSelectedDay(day);
        } else {
            ((RecycleCalendarAdapter) middleView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, -1);
            ((RecycleCalendarAdapter) leftView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, 2);
            ((RecycleCalendarAdapter) rightView.getAdapter()).setMonthDayCalendar(day);
            day.add(Calendar.MONTH, -1);

            ((RecycleCalendarAdapter) middleView.getAdapter()).setSelectDay(day);
            setLeftAndRightSelectedDay(day);
            showUnReadMessage();
        }
    }

    private boolean isSameMonth(Calendar day) {
        return selectedDay.get(Calendar.MONTH) == day.get(Calendar.MONTH) && selectedDay.get(Calendar.YEAR) == day.get(Calendar.YEAR);
    }

    public void setCalendarClickListener(RecycleCalendarAdapter.DateClickListener listener) {
        ((RecycleCalendarAdapter) middleView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) leftView.getAdapter()).setDateClickListener(listener);
        ((RecycleCalendarAdapter) rightView.getAdapter()).setDateClickListener(listener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onDateChanged(selectedDay);
    }

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
}
