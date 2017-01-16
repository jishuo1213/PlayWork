package com.inspur.playwork.view.application.news;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.R;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.view.common.progressbar.CommonDialog;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-15.
 */
public class NewListFragment extends Fragment implements TabRecyAdapter.TabEventListener,
        ViewPager.OnPageChangeListener, SwipeRefreshLayout.OnRefreshListener, NewsViewOperation, RecyclerNewsAdapter.NewsListEventListener {
    private static final String TAG = "NewListFragment";

    public static final String TAB_COUNT = "tab_count";
    public static final String TAB_NAMES = "tab_name";

    private enum NewsType {
        GroupNews,
        GroupAnnouncement,
        UnitNews,
        UnitAnnouncement;

        public static NewsType getItem(int index) {
            switch (index) {
                case 0:
                    return GroupNews;
                case 1:
                    return GroupAnnouncement;
                case 2:
                    return UnitNews;
                case 3:
                    return UnitAnnouncement;
            }
            return GroupNews;
        }
    }

    private int tabCount;
    private String[] tab_names;

//    private RecyclerView currentRecyclerView;

    private ArrayList[] newsListArray;//每个tab页新闻

    private int[] loadingPages;//每个tab页已经加载的新闻的页数

    private int currentPage;//当前tab页index

    private DialogFragment progressDialog;
    private SwipeRefreshLayout refreshLayout;
    private NewsType currentType;
    private ArrayList<View> recyclerViews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabCount = getArguments().getInt(TAB_COUNT);
        tab_names = getArguments().getStringArray(TAB_NAMES);
        newsListArray = new ArrayList[tabCount];
        loadingPages = new int[4];
        currentType = NewsType.GroupNews;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_refresh_recycleview, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationStores.getInstance().setNewsWeakReference(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationStores.getInstance().setNewsWeakReference(null);
    }

    private void initView(View view) {
        RecyclerView tabViews = (RecyclerView) view.findViewById(R.id.recy_tabs);
        tabViews.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        TabRecyAdapter adapter = new TabRecyAdapter(tabViews);
        adapter.setTabNames(tab_names);
        adapter.setListener(this);
        tabViews.setAdapter(adapter);
        recyclerViews = new ArrayList<>();
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.page_main_view);
        for (int i = 0; i < tabCount; i++) {
            recyclerViews.add(LayoutInflater.from(getActivity()).inflate(R.layout.layout_single_recyclerview, viewPager, false));
            loadingPages[i] = 1;
        }
        RecyViewPageAdapter viewPageAdapter = new RecyViewPageAdapter();
        viewPageAdapter.setViewList(recyclerViews);
        currentPage = 0;
        viewPager.setAdapter(viewPageAdapter);
        viewPager.setCurrentItem(currentPage);
        viewPager.addOnPageChangeListener(this);

        RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(0);
        currentRecyclerView.setHasFixedSize(true);
        currentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//        currentRecyclerView.addOnScrollListener(recyclerScrollListener);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_main_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadMoreNews(1);
    }

    private void loadMoreNews(int page) {
        ApplicationStores.getInstance().getNews(currentType.ordinal(), page, PreferencesHelper.
                getInstance().getCurrentUser().company);
    }

    @Override
    public void onTabClick(int pos) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        currentType = NewsType.getItem(position);
        RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(position);
        if (currentRecyclerView.getLayoutManager() == null) {
            currentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            currentRecyclerView.setHasFixedSize(true);
//            currentRecyclerView.addOnScrollListener(recyclerScrollListener);
            loadingPages[currentPage] = 1;
        }
        if (currentRecyclerView.getAdapter() == null) {
            loadMoreNews(1);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRefresh() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void showNews(final int page, final ArrayList<DepartmentNewsBean> newsBeanArrayList) {
        Log.d(TAG, "showNews() called with: page = [" + page + "], newsBeanArrayList = [" + newsBeanArrayList.size() + "]");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPages[currentPage] = page;
                int prviousNewsLength;
                if (newsListArray[currentPage] == null) {
                    prviousNewsLength = 0;
                    newsListArray[currentPage] = newsBeanArrayList;
                } else {
                    prviousNewsLength = newsListArray[currentPage].size();
                    newsListArray[currentPage].addAll(newsBeanArrayList);
                }
                RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(currentPage);
                if (currentRecyclerView.getAdapter() == null) {
                    RecyclerNewsAdapter adapter = new RecyclerNewsAdapter(currentRecyclerView);
                    adapter.setNewsBeanArrayList(newsListArray[currentPage]);
                    adapter.setListEventListener(NewListFragment.this);
                    currentRecyclerView.setAdapter(adapter);
                } else {
                    RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
                    if (prviousNewsLength > 0) {
                        Log.i(TAG, "run: " + prviousNewsLength);
                        adapter.notifyItemRangeInserted(prviousNewsLength, newsBeanArrayList.size());
//                        adapter.notifyDataSetChanged();
//                        Log.i(TAG, "run: " + adapter.getItemCount());
//                        adapter.setFooterViewRefresh(false);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = CommonDialog.getInstance("正在创建聊天...");
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

    @Override
    public void onNewsClick(DepartmentNewsBean newsBean) {

    }

    @Override
    public void onLoadMoreClick() {
        int currentLoadPage = loadingPages[currentPage] + 1;
        loadMoreNews(currentLoadPage);
    }

//    private int lastVisibleItem = 0;

//    private RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener() {
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(currentPage);
//            if (newState == RecyclerView.SCROLL_STATE_IDLE
//                    && lastVisibleItem + 1 == currentRecyclerView.getAdapter().getItemCount()) {
//                RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
//                adapter.setFooterViewRefresh(true);
//                loadMoreNews(++loadingPages[currentPage]);
//            }
//        }
//
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(currentPage);
//            super.onScrolled(recyclerView, dx, dy);
//            lastVisibleItem = ((LinearLayoutManager) currentRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
//        }
//    };
}
