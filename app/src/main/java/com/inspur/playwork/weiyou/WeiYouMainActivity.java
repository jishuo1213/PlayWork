package com.inspur.playwork.weiyou;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.NetWorkUtils;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.weiyou.adapter.DirectoryListAdapter;
import com.inspur.playwork.weiyou.fragment.AddNewAccountFragment;
import com.inspur.playwork.weiyou.fragment.ExchangeListFragment;
import com.inspur.playwork.weiyou.fragment.MailAttachmentFragment;
import com.inspur.playwork.weiyou.fragment.MailDetailFragment;
import com.inspur.playwork.weiyou.fragment.MailListFragment;
import com.inspur.playwork.weiyou.fragment.VUAccountCAFragment;
import com.inspur.playwork.weiyou.fragment.VUAccountSettingsFragment;
import com.inspur.playwork.weiyou.fragment.VUFeedbackFragment;
import com.inspur.playwork.weiyou.fragment.VUSettingsFragment;
import com.inspur.playwork.weiyou.store.VUActivityOperation;
import com.inspur.playwork.weiyou.store.VUStores;
import com.inspur.playwork.weiyou.utils.NetStatusReceiver;
import com.inspur.playwork.weiyou.view.AccountSelectorSpinner;
import com.inspur.playwork.weiyou.view.ass.AccountSpinnerAdapter;

import java.util.ArrayList;

//import com.inspur.playwork.view.message.chat.chosepicture.ChosePictureFragment;

public class WeiYouMainActivity extends BaseActivity implements VUActivityOperation,
        NetStatusReceiver.NetStatusListener,DirectoryListAdapter.DirClickListener{
    private static String TAG = "WeiYouMailActivity——";

    //    引用类型
    public static final int QUOTE_TYPE_NO_QUOTE = 0;
    public static final int QUOTE_TYPE_REPLY = 1;
    public static final int QUOTE_TYPE_REPLY_ALL = 2;
    public static final int QUOTE_TYPE_FORWARD = 3;
    public static final int QUOTE_TYPE_REEDIT = 4;

    public VUStores vuStores;
    private ArrayList<Integer> dirIdArrNormal;
    private ArrayList<Integer> dirIdArrDown;
    public ArrayList<MailAccount> mailAccountCache = new ArrayList<>();//当前登录账号关联的邮箱账号
    public MailAccount currMailAccount;//当前使用的邮箱账号
    public boolean isWritingMail = false;
    public boolean isWritingFeedback = false;

    private TextView currAccountTV;
    public ImageView accountSelectorIV;
    private RelativeLayout currAccountRL;
    private AccountSelectorSpinner mailAccountSpinner;
    private RecyclerView directoryRv;
    private LinearLayout vuSettingBtn;

    private DirectoryListAdapter dirListAdapter;
    private FragmentManager fm;
    private MailListFragment mailListFragment;
    private ExchangeListFragment exchangeListFragment;
    private MailDetailFragment mailDetailFragment;
    private MailAttachmentFragment mailAttachmentFragment;
    private VUAccountSettingsFragment vuAccountSettingsFragment;
    private VUAccountCAFragment vuAccountCAFragment;
    public VUSettingsFragment vuSettingsFragment;
    private VUFeedbackFragment vuFeedbackFragment;
    private AddNewAccountFragment addNewAccountFragment;

    public DrawerLayout drawer;

    private NetStatusReceiver netStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        vuStores = VUStores.getInstance();
        vuStores.register();
        netStatusReceiver= new NetStatusReceiver ();
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netStatusReceiver, filter);

        Log.i(TAG, "----------- onCreate ----------");
        setContentView(R.layout.wy_activity_main);
        dirIdArrDown = new ArrayList<>();
        dirIdArrDown.add(R.drawable.dir_inbox_down);
        dirIdArrDown.add(R.drawable.dir_draftbox_down);
        dirIdArrDown.add(R.drawable.dir_unread_down);
        dirIdArrDown.add(R.drawable.dir_sent_mail_down);
        dirIdArrDown.add(R.drawable.dir_outbox_down);
        dirIdArrDown.add(R.drawable.dir_deleted_mail_down);
        dirIdArrDown.add(R.drawable.dir_marked_mail_down);
//        dirIdArrDown.add(R.drawable.dir_attachment_list_down);
        dirIdArrNormal = new ArrayList<>();
        dirIdArrNormal.add(R.drawable.dir_inbox_normal);
        dirIdArrNormal.add(R.drawable.dir_draftbox_normal);
        dirIdArrNormal.add(R.drawable.dir_unread_normal);
        dirIdArrNormal.add(R.drawable.dir_sent_mail_normal);
        dirIdArrNormal.add(R.drawable.dir_outbox_normal);
        dirIdArrNormal.add(R.drawable.dir_deleted_mail_normal);
        dirIdArrNormal.add(R.drawable.dir_marked_mail_normal);
//        dirIdArrNormal.add(R.drawable.dir_attachment_list_normal);

        drawer = (DrawerLayout) findViewById(R.id.mail_list_layout);
        directoryRv = (RecyclerView) findViewById(R.id.sidebar_directory_rv);

        directoryRv.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
//        directoryRv.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
//        directoryRv.addItemDecoration(new ListViewDecoration(this));// 添加分割线。
        directoryRv.setLayoutManager(new LinearLayoutManager(this));

        mailAccountSpinner = new AccountSelectorSpinner(this);
        mailAccountSpinner.refreshData(mailAccountCache, 0);
        mailAccountSpinner.setItemListener(new AccountSpinnerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                vuStores.switchMailAccount(pos);
            }
        });

        currAccountTV = (TextView) findViewById(R.id.current_account_tv);
        accountSelectorIV = (ImageView) findViewById(R.id.account_selector_iv);
        currAccountRL = (RelativeLayout) findViewById(R.id.current_account_rl);
        currAccountRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailAccountSpinner.setWidth(currAccountTV.getWidth());
                mailAccountSpinner.showAsDropDown(currAccountTV);
                accountSelectorIV.setImageResource(R.drawable.dir_droplist_down);
            }
        });

        vuStores.setVUActivityReference(this);//侧边栏的视图操作
        vuStores.initVUData(false);
        // 开启Fragment事务
        fm = getFragmentManager();
    }

    @Override
    public void refreshSpinner(ArrayList<MailAccount> mailAccountCache, int index) {
        mailAccountSpinner.refreshData(mailAccountCache, index);
    }

    @Override
    public void switchSelectedAccount(String email) {
        drawer.closeDrawer(GravityCompat.START);
        currAccountTV.setText(email);
    }

    @Override
    public void initSettingButton(){
        Log.i(TAG, "getMailDirectoryListCallback 接下来 取邮件列表");
        vuSettingBtn = (LinearLayout) findViewById(R.id.wy_setting_btn);
        vuSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                Log.d(TAG, "vuSettingBtn onClick() called");
                openVUSettingFragment();
            }
        });
    }

    /**
     * 请求完邮件目录数据后，渲染视图
     */
    @Override
    public void renderMailDirectoryView() {

        dirListAdapter = new DirectoryListAdapter(this, vuStores.getDirListData());
        dirListAdapter.setOnDirClickListener(this);
        dirListAdapter.setSelectedDir(0);
        directoryRv.setAdapter(dirListAdapter);
        vuStores.switchMailDirectory(-1);//加载收件箱邮件
    }

    /**
     * 根据目录切换邮件列表
     *  position == -1的时候 说明切换到的不同邮箱的目录
     * @param position
     */
//    int dirIdCache = -1;

    @Override
    public void switchMailList(int position) {
        Log.i(TAG, "switchMailList: switchMailList position = "+position);
        if(dirListAdapter==null) {
            dirListAdapter = new DirectoryListAdapter(this, vuStores.getDirListData());
            dirListAdapter.setOnDirClickListener(this);
            dirListAdapter.setSelectedDir(0);
            directoryRv.setAdapter(dirListAdapter);
        }
        int preSelect = dirListAdapter.getSelectedDir();
        dirListAdapter.setSelectedDir(position);
//        重置之前被选中的目录样式
        dirListAdapter.notifyItemChanged(preSelect);
//        激活新选中的目录样式
        dirListAdapter.notifyItemChanged(position);

    }

    @Override
    public void showMailListFragment(String currDirName) {
        FragmentTransaction transaction = getFT();
        if (mailListFragment == null) {
            mailListFragment = new MailListFragment();
            transaction.add(R.id.wy_fragment_container, mailListFragment);
            transaction.commit();
        } else {
//                if (mailAttachmentFragment != null && mailAttachmentFragment.isVisible()) {
//                    transaction.show(mailListFragment);
//                    transaction.remove(mailAttachmentFragment);
//                    transaction.commit();
//                }
            mailListFragment.setDirName(currDirName);
//        获取邮件列表
            vuStores.loadListData();
        }
    }

//    public void openAttachmentListFragment() {
//        FragmentTransaction ft = getFT();
//        if (mailAttachmentFragment == null) {
//            mailAttachmentFragment = new MailAttachmentFragment();
//        }
//        ft.add(R.id.wy_fragment_container, mailAttachmentFragment);
//        ft.hide(mailListFragment);
//        ft.addToBackStack(null);
//        ft.commit();
//    }
    @Override
    public void openExchangeListFragment() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if(exchangeListFragment==null||!exchangeListFragment.isAdded()){
            FragmentTransaction ft = getFT();
            if (exchangeListFragment == null) {
                exchangeListFragment = new ExchangeListFragment();
            }
            ft.replace(R.id.wy_fragment_container, exchangeListFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    /**
     * 显示邮件详情的方法
     */
    @Override
    public void showMailDetail() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if(mailDetailFragment==null||!mailDetailFragment.isAdded()) {
            FragmentTransaction ft = getFT();
            if (mailDetailFragment == null) {
                mailDetailFragment = new MailDetailFragment();
            }
            ft.replace(R.id.wy_fragment_container, mailDetailFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void openVUSettingFragment() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if(vuSettingsFragment==null||!vuSettingsFragment.isAdded()) {
            FragmentTransaction ft = getFT();
            if (vuSettingsFragment == null) {
                vuSettingsFragment = new VUSettingsFragment();
            }
            ft.replace(R.id.wy_fragment_container, vuSettingsFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

//    /**
//     * 显示写反馈页面的方法
//     */
//    public void gotoWriteFeedback() {
//        if(vuFeedbackFragment==null||!vuFeedbackFragment.isAdded()) {
//            FragmentTransaction ft = getFT();
//            if (vuFeedbackFragment == null) {
//                vuFeedbackFragment = new VUFeedbackFragment();
//            }
//            ft.replace(R.id.wy_fragment_container, vuFeedbackFragment, "vuFeedbackFragment");
//            ft.addToBackStack(null);
//            ft.commit();
//            isWritingFeedback = true;
//        }
//    }

    public void gotoAddNewAccount() {
        FragmentTransaction ft = getFT();
        if (addNewAccountFragment == null) {
            addNewAccountFragment = new AddNewAccountFragment();
        }
        ft.replace(R.id.wy_fragment_container, addNewAccountFragment, "addNewAccountFragment");
        ft.addToBackStack(null);
        ft.commit();
    }

    public void openAccountCASetting() {
        FragmentTransaction ft = getFT();
        if (vuAccountCAFragment == null) {
            vuAccountCAFragment = new VUAccountCAFragment();
        }
        hideInputMethod();
        ft.replace(R.id.wy_fragment_container, vuAccountCAFragment, "vuAccountCAFragment");
        ft.addToBackStack(null);
        ft.commit();
    }
    @Override
    public void openMailAccountSettingFragment() {
        FragmentTransaction ft = getFT();
        if (vuAccountSettingsFragment == null) {
            vuAccountSettingsFragment = new VUAccountSettingsFragment();
        }
        ft.replace(R.id.wy_fragment_container, vuAccountSettingsFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void innerWriteMail(int type){
        Intent intent = new Intent();
        intent.putExtra("type",type);
        gotoWriteMail(intent);
    }

    public void gotoWriteMail(Intent intent) {
        intent.setClass(WeiYouMainActivity.this,WriteMailActivity.class);
        startActivity(intent);
    }

    /**
     * 重新编辑草稿 显示写邮件页面的方法
     */
    @Override
    public void reEditDraftMail() {
        Intent intent = new Intent();
        intent.putExtra("type",WeiYouMainActivity.QUOTE_TYPE_REEDIT);
        gotoWriteMail(intent);
    }

    public void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    public void hideInputMethod() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void toast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeiYouMainActivity.this,msg,Toast.LENGTH_SHORT).show();
            };
        });
    }

    private ProgressDialog progressDialog;

    /**
     * showLoading
     *
     * @param msg
     */
    @Override
    public void showProgressDialog(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null && msg != null) {
                    progressDialog = new ProgressDialog(WeiYouMainActivity.this);
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
    @Override
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

    @Override
    public void onNetStatusChanged(int netType) {
        vuStores.onNetStatusChanged(netType);
    }

    @Override
    public void switchDirectory(int position) {
        drawer.closeDrawer(GravityCompat.START);
//                if (position == directoryData.size() - 1) {
//                    openAttachmentListFragment();//打开 附件列表
//                } else {
        vuStores.switchMailDirectory(position);//切换目录
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPress");
//        if(writeMailFragment!=null){
//            Log.i(TAG, writeMailFragment.isVisible()+"");
//        }
        FragmentTransaction ft = getFT();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mailDetailFragment != null && mailDetailFragment.isVisible()) {
            if(mailDetailFragment.downloadConfirmDialog.isPopWindowShowing()){
                vuStores.cancelDownloadMail();
                mailDetailFragment.downloadConfirmDialog.hidePopWindow();
                return;
            }
            //从 来往邮件列表进的又见详情界面 返回的时候 返回来往邮件列表界面，不显示侧边栏
            if (exchangeListFragment==null) {
                //打开侧边栏手势滑动
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
            super.onBackPressed();
            mailDetailFragment = null;

        }else if (exchangeListFragment != null && exchangeListFragment.isVisible()) {
            super.onBackPressed();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            exchangeListFragment = null;
        } else if (mailAttachmentFragment != null && mailAttachmentFragment.isVisible()){
            super.onBackPressed();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mailAttachmentFragment = null;
        } else if (vuFeedbackFragment != null && vuFeedbackFragment.isResumed()) {
            if (isWritingFeedback) vuFeedbackFragment.onCancelSendFeedback();
            else {
                super.onBackPressed();
                vuFeedbackFragment = null;
            }
        } else if (vuAccountSettingsFragment != null && vuAccountSettingsFragment.isResumed()) {
            Log.i(TAG, "onBackPressed: vuAccountSettingsFragment");
            super.onBackPressed();
            vuAccountSettingsFragment = null;
        } else if (addNewAccountFragment != null && addNewAccountFragment.isResumed()) {
            super.onBackPressed();
            addNewAccountFragment = null;
        } else if (vuSettingsFragment != null && vuSettingsFragment.isVisible()) {
            Log.i(TAG, "onBackPressed: vuSettingsFragment");
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            super.onBackPressed();
            vuSettingsFragment = null;
        } else {
            if (!vuStores.currDirIsInbox()) {
                vuStores.switchMailDirectory(0);
            } else if(mailListFragment!= null && mailListFragment.mailSearchBox.isShown()){
                Log.i(TAG, "onBackPressed: hide searchMailBtn ······");
                mailListFragment.hideSearchBox();
            } else{
                Log.i(TAG, "onBackPressed: 关闭微邮~");
                super.onBackPressed();
            }
        }
    }
    @Override
    public boolean isNetWorkAvailable() {
        return NetWorkUtils.isNetWorkAvailable(this);
    }
    @Override
    public boolean isWifiConnected() {
        return NetWorkUtils.isWifiConnected(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    vuAccountCAFragment.choseFile();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isPaused = false;
    @Override
    public boolean isPaused() {
        return isPaused;
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isPaused = false;
        Log.i(TAG, "onResume: vuStores="+vuStores.getMailListData().size());
//        vuStores.register();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        Log.i(TAG, "onDestroy");
        vuStores.setVUActivityReference(null);
        vuStores.clean();
        vuStores = null;
        FileUtil.clearMailCache();// 清空邮件缓存
        unregisterReceiver(netStatusReceiver);
        super.onDestroy();
    }

}