package com.inspur.playwork.model.news;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by fan on 17-1-16.
 */
public class DepartmentNewsBean implements Parcelable {
    private static final String TAG = "DepartmentNewsBean";
    public String title;
    public String date;
    public String url;
    public String id;
    public String key;
    public String departName;
    public String imageUrl;
    public String entityType;


    public DepartmentNewsBean(JSONObject jsonObject) {
        title = jsonObject.optString("title");
        date = jsonObject.optString("showDate");
        url = jsonObject.optString("entityUrl");
        id = jsonObject.optString("id");
        key = jsonObject.optString("key");
        departName = jsonObject.optString("deptName");
        imageUrl = jsonObject.optString("titleImageUrl");
        entityType = jsonObject.optString("entityType");
    }

    private DepartmentNewsBean(Parcel in) {
        title = in.readString();
        date = in.readString();
        url = in.readString();
        id = in.readString();
        key = in.readString();
        departName = in.readString();
        imageUrl = in.readString();
        entityType = in.readString();
    }

    public static final Creator<DepartmentNewsBean> CREATOR = new Creator<DepartmentNewsBean>() {
        @Override
        public DepartmentNewsBean createFromParcel(Parcel in) {
            return new DepartmentNewsBean(in);
        }

        @Override
        public DepartmentNewsBean[] newArray(int size) {
            return new DepartmentNewsBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(url);
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(departName);
        dest.writeString(imageUrl);
        dest.writeString(entityType);
    }
}
