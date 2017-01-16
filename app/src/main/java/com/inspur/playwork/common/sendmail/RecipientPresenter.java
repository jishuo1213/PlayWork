package com.inspur.playwork.common.sendmail;

import com.inspur.playwork.model.common.UserInfoBean;

/**
 * Created by Bugcode on 2016/3/24.
 */
public interface RecipientPresenter {

    void register();

    void unRegister();

    void initContactsList();

    void searchUserByKeyWord(String keyWord);

    void initRecipientList();

    void delRecipient(UserInfoBean userInfo);

    void updateRecipient();

    void addRecipient(UserInfoBean userInfo);
}
