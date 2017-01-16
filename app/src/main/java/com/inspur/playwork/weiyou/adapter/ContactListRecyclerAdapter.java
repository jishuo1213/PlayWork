package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;

import java.util.ArrayList;

/**
 * 联系人列表RecyclerAdapter
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class ContactListRecyclerAdapter extends RecyclerView.Adapter<ContactListRecyclerAdapter.ViewHolder>
        implements View.OnClickListener {

    private LayoutInflater mLayoutInflater;
    private int mType = 0;
    private ArrayList mDataList;
    private ArrayList<UserInfoBean> selectedContacts;
    private RecyclerView mRecyclerView;
    private AddMemberListener mAddMemberListener;

    public ContactListRecyclerAdapter(Context context, RecyclerView recyclerView, ArrayList<UserInfoBean> sc) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRecyclerView = recyclerView;
        this.selectedContacts = sc;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.wy_adapter_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mType == 2) { // 联系组
            GroupInfoBean groupInfoBean = (GroupInfoBean) mDataList.get(position);
            /*
            如果话题不为空，则显示话题，图标更改为话题聊天图标
            如果话题为空，则显示所有人员姓名，图标更改为非话题聊天图标
             */
//            Log.i("getSubject()-->",groupInfoBean.getSubject());
            if(groupInfoBean.getSubject()!=null){
                setAvatar(holder.contactAvatarTV,groupInfoBean.getSubject());
            }
            holder.contactEmailTV.setText(groupInfoBean.getAllMemberName());
        } else { // 联系人/部门成员
            UserInfoBean userInfoBean = (UserInfoBean) mDataList.get(position);
            if(userInfoBean.name != null){
                setAvatar(holder.contactAvatarTV, userInfoBean.name);
                holder.contactNameTV.setText(userInfoBean.name);
            }
            holder.contactEmailTV.setText(userInfoBean.id + AppConfig.EMAIL_SUFFIX);
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
//            CheckBox contact_cb = (CheckBox) v.findViewById(R.id.checkbox_mail_contact);
//            contact_cb.toggle();
            if (mType == 2) {
                mAddMemberListener.addMember((GroupInfoBean) mDataList.get(position));
            } else {
                mAddMemberListener.addMember((UserInfoBean) mDataList.get(position));
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View contactItemView;
        public TextView contactAvatarTV;
        public TextView contactNameTV;
        public TextView contactEmailTV;
//        public CheckBox contactCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);
            contactItemView = itemView;
            contactAvatarTV = (TextView) itemView.findViewById(R.id.tv_contact_avatar);
            contactNameTV = (TextView) itemView.findViewById(R.id.tv_contact_name);
            contactEmailTV = (TextView) itemView.findViewById(R.id.tv_contact_email);
//            contactCheckbox = (CheckBox) itemView.findViewById(R.id.checkbox_mail_contact);
        }
    }

    // 设置聊天组/部门人员/最近联系人列表数据
    public void setDataList(int type, ArrayList dataList) {
        this.mType = type;
        this.mDataList = dataList;
//        Log.i("setDataList--->","datalist size:"+dataList.size());
    }

    public void setAddMemberListener(AddMemberListener addMemberListener) {
        this.mAddMemberListener = addMemberListener;
    }

    private void setAvatar(TextView avatarTV,String name){
        if (name.length() > 0) {
            name = name.substring(name.length() - 1);
            int nameHash = name.hashCode()%6;
            int bgId = R.drawable.wy_avatar_bg_6;
            switch (nameHash){
                case 1:
                    bgId = R.drawable.wy_avatar_bg_1;
                    break;
                case 2:
                    bgId = R.drawable.wy_avatar_bg_2;
                    break;
                case 3:
                    bgId = R.drawable.wy_avatar_bg_3;
                    break;
                case 4:
                    bgId = R.drawable.wy_avatar_bg_4;
                    break;
                case 5:
                    bgId = R.drawable.wy_avatar_bg_5;
                    break;
            }
            avatarTV.setText(name);
            avatarTV.setBackgroundResource(bgId);
        } else {
            avatarTV.setText("空");
            avatarTV.setBackgroundResource(R.drawable.wy_avatar_bg_6);
        }
    }
//    public void setArrayMap(BitmapCacheManager arrayMap) {
//        this.mArrayMap = arrayMap;
//    }
}
