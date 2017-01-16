package com.inspur.playwork.view.application.addressbook;

import android.database.MatrixCursor;

import com.inspur.playwork.model.common.SearchPersonInfo;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.VChatBean;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-8.
 */

public interface AddressBookViewOperation {

    void showSuggestList(MatrixCursor cursor);

    void showSearchResult(ArrayList<SearchPersonInfo> list);

    void getChatWindowInfoSuccess(ChatWindowInfoBean windowInfoBean);

    void createChatSuccess(VChatBean vChatBean, ChatWindowInfoBean windowInfoBean);

    void searchUserInfoSuccess(ArrayList<UserInfoBean> userList);
}
