package com.inspur.playwork.model.timeline;

import android.support.annotation.NonNull;

import com.inspur.playwork.utils.LunarUtil;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by Fan on 2015/8/6.
 */
public class CalendarDateBean implements Comparable<CalendarDateBean> {

    private LunarDate lunarDate;
    private int year;
    private int month;
    private int day;
    public int dayOfYear;
    private String festival;
    private boolean isCurrentMonth = true;

    public int unReadMessageNum = 0;

    public CalendarDateBean() {
        lunarDate = new LunarDate();
    }

    public void setDate(int year, int month, int day, int dayOfYear) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.dayOfYear = dayOfYear;
        if (lunarDate == null) {
            lunarDate = LunarUtil.getLunarDate(year, month, day);
        } else {
            LunarUtil.setLunarDate(lunarDate, year, month, day);
        }
        unReadMessageNum = 0;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setIsCurrentMonth(boolean isCurrentMonth) {
        this.isCurrentMonth = isCurrentMonth;
    }

    public int getDay() {
        return day;
    }

    public String getFestival() {
        return lunarDate.getLunarDateName();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public boolean isSelectDay(Calendar calendar) {
        return year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) &&
                day == calendar.get(Calendar.DATE);
    }

    public int comparWithCalendar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        if (this.year != year)
            return this.year - year;
        else {
            return this.dayOfYear - calendar.get(Calendar.DAY_OF_YEAR);
        }
    }

    @Override
    public int hashCode() {
        return (lunarDate.hashCode() + year + month + day) * (isCurrentMonth ? 1 : -1);
    }

    @Override
    public String toString() {
        return "CalendarDateBean{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", dayOfYear=" + dayOfYear +
                '}';
    }

    @Override
    public int compareTo(@NonNull CalendarDateBean another) {
        if (this.year != another.year)
            return this.year - another.year;
        else {
            return this.dayOfYear - another.dayOfYear;
        }
    }
}
