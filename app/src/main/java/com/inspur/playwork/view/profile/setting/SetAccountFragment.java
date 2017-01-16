package com.inspur.playwork.view.profile.setting;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.CommonUtils;

/**
 * Created by bugcode on 15-8-13.
 */
public class SetAccountFragment extends Fragment implements View.OnClickListener {

    private ImageButton backImageView;// 返回
    private RelativeLayout accountRelativeLayout;// 手机账号
    private RelativeLayout passwordRelativeLayout;// 修改密码

    private TextView tv_title;

    private View rootView ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.activity_set_account,container,false);
        initViews(rootView);
        return rootView ;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initViews(View view) {
        backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        tv_title = (TextView) view.findViewById(R.id.tv_title) ;
        accountRelativeLayout = (RelativeLayout) view.findViewById(R.id.re_account);
        passwordRelativeLayout = (RelativeLayout) view.findViewById(R.id.re_password);

        tv_title.setText("账号设置");
        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        accountRelativeLayout.setOnClickListener(this);
//        passwordRelativeLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                CommonUtils.back();
                break;
            case R.id.re_account:
                startActivity(new Intent(getActivity(), UpdateAccountStep1Activity.class));
                break;
            case R.id.re_password:
                startActivity(new Intent(getActivity(), UpdatePasswordActivity.class));
                break;
        }
    }
}
