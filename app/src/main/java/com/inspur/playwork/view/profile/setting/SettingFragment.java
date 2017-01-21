package com.inspur.playwork.view.profile.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.BuildConfig;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.VersionInfoBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.ProgressResponseListener;
import com.inspur.playwork.versionUpdate.VersionPlaywork;
import com.inspur.playwork.view.profile.my.AboutFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jianggf on 2015/11/26.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "SettingFragment";
    private View rootView;
    private boolean isCanCheckVersion = true;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.activity_setting, container, false);
        initView(rootView);
        return rootView;
    }

    /**
     * 初始化View组件
     */
    private void initView(View view) {
        ImageButton backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        TextView titleTextView = (TextView) view.findViewById(R.id.tv_title);
//        TextView setAccountTextView = (TextView) view.findViewById(R.id.tv_set_account);
        TextView messageNotifyTextView = (TextView) view.findViewById(R.id.tv_message_notify);
        TextView commonSetTextView = (TextView) view.findViewById(R.id.tv_common_set);
        TextView appSetTextView = (TextView) view.findViewById(R.id.tv_app_set);
        TextView versionUpdateTextView = (TextView) view.findViewById(R.id.tv_version_update);
        TextView aboutTextView = (TextView) view.findViewById(R.id.tv_about);

        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        titleTextView.setText("设置");
//        setAccountTextView.setOnClickListener(this);
        messageNotifyTextView.setOnClickListener(this);
        commonSetTextView.setOnClickListener(this);
        appSetTextView.setOnClickListener(this);
        versionUpdateTextView.setOnClickListener(this);
        aboutTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.animator.fragment_xfraction_in,
                R.animator.fragment_xfraction_out,
                R.animator.fragment_xfraction_pop_in,
                R.animator.fragment_xfraction_pop_out);

        switch (v.getId()) {
            case R.id.tv_set_account:
                ft.replace(R.id.setting_fragment_container, new SetAccountFragment()).addToBackStack(null).commit();
                break;
            case R.id.tv_message_notify:
                ft.replace(R.id.setting_fragment_container, new SetNotifyFragment()).addToBackStack(null).commit();
                break;
            case R.id.tv_common_set:
                ft.replace(R.id.setting_fragment_container, new SetCommonFragment()).addToBackStack(null).commit();
                break;
            case R.id.iv_left:
                CommonUtils.back();
                break;
            case R.id.tv_version_update:
                if (isCanCheckVersion) {
                    UItoolKit.showToastShort(getActivity(), "正在检查更新");
                    checkUpVersion();
                }
                break;
            case R.id.tv_about:
                ft.replace(R.id.setting_fragment_container, new AboutFragment()).addToBackStack(null).commit();
                break;
            default:
                break;
        }
    }

    /*
    * 处理手动检测更新的方法
    * */
    public void checkUpVersion() {
        isCanCheckVersion = false;
        OkHttpClientManager.getInstance().getAsyn(AppConfig.CHECK_NEW_VERSION + "userId=" + PreferencesHelper.getInstance().getCurrentUser().id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isCanCheckVersion = true;
                        UItoolKit.showToastShort(getActivity(), "检查更新失败，可能是网络问题");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        final VersionInfoBean newBean = new VersionInfoBean(res);
//                        final VersionInfoBean oldBean = ;
                        double newVersion = Double.parseDouble(newBean.Version);
                        double oldVersion = BuildConfig.VERSION_CODE / 100.0;
                        Log.i(TAG, "onResponse: " + res + newBean.toString() + newVersion + "-----" + oldVersion);
                        if (newVersion > oldVersion) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    doNewVersionUpdate(BuildConfig.VERSION_NAME, newBean.VersionName, newBean.Updatecontent, newBean.VURL);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isCanCheckVersion = true;
                                    UItoolKit.showToastShort(getActivity(), "已经是最新版");
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    isCanCheckVersion = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UItoolKit.showToastShort(getActivity(), "检查更新失败，可能是网络问题");
                        }
                    });
                }
            }
        });
    }


    private void doNewVersionUpdate(String verName, String newVerName, String updateContent, final String downloadUrl) {
//        String verName = this.getVerName(viewReference.get());
        String sb = "当前版本：（V" +
                verName +
                "）\n" +
                "发现版本：(V" +
                newVerName +
                "）, 是否更新\n" +
                "更新内容：\n" +
                updateContent;
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("软件更新")
                .setMessage(sb)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        UItoolKit.showToastShort(getActivity(), "正在下载更新文件");
                        autoUpdate(downloadUrl, FileUtil.getDownloadPath() + "Waner.apk");
                    }
                })
                .setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
                    }
                }).create();
        //显示更新框
        dialog.show();
    }

    private void autoUpdate(String url, final String fileName) {
        if (!url.contains("user_id")) {
            url += "&user_id=" + PreferencesHelper.getInstance().getCurrentUser().id;
        }
        NotificationCompat.Builder mNotificationCompatBuilder = new NotificationCompat.Builder(getActivity().getApplicationContext()).setOngoing(false).setSmallIcon(R.drawable.waner_logo);
        final NotificationManager mNotificationManager = (NotificationManager) getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final VersionPlaywork.UpdateProgressRunnable runnable = new VersionPlaywork.UpdateProgressRunnable(mNotificationCompatBuilder, mNotificationManager);
        final Handler handler = new Handler();

        final String finalUrl = url;
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                boolean res = OkHttpClientManager.getInstance().downloadFile(finalUrl, fileName, new ProgressResponseListener() {
                    @Override
                    public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                        if (!done) {
                            runnable.setProgress((int) bytesRead, (int) contentLength, "正在下载");
                            handler.post(runnable);
                        } else {
                            runnable.setProgress((int) bytesRead, (int) contentLength, "下载完成");
                            handler.post(runnable);
                        }
                    }
                });
                if (res) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            isCanCheckVersion = true;
                            getActivity().startActivity(FileUtil.getOpenFileIntent(getActivity(), FileUtil.getDownloadPath() + "Waner.apk"));
                            mNotificationManager.cancel(101);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            isCanCheckVersion = true;
                            UItoolKit.showToastShort(getActivity(), "下载更新文件失败，可能网络原因");
                            mNotificationManager.cancel(101);
                        }
                    });
                }
            }
        });

    }

}
