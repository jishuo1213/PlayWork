package com.inspur.playwork.weiyou.view;

/**
 * Created by sunyuan on 2016/12/1 0001 15:12.
 * Email: sunyuan@inspur.com
 */

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

public class DownloadProgressPopWindow{
    private TextView mMenuView;
    private PopupWindow pop;
    public DownloadProgressPopWindow(final Activity context) {
        mMenuView = new TextView(context);
        mMenuView.setWidth(LayoutParams.MATCH_PARENT);
        mMenuView.setHeight(LayoutParams.MATCH_PARENT);
        mMenuView.setGravity(Gravity.CENTER);
        mMenuView.setBackgroundColor(Color.argb(180,0,0,0));
        mMenuView.setTextColor(0xaaffffff);
        //设置SelectPicPopupWindow的View
        pop = new PopupWindow(mMenuView, LinearLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(context, 50), true);
        pop.setAnimationStyle(R.style.MenuAnimationFade);
        pop.setOutsideTouchable(true);
    }
    public void setParam(String text){
        mMenuView.setText(text);
    }
    public void showPopWindow(View targetView){
        if(!pop.isShowing())
            pop.showAtLocation(targetView, Gravity.BOTTOM, 0, 0);
    }

    public void hidePopWindow() {
        pop.dismiss();
    }
}