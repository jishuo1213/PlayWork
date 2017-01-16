package com.inspur.playwork.view.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.MyBitmapEntity;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.EmojiHandler;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.view.common.BadgeView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fan on 15-9-29.
 */
class VChatRecylerAdapter extends RecyclerView.Adapter<VChatRecylerAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "VChatRecylerAdapterFan";


    interface ItemClickListener {
        void onItemClick(VChatBean mBean, int position);

        void onItemLongClick(VChatBean mBean, int position);
    }

    private ArrayList<VChatBean> vChatList;

    private RecyclerView normalChatRecyclerView;

    private ItemClickListener listener;

    private Context mContext;

    private LruCache<String, Bitmap> mImageBitmapCache;


    VChatRecylerAdapter(Context context, RecyclerView recyclerView) {
        this.mContext = context;
        this.normalChatRecyclerView = recyclerView;
        this.mImageBitmapCache = ((PlayWorkApplication) ((MainActivity) mContext).getApplication()).getImageBitmapCache();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_single, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VChatBean chatBean = vChatList.get(position);

        holder.topic.setText(chatBean.topic);
        holder.lastChatTime.setText(DateUtils.time2MMDD(chatBean.lastChatTime));
        if (!TextUtils.isEmpty(chatBean.lastMsg)) {
            if (chatBean.lastMsg.contains("weiliao_images") && chatBean.lastMsg.contains("<img")) {
                holder.lastMsg.setText("[图片]");
            } else if (chatBean.lastMsg.contains("attachmentDownload") && chatBean.lastMsg.startsWith("<div>")) {
                holder.lastMsg.setText("[文件]");
            } else {
                holder.lastMsg.setText(EmojiHandler.getInstance().replaceEmoji(chatBean.lastMsg));
            }
        } else {
            holder.lastMsg.setText("无消息内容...");
        }
        holder.rootView.setOnClickListener(this);
        holder.rootView.setTag(chatBean.groupId);
        Log.i(TAG, "onBindViewHolder: " + chatBean.groupId + "===" + PreferencesHelper.getInstance().getServiceNumGroupId());
        if (!chatBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
            holder.rootView.setOnLongClickListener(this);
            Bitmap bitmap;
            if (mImageBitmapCache.get(chatBean.groupId) != null && !chatBean.isNeedCreateAvatar) {
                bitmap = mImageBitmapCache.get(chatBean.groupId);
            } else {
                bitmap = avatarsBitmap(chatBean.avatarList);
                chatBean.isNeedCreateAvatar = false;
            }
        /*
        单聊去头像背景
         */
            if (chatBean.avatarList.size() == 1) {
                holder.chatAvatar.setPadding(0, 0, 0, 0);
            } else {
                int padding = DeviceUtil.dpTopx(mContext, 1.5f);
                holder.chatAvatar.setPadding(padding, padding, padding, padding);
                //noinspection deprecation
                holder.chatAvatar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.chat_list_avatar_bg));
            }
            if (bitmap != null) {
                holder.chatAvatar.setImageBitmap(bitmap);
                mImageBitmapCache.put(chatBean.groupId, bitmap);
            } else {
                holder.chatAvatar.setImageBitmap(PictureUtils.getDefaultAvatar(mContext));
            }
        } else {
            holder.rootView.setOnLongClickListener(null);
            holder.chatAvatar.setPadding(0, 0, 0, 0);
            holder.chatAvatar.setImageResource(R.mipmap.ic_launcher);
        }

        if (chatBean.unReadMsgNum > 0) {
            if (holder.unReadCount == null) {
                holder.unReadCount = new BadgeView(normalChatRecyclerView.getContext());
                holder.unReadCount.setTargetView(holder.lastMsg);
                holder.unReadCount.setBackgroundResource(R.drawable.bageview_back);
            }
            holder.unReadCount.setText(chatBean.unReadMsgNum + "");
            holder.unReadCount.setVisibility(View.VISIBLE);
        } else {
            if (holder.unReadCount != null)
                holder.unReadCount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
//        Log.i(TAG, "getItemCount: " + vChatList.size());
        return vChatList.size();
    }

    void setvChatList(ArrayList<VChatBean> vChatList) {
        this.vChatList = vChatList;
        Log.i(TAG, "setvChatList: " + vChatList.size());
    }

    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return;
        }
        lastClickTime = time;
        int pos = normalChatRecyclerView.getChildAdapterPosition(v);
        VChatBean mBean = getItem(pos);
//        MessageStores.getInstance().removeUnReadMsg(mBean);
        listener.onItemClick(mBean, pos);
//        mBean.unReadMsgNum = 0;
        ViewHolder viewHolder = (ViewHolder) normalChatRecyclerView.findViewHolderForAdapterPosition(pos);
        if (viewHolder.unReadCount != null) {
            viewHolder.unReadCount.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = normalChatRecyclerView.getChildAdapterPosition(v);
        VChatBean mBean = getItem(pos);
        listener.onItemLongClick(mBean, pos);
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).groupId.hashCode();
    }

    private VChatBean getItem(int position) {
        return vChatList.get(position);
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView topic;
        TextView lastChatTime;
        TextView lastMsg;
        ImageView chatAvatar;
        public View rootView;
        BadgeView unReadCount;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            topic = (TextView) itemView.findViewById(R.id.tv_topic);
            lastChatTime = (TextView) itemView.findViewById(R.id.tv_last_chat_time);
            lastMsg = (TextView) itemView.findViewById(R.id.tv_last_send_msg);
            chatAvatar = (ImageView) itemView.findViewById(R.id.iv_vchat_avatar);
        }
    }

//    private void showUnReadMsg() {
//        ArrayList<UnReadMessageBean> unReadList = MessageStores.getInstance().getvChatUnReadMsg();
//        ArrayList<String> groupIds = MessageStores.getInstance().getChatGroupIds();
//
//
//        if (unReadList == null) {
//            return;
//        }
//        Log.i(TAG, "showUnReadMsg: " + unReadList.size());
//
//        for (VChatBean bean : vChatList)
//            bean.unReadMsgNum = 0;
//
//        for (UnReadMessageBean messageBean : unReadList) {
//            if (groupIds.contains(messageBean.groupId)) {
//                ViewHolder viewHolder = (ViewHolder) normalChatRecyclerView.findViewHolderForItemId(messageBean.groupId.hashCode());
//                int pos;
//
//                if (viewHolder == null) {
//                    pos = getUnReadPos(messageBean.groupId);
//                } else {
//                    pos = viewHolder.getLayoutPosition();
//                }
//
//                if (pos < 0)
//                    continue;
//
//
//                VChatBean taskBean = getItem(pos);
//
//                assert taskBean != null;
//                taskBean.unReadMsgNum++;
//
//                if (taskBean.lastChatTime < messageBean.msgSendTime) {
//                    taskBean.lastChatTime = messageBean.msgSendTime;
//                    taskBean.lastMsg = messageBean.content;
//                }
//            } else {
//                if (gettingWindowInfoLists.contains(messageBean.groupId))
//                    return;
////                Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.GET_NORMAL_CHAT_WINDO_INFO, null, messageBean.groupId);
//                MessageStores.getInstance().getVchatWindowInfo(messageBean.groupId);
//                gettingWindowInfoLists.add(messageBean.groupId);
//            }
//        }
//        updateLastMsg();
//        Collections.sort(vChatList);
//        notifyDataSetChanged();
//    }


//    private void updateLastMsg() {
//        ArrayList<UnReadMessageBean> unReadList = MessageStores.getInstance().getNetReadList();
//        ArrayList<String> groupIds = MessageStores.getInstance().getChatGroupIds();
//
//
//        if (unReadList == null) {
//            return;
//        }
//        Log.i(TAG, "updateLastMsg: " + unReadList.size());
//
//        for (UnReadMessageBean bean : unReadList) {
//            Log.i(TAG, "showUnReadMsg: " + bean.toString());
//        }
//
//        for (UnReadMessageBean messageBean : unReadList) {
//
//            if (groupIds.contains(messageBean.groupId)) {
//                Log.i(TAG, "updateLastMsg: " + messageBean.groupId);
//                RecyclerView.ViewHolder viewHolder = normalChatRecyclerView.findViewHolderForItemId(messageBean.groupId.hashCode());
//                int pos;
//
//                if (viewHolder == null) {
//                    pos = getUnReadPos(messageBean.groupId);
//                } else {
//                    pos = normalChatRecyclerView.getChildAdapterPosition(viewHolder.itemView);
//                }
//
//                if (pos == -1) {
//                    return;
//                }
//
//                VChatBean vChatBean = getItem(pos);
//
//
//                if (vChatBean.lastChatTime < messageBean.msgSendTime) {
//                    vChatBean.lastChatTime = messageBean.msgSendTime;
//                    vChatBean.lastMsg = messageBean.content;
//                }
//            } else {
//                if (gettingWindowInfoLists.contains(messageBean.groupId))
//                    return;
//                MessageStores.getInstance().getVchatWindowInfo(messageBean.groupId);
//                gettingWindowInfoLists.add(messageBean.groupId);
//            }
//        }
//    }


    private int getUnReadPos(String groupId) {
        int i = 0;
        for (VChatBean chatBean : vChatList) {
            if (chatBean.groupId.equals(groupId)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    void setUnReadMsgCount(VChatBean vChatBean) {
        ViewHolder viewHolder = (ViewHolder) normalChatRecyclerView.findViewHolderForItemId(vChatBean.groupId.hashCode());
        int pos;
        if (viewHolder == null) {
            pos = getUnReadPos(vChatBean.groupId);
        } else {
            pos = normalChatRecyclerView.getChildAdapterPosition(viewHolder.rootView);
        }

        if (pos == -1) {
            return;
        }
//        vChatList.remove(pos);

//        VChatBean current =
//        if (newMessage.isNeedToNotify())
//            current.unReadMsgNum++;
//        if (newMessage.isImageMsg()) {
//            current.lastMsg = "[图片]";
//        } else {
//            current.lastMsg = newMessage.content;
//        }
//        current.msgId = newMessage.id;
//        current.lastChatTime = newMessage.sendTime;
        //Collections.
//        int notifyPos = 0;
        if (vChatBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
//            vChatList.add(0, vChatBean);
//            notifyPos = 0;
            notifyItemChanged(0);
        } else {
            if (pos == 1) {
                notifyItemChanged(1);
            } else {
                Collections.sort(vChatList);
                notifyDataSetChanged();
            }
//            notifyPos = 1;
//            vChatList.add(1, vChatBean);
        }

//        notifyItemRangeChanged(notifyPos, pos + 1);
    }

//    void addOneVchat(VChatBean vChatBean) {
//        vChatList.add(0, vChatBean);
//        Collections.sort(vChatList);
////        notifyDataSetChanged();
//        notifyItemInserted(0);
////        showUnReadMsg();
//    }

    /**
     * 生成九宫格头像bitmap
     *
     * @param avatarList
     * @return
     */
    private Bitmap avatarsBitmap(ArrayList<String> avatarList) {
        ArrayList<String> newAvatarList = new ArrayList<>();
        Log.i(TAG, "avatarsBitmap: " + avatarList.size());
        for (int i = 0; i < avatarList.size(); i++) {
            /*
            判断头像文件是否存在
            最多生成9张头像的合成
             */
//            if (avatarExisted(avatarList.get(i)) && (newAvatarList.size() < 9)) {
//                newAvatarList.add(FileUtil.getAvatarFilePath() + avatarList.get(i));
//            }
            if (newAvatarList.size() < 9) {
                if (avatarExisted(avatarList.get(i))) {
                    newAvatarList.add(FileUtil.getAvatarFilePath() + avatarList.get(i));
                } else {
                    newAvatarList.add(null);
                }
            }
        }

        int count = newAvatarList.size();
        Bitmap combineBitmap = null;
        LruCache<String, Bitmap> imageBitmapCache = ((PlayWorkApplication) ((MainActivity) mContext).getApplication()).getImageBitmapCache();
        if (count > 0) {
            List<MyBitmapEntity> bitmapEntityList = PictureUtils.getBitmapEntitys(mContext, count);
            Bitmap[] bitmaps = new Bitmap[count];
            Bitmap bitmap;
            for (int j = 0; j < count; j++) {
//                bitmap = PictureUtils.getAvatar(newAvatarList.get(j)); // 生成头像的bitmap
                /* 生成头像的bitmap */
                if ((newAvatarList.get(j) != null) && (imageBitmapCache.get(newAvatarList.get(j)) != null)) {
                    bitmap = imageBitmapCache.get(newAvatarList.get(j));
                } else if ((newAvatarList.get(j) != null) && (imageBitmapCache.get(newAvatarList.get(j)) == null)) {
                    bitmap = PictureUtils.getAvatar(normalChatRecyclerView.getContext(), newAvatarList.get(j));
                    if (bitmap != null) {
                        imageBitmapCache.put(newAvatarList.get(j), bitmap);
                    } else {
                        bitmap = PictureUtils.getDefaultAvatar(mContext);
                    }
                } else {
                    bitmap = PictureUtils.getDefaultAvatar(mContext);
                }
                bitmaps[j] = ThumbnailUtils.extractThumbnail(bitmap,
                        (int) bitmapEntityList.get(j / 3).width,
                        (int) bitmapEntityList.get(j / 3).width);
            }
            combineBitmap = PictureUtils.getCombineBitmaps(bitmapEntityList, bitmaps);
        }

        return combineBitmap;
    }

    /**
     * 判断头像文件是否存在
     *
     * @param avatar
     * @return
     */
    private boolean avatarExisted(String avatar) {
        String fileName = FileUtil.getAvatarFilePath() + avatar;
        File file = new File(fileName);
        return file.exists() && file.length() > 0;
    }

    void deleteChat(String groupId) {
        int pos = getUnReadPos(groupId);
        if (pos > -1) {
            vChatList.remove(pos);
            RecyclerView.ViewHolder viewHolder = normalChatRecyclerView.findViewHolderForLayoutPosition(pos);
            if (viewHolder != null)
                notifyItemRemoved(pos);
        }
    }


    void setMsgToRead(String groupId) {
        int pos = getUnReadPos(groupId);
        VChatBean vChatBean = getItem(pos);
        MessageStores.getInstance().removeUnReadMsg(vChatBean);
        vChatBean.unReadMsgNum = 0;
        ViewHolder viewHolder = (ViewHolder) normalChatRecyclerView.findViewHolderForAdapterPosition(pos);
        if (viewHolder != null && viewHolder.unReadCount != null) {
            viewHolder.unReadCount.setVisibility(View.GONE);
        }
    }


    void setLastMsg(MessageBean messageBean) {
        int pos = getUnReadPos(messageBean.groupId);
        if (pos > -1) {
            VChatBean vChatBean = getItem(pos);
            if (vChatBean.msgId.equals(messageBean.id)) {
                if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id))
                    vChatBean.lastMsg = "你撤回了一条消息";
                else
                    vChatBean.lastMsg = messageBean.sendMessageUser.name + "撤回了一条消息";
            }
            vChatBean.unReadMsgNum--;
            notifyItemChanged(pos);
        }
    }

    void setLastMsg(String groupId, String msgId) {
        int pos = getUnReadPos(groupId);
        if (pos > -1) {
            VChatBean vChatBean = getItem(pos);
            if (vChatBean.msgId.equals(msgId)) {
                vChatBean.lastMsg = "你删除了一条消息";
            }
            vChatBean.unReadMsgNum--;
            notifyItemChanged(pos);
        }
    }
}
