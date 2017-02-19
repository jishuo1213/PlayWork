package com.inspur.playwork.view.application.weekplan;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.inspur.playwork.R;
import com.inspur.playwork.model.weekplan.SimpleTaskBean;
import com.inspur.playwork.model.weekplan.WeekPlanDetailBean;
import com.inspur.playwork.model.weekplan.WeekPlanHeader;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.UItoolKit;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-11.
 */
public class WeekPlanDetailFragment extends Fragment implements WeekPlanDetailViewOperation, View.OnClickListener, WeekPlanDetailAdapter.TaskClickListener {

    private static final String TAG = "PlanDetailFragmentFan";

    public static final String CLICK_POS = "click_pos";

    //    private TextView title;
//    private TextView dateView;
    private TextView from;
    private TextView submitUser;
    private TextView shareUsers;
    private ViewSwitcher switcher;
    private RecyclerView recyclerView;
    private ArrayList<WeekPlanHeader> headerArrayList;
    private int currentPos;
    private boolean isOnCreate = false;
    private WeekPlanDetailEventListener listener;


//    private PopupWindow addTaskPopWindow;

    public interface WeekPlanDetailEventListener {
        void setTitleView(String title);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof WeekPlanDetailEventListener) {
            this.listener = (WeekPlanDetailEventListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headerArrayList = ApplicationStores.getInstance().getPlanArrayList();
        currentPos = getArguments().getInt(CLICK_POS);
        isOnCreate = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_week_plan_detail, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
//        title = (TextView) v.findViewById(R.id.tv_week_plan_name);
//        dateView = (TextView) v.findViewById(R.id.tv_week_plan_date);
        from = (TextView) v.findViewById(R.id.tv_plan_from);
        submitUser = (TextView) v.findViewById(R.id.tv_plan_to);
        shareUsers = (TextView) v.findViewById(R.id.tv_plan_share);
        ImageButton previousBtn = (ImageButton) v.findViewById(R.id.htime_app_back);
        ImageButton nextBtn = (ImageButton) v.findViewById(R.id.htime_app_forward);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        WeekPlanHeader weekPlanHeader = headerArrayList.get(currentPos);
        setHeaderViews(weekPlanHeader);
        switcher = (ViewSwitcher) v.findViewById(R.id.switch_week_plan_detail);
        recyclerView = (RecyclerView) v.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        if (isOnCreate) {
            ApplicationStores.getInstance().setWeekPlanDetailWeakReference(this);
        }
        showProgress();
        ApplicationStores.getInstance().getPlanDetail(weekPlanHeader.from.id, weekPlanHeader.weekTime);
    }

    private void showProgress() {
        if (switcher.getCurrentView().getId() == R.id.task_recycler_view) {
            switcher.showNext();
        }
    }

    private void setHeaderViews(WeekPlanHeader weekPlanHeader) {
//        title.setText(weekPlanHeader.subject);
        listener.setTitleView(weekPlanHeader.subject);
//        dateView.setText(DateUtils.getOneWeekTextString(weekPlanHeader.weekTime));
        from.setText(weekPlanHeader.from.name);
        if (weekPlanHeader.mainSubmitPerson != null)
            submitUser.setText(weekPlanHeader.mainSubmitPerson.name);
        else {
            submitUser.setText("");
        }
        shareUsers.setText(weekPlanHeader.getShareUserName());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isOnCreate) {
            ApplicationStores.getInstance().setWeekPlanDetailWeakReference(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnCreate = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationStores.getInstance().setWeekPlanDetailWeakReference(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGetWeekPlanDetail(final WeekPlanDetailBean detailBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getAdapter() == null) {
                    WeekPlanDetailAdapter adapter = new WeekPlanDetailAdapter(recyclerView);
                    adapter.setWeekPlanDetailBean(detailBean);
                    adapter.setListener(WeekPlanDetailFragment.this);
                    Log.i(TAG, "run: " + adapter.getItemCount());
                    recyclerView.setAdapter(adapter);
                } else {
                    WeekPlanDetailAdapter adapter = (WeekPlanDetailAdapter) recyclerView.getAdapter();
                    adapter.setWeekPlanDetailBean(detailBean);
                    adapter.notifyDataSetChanged();
                }
                showTaskList();
            }
        });
    }

    private void showTaskList() {
        if (switcher.getCurrentView().getId() == R.id.progressbar) {
            switcher.showPrevious();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.htime_app_back:
                int pos = getPreviousPos();
                if (pos == -1) {
                    UItoolKit.showToastShort(getActivity(), "没有更多可加载的周计划");
                } else {
                    showProgress();
                    currentPos = pos;
                    WeekPlanHeader header = headerArrayList.get(pos);
                    setHeaderViews(header);
                    ApplicationStores.getInstance().getPlanDetail(header.from.id, header.weekTime);
                }
                break;
            case R.id.htime_app_forward:
                int nextPos = getNextPos();
                if (nextPos == -1) {
                    UItoolKit.showToastShort(getActivity(), "没有更多可加载的周计划");
                } else {
                    showProgress();
                    currentPos = nextPos;
                    WeekPlanHeader header = headerArrayList.get(nextPos);
                    setHeaderViews(header);
                    ApplicationStores.getInstance().getPlanDetail(header.from.id, header.weekTime);
                }
                break;
        }
    }

    private int getPreviousPos() {
        int newPos = currentPos;
        do {
            newPos = newPos - 1;
            if (newPos < 0) {
//                UItoolKit.showToastShort(getActivity(), "没有更多可加载的周计划");
                return -1;
            } else {
                WeekPlanHeader header = headerArrayList.get(newPos);
                if (!header.isDateView()) {
                    return newPos;
                }
            }
        } while (newPos >= 0);


        return -1;
    }

    private int getNextPos() {
        int newPos = currentPos;
        do {
            newPos = newPos + 1;
            if (newPos >= headerArrayList.size()) {
//                UItoolKit.showToastShort(getActivity(), "没有更多可加载的周计划");
                return -1;
            } else {
                WeekPlanHeader header = headerArrayList.get(newPos);
                if (!header.isDateView()) {
                    return newPos;
                }
            }
            Log.i(TAG, "getNextPos: " + newPos);
        } while (newPos < headerArrayList.size());


        return -1;
    }

    @Override
    public void onTaskClick(SimpleTaskBean taskBean, int pos) {
        AddWeekPlanDialogFragment fragment = AddWeekPlanDialogFragment.getInstance(taskBean);
        fragment.show(getFragmentManager(), null);
    }

    public SimpleTaskBean getNextTaskBean() {
        return ((WeekPlanDetailAdapter) recyclerView.getAdapter()).getNextTask();
    }

    public SimpleTaskBean getPreviousTaskBean() {
        return ((WeekPlanDetailAdapter) recyclerView.getAdapter()).getPreviousTask();
    }

    public void refreshTaskBean() {
        ((WeekPlanDetailAdapter) recyclerView.getAdapter()).refreshTaskBean();
    }

}
