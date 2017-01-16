package com.inspur.playwork.view.profile.setting;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inspur.playwork.R;

/**
 * 设置
 * Created by bugcode on 15-8-12.
 */
public class SettingActivity extends AppCompatActivity {

    private final String TAG = "SettingActivityFAN";

    private Fragment settingFragment ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        settingFragment = new SettingFragment();
        getFragmentManager().beginTransaction().add(R.id.setting_fragment_container, settingFragment, SettingFragment.TAG).commit();

    }

}
