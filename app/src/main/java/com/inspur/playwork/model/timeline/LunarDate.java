package com.inspur.playwork.model.timeline;

/**
 * Created by Fan on 2015/8/6.
 */
public class LunarDate {

    private int year;
    private int month;
    private int lunarDate;
    private String lunarDateName;


    public String getLunarDateName() {
        return lunarDateName;
    }

    public void setLunarDateName(String lunarDateName) {
        this.lunarDateName = lunarDateName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getLunarDate() {
        return lunarDate;
    }

    public void setLunarDate(int lunarDate) {
        this.lunarDate = lunarDate;
    }

    @Override
    public int hashCode() {
        return year + month + lunarDate + lunarDateName.hashCode();
    }
}
