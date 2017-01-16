package com.inspur.playwork.view.message.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * Created by 笑面V客 on 15-11-3.
 */
public class ChoosePersonLayout extends LinearLayout {

    private float firstX, firstY, lastX, lastY;
    private boolean intercept;
    private int touchSlop;

    public ChoosePersonLayout(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public ChoosePersonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public ChoosePersonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
            intercept = false;
        }
        if (action != MotionEvent.ACTION_DOWN) {
            if (intercept) {
                return true;
            }
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = event.getX();
                lastY = event.getY();
                float dx = lastX - firstX;
                float dy = lastY - firstY;
                float xDiff = Math.abs(dx);
                float yDiff = Math.abs(dy);

                if (dx < 0 && xDiff > touchSlop && xDiff * 0.5 > yDiff) {
                    intercept = true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptHoverEvent(event);
//        return intercept;
    }
}
