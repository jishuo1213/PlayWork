package com.inspur.playwork.view.profile.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;

/**
 * Created by bugcode on 15-8-13.
 */
public class SetNotifyFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SetNotifyFragment" ;
    private ImageButton backImageView;
    private TextView titleTextView;
    private ImageView messageNotifyImageView;
/*    private ImageView notDisturbImageView;
    private LinearLayout setTimeLinearLayout;*/
    private ImageView showDetailImageView;
    private ImageView ringImageView;
    private ImageView shakeImageView;
    private View rootView ;
    private PreferencesHelper pfch ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pfch = PreferencesHelper.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.activity_set_notify,container,false);
        initViews(rootView);
        return rootView ;
    }

    private void initViews(View view) {
        backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        titleTextView = (TextView) view.findViewById(R.id.tv_title);
        messageNotifyImageView = (ImageView) view.findViewById(R.id.iv_message_notify);
        /*notDisturbImageView = (ImageView) findViewById(R.id.iv_not_disturb);
        setTimeLinearLayout = (LinearLayout) findViewById(R.id.ll_set_time);
        startDateTime = (TextView) findViewById(R.id.startDateTime);
        endDateTime = (TextView) findViewById(R.id.endDateTime);*/
        showDetailImageView = (ImageView) view.findViewById(R.id.iv_show_detail);
        ringImageView = (ImageView) view.findViewById(R.id.iv_ring);
        shakeImageView = (ImageView) view.findViewById(R.id.iv_shake);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("消息通知");

        /*接收消息*/
        String isMessageNotify = pfch.readStringPreference("isMessageNotify") ;
        if("1".equals(isMessageNotify)){
            messageNotifyImageView.setSelected(true);
        }else{
            messageNotifyImageView.setSelected(false);
        }
        messageNotifyImageView.setOnClickListener(this);

        /*忽扰设置*/
/*        String isNotD  = pfch.readStringPreference("isNotDisturb");
        if("1".equals(isNotD)){
            notDisturbImageView.setSelected(true);
            setTimeLinearLayout.setVisibility(View.GONE);
        }else{
            notDisturbImageView.setSelected(false);
            setTimeLinearLayout.setVisibility(View.VISIBLE);
        }
        notDisturbImageView.setOnClickListener(this);
        startDateTime.setOnClickListener(this);
        endDateTime.setOnClickListener(this);*/

        /*详情展现*/
        String isShowDetail = pfch.readStringPreference("isShowDetail");
        if("1".equals(isShowDetail)){
            showDetailImageView.setSelected(true);
        }else{
            showDetailImageView.setSelected(false);
        }
        showDetailImageView.setOnClickListener(this);

        /*消息响铃铃声设置*/
        String isRing = pfch.readStringPreference("isRing");
        if("1".equals(isRing)){
            ringImageView.setSelected(true);
        }else{
            ringImageView.setSelected(false);
        }
        ringImageView.setOnClickListener(this);

        /*消息震动设置*/
        String isShake = pfch.readStringPreference("isShake");

        if("1".equals(isShake)){
            shakeImageView.setSelected(true);
        }else{
            shakeImageView.setSelected(false);
        }
        shakeImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_message_notify:
                boolean isMessageNotify = messageNotifyImageView.isSelected() ;
                if(isMessageNotify){
                    pfch.writeToPreferences("isMessageNotify","2");
                }else{
                    pfch.writeToPreferences("isMessageNotify","1");
                }
                messageNotifyImageView.setSelected(!isMessageNotify);
                break;
            /*case R.id.iv_not_disturb:
                boolean notDisturb = notDisturbImageView.isSelected() ;

                if (notDisturb) {
                    setTimeLinearLayout.setVisibility(View.VISIBLE);
                    pfch.writeToPreferences("isNotDisturb", "2");
                } else {
                    setTimeLinearLayout.setVisibility(View.GONE);
                    pfch.writeToPreferences("isNotDisturb", "1");
                }
                notDisturbImageView.setSelected(!notDisturb);
                break;*/
            case R.id.iv_show_detail:
                // 显示详情
                boolean isShow = showDetailImageView.isSelected() ;
                if(!isShow){
                    pfch.writeToPreferences("isShowDetail", "1");
                }else{
                    pfch.writeToPreferences("isShowDetail", "2");
                }
                showDetailImageView.setSelected(!isShow);
                break;
            case R.id.iv_ring:
                // 声音
                boolean isRing = ringImageView.isSelected();
                ringImageView.setSelected(!isRing);
                if(!isRing){
                    DeviceUtil.beep(getActivity(),1);
                    pfch.writeToPreferences("isRing", "1");
                }else{
                    pfch.writeToPreferences("isRing", "2");
                }
                break;
            case R.id.iv_shake:
                // 振动
                boolean isShake = shakeImageView.isSelected();
                if(isShake==false){
                    DeviceUtil.vibrate(getActivity(),500);
                    pfch.writeToPreferences("isShake", "1");
                } else {
                    pfch.writeToPreferences("isShake", "2");
                }
                shakeImageView.setSelected(!isShake);
                break;
            case R.id.iv_left:
                CommonUtils.back();
                break;
          /*  case R.id.startDateTime:
                Log.i(TAG, "开始时间");
                break;
            case R.id.endDateTime:
                Log.i(TAG, "结束时间");
                break;*/
        }
    }
}
