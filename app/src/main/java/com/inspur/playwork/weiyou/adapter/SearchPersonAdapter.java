package com.inspur.playwork.weiyou.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.ViewHolder;

import java.util.ArrayList;

/**
 * Created by 笑面V客 on 15-10-30.
 */
public class SearchPersonAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<UserInfoBean> selectedContacts;
    private ArrayList<UserInfoBean> mDataList;
    private boolean isSelector;
    private Filter mFilter;

    public SearchPersonAdapter(Context context, ArrayList<UserInfoBean> sc,boolean isSelector) {
        this.mContext = context;
        this.selectedContacts = sc;
        this.isSelector = isSelector;
    }

    @Override
    public int getCount() {
        return this.mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.wy_adapter_contact_list, parent, false);
        }
        TextView contactAvatar = ViewHolder.get(convertView, R.id.tv_contact_avatar);
        TextView contactName = ViewHolder.get(convertView, R.id.tv_contact_name);
        TextView contactEmail = ViewHolder.get(convertView, R.id.tv_contact_email);
        UserInfoBean uib = mDataList.get(position);
        if(TextUtils.isEmpty(uib.name)){
            uib.name = uib.email;
        }
        contactName.setText(uib.name);
        contactEmail.setText(uib.email);
        setAvatar(contactAvatar,uib.name);
//        boolean hasAlready = false;
//        for (UserInfoBean u : selectedContacts) {
//            if (u.id.equals(uib.id)) {
//                hasAlready = true;
//            }
//        }
//        CheckBox contactCheckbox = ViewHolder.get(convertView, R.id.checkbox_mail_contact);
//        if(isSelector){
//            contactCheckbox.setChecked(hasAlready);
//        }else{
//            contactCheckbox.setVisibility(View.GONE);
//        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ListFilter();
        }
        return mFilter;
    }

    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                results.count = mDataList.size();
                results.values = mDataList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public void setDataList(ArrayList<UserInfoBean> dataList) {
        this.mDataList = dataList;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setAvatar(TextView avatarTV,String name){
        if (name.length() > 0) {
            if(name.endsWith(")")&&name.length()>1){
                name = name.substring(name.length() - 2,name.length() - 1);
            }else{
                name = name.substring(name.length() - 1);
            }
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
            avatarTV.setBackground(avatarTV.getResources().getDrawable(bgId));
        } else {
            avatarTV.setText("空");
            avatarTV.setBackground(avatarTV.getResources().getDrawable(R.drawable.wy_avatar_bg_6));
        }
    }
}
