package com.inspur.playwork.weiyou;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.chosepicture.ChosePictureFragment;
import com.inspur.playwork.weiyou.fragment.ContactSelectorFragment;
import com.inspur.playwork.weiyou.fragment.SelectLocalAttachmentFragment;
import com.inspur.playwork.weiyou.fragment.WriteMailFragment;
import com.inspur.playwork.weiyou.store.VUStores;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WriteMailActivity extends BaseActivity {

    private static final String TAG = "WriteMailActivity";
    private ProgressDialog progressDialog;
    private FragmentManager fm;
    private WriteMailFragment writeMailFragment;
    private ContactSelectorFragment contactSelectorFragment;
    private ChosePictureFragment choosePictureFragment;
    public SelectLocalAttachmentFragment selectAttachmentFragment;

    public boolean isInsertImage = false;//是否要插入图片

    public VUStores vuStores;
    public MailDetail paramMailDetail;
    public List<MailAttachment> paramAttachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        处理bundle参数
        Intent intent = getIntent();
        paramMailDetail = new MailDetail();

        String email = intent.getStringExtra("email");
        if(!TextUtils.isEmpty(email)){
            paramMailDetail.setEmail(email);
        }
        String from = intent.getStringExtra("from");
        if(!TextUtils.isEmpty(from)){
            paramMailDetail.setFrom(from);
        }
        String to = intent.getStringExtra("to");
        Log.i(TAG, "WriteMailActivity onCreate: to = "+to);
        if(!TextUtils.isEmpty(to)){
            paramMailDetail.setTo(to);
        }
        String cc = intent.getStringExtra("cc");
        if(!TextUtils.isEmpty(cc)){
            paramMailDetail.setCc(cc);
        }
        String subject = intent.getStringExtra("subject");
        if(!TextUtils.isEmpty(subject)){
            paramMailDetail.setSubject(subject);
        }
        String content = intent.getStringExtra("content");
        if(!TextUtils.isEmpty(content)){
            paramMailDetail.setContent(content);
        }
        String messageId = intent.getStringExtra("messageId");
        if(!TextUtils.isEmpty(messageId)){
            paramMailDetail.setMessageId(messageId);
        }
        String references = intent.getStringExtra("references");
        if(!TextUtils.isEmpty(references)){
            paramMailDetail.setReferences(references);
        }
        String uid = intent.getStringExtra("uid");
        if(!TextUtils.isEmpty(uid)){
            paramMailDetail.setUid(uid);
        }
        paramMailDetail.setEncrypted(intent.getBooleanExtra("encrypted",false));
        paramMailDetail.setSigned(intent.getBooleanExtra("signed",false));

        String [] attachments = intent.getStringArrayExtra("attachments");
        if(attachments != null) {
            int i, l = attachments.length;
            if (l > 0) {
                paramAttachments = new ArrayList<>();
                for (i = 0; i < l; i++) {
                    File file = new File(attachments[i]);
                    if (file.exists()) {
                        MailAttachment attachment = new MailAttachment();
                        attachment.setPath(attachments[i]);
                        attachment.setName(file.getName());
                        attachment.setSize(file.length());
                        paramAttachments.add(attachment);
                    }
                }
            }
        }

        setContentView(R.layout.wy_write_mail_activity);
        // 开启Fragment事务
        fm = getFragmentManager();
        vuStores = VUStores.getInstance();
        vuStores.register();
        vuStores.setQuoteType(intent.getIntExtra("type",0));
        openWriteMailView();
    }

    public void openWriteMailView() {
        FragmentTransaction ft = getFT();
        writeMailFragment = new WriteMailFragment();
        ft.add(R.id.wy_write_mail_container, writeMailFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void toast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WriteMailActivity.this,msg,Toast.LENGTH_SHORT).show();
            };
        });
    }

    public void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    public void hideInputMethod() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private FragmentTransaction getFT() {
        FragmentTransaction ft = fm.beginTransaction();
        /*添加的动画效果*/
        ft.setCustomAnimations(
                R.animator.fragment_xfraction_in,
                R.animator.fragment_xfraction_out,
                R.animator.fragment_xfraction_pop_in,
                R.animator.fragment_xfraction_pop_out);
        return ft;
    }

    public int selectAs = 0;
    /**
     * 显示联系人选择器的方法
     */
    public void showContactSelector(int cType) {
        if(contactSelectorFragment==null||!contactSelectorFragment.isAdded()) {
            selectAs = cType;
            FragmentTransaction ft = getFT();
            contactSelectorFragment = new ContactSelectorFragment();
            ft.add(R.id.wy_write_mail_container, contactSelectorFragment);
            ft.hide(writeMailFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    /**
     * 显示图片选择器的方法
     */
    public void showImageSelector() {
//        if(choosePictureFragment==null||!choosePictureFragment.isAdded()) {
        FragmentTransaction ft = getFT();
        if (choosePictureFragment == null) {
            choosePictureFragment = (ChosePictureFragment) ChosePictureFragment.getInstance("确定",true);
//                choosePictureFragment.setMultiChooseMode(true);
            choosePictureFragment.setListener(writeMailFragment);
//                choosePictureFragment.setSendButtonText("确定");
        }
        ft.add(R.id.wy_write_mail_container, choosePictureFragment);
        ft.hide(writeMailFragment);
        ft.addToBackStack(null);
        ft.commit();
//        }
    }

    public void openSelectAttachmentFragment() {
        FragmentTransaction ft = getFT();
        if (selectAttachmentFragment != null) {
//            selectAttachmentFragment = new SelectLocalAttachmentFragment();
            ft.add(R.id.wy_write_mail_container, selectAttachmentFragment);
            ft.hide(writeMailFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }


    @Override
    public void onBackPressed() {
        FragmentTransaction ft = getFT();
        if (selectAttachmentFragment != null && selectAttachmentFragment.isVisible()){
            super.onBackPressed();
            ft.show(writeMailFragment);
            selectAttachmentFragment = null;
        } else if (contactSelectorFragment != null && contactSelectorFragment.isResumed()) {
            super.onBackPressed();
            ft.show(writeMailFragment);
            contactSelectorFragment = null;
        } else if (choosePictureFragment != null && choosePictureFragment.isResumed()) {
            super.onBackPressed();
            ft.show(writeMailFragment);
            choosePictureFragment = null;
        } else if (writeMailFragment != null && writeMailFragment.isResumed()) {
            if (vuStores.isWritingMail) vuStores.onCancelSendMail();
            else {
                Log.i(TAG, "onBackPressed: writeMailFragment != null && writeMailFragment.isResumed()");
                finishWriteMail();
            }
        }
    }

    public void finishWriteMail(){
        vuStores.isWritingMail = false;
        finish();
    }
    /**
     * showLoading
     *
     * @param msg
     */
    public void showProgressDialog(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null && msg != null) {
                    progressDialog = new ProgressDialog(WriteMailActivity.this);
                    progressDialog.setTitle(null);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.setMessage(msg);
                    progressDialog.show();
                } else {
                    progressDialog.setMessage(msg);
                }
            }
        });
    }

    /**
     * hideLoading
     */
    public void dismissProgressDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "WriteMailActivity onDestroy: ");
        vuStores.cleanWriteMailStores();
        vuStores = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeMailFragment.openCamera();
                } else {
                    UItoolKit.showToastShort(this, "拍照请提供相机权限");
                }
                break;
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeMailFragment.choseFile();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isInsertImage = false;
                    showImageSelector();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            case 105:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isInsertImage = true;
                    showImageSelector();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
