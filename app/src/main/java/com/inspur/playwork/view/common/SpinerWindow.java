package com.inspur.playwork.view.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;

/**
 * Created by Fan on 15-12-30.
 */
public class SpinerWindow extends PopupWindow {


    private RecyclerView recyclerView;

    public SpinerWindow(Context context, RecyclerView.Adapter adapter) {
        super(context);
        init(context, adapter);
    }

    public void init(Context context, RecyclerView.Adapter adapter) {
        @SuppressLint("InflateParams")
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_pop_chose_date, null, false);
        setContentView(rootView);
//        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(DeviceUtil.dpTopx(context, 40 * 6));

        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_pop_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    public void showPopWindow(View view) {
        setWidth(view.getWidth());
        showAsDropDown(view);
        recyclerView.scrollToPosition(0);
    }
}
