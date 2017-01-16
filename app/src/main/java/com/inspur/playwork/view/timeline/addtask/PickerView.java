package com.inspur.playwork.view.timeline.addtask;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.TextView;

import com.inspur.playwork.utils.DeviceUtil;

import java.util.ArrayList;


/**
 * Created by Fan on 15-8-24.
 */
public class PickerView extends ViewGroup {


    private static final String TAG = "PickViewFan";

    public static final int YEAR = 1;
    public static final int MONTH = 2;
    public static final int DAY = 3;
    public static final int HOUR = 4;
    public static final int MINUTE = 5;

    private static final int MAX_FLING_DURATION = 600;
    private static final int ACCELERATION = 7;


    /**
     * 用户手指脱离pickview，正在根据速度自己滑动到最后的地点
     */
    private static final int STATE_SETTING = 0;
    /**
     * 用户的手指正在pickiew上拖动
     */
    private static final int STATE_DRAGING = 1;
    /**
     * 待机状态
     */
    private static final int STATE_IDLE = 2;


    public interface ItemSelectedListener {
        void onItemSelect(int selcected, int type);
    }

    private int[] nums;
    private int itemCount;
    private int selectIndex;
    private int childHeight;


    private VelocityTracker velocityTracker;

    private ArrayList<View> viewList;


    private Interpolator interpolator;

    private int firstIndex = 0, lastIndex = 6;


    private AfterScrollAnimUpdateListener listener;

    private AnimStaeListener animStaeListener;


    private Scroller scroller;

    private ItemSelectedListener itemSelectedListener;

    private int type = YEAR;

    private int touchSlop;
    private int minFlingVelocity;
    private int maxFlingVelocity;

    private int state;

    public PickerView(Context context) {
        super(context);
    }

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        nums = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        itemCount = nums.length;
        selectIndex = itemCount / 2;
        viewList = new ArrayList<>();
        listener = new AfterScrollAnimUpdateListener();
        interpolator = new AccelerateInterpolator();
        animStaeListener = new AnimStaeListener();
        scroller = new Scroller(getContext(), sInterpolator);

        touchSlop = DeviceUtil.getFlipDistance(getContext());
        minFlingVelocity = DeviceUtil.getMinFlingVelocity(getContext());
        maxFlingVelocity = DeviceUtil.getMaxFlingVelocity(getContext());
        setScrollState(STATE_IDLE);

        setWillNotDraw(true);
    }


    public void setNums(int[] nums) {
        this.nums = nums;
        itemCount = nums.length;
        selectIndex = itemCount / 2 - 1;
        setTextView();
    }

    public void setNums(int[] nums, int selectIndex) {
        this.nums = nums;
        itemCount = nums.length;
        this.selectIndex = selectIndex;
        setTextView();
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectIndex = selectedIndex;
        setTextView();
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setItemSelectedListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int count = getChildCount();
        if (heightMode == MeasureSpec.EXACTLY) {
            childHeight = measureHeight / 3;
            int childHeightMode = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                measureChild(view, widthMeasureSpec, childHeightMode);
                int childMeasureHeight = view.getMeasuredHeight();
                childHeight = childMeasureHeight < childHeight ? childMeasureHeight : childHeight;
            }
        } else {
            int height = 0;
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                int childWidth = view.getMeasuredWidth();
                int childHeight = view.getMeasuredHeight();
                height += childHeight;
                sizeWidth = sizeWidth > childWidth ? sizeWidth : childWidth;
            }
            childHeight = height / count;
        }
        setMeasuredDimension(sizeWidth, childHeight * 3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void setTextViewNum() {
        setTextView();
        if (itemSelectedListener != null) {
            itemSelectedListener.onItemSelect(getSelectIndex(), type);
        }
    }

    private void setTextView() {
        int count = getChildCount();
        int index = -(count - 1) / 2;
        int viewIndex = firstIndex;
        for (int i = 0; i < count; i++) {
            int numIndex = (selectIndex + index) % itemCount;
            if (numIndex < 0)
                numIndex = (numIndex + itemCount) % itemCount;
            if (viewIndex == count && viewIndex != lastIndex) {
                viewIndex = 0;
            }
            ((TextView) viewList.get(viewIndex)).setText(nums[numIndex] + "");
            index++;
            viewIndex++;
        }
    }

    public int getSelectIndex() {
        int index = selectIndex % itemCount;
        if (index < 0)
            index = (index + itemCount) % itemCount;
        return index;
    }


    float lastXPos, lastYPos;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (state == STATE_SETTING && !scroller.isFinished()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastYPos = event.getY();
                lastXPos = event.getX();
                createVelocityTracker();
                velocityTracker.addMovement(event);
                lastAnimTime = -1;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (velocityTracker != null)
                    velocityTracker.addMovement(event);

                float dx = event.getX() - lastXPos;
                float dy = event.getY() - lastYPos;
                float xDiff = Math.abs(dx);
                float yDiff = Math.abs(dy);

                if (yDiff * 0.5f > xDiff) {
                    setScrollState(STATE_DRAGING);
                    float scrollY = getScrollY() - dy;
                    scrollTo(getScrollX(), (int) scrollY);
                    lastXPos = event.getX();
                    lastYPos = event.getY();
                    lastYPos += scrollY - (int) scrollY;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (state == STATE_DRAGING) {
                    if (velocityTracker == null) {
                        setScrollState(STATE_IDLE);
                        return true;
                    }
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                    int yVelocity = (int) velocityTracker.getYVelocity();
                    yVelocity = Math.abs(yVelocity) < minFlingVelocity ? 0 : yVelocity;
                    yVelocity = Math.abs(yVelocity) > maxFlingVelocity * 3 / 4 ? maxFlingVelocity : yVelocity;
                    scrollingPickView(yVelocity);

                    recycleVelocityTacker();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void scrollingPickView(int yVelocity) {
        setScrollState(STATE_SETTING);
        if (yVelocity == 0) {
            scrollToCloseOne();
        } else {
            doValueAnim(yVelocity);
        }
    }

    private void scrollToCloseOne() {
        int scrollOffSet = getScrollOffSet();
        int scrollOffSetDiff = Math.abs(scrollOffSet);
        if (scrollOffSetDiff > childHeight / 2) {
            int dy = (childHeight - scrollOffSetDiff) * (scrollOffSet / scrollOffSetDiff);
            smoothScrollTo(0, dy);
        } else {
            smoothScrollTo(0, -scrollOffSet);
        }
    }


    private float getDuration(int yVelocity) {
        return Math.min(Math.abs(yVelocity) / ACCELERATION, MAX_FLING_DURATION);
    }

    private void smoothScrollTo(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            setScrollState(STATE_IDLE);
            return;
        }
        float distanceFactor = Math.min(1.0f, 1.0f * Math.abs(dy) / childHeight);
        int duration = (int) (distanceFactor + 1) * 100;
        duration = Math.min(duration, 250);
        scroller.startScroll(getScrollX(), getScrollY(), dx, dy, duration);
        invalidatePickView();
    }


    private void setScrollState(int scrollState) {
        state = scrollState;
    }

    private void recycleVelocityTacker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void createVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private int preScrollY = 0;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (t - preScrollY >= childHeight) {//向上滑动了一个
            scrollUpOverOneView();
            preScrollY += childHeight;
        } else if (t - preScrollY <= -childHeight) {//向下滑动了一个
            scrollDownOverOneView();
            preScrollY -= childHeight;
        }

    }

    private void doValueAnim(int velocity) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(velocity, 0);
        valueAnimator.addUpdateListener(listener);
        valueAnimator.setDuration((long) getDuration(velocity));
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.addListener(animStaeListener);
        valueAnimator.start();
    }

    private int getScrollOffSet() {
        return getScrollY() % childHeight;
    }


    private void scrollDownOverOneView() {
        resetView(false);
    }

    private void scrollUpOverOneView() {
        resetView(true);
    }


    private long lastAnimTime = -1;

    private class AfterScrollAnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private int v0;

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (lastAnimTime != -1) {
                int dTime = (int) (animation.getCurrentPlayTime() - lastAnimTime);
                long dx = (v0 + (int) animation.getAnimatedValue()) * dTime;
                int scrollY = (int) (dx / 2000);
                scrollBy(0, -scrollY);
            }
            lastAnimTime = animation.getCurrentPlayTime();
            v0 = (int) animation.getAnimatedValue();

        }
    }

    private void invalidatePickView() {
        if (Build.VERSION.SDK_INT >= 16)
            postInvalidateOnAnimation();
        else
            postInvalidate();
    }

    private class AnimStaeListener implements ValueAnimator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            scrollToCloseOne();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


    private void resetView(boolean isUp) {
        if (isUp) {
            viewList.get(firstIndex).setY(viewList.get(lastIndex).getY() + childHeight);
            lastIndex = firstIndex;
            firstIndex++;
            if (firstIndex > viewList.size() - 1) {
                firstIndex = 0;
            }
            selectIndex++;
            setTextViewNum();
        } else {
            viewList.get(lastIndex).setY(viewList.get(firstIndex).getY() - childHeight);
            firstIndex = lastIndex;
            lastIndex--;
            if (lastIndex < 0) {
                lastIndex = viewList.size() - 1;
            }
            selectIndex--;
            setTextViewNum();
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset() && !scroller.isFinished()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidatePickView();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = -childHeight * ((getChildCount() - 3) / 2);
        int bottom = -childHeight * ((getChildCount() - 3) / 2);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            bottom += childHeight;
            child.layout(0, top, child.getMeasuredWidth(), bottom);
            top += childHeight;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        viewList.clear();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            viewList.add(view);
        }
        setTextViewNum();
    }

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
}
