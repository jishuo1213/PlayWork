package com.inspur.playwork.weiyou.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Fan on 15-11-17.
 */
public class MailDetailPagerAdapter extends PagerAdapter {
    private static final String TAG = "MailDetailPagerAdapter";

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
