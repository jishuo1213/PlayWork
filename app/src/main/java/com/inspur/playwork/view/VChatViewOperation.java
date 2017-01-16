package com.inspur.playwork.view;

import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.VChatBean;

import java.util.ArrayList;

/**
 * Created by fan on 16-5-6.
 */
public interface VChatViewOperation {

    void showNoralChatList(ArrayList<VChatBean> vchatList);

    void getNormalChatWindowResult(ChatWindowInfoBean windowInfoBean,String uuid);

    void refreshVchatList();

    void reciveOneNormalMsg(VChatBean vChatBean);

    void reciveOneRecallMsg(MessageBean messageBean);

    void reciveOneDeleteMsg(String groupId, String msgId);

    void deleteOneVchat(boolean result, String groupId);

    void setOnReadMsgToRead(String groupId);
}
