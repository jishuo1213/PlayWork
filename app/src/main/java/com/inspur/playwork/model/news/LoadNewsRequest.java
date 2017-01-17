package com.inspur.playwork.model.news;

import com.inspur.playwork.view.application.news.NewListFragment;


/**
 * Created by fan on 17-1-17.
 */
public class LoadNewsRequest {
    private static final String TAG = "LoadNewsRequest";
    public static final int INIT_LOAD = 0;
    public static final int LOAD_MORE_LOAD = 1;
    public static final int PULL_REFRESH_LOAD = 2;

    public String uuid;
    public int page;//加载的新闻页
    public NewListFragment.NewsType newsType;//加载的新闻类型
    public int loadType;//加载类型 初始化刷新 加载更多刷新 下拉书信

    public LoadNewsRequest(String uuid, int page, NewListFragment.NewsType newsType, int loadType) {
        this.uuid = uuid;
        this.page = page;
        this.newsType = newsType;
        this.loadType = loadType;
    }

    @Override
    public String toString() {
        return "LoadNewsRequest{" +
                "uuid='" + uuid + '\'' +
                ", page=" + page +
                ", newsType=" + newsType +
                ", loadType=" + loadType +
                '}';
    }
}