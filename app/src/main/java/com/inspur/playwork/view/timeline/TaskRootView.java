package com.inspur.playwork.view.timeline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;


/**
 * Created by Fan on 15-9-2.
 */
public class TaskRootView extends RelativeLayout {

    private static final String TAG = "TaskRootViewFan";

    private static final float SCALE_FACTOR = 0.35f;
    private static final int ANIM_DURATION = 200;

    public interface TaskRootViewEventListener {
        void onStateChanged(boolean isMonthStae);
    }

    private TaskRootViewEventListener listener;

    private boolean isMonthState;

    private boolean isNeedIntercept;

    private boolean notIntercept = true;

    private int touchSlop;

    private RecyclerView taskRecyclerView;

    private View calendarRootView;
    private View weekRecycleView;
    private View weekTitleView;

    private View rootView;

    private int taskTransDis, weekReclyerTransDis;

    private ToogleAnimStateListener toogleAnimStateListener;

    private VelocityTracker velocityTracker;

    private boolean disableTouch;

    private ValueAnimator currentValueAnim;

    private ViewDragHelper dragHelper;

    public TaskRootView(Context context) {
        this(context, null);
    }

    public TaskRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(true);
        init();
    }

    private void init() {
        touchSlop = DeviceUtil.getFlipDistance(getContext());
        //touchSlop = 10;
        toogleAnimStateListener = new ToogleAnimStateListener();
        dragHelper = ViewDragHelper.create(this, 1.0f, dragHelperCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState: ");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.i(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (rootView == null) {
            rootView = (View) getParent();
            calendarRootView = rootView.findViewById(R.id.calendar_root_view);
            weekRecycleView = rootView.findViewById(R.id.week_recycler_view);
            weekTitleView = rootView.findViewById(R.id.week_view);
            ViewTreeObserver vo = getViewTreeObserver();
            vo.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    taskTransDis = calendarRootView.getHeight() / 6 * 5;
                    weekReclyerTransDis = weekRecycleView.getHeight() + weekTitleView.getHeight();
                    taskHeight = getLayoutParams().height;
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    private float lastXPos, lastYPos, firstYPos;
    private float taskHeight;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

//        return dragHelper.shouldInterceptTouchEvent(ev);

        int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isNeedIntercept = false;
            notIntercept = false;
            recycleVelocityTracker();
        }

        //      Log.i(TAG, "isNeedIntercept" + isNeedIntercept + "notIntercept" + notIntercept);
        if (disableTouch) {
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            if (isNeedIntercept) {
                return true;
            }
            if (notIntercept) {
                return false;
            }
        }

        if (currentValueAnim != null && currentValueAnim.isRunning()) {
//            currentValueAnim.end();
            currentValueAnim.cancel();
            currentValueAnim = null;
        }

        createVelocityTracker();
        velocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                notIntercept = false;
                lastXPos = ev.getRawX();
                firstYPos = lastYPos = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float dx = x - lastXPos;
                float xDiff = Math.abs(dx);
                float dy = y - lastYPos;
                float yDiff = Math.abs(dy);

                if (!notIntercept && yDiff > touchSlop && yDiff * 0.5 > xDiff && dy < 0 && isMonthState) {
                    isNeedIntercept = true;
                    setTranslationY(getTranslationY() + dy);
                    ViewGroup.LayoutParams lp = getLayoutParams();
//                    taskHeight = lp.height;
                    lp.height = (int) (-getTranslationY() + taskHeight + 0.5f);
                    setLayoutParams(lp);
                    lastXPos = x;
                    lastYPos = y;
                    return isNeedIntercept;
                } else if (yDiff > touchSlop && yDiff > xDiff * 0.5 && dy > 0 && !notIntercept && ((LinearLayoutManager) taskRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0 && !isMonthState) {
                    isNeedIntercept = true;
                    setTranslationY(getTranslationY() + dy);
                    //setY(getY() + dy);
                    ViewGroup.LayoutParams lp = getLayoutParams();
                    lp.height = (int) (-getTranslationY() + taskHeight + 0.5f);
                    setLayoutParams(lp);
                    lastXPos = x;
                    lastYPos = y;
                    return isNeedIntercept;
                } else if (xDiff > touchSlop && isMonthState) {
                    isNeedIntercept = true;
                } else if (xDiff > touchSlop && !isMonthState) {
                    isNeedIntercept = false;
                    notIntercept = true;
                }
                break;
        }
        return isNeedIntercept;
    }


    public void setTaskRecyclerView(RecyclerView taskRecyclerView) {
        this.taskRecyclerView = taskRecyclerView;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        //     return super.onTouchEvent(event);

//        dragHelper.processTouchEvent(event);
//        return true;

        if (disableTouch)
            return true;

        if (velocityTracker != null)
            velocityTracker.addMovement(event);


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastXPos = event.getRawX();
                lastYPos = event.getRawY();
                createVelocityTracker();
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getRawX();
                float y = event.getRawY();
                float dx = x - lastXPos;
                float xDiff = Math.abs(dx);
                float dy = y - lastYPos;
                float yDiff = Math.abs(dy);
                if (yDiff > xDiff && isNeedIntercept) {
                    float transY = checkTransY(dy);
                    float scaleFactor = computeScaleFactor(transY);
                    float weekCalendarTrans = computeWeekCalendarTransY(transY);
                    weekRecycleView.setTranslationY(weekCalendarTrans);
                    calendarRootView.setScaleX(scaleFactor);
                    calendarRootView.setScaleY(scaleFactor);
                    //setTranslationY(transY);
                    setTranslationY(transY);
                    setTaskRootHeight(transY);
                    lastXPos = x;
                    lastYPos = y;
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null) {
                    return true;
                }
                velocityTracker.computeCurrentVelocity(1000);
                float yVelocity = velocityTracker.getYVelocity();
                float dy = event.getRawY() - firstYPos;
                if (isNeedIntercept)
                    if (dy > 0) {
                        showMonthCalendar();
                    } else if (dy < 0) {
                        showWeekCalendar();
                    } else {
                        if (yVelocity < 0) {
                            showWeekCalendar();
                        } else {
                            showMonthCalendar();
                        }
                    }
                isNeedIntercept = false;
                notIntercept = false;
                recycleVelocityTracker();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void showWeekCalendar() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(getTranslationY(), -taskTransDis);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.addUpdateListener(toogleCalendarUpdateListener);
        valueAnimator.addListener(toogleAnimStateListener);
        toogleAnimStateListener.setIsShowWeek(true);
        currentValueAnim = valueAnimator;
        valueAnimator.start();
    }


    public void setDisableTouch(boolean disableTouch) {
        this.disableTouch = disableTouch;
    }

    public void showMonthCalendar() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(getTranslationY(), 0);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.addUpdateListener(toogleCalendarUpdateListener);
        valueAnimator.addListener(toogleAnimStateListener);
        toogleAnimStateListener.setIsShowWeek(false);
        currentValueAnim = valueAnimator;
        valueAnimator.start();
    }

    private void setTaskRootHeight(float transY) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = computTaskRootHeight(transY);
        setLayoutParams(lp);
    }

    private int computTaskRootHeight(float transY) {
        return (int) (-transY + taskHeight + 0.5f);
    }


    private float computeWeekCalendarTransY(float transY) {
        float factor = transY / taskTransDis;
        return -factor * weekReclyerTransDis;
    }

    private float computeScaleFactor(float transY) {
        float factor = transY / taskTransDis;

        return 1.0f + factor * SCALE_FACTOR;
    }

    private float checkTransY(float dy) {
        float transY = getTranslationY() + dy;
        if (transY > 0) {
            return 0;
        } else if (transY < -taskTransDis) {
            return -taskTransDis;
        }
        return transY;
    }

    public void setIsNeedIntercept(boolean isNeedIntercept) {
        //   this.isNeedIntercept = isNeedIntercept;
        isMonthState = isNeedIntercept;
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

    private ValueAnimator.AnimatorUpdateListener toogleCalendarUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            float scaleFactor = computeScaleFactor(value);
            float weekTransY = computeWeekCalendarTransY(value);
            setTaskRootHeight(value);
            setTranslationY(value);
            calendarRootView.setScaleX(scaleFactor);
            calendarRootView.setScaleY(scaleFactor);
            weekRecycleView.setTranslationY(weekTransY);
        }


    };

    public void setListener(TaskRootViewEventListener listener) {
        this.listener = listener;
    }

    public void clearListener() {
        this.listener = null;
        toogleAnimStateListener = null;
        toogleCalendarUpdateListener = null;
    }

    private class ToogleAnimStateListener extends AnimatorListenerAdapter {

        private boolean isShowWeek;
        private boolean isCancel;

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            isCancel = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isMonthState = !isShowWeek;
            if (listener != null && !isCancel) {
                listener.onStateChanged(isMonthState);
            }
            isCancel = false;
            currentValueAnim = null;
        }

        public void setIsShowWeek(boolean isShowWeek) {
            this.isShowWeek = isShowWeek;
        }
    }

    public int getShadowViewHeight() {
        return DeviceUtil.dpTopx(getContext(), 2);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow: taskRoot view detach");
    }

    private ViewDragHelper.Callback dragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            return super.onEdgeLock(edgeFlags);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }

        @Override
        public int getOrderedChildIndex(int index) {
            return super.getOrderedChildIndex(index);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return super.getViewHorizontalDragRange(child);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return super.clampViewPositionHorizontal(child, left, dx);
        }
    };
}
