package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailAttachmentAdapter;
import com.inspur.playwork.weiyou.store.MailAttachmentOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Fan on 15-9-18.
 */
public class MailAttachmentFragment extends Fragment implements MailAttachmentOperation {

    private static final String TAG = "MailAttachmentFragment";

    private ListView mailAttachmentListView;
    private MailAttachmentAdapter maAdapter;
    private ArrayList<MailAttachment> mailAttachmentListData;
    private WeiYouMainActivity wyma;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity) getActivity();
        wyma.vuStores.setMailAttachmentReference(this);
        mailAttachmentListData = new ArrayList<>();
//        DBUtil.queryMailAttachmentList(wyma.currMailAccount.getEmail());File file = new File(getMailCachePath());
        String email = wyma.currMailAccount.getEmail();
        File file = new File(FileUtil.getCurrMailAttachmentsPath(email));
        File[] files = file.listFiles();
        if (files != null && files.length >= 0) {
            for (File _file : files) {
//                Long id, String name, String path, String url, Long size, String email, java.util.Date createTime, long mailId
                MailAttachment ma = new MailAttachment(null, _file.getName(), _file.getAbsolutePath(), null, _file.length(), email, new Date(_file.lastModified()), 0l);
                mailAttachmentListData.add(ma);
            }
        }
        Collections.sort(mailAttachmentListData);
//        关闭侧边栏手势滑动
        if (wyma.drawer != null)
            wyma.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
        mailAttachmentListView = (ListView) rootView.findViewById(R.id.mal_lv);
        maAdapter = new MailAttachmentAdapter(wyma, mailAttachmentListData, false, false);
        mailAttachmentListView.setAdapter(maAdapter);
        mailAttachmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MailAttachment ma = mailAttachmentListData.get(i);
                String amPath = ma.getPath();
                getActivity().startActivity(FileUtil.getOpenFileIntent(getActivity(), amPath));
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        wyma.vuStores.setMailAttachmentReference(null);
        super.onDestroy();
    }

    public void queryMAListCallback(ArrayList<MailAttachment> maList) {
//        this.mailAttachmentListData = maList;
    }
}
