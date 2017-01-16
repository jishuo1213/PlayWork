package com.inspur.playwork.view.application.news;

import com.inspur.playwork.model.news.DepartmentNewsBean;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-16.
 */
public interface NewsViewOperation {
    void showNews(int page,ArrayList<DepartmentNewsBean> newsBeanArrayList);
}
