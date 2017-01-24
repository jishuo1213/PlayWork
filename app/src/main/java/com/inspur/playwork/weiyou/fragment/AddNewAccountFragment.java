package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.store.AddNewAccountOperation;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;

/**
 * Created by 孙 on 2016/1/20 0020.
 */
public class AddNewAccountFragment extends Fragment implements AddNewAccountOperation,View.OnClickListener {
    private static final String TAG = "AddNewAccountFragment";
    private static final String POP3_INCOMING_PORT = "110";
    private static final String POP3_INCOMING_PORT_SSL = "995";
    private static final String POP3_OUTGOING_PORT = "25";
    private static final String POP3_OUTGOING_PORT_SSL = "465";
    private WeiYouMainActivity wyma;

    private ImageButton backBtnTV;
    private EditText accountEmailEV;
    private EditText passwordEV;
    private CheckBox passwordVisibleBtn;
    private CheckBox settingModeBtn;
    private LinearLayout moreSettings;
    private EditText displayNameEV;
    private EditText nickNameEV;
    private EditText receiveServerEV;
    private EditText receivePortEV;
    private CheckBox receiveSSLSB;
    private EditText sendServerEV;
    private EditText sendPortEV;
    private CheckBox sendSSLSB;

    private LinearLayout finishAccountTV;

    private Handler mHandler;
    private MailAccount mailAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setAddNewAccountReference(this);
        mailAccount = new MailAccount();
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View localView = inflater.inflate(R.layout.wy_fragment_add_mail_account, container, false);
        backBtnTV = (ImageButton) localView.findViewById(R.id.wy_back_btn);
        accountEmailEV = (EditText) localView.findViewById(R.id.aa_account_email_et);
        passwordEV = (EditText) localView.findViewById(R.id.aa_password_et);
        passwordVisibleBtn = (CheckBox) localView.findViewById(R.id.aa_show_password);
        settingModeBtn = (CheckBox) localView.findViewById(R.id.aa_setting_mode_cb);
        moreSettings = (LinearLayout) localView.findViewById(R.id.aa_detail_settings);
        displayNameEV = (EditText) localView.findViewById(R.id.aa_display_name);
        nickNameEV = (EditText) localView.findViewById(R.id.aa_nickname);
        receiveServerEV = (EditText) localView.findViewById(R.id.aa_receive_server_et);
        receivePortEV = (EditText) localView.findViewById(R.id.aa_receive_port_et);
        receiveSSLSB = (CheckBox) localView.findViewById(R.id.aa_receive_ssl_switch);
        sendServerEV = (EditText) localView.findViewById(R.id.aa_send_server_et);
        sendPortEV = (EditText) localView.findViewById(R.id.aa_send_port_et);
        sendSSLSB = (CheckBox) localView.findViewById(R.id.aa_send_ssl_switch);
        finishAccountTV = (LinearLayout) localView.findViewById(R.id.aa_finish_btn);
        finishAccountTV.setEnabled(false);

        accountEmailEV.addTextChangedListener(tw);
        passwordEV.addTextChangedListener(twPWD);

        backBtnTV.setOnClickListener(this);
        passwordVisibleBtn.setOnClickListener(this);
        settingModeBtn.setOnClickListener(this);
        receiveSSLSB.setOnClickListener(this);
        sendSSLSB.setOnClickListener(this);
        finishAccountTV.setOnClickListener(this);
//        默认邮箱账号输入框获取焦点
        accountEmailEV.requestFocus();
        wyma.showKeyboard(accountEmailEV);
        return localView;
    }

    TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String email = accountEmailEV.getText().toString();
            if ( email.length() > 0 && WeiYouUtil.isEmail(email) && passwordEV.getText().length() > 0) {
                finishAccountTV.setEnabled(true);
            } else {
                finishAccountTV.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    TextWatcher twPWD = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String password = passwordEV.getText().toString();
            String email = accountEmailEV.getText().toString();
            if ( password.length() > 0 && email.length() > 0 && WeiYouUtil.isEmail(email)) {
                finishAccountTV.setEnabled(true);
            } else {
                finishAccountTV.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wy_back_btn:
                wyma.onBackPressed();
                break;
            case R.id.aa_show_password:
                if (passwordVisibleBtn.isChecked()) {
                    passwordEV.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordEV.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.aa_setting_mode_cb:
                if (settingModeBtn.isChecked()) {
                    moreSettings.setVisibility(View.GONE);
                } else {
                    moreSettings.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.aa_receive_ssl_switch:
                if (receiveSSLSB.isChecked()) {
                    receivePortEV.setText(POP3_INCOMING_PORT_SSL);
                } else receivePortEV.setText(POP3_INCOMING_PORT);
                break;
            case R.id.aa_send_ssl_switch:
                if (sendSSLSB.isChecked()) {
                    sendPortEV.setText(POP3_OUTGOING_PORT_SSL);
                } else sendPortEV.setText(POP3_OUTGOING_PORT);
                break;
            case R.id.aa_finish_btn:
                saveMailAccount();
                break;
        }
    }

    private void saveMailAccount() {
        String email = accountEmailEV.getText().toString();
        String[] emailArray = email.split("@");
        String account = emailArray[0];
        String suffix = emailArray[1];
        mailAccount.setEmail(email);
        mailAccount.setAccount(EncryptUtil.encrypt2aesAD(account));

        String password = passwordEV.getText().toString();
        mailAccount.setPassword(EncryptUtil.encrypt2aesAD(password));

        Log.d(TAG, "saveMailAccount() called with: " + email + "  password: " + password);
        String displayName = displayNameEV.getText().toString();
        if (TextUtils.isEmpty(displayName)) {
            displayName = email;
        }
        mailAccount.setDisplayName(displayName);

        String nickName = nickNameEV.getText().toString();
        if (TextUtils.isEmpty(nickName)) {
            nickName = account;
        }
        mailAccount.setNickName(nickName);

        boolean inComingSSL = receiveSSLSB.isChecked();
        String inComingHost = receiveServerEV.getText().toString();
        if (TextUtils.isEmpty(inComingHost)) {
            inComingHost = "pop." + suffix;
        }
        String inComingPort = receivePortEV.getText().toString();
        if (TextUtils.isEmpty(inComingPort)) {
            inComingPort = inComingSSL ? POP3_INCOMING_PORT_SSL : POP3_INCOMING_PORT;
        }
        boolean outGoingSSL = sendSSLSB.isChecked();
        String outGoingHost = sendServerEV.getText().toString();
        if (TextUtils.isEmpty(outGoingHost)) {
            outGoingHost = "smtp." + suffix;
        }
        String outGoingPort = receivePortEV.getText().toString();
        if (TextUtils.isEmpty(outGoingPort)) {
            outGoingPort = outGoingSSL ? POP3_OUTGOING_PORT_SSL : POP3_OUTGOING_PORT;
        }

        mailAccount.setInComingHost(inComingHost);
        mailAccount.setInComingPort(inComingPort);
        mailAccount.setInComingSSL(inComingSSL);
        mailAccount.setOutGoingHost(outGoingHost);
        mailAccount.setOutGoingPort(outGoingPort);
        mailAccount.setOutGoingSSL(outGoingSSL);

        if(outGoingHost.endsWith(AppConfig.EMAIL_SUFFIX)){
            mailAccount.setOutGoingTLS(true);
        }

        if (!inComingSSL)
            mailAccount.setProtocol("pop3");
        else
            mailAccount.setProtocol("pop3s");

        wyma.vuStores.addNewMailAccount(mailAccount);
    }

    @Override
    public void saveMailAccountCallback(boolean res) {
        if (res) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    wyma.dismissProgressDialog();
                    wyma.onBackPressed();
                }
            }, 500);
        } else {
            wyma.dismissProgressDialog();
            wyma.toast("邮箱账号保存失败");
        }
    }

    @Override
    public void onDestroy() {
        wyma.vuStores.setAddNewAccountReference(null);
        super.onDestroy();
    }
}
