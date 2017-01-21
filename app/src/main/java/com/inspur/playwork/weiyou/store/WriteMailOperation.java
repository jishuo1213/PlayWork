package com.inspur.playwork.weiyou.store;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunyuan on 2016/11/23 0023 11:18.
 * Email: sunyuan@inspur.com
 */
public interface WriteMailOperation {

    void generateContactTV(UserInfoBean userInfoBean,int type);

    void refreshSearchResultListView(ArrayList<UserInfoBean> contactSearchResult);

    void emptyInputText();

    String getSubjectText();

    String getDraftHtmlContent();

    void fillDraftData(String draftOrignalSubject, ArrayList<UserInfoBean> toList, ArrayList<UserInfoBean> ccList, String draftOrignalContent, boolean encrypted, boolean signed,boolean hasCurrUsingCa);

    void showSaveDraftPopWindow();

    void prepareToSendMail(MailDetail _md);

    void refreshAttachmentListView();

    void showNoPbkWarningDialog(String msg,int showContinueSendButton);

    void emptyContactTV();

    void closeWriteMailFragment();

    void showRemindSubjectDialog();

    void toast(String msg);

    MailDetail getParamMailDetail();

    List<MailAttachment> getParamAttachments();

}
