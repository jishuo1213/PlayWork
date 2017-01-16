package com.inspur.playwork.weiyou.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.inspur.playwork.R;

public class VUFileSelectorDialog {

    public ConfirmDialogListener confirmDialogListener;
    private final PopupWindow pop;
    private final View popWindowView;
    private Activity activity;

    public VUFileSelectorDialog(Activity context,ConfirmDialogListener cdl) {
        this.activity = context;
        this.confirmDialogListener = cdl;
        popWindowView = LayoutInflater.from(activity).inflate(R.layout.wy_pw_attachment_selector, null);
        popWindowView.findViewById(R.id.wy_pwas_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                confirmDialogListener.onCameraSelected();
            }
        });
        popWindowView.findViewById(R.id.wy_pwas_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                confirmDialogListener.onGallerySelected();
            }
        });
        popWindowView.findViewById(R.id.wy_pwas_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                confirmDialogListener.onFileSelected();
            }
        });
        popWindowView.findViewById(R.id.wy_pwas_local_attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                confirmDialogListener.onLocalAttachmentSelected();
            }
        });
//        popWindowView.findViewById(R.id.wy_pwas_music).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                pop.dismiss();
//                confirmDialogListener.onMusicSelected();
//            }
//        });
//        popWindowView.findViewById(R.id.wy_pwas_video).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                pop.dismiss();
//                confirmDialogListener.onVideoSelected();
//            }
//        });
        popWindowView.setFocusableInTouchMode(true);
        pop = new PopupWindow(popWindowView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
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

//    public void setConfirmDialogListener(ConfirmDialogListener confirmDialogListener) {
//        this.confirmDialogListener = confirmDialogListener;
//    }

    public interface ConfirmDialogListener {
        void onCameraSelected();
        void onGallerySelected();
        void onFileSelected();

        void onLocalAttachmentSelected();
//        void onMusicSelected();
//        void onVideoSelected();
    }
}
