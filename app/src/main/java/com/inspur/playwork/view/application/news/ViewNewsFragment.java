package com.inspur.playwork.view.application.news;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.model.news.LoadDetailRequest;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.view.common.VpSwipeRefreshLayout;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-17.
 */
public class ViewNewsFragment extends Fragment implements ViewPager.OnPageChangeListener,
        SwipeRefreshLayout.OnRefreshListener, NewsDetailOperation, VpSwipeRefreshLayout.CanChildScrollUpCallback {
    private static final String TAG = "ViewNewsFragment";

    private SwipeRefreshLayout refreshLayout;
    private ArrayList<View> viewList;
//    private View rootView;

    private ArrayList<DepartmentNewsBean> newsList;

    private TextView newsTitle;
    private TextView newsDate;

    private int currentPos;


    private ArrayMap<String, LoadDetailRequest> loadArrayMap;

    private boolean isOnCreate;
    private boolean isRestView;


    public interface DetailNewsEvent {
        int getFirstPos();

        ArrayList<DepartmentNewsBean> getNewsList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        isOnCreate = true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "onAttach: ");
        if (activity instanceof DetailNewsEvent) {
            DetailNewsEvent event = (DetailNewsEvent) activity;
            currentPos = event.getFirstPos();
            newsList = event.getNewsList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
//        if (rootView == null) {
        View rootView = inflater.inflate(R.layout.layout_view_news_detail, container, false);
        initView(rootView);
//        }
//        } else {
//            resetViews();
//        }
        ApplicationStores.getInstance().setNewsDetailWeakReference(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    private void loadData() {
        DepartmentNewsBean currentBean = newsList.get(currentPos);
        newsTitle.setText(currentBean.title);
        newsDate.setText(currentBean.date);
        Log.i(TAG, "loadData: " + currentPos);
        if (CommonUtils.isUrlVliad(currentBean.url)) {
            LoadDetailRequest request = new LoadDetailRequest(currentBean.id, currentPos);
            request.url = currentBean.url;
            loadArrayMap.put(currentBean.id, request);
            WebView webView = (WebView) viewList.get(currentPos % 5);
            webView.loadUrl(currentBean.url);
        } else {
            Log.i(TAG, "loadData: setRefreshing");
            refreshLayout.setRefreshing(true);
            loadDetail(currentBean);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: " + isRestView);
        if (!isRestView)
            loadData();
    }

    private void loadDetail(DepartmentNewsBean currentBean) {
        LoadDetailRequest newRequest = new LoadDetailRequest(currentBean.id, currentPos);
        loadArrayMap.put(currentBean.id, newRequest);
        ApplicationStores.getInstance().getNewsDetail(currentBean.id, currentBean.key);
    }

//    private void resetViews() {
//        isRestView = true;
//        for (View webView : viewList) {
//            ((WebView) webView).loadUrl("about:blank");
//        }
//        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.web_main_view);
//        NewsDetailPageAdapter adapter = new NewsDetailPageAdapter();
//        Log.i(TAG, "resetViews: " + viewList.size() + "========" + currentPos);
////        adapter.setViewList(viewList);
//        adapter.setCount(newsList.size());
////        viewPager.setCurrentItem(currentPos);
////        adapter.notifyDataSetChanged();
////        viewPager.setAdapter(adapter);
////        adapter.notifyDataSetChanged();
//        adapter.setViewList(viewList);
//        viewPager.setAdapter(adapter);
//        viewPager.setCurrentItem(currentPos);
//    }

//    private WebView viewPager;

    private void initView(View v) {
        loadArrayMap = new ArrayMap<>();
        viewList = new ArrayList<>();
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_content_layout);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.web_main_view);
        newsTitle = (TextView) v.findViewById(R.id.tv_news_title);
        newsDate = (TextView) v.findViewById(R.id.tv_date);
        NewsDetailPageAdapter adapter = new NewsDetailPageAdapter();
        for (int i = 0; i < 5; i++) {
            WebView webView = (WebView) LayoutInflater.from(getActivity()).inflate(R.layout.layout_single_webview, viewPager, false);
            setWebViewArgs(webView);
            viewList.add(webView);
        }
        adapter.setViewList(viewList);
        adapter.setCount(newsList.size());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPos);
        viewPager.addOnPageChangeListener(this);
        refreshLayout.setOnRefreshListener(this);
        ((VpSwipeRefreshLayout) refreshLayout).setCanChildScrollUpCallback(this);
    }

    private void setWebViewArgs(WebView mWebView) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setTextZoom(250);


        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setInitialScale(70);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); //取消滚动条白边效果
        mWebView.setWebViewClient(webViewClient);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        if (!isOnCreate)
            ApplicationStores.getInstance().setNewsDetailWeakReference(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        ApplicationStores.getInstance().setNewsDetailWeakReference(null);
    }


    @Override
    public void onRefresh() {
        DepartmentNewsBean bean = newsList.get(currentPos);
        loadDetail(bean);
    }

    @Override
    public void getDetail(final String id, final String url) {
        Log.d(TAG, "getDetail() called with: id = [" + id + "], url = [" + url + "]");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                LoadDetailRequest request = loadArrayMap.get(id);
                Log.i(TAG, "run: " + (currentPos % 5) + currentPos + request.pageIndex);
                if (currentPos == request.pageIndex) {//是需要的数据
                    request.url = url;
                    WebView view = (WebView) viewList.get(currentPos % 5);
                    view.loadUrl(url);
                } else {
                    request.url = url;
                }
            }
        });
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected: " + position);
        currentPos = position;
        DepartmentNewsBean bean = newsList.get(position);
        newsTitle.setText(bean.title);
        newsDate.setText(bean.date);
        if (CommonUtils.isUrlVliad(bean.url)) {
            LoadDetailRequest request = new LoadDetailRequest(bean.id, currentPos);
            request.url = bean.url;
            loadArrayMap.put(bean.id, request);
            WebView webView = (WebView) viewList.get(currentPos % 5);
            webView.loadUrl(bean.url);
            return;
        }
        LoadDetailRequest request = loadArrayMap.get(bean.id);

        if (request != null && !TextUtils.isEmpty(request.url)) {
            WebView view = (WebView) viewList.get(currentPos % 5);
            view.loadUrl(request.url);
        } else {
            refreshLayout.setRefreshing(true);
            loadDetail(bean);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private WebViewClient webViewClient = new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "shouldOverrideUrlLoading: =================================" + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return viewList.get(currentPos % 5).getScrollY() > 0;
    }
}
