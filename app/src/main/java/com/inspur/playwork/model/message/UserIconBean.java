package com.inspur.playwork.model.message;

import android.graphics.drawable.Drawable;

/**
 * Created by fan on 15-8-18.
 */
public class UserIconBean {

    private Drawable iconDrawable;
    private String userName;
    public String pinyinName;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    @Override
    public int hashCode() {
        return userName.hashCode() + iconDrawable.hashCode();
    }
}
