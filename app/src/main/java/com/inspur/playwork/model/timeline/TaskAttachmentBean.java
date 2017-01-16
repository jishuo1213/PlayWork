package com.inspur.playwork.model.timeline;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;

/**
 * Created by Fan on 15-9-18.
 */
public class TaskAttachmentBean implements Parcelable {

    private static final String TAG = "TaskAttachmentBeanFan";

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    public String attachmentName;
    public String attachPath;
    public String docId;

    public long attachSize;

    public String taskId;

    public String localPath;

    public TaskAttachmentBean(JSONObject json, String taskId) {
        attachmentName = json.optString("am_name");
        attachPath = json.optString("am_path");
        docId = json.optString("docId");
        attachSize = json.optLong("am_size");
        if (attachPath.startsWith("http")) {
            URI uri = URI.create(attachPath);
            attachPath = "doc?" + uri.getQuery();
        }
        this.taskId = taskId;
    }

    public TaskAttachmentBean(LocalFileBean localFileBean, String docId) {
        this.docId = docId;
        attachmentName = localFileBean.name;
        attachSize = localFileBean.size;
        attachPath = "doc?doc_id=" + docId + "&system_name=weiyou";
    }

    public TaskAttachmentBean(String filePath, String taskId) {
        File file = new File(filePath);
        attachSize = file.length();
        attachmentName = file.getName();
        docId = "";
        this.taskId = taskId;
        attachPath = filePath;
        localPath = filePath;
    }

    public TaskAttachmentBean() {

    }

    protected TaskAttachmentBean(Parcel in) {
        //taskId = in.readString();
        attachmentName = in.readString();
        attachPath = in.readString();
        docId = in.readString();

        attachSize = in.readLong();

        taskId = in.readString();

        localPath = in.readString();
    }

    public String getAttachFilePath() {
        return FileUtil.getAttachmentPath() + taskId + File.separator + docId + "-" + attachmentName;
    }

    public String getAttachDownLoadUrl() {
        return AppConfig.UPLOAD_FILE_URI_SERVICE + attachPath;
    }

    public boolean isAttachmentDownloaded() {
        File file = new File(getAttachFilePath());
        return file.exists();
    }

    public static final Creator<TaskAttachmentBean> CREATOR = new Creator<TaskAttachmentBean>() {
        @Override
        public TaskAttachmentBean createFromParcel(Parcel in) {
            return new TaskAttachmentBean(in);
        }

        @Override
        public TaskAttachmentBean[] newArray(int size) {
            return new TaskAttachmentBean[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(taskId);
        dest.writeString(attachmentName);
        dest.writeString(attachPath);
        dest.writeString(docId);

        dest.writeLong(attachSize);

        dest.writeString(taskId);
        dest.writeString(localPath);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("am_name", attachmentName);
            jsonObject.put("docId", docId);
            jsonObject.put("am_size", attachSize);
            jsonObject.put("am_path", getAttachDownLoadUrl());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSize() {
        float result;
        if (attachSize <= KB) {
            return attachSize + "B";
        } else if (attachSize < MB) {
            result = attachSize / KB;
            return result + "KB";
        } else if (attachSize < GB) {
            result = attachSize / MB;
            return result + "MB";
        } else {
            result = attachSize / GB;
            return result + "GB";
        }
    }
}
