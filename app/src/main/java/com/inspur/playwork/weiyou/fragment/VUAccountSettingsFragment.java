package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.store.AccountSettingsOperation;
import com.inspur.playwork.weiyou.view.VUConfirmDialog;

public class VUAccountSettingsFragment extends Fragment implements AccountSettingsOperation,View.OnClickListener, VUConfirmDialog.ConfirmDialogListener {
    private static final String INSPUR_OUTGOING_PORT = "587";
    private static final String POP3_INCOMING_PORT = "110";
    private static final String POP3_INCOMING_PORT_SSL = "995";
    private static final String POP3_OUTGOING_PORT = "25";
    private static final String POP3_OUTGOING_PORT_SSL = "465";
    private static final String TAG = "VUSettingsFragment-->";
    private EditText accountEV;
    private TextView accountEmailTV;
    private ImageView backBtnTV;
    private RelativeLayout caSettingRL;
    private RelativeLayout deleteAccountTV;
    private EditText displayNameEV;
    private LinearLayout finishSettingAccountTV;
    private Handler mHandler;
    private EditText nickNameEV;
    private EditText passwordEV;
    private CheckBox passwordVisibleBtn;
    private EditText receivePortEV;
    private CheckBox receiveSSLSB;
    private EditText receiveServerEV;
    private EditText sendPortEV;
    private CheckBox sendSSLSB;
    private EditText sendServerEV;
    private WeiYouMainActivity wyma;
    private VUConfirmDialog vuConfirmDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setAccountSettingsReference(this);
        mHandler = new Handler();
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View localView = paramLayoutInflater.inflate(R.layout.wy_fragment_account_settings, paramViewGroup, false);
        backBtnTV = ((ImageView) localView.findViewById(R.id.wy_back_btn));
        accountEmailTV = ((TextView) localView.findViewById(R.id.as_account_email));
//        displayNameEV = ((EditText) localView.findViewById(R.id.as_display_name));
        nickNameEV = ((EditText) localView.findViewById(R.id.as_nickname));
        accountEV = ((EditText) localView.findViewById(R.id.as_account_ev));
        passwordEV = ((EditText) localView.findViewById(R.id.as_password_ev));
        passwordVisibleBtn = ((CheckBox) localView.findViewById(R.id.as_show_password));
        receiveServerEV = ((EditText) localView.findViewById(R.id.as_receive_server_et));
        receivePortEV = ((EditText) localView.findViewById(R.id.as_receive_port_et));
        receiveSSLSB = ((CheckBox) localView.findViewById(R.id.as_receive_ssl_switch));
        sendServerEV = ((EditText) localView.findViewById(R.id.as_send_server_et));
        sendPortEV = ((EditText) localView.findViewById(R.id.as_send_port_et));
        sendSSLSB = ((CheckBox) localView.findViewById(R.id.as_send_ssl_switch));
        deleteAccountTV = ((RelativeLayout) localView.findViewById(R.id.as_delete_account));
        caSettingRL = ((RelativeLayout) localView.findViewById(R.id.as_account_ca));
        caSettingRL.setOnClickListener(this);
        finishSettingAccountTV = ((LinearLayout) localView.findViewById(R.id.as_finish_setting_btn));
        setViewEventListener();
        fillData(wyma.vuStores.getNewMailAccount());
        return localView;
    }

    private void fillData(MailAccount mailAccount) {
        if (mailAccount.getEmail().endsWith(AppConfig.EMAIL_SUFFIX)) {
            deleteAccountTV.setVisibility(View.GONE);
        }
        accountEmailTV.setText(mailAccount.getEmail());
//        displayNameEV.setText(mailAccount.getDisplayName());
        nickNameEV.setText(mailAccount.getNickName());
        receiveServerEV.setText(mailAccount.getInComingHost());
        receivePortEV.setText(mailAccount.getInComingPort());
        receiveSSLSB.setChecked(mailAccount.getInComingSSL());
        sendServerEV.setText(mailAccount.getOutGoingHost());
        sendPortEV.setText(mailAccount.getOutGoingPort());
        sendSSLSB.setChecked(mailAccount.getOutGoingSSL());
        try {
            accountEV.setText(EncryptUtil.aesDecryptAD(mailAccount.getAccount()));
            passwordEV.setText(EncryptUtil.aesDecryptAD(mailAccount.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        vuConfirmDialog = new VUConfirmDialog(wyma, "删除账户\n\r" + mailAccount.getEmail(), "删除", "取消");
        vuConfirmDialog.setConfirmDialogListener(this);
    }

    private void setViewEventListener() {
        backBtnTV.setOnClickListener(this);
        passwordVisibleBtn.setOnClickListener(this);
        receiveSSLSB.setOnClickListener(this);
        sendSSLSB.setOnClickListener(this);
        deleteAccountTV.setOnClickListener(this);
        finishSettingAccountTV.setOnClickListener(this);
    }

    private void saveChanges() {
//        String displayName = displayNameEV.getText().toString();
        String nickName = nickNameEV.getText().toString();
        String account = EncryptUtil.encrypt2aesAD(accountEV.getText().toString());
        String password = EncryptUtil.encrypt2aesAD(passwordEV.getText().toString());
        String inComingHost = receiveServerEV.getText().toString();
        String inComingPort = receivePortEV.getText().toString();
        boolean inComingSSL = receiveSSLSB.isChecked();
        String outGoingHost = sendServerEV.getText().toString();
        String outGoingPort = sendPortEV.getText().toString();
        boolean outGoingSSL = sendSSLSB.isChecked();
        wyma.vuStores.saveMailAccountChanges(nickName,account,password,inComingHost,
                inComingPort,inComingSSL,outGoingHost,outGoingPort,outGoingSSL);
    }

    @Override
    public void updateMailAccountCallback(boolean res) {
        if (res) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    wyma.onBackPressed();
                }
            }, 500);
        } else {
            wyma.toast("更新账号信息失败");
            finishSettingAccountTV.setEnabled(true);
        }
    }

    public void onClick(View paramView) {
        switch (paramView.getId()) {
            case R.id.wy_back_btn:
                wyma.onBackPressed();
                break;
            case R.id.as_show_password:
                if (passwordVisibleBtn.isChecked()) {
                    passwordEV.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordEV.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.as_receive_ssl_switch:
                if (receiveSSLSB.isChecked()) {
                    receivePortEV.setText(POP3_INCOMING_PORT_SSL);
                } else receivePortEV.setText(POP3_INCOMING_PORT);
                break;
            case R.id.as_send_ssl_switch:
                if (sendSSLSB.isChecked()) {
                    if (wyma.vuStores.isDefaultAccount()) {
                        sendPortEV.setText(INSPUR_OUTGOING_PORT);
                    } else sendPortEV.setText(POP3_OUTGOING_PORT_SSL);
                } else sendPortEV.setText(POP3_OUTGOING_PORT);
                break;
            case R.id.as_delete_account:
                vuConfirmDialog.showPopWindow(finishSettingAccountTV);
                break;
            case R.id.as_finish_setting_btn:
                saveChanges();
                paramView.setEnabled(false);
                break;
            case R.id.as_account_ca:
                //打开数字证书设置的fragment
                wyma.openAccountCASetting();
                break;
        }
    }

    @Override
    public void deleteMailAccountCallback(boolean res) {
        if (res) {
            wyma.onBackPressed();
        } else {
            wyma.toast("删除邮箱账号失败");
        }
    }

    @Override
    public void onButton1Click() {wyma.vuStores.deleteMailAccount();}

    @Override
    public void onButton2Click() {}
}