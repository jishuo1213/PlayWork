package com.inspur.playwork.view.application.news;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

/**
 * Created by Fan on 2017/1/15.
 */

public class RecyViewPageAdapter extends PagerAdapter {

    private ArrayList<View> viewList;


    @Override
    public int getCount() {
        return viewList.size();
    }

    public void setViewList(ArrayList<View> viewList) {
        this.viewList = viewList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position));
        return viewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
