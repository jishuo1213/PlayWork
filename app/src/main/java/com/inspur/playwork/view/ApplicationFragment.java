package com.inspur.playwork.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.appContainer.AppContainerActivity;
import com.inspur.playwork.utils.appContainer.AppContainerPortiaActivity;
import com.inspur.playwork.view.application.addressbook.AddressBookActivity;
import com.inspur.playwork.view.application.news.GroupNewsActivity;
import com.inspur.playwork.view.application.weekplan.WeekPlanActivity;
import com.inspur.playwork.weiyou.WeiYouMainActivity;

/**
 * 应用列表fragment
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class ApplicationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ApplicationFragmentFan";

    private RelativeLayout weiyouRelativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        weiyouRelativeLayout = (RelativeLayout) view.findViewById(R.id.relative_weiyou);
        weiyouRelativeLayout.setOnClickListener(this);
        view.findViewById(R.id.relative_weizhi).setOnClickListener(this);
        view.findViewById(R.id.relative_weipan).setOnClickListener(this);
        view.findViewById(R.id.relative_inspur_weekplan).setOnClickListener(this);
        view.findViewById(R.id.relative_inspur_mbo).setOnClickListener(this);
        view.findViewById(R.id.relative_contacts).setOnClickListener(this);
        view.findViewById(R.id.relative_week_plan).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.relative_weiyou:
//                Log.i(TAG, "onClick:/**/ " + s.length());
                intent = new Intent(getActivity(), WeiYouMainActivity.class);
                startActivity(intent);
                break;
            case R.id.relative_weizhi:
                startActivity(getInitedIntent(R.id.relative_weizhi));
                break;
            case R.id.relative_weipan:
                Log.i("App onClick", " R.id.relative_weipan");
                startActivity(getInitedIntent(R.id.relative_weipan));
                break;
            case R.id.relative_inspur_weekplan:
                Log.i("App onClick", " R.id.relative_inspur_weekplan");
                startActivity(getInitedIntent(R.id.relative_inspur_weekplan));
                break;
            case R.id.relative_inspur_mbo:
                Log.i("App onClick", " R.id.relative_inspur_mbo");
                startActivity(getInitedIntent(R.id.relative_inspur_mbo));
                break;
            case R.id.relative_contacts:
                startActivity(new Intent(getActivity(), AddressBookActivity.class));
                break;
            case R.id.relative_week_plan:
                startActivity(new Intent(getActivity(), GroupNewsActivity.class));
                break;
        }
    }

    public Intent getInitedIntent(int id) {
        Intent intent = null;
        switch (id) {
            case R.id.relative_weizhi:
                intent = new Intent(getActivity(), AppContainerActivity.class);
                intent.putExtra("app_name", "微知");
                intent.putExtra("app_code", 1);
                intent.putExtra("app_url", AppConfig.APP_KW_LOGIN);
                break;
            case R.id.relative_weipan:
                intent = new Intent(getActivity(), AppContainerActivity.class);
                intent.putExtra("app_name", "微盘");
                intent.putExtra("app_code", 2);
                intent.putExtra("app_url", AppConfig.APP_DISK_LOGIN);
                break;
            case R.id.relative_inspur_weekplan:
                intent = new Intent(getActivity(), AppContainerPortiaActivity.class);
                intent.putExtra("app_name", "集团周计划");
                intent.putExtra("app_code", 3);
//                intent = new Intent(getActivity(), WeekPlanActivity.class);
                break;
            case R.id.relative_inspur_mbo:
                intent = new Intent(getActivity(), AppContainerPortiaActivity.class);
                intent.putExtra("app_name", "集团MBO");
                intent.putExtra("app_code", 4);
                break;
        }
        return intent;
    }
}