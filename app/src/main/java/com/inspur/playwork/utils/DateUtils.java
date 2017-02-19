package com.inspur.playwork.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.inspur.playwork.model.timeline.TaskBean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Fan on 15-8-27.
 */
public class DateUtils {

    private static final String TAG = "DateUtilsFan";
    private static Calendar tempCalendar = Calendar.getInstance();
    private static String[] chinaWeekText = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    public static int getTimeNoonOrAfterNoon(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 12 ? TaskBean.NOON : hour > 17 ? TaskBean.NIGHT : TaskBean.AFTERNOON;
    }

    public static int getTimeNoonOrAfterNoon(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 12 ? 1 : hour > 17 ? 3 : 2;
    }

    public static boolean isToday(Calendar selectday) {
        Calendar calendar = Calendar.getInstance();
        trimCalendarDate(calendar);
        return calendar.getTimeInMillis() <= selectday.getTimeInMillis();
    }

    public static String getOneWeekTextString(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            calendar.add(Calendar.DATE, -6);
        } else {
            calendar.add(Calendar.DATE, 2 - dayOfWeek);
        }
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.get(Calendar.YEAR)).
                append(".").
                append(month >= 10 ? month : "0" + month).
                append(".").
                append(date >= 10 ? date : "0" + date);
        sb.append("~");
        calendar.add(Calendar.DATE, 6);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        sb.append(calendar.get(Calendar.YEAR)).
                append(".").
                append(month >= 10 ? month : "0" + month).
                append(".").
                append(date >= 10 ? date : "0" + date);
        return sb.toString();
    }

    public static boolean isSameYearMonth(Calendar selectday) {
        Calendar calendar = Calendar.getInstance();
        Log.i(TAG, "isSameYearMonth: " + (selectday == null));
        return calendar.get(Calendar.YEAR) == selectday.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == selectday.get(Calendar.MONTH);
    }


    public static void setCalendarYear(Calendar calendar, int year) {
        calendar.set(Calendar.YEAR, year);
    }

    public static void setCalendarMonth(Calendar calendar, int month) {
        calendar.set(Calendar.MONTH, month);
    }

    public static void setCalendarDay(Calendar calendar, int day) {
        calendar.set(Calendar.DATE, day);
    }

    public static void setCalendarHour(Calendar calendar, int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
    }

    public static void setCalendarMinute(Calendar calendar, int minute) {
        calendar.set(Calendar.MINUTE, minute);
    }

    public static void setCalendarSeconds(Calendar calendar, int seconds) {
        calendar.set(Calendar.SECOND, seconds);
    }

    public static void trimCalendarDate(Calendar calendar) {
        setCalendarHour(calendar, 0);
        setCalendarMinute(calendar, 0);
        setCalendarSeconds(calendar, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static long getStartPointOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        trimCalendarDate(calendar);
        setCalendarSeconds(calendar, 1);
        return calendar.getTimeInMillis();
    }

    public static long getEndPointOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        setCalendarHour(calendar, 23);
        setCalendarMinute(calendar, 59);
        setCalendarSeconds(calendar, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getCalendarAllText(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return sb.append(calendar.get(Calendar.YEAR)).append("年").
                append(month < 10 ? "0" + month : month).append("月").
                append(day < 10 ? "0" + day : day).append("日 ").
                append(hour < 10 ? "0" + hour : hour).append(":").
                append(minute < 10 ? "0" + minute : minute).toString();
    }

    public static String getTimeHasNoSecond(long time) {
        tempCalendar.clear();
        tempCalendar.setTimeInMillis(time);
        return getCalendarAllText(tempCalendar);
    }

    public static String getCalendarAllText(long time) {
        tempCalendar.clear();
        tempCalendar.setTimeInMillis(time);
        StringBuilder sb = new StringBuilder();
        int month = tempCalendar.get(Calendar.MONTH) + 1;
        int day = tempCalendar.get(Calendar.DATE);
        int hour = tempCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = tempCalendar.get(Calendar.MINUTE);
        int second = tempCalendar.get(Calendar.SECOND);
        return sb.append(tempCalendar.get(Calendar.YEAR)).append("年").
                append(month < 10 ? "0" + month : month).append("月").
                append(day < 10 ? "0" + day : day).append("日 ").
                append(hour < 10 ? "0" + hour : hour).append(":").
                append(minute < 10 ? "0" + minute : minute).append(":").
                append(second < 10 ? "0" + second : second).toString();
    }

    public static String getCalendarText(long time) {
        tempCalendar.clear();
        tempCalendar.setTimeInMillis(time);
        StringBuilder sb = new StringBuilder();
        int month = tempCalendar.get(Calendar.MONTH) + 1;
        int day = tempCalendar.get(Calendar.DATE);
        int hour = tempCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = tempCalendar.get(Calendar.MINUTE);
        return sb.append(tempCalendar.get(Calendar.YEAR)).append("-").
                append(month < 10 ? "0" + month : month).append("-").
                append(day < 10 ? "0" + day : day).append(" ").
                append(hour < 10 ? "0" + hour : hour).append(":").
                append(minute < 10 ? "0" + minute : minute).toString();
    }

    private static String getCalendarText(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return sb.append(calendar.get(Calendar.YEAR)).append("年").
                append(month < 10 ? "0" + month : month).append("月").
                append(day < 10 ? "0" + day : day).append("日").toString();
    }

    public static String getCalendarDateWeek(long time) {
        tempCalendar.setTimeInMillis(time);
        StringBuilder sb = new StringBuilder();
        int year = tempCalendar.get(Calendar.YEAR);
        int month = tempCalendar.get(Calendar.MONTH) + 1;
        int day = tempCalendar.get(Calendar.DATE);
        int week = tempCalendar.get(Calendar.DAY_OF_WEEK);
        return sb.append(year).append("-").append(month < 10 ? "0" + month : month).append("-").
                append(day < 10 ? "0" + day : day).append(" ")
                .append(chinaWeekText[week - 1]).toString();
    }

    public static String getCalendarYearMonth(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        int month = calendar.get(Calendar.MONTH) + 1;
        return sb.append(calendar.get(Calendar.YEAR)).append(" 年 ").
                append(month < 10 ? "0" + month : month).append(" 月 ").toString();
    }

    public static String getLongTimePointText(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return getCalendarTimePointText(calendar);
    }

    @NonNull
    public static String getCalendarTimePointText(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return sb.append(hour < 10 ? "0" + hour : hour).append(":").
                append(minute < 10 ? "0" + minute : minute).toString();
    }

    public static String getSendReciveTimeDateText(long time) {
        StringBuilder sb = new StringBuilder();
        tempCalendar.clear();
        tempCalendar.setTimeInMillis(time);
        int month = tempCalendar.get(Calendar.MONTH) + 1;
        int day = tempCalendar.get(Calendar.DATE);
        int hour = tempCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = tempCalendar.get(Calendar.MINUTE);
        int sencond = tempCalendar.get(Calendar.SECOND);
        return sb.append(month < 10 ? "0" + month : month).append("月").
                append(day < 10 ? "0" + day : day).append("日 ").
                append(hour < 10 ? "0" + hour : hour).append(":").
                append(minute < 10 ? "0" + minute : minute).append(":").
                append(sencond < 10 ? "0" + sencond : sencond).toString();
    }

    public static int getTimePeriod(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour == 0 ? TaskBean.NOON : hour == 12 ? TaskBean.AFTERNOON : TaskBean.NIGHT;
    }

    public static String getLongTimeDateText(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return getCalendarDateText(calendar);
    }

    @NonNull
    public static String getCalendarDateText(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);

        return sb.append(month < 10 ? "0" + month : month).append("/").
                append(day < 10 ? "0" + day : day).toString();
    }

    public static String time2MMDD(long time) {
        long currentTime = System.currentTimeMillis();
        tempCalendar.setTimeInMillis(currentTime);
        tempCalendar.set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        currentTime = tempCalendar.getTimeInMillis();

        long standarTime = 86400000L;
        long dtime = currentTime - time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        if (dtime <= 0) {
            return getCalendarTimePointText(calendar);
        } else if ((dtime <= standarTime)) {
            return "昨天";
        } else if ((dtime > standarTime) && (dtime <= 2 * standarTime)) {
            return "前天";
        } else if (calendar.get(Calendar.YEAR) == tempCalendar.get(Calendar.YEAR)) {
            return getDateMMDD(calendar);
        } else {
            return getCalendarText(calendar);
        }
    }

    public static String getDateMMDD(Calendar calendar) {
        StringBuffer sb = new StringBuffer();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);

        return sb.append(month < 10 ? "0" + month : month).append("月")
                .append(day < 10 ? "0" + day : day).append("日").toString();
    }

    public static String getDatedFName(String fname) {
        StringBuffer result = new StringBuffer();
        SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
        String dateSfx = "_" + df.format(new Date());
        int idx = fname.lastIndexOf('.');
        if (idx != -1) {
            result.append(fname.substring(0, idx));
            result.append(dateSfx);
            result.append(fname.substring(idx));
        } else {
            result.append(fname);
            result.append(dateSfx);
        }
        return result.toString();
    }


    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        Log.d(TAG, "isSameDayOfMillis() called with: " + "ms1 = [" + ms1 + "], ms2 = [" + ms2 + "]");
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY
                && interval > -1L * MILLIS_IN_DAY
                && toDay(ms1) == toDay(ms2);
    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    public static int[] getDayWeekNum(long time) {
        int[] res = new int[2];
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTimeInMillis(time);
        int year = currentDay.get(Calendar.YEAR);
        int mouth = currentDay.get(Calendar.MONTH);
        int day = currentDay.get(Calendar.DATE);
        int dateNums = currentDay.get(Calendar.DAY_OF_YEAR);
        currentDay.set(Calendar.MONTH, 0);
        currentDay.set(Calendar.DATE, 1);
        int yearFirstDayofWeek = currentDay.get(Calendar.DAY_OF_WEEK);
        if (yearFirstDayofWeek <= 5 && yearFirstDayofWeek != 1) {
            dateNums += (yearFirstDayofWeek - 2);
        } else {
            if (yearFirstDayofWeek == 1) {
                dateNums -= 1;
            } else {
                dateNums -= (7 - yearFirstDayofWeek + 2);
            }
        }
        if (dateNums <= 0) {//去年的最后一周
            currentDay.add(Calendar.DATE, -1);
            return getDayWeekNum(currentDay.getTimeInMillis());
        }
        if (mouth == 11 && (31 - day) < 7) {
            currentDay.set(Calendar.MONTH, 11);
            currentDay.set(Calendar.DATE, 31);
            int lastDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
            if (lastDayOfWeek < 5 && lastDayOfWeek > 1) {
                if ((31 - day) < (lastDayOfWeek - 1)) {//明年的第一周
                    res[0] = year + 1;
                    res[1] = 1;
                    return res;
                }
            }
        }

        if (dateNums % 7 == 0) {
            res[0] = year;
            res[1] = dateNums / 7;
            return res;
        } else {
            res[0] = year;
            res[1] = dateNums / 7 + 1;
            return res;
        }
    }

//    private static Pattern urlPattern;
//    private static String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
//
//
//    public static boolean isUrlVliad(String url) {
//        if (urlPattern == null)
//            urlPattern = Pattern.compile(urlRegex);
//        Matcher matcher = urlPattern.matcher(url);
//        return matcher.matches();
//    }


    public static void main(String[] args) {
        Calendar c1 = Calendar.getInstance();

//        Calendar c2 = Calendar.getInstance();
//        c1.set(Calendar.YEAR, 2016);
//        c1.set(Calendar.MONTH, 0);
//        c1.set(Calendar.DATE, 1);
//        c1.set(Calendar.MONTH, 0);
//        c1.set(Calendar.MONTH, 0);
//        c2.set(Calendar.DATE, 4);
//        c2.set(Calendar.HOUR_OF_DAY, 1);
//        c2.set(Calendar.MINUTE, 0);
//        System.out.println(isSameDayOfMillis(c1.getTimeInMillis(), c2.getTimeInMillis()));
//        System.out.println(isUrlVliad("http://office7.inspur.com:8082/eportal/fileDir/inspur/resource/cms/2017/01/img_mobile_site/2017011223160635012_small.jpg"));
        int[] res = getDayWeekNum(c1.getTimeInMillis());

        System.out.println(res[0]);
        System.out.println(res[1]);
    }
}
