package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;

import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class SelectedContactAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserInfoBean> dataSource;

    public SelectedContactAdapter(Context context, List<UserInfoBean> dataSource) {
        this.mContext = context;
        this.dataSource = dataSource;
    }

    /**
     * 元素的个数
     */
    public int getCount() {
        return dataSource.size();
    }

    public Object getItem(int position) {
        return dataSource.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    //用以生成在ListView中展示的一个个元素View
    public View getView(final int position, View convertView, ViewGroup parent) {
        //优化ListView
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wy_adapter_selected_contact_list, null);
            ItemViewCache viewCache = new ItemViewCache();
            viewCache.contactNameTextView = (TextView) convertView.findViewById(R.id.item_selected_contact);
            convertView.setTag(viewCache);
        }
        UserInfoBean uib = (UserInfoBean) getItem(position);
        ItemViewCache cache = (ItemViewCache) convertView.getTag();
        //设置文本和图片，然后返回这个View，用于ListView的Item的展示
        cache.contactNameTextView.setText(uib.name.length()>0?uib.name:uib.email);
        return convertView;
    }

    //元素的缓冲类,用于优化ListView
    private static class ItemViewCache {
        public TextView contactNameTextView;
    }
}