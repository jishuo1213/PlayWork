package com.inspur.playwork.weiyou.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.weiyou.viewpager.PagerAdapter;

import java.util.ArrayList;

/**
 * Created by Fan on 15-11-17.
 */
public class MailDetailPagerAdapter extends PagerAdapter {
    private static final String TAG = "MailDetailPagerAdapter";

    private ArrayList<View> mdViewList;

    public MailDetailPagerAdapter(ArrayList<View> mdvl) {
        this.mdViewList = mdvl;
    }

    @Override
    public int getCount() {
        return mdViewList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mdViewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        container.removeView(mdViewList.get(position));
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
//        return mdViewList.indexOf(object);
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
