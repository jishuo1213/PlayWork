package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.db.bean.MailAccount;

import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class MailAccountListAdapter extends BaseAdapter {

    private static final String TAG = "MailAccountListAdapter-->";
    private Context mContext;
    private List<MailAccount> dataSource;

    public MailAccountListAdapter(Context context, List<MailAccount> maList) {
        this.mContext = context;
        this.dataSource = maList;
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.wy_adapter_settings_account_list, parent, false);
            ItemViewCache viewCache = new ItemViewCache();
            viewCache.accountNameTV = (TextView) convertView.findViewById(R.id.wy_setting_account_name);
            convertView.setTag(viewCache);
        }
        MailAccount ma = dataSource.get(position);
        ItemViewCache cache = (ItemViewCache) convertView.getTag();
        cache.accountNameTV.setText(ma.getEmail());
        return convertView;
    }

    //元素的缓冲类,用于优化ListView
    private static class ItemViewCache {
        public TextView accountNameTV;
    }
}