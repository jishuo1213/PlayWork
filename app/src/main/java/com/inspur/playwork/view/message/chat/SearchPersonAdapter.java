package com.inspur.playwork.view.message.chat;

import android.content.Context;
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
class SearchPersonAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<UserInfoBean> mDataList;
    private Filter mFilter;

    SearchPersonAdapter(Context context) {
        this.mContext = context;
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
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.layout_search_contact, parent, false);
        }
        UserInfoBean bean = mDataList.get(position);
        TextView contactName = ViewHolder.get(convertView, R.id.tv_contact_name);
        TextView id = ViewHolder.get(convertView, R.id.tv_contact_id);

        contactName.setText(bean.name);
        id.setText(bean.id);
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
}
