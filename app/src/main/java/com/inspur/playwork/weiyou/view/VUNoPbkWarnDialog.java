package com.inspur.playwork.weiyou.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

public class VUNoPbkWarnDialog {

    public NoPbkWarnDialogListener noPbkWarnDialogListener;
    private final PopupWindow pop;
    private final View popWindowView;
    private Activity activity;

    public VUNoPbkWarnDialog(Activity context, String msg,int showContinueButton) {
        this.activity = context;
        popWindowView = LayoutInflater.from(activity).inflate(R.layout.wy_pw_nopbk_confirm, null);
        if (msg != null)
            ((TextView) popWindowView.findViewById(R.id.wy_pw_nopbk_emails)).setText(msg);
        popWindowView.findViewById(R.id.wy_send_no_encrypt_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                noPbkWarnDialogListener.onSendNoEncryptMailButtonClick();
            }
        });
        View continueButton = popWindowView.findViewById(R.id.wy_continue_send_btn);
        if(showContinueButton!=0) {
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pop.dismiss();
                    noPbkWarnDialogListener.onContinueSendButtonClick();
                }
            });
            continueButton.setVisibility(View.VISIBLE);
        }else{
            continueButton.setVisibility(View.GONE);
        }
        popWindowView.findViewById(R.id.wy_cancel_send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                noPbkWarnDialogListener.onCancelSendButtonClick();
            }
        });
        popWindowView.setFocusableInTouchMode(true);
        pop = new PopupWindow(popWindowView, LinearLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(activity, 260), true);
        pop.setAnimationStyle(R.style.MenuAnimationFade);
        pop.setOutsideTouchable(true);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1f;
                activity.getWindow().setAttributes(lp);
            }
        });
    }

    public void showPopWindow(View targetView) {
        ColorDrawable cd = new ColorDrawable(0x000000);
        pop.setBackgroundDrawable(cd);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.4f;
        activity.getWindow().setAttributes(lp);
        pop.showAtLocation(targetView, Gravity.BOTTOM, 0, 0);
    }
    public void hidePopWindow(){
        pop.dismiss();
    }

    public boolean isPopWindowShowing(){
        return pop.isShowing();
    }

    public void setNoPbkWarnDialogListener(NoPbkWarnDialogListener noPbkWarnDialogListener) {
        this.noPbkWarnDialogListener = noPbkWarnDialogListener;
    }

    public interface NoPbkWarnDialogListener {
        void onSendNoEncryptMailButtonClick();
        void onContinueSendButtonClick();
        void onCancelSendButtonClick();
    }
}
