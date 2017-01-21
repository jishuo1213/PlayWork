package com.inspur.playwork.view.login;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.common.ConnectType;
import com.inspur.playwork.actions.login.LoginActions;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.core.PlayWorkServiceNew;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.stores.login.LoginStores;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.GuideActivity;
import com.inspur.playwork.view.common.progressbar.CommonDialog;


import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Fan on 15-9-11.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "LoginFragmentFan";

    private EditText userName, password;
    private LoginStores loginStores;

    private DialogFragment progressDialog;

    private ImageView imageView;

    private LoginHandler handler;

    private Dispatcher dispatcher;

    private boolean isShowInputMethod;

    private PreferencesHelper pfh;
    private UserInfoBean user;

    private boolean isNeedEncrypt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginStores = LoginStores.getInstance();
        loginStores.register();
        dispatcher = Dispatcher.getInstance();
        dispatcher.register(this);
        pfh = PreferencesHelper.getInstance();
        user = pfh.getCurrentUser();

        handler = new LoginHandler(new WeakReference<>(this));
        boolean res = getActivity().bindService(new Intent(getActivity(), PlayWorkServiceNew.class), connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "onCreate: " + res);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_login, container, false);
        initView(v);
        return v;
    }

    @Override
    public void onViewCreated(final View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        userName.setFocusable(true);
        userName.setFocusableInTouchMode(true);
        userName.requestFocus();
        userName.postDelayed(new Runnable() {
            @Override
            public void run() {
                isShowInputMethod = true;
                showKeyboard(userName);
            }
        }, 500);
        final ViewTreeObserver observer = v.getViewTreeObserver();
        final Rect r = new Rect();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (isShowInputMethod) {
                    v.getWindowVisibleDisplayFrame(r);
                    int screenHeight = DeviceUtil.getDeviceScreenHeight(getActivity().getApplicationContext());
                    int heightDifference = screenHeight - r.bottom;

                    pfh.writeToPreferences(PreferencesHelper.INPUT_HEIGHT, heightDifference);
                    if (Build.VERSION.SDK_INT >= 16) {
                        v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Dispatcher.getInstance().isRegistered(loginStores))
            loginStores.unRegister();
        if (Dispatcher.getInstance().isRegistered(this))
            dispatcher.unRegister(this);
        Log.i(TAG, "onDestroy: " + pfh.readBooleanPreference(PreferencesHelper.HAVE_LOGIN_AD_SERVER));
        if (!pfh.readBooleanPreference(PreferencesHelper.HAVE_LOGIN_AD_SERVER)) {
            disconnectTimeLine(ConnectType.DISCONNECT_WHEN_EXIT_LOGIN);
        }
        getActivity().unbindService(connection);
    }

    private void disconnectTimeLine(int type) {
        Log.i(TAG, "disconnectTimeLine: ");
        ((PlayWorkServiceNew) binder.getService()).logout();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
      /*  switch (checkedId) {
            case R.id.local_net:
                break;
            case R.id.wide_net:
                break;
        }*/
    }

    private static class LoginHandler extends Handler {

        private WeakReference<LoginFragment> reference;

        public LoginHandler(WeakReference<LoginFragment> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    reference.get().loginSuccessHandler();
                    break;
                case 1:
                    reference.get().loginFailedHandler(msg.arg1);
                    break;
                case 2:
                    reference.get().doAfterAdLog();
                    break;
                case 3:
                    reference.get().loginTimeLineServer();
                    break;
                case 4:
                    reference.get().connectToTimeLineTimeout();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private PlayWorkServiceNew.Binder binder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (PlayWorkServiceNew.Binder) service;
            Log.i(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @SuppressWarnings("unused")
    public void onEvent(UpdateUIAction updateUIAction) {
        switch (updateUIAction.getActionType()) {
            case LoginActions.LOGIN_AD_SERVER_SUCCESS:
                handler.sendMessage(handler.obtainMessage(2));
                break;
            case LoginActions.LOGIN_TIMELINE_SUCCESS:
                handler.sendMessage(handler.obtainMessage(0));
                break;
            case LoginActions.LOGIN_FAILED:
                handler.sendMessage(handler.obtainMessage(1, (int) updateUIAction.getActionData().get(0), 0));
                break;
            case NetWorkActions.CONNECT_TO_TIMELINE_SERVER_SUCCESS:
                handler.sendMessage(handler.obtainMessage(3));
                break;
            case NetWorkActions.CONNECT_TO_TIMELINE_SERVER_TIME_OUT:
                handler.sendMessage(handler.obtainMessage(4));
                break;
        }
    }

    private void connectToTimeLineTimeout() {
        UItoolKit.showToastShort(getActivity(), "连接时间轴服务器超时,请检查是否需要切换内外网");
        dismissProgressDialog();
    }

    private void loginTimeLineServer() {
        Dispatcher.getInstance().dispatchStoreActionEvent(LoginActions.LOGIN_TIME_LINE_SERVER);
    }

    private void doAfterAdLog() {
        ((PlayWorkApplication) getActivity().getApplication()).getDbOperation().init(getActivity().getApplicationContext(), PreferencesHelper.getInstance().getCurrentUser(true).id);
        pfh.writeToPreferences(PreferencesHelper.USER_NAME, userName.getText().toString());
//        if (((PlayWorkService) binder.getService()).getConnectedState()) {
//            loginTimeLineServer();
//        } else {
//            Dispatcher.getInstance().dispatchNetWorkAction(NetWorkActions.SEND_CONNECT_TO_TIMELINE_SERVER, ConnectType.CONNECT_FROM_LOGIN);
//        }
        ((PlayWorkServiceNew) binder.getService()).setCurrentUser(PreferencesHelper.getInstance().getCurrentUser());
    }

    private void loginFailedHandler(int res) {
        pfh.writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
        pfh.writeToPreferences(PreferencesHelper.HAVE_LOGIN_AD_SERVER, false);
        switch (res) {
            case 1:
                UItoolKit.showToastShort(getActivity(), "网络连接出错，请检查网络");
                break;
            case 2:
                UItoolKit.showToastShort(getActivity(), "登陆失败，用户名或密码错误！");
                break;
            case 3:
                UItoolKit.showToastShort(getActivity(), "网络连接出错，请检查网络");
                break;
            case 4:
                UItoolKit.showToastShort(getActivity(), "获取用户信息出错，请联系85105848");
                break;
        }
        dismissProgressDialog();
    }

    private void loginSuccessHandler() {
        pfh.writeToPreferences(PreferencesHelper.HAVE_LOGIN_TIME_LINE, true);
        dismissProgressDialog();
        if (pfh.readBooleanPreference(PreferencesHelper.IS_GUIDE_PAGE_SHOW)) {
            startActivity(new Intent(getActivity(), MainActivity.class));
        } else {
            startActivity(new Intent(getActivity(), GuideActivity.class));
        }
        getActivity().finish();
    }

    private void initView(final View v) {
        userName = (EditText) v.findViewById(R.id.edit_login_username);
        password = (EditText) v.findViewById(R.id.edit_login_password);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        Button loginBtn = (Button) v.findViewById(R.id.btn_log_in);
  /*      netTypeRadio = (RadioGroup) v.findViewById(R.id.radio);*/
        loginBtn.setOnClickListener(this);
        userName.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcherPassword);
        isNeedEncrypt = true;
        if (user != null) {
            userName.setText(user.id);
            password.setText(user.passWord);
            Long avatar = ((PlayWorkApplication) getActivity().
                    getApplication()).
                    getAvatars().
                    get(user.id);
            isNeedEncrypt = false;
            if (avatar == null) {
                return;
            }
            File avatarFile = new File(FileUtil.getAvatarFilePath() + userName + "-" + avatar + ".png");
            if (avatarFile.exists()) {
                imageView.setImageURI(Uri.fromFile(avatarFile));
            }
        }

       /* netType = pfh.readIntPreference(PreferencesHelper.NET_WORK_TYPE);
        netTypeRadio.setOnCheckedChangeListener(this);
        if (netType == ConnectType.LOCAL_NET) {
            pfh.writeToPreferences(PreferencesHelper.NET_WORK_TYPE, ConnectType.LOCAL_NET);
            netTypeRadio.check(R.id.local_net);
            AppConfig.settingUrl(ConnectType.LOCAL_NET);
        } else {
            pfh.writeToPreferences(PreferencesHelper.NET_WORK_TYPE, ConnectType.WIDE_NET);
            netTypeRadio.check(R.id.wide_net);
            AppConfig.settingUrl(ConnectType.WIDE_NET);
        }*/
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(userName.getText()) || TextUtils.isEmpty(password.getText())) {
            UItoolKit.showToastShort(getActivity(), "帐号或密码不能为空");
            return;
        }
        if (((PlayWorkServiceNew) binder.getService()).getConnectedState() != 0) {
            UItoolKit.showToastShort(getActivity(), "未连接到时间轴服务器，请检查网络");
            return;
        }
        //让手机键盘消失的方法
        showProgressDialog();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        dispatcher.dispatchStoreActionEvent(LoginActions.LOGIN_TO_ADSERVER,
                userName.getText().toString(), password.getText().toString(), isNeedEncrypt);
    }

    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = CommonDialog.getInstance("正在登录...");
            progressDialog.setCancelable(false);
            progressDialog.show(getFragmentManager(), null);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private TextWatcher textWatcherPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            System.out.println("beforeTextChanged----"+s.toString());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            System.out.println("onTextChanged---"+s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            isNeedEncrypt = true;
        }
    };
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            System.out.println("beforeTextChanged----"+s.toString());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            System.out.println("onTextChanged---"+s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
//            System.out.println("onTextChanged---"+s.toString());
            String s1 = s.toString().toLowerCase();
            if (s1.length() > 0) {
                if (user != null && s1.equals(user.id.toLowerCase())) {
                    if (((PlayWorkApplication) getActivity().getApplication()).getAvatars() == null ||
                            !((PlayWorkApplication) getActivity().getApplication()).getAvatars().containsKey(s1)) {
                        imageView.setImageResource(R.drawable.icon_chat_default_avatar);
                        return;
                    }
                    long avatar = ((PlayWorkApplication) getActivity().getApplication()).getAvatars().get(s1);
                    File avatarFile = new File(FileUtil.getAvatarFilePath() + s1 + "-" + avatar + ".png");
                    if (avatarFile.exists()) {
                        imageView.setImageURI(Uri.fromFile(avatarFile));
                    }
                } else {
                    imageView.setImageResource(R.drawable.icon_chat_default_avatar);
                    password.setText("");
                    isNeedEncrypt = true;
                }
            }

        }
    };
}
