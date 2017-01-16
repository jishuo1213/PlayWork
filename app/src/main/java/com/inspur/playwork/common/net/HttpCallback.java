package com.inspur.playwork.common.net;

import android.util.Log;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by fan on 16-8-18.
 */
public class HttpCallback implements Callback {
    private static final String TAG = "HttpCallback";

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

    }
}
