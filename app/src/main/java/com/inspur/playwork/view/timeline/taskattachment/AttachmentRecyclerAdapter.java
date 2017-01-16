package com.inspur.playwork.view.timeline.taskattachment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.DownLoadFileTask;

import java.util.ArrayList;

/**
 * Created by Fan on 15-9-18.
 */
public class AttachmentRecyclerAdapter extends RecyclerView.Adapter<AttachmentRecyclerAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "AttachRecyAdapterFan";

    private static final int LOCAL_FILE_MODE = 1;
    private static final int ATTACHMENT_FILE_MODE = 2;
    private ArrayList<TaskAttachmentBean> attachmentList;

    private ArrayList<LocalFileBean> localFileBeans;

    private RecyclerView attachmentRecyclerView;

    private int type;

    public AttachmentRecyclerAdapter(RecyclerView attachmentRecyclerView) {
        this.attachmentRecyclerView = attachmentRecyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_attachment_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (type == ATTACHMENT_FILE_MODE) {
            TaskAttachmentBean attachmentBean = attachmentList.get(position);
            holder.attachmentName.setText(attachmentBean.attachmentName);
            holder.attachmentSize.setText(attachmentBean.getSize());
            if (attachmentBean.isAttachmentDownloaded())
                holder.attachmentState.setText("已下载");
            else {
                holder.attachmentState.setText("未下载");
            }
            //TODO:用正则表达式判断
            if (attachmentBean.attachmentName.endsWith("doc")) {
                holder.attachmentIcon.setImageResource(R.drawable.attachment_word);
            } else if (attachmentBean.attachmentName.endsWith("ppt")) {
                holder.attachmentIcon.setImageResource(R.drawable.attachment_ppt);
            } else if (attachmentBean.attachmentName.endsWith("xls")) {
                holder.attachmentIcon.setImageResource(R.drawable.attachment_excel);
            } else {
                holder.attachmentIcon.setImageResource(R.drawable.attachment_common);
            }

            holder.rootView.setOnClickListener(this);
        } else if (type == LOCAL_FILE_MODE) {
            LocalFileBean mBean = localFileBeans.get(position);
            holder.attachmentName.setText(mBean.name);
            holder.attachmentSize.setText(mBean.getFileSizeStr());
            holder.attachmentState.setText("删除");
            holder.attachmentState.setOnClickListener(this);
        }
    }

    public void setAttachmentList(ArrayList<TaskAttachmentBean> attachmentList) {
        this.attachmentList = attachmentList;
        type = ATTACHMENT_FILE_MODE;
        //notifyDataSetChanged();
    }

    public void setLocalFileList(ArrayList<LocalFileBean> localFileBeans, boolean needNotify) {
        this.localFileBeans = localFileBeans;
        type = LOCAL_FILE_MODE;
        if (needNotify)
            notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (type == ATTACHMENT_FILE_MODE)
            return attachmentList.size();
        else
            return localFileBeans.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_attach_state && type == LOCAL_FILE_MODE) {
            int pos = attachmentRecyclerView.getChildAdapterPosition((View) v.getParent());
            localFileBeans.remove(pos);
            notifyItemRemoved(pos);
            return;
        }
        int pos = attachmentRecyclerView.getChildAdapterPosition(v);
        ViewHolder holder = (ViewHolder) attachmentRecyclerView.findViewHolderForAdapterPosition(pos);
        TaskAttachmentBean mBean = attachmentList.get(pos);
        if (!mBean.isAttachmentDownloaded())
            downLoadFile(mBean.getAttachFilePath(), mBean.getAttachDownLoadUrl(), holder.attachmentState);
        else {
            Intent intent = FileUtil.getOpenFileIntent(holder.rootView.getContext(),mBean.getAttachFilePath());
            if (intent == null)
                UItoolKit.showToastShort(holder.rootView.getContext(), "未识别的文件类型");
            else
                try {
                    holder.rootView.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    UItoolKit.showToastShort(holder.rootView.getContext(),"没有找到应用程序打开该类型的文件");
                }
        }
    }

    private void downLoadFile(String filePath, String url, TextView textView) {
        DownLoadFileTask loadFileTask = new DownLoadFileTask(textView);
        textView.setTag(loadFileTask);
        loadFileTask.execute(filePath, url);
    }

    public LocalFileBean updateFileByPath(String filePath, boolean success) {
        int index = 0;
        for (LocalFileBean fileBean : localFileBeans) {
            if (fileBean.currentPath.equals(filePath)) {
                break;
            }
            index++;
        }
        Log.i(TAG, "updateFileByPath: " + index);
        if (index < localFileBeans.size()) {
            AttachmentRecyclerAdapter.ViewHolder viewHolder = (ViewHolder) attachmentRecyclerView.findViewHolderForAdapterPosition(index);
            if (success)
                viewHolder.attachmentState.setText("上传成功");
            else {
                viewHolder.attachmentState.setText("上传失败");
                localFileBeans.remove(index);
                notifyItemRemoved(index);
            }
            return localFileBeans.get(index);
        }
        return null;
    }

    public void updateProgress(String clientId, int progress) {
        int index = 0;
        for (LocalFileBean fileBean : localFileBeans) {
            if (fileBean.currentPath.equals(clientId)) {
                break;
            }
            index++;
        }
        if (index < localFileBeans.size()) {
            AttachmentRecyclerAdapter.ViewHolder viewHolder = (ViewHolder) attachmentRecyclerView.findViewHolderForAdapterPosition(index);
            viewHolder.attachmentState.setText(progress + ".0%");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView attachmentName;
        public ImageView attachmentIcon;
        public TextView attachmentSize;
        public TextView attachmentCreateTime;
        public TextView attachmentState;
        public View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            attachmentName = (TextView) itemView.findViewById(R.id.tv_attach_name);
            attachmentIcon = (ImageView) itemView.findViewById(R.id.img_attachment_icon);
            attachmentSize = (TextView) itemView.findViewById(R.id.tv_attach_size);
            attachmentCreateTime = (TextView) itemView.findViewById(R.id.tv_attach_create_time);
            attachmentState = (TextView) itemView.findViewById(R.id.tv_attach_state);
        }
    }
}
