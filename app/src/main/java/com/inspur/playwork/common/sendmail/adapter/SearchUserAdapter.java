package com.inspur.playwork.common.sendmail.adapter;

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
 * Created by Bugcode on 2016/3/7.
 */
public class SearchUserAdapter extends BaseAdapter implements Filterable {

    private ArrayList<UserInfoBean> dataList = new ArrayList<>();

    @Override
    public int getCount() {
        return this.dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_contact, parent, false);
        TextView contactName = ViewHolder.get(convertView, R.id.tv_contact_name);
        TextView contactId = ViewHolder.get(convertView, R.id.tv_contact_id);
        contactName.setText(this.dataList.get(position).name);
        contactId.setText(this.dataList.get(position).id);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new MyFilter();
    }

    private class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint == null) {
                filterResults.count = dataList.size();
                filterResults.values = dataList;
            }
            return filterResults;
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
        this.dataList = dataList;
    }
}
