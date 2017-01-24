package com.inspur.playwork.view.profile.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.view.common.BaseActivity;

/**
 * Created by bugcode on 15-8-13.
 */
public class UpdateAccountStep4Activity extends BaseActivity implements View.OnClickListener {

    private TextView leftTextView;// 左按钮
    private TextView titleTextView;// 标题
    private TextView rightTextView;// 右按钮
    private EditText contentEditText;// 修改内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_my_info);
        initViews();
    }

    private void initViews() {
        leftTextView = (TextView) findViewById(R.id.tv_left);
        titleTextView = (TextView) findViewById(R.id.tv_title);
        rightTextView = (TextView) findViewById(R.id.tv_right);
        contentEditText = (EditText) findViewById(R.id.et_content);

        leftTextView.setOnClickListener(this);
        titleTextView.setText("更改手机账号");
        rightTextView.setText("完成");
        contentEditText.setHint("请输入短信验证码");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left:
                finish();
                break;
        }
    }
}
