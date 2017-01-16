package com.inspur.playwork.model.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.db.bean.MailContacts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


/**
 * Created by Fan on 15-9-22.
 */
public class UserInfoBean implements Parcelable {

    public long avatar;
    public String id;
    public String uid;
    public String email;
    public String name;


    private String tel;
    public String department;
    private String departmentId;
    public String subDepartment;
    public String subDepartmentId;
    public String company;
//    private String companyId;

    public String passWord;


    public UserInfoBean() {

    }

    protected UserInfoBean(Parcel in) {
        avatar = in.readLong();
        id = in.readString();
        uid = in.readString();
        email = in.readString();
        name = in.readString();

        tel = in.readString();
        department = in.readString();
        departmentId = in.readString();
        passWord = in.readString();
        subDepartment = in.readString();
        subDepartmentId = in.readString();
        company = in.readString();
    }

    public UserInfoBean(JSONObject jsonObject) {
        avatar = jsonObject.optLong("avatar");
        id = jsonObject.optString("id");
        uid = jsonObject.optString("uid");
        email = jsonObject.optString("email");
        name = jsonObject.optString("name");
    }

    public UserInfoBean(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert jsonObject != null;
        avatar = jsonObject.optLong("avatar");
        id = jsonObject.optString("id");
        uid = jsonObject.optString("uid");
        email = jsonObject.optString("email");
        name = jsonObject.optString("name");
    }

    public UserInfoBean(MailContacts mc) {
        String _email = mc.getAddress();
        this.id = _email.split("@")[0];
        this.email = _email;
        this.name = mc.getPersonal();
    }

    public JSONObject getUserJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("avatar", avatar);
            result.put("id", id);
            result.put("uid", uid);
            result.put("email", email);
            result.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final Creator<UserInfoBean> CREATOR = new Creator<UserInfoBean>() {
        @Override
        public UserInfoBean createFromParcel(Parcel in) {
            return new UserInfoBean(in);
        }

        @Override
        public UserInfoBean[] newArray(int size) {
            return new UserInfoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(avatar);
        dest.writeString(id);
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(name);

        dest.writeString(tel);
        dest.writeString(department);
        dest.writeString(departmentId);
//        dest.writeString(company);
//        dest.writeString(companyId);

        dest.writeString(passWord);
        dest.writeString(subDepartment);
        dest.writeString(subDepartmentId);

        dest.writeString(company);
    }

    public boolean isAvatarFileExit() {
        File file = new File(getAvatarPath());
        return file.exists();
    }

    public String getAvatarPath() {
        return FileUtil.getAvatarFilePath() + id + "-" + avatar + ".png";
    }

    @Override
    public String toString() {
        return "UserInfoBean{" +
                "id='" + id + '\'' +
                ", avatar=" + avatar +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof UserInfoBean) {
            return this.id.equals(((UserInfoBean) o).id);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
