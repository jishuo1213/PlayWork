package com.inspur.playwork.weiyou.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;

import java.util.ArrayList;

/**
 * Created by Fan on 15-10-6.
 */
public class ContactSelectorRecyclerAdapter extends RecyclerView.Adapter<ContactSelectorRecyclerAdapter.ViewHolder>
        implements View.OnClickListener {
    public final static String TAG = "ContactSelectorRecyclerAdapter";
    private ArrayList<UserInfoBean> dataList;
    private RecyclerView chatPersonRecyclerView;

    private LayoutInflater inflater;

    private PersonSelectedListener selectedListener;

    private int type = 0; // 操作类型 1、正在聊天->屏蔽/退出/联系列表 2、屏蔽->正在聊天 3、退出->正在聊天

    public interface PersonSelectedListener {
        void onPersonSelected(UserInfoBean userInfoBean);
    }

    public ContactSelectorRecyclerAdapter(RecyclerView chatPersonRecyclerView) {
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
        holder.contactNameTV.setText(mBean.name);
        holder.contactEmailTV.setText(mBean.id + AppConfig.EMAIL_SUFFIX);
        if (mBean.name.length() > 0) {
            holder.contactNameTV.setText(mBean.name.substring(mBean.name.length() - 1, mBean.name.length()));
        }
        holder.rootView.setOnClickListener(this);

        String filePath = mBean.getAvatarPath();
        Log.i("filePath--------", filePath);
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

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onClick(View v) {
//        int pos = chatPersonRecyclerView.getChildAdapterPosition(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView contactAvatarTV;
        public TextView contactNameTV;
        public TextView contactEmailTV;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            contactAvatarTV = (TextView) itemView.findViewById(R.id.tv_contact_avatar);
            contactNameTV = (TextView) itemView.findViewById(R.id.tv_contact_name);
            contactEmailTV = (TextView) itemView.findViewById(R.id.tv_contact_email);
        }
    }
}
