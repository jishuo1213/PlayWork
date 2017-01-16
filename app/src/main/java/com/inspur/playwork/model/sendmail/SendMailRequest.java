package com.inspur.playwork.model.sendmail;

import com.google.gson.annotations.SerializedName;
import com.inspur.playwork.model.common.UserInfoBean;

/**
 * Created by Bugcode on 2016/3/18.
 */
public class SendMailRequest {
    @SerializedName("Subject")
    public String subject;
    @SerializedName("From")
    public UserInfoBean from;
    @SerializedName("To")
    public UserInfoBean[] to;
    @SerializedName("IsDel")
    public int isDel;
    @SerializedName("Attachment")
    public String[] attachment;
    @SerializedName("Custom")
    public CustomInfo custom;
    @SerializedName("Content")
    public String content;
    @SerializedName("PromptContent")
    public String summary;
    @SerializedName("Type")
    public int type;
    @SerializedName("TaskId")
    public String taskId;
    @SerializedName("ChatId")
    public String chatId;
    @SerializedName("CreateTime")
    public long createTime;
    @SerializedName("IsEDCrypts")
    public boolean isEDCrypts;
}
