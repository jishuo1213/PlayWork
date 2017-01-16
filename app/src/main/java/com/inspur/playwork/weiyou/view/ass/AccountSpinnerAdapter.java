package com.inspur.playwork.weiyou.view.ass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.db.bean.MailAccount;

import java.util.ArrayList;
import java.util.List;

public class AccountSpinnerAdapter<T> extends BaseAdapter {
	public static interface IOnItemSelectListener{
		public void onItemClick(int pos);
	};
	
	 private Context mContext;   
	 private List<MailAccount> mObjects = new ArrayList<>();
	 private int mSelectItem = 0;
	    
	 private LayoutInflater mInflater;
	
	 public AccountSpinnerAdapter(Context context){
		 init(context);
	 }
	 
	 public void refreshData(List<MailAccount> objects, int selIndex){
		 mObjects = objects;
		 if (selIndex < 0){
			 selIndex = 0;
		 }
		 if (selIndex >= mObjects.size()){
			 selIndex = mObjects.size() - 1;
		 }
		 mSelectItem = selIndex;
	 }
	 
	 private void init(Context context) {
	        mContext = context;
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	    
	    
	@Override
	public int getCount() {

		return mObjects.size();
	}

	@Override
	public Object getItem(int pos) {
		return mObjects.get(pos).getEmail();
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		 ViewHolder viewHolder;
    	 
	     if (convertView == null) {
	    	 convertView = mInflater.inflate(R.layout.wy_layout_spiner_item, null);
	         viewHolder = new ViewHolder();
	         viewHolder.mTextView = (TextView) convertView.findViewById(R.id.textView);
	         convertView.setTag(viewHolder);
	     } else {
	         viewHolder = (ViewHolder) convertView.getTag();
	     }

	     
	     String item = (String) getItem(pos);
		 viewHolder.mTextView.setText(item);

	     return convertView;
	}

	public static class ViewHolder
	{
	    public TextView mTextView;
    }


}
