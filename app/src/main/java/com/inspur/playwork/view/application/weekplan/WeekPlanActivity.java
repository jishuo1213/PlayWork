package com.inspur.playwork.view.application.weekplan;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.weekplan.SimpleTaskBean;
import com.inspur.playwork.view.common.BaseActivity;

/**
 * Created by fan on 17-1-11.
 */
public class WeekPlanActivity extends BaseActivity implements View.OnClickListener,
        WeekPlanListFragment.WeekPlanListEventListener, AddWeekPlanDialogFragment.AddTaskEventListener,
        WeekPlanDetailFragment.WeekPlanDetailEventListener {

    private static final String TAG = "WeekPlanActivity";

    TextView titleView;
    private Fragment weekPlanListFragment;

    private Fragment weekPlanDetailFragment;

    private boolean isDetailViewShow = false;

    private int currentYear;
    private int currentWeekNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_address_book);
        titleView = (TextView) findViewById(R.id.tv_title);
        titleView.setText("周计划");
        ImageButton button = (ImageButton) findViewById(R.id.iv_left);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        weekPlanListFragment = new WeekPlanListFragment();
        getFragmentManager().beginTransaction().add(R.id.fram_app_container, weekPlanListFragment).commit();
    }

    @Override
    public void onClick(View v) {
        if (isDetailViewShow) {
            hideDetailFragment();
            return;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onChangeWeek(int year, int weekNum) {
        this.currentYear = year;
        this.currentWeekNum = weekNum;
//        titleView.setText(year + "年" + "第" + weekNum + "周");
        setTitleView(year + "年" + "第" + weekNum + "周");
    }

    @Override
    public void onWeekPlanClick(int pos) {
        if (weekPlanDetailFragment == null) {
            weekPlanDetailFragment = new WeekPlanDetailFragment();
            showDetailFragment(pos);
        } else {
            showDetailFragment(pos);
        }
    }

    private void showDetailFragment(int pos) {
        Bundle arg = new Bundle();
        arg.putInt(WeekPlanDetailFragment.CLICK_POS, pos);
        weekPlanDetailFragment.setArguments(arg);
        getFragmentManager().beginTransaction().hide(weekPlanListFragment).
                add(R.id.fram_app_container, weekPlanDetailFragment).commit();
        isDetailViewShow = true;
    }

    @Override
    public void setTitleView(String title) {
        titleView.setText(title);
    }

    @Override
    public void onBackPressed() {
        if (isDetailViewShow) {
            hideDetailFragment();
            return;
        }

        super.onBackPressed();
    }

    private void hideDetailFragment() {
        getFragmentManager().beginTransaction().
                show(weekPlanListFragment).remove(weekPlanDetailFragment).commit();
        isDetailViewShow = false;
        setTitleView(currentYear + "年" + "第" + currentWeekNum + "周");
    }

    @Override
    public SimpleTaskBean getNextTask() {
        return ((WeekPlanDetailFragment) weekPlanDetailFragment).getNextTaskBean();
    }

    @Override
    public SimpleTaskBean getPreviousTask() {
        return ((WeekPlanDetailFragment) weekPlanDetailFragment).getPreviousTaskBean();
    }

    @Override
    public void refreshTaskContent() {
        ((WeekPlanDetailFragment) weekPlanDetailFragment).refreshTaskBean();
    }
}
