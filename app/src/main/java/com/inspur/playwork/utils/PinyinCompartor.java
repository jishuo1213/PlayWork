package com.inspur.playwork.utils;

import com.inspur.playwork.model.message.UserIconBean;

import java.util.Comparator;

/**
 * Created by fan on 15-8-19.
 */
public class PinyinCompartor implements Comparator<UserIconBean>{
    @Override
    public int compare(UserIconBean lhs, UserIconBean rhs) {
        if(lhs.pinyinName.equals("@") || rhs.pinyinName.equals("#")){
            return -1;
        }else if(lhs.pinyinName.equals("#") || rhs.pinyinName.equals("@")){
            return 1;
        }
        return lhs.pinyinName.compareTo(rhs.pinyinName);
    }
}
