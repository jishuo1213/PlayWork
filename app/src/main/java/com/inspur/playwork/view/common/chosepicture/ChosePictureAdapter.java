package com.inspur.playwork.view.common.chosepicture;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inspur.playwork.R;
import com.inspur.playwork.model.common.ImageFloderBean;
import com.inspur.playwork.utils.DeviceUtil;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Fan on 15-11-12.
 */
public class ChosePictureAdapter extends RecyclerView.Adapter<ChosePictureAdapter.ViewHolder> implements View.OnClickListener {


    private static final String TAG = "ChosePictureAdapter";

    private ImageFloderBean floderBean;

//    private BitmapCacheManager pictureCache;

    private RecyclerView pictureRecyclerView;

    private ArrayList<String> selectedItems;

    private ChosePictureListener listener;

    private ArrayList<ImageFloderBean> allImageFloderList;

    private int allCount;

    private int[] sectionNum;

    private boolean isNew;

    private ArrayList<String> allImagePath;

    private int pictureLength;

    void setAllImagePath(ArrayList<String> allImagePath) {
        this.allImagePath = allImagePath;
        isNew = true;
        allCount = allImagePath.size();
    }


    interface ChosePictureListener {
        void showSendText(boolean isShow);
    }

    ChosePictureAdapter(RecyclerView pictureRecyclerView) {
        this.pictureRecyclerView = pictureRecyclerView;
        selectedItems = new ArrayList<>();
        pictureLength = (DeviceUtil.getDeviceScreenWidth(pictureRecyclerView.getContext())) / 3;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chose_picture_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String path = getItem(position);
//        Bitmap image = pictureCache.getBitmapFromMemoryCache(path);
//        if (image == null) {
//            loadBitmap(path, holder.pictureView);
//        } else {
//            holder.pictureView.setImageBitmap(image);
//        }
        Glide.with(pictureRecyclerView.getContext()).load(new File(path))
                .override(pictureLength, pictureLength)
                .placeholder(R.drawable.pictures_no)
                .into(holder.pictureView);
        holder.choseStatus.setSelected(selectedItems.contains(path));
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return allCount;
    }


    public String getItem(int pos) {

        if (isNew)
            return allImagePath.get(pos);

        int cursor = 0;
        ImageFloderBean currentFloderBean;

        while (pos >= sectionNum[cursor]) {
            cursor++;
        }

        currentFloderBean = allImageFloderList.get(cursor);
        if (cursor >= 1)
            return currentFloderBean.floderPath + File.separator + currentFloderBean.pictureName.get(pos - (sectionNum[cursor - 1]));
        else
            return currentFloderBean.floderPath + File.separator + currentFloderBean.pictureName.get(pos);
    }

    public void setListener(ChosePictureListener listener) {
        this.listener = listener;
    }

//    void setPictureCache(BitmapCacheManager pictureCache) {
//        this.pictureCache = pictureCache;
//    }

    void setFloderBean(ImageFloderBean floderBean) {
        this.floderBean = floderBean;
    }


    ArrayList<String> getSelectedItems() {
        return selectedItems;
    }

    public void setAllImageFloderList(ArrayList<ImageFloderBean> allImageFloderList) {
        this.allImageFloderList = allImageFloderList;
        sectionNum = new int[allImageFloderList.size()];
        allCount = 0;
        int i = 0;
        for (ImageFloderBean imageFloderBean : allImageFloderList) {
            allCount += imageFloderBean.pictureName.size();
            sectionNum[i] = allCount;
            i++;
        }
    }

    private void loadBitmap(String path, ImageView imageView) {
//        if (PictureUtils.cancelPotentialWork(path, imageView)) {
//            LoadBitmapWorkerTask task = new LoadBitmapWorkerTask(imageView, pictureCache);
//            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//            imageView.setImageDrawable(drawable);
//            task.execute(path, null);
//        }
    }

    @Override
    public void onClick(View v) {
        int pos = pictureRecyclerView.getChildAdapterPosition(v);
        ViewHolder viewHolder = (ViewHolder) pictureRecyclerView.findViewHolderForAdapterPosition(pos);
        viewHolder.choseStatus.setSelected(!viewHolder.choseStatus.isSelected());
        String path = getItem(pos);
        if (viewHolder.choseStatus.isSelected()) {
            selectedItems.add(path);
        } else {
            selectedItems.remove(path);
        }
        if (selectedItems.size() > 0) {
            listener.showSendText(true);
        } else {
            listener.showSendText(false);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView pictureView;
        TextView choseStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            pictureView = (ImageView) itemView.findViewById(R.id.id_item_image);
            choseStatus = (TextView) itemView.findViewById(R.id.id_item_select);
        }
    }


    public static void main(String[] argv) {

        int[] sectionNum = {0, 9, 13, 25, 34, 35, 67, 80, 82, 85, 90};

        int last = sectionNum.length - 1;

        int first = 0;
        int middle = -10;

        int pos = 89;
        while (first <= last) {
            middle = (last + first) / 2;
            if (pos > sectionNum[middle]) {
                first = middle + 1;
            } else if (pos < sectionNum[middle]) {
                last = middle - 1;
            } else {
                break;
            }
        }
        System.out.println(middle);
        System.out.println(sectionNum[middle - 1]);
    }

}
