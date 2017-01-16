package com.inspur.playwork.model.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.utils.FileUtil;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by Fan on 15-11-16.
 */
public class SimpleUserInfoBean implements Parcelable{

    public String name;
    public long avatar;
    public String userId;


    public SimpleUserInfoBean(UserInfoBean userInfoBean) {
        name = userInfoBean.name;
        avatar = userInfoBean.avatar;
        userId = userInfoBean.id;
    }

    public SimpleUserInfoBean(JSONObject json) {
        name = json.optString("name");
        avatar = json.optLong("avatar");
        userId = json.optString("id");
    }

    protected SimpleUserInfoBean(Parcel in) {
        name = in.readString();
        avatar = in.readLong();
        userId = in.readString();
    }

    public static final Creator<SimpleUserInfoBean> CREATOR = new Creator<SimpleUserInfoBean>() {
        @Override
        public SimpleUserInfoBean createFromParcel(Parcel in) {
            return new SimpleUserInfoBean(in);
        }

        @Override
        public SimpleUserInfoBean[] newArray(int size) {
            return new SimpleUserInfoBean[size];
        }
    };

    public boolean isAvatarFileExit() {
        File file = new File(FileUtil.getAvatarFilePath() + userId + "-" + avatar + ".png");
        return file.exists();
    }

    public String getAvatarPath() {
        return FileUtil.getAvatarFilePath() + userId + "-" + avatar + ".png";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(avatar);
        dest.writeString(userId);
    }
}
