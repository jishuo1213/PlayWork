package com.inspur.playwork.common.chosefile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.loadpicture.BitmapCacheManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Fan on 16-3-25.
 */
public class ChoseFileDialogFragment extends DialogFragment implements ChoseLocalFileAdapter.ChoseFileEventListener,
        DialogInterface.OnKeyListener, View.OnClickListener {

    private static final String TAG = "ChoseFileDialogFan";

    private RecyclerView fileList;
    private TextView parentPath;
    private TextView choseInfo;
    private TextView confrimChose;
    private TextView titleView;

    private String[] rootPath;
    private ArrayList<LocalFileBean> selectFileList = new ArrayList<>();
    private LocalFileBean parentFileBean;

    private long totalSize;

    private ChoseFileResListener listener;

    private LruCache<String, ArrayList<LocalFileBean>> cache = new LruCache<>(10);

    private BitmapCacheManager photoCache;

    private boolean singleSelectMode = false;

    public void setSingleSelectMode(boolean singleSelectMode) {
        this.singleSelectMode = singleSelectMode;
    }

    public interface ChoseFileResListener {
        void onFileSelect(ArrayList<LocalFileBean> choseFileList);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        photoCache = BitmapCacheManager.findOrCreateRetainFragment(getFragmentManager());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.add_task_dialog);
        @SuppressLint("InflateParams")
        View dialogRoot = LayoutInflater.from(getActivity()).inflate(R.layout.layout_chose_file, null);
        dialog.setContentView(dialogRoot);
        initView(dialogRoot);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = DeviceUtil.getDeviceScreenWidth(getActivity());
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        Rect rect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        lp.height = rect.height();
        dialogWindow.setAttributes(lp);
        dialog.setOnKeyListener(this);
        return dialog;
    }

    private void initView(View rootView) {
        fileList = (RecyclerView) rootView.findViewById(R.id.recy_file_list);
        parentPath = (TextView) rootView.findViewById(R.id.tv_path);
        choseInfo = (TextView) rootView.findViewById(R.id.tv_chose_file_info);
        confrimChose = (TextView) rootView.findViewById(R.id.tv_confrim_chose);
        ImageButton tv_left = (ImageButton) rootView.findViewById(R.id.iv_left);
        titleView = (TextView) rootView.findViewById(R.id.tv_title);
        fileList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rootPath = FileUtil.getStorageDirectories(getActivity());
        ArrayList<LocalFileBean> fileBeanArrayList = initRootLocalFileList();
        parentPath.setHint("所有文件");
        titleView.setText("选择文件");
        ChoseLocalFileAdapter adapter = new ChoseLocalFileAdapter(fileList);
        adapter.setListener(this);
        adapter.setPhotoCache(photoCache);
        adapter.show(fileBeanArrayList, false);
        adapter.setSingleSelectedMode(singleSelectMode);
        fileList.setAdapter(adapter);
        confrimChose.setOnClickListener(this);
        if(!TextUtils.isEmpty(okBtnText)) confrimChose.setText(okBtnText);
        tv_left.setVisibility(View.VISIBLE);
        tv_left.setOnClickListener(this);
    }

    private String okBtnText;
    public void setOkBtnText(String btnName){
        okBtnText = btnName;
    }

    @NonNull
    private ArrayList<LocalFileBean> initRootLocalFileList() {
        ArrayList<LocalFileBean> res = cache.get("root");
        if (res != null)
            return res;
        ArrayList<LocalFileBean> fileBeanArrayList = new ArrayList<>();
        for (String path : rootPath) {
            Log.i(TAG, "initView: " + path);
            LocalFileBean localFileBean = new LocalFileBean(true, path);
            fileBeanArrayList.add(localFileBean);
        }
        cache.put("root", fileBeanArrayList);
        return fileBeanArrayList;
    }

    public void setListener(ChoseFileResListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFileSelected(LocalFileBean choseBean, boolean isSelected) {
        if (isSelected) {
            if(singleSelectMode) {
                totalSize = choseBean.size;
                selectFileList.clear();
            }else{
                totalSize += choseBean.size;
            }
            selectFileList.add(choseBean);
        } else {
            totalSize -= choseBean.size;
            selectFileList.remove(choseBean);
        }
        choseInfo.setText("已选" + FileUtil.getFileSizeStr(totalSize));
        if(TextUtils.isEmpty(okBtnText)) {
            if (selectFileList.size() > 0) {
                confrimChose.setSelected(true);
                confrimChose.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                confrimChose.setText("发送(" + selectFileList.size() + ")");
            } else {
                confrimChose.setSelected(false);
                confrimChose.setTextColor(ResourcesUtil.getInstance().getColor(R.color.text_gray));
                confrimChose.setText("发送(" + 0 + ")");
            }
        }else{
            confrimChose.setSelected(isSelected);
            confrimChose.setTextColor(ResourcesUtil.getInstance().getColor(isSelected?R.color.white:R.color.text_gray));
        }
        titleView.setText("已选" + selectFileList.size() + "个");
    }

    @Override
    public void onOpenNewFloder(LocalFileBean parentBean) {
        ChoseLocalFileAdapter adapter = (ChoseLocalFileAdapter) fileList.getAdapter();
        ArrayList<LocalFileBean> res = cache.get(parentBean.currentPath);
        Log.i(TAG, "onOpenNewFloder: " + parentBean.currentPath);
        if (res == null) {
            res = parentBean.getChildFiles();
            cache.put(parentBean.currentPath, res);
        }
        adapter.show(res, true);
        parentFileBean = parentBean;
        parentPath.setText(parentFileBean.currentPath);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            onBackPressed(dialog);
            return true;
        }
        return false;
    }

    public void onBackPressed(DialogInterface dialog) {
        if (parentFileBean == null) {
            dialog.cancel();
        } else {
            Log.i(TAG, "onBackPressed: " + parentFileBean.currentPath);
            if (checkIsRoot(parentFileBean.currentPath)) {
                ChoseLocalFileAdapter adapter = (ChoseLocalFileAdapter) fileList.getAdapter();
                ArrayList<LocalFileBean> res = cache.get("root");
                if (res == null) {
                    res = initRootLocalFileList();
                    cache.put("root", res);
                }
                adapter.show(res, true);
                parentFileBean = null;
                parentPath.setText("");
            } else {
                Log.i(TAG, "onBackPressed: " + parentFileBean.parentPath);
                parentFileBean = new LocalFileBean(checkIsRoot(parentFileBean.parentPath), parentFileBean.parentPath);
                ChoseLocalFileAdapter adapter = (ChoseLocalFileAdapter) fileList.getAdapter();
                ArrayList<LocalFileBean> res = cache.get(parentFileBean.currentPath);
                if (res == null) {
                    res = parentFileBean.getChildFiles();
                    cache.put(parentFileBean.currentPath, res);
                }
                adapter.show(res, true);
                parentPath.setText(parentFileBean.currentPath);
            }
        }
    }

    private boolean checkIsRoot(String parentPath) {
        for (String root : rootPath) {
            if (root.equals(parentPath + File.separator) || root.equals(parentPath))
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confrim_chose:
                if (selectFileList.size() > 0) {
                    listener.onFileSelect(selectFileList);
                    dismiss();
                }
                break;
            case R.id.iv_left:
                onBackPressed(getDialog());
                break;
        }
    }
}
