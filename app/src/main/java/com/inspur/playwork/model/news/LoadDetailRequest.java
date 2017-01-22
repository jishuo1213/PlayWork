package com.inspur.playwork.model.news;

/**
 * Created by fan on 17-1-18.
 */
public class LoadDetailRequest {
    private static final String TAG = "LoadDetailRequest";
    public String id;
    public int pageIndex;
    public String url;

    public LoadDetailRequest(String id, int pageIndex) {
        this.id = id;
        this.pageIndex = pageIndex;
    }
}
