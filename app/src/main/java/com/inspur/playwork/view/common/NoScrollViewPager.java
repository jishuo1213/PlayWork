package com.inspur.playwork.view.common;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Fan on 16-1-26.
 */
public class NoScrollViewPager extends ViewPager {

    private boolean notScrol = false;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNotScrol(boolean notScrooll) {
        this.notScrol = notScrooll;
    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        /* return false;//super.onTouchEvent(arg0); */
        if (notScrol)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (notScrol)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }
}
