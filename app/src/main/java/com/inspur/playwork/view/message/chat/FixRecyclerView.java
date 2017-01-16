package com.inspur.playwork.view.message.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Fan on 15-10-11.
 */
public class FixRecyclerView extends RecyclerView {

    public FixRecyclerView(Context context) {
        super(context);
    }

    public FixRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction < 1) {
            boolean original = super.canScrollVertically(direction);
            return !original && getChildAt(0) != null && getChildAt(0).getTop() < 0 || original;
        }
        return super.canScrollVertically(direction);
    }
}
