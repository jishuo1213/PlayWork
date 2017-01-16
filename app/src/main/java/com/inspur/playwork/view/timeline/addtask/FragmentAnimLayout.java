package com.inspur.playwork.view.timeline.addtask;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;


public class FragmentAnimLayout extends LinearLayout {

    private ViewTreeObserver.OnPreDrawListener preDrawListener;


    public FragmentAnimLayout(Context context) {
        super(context);
    }

    public FragmentAnimLayout(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FragmentAnimLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public void setYFraction(float fraction) {
        final float yFraction = fraction;
        if (getHeight() == 0) {
            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                        setYFraction(yFraction);
                        return true;
                    }
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }
        float translationY = getHeight() * fraction;
        setTranslationY(translationY);
    }

    @SuppressWarnings("unused")
    public void setXFraction(float fraction) {
        final float xFraction = fraction;
        if (getWidth() == 0) {
            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                        setXFraction(xFraction);
                        return true;
                    }
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }
        float translationX = getWidth() * fraction;
        setTranslationX(translationX);
    }
}
