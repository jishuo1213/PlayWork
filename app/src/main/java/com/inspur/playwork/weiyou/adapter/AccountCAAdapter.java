package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.weiyou.rsa.CAObject;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class AccountCAAdapter extends BaseAdapter {


    private static final String TAG = "AccountCAAdapter-->";
    private Context mContext;
    private List<CAObject> dataSource;

    public AccountCAAdapter(Context context, List<CAObject> caListData) {
        this.mContext = context;
        this.dataSource = caListData;
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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemViewCache viewCache;
        if (view == null) {;
            view = LayoutInflater.from(mContext).inflate(R.layout.wy_view_ca, null);
            viewCache = new ItemViewCache();
            viewCache.canameTV = (TextView) view.findViewById(R.id.ca_friendly_name);
            viewCache.inUsingTV = (TextView) view.findViewById(R.id.ca_in_using);
            viewCache.cnTV = (TextView) view.findViewById(R.id.ca_cn);
            viewCache.issuerTV = (TextView) view.findViewById(R.id.ca_issuer);
            viewCache.cert_dateTV = (TextView) view.findViewById(R.id.ca_cert_date);
            view.setTag(viewCache);
        } else {
            viewCache = (ItemViewCache) view.getTag();
        }

        CAObject cao = (CAObject)getItem(i);
        viewCache.canameTV.setText(cao.getCaname());
        viewCache.cnTV.setText(cao.getCn());
        viewCache.issuerTV.setText(cao.getIssuer());
        if(cao.isDefaultCA()){
            viewCache.inUsingTV.setVisibility(View.VISIBLE);
        }else{
            viewCache.inUsingTV.setVisibility(View.GONE);
        }

        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        viewCache.cert_dateTV.setText(df.format(cao.getCert_date()));

        return view;
    }

    //元素的缓冲类,用于优化ListView
    private static class ItemViewCache {
        public TextView canameTV;
        public TextView inUsingTV;
        public TextView cnTV;
        public TextView issuerTV;
        public TextView cert_dateTV;
    }
}