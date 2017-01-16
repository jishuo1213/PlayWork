package com.inspur.playwork.view.profile.my;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * 个人信息--更改地址
 * Created by bugcode on 15-8-12.
 */
public class UpdateAddressActivity extends Activity implements View.OnClickListener {

    private TextView titleTextView;// 标题
    private TextView cancelButton;// 取消按钮
    private TextView submitButton;// 确定按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_my_info);
        initViews();
    }

    /**
     * 初始化View组件
     */
    private void initViews() {
        titleTextView = (TextView) findViewById(R.id.tv_title);
        cancelButton = (TextView) findViewById(R.id.tv_left);
        submitButton = (TextView) findViewById(R.id.tv_right);

        titleTextView.setText("更改地址");
        cancelButton.setOnClickListener(this);
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
