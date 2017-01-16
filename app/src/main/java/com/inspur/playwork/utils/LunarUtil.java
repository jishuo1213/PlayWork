package com.inspur.playwork.utils;

import android.text.TextUtils;

import com.inspur.playwork.model.timeline.LunarDate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fan on 2015/8/6.
 */
public class LunarUtil {

    private static LunarUtil lunarUtil;

    private LunarUtil() {
        result = new long[7];
    }

    private static GregorianCalendar utcCal = null;

    private long[] lunarInfo = new long[]{0x04bd8, 0x04ae0,
            0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0,
            0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540,
            0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5,
            0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3,
            0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0,
            0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0,
            0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8,
            0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570,
            0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5,
            0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
            0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50,
            0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0,
            0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7,
            0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50,
            0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954,
            0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260,
            0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
            0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0,
            0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20,
            0x0ada0};
/*    private String[] nStr1 = new String[]{"", "正", "二", "三", "四",
            "五", "六", "七", "八", "九", "十", "冬月", "腊月"};*/
/*    private String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊",
            "己", "庚", "辛", "壬", "癸"};
    private String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰",
            "巳", "午", "未", "申", "酉", "戌", "亥"};
    private String[] Animals = new String[]{"鼠", "牛", "虎", "兔",
            "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};*/


    /**
     * 国历节日 *表示放假日
     */
    private final static String[] sFtv = {
            "0101*元旦", "0214 情人节", "0308 妇女节", "0312 植树节",
            "0401 愚人节", "0501*劳动节", "0504 青年节", "0512 护士节", "0601 儿童节", "0701 建党节",
            "0801 建军节", "0808 父亲节",
            "0910 教师节", "1001*国庆节", "1006 老人节",
            "1024 联合国日", "1111 光棍节",
            "1225 圣诞节"
    };
    /**
     * 农历节日 *表示放假日
     */
    private final static String[] lFtv = {
            "0101*春节", "0115 元宵节",
            "0505*端午节",
            "0707 七夕节", "0715 中元节", "0815*中秋节",
            "0909 重阳节",
            "1208 腊八节", "1224 小年", "0100*除夕"
    };

    private final static String[] solarTerm = {
            "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
            "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
            "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };

    private final static String[] months = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月",
    };

    private final static int[] solarTermInfo = {
            0, 21208, 42467, 63836, 85337, 107014, 128867, 150921,
            173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033,
            353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758
    };


    private final long TIME_1900_01_31 = getSomeDayTimeMillion(1900, 1, 31);
    private Calendar calculateCalendar;
    private long[] result;


    /**
     * 传回农历 y年的总天数
     */
    private int lYearDays(int y) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[y - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(y));
    }

    /**
     * 传回农历 y年闰月的天数
     */
    private int leapDays(int y) {
        if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }


    private long getSomeDayTimeMillion(int year, int month, int day) {
        if (calculateCalendar == null) {
            calculateCalendar = Calendar.getInstance();
        }
        calculateCalendar.set(year, month - 1, day);
        return calculateCalendar.getTimeInMillis();
    }

    /**
     * 传回农历 y年闰哪个月 1-12 , 没闰传回 0
     */
    private int leapMonth(int y) {
        return (int) (lunarInfo[y - 1900] & 0xf);
    }

    /**
     * 传回农历 y年m月的总天数
     */
    private int monthDays(int y, int m) {
        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * 传回农历 y年的生肖
     */
   /* public String AnimalsYear(int y) {
        return Animals[(y - 4) % 12];
    }*/

    /**
     * 传入 月日的offset 传回干支,0=甲子
     */
/*    private String cyclicalm(int num) {
        return (Gan[num % 10] + Zhi[num % 12]);
    }*/

    /**
     * 传入 offset 传回干支, 0=甲子
     */
/*    public String cyclical(int y) {
        int num = y - 1900 + 36;
        return (cyclicalm(num));
    }*/

    /**
     * 传出农历.year0 .month1 .day2 .yearCyl3 .monCyl4 .dayCyl5 .isLeap6
     */

   /*  private long[] Lunar(int y, int m) {
        long[] nongDate = new long[7];
        int i, temp = 0, leap;
        Date baseDate = new GregorianCalendar(1900 + 1900, 1, 31).getTime();
        Date objDate = new GregorianCalendar(y + 1900, m, 1).getTime();
        long offset = (objDate.getTime() - baseDate.getTime()) / 86400000L;
        if (y < 2000)
            offset += year19[m - 1];
        if (y > 2000)
            offset += year20[m - 1];
        if (y == 2000)
            offset += year2000[m - 1];
        nongDate[5] = offset + 40;
        nongDate[4] = 14;
        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
            nongDate[4] += 12;
        }
        if (offset < 0) {
            offset += temp;
            i--;
            nongDate[4] -= 12;
        }
        nongDate[0] = i;
        nongDate[3] = i - 1864;
        leap = leapMonth(i); // 闰哪个月
        nongDate[6] = 0;
        for (i = 1; i < 13 && offset > 0; i++) {
            // 闰月
            if (leap > 0 && i == (leap + 1) && nongDate[6] == 0) {
                --i;
                nongDate[6] = 1;
                temp = leapDays((int) nongDate[0]);
            } else {
                temp = monthDays((int) nongDate[0], i);
            }
            // 解除闰月
            if (nongDate[6] == 1 && i == (leap + 1))
                nongDate[6] = 0;
            offset -= temp;
            if (nongDate[6] == 0)
                nongDate[4]++;
        }
        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (nongDate[6] == 1) {
                nongDate[6] = 0;
            } else {
                nongDate[6] = 1;
                --i;
                --nongDate[4];
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
            --nongDate[4];
        }
        nongDate[1] = i;
        nongDate[2] = offset + 1;
        return nongDate;
    }*/

    /**
     * 传出y年m月d日对应的农历.year0 .month1 .day2 .yearCyl3 .monCyl4 .dayCyl5 .isLeap6
     */
    private long[] calElement(int y, int m, int d) {
        int i, temp = 0, leap;
        //    Date baseDate = new GregorianCalendar(0 + 1900, 0, 31).getTime();//1900-01-31
        //     Date objDate = new GregorianCalendar(y, m - 1, d).getTime();//传入天
        long offset = (getSomeDayTimeMillion(y, m, d) - TIME_1900_01_31) / 86400000L;
        result[5] = offset + 40;
        result[4] = 14;
        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
            result[4] += 12;
        }
        if (offset < 0) {
            offset += temp;
            i--;
            result[4] -= 12;
        }
        result[0] = i;
        result[3] = i - 1864;
        leap = leapMonth(i); // 闰哪个月
        result[6] = 0;
        for (i = 1; i < 13 && offset > 0; i++) {
            // 闰月
            if (leap > 0 && i == (leap + 1) && result[6] == 0) {
                --i;
                result[6] = 1;
                temp = leapDays((int) result[0]);
            } else {
                temp = monthDays((int) result[0], i);
            }
            // 解除闰月
            if (result[6] == 1 && i == (leap + 1))
                result[6] = 0;
            offset -= temp;
            if (result[6] == 0)
                result[4]++;
        }
        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (result[6] == 1) {
                result[6] = 0;
            } else {
                result[6] = 1;
                --i;
                --result[4];
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
            --result[4];
        }
        result[1] = i;
        result[2] = offset + 1;
        return result;
    }

    public String getChinaDate(int month, int day) {
        String a = "";
        if (day == 1) {
            return months[month - 1];
        }
        if (day == 10)
            return "初十";
        if (day == 20)
            return "二十";
        if (day == 30)
            return "三十";
        int two = (day) / 10;
        if (two == 0)
            a = "初";
        if (two == 1)
            a = "十";
        if (two == 2)
            a = "廿";
        if (two == 3)
            a = "三";
        int one = day % 10;
        switch (one) {
            case 1:
                a += "一";
                break;
            case 2:
                a += "二";
                break;
            case 3:
                a += "三";
                break;
            case 4:
                a += "四";
                break;
            case 5:
                a += "五";
                break;
            case 6:
                a += "六";
                break;
            case 7:
                a += "七";
                break;
            case 8:
                a += "八";
                break;
            case 9:
                a += "九";
                break;
        }
        return a;
    }

    private final static Pattern sFreg = Pattern.compile("^(\\d{2})(\\d{2})([\\s\\*])(.+)$");

    /**
     * 获取某天的农历，其中month是程序月，即比现实月少一
     *
     * @param year
     * @param month
     * @param day
     * @return
     */

    public static LunarDate getLunarDate(int year, int month, int day) {
        LunarDate lunarDate = new LunarDate();
        if (lunarUtil == null) {
            lunarUtil = new LunarUtil();
        }
        long[] date = lunarUtil.calElement(year, month + 1, day);
        lunarDate.setMonth((int) date[1]);
        lunarDate.setLunarDate((int) date[2]);
        lunarDate.setLunarDateName(lunarUtil.getChinaDate((int) date[1], (int) date[2]));
        return lunarDate;
    }


    /**
     * 获取某天的农历，其中month是程序月，即比现实月少一
     */
    public static void setLunarDate(LunarDate lunarDate, int year, int month, int day) {
        if (lunarUtil == null) {
            lunarUtil = new LunarUtil();
        }
        long[] date = lunarUtil.calElement(year, month + 1, day);
        lunarDate.setMonth((int) date[1]);
        lunarDate.setLunarDate((int) date[2]);

        String sFestival = ""; //公历节日
        String lFestival = ""; //农历节日
        String solarTerm;

        int sM = month + 1;
        int lM = (int) date[1];
        int lD = (int) date[2];
        Matcher m;
        for (String aSFtv : sFtv) {
            m = sFreg.matcher(aSFtv);
            if (m.find()) {
                if (sM == toInt(m.group(1)) && day == toInt(m.group(2))) {
                    sFestival = m.group(4);
                    break;
                }
            }
        }

        if (TextUtils.isEmpty(sFestival)) {
            for (String aLFtv : lFtv) {
                m = sFreg.matcher(aLFtv);
                if (m.find()) {
                    if (lM == toInt(m.group(1)) && lD == toInt(m.group(2))) {
                        lFestival = m.group(4);
                        break;
                    }
                }
            }
        } else {
            lunarDate.setLunarDateName(sFestival);
            return;
        }
        if (TextUtils.isEmpty(lFestival)) {
            solarTerm = getTermString(year, month, day);
        } else {
            lunarDate.setLunarDateName(lFestival);
            return;
        }

        if (TextUtils.isEmpty(solarTerm)) {
            lunarDate.setLunarDateName(lunarUtil.getChinaDate((int) date[1], (int) date[2]));
        } else {
            lunarDate.setLunarDateName(solarTerm);
        }

    }

    private static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getTermString(int solarYear, int solarMonth, int solarDay) {
        // 二十四节气
        String termString = "";
        if (getSolarTermDay(solarYear, solarMonth * 2) == solarDay) {
            termString = solarTerm[solarMonth * 2];
        } else if (getSolarTermDay(solarYear, solarMonth * 2 + 1) == solarDay) {
            termString = solarTerm[solarMonth * 2 + 1];
        }
        return termString;
    }

    private static int getSolarTermDay(int solarYear, int index) {
        long l = (long) 31556925974.7 * (solarYear - 1900) + solarTermInfo[index] * 60000L;
        l = l + UTC(1900, 0, 6, 2, 5, 0);
        return getUTCDay(new Date(l));
    }

    private static void makeUTCCalendar() {
        if (utcCal == null) {
            utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        }
    }

    public static long UTC(int y, int m, int d, int h, int min, int sec) {
        makeUTCCalendar();
        utcCal.clear();
        utcCal.set(y, m, d, h, min, sec);
        return utcCal.getTimeInMillis();
    }

    public static int getUTCDay(Date date) {
        makeUTCCalendar();
        utcCal.clear();
        utcCal.setTimeInMillis(date.getTime());
        return utcCal.get(Calendar.DAY_OF_MONTH);
    }
}
