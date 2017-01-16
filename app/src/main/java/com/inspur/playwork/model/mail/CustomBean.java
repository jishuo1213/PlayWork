package com.inspur.playwork.model.mail;

import com.google.gson.annotations.SerializedName;

/**
 * 自定义属性实体类
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class CustomBean {

    private int setPrivate;
    private long startTime;
    private String creatorName;
    private long endDay;
    private long startDay;
    private long timeDay;
    @SerializedName("XH")
    private int xH;
    private String taskPlace;// 地点
    private long endTime;
    private int timeType;
    private String creator;// 创建人
    private String taskTitle;// 标题

    public int getSetPrivate() {
        return setPrivate;
    }

    public void setSetPrivate(int setPrivate) {
        this.setPrivate = setPrivate;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public long getEndDay() {
        return endDay;
    }

    public void setEndDay(long endDay) {
        this.endDay = endDay;
    }

    public long getStartDay() {
        return startDay;
    }

    public void setStartDay(long startDay) {
        this.startDay = startDay;
    }

    public long getTimeDay() {
        return timeDay;
    }

    public void setTimeDay(long timeDay) {
        this.timeDay = timeDay;
    }

    public int getxH() {
        return xH;
    }

    public void setxH(int xH) {
        this.xH = xH;
    }

    public String getTaskPlace() {
        return taskPlace;
    }

    public void setTaskPlace(String taskPlace) {
        this.taskPlace = taskPlace;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getTimeType() {
        return timeType;
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
}
