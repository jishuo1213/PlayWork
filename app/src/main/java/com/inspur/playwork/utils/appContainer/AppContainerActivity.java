package com.inspur.playwork.utils.appContainer;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.progressbar.CommonDialog;

import java.lang.ref.WeakReference;

/**
 * Created by sunyuan on 2016/2/29 0029 19:06.
 * Email: sunyuan@inspur.com
 */
public class AppContainerActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "AppContainerActivity";

    private WebView mWebView;
    private String username;
    private String userpwd;
    private String appLoginUrl;
    private String appHomeUrl;
    private AppHandler handler;
    private boolean isNetCanUse = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new AppHandler(this);
        Intent intent = getIntent();
        UserInfoBean selfUser = PreferencesHelper.getInstance().getCurrentUser();
        username = selfUser.id;
        userpwd = selfUser.passWord;
        String appName = intent.getStringExtra("app_name");
        int appCode = intent.getIntExtra("app_code", 0);
        switch (appCode) {
            case 1:
                appLoginUrl = intent.getStringExtra("app_url");
                appHomeUrl = appLoginUrl.substring(0, appLoginUrl.length() - 6);
                break;
            case 2:
                appLoginUrl = intent.getStringExtra("app_url");
                appHomeUrl = appLoginUrl.substring(0, appLoginUrl.length() - 6);
                break;
            case 3:
                appLoginUrl = AppConfig.getAPP_INSPUR_WEEKPLAN(username);
                appHomeUrl = appLoginUrl;
                break;
            case 4:
                appLoginUrl = AppConfig.getAPP_INSPUR_MBO(username);
                appHomeUrl = appLoginUrl;
        }
        Log.i("AppContainerActivity", appName);
        Log.i("AppContainerActivity", appLoginUrl);

        setContentView(R.layout.activity_app_container);
        setProgressBarVisibility(true);
        mWebView = (WebView) findViewById(R.id.htime_app_wv);
//        返回按钮
        findViewById(R.id.htime_app_back).setOnClickListener(this);
        findViewById(R.id.htime_app_forward).setOnClickListener(this);
        findViewById(R.id.htime_app_home).setOnClickListener(this);
        findViewById(R.id.htime_app_refresh).setOnClickListener(this);
        findViewById(R.id.htime_app_close).setOnClickListener(this);

        TextView title = (TextView) findViewById(R.id.htime_app_home);
        title.setText(appName);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        //WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        //把图片加载放在最后来加载渲染
//        webSettings.setBlockNetworkImage(true);
//        webSettings.setSupportZoom(true);
        // 开启DOM缓存。
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(getApplicationContext().getCacheDir().getAbsolutePath());

        //设置WebView的一些缩放功能点
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setInitialScale(70);
        mWebView.setHorizontalScrollbarOverlay(true);
        //listview,webview中滚动拖动到顶部或者底部时的阴影
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); //取消滚动条白边效果

        mWebView.setWebViewClient(new WebViewClient() {
            // Load opened URL in the application instead of standard browser
            // application
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading: =================================" + url);
                view.loadUrl(url);
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            // Set progress bar during loading
            public void onProgressChanged(WebView view, int progress) {
                AppContainerActivity.this.setProgress(progress * 100);
            }
        });

        mWebView.addJavascriptInterface(new JSInterface(), "jsi");

        checkNet();
    }

    private DialogFragment progressDialog;

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = CommonDialog.getInstance("正在检测网络...");
            progressDialog.setCancelable(false);
            progressDialog.show(getFragmentManager(), null);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void checkNet() {
        showProgressDialog();
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                if (CommonUtils.runPingIProcess("10.100.1.11")) {
                    handler.sendEmptyMessage(1);
                    isNetCanUse = true;
                } else {
                    handler.sendEmptyMessage(2);
                    isNetCanUse = false;
                }
            }
        });
    }

    private void loadUrl(String appContainerEntrance) {
        mWebView.loadUrl(appContainerEntrance);
    }

    private static class AppHandler extends Handler {

        private WeakReference<AppContainerActivity> refre;

        public AppHandler(AppContainerActivity refre) {
            this.refre = new WeakReference<>(refre);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (refre.get() != null) {
                        refre.get().dismissProgressDialog();
                        refre.get().loadUrl(AppConfig.APP_CONTAINER_ENTRANCE);
                    }
                    break;
                case 2:
                    if (refre.get() != null) {
                        refre.get().dismissProgressDialog();
                        UItoolKit.showToastShort(refre.get(), "请在内网下使用微知微盘");
                        refre.get().loadUrl("file:///android_asset/offline.html");
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.htime_app_back:
                if (isNetCanUse)
                    mWebView.goBack();
                break;
            case R.id.htime_app_forward:
                if (isNetCanUse)
                    mWebView.goForward();
                break;
            case R.id.htime_app_home:
                if (isNetCanUse)
                    loadUrl(appHomeUrl);
                break;
            case R.id.htime_app_refresh:
                if (isNetCanUse)
                    mWebView.reload();
                else
                    checkNet();
                break;
            case R.id.htime_app_close:
                mWebView.clearCache(true);
                finish();
                break;
        }
    }

    final class JSInterface {

        @JavascriptInterface
        public String getU() {
            Log.i(TAG, "getU: " + username);
            return username;
        }

        @JavascriptInterface
        public String getP() {
            Log.i(TAG, "getP: " + userpwd);
//            Log.i(TAG, "downloadFile: " + Thread.currentThread().getName());
            return userpwd;
        }

        @JavascriptInterface
        public String getA() {
            return appLoginUrl;
        }

        @JavascriptInterface
        public void downloadFile(String url) {
            Log.i(TAG, "downloadFile: " + Thread.currentThread().getName());
        }

        @JavascriptInterface
        public void openVpn(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        }
    }
}
