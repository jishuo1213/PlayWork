package com.inspur.playwork.model.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Fan on 16-3-17.
 */
public class SmallMailBean implements Parcelable {

    public static final int SEND_MODE = 0;
    public static final int VIEW_MODE = 1;
    public static final int REPLY_MODE = 2;

    public String mailId;
    public String messageId;
    public long sendTime;
    public UserInfoBean sendUser;
    public ArrayList<UserInfoBean> toUserList;
    public String subject;
    public String content;
    public String sendToString;

    public String taskId;
    public String chatId;

    public int type;

    public String toUserNames;

    public ArrayList<TaskAttachmentBean> attchments;

    public String attchmentString = "[]";

    public SmallMailBean() {

    }

    public SmallMailBean(JSONObject data) {
        mailId = data.optString("_id");
        sendTime = data.optLong("CreateTime");
        sendUser = new UserInfoBean(data.optJSONObject("From"));
        JSONArray toArray = data.optJSONArray("To");
        initToUserList(toArray);
        subject = data.optString("Subject");
        if (data.optBoolean("IsEDCrypts"))
            content = EncryptUtil.aesDecrypt(data.optString("Content"));
        else
            content = data.optString("Content");
        sendToString = toArray.toString();
        JSONArray attchmentArray = data.optJSONArray("Attachment");
        if (attchmentArray != null) {
            attchmentString = attchmentArray.toString();
        } else {
            attchmentString = "[]";
            attchmentArray = new JSONArray();
        }
        initAttachmentList(attchmentArray);
    }

    private void initAttachmentList(JSONArray attchmentArray) {
        attchments = new ArrayList<>();
        int length = attchmentArray.length();
        for (int i = 0; i < length; ++i) {
            TaskAttachmentBean attachmentBean = new TaskAttachmentBean(attchmentArray.optJSONObject(i), taskId);
            attchments.add(attachmentBean);
        }
    }

    protected SmallMailBean(Parcel in) {
        mailId = in.readString();
        messageId = in.readString();
        sendTime = in.readLong();
        sendUser = in.readParcelable(UserInfoBean.class.getClassLoader());
        toUserList = in.createTypedArrayList(UserInfoBean.CREATOR);
        subject = in.readString();
        content = in.readString();
        sendToString = in.readString();
        taskId = in.readString();
        chatId = in.readString();
        type = in.readInt();
        toUserNames = in.readString();
    }

    public static final Creator<SmallMailBean> CREATOR = new Creator<SmallMailBean>() {
        @Override
        public SmallMailBean createFromParcel(Parcel in) {
            return new SmallMailBean(in);
        }

        @Override
        public SmallMailBean[] newArray(int size) {
            return new SmallMailBean[size];
        }
    };

    public void initToUserList(String toUserJson) {
        JSONArray toUserArray;
        try {
            toUserArray = new JSONArray(toUserJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        initToUserList(toUserArray);
    }

    public void initAttachmentList(String jsonArray) {
        try {
            initAttachmentList(new JSONArray(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initToUserList(JSONArray toUserArray) {
        int length = toUserArray.length();
        toUserList = new ArrayList<>();
        toUserNames = "";
        for (int i = 0; i < length; ++i) {
            UserInfoBean user = new UserInfoBean(toUserArray.optJSONObject(i));
            toUserList.add(user);
            if (i < (length - 1)) {
                toUserNames += user.name + ",";
            } else {
                toUserNames += user.name;
            }
        }
    }

    public void initSendToString() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < toUserList.size(); i++) {
            try {
                jsonArray.put(i, toUserList.get(i).getUserJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sendToString = jsonArray.toString();
    }

    public void initToUserNames() {
        toUserNames = "";
        for (int i = 0; i < toUserList.size(); i++) {
            if (i < (toUserList.size() - 1)) {
                toUserNames += toUserList.get(i).name + ",";
            } else {
                toUserNames += toUserList.get(i).name;
            }
        }
    }

    public String getToUserNames() {
        String toUserNames = "";
        for (int i = 0; i < toUserList.size(); i++) {
            if (i < (toUserList.size() - 1)) {
                toUserNames += toUserList.get(i).name + ",";
            } else {
                toUserNames += toUserList.get(i).name;
            }
        }
        return toUserNames;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mailId);
        dest.writeString(messageId);
        dest.writeLong(sendTime);
        dest.writeParcelable(sendUser, flags);
        dest.writeTypedList(toUserList);
        dest.writeString(subject);
        dest.writeString(content);
        dest.writeString(sendToString);
        dest.writeString(taskId);
        dest.writeString(chatId);
        dest.writeInt(type);
        dest.writeString(toUserNames);
    }

    @Override
    public String toString() {
        return "SmallMailBean{" +
                "mailId='" + mailId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", sendTime=" + sendTime +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", sendToString='" + sendToString + '\'' +
                ", taskId='" + taskId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", type=" + type +
                '}';
    }

    public void setAttachmentsTaskId(String taskId) {
        this.taskId = taskId;
        for (TaskAttachmentBean mBean : attchments) {
            mBean.taskId = taskId;
        }
    }

    public String getAttachmentFloder() {
        return FileUtil.getAttachmentPath() + taskId + File.separator;
    }
}
