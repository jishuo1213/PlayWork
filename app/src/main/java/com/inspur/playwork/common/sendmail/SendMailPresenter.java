package com.inspur.playwork.common.sendmail;

import com.inspur.playwork.model.message.SmallMailBean;

/**
 * Created by Bugcode on 2016/3/16.
 */
public interface SendMailPresenter {

    void register();

    void unregister();

    void sendMail(SmallMailBean smallMail, String bodyContent, String historyContent);

    String getHistoryContent(SmallMailBean smallMail);
}
