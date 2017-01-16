package com.inspur.playwork.view.profile.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by bugcode on 15-8-13.
 */
public class UpdateAccountStep1Activity extends Activity implements View.OnClickListener {

    private ImageButton backImageView;// 返回按钮
    private TextView titleTextView;// 标题
    private TextView updateTelTextView;// 更改手机号按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_telephone);
        initViews();
    }

    private void initViews() {
        backImageView = (ImageButton) findViewById(R.id.iv_left);
        titleTextView = (TextView) findViewById(R.id.tv_title);
        updateTelTextView = (TextView) findViewById(R.id.tv_update_tel);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("更改手机账号");
        updateTelTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.tv_update_tel:
                startActivity(new Intent(UpdateAccountStep1Activity.this,
                        UpdateAccountStep2Activity.class));
                break;
        }
    }
}
