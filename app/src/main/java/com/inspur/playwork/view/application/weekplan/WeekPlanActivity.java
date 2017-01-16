package com.inspur.playwork.view.application.weekplan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by fan on 17-1-11.
 */
public class WeekPlanActivity extends Activity implements View.OnClickListener, WeekPlanListFragment.WeekPlanListEventListener {
    private static final String TAG = "WeekPlanActivity";
    TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_address_book);
        titleView = (TextView) findViewById(R.id.tv_title);
        titleView.setText("周计划");
        ImageButton button = (ImageButton) findViewById(R.id.iv_left);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        getFragmentManager().beginTransaction().add(R.id.fram_app_container, new WeekPlanListFragment()).commit();


    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onChangeWeek(int year, int weekNum) {
        titleView.setText(year + "年" + "第" + weekNum + "周");
    }
}
