package com.inspur.playwork.view.profile.my;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.view.common.GuideActivity;
import com.inspur.playwork.view.profile.setting.SettingActivity;

import java.io.File;

/**
 * 我fragment
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    private static final String TAG = "ProfileFragmentFan";

    private ImageView myAvatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        RelativeLayout mReMyInfo = (RelativeLayout) view.findViewById(R.id.re_my_info);
        RelativeLayout mReSettings = (RelativeLayout) view.findViewById(R.id.re_settings);
        RelativeLayout help = (RelativeLayout) view.findViewById(R.id.re_help);
        view.findViewById(R.id.re_recommend).setOnClickListener(this);
        view.findViewById(R.id.re_login_web).setOnClickListener(this);


        UserInfoBean user = PreferencesHelper.getInstance().getCurrentUser();
        TextView myName = (TextView) view.findViewById(R.id.tv_topic);
        TextView myDept = (TextView) view.findViewById(R.id.tv_department);
        TextView logoutTextView = (TextView) view.findViewById(R.id.tv_logout);
        myAvatar = (ImageView) view.findViewById(R.id.iv_vchat_avatar);

        if (user.id.equals("zhenghao")) {
            if (Build.VERSION.SDK_INT >= 23)
                myName.setTextColor(getResources().getColor(R.color.red, getActivity().getTheme()));
            else
                myName.setTextColor(getResources().getColor(R.color.red));

        }
        myName.setText(user.name);
        if (TextUtils.isEmpty(user.subDepartment))
            myDept.setText(user.department);
        else
            myDept.setText(user.subDepartment);

//        ArrayMap<String, Long> avatars = ((PlayWorkApplication) getActivity().getApplication()).getAvatars();

//        File file ;
//
//
//        if (avatars.containsKey(user.getUserId())) {
//            file = FileUtil.getFile(AppConfig.AVATAR_DIR, user.getUserId() + "-" + avatars.get(user.getUserId()) + ".png");
//        } else {
//            file = FileUtil.getFile(AppConfig.AVATAR_DIR, user.getUserId() + "-" + user.getUserId() + ".png");
//        }
//
//        Log.i(TAG, "initViews: " + avatars.containsKey(user.getUserId()));


//        myAvatar.setImageURI(uri);
        Glide.with(getActivity()).load(new File(PreferencesHelper.getInstance().getCurrentUser().getAvatarPath())).placeholder(R.drawable.icon_chat_default_avatar).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                into(myAvatar);

        mReMyInfo.setOnClickListener(this);
        mReSettings.setOnClickListener(this);
        help.setOnClickListener(this);
        logoutTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_my_info:
                startActivityForResult(new Intent(getActivity(), MyInfoActivity.class), 100);
                break;
            case R.id.re_settings:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.tv_logout:

                PreferencesHelper.getInstance()
                        .writeToPreferences2Exit(PreferencesHelper.HAVE_LOGIN_TIME_LINE, false);
                PreferencesHelper.getInstance()
                        .writeToPreferences2Exit(PreferencesHelper.HAVE_LOGIN_AD_SERVER, false);
                ((MainActivity) getActivity()).setUserLogOut(true);
                getActivity().finish();
                break;
            case R.id.re_recommend:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, AppConfig.SHARE_MESSAGE_TO_OTHERS +
                        PreferencesHelper.getInstance().getCurrentUser().id);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.re_login_web:
                Dialog dialog = new Dialog(getActivity(), R.style.normal_dialog);
                dialog.setContentView(R.layout.two_code_dialog);
                dialog.show();
                break;
            case R.id.re_help:
                Intent guideIntent = new Intent(getActivity(), GuideActivity.class);
                guideIntent.putExtra(GuideActivity.USER_OPEN, true);
                startActivity(guideIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Uri imgUri = data.getExtras().getParcelable("NewAvatar");
            myAvatar.setImageURI(imgUri);
        }
    }
}
