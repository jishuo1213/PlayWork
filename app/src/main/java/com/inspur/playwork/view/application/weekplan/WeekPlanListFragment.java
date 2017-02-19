package com.inspur.playwork.view.application.weekplan;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.inspur.playwork.R;
import com.inspur.playwork.model.weekplan.WeekPlanHeader;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.timeline.TaskListViewPageAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by fan on 17-1-12.
 */
public class WeekPlanListFragment extends Fragment implements WeekPlanViewOperation, WeekPlanListAdapter.WeekPlanListListener {
    private static final String TAG = "WeekPlanListFragment";

    private static final int VIEW_LENGTH = 5;


    private ViewPager viewPager;
    private ArrayList<View> viewList;
    private RecyclerView weekPlanRecyclerView;
    private ViewSwitcher currentSelectView;
    private Calendar calendar;
    private WeekPlanListEventListener eventListener;


//    private int currentItem;

    public interface WeekPlanListEventListener {
        void onChangeWeek(int year, int weekNum);

        void onWeekPlanClick(int pos);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof WeekPlanListEventListener)
            eventListener = (WeekPlanListEventListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewList = new ArrayList<>();
        calendar = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_share_week_plan_list, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {
        viewPager = (ViewPager) v.findViewById(R.id.view_page_week_plan_list);
        for (int i = 0; i < VIEW_LENGTH; i++) {
            ViewSwitcher taskListView = (ViewSwitcher) LayoutInflater.from(getActivity()).inflate(R.layout.task_list_switch_recyclerview,
                    viewPager, false);
            taskListView.showNext();
            viewList.add(taskListView);
        }
        TaskListViewPageAdapter adapter = new TaskListViewPageAdapter();
        viewPager.setAdapter(adapter);
        adapter.setViewList(viewList);
//        currentItem = (Integer.MAX_VALUE - 2) / 2;
        viewPager.setCurrentItem((Integer.MAX_VALUE - 2) / 2);
        currentSelectView = (ViewSwitcher) viewList.get(2);
        viewPager.addOnPageChangeListener(new WeekPlanViewChangeListener());
        weekPlanRecyclerView = (RecyclerView) currentSelectView.findViewById(R.id.task_recycler_view);
        weekPlanRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadWeekPlanList();
    }

    private void loadWeekPlanList() {
        ApplicationStores.getInstance().getShareWeekPlanList(calendar.getTimeInMillis());
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationStores.getInstance().setWeekPlanReference(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (eventListener != null) {
            setTitleView();
        }

        if (weekPlanRecyclerView.getAdapter() != null) {
            weekPlanRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationStores.getInstance().setWeekPlanReference(null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showShareWeekPlanList(final ArrayList<WeekPlanHeader> list) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (list == null) {
                    UItoolKit.showToastShort(getActivity(), "获取周计划列表失败");
                    return;
                }
                Log.i(TAG, "run: " + list.size());
                if (weekPlanRecyclerView.getAdapter() == null) {
                    WeekPlanListAdapter adapter = new WeekPlanListAdapter(weekPlanRecyclerView);
                    adapter.setWeekPlanHeaders(list);
                    adapter.setListEventListener(WeekPlanListFragment.this);
                    weekPlanRecyclerView.setAdapter(adapter);
                } else {
                    WeekPlanListAdapter adapter = (WeekPlanListAdapter) weekPlanRecyclerView.getAdapter();
                    adapter.notifyDataSetChanged();
                }
                if (currentSelectView.getCurrentView().getId() == R.id.progressbar)
                    currentSelectView.showPrevious();
            }
        });
    }

    private class WeekPlanViewChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private int prePos = (Integer.MAX_VALUE - 2) / 2;

        WeekPlanViewChangeListener() {
            super();
        }

        @Override
        public void onPageSelected(int position) {
            currentSelectView = (ViewSwitcher) viewList.get(position % VIEW_LENGTH);
            if (currentSelectView.getCurrentView().getId() == R.id.task_recycler_view) {
                weekPlanRecyclerView = (RecyclerView) currentSelectView.getCurrentView();
                currentSelectView.showPrevious();
            } else {
                weekPlanRecyclerView = (RecyclerView) currentSelectView.findViewById(R.id.task_recycler_view);
            }
            if (weekPlanRecyclerView.getLayoutManager() == null) {
                weekPlanRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            }
            if (prePos > position) {
                calendar.add(Calendar.DATE, -7);
            } else {
                calendar.add(Calendar.DATE, 7);
            }
            if (eventListener != null) {
                setTitleView();
            }
            loadWeekPlanList();
            prePos = position;
        }
    }

    @Override
    public void onWeekPlanClick(int pos) {
        eventListener.onWeekPlanClick(pos);
    }

    private void setTitleView() {
        int[] yearWeek = DateUtils.getDayWeekNum(calendar.getTimeInMillis());
        eventListener.onChangeWeek(yearWeek[0], yearWeek[1]);
    }
}
