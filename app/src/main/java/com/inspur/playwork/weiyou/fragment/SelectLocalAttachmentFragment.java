package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.inspur.playwork.R;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.weiyou.WriteMailActivity;
import com.inspur.playwork.weiyou.adapter.MailAttachmentAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Fan on 15-9-18.
 */
public class SelectLocalAttachmentFragment extends Fragment{

    private static final String TAG = "MailAttachmentFragment";

    private ListView mailAttachmentListView;
    private MailAttachmentAdapter maAdapter;
    private ArrayList<MailAttachment> mailAttachmentListData;
    private ArrayList<MailAttachment> selectedAttachmentList;
    private WriteMailActivity wyma;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WriteMailActivity)getActivity();
        mailAttachmentListData= new ArrayList<>();
        selectedAttachmentList= new ArrayList<>();
//        DBUtil.queryMailAttachmentList(wyma.currMailAccount.getEmail());File file = new File(getMailCachePath());
        String email = wyma.vuStores.currEmail;
        File file = new File(FileUtil.getCurrMailAttachmentsPath(email));
        File[] files = file.listFiles();
        if (files != null && files.length >= 0) {
            for (File _file : files) {
//                Long id, String name, String path, String url, Long size, String email, java.util.Date createTime, long mailId
                MailAttachment ma = new MailAttachment(null,_file.getName(),_file.getAbsolutePath(),null,_file.length(),email,new Date(_file.lastModified()),0l);
                mailAttachmentListData.add(ma);
            }
        }
        Collections.sort(mailAttachmentListData);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.wy_fragment_attachment_list, container, false);
//        return rootView;
        rootView.findViewById(R.id.wy_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wyma.onBackPressed();
            }
        });

        rootView.findViewById(R.id.select_attachment_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ofsl!=null){
                    ofsl.onFinishSelect(selectedAttachmentList);
                    wyma.onBackPressed();
                }
            }
        });
        mailAttachmentListView = (ListView) rootView.findViewById(R.id.mal_lv);
        maAdapter = new MailAttachmentAdapter(wyma,mailAttachmentListData,false,true);
        mailAttachmentListView.setAdapter(maAdapter);
        mailAttachmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MailAttachment ma = mailAttachmentListData.get(i);
                Log.i(TAG, "mailAttachmentListView onItemClick: "+ma.getName());
                CheckBox cb = (CheckBox)view.findViewById(R.id.wy_attachment_checkbox);
                boolean isChecked = !cb.isChecked();
                if(isChecked){
                    selectedAttachmentList.add(ma);
                }else{
                    selectedAttachmentList.remove(ma);
                }
                cb.setChecked(isChecked);
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Dispatcher.getInstance().unRegister(this);
    }
//
//    public void queryMAListCallback(ArrayList<MailAttachment> maList) {
////        this.mailAttachmentListData = maList;
//    }


    private SelectLocalAttachmentListener ofsl;
    public interface SelectLocalAttachmentListener{
        void onFinishSelect(List<MailAttachment> selectedAttacmentList);
    }
    public void setOnFinishSelectListener(SelectLocalAttachmentListener rbListener){
        this.ofsl = rbListener;
    }
}
