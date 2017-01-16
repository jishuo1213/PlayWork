package com.inspur.playwork.view.application.addressbook;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by fan on 17-1-6.
 */
class SearchSuggestAdapter extends CursorAdapter {
    private static final String TAG = "SearchSuggestAdapter";

    SearchSuggestAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(TAG, "newView: ========");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_search_suggest, parent, false);
        SearchSuggestAdapter.ViewHolder viewHolder = new ViewHolder(v);
        v.setTag(viewHolder);
        return v;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.i(TAG, "bindView:  ");
        SearchSuggestAdapter.ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.name.setText(cursor.getString(1));
        viewHolder.id.setText(cursor.getString(2));
    }

    private static class ViewHolder {
        public ImageView avatar;
        public TextView name;
        public TextView id;

        public ViewHolder(View v) {
            avatar = (ImageView) v.findViewById(R.id.iv_contact_avatar);
            name = (TextView) v.findViewById(R.id.tv_contact_name);
            id = (TextView) v.findViewById(R.id.tv_contact_id);
        }
    }
}
