package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;

import java.io.File;
import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class CAFileListAdapter extends BaseAdapter {


    private static final String TAG = "CAFileListAdapter-->";
    private Context mContext;
    private List<File> dataSource;

    public CAFileListAdapter(Context context, List<File> caListData) {
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
            view = LayoutInflater.from(mContext).inflate(R.layout.wy_view_ca_file, null);
            viewCache = new ItemViewCache();
            viewCache.caFileNameTV = (TextView) view.findViewById(R.id.ca_file_name);
            view.setTag(viewCache);
        } else {
            viewCache = (ItemViewCache) view.getTag();
        }

        File cf = (File)getItem(i);
        viewCache.caFileNameTV.setText(cf.getName());

        return view;
    }

    //元素的缓冲类,用于优化ListView
    private static class ItemViewCache {
        public TextView caFileNameTV;
    }
}