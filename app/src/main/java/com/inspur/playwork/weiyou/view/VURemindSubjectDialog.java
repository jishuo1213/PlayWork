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

public class VURemindSubjectDialog {

    public ConfirmDialogListener confirmDialogListener;
    private final PopupWindow pop;
    private final View popWindowView;
    private Activity activity;

    public VURemindSubjectDialog(Activity context) {
        this.activity = context;
        popWindowView = LayoutInflater.from(activity).inflate(R.layout.wy_pop_window, null);
        TextView cancelButton = (TextView) popWindowView.findViewById(R.id.wy_confirm_btn1);
        TextView okButton = (TextView) popWindowView.findViewById(R.id.wy_confirm_btn2);
        ((TextView) popWindowView.findViewById(R.id.wy_pop_window_title)).setText("邮件现在还没有主题");
        cancelButton.setText("去填写");
        okButton.setText("仍要发送");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                confirmDialogListener.sendNoSubjectMail();
            }
        });
        popWindowView.setFocusableInTouchMode(true);
        pop = new PopupWindow(popWindowView, LinearLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(activity, 150), true);
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

    public void setConfirmDialogListener(ConfirmDialogListener confirmDialogListener) {
        this.confirmDialogListener = confirmDialogListener;
    }

    public interface ConfirmDialogListener {
        void sendNoSubjectMail();
    }
}
