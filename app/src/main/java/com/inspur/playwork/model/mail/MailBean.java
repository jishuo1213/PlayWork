package com.inspur.playwork.model.mail;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class MailBean {

    @SerializedName("_id")
    private int id;
    @SerializedName("MailCount")
    private int mailCount;
    @SerializedName("HasMark")
    private int hasMark;
    @SerializedName("Attachment")
    private List<AttachmentBean> attachmentList;// 附件列表
    @SerializedName("HasDraft")
    private int hasDraft;
    @SerializedName("MailRP")
    private List<UserBean> usersList;// 加入任务的人员列表
    @SerializedName("Subject")
    private String subject;// 任务主题
    @SerializedName("Type")
    private int type;// 任务类型标识 type=2的为任务
    @SerializedName("UpdateTime")
    private long updateTime;
    @SerializedName("Avatar")
    private int avatar;// 头像
    private CustomBean custom;// 自定义属性
    private UserBean from;// 创建人信息
    @SerializedName("HasRead")
    private int hasRead;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMailCount() {
        return mailCount;
    }

    public void setMailCount(int mailCount) {
        this.mailCount = mailCount;
    }

    public int getHasMark() {
        return hasMark;
    }

    public void setHasMark(int hasMark) {
        this.hasMark = hasMark;
    }

    public List<AttachmentBean> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<AttachmentBean> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public int getHasDraft() {
        return hasDraft;
    }

    public void setHasDraft(int hasDraft) {
        this.hasDraft = hasDraft;
    }

    public List<UserBean> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<UserBean> usersList) {
        this.usersList = usersList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public CustomBean getCustom() {
        return custom;
    }

    public void setCustom(CustomBean custom) {
        this.custom = custom;
    }

    public UserBean getFrom() {
        return from;
    }

    public void setFrom(UserBean from) {
        this.from = from;
    }

    public int getHasRead() {
        return hasRead;
    }

    public void setHasRead(int hasRead) {
        this.hasRead = hasRead;
    }
}
