package com.inspur.playwork.weiyou.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

public class VUConfirmDialog {

    private static final String TAG = "VUConfirmDialog";
    public ConfirmDialogListener confirmDialogListener;
    private PopupWindow pop;
    private View popWindowView;
    private Activity activity;

    public VUConfirmDialog(Activity context, String msg, String btn1Name, String btn2Name) {
        initPopWIndow(context,msg);
        Button button1 = (Button) popWindowView.findViewById(R.id.wy_confirm_btn1);
        Button button2 = (Button) popWindowView.findViewById(R.id.wy_confirm_btn2);
        if (button1 != null) button1.setText(btn1Name);
        if (button2 != null) button2.setText(btn2Name);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                if(confirmDialogListener!=null) confirmDialogListener.onButton1Click();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
                if(confirmDialogListener!=null) confirmDialogListener.onButton2Click();
            }
        });
    }
//    public VUConfirmDialog(Activity context, String msg, String btn1Name, int btn1Color, String btn2Name,int btn2Color) {
//        initPopWIndow(context,msg);
//        Button button1 = (Button) popWindowView.findViewById(R.id.wy_confirm_btn1);
//        Button button2 = (Button) popWindowView.findViewById(R.id.wy_confirm_btn2);
//        if (button1 != null) button1.setText(btn1Name);
//        if (button2 != null) button2.setText(btn2Name);
//        try {
//            if(btn1Color!=0) button1.setBackgroundColor(context.getResources().getColor(btn1Color));
//            if(btn2Color!=0) button2.setBackgroundColor(context.getResources().getColor(btn2Color));
//        }catch (Exception e){
//            Log.e(TAG, "VUConfirmDialog: 设置按钮颜色出错");
//            e.printStackTrace();
//        }
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                pop.dismiss();
//                if(confirmDialogListener!=null) confirmDialogListener.onButton1Click();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                pop.dismiss();
//                if(confirmDialogListener!=null) confirmDialogListener.onButton2Click();
//            }
//        });
//    }



    private void initPopWIndow(Activity context, String msg){
        this.activity = context;
        popWindowView = LayoutInflater.from(activity).inflate(R.layout.wy_pop_window, null);
        if (msg != null)
            ((TextView) popWindowView.findViewById(R.id.wy_pop_window_title)).setText(msg);
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

    public void setOutsideTouchDisable(){
        pop.setOutsideTouchable (false);// 设置点击屏幕Dialog不消失
        pop.setFocusable(false);
    }

    public void showPopWindow(View targetView) {
        ColorDrawable cd = new ColorDrawable(0x000000);
        pop.setBackgroundDrawable(cd);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.3f;
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
        void onButton1Click();
        void onButton2Click();
    }
}
