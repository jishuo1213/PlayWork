package com.inspur.playwork.utils.loadfile;

import android.os.AsyncTask;
import android.widget.TextView;

import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.UItoolKit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;

/**
 * Created by Fan on 15-10-19.
 */
public class DownLoadFileTask extends AsyncTask<String, Double, Boolean> {

    private WeakReference<TextView> progressRefrence;

    NumberFormat nt = NumberFormat.getPercentInstance();

    public DownLoadFileTask(TextView progressView) {
        progressRefrence = new WeakReference<>(progressView);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        nt.setMinimumFractionDigits(2);
        nt.setMaximumIntegerDigits(3);
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
        if (progressRefrence != null) {
            if (progressRefrence.get().getTag() == this)
                progressRefrence.get().setText(nt.format(values[0]) + "");
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (isCancelled())
            return;
        if (result && progressRefrence != null) {
            progressRefrence.get().setText("已下载");
            UItoolKit.showToastShort(progressRefrence.get().getContext(), "下载附件成功");
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String saveFilePath = params[0];
        String downLoadUrl = params[1];
        return OkHttpClientManager.getInstance().downloadFile(downLoadUrl, saveFilePath, new ProgressResponseListener() {
            @Override
            public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                if (!isCancelled()) {
                    if (contentLength > 0) {
                        double percent = (double) bytesRead / contentLength;
                        publishProgress(percent);
                    }
                }
            }
        });
    }
}
