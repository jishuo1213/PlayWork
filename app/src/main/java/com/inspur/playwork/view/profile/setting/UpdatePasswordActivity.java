package com.inspur.playwork.view.profile.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by bugcode on 15-8-13.
 */
public class UpdatePasswordActivity extends Activity implements View.OnClickListener {

    private ImageButton backImageView;
    private TextView titleTextView;
    private TextView rightTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        initViews();
    }

    private void initViews() {
        backImageView = (ImageButton) findViewById(R.id.iv_left);
        titleTextView = (TextView) findViewById(R.id.tv_title);
        rightTextView = (TextView) findViewById(R.id.tv_right);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("修改密码");
        rightTextView.setText("保存");
        rightTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                finish();
                break;
        }
    }
}
