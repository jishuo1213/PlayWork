package com.inspur.playwork.view.application.addressbook;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.SearchPersonInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fan on 17-1-8.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private static final String TAG = "SearchResultAdapter";

    private ArrayList<SearchPersonInfo> resultList;

    private RecyclerView recyclerView;

    private Set<String> viewedPerson;


    private ItemEventListener listener;

    interface ItemEventListener {
        void onItemClick(String id);

        void onMobilePhoneClick(String phoneNum);

        void onTelClick(String telNum);

        void onSendMsgClick(String email);

        void onSendEmailClick(String email,String name);
    }

    SearchResultAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        viewedPerson = new HashSet<>();

    }

    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchPersonInfo info = getItem(position);
        holder.departname.setText(info.orginfo);
        holder.email.setText("邮箱：" + info.email);
        holder.name.setText(info.name);
        holder.telphone.setText("电话：" + info.tel);
        holder.telphone.setOnClickListener(telClickListener);
        if (!viewedPerson.contains(info.email))
            holder.mobile.setText("点击查看手机");
        else
            holder.mobile.setText("手机：" + info.mobile);
        holder.mobile.setOnClickListener(mobileClickListener);
        holder.infoView.setOnClickListener(itemClickListener);
        holder.sendChatView.setOnClickListener(sendChatListener);
        holder.email.setOnClickListener(sendEmailListener);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    private SearchPersonInfo getItem(int pos) {
        return resultList.get(pos);
    }

    void setResultList(ArrayList<SearchPersonInfo> resultList) {
        this.resultList = resultList;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        TextView departname;
        public TextView email;
        TextView mobile;
        TextView telphone;
        private View infoView;
        private View sendChatView;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            departname = (TextView) itemView.findViewById(R.id.tv_person_department);
            email = (TextView) itemView.findViewById(R.id.tv_person_mail);
            mobile = (TextView) itemView.findViewById(R.id.tv_person_phone);
            telphone = (TextView) itemView.findViewById(R.id.tv_person_department_num);
            infoView = itemView.findViewById(R.id.lin_preson_info);
            sendChatView = itemView.findViewById(R.id.send_chat);
        }
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent());
            SearchPersonInfo info = getItem(pos);
            if (viewedPerson.contains(info.email)) {
                return;
            }
            viewedPerson.add(info.email);
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(pos);
            viewHolder.mobile.setText("手机：" + info.mobile);
            if (listener != null) {
                listener.onItemClick(info.email);
            }
        }
    };

    private View.OnClickListener mobileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
            SearchPersonInfo info = getItem(pos);
            Log.i(TAG, "onClick: " + info.toString());
            if (viewedPerson.contains(info.email)) {
                if (listener != null) {
                    listener.onMobilePhoneClick(info.mobile);
                }
            } else {
                viewedPerson.add(info.email);
                ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(pos);
                viewHolder.mobile.setText("手机：" + info.mobile);
                if (listener != null) {
                    listener.onItemClick(info.email);
                    listener.onMobilePhoneClick(info.mobile);
                }
            }
        }
    };

    private View.OnClickListener telClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
            SearchPersonInfo info = getItem(pos);
            Log.i(TAG, "onClick: " + info.toString());
            if (listener != null) {
                listener.onTelClick(info.tel);
            }
        }
    };

    private View.OnClickListener sendChatListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent());
            SearchPersonInfo info = getItem(pos);
            if (listener != null) {
                listener.onSendMsgClick(info.email);
            }
        }
    };

    private View.OnClickListener sendEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
            SearchPersonInfo info = getItem(pos);
            if (listener != null) {
                listener.onSendEmailClick(info.email,info.name);
            }
        }
    };
}
