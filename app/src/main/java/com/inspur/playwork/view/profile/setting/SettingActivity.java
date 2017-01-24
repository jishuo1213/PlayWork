package com.inspur.playwork.view.profile.setting;

import android.app.Fragment;
import android.os.Bundle;

import com.inspur.playwork.R;
import com.inspur.playwork.view.common.BaseActivity;

/**
 * 设置
 * Created by bugcode on 15-8-12.
 */
public class SettingActivity extends BaseActivity {

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
