package com.inspur.playwork.weiyou.view;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.view.ass.AccountSpinnerAdapter;
import com.inspur.playwork.weiyou.view.ass.AccountSpinnerAdapter.IOnItemSelectListener;

import java.util.List;

public class AccountSelectorSpinner extends PopupWindow implements OnItemClickListener {

    private WeiYouMainActivity mContext;
    private ListView mListView;
    private AccountSpinnerAdapter mAdapter;
    private IOnItemSelectListener mItemSelectListener;

    public AccountSelectorSpinner(WeiYouMainActivity context) {
        super(context);
        mContext = context;
        init();
    }

    public void setItemListener(AccountSpinnerAdapter.IOnItemSelectListener listener) {
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

        mAdapter = new AccountSpinnerAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    public void refreshData(List<MailAccount> list, int selIndex) {
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

    @Override
    public void dismiss() {
        try {
            mContext.accountSelectorIV.setImageResource(R.drawable.dir_droplist_normal);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.dismiss();
    }
}
