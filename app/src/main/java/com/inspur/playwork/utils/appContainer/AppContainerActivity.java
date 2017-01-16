package com.inspur.playwork.utils.appContainer;

import android.content.Intent;
import android.os.Bundle;
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
import com.inspur.playwork.utils.PreferencesHelper;

/**
 * Created by sunyuan on 2016/2/29 0029 19:06.
 * Email: sunyuan@inspur.com
 */
public class AppContainerActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "AppContainerActivity";

    private WebView mWebView;
    private String username;
    private String userpwd;
    private String appName;
    private String appLoginUrl;
    private String appHomeUrl;
    private int appCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        UserInfoBean selfUser = PreferencesHelper.getInstance().getCurrentUser();
        username = selfUser.id;
        userpwd = selfUser.passWord;
        appName = intent.getStringExtra("app_name");
        appCode = intent.getIntExtra("app_code", 0);
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
                Log.i(TAG, "shouldOverrideUrlLoading: ================================="+url);
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

        mWebView.loadUrl(AppConfig.APP_CONTAINER_ENTRANCE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.htime_app_back:
                mWebView.goBack();
                break;
            case R.id.htime_app_forward:
                mWebView.goForward();
                break;
            case R.id.htime_app_home:
                mWebView.loadUrl(appHomeUrl);
                break;
            case R.id.htime_app_refresh:
                mWebView.reload();
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
            Log.i(TAG, "getU: "+username);
            return username;
        }

        @JavascriptInterface
        public String getP() {
            Log.i(TAG, "getP: "+userpwd);
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
    }

}
