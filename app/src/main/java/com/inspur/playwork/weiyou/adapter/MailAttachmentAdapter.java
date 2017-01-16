package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.weiyou.utils.VUFileUtil;

import java.util.List;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class MailAttachmentAdapter extends BaseAdapter {

    private static final String TAG = "MailAttachmentAdapter";
    private Context mContext;
    private List<MailAttachment> dataSource;
    private boolean delete_able = false;
    private boolean isSelectFile = false;
    private RemoveButtonListener rbl;

    public MailAttachmentAdapter(Context context, List<MailAttachment> dataSource,boolean deleteAble,boolean isSelectFile) {
        this.mContext = context;
        this.dataSource = dataSource;
        this.delete_able = deleteAble;
        this.isSelectFile = isSelectFile;
    }

    /**
     * 元素的个数
     */
    public int getCount() {
        return dataSource.size();
    }

    public MailAttachment getItem(int position) {
        return dataSource.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //用以生成在ListView中展示的一个个元素View
    public View getView(final int position, View convertView, ViewGroup parent) {
        //优化ListView
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wy_view_attachment, null);
            ItemViewCache viewCache = new ItemViewCache();
            viewCache.attachmentTypeImageView = (ImageView) convertView.findViewById(R.id.wy_attachment_icon);
            viewCache.attachmentNameTextView = (TextView) convertView.findViewById(R.id.wy_attachment_name);
            viewCache.attachmentSizeTextView = (TextView) convertView.findViewById(R.id.wy_attachment_size);
            viewCache.attachmentStatusTextView = (TextView) convertView.findViewById(R.id.wy_attachment_status);
            viewCache.attachmentRemoveButton = (ImageView) convertView.findViewById(R.id.wy_attachment_remove);
            viewCache.selectAttachmentCheckBox = (CheckBox) convertView.findViewById(R.id.wy_attachment_checkbox);
            convertView.setTag(viewCache);
        }
        final MailAttachment ma = getItem(position);
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String str = ((TextView) v.findViewById(R.id.wy_attachment_status)).getText().toString();
//                if (rbl != null) {
//                    if (!str.equals("已上传")) {
//                        rbl.onAttachmentClick(position);
//                    }
//                }else if (isSelectFile){
//
//                }else{
//                    String amPath = ma.getPath();
//                    mContext.startActivity(VUFileUtil.openFile(amPath));
//                }
//            }
//        });

        String am_name = ma.getName();
        if(am_name == null){
            am_name = "null";
        }
        am_name = am_name.toLowerCase();
        ItemViewCache cache = (ItemViewCache) convertView.getTag();
        //设置文本和图片，然后返回这个View，用于ListView的Item的展示
        int fileIcon = R.drawable.attachment_common;
        if (am_name.endsWith("ppt") || am_name.endsWith("pptx")) {
            fileIcon = R.drawable.attachment_ppt;
        } else if (am_name.endsWith("doc") || am_name.endsWith("docx")) {
            fileIcon = R.drawable.attachment_word;
        } else if (am_name.endsWith("xls") || am_name.endsWith("xlsx")) {
            fileIcon = R.drawable.attachment_excel;
        } else if (am_name.endsWith("zip") || am_name.endsWith("rar") || am_name.endsWith("gz")
                || am_name.endsWith("tar") || am_name.endsWith("7z")) {
            fileIcon = R.drawable.attachment_compress;
        } else if (am_name.endsWith("png") || am_name.endsWith("jpg") || am_name.endsWith("gif")
                || am_name.endsWith("bmp") || am_name.endsWith("psd") || am_name.endsWith("jpeg")) {
            fileIcon = R.drawable.attachment_image;
        } else if (am_name.endsWith("mp3") || am_name.endsWith("xmf") || am_name.endsWith("m4a")
                || am_name.endsWith("ogg") || am_name.endsWith("wav") || am_name.endsWith("mid")) {
            fileIcon = R.drawable.attachment_audio;
        } else if (am_name.endsWith("mp4") || am_name.endsWith("3gp")) {
            fileIcon = R.drawable.attachment_video;
        }
        cache.attachmentTypeImageView.setImageResource(fileIcon);
        cache.attachmentNameTextView.setText(ma.getName());
        cache.attachmentSizeTextView.setText(VUFileUtil.convertFileSize(ma.getSize()));
        cache.attachmentRemoveButton.setVisibility(View.GONE);
        if(!delete_able){
            cache.attachmentStatusTextView.setVisibility(View.VISIBLE);
//            if (ma.getPath() != null && new File(ma.getPath()).exists()) {
//                cache.attachmentStatusTextView.setText(R.string.attachment_finish_download);
//            } else {
//                cache.attachmentStatusTextView.setText(R.string.attachment_not_download);
//            }
            cache.attachmentStatusTextView.setText("打开");
        }else{
            cache.attachmentRemoveButton.setVisibility(View.VISIBLE);
            cache.attachmentRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rbl.onRemoveButtonClick(position);
                }
            });
        }
        if(isSelectFile){//是否是选择附件模式
            cache.attachmentStatusTextView.setVisibility(View.GONE);
            cache.selectAttachmentCheckBox.setVisibility(View.VISIBLE);
            cache.selectAttachmentCheckBox.setChecked(false);
        }else{
            cache.attachmentStatusTextView.setVisibility(View.VISIBLE);
            cache.selectAttachmentCheckBox.setVisibility(View.GONE);
        }

        return convertView;
    }

    //元素的缓冲类,用于优化ListView
    private static class ItemViewCache {
        public ImageView attachmentTypeImageView;
        public TextView attachmentNameTextView;
        public TextView attachmentSizeTextView;
        public TextView attachmentStatusTextView;
        public ImageView attachmentRemoveButton;
        public CheckBox selectAttachmentCheckBox;
    }

    public interface RemoveButtonListener{
        void onRemoveButtonClick(int postion);
        void onAttachmentClick(int postion);
    }

    public void setRemoveButtonListener(RemoveButtonListener rbListener){
        this.rbl = rbListener;
    }

}