package com.inspur.playwork.view.message.chat;

import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.CustomProperty;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.message.VChatBean;

import java.util.ArrayList;

/**
 * Created by fan on 16-9-9.
 */
public interface ChatViewOperation {
    void showMessageHistory(ArrayList<MessageBean> messageList, long time);

    void onSendMessageResult(boolean isSuccess, String uuid);

    void onReciveMessage(String groupId, MessageBean messageBean);

    void onGetNotes(String notes);

    void onHaveNoMoreMeesage();

    void setTaskProperty(boolean result, CustomProperty customProperty);

    void onSetNotesResult(boolean result, String notes);

    void onUpdateGroupName(boolean result);

    void onDeleteOneMessage(boolean result, String msgId);

    void onRecallOneMessage(boolean result, String msgId);

    void showMsgBeforeSendToServer(MessageBean messageBean);

    void onGetSmallMailDetail(String mark, SmallMailBean smallMailBean);

    void onCreateNewChat(ChatWindowInfoBean windowInfoBean, VChatBean vChatBean);

    void renameChatSubject(String subject);

}
