package com.inspur.playwork.view.profile.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;

/**
 * Created by bugcode on 15-8-13.
 */
public class SetCommonFragment extends Fragment implements View.OnClickListener {

    private ImageButton backImageView;
    private TextView titleTextView;
    private ImageView earModeImageView;

    // 清空图片缓存
    private TextView clearImgTextView ;
    private TextView clearChatTextView ;

    private View rootView ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_set_common);
//        initViews();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.activity_set_common,container,false);
        initViews(rootView);
        return rootView ;
    }

    private void initViews(View view) {
        backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        titleTextView = (TextView) view.findViewById(R.id.tv_title);
        earModeImageView = (ImageView) view.findViewById(R.id.iv_ear_mode);

        clearImgTextView = (TextView) view.findViewById(R.id.tv_clear_cache);
        clearChatTextView = (TextView) view.findViewById(R.id.tv_clear_record);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("通用");
        earModeImageView.setOnClickListener(this);
        if("1".equals(PreferencesHelper.getInstance().readStringPreference("isEarMode"))){
            earModeImageView.setSelected(true);
        }else{
            earModeImageView.setSelected(false);
        }
        clearImgTextView.setOnClickListener(this);
        clearChatTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                CommonUtils.back();
                break;
            case R.id.iv_ear_mode:
                boolean iv_ear_mode = earModeImageView.isSelected();

                if(iv_ear_mode){
                    PreferencesHelper.getInstance().writeToPreferences("isEarMode","2");
                }else{
                    PreferencesHelper.getInstance().writeToPreferences("isEarMode","1");
                }
                earModeImageView.setSelected(!iv_ear_mode);
                UItoolKit.showToastShort(getActivity(), "设置成功");
                break;
            case R.id.tv_clear_cache:
                String imgCachePath = FileUtil.getSDCardRoot() + AppConfig.AVATAR_DIR;
//                删除图片文件
//                FileUtil.deleteFile(imgCachePath);
                UItoolKit.showToastShort(getActivity(), "图片缓存清理成功");
                break;
            case R.id.tv_clear_record:
                UItoolKit.showToastShort(getActivity(), "聊天记录缓存清理成功");
                break ;
        }
    }
}
