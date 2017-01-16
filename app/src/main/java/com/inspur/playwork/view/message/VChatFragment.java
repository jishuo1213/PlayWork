package com.inspur.playwork.view.message;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.UnReadMsgChangeListener;
import com.inspur.playwork.view.VChatViewOperation;
import com.inspur.playwork.view.message.chat.ChatActivityNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * 消息列表fragment
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class VChatFragment extends Fragment implements VChatRecylerAdapter.ItemClickListener, View.OnClickListener, VChatViewOperation {

    private static final String TAG = "VChatFragmentFan";
//    public static final int UPDATE_VCHAT_LIST = 100;

    private MessageStores messageStores;

    private RecyclerView vChatRecyclerView;

    private ArrayList<VChatBean> chatList;

    private UnReadMsgChangeListener listener;

//    private boolean needShowUnRead;

    private VChatBean clickBean;

    private Dialog alertDialog;

//    private Handler handler;

    private ArrayList<String> getInfoList;

    private String needUUid;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "onAttach: " + "vchat on attach");
        listener = (UnReadMsgChangeListener) activity;
//        Dispatcher.getInstance().register(this);
        messageStores = MessageStores.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ===========");
        if (chatList != null)
            refreshVchatList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        handler = new Handler();
        getInfoList = new ArrayList<>();
        messageStores.setVchatViewReference(this);
        messageStores.getLocalChatList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        Log.i(TAG, "onDetach: ");
        Dispatcher dispatcher = Dispatcher.getInstance();
        if (dispatcher != null) {
            dispatcher.unRegister(this);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        MessageStores.getInstance().setVchatViewReference(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View view) {
        vChatRecyclerView = (RecyclerView) view.findViewById(R.id.normal_chat_list);
        vChatRecyclerView.setHasFixedSize(true);
        vChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        if (chatList != null && vChatRecyclerView.getAdapter() == null) {
            VChatRecylerAdapter adapter = new VChatRecylerAdapter(getActivity(), vChatRecyclerView);
            adapter.setvChatList(chatList);
            adapter.setListener(this);
            Log.i(TAG, "initViews: " + chatList.size());
            vChatRecyclerView.setAdapter(adapter);
        }
    }


    private void deleteChatByGruopId(String groupId) {
        MessageStores.getInstance().removeUnReadMsg(groupId);
        MessageStores.getInstance().removeChatId(groupId);
        ((VChatRecylerAdapter) vChatRecyclerView.getAdapter()).deleteChat(groupId);
        listener.onMsgCountChange(1, 1);
    }

    private void showPhotoDialog() {
        if (alertDialog == null) {
            alertDialog = new Dialog(getActivity(), R.style.normal_dialog);
            alertDialog.setContentView(R.layout.chat_menu_dialog);
            alertDialog.show();
//            Window window = alertDialog.getWindow();
//            window.setContentView(R.layout.chat_menu_dialog);
            TextView deleteView = (TextView) alertDialog.findViewById(R.id.tv_content1);
            deleteView.setOnClickListener(this);
            return;
        }
        alertDialog.show();
    }

    @Override
    public void onItemClick(VChatBean mBean, int position) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.GET_NORMAL_CHAT_WINDO_INFO, mBean, position);

//        handler.removeCallbacks(getChatWindowInfoRunnable);
//        handler.postDelayed(getChatWindowInfoRunnable, 500);
//        MessageStores.getInstance().getNormalChatWindowInfo(mBean);
        Log.i(TAG, "onItemClick: " + getInfoList.size());
        if (getInfoList.contains(mBean.groupId) || getInfoList.size() >= 2) {
            return;
        }
        getInfoList.add(mBean.groupId);
        String uuid = UUID.randomUUID().toString();
        needUUid = uuid;
        clickBean = mBean;
        MessageStores.getInstance().getNormalChatWindowInfo(clickBean, uuid);
//        listener.onMsgCountChange(1, 1);
    }

    @Override
    public void onItemLongClick(VChatBean mBean, int position) {
        clickBean = mBean;
        showPhotoDialog();
    }

//    private void updateVChatList(VChatBean vChatBean) {
//        int pos = ((VChatRecylerAdapter) vChatRecyclerView.getAdapter()).getUnReadPos(vChatBean.groupId);
//        if (pos > -1) {
//            VChatBean tempBean = chatList.get(pos);
///*            if ((!TextUtils.isEmpty(tempBean.msgId) && tempBean.msgId.equals(vChatBean.msgId)) && tempBean.topic.equals(vChatBean.topic)) {
//                return;
//            }*/
//            tempBean.msgId = vChatBean.msgId;
//            tempBean.lastMsg = vChatBean.lastMsg;
//            tempBean.lastChatTime = vChatBean.lastChatTime;
//            tempBean.topic = vChatBean.topic;
//            tempBean.isNeedCreateAvatar = vChatBean.isNeedCreateAvatar;
//            Log.i(TAG, "updateVChatList: " + vChatBean.isNeedCreateAvatar);
//            tempBean.avatarList = vChatBean.avatarList;
//            tempBean.memberList = vChatBean.memberList;
//            tempBean.unReadMsgNum = 0;
//            Collections.sort(chatList);
////            vChatRecyclerView.getAdapter().notifyItemRangeChanged(0, chatList.indexOf(tempBean) + 1);
//            vChatRecyclerView.getAdapter().notifyDataSetChanged();
//        } else {
//            chatList.add(0, vChatBean);
//            messageStores.getChatGroupIds().add(vChatBean.groupId);
//            vChatRecyclerView.getAdapter().notifyItemInserted(0);
//            vChatRecyclerView.scrollToPosition(0);
//        }
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_content1) {
            alertDialog.dismiss();
//            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.DELETE_VCHAT_ONE_CHAT, clickBean.groupId);
            MessageStores.getInstance().deleteOneChat(clickBean.groupId);
        }
    }

    @Override
    public void showNoralChatList(ArrayList<VChatBean> vchatList) {
        chatList = vchatList;
        Log.i(TAG, "showNoralChatList: " + vchatList.size());
        if (getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(chatList);
                Log.i(TAG, "run: " + (vChatRecyclerView == null));
                if (vChatRecyclerView != null) {
                    if (vChatRecyclerView.getAdapter() == null) {
                        VChatRecylerAdapter adapter = new VChatRecylerAdapter(getActivity(), vChatRecyclerView);
                        adapter.setvChatList(chatList);
                        adapter.setListener(VChatFragment.this);
                        vChatRecyclerView.setAdapter(adapter);
//                        if (needShowUnRead) {
//                            Log.i(TAG, "onEventMainThread: " + "showUnReadMsg");
//                            adapter.showUnReadMsg();
//                        }
                    } else {
                        VChatRecylerAdapter adapter = (VChatRecylerAdapter) vChatRecyclerView.getAdapter();
                        adapter.setvChatList(chatList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void getNormalChatWindowResult(final ChatWindowInfoBean windowInfoBean, final String uuid) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_CHAT_MESSAGE_LIST, MessageStores.getInstance().getUnReadMap().get(windowInfoBean.groupId), windowInfoBean.groupId);
                Log.i(TAG, "getNormalChatWindowResult: --------------" + (clickBean == null));
                Log.i(TAG, "getNormalChatWindowResult: --------------" + needUUid + "====" + uuid);

                if (windowInfoBean == null) {
                    getInfoList.remove(uuid);
                    UItoolKit.showToastShort(getActivity(), "获取聊天信息失败");
                    return;
                }
                getInfoList.remove(windowInfoBean.groupId);
                if (!needUUid.equals(uuid)) {
                    Log.i(TAG, "run:!needUUid.equals(uuid) return");
                    return;
                }
                Intent intent = new Intent(getActivity(), ChatActivityNew.class);
                intent.putExtra(ChatActivityNew.CHAT_WINDOW_INFO, windowInfoBean);
                intent.putExtra(ChatActivityNew.VCHAT_BEAN, clickBean);
                clickBean = null;
                startActivity(intent);
            }
        });
    }

    @Override
    public void refreshVchatList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageStores.getInstance().sortVchatList();
                VChatRecylerAdapter adapter = (VChatRecylerAdapter) vChatRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void reciveOneNormalMsg(final VChatBean vChatBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VChatRecylerAdapter VChatRecylerAdapter = (VChatRecylerAdapter) vChatRecyclerView.getAdapter();
                VChatRecylerAdapter.setUnReadMsgCount(vChatBean);
                listener.onMsgCountChange(1, 1);
            }
        });

    }

    @Override
    public void reciveOneRecallMsg(final MessageBean messageBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VChatRecylerAdapter adapter = (VChatRecylerAdapter) vChatRecyclerView.getAdapter();
                adapter.setLastMsg(messageBean);
                listener.onMsgCountChange(1, 1);
            }
        });
    }

    @Override
    public void reciveOneDeleteMsg(final String groupId, final String msgId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VChatRecylerAdapter adapter = (VChatRecylerAdapter) vChatRecyclerView.getAdapter();
                adapter.setLastMsg(groupId, msgId);
                listener.onMsgCountChange(1, 1);
            }
        });
    }

    @Override
    public void deleteOneVchat(final boolean result, final String groupId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result) {
                    deleteChatByGruopId(groupId);
                } else {
                    UItoolKit.showToastShort(getActivity(), "删除聊天失败");
                }
            }
        });
    }

    @Override
    public void setOnReadMsgToRead(final String groupId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((VChatRecylerAdapter) vChatRecyclerView.getAdapter()).setMsgToRead(groupId);
                listener.onMsgCountChange(1, 1);
            }
        });
    }
}
