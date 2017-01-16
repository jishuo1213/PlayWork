package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.inspur.playwork.R;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailAccountListAdapter;
import com.inspur.playwork.weiyou.store.VUSettingsOperation;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;

/**
 * Created by 孙 on 2015/12/2 0002.
 */
public class VUSettingsFragment extends Fragment implements View.OnClickListener,VUSettingsOperation{
    private static final String TAG = "VUSettingsFragment-->";
    private WeiYouMainActivity wyma;

    private LinearLayout addNewAccountLL;
//    private RelativeLayout sendFeedbackLL;

    private ListView mailAccountListLv;

    public MailAccountListAdapter mlAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setVUSettingsReference(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wy_fragment_mail_settings, container, false);
        mailAccountListLv = (ListView) view.findViewById(R.id.wy_setting_account_lv);
        mailAccountListLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                wyma.vuStores.openMailAccountSettings(i);
            }
        });
//        初始化listView
        if(mailAccountListLv.getAdapter() == null) {
            mlAdapter = new MailAccountListAdapter(wyma, wyma.vuStores.getMailAccountList());
            mailAccountListLv.setAdapter(mlAdapter);
        }
        WeiYouUtil.setListViewHeightBasedOnChildren(mailAccountListLv);
        addNewAccountLL = (LinearLayout)view.findViewById(R.id.wy_add_new_account_btn);
        view.findViewById(R.id.wy_back_btn).setOnClickListener(this);//返回按钮
        addNewAccountLL.setOnClickListener(this);
//        sendFeedbackLL = (RelativeLayout)view.findViewById(R.id.wy_setting_feedback);
//        sendFeedbackLL.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.wy_back_btn:
                wyma.onBackPressed();
                break;
//            case R.id.wy_setting_feedback:
//                wyma.gotoWriteFeedback();
//                break;
            case R.id.wy_add_new_account_btn:
                wyma.gotoAddNewAccount();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mlAdapter.notifyDataSetChanged();
    }
}

