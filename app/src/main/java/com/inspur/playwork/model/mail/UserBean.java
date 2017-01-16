package com.inspur.playwork.model.mail;

/**
 * 人员信息实体类
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class UserBean {
    private String id;
    private String uid;
    private String name;// 姓名
    private int avatar;// 头像
    private String email;// 邮箱
    private String type;// 类型
    private int mailCount;
    private int hasDraft;
    private int hasDel;
    private int hasMark;
    private int hasRead;
    private int isDraft;
    private int isDel;
    private int isMark;
    private int isRead;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMailCount() {
        return mailCount;
    }

    public void setMailCount(int mailCount) {
        this.mailCount = mailCount;
    }

    public int getHasDraft() {
        return hasDraft;
    }

    public void setHasDraft(int hasDraft) {
        this.hasDraft = hasDraft;
    }

    public int getHasDel() {
        return hasDel;
    }

    public void setHasDel(int hasDel) {
        this.hasDel = hasDel;
    }

    public int getHasMark() {
        return hasMark;
    }

    public void setHasMark(int hasMark) {
        this.hasMark = hasMark;
    }

    public int getHasRead() {
        return hasRead;
    }

    public void setHasRead(int hasRead) {
        this.hasRead = hasRead;
    }

    public int getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(int isDraft) {
        this.isDraft = isDraft;
    }

    public int getIsDel() {
        return isDel;
    }

    public void setIsDel(int isDel) {
        this.isDel = isDel;
    }

    public int getIsMark() {
        return isMark;
    }

    public void setIsMark(int isMark) {
        this.isMark = isMark;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
}
