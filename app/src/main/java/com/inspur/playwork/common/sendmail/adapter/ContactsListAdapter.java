package com.inspur.playwork.common.sendmail.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;

import java.util.ArrayList;

/**
 * Created by Bugcode on 2016/3/7.
 */
public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ContactsViewHolder> implements View.OnClickListener {

    private int contactsType;
    private ArrayList contactsList;
    private ArrayMap<String, Long> avatarCache;
    private Context context;
    //    private BitmapCacheManager bitmapCacheManager;
    private OnContactsListClickListener onContactsListClickListener;

    public interface OnContactsListClickListener {
        void onContactsItemClick(View v, UserInfoBean userInfo);

        void onContactsItemClick(View v, GroupInfoBean groupInfo);
    }

    public ContactsListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        // contactsType=2 群
        if (contactsType == 2) {
            GroupInfoBean tempGroupInfo = (GroupInfoBean) contactsList.get(position);
            this.setContactsName(holder, tempGroupInfo);
            holder.itemView.setTag(tempGroupInfo);

        } else {
            UserInfoBean tempUserInfo = (UserInfoBean) contactsList.get(position);
            holder.contactsName.setText(tempUserInfo.name);
//            this.setContactsAvatar(holder, tempUserInfo);
//            holder.contactsId.setText(tempUserInfo.id);
            AdapterUtil.setContactsAvatar(holder.contactsAvatar, tempUserInfo, avatarCache, null);
            holder.itemView.setTag(tempUserInfo);
        }
        holder.itemView.setOnClickListener(this);
    }

    private void setContactsName(ContactsViewHolder holder, GroupInfoBean groupInfo) {
//        holder.contactsId.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(groupInfo.getTaskId())) {
            holder.contactsAvatar.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_member_group_task));
            holder.contactsName.setText(groupInfo.getSubject());
        } else if (!TextUtils.isEmpty(groupInfo.getSubject())) {
            holder.contactsAvatar.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_member_group_topic));
            holder.contactsName.setText(groupInfo.getSubject());
        } else {
            holder.contactsAvatar.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_member_group_no_topic));
            holder.contactsName.setText(groupInfo.getAllMemberName());
        }
    }

//    private void setContactsAvatar(ContactsViewHolder holder, UserInfoBean userInfo) {
//        if (avatarCache.containsKey(userInfo.id)) {
//            Long avatarId = avatarCache.get(userInfo.id);
//            if (avatarId >= userInfo.avatar) {
//                userInfo.avatar = avatarId;
//            } else {
//                avatarCache.put(userInfo.id, userInfo.avatar);
//            }
//        } else {
//            avatarCache.put(userInfo.id, userInfo.avatar);
//        }
//
//        String avatarPath = userInfo.getAvatarPath();
////        Bitmap avatarBitmap = bitmapCacheManager.getBitmapFromMemoryCache(avatarPath);
////        if (avatarBitmap != null) {
////            holder.contactsAvatar.setImageBitmap(avatarBitmap);
////        } else {
//        if (userInfo.isAvatarFileExit()) {
//            PictureUtils.loadAvatarBitmap(avatarPath, holder.contactsAvatar, null);
//        } else {
//            PictureUtils.downLoadAvatar(avatarPath, AppConfig.AVATAR_ROOT_PATH + userInfo.avatar, holder.contactsAvatar, null);
//        }
////        }
//    }

    @Override
    public int getItemCount() {
        return this.contactsList.size();
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        ImageView contactsAvatar;
        TextView contactsName;
//        TextView contactsId;

        ContactsViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.contactsAvatar = (ImageView) itemView.findViewById(R.id.iv_contact_avatar);
            this.contactsName = (TextView) itemView.findViewById(R.id.tv_contact_name);
//            contactsId = (TextView) itemView.findViewById(R.id.tv_contact_id);
        }
    }

    @Override
    public void onClick(View v) {
        if (onContactsListClickListener == null)
            return;
        if (contactsType == 2) {
            onContactsListClickListener.onContactsItemClick(v, (GroupInfoBean) v.getTag());
        } else {
            onContactsListClickListener.onContactsItemClick(v, (UserInfoBean) v.getTag());
        }
    }

    public void setContactsType(int contactsType) {
        this.contactsType = contactsType;
    }

    public void setContactsList(ArrayList contactsList) {
        this.contactsList = contactsList;
    }

    public void setAvatarCache(ArrayMap<String, Long> avatarCache) {
        this.avatarCache = avatarCache;
    }

//    public void setBitmapCacheManager(BitmapCacheManager bitmapCacheManager) {
//        this.bitmapCacheManager = bitmapCacheManager;
//    }

    public void setOnContactsListClickListener(OnContactsListClickListener onContactsListClickListener) {
        this.onContactsListClickListener = onContactsListClickListener;
    }
}
