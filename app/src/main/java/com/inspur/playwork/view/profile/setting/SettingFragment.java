package com.inspur.playwork.view.profile.setting;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.versionUpdate.VersionPlaywork;
import com.inspur.playwork.view.profile.my.AboutFragment;

/**
 * Created by jianggf on 2015/11/26.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "SettingFragment";
    private ImageButton backImageView;
    private TextView titleTextView;
    private TextView setAccountTextView;// 账号设置
    private TextView messageNotifyTextView;// 消息通知
    private TextView commonSetTextView;// 通用
    private TextView appSetTextView;// 应用设置
    private TextView versionUpdateTextView;// 版本更新
    private TextView aboutTextView;// 关于
    private View rootView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.activity_setting, container, false);
        initView(rootView);
        return rootView;
    }

    /**
     * 初始化View组件
     */
    private void initView(View view) {
        backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        titleTextView = (TextView) view.findViewById(R.id.tv_title);
        setAccountTextView = (TextView) view.findViewById(R.id.tv_set_account);
        messageNotifyTextView = (TextView) view.findViewById(R.id.tv_message_notify);
        commonSetTextView = (TextView) view.findViewById(R.id.tv_common_set);
        appSetTextView = (TextView) view.findViewById(R.id.tv_app_set);
        versionUpdateTextView = (TextView) view.findViewById(R.id.tv_version_update);
        aboutTextView = (TextView) view.findViewById(R.id.tv_about);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("设置");
//        setAccountTextView.setOnClickListener(this);
        messageNotifyTextView.setOnClickListener(this);
        commonSetTextView.setOnClickListener(this);
        appSetTextView.setOnClickListener(this);
        versionUpdateTextView.setOnClickListener(this);
        aboutTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.animator.fragment_xfraction_in,
                R.animator.fragment_xfraction_out,
                R.animator.fragment_xfraction_pop_in,
                R.animator.fragment_xfraction_pop_out);

        switch (v.getId()) {
            case R.id.tv_set_account:
                ft.replace(R.id.setting_fragment_container, new SetAccountFragment()).addToBackStack(null).commit();
                break;
            case R.id.tv_message_notify:
                ft.replace(R.id.setting_fragment_container, new SetNotifyFragment()).addToBackStack(null).commit();
                break;
            case R.id.tv_common_set:
                ft.replace(R.id.setting_fragment_container, new SetCommonFragment()).addToBackStack(null).commit();
                break;
            case R.id.iv_left:
                CommonUtils.back();
                break;
            case R.id.tv_version_update:
                checkUpVersion();
                break;
            case R.id.tv_about:
                ft.replace(R.id.setting_fragment_container, new AboutFragment()).addToBackStack(null).commit();
                break;
            default:
                break;
        }
    }

    /*
    * 处理手动检测更新的方法
    * */
    public void checkUpVersion() {
        new VersionPlaywork(this.getActivity(), true, PreferencesHelper.getInstance().getVersionInfo(), new Handler());
    }

}
