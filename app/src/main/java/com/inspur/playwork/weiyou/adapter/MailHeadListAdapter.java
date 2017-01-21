package com.inspur.playwork.weiyou.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 孙 on 2015/11/12 0012.
 */
public class MailHeadListAdapter extends SwipeMenuAdapter<MailHeadListAdapter.MailHeadViewHolder> {

    private static final String TAG = "MailHeadListAdapter-->";
    public static final int ITEM_TYPE_CONTENT = 1;
    public static final int ITEM_TYPE_BOTTOM = 2;
    private LayoutInflater mLayoutInflater;

    private ArrayList<MailDetail> dataSourceAll = new ArrayList<>();
    private static ArrayList<MailDetail> dataSource = new ArrayList<>();
    private ArrayList mFilteredArrayList = new ArrayList<>();
    private MailFilter mFilter;
    private boolean showFooterView = false;
    private ItemClickListener icListener;

    public MailHeadListAdapter(Context context,ArrayList<MailDetail> mailListData,boolean showFooterView) {
        mLayoutInflater = LayoutInflater.from(context);
        this.mFilteredArrayList = new ArrayList<>();
        this.showFooterView = showFooterView;
        this.dataSourceAll = this.dataSource = mailListData;
    }

    //判断当前item类型
    @Override
    public int getItemViewType(int position) {
        if ( showFooterView && position == getItemCount()-1) {
            //底部View
            return ITEM_TYPE_BOTTOM;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }
    @Override
    public int getItemCount() {
        int size = dataSource.size();
        return showFooterView?size + 1:size;
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        if(showFooterView && viewType == ITEM_TYPE_BOTTOM)
            return mLayoutInflater.inflate(R.layout.wy_mail_list_footer, null);
        else
            return mLayoutInflater.inflate(R.layout.wy_adapter_mail_list, null);
    }

    @Override
    public MailHeadViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new MailHeadViewHolder(realContentView,viewType == ITEM_TYPE_BOTTOM);
    }

    @Override
    public void onBindViewHolder(MailHeadViewHolder holder, final int position) {
        if(!holder.isFooter) {
            final MailDetail _md = getItem(position);
            holder.setData(_md, position);
        }
        holder.setOnItemClickListener(icListener);
    }

    private static MailDetail getItem(int position){
        if(position>-1 && position<dataSource.size())
            return dataSource.get(position);
        else {
            Log.i(TAG, "getItem失败 position="+ position);
            return null;
        }
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MailFilter();
        }
        return mFilter;
    }

    public void setFooterViewVisible(boolean showFooter) {
        this.showFooterView = showFooter;
    }

    //过滤数据
    class MailFilter extends Filter {
        //执行筛选
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                filterResults.values = dataSourceAll;
            } else {
                mFilteredArrayList.clear();
                for (MailDetail md : dataSource) {
                    String toStr = md.getTo();
                    String ccStr = md.getCc();
                    if (md.getSubject().contains(charSequence)||md.getFrom().contains(charSequence)||(toStr!=null&&toStr.contains(charSequence))||(ccStr!=null&&ccStr.contains(charSequence))) {
                        mFilteredArrayList.add(md);
                    }
                }
//                Log.i(TAG, "mFilteredArrayList.size() = " + mFilteredArrayList.size());
                filterResults.values = mFilteredArrayList;
            }
            return filterResults;
        }

        //筛选结果
        @Override
        protected void publishResults(CharSequence arg0, FilterResults results) {
            dataSource = (ArrayList<MailDetail>) results.values;
            notifyDataSetChanged();
        }
    }

    static class MailHeadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mailHeadAvatarIv;
        ImageView mailHeadAttachmentIv;
        ImageView mailHeadMarkedStarIv;
        ImageView mailHeadEncryptIv;
        ImageView mailHeadSignedIv;
        TextView mailHeadUnreadDotTv;
        TextView mailHeadSenderTv;
        TextView mailHeadSendTimeTv;
        TextView mailHeadSubjectTv;
        TextView mailHeadContentTv;
        ProgressBar sentProgressBar;
        boolean isFooter;
        ItemClickListener icListener;

        public MailHeadViewHolder(View itemView,boolean isFooter) {
            super(itemView);
            if(!isFooter) {
                mailHeadAvatarIv = (TextView) itemView.findViewById(R.id.mail_head_avatar);
                mailHeadAttachmentIv = (ImageView) itemView.findViewById(R.id.mail_head_attachment_icon);
                mailHeadMarkedStarIv = (ImageView) itemView.findViewById(R.id.mail_head_marked_star);
                mailHeadEncryptIv = (ImageView) itemView.findViewById(R.id.mail_head_encrypt_icon);
                mailHeadSignedIv = (ImageView) itemView.findViewById(R.id.mail_head_signed_icon);
                mailHeadUnreadDotTv = (TextView) itemView.findViewById(R.id.mail_head_unread_dot);
                mailHeadSenderTv = (TextView) itemView.findViewById(R.id.mail_head_sender);
                mailHeadSendTimeTv = (TextView) itemView.findViewById(R.id.mail_head_send_time);
                mailHeadSubjectTv = (TextView) itemView.findViewById(R.id.mail_head_subject);
                sentProgressBar = (ProgressBar) itemView.findViewById(R.id.sentProgressBar);
            }
            this.isFooter = isFooter;

            itemView.setOnClickListener(this);
        }

        public void setOnItemClickListener(ItemClickListener icListener) {
            this.icListener = icListener;
        }
        @Override
        public void onClick(View v) {
            if (icListener != null) {
                if(isFooter) {
                    final View progressBar = v.findViewById(R.id.load_more_progressBar);
                    final TextView tv = (TextView) v.findViewById(R.id.load_more_tv);
                    progressBar.setVisibility(View.VISIBLE);
                    tv.setText("正在加载邮件...");
                    tv.setTextColor(ResourcesUtil.getInstance().getColor(R.color.skyblue));
                    icListener.loadMoreMail();
                    progressBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            tv.setText("点击加载更多邮件");
                            tv.setTextColor(ResourcesUtil.getInstance().getColor(R.color.wy_common_text_light_color));
                        }
                    },2000);
                }
                else {
                    icListener.onMailClickCB(getItem(getAdapterPosition()));
                }
            }
        }

        public void setData(final MailDetail mailBean, int position) {
            Date now = new Date();
            Date sendDate;
            sendDate = mailBean.getCreateTime();
            String showTime;
            String senderText = "";
            //如果 未读 则显示未读红点
            mailHeadUnreadDotTv.setVisibility(mailBean.getIsRead() ? View.GONE : View.VISIBLE);
            //如果 未读 则显示标记五角星
            mailHeadMarkedStarIv.setVisibility(mailBean.getIsMarked() ? View.VISIBLE : View.GONE);
//        Integer etI = ;
            if(mailBean.getEncrypted()) mailHeadEncryptIv.setVisibility(View.VISIBLE);
            else mailHeadEncryptIv.setVisibility(View.GONE);

            if(mailBean.getSigned()) mailHeadSignedIv.setVisibility(View.VISIBLE);
            else mailHeadSignedIv.setVisibility(View.GONE);

            // 头像的点击事件
            mailHeadAvatarIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (icListener != null) icListener.onAvatarClick(mailBean);
                }
            });

            if (AppConfig.WY_CFG.DIR_ID_DRAFTBOX == mailBean.getDirectoryId() ||
                    AppConfig.WY_CFG.DIR_ID_SENT_MAIL == mailBean.getDirectoryId() ||
                    AppConfig.WY_CFG.DIR_ID_OUTBOX == mailBean.getDirectoryId()) {
                sendDate = mailBean.getUpdateTime();
                String toStr = mailBean.getTo();
                if (!TextUtils.isEmpty(mailBean.getTo())) {
                    try {
                        //                显示收件人
                        senderText = convertReceivers(toStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else senderText = "";
            } else {
//            显示发件人
                try {
                    JSONObject from = new JSONObject(mailBean.getFrom());
                    senderText = from.getString("name");
                    if (senderText.length() == 0) {
                        senderText = from.getString("id");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mailHeadSenderTv.setText(senderText);
            setAvatar(mailHeadAvatarIv, senderText);
//        如果有附件，则显示附件图标
            if(mailBean.getHasAttachment()){
                mailHeadAttachmentIv.setVisibility(View.VISIBLE);
            }else{
                mailHeadAttachmentIv.setVisibility(View.GONE);
            }
            if(mailBean.getDirectoryId() != AppConfig.WY_CFG.DIR_ID_OUTBOX) {
            //  加工邮件时间
                if (sendDate.getYear() == now.getYear()) {
                    if (sendDate.getMonth() == now.getMonth()) {
                        if (sendDate.getDate() == now.getDate()) {
                            int min = sendDate.getMinutes();
                            showTime = sendDate.getHours() + ":" + (min < 10 ? "0" + min : min);
                        } else {
                            showTime = (sendDate.getMonth() + 1) + "月" + sendDate.getDate() + "日";
                        }
                    } else {
                        showTime = (sendDate.getMonth() + 1) + "月" + sendDate.getDate() + "日";
                    }
                } else {
                    showTime = sendDate.getYear() + 1900 + "年" + (sendDate.getMonth() + 1) + "月" + sendDate.getDate() + "日";
                }
                mailHeadSendTimeTv.setText(showTime);
                sentProgressBar.setVisibility(View.GONE);
            }else{
                String sendStatusStr = "等待发送";
//                Log.i(TAG, "setData: mailBean.getSendStatus() = "+mailBean.getSendStatus());
                switch ((int)mailBean.getSendStatus()){
                    case 1:
                        sendStatusStr = "已发送";
                        break;
                    case 2:
                        sendStatusStr = "正在发送 "+mailBean.getSentPercentage()+"%";
                        break;
                    case 3:
                        sendStatusStr = "等待发送";
                        break;
                    case 4:
                        sendStatusStr = "发送失败";
                        break;
                }
                mailHeadSendTimeTv.setText(sendStatusStr);
                sentProgressBar.setVisibility(View.VISIBLE);
                sentProgressBar.setProgress((int) mailBean.getSentPercentage());
            }
//        设置邮件主题
            mailHeadSubjectTv.setText(mailBean.getSubject());
//        String summary = mailBean.getContent();
//        Log.i(TAG, "mail header summary: "+summary);
////                    加工要显示的邮件摘要
//        summary =;
//        summary = summary.replaceAll("<[^>]+>","").replaceAll("<(.|\n)*?>","");
//        summary = summary.replaceAll("(<style>).*?(?=<\\/style>)", "$1").replaceAll("&nbsp;", "");
//        mailHeadContentTv.setText( Html.fromHtml(summary.replaceAll("(<!-).*?(?=-->)", "$1")).toString());
        }

        public String convertReceivers(String arrString) throws JSONException {
            JSONArray toArr = new JSONArray(arrString);
            int toArrLength = toArr.length();
            String toNames = "";
            for (int i = 0; i < toArrLength; i++) {
                JSONObject jobj = (JSONObject) toArr.get(i);
                if (jobj.getString("name").length() > 0) {
                    toNames += jobj.getString("name") + ",";
                } else {
                    toNames += jobj.getString("email") + ",";
                }
            }
//        Log.i(TAG, "convertReceivers---" + toNames);
            return toNames.substring(0, toNames.length() - 1);
        }

        private void setAvatar(TextView avatarTV, String name) {
            if (name.length() > 0) {
                if(name.endsWith(")")&&name.length()>1){
                    name = name.substring(name.length() - 2,name.length() - 1);
                }else{
                    name = name.substring(name.length() - 1);
                }
                int nameHash = name.hashCode() % 6;
                int bgId = R.drawable.wy_avatar_bg_6;
                switch (nameHash) {
                    case 1:
                        bgId = R.drawable.wy_avatar_bg_1;
                        break;
                    case 2:
                        bgId = R.drawable.wy_avatar_bg_2;
                        break;
                    case 3:
                        bgId = R.drawable.wy_avatar_bg_3;
                        break;
                    case 4:
                        bgId = R.drawable.wy_avatar_bg_4;
                        break;
                    case 5:
                        bgId = R.drawable.wy_avatar_bg_5;
                        break;
                }
                avatarTV.setText(name);
                avatarTV.setBackgroundResource(bgId);
            } else {
                avatarTV.setText("空");
                avatarTV.setBackgroundResource(R.drawable.wy_avatar_bg_6);
            }
        }
    }

    public void setIcListener(ItemClickListener icListener) {
        this.icListener = icListener;
    }

    public interface ItemClickListener {

        void onAvatarClick(MailDetail md);

        void onMailClickCB(MailDetail md);

        void loadMoreMail();
    }

}