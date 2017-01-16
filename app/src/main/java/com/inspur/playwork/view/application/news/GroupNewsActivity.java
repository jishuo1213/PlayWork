package com.inspur.playwork.view.application.news;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by fan on 17-1-16.
 */
public class GroupNewsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "GroupNewsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_address_book);
        TextView titleView = (TextView) findViewById(R.id.tv_title);
        titleView.setText("集团新闻");
        ImageButton button = (ImageButton) findViewById(R.id.iv_left);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        NewListFragment fragment = new NewListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(NewListFragment.TAB_COUNT, 4);
        bundle.putStringArray(NewListFragment.TAB_NAMES, new String[]{"集团新闻", "集团公告", "公司新闻", "公司公告"});
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.fram_app_container, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
