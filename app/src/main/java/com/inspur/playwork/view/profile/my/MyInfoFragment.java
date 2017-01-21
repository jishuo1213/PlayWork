package com.inspur.playwork.view.profile.my;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;

import java.io.File;


/**
 * Created by Fan on 15-11-18.
 */
public class MyInfoFragment extends Fragment implements View.OnClickListener {


    public static final String TAG = "MyInfoFragment";

    private ImageView avatarImageView;

    private AlertDialog alertDialog;

    private InfoEventListener eventListener;

    private View rootView;

    public interface InfoEventListener {
        void showChosePictureFragment();

        void showPhotoPictureFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        eventListener = (InfoEventListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.activity_my_info, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        eventListener = null;
    }

    private void initView(View v) {
        UserInfoBean userInfoJsonObject = PreferencesHelper.getInstance().getCurrentUser();
        avatarImageView = (ImageView) v.findViewById(R.id.iv_head);
        TextView p_nameTextView = (TextView) v.findViewById(R.id.p_name);
        TextView p_companyTextView = (TextView) v.findViewById(R.id.p_company);
        TextView p_deptTextView = (TextView) v.findViewById(R.id.p_dept);
        TextView p_codeTextView = (TextView) v.findViewById(R.id.p_code);
        TextView p_phoneTextView = (TextView) v.findViewById(R.id.p_phone);
        TextView p_emailTextView = (TextView) v.findViewById(R.id.p_email);
        TextView p_addressTextView = (TextView) v.findViewById(R.id.p_address);

        View headRelativeLayout = v.findViewById(R.id.re_head);
        View nameRelativeLayout = v.findViewById(R.id.re_name);
        View telephoneRelativeLayout = v.findViewById(R.id.re_telephone);
        View mailRelativeLayout = v.findViewById(R.id.re_mail);
        View addressRelativeLayout = v.findViewById(R.id.re_address);

        // Uri uri = Uri.fromFile(FileUtil.getFile(AppConfig.AVATAR_DIR, userInfoJsonObject.getAvatar() + ".png"));
//        avatarImageView.setImageBitmap(PictureUtils.getAvatar(avatarImageView.getContext(), PreferencesHelper.getInstance().getCurrentUser().getAvatarPath()));
        Glide.with(getActivity()).load(new File(PreferencesHelper.getInstance().getCurrentUser().getAvatarPath()))
                .placeholder(R.drawable.icon_chat_default_avatar).
                diskCacheStrategy(DiskCacheStrategy.NONE).into(avatarImageView);
        p_nameTextView.setText(userInfoJsonObject.name);
        p_companyTextView.setText(userInfoJsonObject.company);
        p_deptTextView.setText(userInfoJsonObject.department);
        p_codeTextView.setText(userInfoJsonObject.uid.substring(1));
        p_phoneTextView.setText("");
        p_emailTextView.setText(userInfoJsonObject.id + AppConfig.EMAIL_SUFFIX);
        p_addressTextView.setText("");
        headRelativeLayout.setOnClickListener(this);
        nameRelativeLayout.setOnClickListener(this);
        telephoneRelativeLayout.setOnClickListener(this);
        mailRelativeLayout.setOnClickListener(this);
        addressRelativeLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_head:
                showPhotoDialog();
                break;
            case R.id.re_name:
                break;
            case R.id.re_telephone:
                break;
            case R.id.re_mail:
                break;
            case R.id.re_address:
                break;
            case R.id.tv_content1:
                alertDialog.dismiss();
                if (DeviceUtil.getPermission(getActivity(), Manifest.permission.CAMERA, 100))
                    eventListener.showPhotoPictureFragment();
                break;
            case R.id.tv_content2:
                alertDialog.dismiss();
                if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 101))
                    eventListener.showChosePictureFragment();
                break;
        }
    }

    public void updateAvatar(Bitmap bitmap, long avatarf) {
        Glide.with(getActivity()).load(new File(PreferencesHelper.getInstance().getCurrentUser().getAvatarPath()))
                .placeholder(R.drawable.icon_chat_default_avatar).
                diskCacheStrategy(DiskCacheStrategy.NONE).into(avatarImageView);
    }

    private void showPhotoDialog() {
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.chose_picture_dialog);
        TextView paizhaoTextView = (TextView) window.findViewById(R.id.tv_content1);
        paizhaoTextView.setOnClickListener(this);

        TextView xiangceTextView = (TextView) window.findViewById(R.id.tv_content2);
        xiangceTextView.setOnClickListener(this);
    }
}
