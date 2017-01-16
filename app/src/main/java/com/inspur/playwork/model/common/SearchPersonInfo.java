package com.inspur.playwork.model.common;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by fan on 17-1-8.
 */
public class SearchPersonInfo implements Parcelable {
    private static final String TAG = "SearchPersonInfo";

    public String name;
    public String email;
    public String mobile;
    public String tel;
    public String orginfo;
    public String sex;

    protected SearchPersonInfo(Parcel in) {
        name = in.readString();
        email = in.readString();
        mobile = in.readString();
        tel = in.readString();
        orginfo = in.readString();
        sex = in.readString();
    }

    public SearchPersonInfo(JSONObject object) {
        name = object.optString("Name");
        email = object.optString("Email");
        mobile = object.optString("Mobil");
        tel = object.optString("Tel");
        orginfo = object.optString("Orginfo");
        sex = object.optString("Sex");
    }

    public static final Creator<SearchPersonInfo> CREATOR = new Creator<SearchPersonInfo>() {
        @Override
        public SearchPersonInfo createFromParcel(Parcel in) {
            return new SearchPersonInfo(in);
        }

        @Override
        public SearchPersonInfo[] newArray(int size) {
            return new SearchPersonInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(mobile);
        dest.writeString(tel);
        dest.writeString(orginfo);
        dest.writeString(sex);
    }

    @Override
    public String toString() {
        return "SearchPersonInfo{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", tel='" + tel + '\'' +
                ", orginfo='" + orginfo + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}
