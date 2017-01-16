package com.inspur.playwork.view.timeline;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Fan on 15-9-5.
 */
public class CalendarGridLayoutManager extends GridLayoutManager {

    private static final String TAG = "CalendarLayoutManagerFan";

    private int count = 6;

    public CalendarGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CalendarGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public CalendarGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    private boolean hasMeasure = false;
    private int width, height;

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {

        if (!hasMeasure) {
            View view = recycler.getViewForPosition(0);
            measureChild(view, widthSpec, heightSpec);
            width = View.MeasureSpec.getSize(widthSpec);
            height = view.getMeasuredHeight() * count;
            hasMeasure = true;
        }
        setMeasuredDimension(width, height);
    }

    public void setCount(int count) {
        this.count = count;
    }
}
