package com.inspur.playwork.view.application.news;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.model.news.LoadNewsRequest;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by fan on 17-1-15.
 */
public class NewListFragment extends Fragment implements TabRecyAdapter.TabEventListener,
        ViewPager.OnPageChangeListener, SwipeRefreshLayout.OnRefreshListener, NewsViewOperation, RecyclerNewsAdapter.NewsListEventListener, View.OnClickListener {
    private static final String TAG = "NewListFragment";

    public static final String TAB_COUNT = "tab_count";
    public static final String TAB_NAMES = "tab_name";


    public interface NewsListEventListener {
        void onTabChange(int index);

        void onNewsClick(int pos, ArrayList<DepartmentNewsBean> newsList);

        String getCompanyName(String company, String department);
    }

    private NewsListEventListener eventListener;

    public enum NewsType {
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

    private ArrayList[] newsListArray;//每个tab页新闻

    private int[] loadingPages;//每个tab页已经加载的新闻的页数

    private int currentPage;//当前tab页index

    private ArrayMap<String, LoadNewsRequest> requestArrayMap;
    private String currentNeedRequestId;

    //    private DialogFragment progressDialog;
    private SwipeRefreshLayout refreshLayout;
    private NewsType currentType;
    private ArrayList<View> recyclerViews;
    private ViewPager viewPager;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof NewsListEventListener) {
            eventListener = (NewsListEventListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabCount = getArguments().getInt(TAB_COUNT);
        tab_names = getArguments().getStringArray(TAB_NAMES);
        newsListArray = new ArrayList[tabCount];
        loadingPages = new int[4];
        currentType = NewsType.GroupNews;
        requestArrayMap = new ArrayMap<>();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    private TextView[] tabArray;

    private void initView(View view) {
//        RecyclerView tabViews = (RecyclerView) view.findViewById(R.id.recy_tabs);
//        tabViews.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//        TabRecyAdapter adapter = new TabRecyAdapter(tabViews);
//        adapter.setTabNames(tab_names);
//        adapter.setListener(this);
//        tabViews.setAdapter(adapter);

        TextView tab1 = (TextView) view.findViewById(R.id.tv_tab1);
        TextView tab2 = (TextView) view.findViewById(R.id.tv_tab2);
        TextView tab3 = (TextView) view.findViewById(R.id.tv_tab3);
        TextView tab4 = (TextView) view.findViewById(R.id.tv_tab4);
        tabArray = new TextView[]{tab1, tab2, tab3, tab4};
        int length = tab_names.length;
        for (int i = 0; i < length; i++) {
            tabArray[i].setText(tab_names[i]);
            tabArray[i].setOnClickListener(this);
        }

        recyclerViews = new ArrayList<>();
        viewPager = (ViewPager) view.findViewById(R.id.page_main_view);
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
        tabArray[currentPage].setSelected(true);

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
        loadMoreNews(LoadNewsRequest.INIT_LOAD, 1);
        refreshLayout.setRefreshing(true);
    }

    private void loadMoreNews(int loadType, int page) {
        Log.d(TAG, "loadMoreNews() called with: loadType = [" + loadType + "], page = [" + page + "]");
        String uuid = UUID.randomUUID().toString();
        currentNeedRequestId = uuid;
        LoadNewsRequest request = new LoadNewsRequest(uuid, page, currentType, loadType);
        requestArrayMap.put(uuid, request);
        ApplicationStores.getInstance().getNews(uuid, request.newsType.ordinal(), request.page, eventListener.getCompanyName(PreferencesHelper.
                getInstance().getCurrentUser().company, PreferencesHelper.getInstance().getCurrentUser().department));
    }

    @Override
    public void onTabClick(int pos) {
        Log.i(TAG, "onTabClick: " + pos);
        if (eventListener != null) {
            eventListener.onTabChange(pos);
        }
        viewPager.setCurrentItem(pos);
    }

    @Override
    public void onClick(View v) {
        int pos = 0;
        tabArray[currentPage].setSelected(false);
        switch (v.getId()) {
            case R.id.tv_tab1:
                pos = 0;
                break;
            case R.id.tv_tab2:
                pos = 1;
                break;
            case R.id.tv_tab3:
                pos = 2;
                break;
            case R.id.tv_tab4:
                pos = 3;
                break;
        }

        tabArray[pos].setSelected(true);
        if (eventListener != null) {
            eventListener.onTabChange(pos);
        }
        viewPager.setCurrentItem(pos);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabArray[currentPage].setSelected(false);
        if (eventListener != null)
            eventListener.onTabChange(position);
        currentPage = position;
        tabArray[position].setSelected(true);
        currentType = NewsType.getItem(position);
        RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(position);
        if (currentRecyclerView.getLayoutManager() == null) {//还没有加载过数据
            currentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            currentRecyclerView.setHasFixedSize(true);
//            currentRecyclerView.addOnScrollListener(recyclerScrollListener);
            loadingPages[currentPage] = 1;
            refreshLayout.setRefreshing(true);
            if (currentRecyclerView.getAdapter() == null) {
                loadMoreNews(LoadNewsRequest.INIT_LOAD, 1);
            }
        } else {
            Log.i(TAG, "onPageSelected: " + refreshLayout.isRefreshing());
            if (currentRecyclerView.getAdapter() == null) {
                loadingPages[currentPage] = 1;
                loadMoreNews(LoadNewsRequest.INIT_LOAD, 1);
            } else {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRefresh() {
        loadMoreNews(LoadNewsRequest.PULL_REFRESH_LOAD, 1);
        refreshLayout.setRefreshing(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void showNews(final String uuid, final ArrayList<DepartmentNewsBean> newsBeanArrayList) {
        Log.d(TAG, "showNews() called with: uuid = [" + uuid + "], newsBeanArrayList = [" + newsBeanArrayList.size() + "]");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadNewsRequest request = requestArrayMap.get(uuid);
                Log.i(TAG, "run: " + request + "" + currentNeedRequestId);
                if (request == null)
                    return;
                if (uuid.equals(currentNeedRequestId)) {//返回的数据是需要的数据
                    if (request.newsType == currentType) {//是当前的tab页
                        setCurrentTabResult(request, newsBeanArrayList);
                    } else {
                        setNotCurrentTabResult(request, newsBeanArrayList);
                    }
                } else {
                    if (request.newsType == currentType) {//返回页是一样的新闻类型但不是需要的数据
                        boolean isHaveMore = false;
                        for (LoadNewsRequest loadNewsRequest : requestArrayMap.values()) {
                            if (loadNewsRequest.newsType == currentType) {
                                if (!loadNewsRequest.uuid.equals(uuid)) {
                                    isHaveMore = true;
                                }
                            }
                        }
                        if (!isHaveMore) {//
                            if (currentPage == request.newsType.ordinal())
                                setCurrentTabResult(request, newsBeanArrayList);
                        } else {
                            if (request.loadType == LoadNewsRequest.PULL_REFRESH_LOAD) {//
                                if (refreshLayout.isRefreshing()) {
                                    refreshLayout.setRefreshing(false);
                                }
                            }
                        }
                    } else {
                        setNotCurrentTabResult(request, newsBeanArrayList);
                    }
                }
                requestArrayMap.remove(uuid);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setCurrentTabResult(LoadNewsRequest request, ArrayList<DepartmentNewsBean> newsBeanArrayList) {
        int index = currentPage;
        if (request.loadType == LoadNewsRequest.PULL_REFRESH_LOAD) {//下拉刷新，此时需要刷新数据
            newsListArray[index] = newsBeanArrayList;
            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(index);
            if (currentRecyclerView.getAdapter() == null) {
                setRecyclerViewInitAdapter(currentRecyclerView, index);
            } else {
                RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
                adapter.setNewsBeanArrayList(newsListArray[index]);
                adapter.notifyDataSetChanged();
            }
            refreshLayout.setRefreshing(false);
            loadingPages[index] = 1;
        } else if (request.loadType == LoadNewsRequest.LOAD_MORE_LOAD) {//加载更多
            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(index);
            loadingPages[index] = request.page;
            int prviousNewsLength;
            prviousNewsLength = newsListArray[index].size();
            newsListArray[index].addAll(newsBeanArrayList);
            RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
            adapter.notifyItemRangeInserted(prviousNewsLength, newsBeanArrayList.size());
            adapter.setFooterViewRefresh(false);
        } else {
            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(index);
            loadingPages[index] = request.page;
            newsListArray[index] = newsBeanArrayList;
            setRecyclerViewInitAdapter(currentRecyclerView, index);
            refreshLayout.setRefreshing(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void setNotCurrentTabResult(LoadNewsRequest request, ArrayList<DepartmentNewsBean> newsBeanArrayList) {
        int requestIndex = request.newsType.ordinal();
        Log.i(TAG, "run: requestIndex" + requestIndex);
        RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(requestIndex);
        if (currentRecyclerView.getAdapter() == null) {
            newsListArray[requestIndex] = newsBeanArrayList;
            setRecyclerViewInitAdapter(currentRecyclerView, requestIndex);
        } else {
            RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
            if (request.loadType == LoadNewsRequest.PULL_REFRESH_LOAD ||
                    request.loadType == LoadNewsRequest.INIT_LOAD) {//下拉刷新的数据
                loadingPages[requestIndex] = 1;
                newsListArray[requestIndex] = newsBeanArrayList;
                adapter.setNewsBeanArrayList(newsListArray[requestIndex]);
                adapter.notifyDataSetChanged();
            } else if (request.loadType == LoadNewsRequest.LOAD_MORE_LOAD) {
                int requestTabPage = loadingPages[requestIndex];
                if (request.page - requestTabPage == 1) {
                    int prviousNewsLength = newsListArray[requestIndex].size();
                    loadingPages[requestIndex] = request.page;
                    newsListArray[requestIndex].addAll(newsBeanArrayList);
                    adapter.notifyItemRangeInserted(prviousNewsLength, newsBeanArrayList.size());
                    adapter.setFooterViewRefresh(false);
                }
            }
        }
    }

    @Override
    public void showNewsError(final String uuid, final int errCode) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadNewsRequest request = requestArrayMap.get(uuid);
                switch (errCode) {
                    case NewsViewOperation.QUERY_RES_EMPTY:
                        if (request.newsType == currentType) {
                            if (request.loadType == LoadNewsRequest.LOAD_MORE_LOAD) {
                                RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(currentPage);
                                RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
                                adapter.setFooterViewRefresh(false);
                                UItoolKit.showToastShort(getActivity(), "没有更多可加载的数据");
                            }
                            UItoolKit.showToastShort(getActivity(), "当前没有可加载的数据");
                        }
                        break;
                    case NewsViewOperation.NET_ERROR:
                        if (request.loadType == LoadNewsRequest.INIT_LOAD) {
                            UItoolKit.showToastShort(getActivity(), "加载新闻失败，请检查网络");
                            refreshLayout.setRefreshing(false);
                        } else if (request.loadType == LoadNewsRequest.LOAD_MORE_LOAD) {
                            UItoolKit.showToastShort(getActivity(), "加载更多新闻失败，请检查网络");
                            RecyclerView currentRecyclerView = (RecyclerView) recyclerViews.get(currentPage);
                            RecyclerNewsAdapter adapter = (RecyclerNewsAdapter) currentRecyclerView.getAdapter();
                            adapter.setFooterViewRefresh(false);
                        } else {
                            UItoolKit.showToastShort(getActivity(), "刷新新闻失败，请检查网络");
                            refreshLayout.setRefreshing(false);
                        }
                        break;
                }
                requestArrayMap.remove(uuid);
            }
        });
    }

    private void setRecyclerViewInitAdapter(RecyclerView currentRecyclerView, int index) {
        RecyclerNewsAdapter adapter = new RecyclerNewsAdapter(currentRecyclerView);
        //noinspection unchecked
        adapter.setNewsBeanArrayList(newsListArray[index]);
        adapter.setListEventListener(NewListFragment.this);
        currentRecyclerView.setAdapter(adapter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onNewsClick(int pos) {
        if (eventListener != null) {
            eventListener.onNewsClick(pos, newsListArray[currentPage]);
        }
    }

    @Override
    public void onLoadMoreClick() {
        int currentLoadPage = loadingPages[currentPage] + 1;
        loadMoreNews(LoadNewsRequest.LOAD_MORE_LOAD, currentLoadPage);
    }

}
