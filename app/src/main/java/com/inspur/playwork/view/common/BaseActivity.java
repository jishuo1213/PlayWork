package com.inspur.playwork.view.common;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by fan on 16-4-11.
 */
public class BaseActivity extends AppCompatActivity {



    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.i(getClass().getName(), "onUserLeaveHint: ");
//        sendBroadcast();
    }
}
