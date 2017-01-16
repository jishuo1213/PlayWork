package com.inspur.playwork.weiyou.store;

/**
 * Created by sunyuan on 2016/11/23 0023 11:14.
 * Email: sunyuan@inspur.com
 */

public interface MailListOperation {

    void setListViewPullWay();

    void renderMailListView();

    void showDownloadInfo(int index, int sum);

    void showDownloadInfo(String msgStr,boolean isDownloadOver);

    void refreshMailItem(int index);

//    void setSentPercentage(int index ,int sentPercentage);
//
//    void deleteChildView(int index);
}
