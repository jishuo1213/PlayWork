package com.inspur.playwork.view.timeline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

/**
 * Created by Fan on 15-10-23.
 */
public class ItemTouchListener implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "ItemTouchListenerFan";

    public static final int LEFT_DIRECTION = -1;

    public static final int RIGHT_DIRECTION = 1;

    private static final int SHOW_ANIMATION_DURATION = 400;
    private static final int RECOVER_ANIMATION_DURATION = 350;


    private float lastXPos, lastYPos;

    private boolean isIntercept;

    private boolean notIntercept;

    private View itemView;

    private int pos;

    private int touchSlop;

    private int screenWidth;

    private LayoutInflater inflater;

    private View swipeNotChangeView;

    private ImageView goRightView;

    private ImageView goLeftView;

    private TextView swipeNoChangeText;

    private ImageView leftView, rightView;

    private RecyclerTaskAdapter.ViewHolder currentHolder;

    private float firstXpos;

    private int firstDirection;

    private View currentImageView; //当前图片的imageView

    private VelocityTracker velocityTracker;

    private int minFlingVelocity;

    private SwipeLeftOrRightListener listener;

    private AnimatorListener animatorListener;

    public interface SwipeLeftOrRightListener {
        void onSwipeEnd(int pos, int direction);
    }

    public ItemTouchListener(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        inflater = LayoutInflater.from(context);
        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Context context) {
        int height = DeviceUtil.dpTopx(context, 40);
        swipeNotChangeView = inflater.inflate(R.layout.layout_swip_no_change_view, null);
        goRightView = new ImageView(context);
        FrameLayout.LayoutParams rightLayoutParams = new FrameLayout.LayoutParams(2 * screenWidth, height + 1);
        goRightView.setLayoutParams(rightLayoutParams);
        goRightView.setBackgroundResource(R.drawable.sweeplist_call_tab);

        goLeftView = new ImageView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(2 * screenWidth, height + 1);
        goLeftView.setLayoutParams(layoutParams);
        goLeftView.setBackgroundResource(R.drawable.sweeplist_message_tab);

        swipeNoChangeText = (TextView) swipeNotChangeView.findViewById(R.id.tv_what_action);
        leftView = (ImageView) swipeNotChangeView.findViewById(R.id.img_call);
        rightView = (ImageView) swipeNotChangeView.findViewById(R.id.img_msg);

        //maxFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        minFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

        animatorListener = new AnimatorListener();
    }


    public void setListener(SwipeLeftOrRightListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        int action = e.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
            isIntercept = false;
            notIntercept = false;
            recycleVelocityTracker();
        }

        if (action != MotionEvent.ACTION_DOWN) {
            if (isIntercept)
                return true;
            if (notIntercept)
                return false;
        }

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastXPos = e.getX();
                lastYPos = e.getY();
                firstXpos = lastXPos;
                itemView = rv.findChildViewUnder(lastXPos, lastYPos);
                pos = rv.getChildAdapterPosition(itemView);
                currentHolder = (RecyclerTaskAdapter.ViewHolder) rv.findViewHolderForAdapterPosition(pos);
                RecyclerTaskAdapter adapter = (RecyclerTaskAdapter) rv.getAdapter();
                if (itemView == null || pos == RecyclerView.NO_POSITION || pos == 0 || adapter.isInvalidPos(pos))
                    notIntercept = true;
                createVelocityTracker();
                velocityTracker.addMovement(e);
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = e.getX();
                float currentY = e.getY();
                float dx = currentX - lastXPos;
                float dy = currentY - lastYPos;
                float xDiff = Math.abs(dx);
                float yDiff = Math.abs(dy);

                if (yDiff > touchSlop) {
                    notIntercept = true;
                    lastXPos = currentX;
                    lastYPos = currentY;
                    recycleVelocityTracker();
                }
                if (!notIntercept && xDiff > touchSlop
                        && xDiff * 0.5 > yDiff && itemView != null && pos != RecyclerView.NO_POSITION) {
                    isIntercept = true;
                    lastXPos = currentX;
                    lastYPos = currentY;
                    hideViews();
                    if (dx > 0) { // 右滑
                        addGoView(goRightView, -screenWidth);
                        ((FrameLayout) itemView).addView(swipeNotChangeView);
                        showRightViewText();
                        dx = Math.round(dx);
                        firstDirection = RIGHT_DIRECTION;
                    } else { // 左滑
                        addGoView(goLeftView, 0);
                        ((FrameLayout) itemView).addView(swipeNotChangeView);
                        showLeftViewText();
                        dx = Math.round(dx);
                        firstDirection = LEFT_DIRECTION;
                    }
                    currentImageView.setX(currentImageView.getX() + dx);
                    velocityTracker.addMovement(e);
                }
                break;
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }
        return isIntercept;
    }

    private void hideViews() {
        currentHolder.taskAllViews.setVisibility(View.GONE);
    }

    private void showViews() {
        currentHolder.taskAllViews.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_MOVE: {
                velocityTracker.addMovement(e);
                float currentX = e.getX();
                float currentY = e.getY();
                float dx = currentX - lastXPos;

                if (currentX > firstXpos && firstDirection == LEFT_DIRECTION) {
                    removeLeftAddRight();
                    firstDirection = RIGHT_DIRECTION;
                } else if (currentX < firstXpos && firstDirection == RIGHT_DIRECTION) {
                    removeRightAddLeft();
                    firstDirection = LEFT_DIRECTION;
                }
                dx = Math.round(dx);
                currentImageView.setX(currentImageView.getX() + dx);

                lastXPos = currentX;
                lastYPos = currentY;
                break;
            }
            case MotionEvent.ACTION_UP: {
                velocityTracker.addMovement(e);
                velocityTracker.computeCurrentVelocity(1000);

                int xVelocity;
                xVelocity = (int) velocityTracker.getXVelocity();

                float dx = e.getX() - firstXpos;

                if (dx >= screenWidth / 2) {
                    if (xVelocity <= -minFlingVelocity)
                        showGoLeft();
                    else
                        showGoRight();
                } else if (dx <= -screenWidth / 2) {
                    if (xVelocity >= minFlingVelocity)
                        showGoRight();
                    else
                        showGoLeft();
                } else if (dx < screenWidth / 2 || dx > -screenWidth / 2) {
                    if (Math.abs(xVelocity) <= minFlingVelocity)
                        recoverLeftOrRightView();
                    else if (xVelocity < -minFlingVelocity)
                        showGoLeft();
                    else if (xVelocity > minFlingVelocity)
                        showGoRight();
                }
                recycleVelocityTracker();
                break;
            }
        }

    }

    private void recoverLeftOrRightView() {
        if (currentImageView == goLeftView) {
            ObjectAnimator oa = ObjectAnimator.ofFloat(goLeftView, "x", goLeftView.getX(), 0);
            oa.setDuration(RECOVER_ANIMATION_DURATION);
            animatorListener.setType(AnimatorListener.RECOVER_LEFT_OR_RIGHT_VIEW);
            oa.addListener(animatorListener);
            oa.start();
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(goRightView, "x", goRightView.getX(), -screenWidth);
            oa.setDuration(RECOVER_ANIMATION_DURATION);
            animatorListener.setType(AnimatorListener.RECOVER_LEFT_OR_RIGHT_VIEW);
            oa.addListener(animatorListener);
            oa.start();
        }
    }


    private void showGoRight() {
        if (currentImageView == goLeftView) {
            recoverLeftViewAndShowRight();
        } else {
            showRightView();
        }
    }

    private void showGoLeft() {
        if (currentImageView == goRightView) {
            recoverRightViewAndShowLeft();
        } else {
            showLeftView();
        }
    }

    private void showRightView() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(goRightView, "x", goRightView.getX(), 0);
        oa.setDuration(RECOVER_ANIMATION_DURATION);
        animatorListener.setType(AnimatorListener.SHOW_RIGHT_VIEW);
        oa.addListener(animatorListener);
        oa.start();
    }

    private void recoverLeftViewAndShowRight() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(goLeftView, "x", goLeftView.getX(), 0);
        oa.setDuration(RECOVER_ANIMATION_DURATION);
        animatorListener.setType(AnimatorListener.RECOVER_LEFT_VIEW);
        oa.addListener(animatorListener);
        oa.start();
    }

    private void recoverRightViewAndShowLeft() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(goRightView, "x", goRightView.getX(), -screenWidth);
        oa.setDuration(RECOVER_ANIMATION_DURATION);
        animatorListener.setType(AnimatorListener.RECOVER_RIGHT_VIEW);
        oa.addListener(animatorListener);
        oa.start();
    }

    private void showLeftView() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(goLeftView, "x", goLeftView.getX(), -screenWidth);
        oa.setDuration(RECOVER_ANIMATION_DURATION);
        animatorListener.setType(AnimatorListener.SHOW_LEFT_VIEW);
        oa.addListener(animatorListener);
        oa.start();
    }


    private class AnimatorListener extends AnimatorListenerAdapter {

        private static final int RECOVER_LEFT_VIEW = 0;

        private static final int SHOW_RIGHT_VIEW = 1;

        private static final int RECOVER_RIGHT_VIEW = 2;

        private static final int SHOW_LEFT_VIEW = 3;

        private static final int RECOVER_LEFT_OR_RIGHT_VIEW = 4;

        private int type;


        @Override
        public void onAnimationEnd(Animator animation) {
            switch (type) {
                case SHOW_RIGHT_VIEW:
                    listener.onSwipeEnd(pos, RIGHT_DIRECTION);
                    removeViews();
                    break;
                case RECOVER_LEFT_VIEW: {
                    removeLeftAddRight();
                    ObjectAnimator oa = ObjectAnimator.ofFloat(goRightView, "x", -screenWidth, 0);
                    oa.addListener(this);
                    type = SHOW_RIGHT_VIEW;
                    oa.setDuration(SHOW_ANIMATION_DURATION);
                    oa.start();
                    break;
                }
                case RECOVER_RIGHT_VIEW: {
                    removeRightAddLeft();
                    ObjectAnimator oa = ObjectAnimator.ofFloat(goLeftView, "x", 0, -screenWidth);
                    oa.addListener(this);
                    type = SHOW_LEFT_VIEW;
                    oa.setDuration(SHOW_ANIMATION_DURATION);
                    oa.start();
                    break;
                }
                case SHOW_LEFT_VIEW:
                    listener.onSwipeEnd(pos, LEFT_DIRECTION);
                    removeViews();
                    break;
                case RECOVER_LEFT_OR_RIGHT_VIEW:
                    removeViews();
                    break;
            }
        }

        public void setType(int type) {
            this.type = type;
        }
    }


    private void addGoView(ImageView goLeftView, int x) {
        ((FrameLayout) itemView).addView(goLeftView);
        goLeftView.setX(x);
        currentImageView = goLeftView;
    }

    private void addGoView(ImageView goLeftView, int x, int index) {
        ((FrameLayout) itemView).addView(goLeftView, index);
        goLeftView.setX(x);
        currentImageView = goLeftView;
    }

    private void showLeftViewText() {
        leftView.setVisibility(View.VISIBLE);
        rightView.setVisibility(View.GONE);
        swipeNoChangeText.setText("新增");
        swipeNoChangeText.setVisibility(View.VISIBLE);
    }

    private void showRightViewText() {
        leftView.setVisibility(View.GONE);
        rightView.setVisibility(View.VISIBLE);
        swipeNoChangeText.setText("聊天");
        swipeNoChangeText.setVisibility(View.VISIBLE);
    }

    private void removeLeftAddRight() {
        ((FrameLayout) itemView).removeView(goLeftView);
        addGoView(goRightView, -screenWidth, 0);
        showRightViewText();
    }


    private void removeRightAddLeft() {
        ((FrameLayout) itemView).removeView(goRightView);
        addGoView(goLeftView, 0, 0);
        showLeftViewText();
    }

    private void removeViews() {
        showViews();
        ((FrameLayout) itemView).removeView(swipeNotChangeView);
        ((FrameLayout) itemView).removeView(goLeftView);
        ((FrameLayout) itemView).removeView(goRightView);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

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
}