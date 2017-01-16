package com.inspur.playwork.view.application.news;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;


/**
 * Created by fan on 17-1-15.
 */
class TabRecyAdapter extends RecyclerView.Adapter<TabRecyAdapter.ViewHolder> {
    private static final String TAG = "TabRecyAdapter";

    interface TabEventListener {
        void onTabClick(int pos);
    }

    private String[] tabNames;

    private TabEventListener listener;

    private RecyclerView recyclerView;

    TabRecyAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_textview, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(getItem(position));
        holder.itemView.setOnClickListener(tabClickListener);
    }

    @Override
    public int getItemCount() {
        return tabNames.length;
    }

    public String getItem(int pos) {
        return tabNames[pos];
    }

    void setTabNames(String[] tabNames) {
        this.tabNames = tabNames;
    }

    public void setListener(TabEventListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private View.OnClickListener tabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                int pos = recyclerView.getChildLayoutPosition(v);
                listener.onTabClick(pos);
            }
        }
    };
}
