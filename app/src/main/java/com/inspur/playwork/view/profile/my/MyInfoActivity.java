package com.inspur.playwork.view.profile.my;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.actions.common.CommonActions;
import com.inspur.playwork.actions.network.NetWorkActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.chosepicture.ChosePictureFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by bugcode on 15-8-11.
 */
public class MyInfoActivity extends BaseActivity implements MyInfoFragment.InfoEventListener,
        ChosePictureFragment.SelectedPicureListener, ClipPictureFragment.ClipListener, View.OnClickListener {

    private static final String TAG = "MyInfoActivity";

    private ImageButton mBackImageButton;
    private TextView mTitleTextView;

    private Fragment myInfoFragment;

    private Fragment chosePictureFragment;

    private UserInfoBean currentUser;

    private ProgressDialog progressDialog;

    private String imgPath;

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;

    private Handler handler;

    private Bitmap newAvatarBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_my_info);
        mBackImageButton = (ImageButton) findViewById(R.id.iv_left);
        mBackImageButton.setVisibility(View.VISIBLE);
        mBackImageButton.setOnClickListener(this);
        mTitleTextView = (TextView) findViewById(R.id.tv_title);
        mTitleTextView.setText("个人信息");
        currentUser = PreferencesHelper.getInstance().getCurrentUser();
        myInfoFragment = new MyInfoFragment();
        handler = new InfoActivityHandler(new WeakReference<>(this));
        getFragmentManager().beginTransaction().add(R.id.my_info_fragment_container, myInfoFragment, MyInfoFragment.TAG).commit();
    }

    @Override
    public void showChosePictureFragment() {
        if (chosePictureFragment == null) {
            chosePictureFragment = ChosePictureFragment.getInstance("确定", false);
        }
        getFragmentManager().beginTransaction().add(R.id.my_info_fragment_container, chosePictureFragment, ChosePictureFragment.TAG).commit();
    }

    @Override
    public void showPhotoPictureFragment() {
        imgPath = FileUtil.getImageFilePath() + System.currentTimeMillis() + ".png";
        startActivityForResult(CommonUtils.getTakePhoteIntent(this, imgPath), PHOTO_REQUEST_TAKEPHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPhotoPictureFragment();
                } else {
                    UItoolKit.showToastShort(this, "请提供相机权限");
                }
                break;
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showChosePictureFragment();
                } else {
                    UItoolKit.showToastShort(this, "请提供读取文件权限");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_TAKEPHOTO) {
            File file = new File(imgPath);
            if (file.exists()) {
                onPictureSelect(imgPath);
            }
        }
    }

    @Override
    public void onPictureSelect(String path) {
        Fragment clipPictureFragment = ClipPictureFragment.getInstance(path);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.my_info_fragment_container, clipPictureFragment, ClipPictureFragment.TAG);
        Fragment chosePic = getFragmentManager().findFragmentByTag(ChosePictureFragment.TAG);
        if (chosePic != null)
            ft.remove(chosePic);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment clipFragment = getFragmentManager().findFragmentByTag(ClipPictureFragment.TAG);
        if (clipFragment != null) {
            getFragmentManager().beginTransaction().remove(clipFragment).commit();
            if (!TextUtils.isEmpty(imgPath)) {
                File file = new File(imgPath);
                if (file.exists()) {
                    file.delete();
                    imgPath = "";
                }
            }
            return;
        }

        Fragment fragment = getFragmentManager().findFragmentByTag(ChosePictureFragment.TAG);
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("NewAvatar", Uri.fromFile(new File(currentUser.getAvatarPath())));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void dismissClipFragment() {
        Fragment fragment = getFragmentManager().findFragmentByTag(ClipPictureFragment.TAG);
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onClipClick(final Bitmap bitmap) {
        newAvatarBitmap = bitmap;
        final String filePah = FileUtil.getAvatarFilePath() + System.currentTimeMillis() + currentUser.id + ".png";
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                PictureUtils.saveBitmapToFile(bitmap, filePah);
                handler.sendMessage(handler.obtainMessage(2, filePah));
                if (!TextUtils.isEmpty(imgPath)) {
                    File file = new File(imgPath);
                    if (file.exists()) {
                        file.delete();
                        imgPath = "";
                    }
                }
            }
        });
    }


    private void upLoadImg(final String filePath) {
        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                new OkHttpClientManager.Param("user_id", currentUser.id),
                new OkHttpClientManager.Param("system_name", "avatar"),
        };


        Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.UPLOAD_FILE_BY_HTTP_POST, new File(filePath), params,
                new Callback() {
                    @Override
                    public void onFailure(Call request, IOException e) {

//                        Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.UPLOAD_IMG_FAILED);
                        handler.sendEmptyMessage(3);
                        File file = new File(filePath);
                        file.delete();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                long docId = jsonObject.optLong("docid");
                                FileUtil.renameFile(filePath, currentUser.id + "-" + docId);
                                String changeAvatar_uri = AppConfig.UPDATE_AVATAR_PORTRAIT + "avatar=" + docId
                                        + "&userid=" + currentUser.id;
                                updateMyAvatar(changeAvatar_uri, docId);
                                Dispatcher.getInstance().dispatchNetWorkAction(NetWorkActions.USER_AVATAR_DOWNLOADED, currentUser.id, docId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, null);
    }


    private void updateMyAvatar(String uri, final long docid) {

        OkHttpClientManager.getInstance().getAsyn(uri, new Callback() {
            @Override
            public void onFailure(Call request, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Log.i(TAG, response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(handler.obtainMessage(1, docid));
            }
        });
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(null);
            progressDialog.setProgressStyle(ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setMessage("正在上传头像");
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                Intent intent = new Intent();
                intent.putExtra("NewAvatar", Uri.fromFile(new File(currentUser.getAvatarPath())));
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }

    private static class InfoActivityHandler extends Handler {

        private WeakReference<MyInfoActivity> reference;

        public InfoActivityHandler(WeakReference<MyInfoActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    reference.get().upLoadAvatarSuccess((long) msg.obj);
                    break;
                case 2:
                    reference.get().upLoadImg((String) msg.obj);
                    break;
                case 3:
                    reference.get().uploadImgFailed();
                    break;
            }
        }
    }

    private void uploadImgFailed() {
        dismissProgressDialog();
        UItoolKit.showToastShort(this, "上传头像失败，可能是网络太慢了");
    }

    private void upLoadAvatarSuccess(long docid) {
        currentUser.avatar = docid;
        UItoolKit.showToastShort(this, "上传头像成功");
        dismissProgressDialog();
        dismissClipFragment();
        ((MyInfoFragment) myInfoFragment).updateAvatar(newAvatarBitmap, docid);
    }
}
