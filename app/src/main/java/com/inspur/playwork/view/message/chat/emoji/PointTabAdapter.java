package com.inspur.playwork.view.message.chat.emoji;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by fan on 16-9-27.
 */
public class PointTabAdapter extends RecyclerView.Adapter<PointTabAdapter.ViewHolder> implements ViewPager.OnPageChangeListener {
    private static final String TAG = "PointTabAdapter";

    private int size;

    private int selectIndex;

    private RecyclerView recyclerView;

    private TabClickListener listener;

    public interface TabClickListener {
        void onTabClick(int selectIndex);
    }

    public PointTabAdapter(RecyclerView recyclerView, int size, int selectIndex) {
        this.size = size;
        this.selectIndex = selectIndex;
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_tab_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == selectIndex) {
            holder.tab.setSelected(true);
        } else {
            holder.tab.setSelected(false);
        }
        holder.tab.setOnClickListener(tabClickListener);
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View tab;

        public ViewHolder(View itemView) {
            super(itemView);
            tab = itemView.findViewById(R.id.tv_tab);
        }
    }

    public void showNext() {
        ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
        preViewHolder.tab.setSelected(false);
        ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(++selectIndex);
        newViewHolder.tab.setSelected(true);
    }

    public void showPrevious() {
        ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
        preViewHolder.tab.setSelected(false);
        ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(--selectIndex);
        newViewHolder.tab.setSelected(true);
    }

    public void setListener(TabClickListener listener) {
        this.listener = listener;
    }

    private View.OnClickListener tabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildAdapterPosition((View) v.getParent());
            ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
            preViewHolder.tab.setSelected(false);
            ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(pos);
            newViewHolder.tab.setSelected(true);
            selectIndex = pos;
            listener.onTabClick(pos);
        }
    };


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position > selectIndex) {
            showNext();
        } else if (position < selectIndex) {
            showPrevious();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
