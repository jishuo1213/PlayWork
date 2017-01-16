package com.inspur.playwork.view.message.chat;

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
import com.inspur.playwork.model.common.SimpleUserInfoBean;
import com.inspur.playwork.utils.PictureUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Fan on 15-11-16.
 */
public class PopWindowAdapter extends RecyclerView.Adapter<PopWindowAdapter.ViewHolder> {

    private static final String TAG = "PopWindowAdapterFan";

    private ArrayList<SimpleUserInfoBean> userBeanList;

//    private BitmapCacheManager avatarCache;

    private ArrayMap<String, Long> avatars;

    public void setUserBeanList(ArrayList<SimpleUserInfoBean> userBeanList) {
        this.userBeanList = userBeanList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chose_chat_person_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleUserInfoBean mBean = getItem(position);


        holder.name.setText(mBean.name);

        if (avatars.containsKey(mBean.userId)) {
            long avatarId = avatars.get(mBean.userId);
            if (avatarId >= mBean.avatar) {
                mBean.avatar = avatarId;
            } else {
                avatars.put(mBean.userId, mBean.avatar);
            }
        } else {
            avatars.put(mBean.userId, mBean.avatar);
        }

        String filePath = mBean.getAvatarPath();

//        Bitmap avatar = avatarCache.getBitmapFromMemoryCache(filePath);
//        if (avatar != null) {
//            holder.imageView.setImageBitmap(avatar);
//        } else {
        if (mBean.isAvatarFileExit()) {
            loadAvatarBitmap(filePath, holder.imageView);
        } else {
            holder.imageView.setImageBitmap(PictureUtils.getDefaultAvatar(holder.imageView.getContext()));
        }
//        }
    }

    private void loadAvatarBitmap(String path, ImageView imageView) {
//        if (PictureUtils.cancelPotentialWork(path, imageView)) {
//            NormalLoadBitmapTask task = new NormalLoadBitmapTask(imageView, avatarCache);
//            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//            imageView.setImageDrawable(drawable);
//            task.execute(path);
//        }
        Glide.with(imageView.getContext())
                .load(new File(path))
                .placeholder(R.drawable.icon_chat_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }


    @Override
    public int getItemCount() {
        return userBeanList.size();
    }

    public SimpleUserInfoBean getItem(int position) {
        return userBeanList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_people_name);
            imageView = (ImageView) itemView.findViewById(R.id.img_people_icon);
        }
    }

//    public void setAvatarCache(BitmapCacheManager avatarCache) {
////        this.avatarCache = avatarCache;
//    }

    public void setAvatars(ArrayMap<String, Long> avatars) {
        this.avatars = avatars;
    }
}
