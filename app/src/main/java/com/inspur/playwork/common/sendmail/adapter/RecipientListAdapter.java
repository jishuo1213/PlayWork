package com.inspur.playwork.common.sendmail.adapter;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;

import java.util.ArrayList;

/**
 * Created by Bugcode on 2016/3/10.
 */
public class RecipientListAdapter extends RecyclerView.Adapter<RecipientListAdapter.ChatViewHolder> implements View.OnClickListener {

    private ArrayList<UserInfoBean> dataList;
    private ArrayMap<String, Long> avatarCache;
//    private BitmapCacheManager bitmapCacheManager;
    private UserInfoBean tempUserInfo;
    private OnRecipientListClickListener onRecipientListClickListener;

    public interface OnRecipientListClickListener {
        void onRecipientItemClick(View v, UserInfoBean userInfo);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipient_list, parent, false);
        ChatViewHolder chatViewHolder = new ChatViewHolder(view);
        return chatViewHolder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        tempUserInfo = dataList.get(position);
        holder.itemView.setTag(tempUserInfo);
        holder.itemView.setOnClickListener(this);
        holder.contactsName.setText(tempUserInfo.name);
        AdapterUtil.setContactsAvatar(holder.contactsAvatar, tempUserInfo, avatarCache, null);
    }

    @Override
    public int getItemCount() {
        return this.dataList.size();
    }

    @Override
    public void onClick(View v) {
        if (onRecipientListClickListener != null)
            onRecipientListClickListener.onRecipientItemClick(v, (UserInfoBean) v.getTag());
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public ImageView contactsAvatar;
        public TextView contactsName;

        public ChatViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.contactsAvatar = (ImageView) itemView.findViewById(R.id.iv_contact_avatar);
            this.contactsName = (TextView) itemView.findViewById(R.id.tv_contact_name);
        }
    }

    public void setDataList(ArrayList<UserInfoBean> dataList) {
        this.dataList = dataList;
    }

    public void setAvatarCache(ArrayMap<String, Long> avatarCache) {
        this.avatarCache = avatarCache;
    }
//
//    public void setBitmapCacheManager(BitmapCacheManager bitmapCacheManager) {
////        this.bitmapCacheManager = bitmapCacheManager;
//    }

    public void setOnRecipientListClickListener(OnRecipientListClickListener onRecipientListClickListener) {
        this.onRecipientListClickListener = onRecipientListClickListener;
    }
}
