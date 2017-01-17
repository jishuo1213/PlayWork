package com.inspur.playwork.view.application.news;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.R;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.utils.CommonUtils;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-16.
 */
class RecyclerNewsAdapter extends RecyclerView.Adapter<RecyclerNewsAdapter.ViewHolder> {
    private static final String TAG = "RecyclerNewsAdapter";

    private static final int FOOTER_VIEW = 1;
    private static final int ITEM_VIEW = 2;

    private ArrayList<DepartmentNewsBean> newsBeanArrayList;
    private RecyclerView recyclerView;
    private NewsListEventListener listEventListener;


    interface NewsListEventListener {
        void onNewsClick(DepartmentNewsBean newsBean);

        void onLoadMoreClick();
    }

    RecyclerNewsAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    void setListEventListener(NewsListEventListener listEventListener) {
        this.listEventListener = listEventListener;
    }

    @Override
    public int getItemViewType(int position) {
        Log.i(TAG, "getItemViewType: " + position);
        if (position == newsBeanArrayList.size()) {
            return FOOTER_VIEW;
        }
        return ITEM_VIEW;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resId = -1;
        switch (viewType) {
            case FOOTER_VIEW:
                resId = R.layout.layout_footer_view;
                break;
            case ITEM_VIEW:
                resId = R.layout.layout_news_item;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.type) {
            case ITEM_VIEW:
                DepartmentNewsBean departmentNewsBean = getItem(position);
                holder.dateView.setText(departmentNewsBean.date);
                holder.newsTitle.setText(departmentNewsBean.title);
                if (CommonUtils.isUrlVliad(departmentNewsBean.imageUrl)) {
                    String res = departmentNewsBean.imageUrl.replace("_small", "");
                    Glide.with(holder.itemView.getContext()).load(res).
                            diskCacheStrategy(DiskCacheStrategy.RESULT).placeholder(R.drawable.inspur_logo)
                            .into(holder.newsThumb);
                } else {
                    Glide.with(holder.itemView.getContext()).load(R.drawable.inspur_logo).into(holder.newsThumb);
                }
                holder.itemView.setOnClickListener(newsClickListenr);
                break;
            case FOOTER_VIEW:
                holder.loadMoreView.setOnClickListener(loadMoreClickListener);
                holder.loadingView.setVisibility(View.GONE);
                holder.loadMoreView.setVisibility(View.VISIBLE);
                break;
        }
    }

    void setNewsBeanArrayList(ArrayList<DepartmentNewsBean> newsBeanArrayList) {
        this.newsBeanArrayList = newsBeanArrayList;
    }

    public DepartmentNewsBean getItem(int pos) {
        return newsBeanArrayList.get(pos);
    }

    @Override
    public int getItemCount() {
        return newsBeanArrayList.size() + 1;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        int type;
        ImageView newsThumb;
        TextView newsTitle;
        TextView dateView;
        TextView loadMoreView;
        View loadingView;


        public ViewHolder(View itemView, int type) {
            super(itemView);
            this.type = type;
            switch (type) {
                case FOOTER_VIEW:
                    loadMoreView = (TextView) itemView.findViewById(R.id.tv_load_more);
                    loadingView = itemView.findViewById(R.id.tv_load_more_progress);
                    break;
                case ITEM_VIEW:
                    newsThumb = (ImageView) itemView.findViewById(R.id.image_news_thumbnail);
                    newsTitle = (TextView) itemView.findViewById(R.id.tv_news_title);
                    dateView = (TextView) itemView.findViewById(R.id.tv_news_date);
                    break;
            }
        }
    }

    private View.OnClickListener newsClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition(v);
            DepartmentNewsBean bean = getItem(pos);
            if (listEventListener != null) {
                listEventListener.onNewsClick(bean);
            }
        }
    };

    private View.OnClickListener loadMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listEventListener != null) {
                setFooterViewRefresh(true);
                listEventListener.onLoadMoreClick();
            }
        }
    };

    void setFooterViewRefresh(boolean isRefresh) {
        ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(getItemCount() - 1);
        if(viewHolder == null)
            return;
        if (isRefresh) {
            if (viewHolder.type == FOOTER_VIEW) {
                viewHolder.loadMoreView.setVisibility(View.GONE);
                viewHolder.loadingView.setVisibility(View.VISIBLE);
            }
        } else {
            if (viewHolder.type == FOOTER_VIEW) {
                viewHolder.loadMoreView.setVisibility(View.VISIBLE);
                viewHolder.loadingView.setVisibility(View.GONE);
            }
        }
    }
}
