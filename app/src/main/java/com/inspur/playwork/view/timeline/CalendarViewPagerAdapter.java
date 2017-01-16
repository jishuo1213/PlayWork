package com.inspur.playwork.view.timeline;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Fan on 15-12-25.
 */
public class CalendarViewPagerAdapter extends PagerAdapter {

    private static final String TAG = "CalendarPagerAdapter";

    private ArrayList<View> viewList;

    private InitItemListener listener;

    public interface InitItemListener {
        void onInitItem(View view, int pos);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = viewList.get(position % 5);
        if (listener != null)
            listener.onInitItem(view, position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView(viewList.get(position % 5));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setViewList(ArrayList<View> viewList) {
        this.viewList = viewList;
    }

    public void setListener(InitItemListener listener) {
        this.listener = listener;
    }
}
