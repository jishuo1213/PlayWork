package com.inspur.playwork.view.message.chat;

import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.loadfile.DownLoadPictureTask;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Fan on 15-10-6.
 */
public class ChosePersonRecyclerAdapter extends RecyclerView.Adapter<ChosePersonRecyclerAdapter.ViewHolder>
        implements View.OnClickListener {

    private ArrayList<UserInfoBean> dataList;
    private RecyclerView chatPersonRecyclerView;

    private LayoutInflater inflater;

    private PersonSelectedListener selectedListener;

    private ChangeMemberListener changeListener;

//    private BitmapCacheManager arrayMap;

    private boolean isQuickChat = false;

    private ArrayMap<String, Long> avatars;

    private int type = 0; // 操作类型 1、正在聊天->屏蔽/退出/联系列表 2、屏蔽->正在聊天 3、退出->正在聊天

    public interface PersonSelectedListener {
        void onPersonSelected(UserInfoBean userInfoBean);
    }

    public interface ChangeMemberListener {
        /**
         * 人员调整接口回调方法
         *
         * @param userInfoBean 需要操作的人员对象
         * @param type         操作类型 1、正在聊天->屏蔽/退出/联系列表 2、屏蔽->正在聊天 3、退出->正在聊天
         */
        void onChangeMember(UserInfoBean userInfoBean, int type);
    }

    public ChosePersonRecyclerAdapter(RecyclerView chatPersonRecyclerView) {
        this.chatPersonRecyclerView = chatPersonRecyclerView;
        inflater = LayoutInflater.from(chatPersonRecyclerView.getContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.layout_chose_chat_person_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserInfoBean mBean = dataList.get(position);
        holder.userName.setText(mBean.name);
        holder.rootView.setOnClickListener(this);

        if (avatars.containsKey(mBean.id)) {
            long avatarId = avatars.get(mBean.id);
            if (avatarId >= mBean.avatar) {
                mBean.avatar = avatarId;
            } else {
                avatars.put(mBean.id, mBean.avatar);
            }
        } else {
            avatars.put(mBean.id, mBean.avatar);
        }

//        String filePath = mBean.getAvatarPath();
//        Bitmap avater = arrayMap.getBitmapFromMemoryCache(filePath);
//        if (avater != null) {
//            holder.userAvatar.setImageBitmap(avater);
//        } else {
        if (mBean.isAvatarFileExit()) {
            loadAvatarBitmap(mBean.getAvatarPath(), holder.userAvatar);
        } else {
            downLoadAvatar(mBean.getAvatarPath(), AppConfig.AVATAR_ROOT_PATH + mBean.avatar, holder.userAvatar);
        }
//        }
    }

    private void loadAvatarBitmap(String path, ImageView imageView) {
        Glide.with(imageView.getContext()).
                load(new File(path)).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                placeholder(R.drawable.icon_chat_default_avatar).
                into(imageView);
    }

    private void downLoadAvatar(String filePath, String url, ImageView userAvatar) {
        DownLoadPictureTask downLoadPictureTask = new DownLoadPictureTask(userAvatar);
        downLoadPictureTask.execute(filePath, url);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setChatPersonList(ArrayList<UserInfoBean> dataList) {
        this.dataList = dataList;
    }

    public void setSelectedListener(PersonSelectedListener selectedListener) {
        this.selectedListener = selectedListener;
    }

    public void setChangeListener(ChangeMemberListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onClick(View v) {
        int pos = chatPersonRecyclerView.getChildAdapterPosition(v);
        if (isQuickChat && (selectedListener != null)) {
            selectedListener.onPersonSelected(dataList.get(pos));
        }
        if (!isQuickChat && (changeListener != null)) {
            changeListener.onChangeMember(dataList.get(pos), type);
        }
    }

//    public void setArrayMap(BitmapCacheManager arrayMap) {
//        this.arrayMap = arrayMap;
//    }

    public void setAvatars(ArrayMap<String, Long> avatars) {
        this.avatars = avatars;
    }

    /**
     * 设置快速聊天模式
     *
     * @param isQuickChat
     */
    public void setIsQuickChat(boolean isQuickChat) {
        this.isQuickChat = isQuickChat;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userAvatar;
        public TextView userName;
        public View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            userAvatar = (ImageView) itemView.findViewById(R.id.img_people_icon);
            userName = (TextView) itemView.findViewById(R.id.tv_people_name);
        }
    }
}
