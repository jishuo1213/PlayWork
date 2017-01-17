package com.inspur.playwork.view.application.news;

import com.inspur.playwork.model.news.DepartmentNewsBean;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-16.
 */
public interface NewsViewOperation {

    int QUERY_RES_EMPTY = 0;
    int NET_ERROR = 1;

    void showNews(String uuid, ArrayList<DepartmentNewsBean> newsBeanArrayList);
    void showNewsError(String uuid, int errCode);
}
