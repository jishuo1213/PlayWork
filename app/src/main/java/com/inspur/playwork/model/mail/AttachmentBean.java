package com.inspur.playwork.model.mail;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 邮箱附件实体类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class AttachmentBean implements Parcelable {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    public String name;
    public String docId;
    public long size;
    public String smallMailId;

    protected AttachmentBean(Parcel in) {
        name = in.readString();
        docId = in.readString();
        size = in.readLong();
        smallMailId = in.readString();
    }

    public AttachmentBean(String smallMailId, JSONObject attachment) {
        this.smallMailId = smallMailId;
        name = attachment.optString("am_name");
        docId = attachment.optString("docId");
        size = attachment.optLong("size");
    }

    public static final Creator<AttachmentBean> CREATOR = new Creator<AttachmentBean>() {
        @Override
        public AttachmentBean createFromParcel(Parcel in) {
            return new AttachmentBean(in);
        }

        @Override
        public AttachmentBean[] newArray(int size) {
            return new AttachmentBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(docId);
        dest.writeLong(size);
        dest.writeString(smallMailId);
    }

    public String getDownLoadUrl() {
        return AppConfig.UPLOAD_FILE_URI_SERVICE + "doc?doc_id=" + docId + "&system_name=weiyou";
    }

    public String getLocalPath() {
        return FileUtil.getAttachmentPath() + smallMailId + File.separator + docId + "-" + name;
    }

    public String getFileSizeStr() {
        float result;
        if (size <= KB) {
            return size + "B";
        } else if (size < MB) {
            result = size / KB;
            return result + "KB";
        } else if (size < GB) {
            result = size / MB;
            return result + "MB";
        } else {
            result = size / GB;
            return result + "GB";
        }
    }

    public boolean isAttechmentExits() {
        return new File(getLocalPath()).exists();
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("am_name", name);
            jsonObject.put("docId", docId);
            jsonObject.put("size", size);
            jsonObject.put("size", size);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
