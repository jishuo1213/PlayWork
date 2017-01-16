package com.inspur.playwork.view.message.chat;

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
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.utils.PictureUtils;

import java.util.ArrayList;

/**
 * 联系人列表RecyclerAdapter
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
class ContactListRecyclerAdapter extends RecyclerView.Adapter<ContactListRecyclerAdapter.ViewHolder>
        implements View.OnClickListener {

    private LayoutInflater mLayoutInflater;
    private int mType = 0;
    private ArrayList mDataList;
    //    private BitmapCacheManager mArrayMap;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private AddMemberListener mAddMemberListener;

    private ArrayMap<String, Long> avatars;

    interface AddMemberListener {
        /**
         * 增加单个人员接口回调方法
         *
         * @param userInfoBean 添加的人员信息
         */
        void addMember(UserInfoBean userInfoBean);

        /**
         * 增加一组人员接口回调方法
         *
         * @param groupInfoBean 添加的群组信息
         */
        void addMember(GroupInfoBean groupInfoBean);
    }

    ContactListRecyclerAdapter(Context context, RecyclerView recyclerView) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mRecyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mType == 2) { // 联系组
            GroupInfoBean groupInfoBean = (GroupInfoBean) mDataList.get(position);
            // 加工“群”显示样式 任务/话题/非话题
//            holder.contactId.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(groupInfoBean.getTaskId())) {
                holder.contactAvatarImageView.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.icon_member_group_task));
                holder.contactNameTextView.setText(groupInfoBean.getSubject());
            } else if (!TextUtils.isEmpty(groupInfoBean.getSubject())) {
                holder.contactAvatarImageView.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.icon_member_group_topic));
                holder.contactNameTextView.setText(groupInfoBean.getSubject());
            } else {
                holder.contactAvatarImageView.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.icon_member_group_no_topic));
                holder.contactNameTextView.setText(groupInfoBean.getAllMemberName());
            }
        } else { // 联系人/部门成员
            UserInfoBean userInfoBean = (UserInfoBean) mDataList.get(position);
            holder.contactNameTextView.setText(userInfoBean.name);
//            holder.contactId.setText(userInfoBean.id);
            if (avatars.containsKey(userInfoBean.id)) {
                long avatarId = avatars.get(userInfoBean.id);
                if (avatarId >= userInfoBean.avatar) {
                    userInfoBean.avatar = avatarId;
                } else {
                    avatars.put(userInfoBean.id, userInfoBean.avatar);
                }
            } else {
                avatars.put(userInfoBean.id, userInfoBean.avatar);
            }

            String avatarFilePath = userInfoBean.getAvatarPath();
//            Bitmap avatar = mArrayMap.getBitmapFromMemoryCache(avatarFilePath);
//            if (avatar != null) {
//                holder.contactAvatarImageView.setImageBitmap(avatar);
//            } else {
            if (userInfoBean.isAvatarFileExit()) {
                PictureUtils.loadAvatarBitmap(avatarFilePath, holder.contactAvatarImageView, null);
            } else {
                PictureUtils.downLoadAvatar(avatarFilePath, AppConfig.AVATAR_ROOT_PATH + userInfoBean.avatar, holder.contactAvatarImageView, null);
            }
//            }
        }
        holder.contactItemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onClick(View v) {
        int position = this.mRecyclerView.getChildAdapterPosition(v);
        if (mAddMemberListener != null) {
            if (mType == 2) {
                mAddMemberListener.addMember((GroupInfoBean) mDataList.get(position));
            } else {
                mAddMemberListener.addMember((UserInfoBean) mDataList.get(position));
            }
        }
    }

    public void setAvatars(ArrayMap<String, Long> avatars) {
        this.avatars = avatars;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View contactItemView;
        ImageView contactAvatarImageView;
        TextView contactNameTextView;
//        TextView contactId;

        public ViewHolder(View itemView) {
            super(itemView);
            contactItemView = itemView;
            contactAvatarImageView = (ImageView) itemView.findViewById(R.id.iv_contact_avatar);
            contactNameTextView = (TextView) itemView.findViewById(R.id.tv_contact_name);
//            contactId = (TextView) itemView.findViewById(R.id.tv_contact_id);
        }
    }

    // 设置聊天组/部门人员/最近联系人列表数据
    public void setDataList(int type, ArrayList dataList) {
        this.mType = type;
        this.mDataList = dataList;
    }

    void setAddMemberListener(AddMemberListener addMemberListener) {
        this.mAddMemberListener = addMemberListener;
    }
//
//    public void setArrayMap(BitmapCacheManager arrayMap) {
////        this.mArrayMap = arrayMap;
//    }
}
