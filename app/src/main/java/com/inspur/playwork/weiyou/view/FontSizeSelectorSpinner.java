package com.inspur.playwork.weiyou.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.inspur.playwork.R;
import com.inspur.playwork.weiyou.view.ass.FontSizeSpinnerAdapter;

import java.util.List;

public class FontSizeSelectorSpinner extends PopupWindow implements OnItemClickListener{

    private Context mContext;
    private ListView mListView;
    private FontSizeSpinnerAdapter mAdapter;
    private FontSizeSpinnerAdapter.IOnItemSelectListener mItemSelectListener;


    public FontSizeSelectorSpinner(Context context) {
        super(context);

        mContext = context;
        init();
    }


    public void setItemListener(FontSizeSpinnerAdapter.IOnItemSelectListener listener) {
        mItemSelectListener = listener;
    }


    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.wy_layout_spiner_window, null);
        setContentView(view);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);


        mListView = (ListView) view.findViewById(R.id.listview);


        mAdapter = new FontSizeSpinnerAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }


    public void refreshData(List list, int selIndex) {
        if (list != null && selIndex != -1) {
            mAdapter.refreshData(list, selIndex);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (mItemSelectListener != null) {
            mItemSelectListener.onItemClick(pos);
        }
    }


}
