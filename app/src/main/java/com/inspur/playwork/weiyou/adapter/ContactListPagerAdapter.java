package com.inspur.playwork.weiyou.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by LazyBox on 15-11-10.
 */
public class ContactListPagerAdapter extends PagerAdapter {

    private List<View> mViewList;

    public ContactListPagerAdapter(List<View> viewList) {
        this.mViewList = viewList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(this.mViewList.get(position));
        return this.mViewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(this.mViewList.get(position));
    }

    @Override
    public int getCount() {
        return this.mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
