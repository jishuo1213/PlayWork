package com.inspur.playwork.view.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.IMMLeaks;
import com.inspur.playwork.view.common.BaseActivity;

/**
 * Created by Fan on 15-9-11.
 */
public class LoginActivity extends BaseActivity {


    protected void onCreate(Bundle savedInstanceState) {
        IMMLeaks.fixFocusedViewLeak(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login_activity);
        getFragmentManager().beginTransaction().add(R.id.login_container, new LoginFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}