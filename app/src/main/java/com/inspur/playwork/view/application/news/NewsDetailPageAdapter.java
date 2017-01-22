package com.inspur.playwork.view.application.news;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-18.
 */
public class NewsDetailPageAdapter extends PagerAdapter {
    private static final String TAG = "NewsDetailPageAdapter";

    private ArrayList<View> viewList;
    private int count;

    public void setViewList(ArrayList<View> viewList) {
        this.viewList = viewList;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {

        return count;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.i(TAG, "instantiateItem: " + position);
        int length = viewList.size();
        container.addView(viewList.get(position % length));
        return viewList.get(position % length);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int length = viewList.size();
        container.removeView(viewList.get(position % length));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
