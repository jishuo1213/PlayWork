package com.inspur.playwork.view.common.progressbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

/**
 * Created by Fan on 15-12-15.
 */
public class CommonDialog extends DialogFragment {

    private static final String TIP_TEXT = "tip";
    private static final String IS_COMMON = "iscommon";

    public static DialogFragment getInstance(String text) {
        Bundle bundle = new Bundle();
        DialogFragment dialogFragment = new CommonDialog();
        bundle.putString(TIP_TEXT, text);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    public static DialogFragment getInstance(String text, boolean common) {
        Bundle bundle = new Bundle();
        DialogFragment dialogFragment = new CommonDialog();
        bundle.putString(TIP_TEXT, text);
        bundle.putBoolean(IS_COMMON, common);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int sh = DeviceUtil.getDeviceScreenHeight(getActivity());
        Dialog dialog = new Dialog(getActivity(), R.style.common_dialog);
        if (!getArguments().getBoolean(IS_COMMON)) {
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wmlp = window.getAttributes();
            wmlp.gravity = Gravity.BOTTOM;
            wmlp.y = Double.valueOf(sh * 0.382).intValue();
            window.setAttributes(wmlp);
        }
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.layout_common_dialog, null);
        dialog.setContentView(v);
        dialog.setCanceledOnTouchOutside(false);
        TextView tipView = (TextView) v.findViewById(R.id.tv_show_text);
        String tip = getArguments().getString(TIP_TEXT);
        if (!TextUtils.isEmpty(tip))
            tipView.setText(tip);
        return dialog;
    }
}
