package com.inspur.playwork.common.chosefile;

import android.graphics.Bitmap;
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
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.loadpicture.AsyncDrawable;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;
import com.inspur.playwork.utils.loadpicture.LoadThumbWorkerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by fan on 16-3-28.
 */
class ChoseLocalFileAdapter extends RecyclerView.Adapter<ChoseLocalFileAdapter.ViewHolder> {

    private static final String TAG = "ChoseLocalFileFan";

    private ArrayList<LocalFileBean> localFilesList;
    private RecyclerView recyclerView;

    private Set<String> selectFiles;
    private int selectedPosition = -1;
    private ChoseFileEventListener listener;

    private BitmapCacheManager photoCache;

    interface ChoseFileEventListener {
        void onFileSelected(LocalFileBean choseBean, boolean isSelected);

        void onOpenNewFloder(LocalFileBean parentBean);
    }

    ChoseLocalFileAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        selectFiles = new HashSet<>();
    }

    void setPhotoCache(BitmapCacheManager photoCache) {
        this.photoCache = photoCache;
    }

    @Override
    public int getItemViewType(int position) {
        LocalFileBean mBean = localFilesList.get(position);
        if (mBean.isRoot)
            return LocalFileBean.ROOT_DIR;
        if (mBean.isDir)
            return LocalFileBean.NORMAL_DIR;
        return LocalFileBean.NORMAL_FILE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: " + viewType);
        int layoutId = -1;
        switch (viewType) {
            case LocalFileBean.NORMAL_FILE:
                layoutId = R.layout.layout_chose_file_file;
                break;
            case LocalFileBean.ROOT_DIR:
                layoutId = R.layout.layout_chose_file_root;
                break;
            case LocalFileBean.NORMAL_DIR:
                layoutId = R.layout.layout_chose_file_root;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocalFileBean mBean = getItem(position);
        holder.name.setText(mBean.name);
        switch (getItemViewType(position)) {
            case LocalFileBean.NORMAL_FILE:
                holder.fileSize.setText(mBean.getFileSizeStr());
                holder.createTime.setText(DateUtils.getTimeHasNoSecond(mBean.createTime));
                holder.selectStatus.setSelected(selectFiles.contains(mBean.currentPath));
                holder.itemView.setOnClickListener(normalFileClickListener);
                switch (mBean.fileType) {
                    case LocalFileBean.APK:
                        Bitmap bitmap = photoCache.getBitmapFromMemoryCache(mBean.currentPath);
                        if (bitmap == null)
                            loadApkBitmap(mBean.currentPath, holder.icon);
                        else
                            holder.icon.setImageBitmap(bitmap);
                        break;
                    case LocalFileBean.ZIP:
                        holder.icon.setImageResource(R.drawable.attachment_compress);
                        break;
                    case LocalFileBean.IMAGE:
                        holder.icon.setImageResource(R.drawable.attachment_image);

                        loadThumbBitmap(mBean.currentPath, holder.icon);
                        break;
                    case LocalFileBean.PDF:
                        holder.icon.setImageResource(R.drawable.document);
                        break;
                    case LocalFileBean.VIDEO:
                        holder.icon.setImageResource(R.drawable.attachment_video);
                        break;
                    case LocalFileBean.TXT:
                        holder.icon.setImageResource(R.drawable.attachment_word);
                        break;
                    case LocalFileBean.AUDIO:
                        holder.icon.setImageResource(R.drawable.attachment_audio);
                        break;
                    case LocalFileBean.OTHERS:
                        holder.icon.setImageResource(R.drawable.document);
                        break;
                }
                break;
            case LocalFileBean.ROOT_DIR:
                holder.itemView.setOnClickListener(dirClickListener);
                break;
            case LocalFileBean.NORMAL_DIR:
                holder.icon.setImageResource(R.drawable.folder);
                holder.itemView.setOnClickListener(dirClickListener);
                break;
        }
    }

    private void loadThumbBitmap(String path, ImageView imageView) {

        Glide.with(imageView.getContext()).load(new File(path)).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
    }

    private void loadApkBitmap(String path, ImageView imageView) {
        if (PictureUtils.cancelPotentialWork(path, imageView)) {
            LoadThumbWorkerTask task = new LoadThumbWorkerTask(imageView, photoCache);
            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
            imageView.setImageDrawable(drawable);
            task.execute(path);
        }
    }


    @Override
    public int getItemCount() {
        return localFilesList.size();
    }


    private LocalFileBean getItem(int position) {
        return localFilesList.get(position);
    }


    public void setListener(ChoseFileEventListener listener) {
        this.listener = listener;
    }

    public void show(ArrayList<LocalFileBean> fileList, boolean needNotify) {
        localFilesList = fileList;
        if (needNotify)
            notifyDataSetChanged();
    }

    public void setSingleSelectedMode(boolean singleSelectedMode) {
        this.singleSelectedMode = singleSelectedMode;
    }

    private boolean singleSelectedMode;

    private View.OnClickListener normalFileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildAdapterPosition(v);
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            viewHolder.selectStatus.setSelected(!viewHolder.selectStatus.isSelected());
            LocalFileBean clickBean = getItem(pos);
            boolean isSelected = viewHolder.selectStatus.isSelected();
            listener.onFileSelected(clickBean, isSelected);
            Log.i(TAG, "normalFileClickListener: "+isSelected);
            if (isSelected) {
                if(singleSelectedMode) {
                    selectFiles.clear();
                    if (selectedPosition > -1 && selectedPosition != pos) {
                        ViewHolder preSelectedViewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedPosition);
                        preSelectedViewHolder.selectStatus.setSelected(false);
                    }
                    selectedPosition = pos;
                }
                selectFiles.add(clickBean.currentPath);
            }else
                selectFiles.remove(clickBean.currentPath);
        }
    };

    public Set<String> getSelectFiles() {
        return selectFiles;
    }

    private View.OnClickListener dirClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildAdapterPosition(v);
            Log.i(TAG, "onClick: " + pos);
            LocalFileBean clickBean = getItem(pos);
//            show(clickBean.getChildFiles(), true);
            listener.onOpenNewFloder(clickBean);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageView icon;
        ImageView selectStatus;
        TextView fileSize;
        public TextView createTime;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_file_name);
            icon = (ImageView) itemView.findViewById(R.id.img_file_icon);
            selectStatus = (ImageView) itemView.findViewById(R.id.btn_file_select_status);
            fileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
            createTime = (TextView) itemView.findViewById(R.id.tv_file_create_time);
        }
    }
}
