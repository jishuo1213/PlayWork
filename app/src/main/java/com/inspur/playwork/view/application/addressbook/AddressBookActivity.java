package com.inspur.playwork.view.application.addressbook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.view.common.BaseActivity;

public class AddressBookActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_address_book);
        TextView titleView = (TextView) findViewById(R.id.tv_title);
        titleView.setText("通讯录");
        ImageButton button = (ImageButton) findViewById(R.id.iv_left);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        getFragmentManager().beginTransaction().add(R.id.fram_app_container, new AddressBookFragment()).commit();
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
