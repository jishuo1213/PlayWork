package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.db.bean.MailDirectory;

import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class DirectoryListAdapter extends RecyclerView.Adapter<DirectoryListAdapter.DirViewHolder> {

    private final int unselectedTextColor;
    private List<MailDirectory> dataSource;
    private DirClickListener dcListener;
    private LayoutInflater layoutInflater;
    private int selectedTextColor;
    int selected;
    public DirectoryListAdapter(Context context, List<MailDirectory> dataSource) {
        this.dataSource = dataSource;
        this.layoutInflater = LayoutInflater.from(context);
        this.selectedTextColor = context.getResources().getColor(R.color.dir_name_down);
        this.unselectedTextColor = context.getResources().getColor(R.color.wy_common_text_color);
    }

    @Override
    public DirViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DirViewHolder(layoutInflater.inflate(R.layout.wy_adapter_directory_list, null));
    }

    @Override
    public void onBindViewHolder(DirViewHolder holder, int position) {
//        holder.setSelectedTextColor(selectedTextColor);
        if(position == selected){
            holder.dirIconImageView.setImageResource(R.drawable.dir_inbox_down);
            holder.dirNameTextView.setTextColor(selectedTextColor);
        }else{
            holder.dirIconImageView.setImageResource(R.drawable.dir_inbox_normal);
            holder.dirNameTextView.setTextColor(unselectedTextColor);
        }
        holder.setData(dataSource.get(position));
        holder.setOnDirClickListener(dcListener);
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public void setSelectedDir(int selected){
        this.selected = selected;
    }

    public int getSelectedDir() {
        return selected;
    }

    public void setOnDirClickListener(DirClickListener dcListener) {
        this.dcListener = dcListener;
    }

    //元素的缓冲类,用于优化ListView
    static class DirViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView dirNameTextView;
        ImageView dirIconImageView;
        DirClickListener dcListener;
//        private int selectedTextColor;

        public DirViewHolder(View itemView) {
            super(itemView);
            dirNameTextView = (TextView) itemView.findViewById(R.id.item_directory_name);
            dirIconImageView = (ImageView) itemView.findViewById(R.id.item_directory_icon);
            itemView.setOnClickListener(this);
        }

        public void setOnDirClickListener(DirClickListener dcListener) {
            this.dcListener = dcListener;
        }

        @Override
        public void onClick(View v) {
            if (dcListener != null) {
                dcListener.switchDirectory(getAdapterPosition());
            }
        }

        public void setData(MailDirectory dirBean) {
            //设置文本和图片，然后返回这个View，用于ListView的Item的展示
            dirNameTextView.setText(dirBean.getName());
            if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_INBOX)) {
                dirIconImageView.setImageResource(R.drawable.dir_inbox_normal);
//                dirIconImageView.setImageResource(R.drawable.dir_inbox_down);
//                dirNameTextView.setTextColor(selectedTextColor);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_DRAFTBOX)) {
                dirIconImageView.setImageResource(R.drawable.dir_draftbox_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_UNREAD_MAIL)) {
                dirIconImageView.setImageResource(R.drawable.dir_unread_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_SENT_MAIL)) {
                dirIconImageView.setImageResource(R.drawable.dir_sent_mail_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_OUTBOX)) {
                dirIconImageView.setImageResource(R.drawable.dir_outbox_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_DELETED_MAIL)) {
                dirIconImageView.setImageResource(R.drawable.dir_deleted_mail_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_MARKED_MAIL)) {
                dirIconImageView.setImageResource(R.drawable.dir_marked_mail_normal);
            } else if (dirBean.getId().equals(AppConfig.WY_CFG.DIR_ID_ATTACHMENT_LIST)) {
                dirIconImageView.setImageResource(R.drawable.dir_attachment_list_normal);
            } else {
                dirIconImageView.setImageResource(R.drawable.dir_inbox_normal);
            }
        }

//        public void setSelectedTextColor(int selectedTextColor) {
//            this.selectedTextColor = selectedTextColor;
//        }
    }

    public interface DirClickListener {

        void switchDirectory(int position);
    }
}