package com.inspur.playwork.view.login;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.login.LoginActions;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.core.PlayWorkServiceNew;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.stores.login.LoginStores;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.GuideActivity;

/**
 * Created by Fan on 15-9-25.
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivityFan";

    private boolean isLogin;

    private boolean isCreate;

    private boolean isStartMainActivity = false;

    private boolean isStartLoginActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: " + (Dispatcher.getInstance() == null));
        if (((PlayWorkApplication) getApplication()).isLogOut()) {
            ((PlayWorkApplication) getApplication()).resume();
        }
        LoginStores.getInstance().register();
        Dispatcher.getInstance().register(this);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.layout_welcome);
        isLogin = checkNeedLogin();
        isCreate = true;
        if (!isLogin) {
            startLoginActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogin && isCreate) {
            bindService(new Intent(this, PlayWorkServiceNew.class), connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void startLoginActivity() {
        isStartLoginActivity = true;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dispatcher.getInstance().unRegister(this);
        if (!isStartMainActivity && !isStartLoginActivity)
            ((PlayWorkServiceNew) binder.getService()).logout();
        if (isLogin)
            unbindService(connection);
    }

    private PlayWorkServiceNew.Binder binder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (PlayWorkServiceNew.Binder) service;
            Log.i(TAG, "onServiceConnected: ----------=======");
            if (isCreate) {
                checkPasswordToServer();
                isCreate = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void checkPasswordToServer() {
        UserInfoBean info = PreferencesHelper.getInstance().getCurrentUser();
        Dispatcher.getInstance().dispatchStoreActionEvent(LoginActions.LOGIN_TO_ADSERVER, info.id, info.passWord, false);
    }

    private boolean checkNeedLogin() {
        return PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.HAVE_LOGIN_AD_SERVER);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateUIAction action) {
        switch (action.getActionType()) {
            case LoginActions.LOGIN_AD_SERVER_SUCCESS:
                ((PlayWorkApplication) getApplication()).getDbOperation().init(getApplicationContext(), PreferencesHelper.getInstance().getCurrentUser(true).id);
//                if (((PlayWorkService) binder.getService()).getConnectedState()) {
//                    Dispatcher.getInstance().dispatchStoreActionEvent(LoginActions.LOGIN_TIME_LINE_SERVER);
//                } else {
//                    PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
//                    Dispatcher.getInstance().dispatchNetWorkAction(NetWorkActions.SEND_CONNECT_TO_TIMELINE_SERVER, ConnectType.CONNECT_FROM_WELCOME);
//                }

                Log.i(TAG, "onEventMainThread: " + ((PlayWorkServiceNew) binder.getService()).getConnectedState());
                if (((PlayWorkServiceNew) binder.getService()).getConnectedState() == 2) {
                    startMainActivity();
                    return;
                }
                Log.i(TAG, "onEventMainThread: LOGIN_AD_SERVER_SUCCESS");
                ((PlayWorkServiceNew) binder.getService()).setCurrentUser(PreferencesHelper.getInstance().getCurrentUser());
                break;
            case NetWorkActions.CONNECT_TO_TIMELINE_SERVER_SUCCESS:
                Dispatcher.getInstance().dispatchStoreActionEvent(LoginActions.LOGIN_TIME_LINE_SERVER);
                break;
            case LoginActions.LOGIN_FAILED:
                UItoolKit.showToastShort(this, "登录失败，请重新登录");
                PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_AD_SERVER, false);
                PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
                startLoginActivity();
                break;
            case LoginActions.LOGIN_TIMELINE_SUCCESS:
                startMainActivity();
                break;
            case NetWorkActions.CONNECT_TO_TIMELINE_SERVER_TIME_OUT:
                UItoolKit.showToastShort(this, "连接时间轴服务器超时");
                startLoginActivity();
                break;
        }
    }

    private void startMainActivity() {
        isStartMainActivity = true;

        LoginStores.getInstance().unRegister();
        if (PreferencesHelper.getInstance().readBooleanPreference(PreferencesHelper.IS_GUIDE_PAGE_SHOW)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, GuideActivity.class));
        }
//        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
