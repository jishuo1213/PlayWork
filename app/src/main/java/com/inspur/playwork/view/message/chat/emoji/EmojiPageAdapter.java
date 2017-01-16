package com.inspur.playwork.view.message.chat.emoji;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by fan on 16-9-26.
 */
public class EmojiPageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "EmojiPageAdapter";

    private ArrayList<Fragment> fragmentArrayList;

    public EmojiPageAdapter(FragmentManager fm) {
        super(fm);
    }


    public void setFragmentArrayList(ArrayList<Fragment> fragmentArrayList) {
        this.fragmentArrayList = fragmentArrayList;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "getItem: " + position);
        return fragmentArrayList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }
}
