package com.inspur.playwork.weiyou.store;

import com.inspur.playwork.utils.db.bean.MailAccount;

import java.util.ArrayList;

/**
 * Created by sunyuan on 2016/11/23 0023 11:18.
 * Email: sunyuan@inspur.com
 */
public interface VUActivityOperation {
    void refreshSpinner(ArrayList<MailAccount> mailAccountCache, int index);
    void switchSelectedAccount(String email);
    void toast(String msg);

    void renderMailDirectoryView();

    void showMailListFragment(String currDirName);

    void switchMailList(int position);

    void openExchangeListFragment();

    void showMailDetail();

    void reEditDraftMail();

    void showProgressDialog(String msgStr);

    boolean isPaused();

    void dismissProgressDialog();

    void openMailAccountSettingFragment();

    void initSettingButton();

    boolean isNetWorkAvailable();

    boolean isWifiConnected();
}
