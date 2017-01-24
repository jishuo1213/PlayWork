package com.inspur.playwork.weiyou.store;

import android.content.res.Resources;

import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;

import java.util.List;

/**
 * Created by sunyuan on 2016/11/23 0023 11:18.
 * Email: sunyuan@inspur.com
 */
public interface MailDetailOperation {
    void addViewToViewList();

    void renderCurrentView(MailDetail md);

    Resources getResources();

    void renderAttachmentList(List<MailAttachment> currAttachmentList);

    void showDownloadComfirmDialog();

    void loadMailContentInWebView(String content, String contentType);
}
