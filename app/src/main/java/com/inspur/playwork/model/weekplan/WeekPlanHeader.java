package com.inspur.playwork.model.weekplan;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.model.common.UserInfoBean;

import org.json.JSONObject;

import java.text.Collator;

/**
 * Created by fan on 17-1-12.
 */
public class WeekPlanHeader implements Parcelable, Comparable<WeekPlanHeader> {
    private static final String TAG = "WeekPlanHeader";

    public static final int MY_PLAN_TITLE = 0;
    public static final int MY_PLAN = 1;
    public static final int OTHER_PLAN_TITLE = 2;
    public static final int OTHER_PLAN = 3;


    public String subject;

    public long updateTime;

    private String weekId;

    private int staus;

//    public String departMent;

//    private String fromName;

    public int type;

    public UserInfoBean from;

    private static WeekPlanHeader myPlanTitle;
    private static WeekPlanHeader otherPlanTitle;

    public WeekPlanHeader(JSONObject planJson) {
        subject = planJson.optString("Subject");
        updateTime = planJson.optLong("UpdateTime");
        weekId = planJson.optString("WeekId");
        staus = planJson.optInt("Status");
        from = new UserInfoBean(planJson.optJSONObject("From"));
        from.department = planJson.optJSONObject("From").optString("department");
//        departMent = planJson.optJSONObject("From").optString("department");
//        fromName = planJson.optJSONObject("From").optString("name");
        type = OTHER_PLAN;
    }

    public WeekPlanHeader(int type) {
        this.type = type;
    }

    public static WeekPlanHeader getInstance(int type) {
        switch (type) {
            case MY_PLAN_TITLE:
                if (myPlanTitle == null)
                    myPlanTitle = new WeekPlanHeader(type);
                return myPlanTitle;
            case OTHER_PLAN_TITLE:
                if (otherPlanTitle == null)
                    otherPlanTitle = new WeekPlanHeader(type);
                return otherPlanTitle;
        }
        return null;
    }

    private WeekPlanHeader(Parcel in) {
        subject = in.readString();
        updateTime = in.readLong();
        weekId = in.readString();
        staus = in.readInt();
        from = in.readParcelable(getClass().getClassLoader());
        type = in.readInt();
    }

    public static final Creator<WeekPlanHeader> CREATOR = new Creator<WeekPlanHeader>() {
        @Override
        public WeekPlanHeader createFromParcel(Parcel in) {
            return new WeekPlanHeader(in);
        }

        @Override
        public WeekPlanHeader[] newArray(int size) {
            return new WeekPlanHeader[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeLong(updateTime);
        dest.writeString(weekId);
        dest.writeInt(staus);
        dest.writeParcelable(from, flags);
        dest.writeInt(type);
    }

    private Collator cmp = Collator.getInstance(java.util.Locale.CHINA);

    @Override
    public int compareTo(WeekPlanHeader o) {
        int otherType = o.type;
        int thisType = type;
        if (otherType == 0)
            return 1;
        if (thisType == 0)
            return -1;

        if (thisType < otherType) {
            return -1;
        }
        if (thisType > otherType) {
            return 1;
        }

        if (otherType == thisType) {
            return cmp.compare(this.from.name, o.from.name);
        }
        return 0;
    }
}
