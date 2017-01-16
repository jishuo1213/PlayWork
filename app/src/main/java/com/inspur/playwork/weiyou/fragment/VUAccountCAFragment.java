package com.inspur.playwork.weiyou.fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.common.chosefile.ChoseFileDialogFragment;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.AccountCAAdapter;
import com.inspur.playwork.weiyou.rsa.CAObject;
import com.inspur.playwork.weiyou.store.AccountCaOperation;
import com.inspur.playwork.weiyou.view.VUConfirmDialog;

import java.util.ArrayList;
import java.util.List;

public class VUAccountCAFragment extends Fragment implements AccountCaOperation,View.OnClickListener, ChoseFileDialogFragment.ChoseFileResListener, VUConfirmDialog.ConfirmDialogListener {

    private static final String TAG = "VUSettingsFragment-->";
    private WeiYouMainActivity wyma;

    private TextView installCABtn;
    private ImageView backBtnTV;
    private ListView installedCALV;
    private VUConfirmDialog vucd;
    private AccountCAAdapter acaAdapter;
    private CheckBox encIV;
    private CheckBox signIV;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity) getActivity();
        wyma.vuStores.setAccountCaReference(this);
//        CAEncryptUtils.saveCaListData(account,new ArrayList<CAObject>());//测试用，清空证书列表
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View localView = paramLayoutInflater.inflate(R.layout.wy_fragment_account_safe, paramViewGroup, false);
        backBtnTV = ((ImageButton) localView.findViewById(R.id.wy_back_btn));
        backBtnTV.setOnClickListener(this);

        installCABtn = ((TextView) localView.findViewById(R.id.as_install_ca_btn));
        installCABtn.setOnClickListener(this);

        installedCALV = ((ListView) localView.findViewById(R.id.wy_installed_ca_lv));
        installedCALV.setEmptyView(localView.findViewById(R.id.wy_ca_lv_empty_view));
        List<CAObject> caList = wyma.vuStores.getCaList();
        acaAdapter = new AccountCAAdapter(wyma, caList);
        installedCALV.setAdapter(acaAdapter);
        installedCALV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                wyma.vuStores.setDeleteIndex(i);
                vucd.showPopWindow(view);
                return true;
            }
        });
        installedCALV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                wyma.vuStores.caClickHandler(i);
            }
        });

        vucd = new VUConfirmDialog(wyma, "确定要删除此证书吗？", "取消", "确定");
        vucd.setConfirmDialogListener(this);
        encIV = (CheckBox) localView.findViewById(R.id.as_default_encrypt_iv);
        signIV = (CheckBox)localView.findViewById(R.id.as_default_sign_iv);
        encIV.setOnClickListener(this);
        signIV.setOnClickListener(this);
        if(caList.size()>0) {
            if (wyma.vuStores.getDefaultEncryptMail()) {
                encIV.setChecked(true);
            }
            if(wyma.vuStores.getDefaultSignMail()){
                signIV.setChecked(true);
            }
        }
        else{
            encIV.setEnabled(false);
            signIV.setEnabled(false);
        }
        wyma.vuStores.initAccountCaData();
        return localView;
    }

    @Override
    public void refreshCaListView() {
        acaAdapter.notifyDataSetChanged();
        boolean cbEnabled = acaAdapter.getCount()>0;
        encIV.setEnabled(cbEnabled);
        signIV.setEnabled(cbEnabled);
        encIV.setChecked(cbEnabled);
        signIV.setChecked(cbEnabled);
    }

    private LocalFileBean selectedCAFile;
    private PopupWindow inputCAPwdPopWindow = null;
    private EditText inputPwdET;

    /**
     * 创建PopupWindow
     */
    protected void initInputPwdPopWindow() {
        View popupWindow_view = wyma.getLayoutInflater().inflate(R.layout.wy_pw_input_pwd, null, false);
        inputCAPwdPopWindow = new PopupWindow(popupWindow_view, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);

        inputPwdET = (EditText) popupWindow_view.findViewById(R.id.wy_input_pwd_et);
        inputPwdET.requestFocus();//获取焦点
        InputMethodManager imm = (InputMethodManager) wyma.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//显示虚拟键盘

        popupWindow_view.findViewById(R.id.wy_input_pwd_ok_btn).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.wy_input_pwd_cancel_btn).setOnClickListener(this);

        inputCAPwdPopWindow.setAnimationStyle(R.style.MenuAnimationFade);
        inputCAPwdPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = wyma.getWindow().getAttributes();
                lp.alpha = 1f;
                wyma.getWindow().setAttributes(lp);
            }
        });
        ColorDrawable cd = new ColorDrawable(0x000000);
        inputCAPwdPopWindow.setBackgroundDrawable(cd);
        inputCAPwdPopWindow.setOutsideTouchable(false);//为了使popWindow点其他地方也不消失
        WindowManager.LayoutParams lp = wyma.getWindow().getAttributes();
        lp.alpha = 0.4f;
        wyma.getWindow().setAttributes(lp);
        inputCAPwdPopWindow.showAtLocation(this.getView(), Gravity.CENTER, 0, -130);

    }

    /***
     * 隐藏证书文件列表弹窗
     */
    private void hideInputPwdPopWindow() {
        inputCAPwdPopWindow.dismiss();
        inputCAPwdPopWindow = null;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wy_back_btn:
                wyma.onBackPressed();
                break;
            case R.id.as_install_ca_btn:
                if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 102)) {
                    choseFile();
                }
                break;
            case R.id.as_default_encrypt_iv:
                wyma.vuStores.setDefaultEncryptWay(((CheckBox)v).isChecked());
                break;
            case R.id.as_default_sign_iv:
                wyma.vuStores.setDefaultSignWay(((CheckBox)v).isChecked());
                break;
            case R.id.wy_input_pwd_ok_btn:
                wyma.vuStores.verifyAndSaveCert(selectedCAFile.currentPath, selectedCAFile.name, inputPwdET.getText().toString());
                hideInputPwdPopWindow();
                break;
            case R.id.wy_input_pwd_cancel_btn:
                hideInputPwdPopWindow();
                break;
        }
    }

    public void choseFile() {
        ChoseFileDialogFragment dialogFragment = new ChoseFileDialogFragment();
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setListener(VUAccountCAFragment.this);
    }

    @Override
    public void onFileSelect(ArrayList<LocalFileBean> choseFileList) {
        if (choseFileList.size() > 1) {
            wyma.toast("请一次选择一个证书文件");
            return;
        }
        selectedCAFile = choseFileList.get(0);
        if (!selectedCAFile.name.endsWith("pfx")) {
            wyma.toast("请选择扩展名为“pfx”的证书文件");
            return;
        }
        initInputPwdPopWindow();
    }

    @Override
    public void onButton2Click() {
        wyma.vuStores.uninstallCert();
        acaAdapter.notifyDataSetChanged();
    }

    @Override
    public void onButton1Click() {

    }
}