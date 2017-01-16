package com.inspur.playwork.model.common;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by fan on 17-1-13.
 */
public class VersionInfoBean implements Parcelable {
    private static final String TAG = "VersionInfoBean";

    public String VURL;
    public String Version;
    public String VersionName;
    public String Updatecontent;


    public VersionInfoBean(JSONObject versionJson) {
        VURL = versionJson.optString("VURL");
        Version = versionJson.optString("version");
        VersionName = versionJson.optString("versioname");
        Updatecontent = versionJson.optString("updatecontent");
    }

    private VersionInfoBean(Parcel in) {
        VURL = in.readString();
        Version = in.readString();
        VersionName = in.readString();
        Updatecontent = in.readString();
    }

    public static final Creator<VersionInfoBean> CREATOR = new Creator<VersionInfoBean>() {
        @Override
        public VersionInfoBean createFromParcel(Parcel in) {
            return new VersionInfoBean(in);
        }

        @Override
        public VersionInfoBean[] newArray(int size) {
            return new VersionInfoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(VURL);
        dest.writeString(Version);
        dest.writeString(VersionName);
        dest.writeString(Updatecontent);
    }
}
