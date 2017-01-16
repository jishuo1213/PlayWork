package com.inspur.playwork.weiyou.viewpager;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fan on 15-8-15.
 */
public abstract class PagerAdapter {
    private DataSetObservable mDataSetObservable = new DataSetObservable();

    public static final int POSITION_UNCHANGED = -1;
    public static final int POSITION_NONE = -2;

    public abstract int getCount();

    public abstract Object instantiateItem(ViewGroup container, int position);

    public abstract boolean isViewFromObject(View view, Object object);

    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    public void setPrimaryItem(View container, int position, Object object) {

    }

    public Parcelable saveState() {
        return null;
    }

    public void restoreState(Parcelable saveState, ClassLoader loader) {

    }

    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void registerDataSetObservable(DataSetObserver dataSetObservable) {
        mDataSetObservable.registerObserver(dataSetObservable);
    }

    public void unRegisterDataSetObservable(DataSetObserver dataSetObserver) {
        mDataSetObservable.unregisterObserver(dataSetObserver);
    }

    public float getPageWidth(int position) {
        return 1.0f;
    }

    public void startUpdate(ViewGroup container) {
    }

    public void finishUpdate(ViewGroup container) {

    }

    public Object getItem(int position) {
        return null;
    }

}
