package com.inspur.playwork.view.timeline;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.inspur.playwork.R;

import java.util.ArrayList;

/**
 * Created by Fan on 15-12-1.
 */
public class TaskListViewPageAdapter extends PagerAdapter {

    private static final String TAG = "TaskViewPageAdapterFan";

    private ArrayList<View> viewList;
    private int length;

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = viewList.get(position % length);
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position % length));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setViewList(ArrayList<View> viewList) {
        this.viewList = viewList;
        length = viewList.size();
    }
}
