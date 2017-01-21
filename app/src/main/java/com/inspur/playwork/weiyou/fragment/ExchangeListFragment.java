package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailHeadListAdapter;
import com.inspur.playwork.weiyou.store.ExchangeMailListOperation;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;

/**
 * Created by 孙 on 2015/12/2 0002.
 */
public class ExchangeListFragment extends Fragment implements ExchangeMailListOperation,View.OnClickListener,MailHeadListAdapter.ItemClickListener {
    private static final String TAG = "MailListFragment-->";
    private WeiYouMainActivity wyma;
    private SwipeMenuRecyclerView mailListLv;
    private MailHeadListAdapter mMenuAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity)getActivity();
//        wyma.vuStores.setExchangeMailListOperation(this);
//        关闭侧边栏手势滑动
        if(wyma.drawer!=null)
            wyma.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wy_fragment_exchange_list, container, false);
//        打开侧边栏手势滑动
        view.findViewById(R.id.wy_back_btn).setOnClickListener(this);
        view.findViewById(R.id.el_write_mail_btn).setOnClickListener(this);

        ArrayList<MailDetail> mdList = wyma.vuStores.getExchangeMailList();
        if(mdList.size() == 0){
            view.findViewById(R.id.el_no_mail_tv).setVisibility(View.VISIBLE);
        }else{
            mailListLv = (SwipeMenuRecyclerView) view.findViewById(R.id.ml_recycler_view);
            mailListLv.setLayoutManager(new LinearLayoutManager(wyma));// 布局管理器。
            mailListLv.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
            mailListLv.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
//        mailListLv.addItemDecoration(new ListViewDecoration());// 添加分割线。

            // 为SwipeRecyclerView的Item创建菜单就两句话，不错就是这么简单：
            // 设置菜单创建器。
            mailListLv.setSwipeMenuCreator(swipeMenuCreator);
            // 设置菜单Item点击监听。
            mailListLv.setSwipeMenuItemClickListener(menuItemClickListener);

            if(mailListLv.getAdapter() == null) {
                mMenuAdapter = new MailHeadListAdapter(wyma,mdList,false);
                mMenuAdapter.setIcListener(this);
                mailListLv.setAdapter(mMenuAdapter);
            }
        }
        return view;
    }


    /**
     * 菜单创建器。在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {

            SwipeMenuItem addItem = new SwipeMenuItem(wyma)
                    .setBackgroundDrawable(R.drawable.wy_ml_mark_item)// 点击的背景。
                    .setImage(R.drawable.wy_ml_mark_img) // 图标。
                    .setText("标记") // 文字。
                    .setTextSize(16) // 文字大小。
                    .setTextColor(Color.WHITE) // 文字颜色。
                    .setWidth(DeviceUtil.dpTopx(wyma,100)) // 宽度。
                    .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT); // 高度。
            swipeRightMenu.addMenuItem(addItem); // 添加一个按钮到左侧菜单。

            SwipeMenuItem deleteItem = new SwipeMenuItem(wyma)
                    .setBackgroundDrawable(R.drawable.wy_ml_delete_item)
                    .setImage(R.drawable.wy_mail_list_delete) // 图标。
                    .setText("删除") // 文字。
                    .setTextColor(Color.WHITE) // 文字颜色。
                    .setTextSize(16) // 文字大小。
                    .setWidth(DeviceUtil.dpTopx(wyma,100))
                    .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
            swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.

            // 上面的菜单哪边不要菜单就不要添加。
        }
    };

    /**
     * 菜单点击监听。
     */
    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        /**
         * Item的菜单被点击的时候调用。
         * @param closeable       closeable. 用来关闭菜单。
         * @param adapterPosition adapterPosition. 这个菜单所在的item在Adapter中position。
         * @param menuPosition    menuPosition. 这个菜单的position。比如你为某个Item创建了2个MenuItem，那么这个position可能是是 0、1，
         * @param direction       如果是左侧菜单，值是：SwipeMenuRecyclerView#LEFT_DIRECTION，如果是右侧菜单，值是：SwipeMenuRecyclerView#RIGHT_DIRECTION.
         */
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。

            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                MailDetail targetMD = wyma.vuStores.getMailListData().get(adapterPosition);
                switch (menuPosition){
                    case 0://标记
                        wyma.vuStores.markMail(targetMD);
                        mMenuAdapter.notifyItemChanged(adapterPosition);
                        break;
                    case 1://删除
                        wyma.vuStores.deleteMail(targetMD);
                        mMenuAdapter.notifyItemRemoved(adapterPosition);
                        break;
                }
            }
        }
    };

    @Override
    public void onAvatarClick(MailDetail md) {}

    @Override
    public void onMailClickCB(MailDetail md) {
        wyma.vuStores.mailClickHandler(md);
    }

    @Override
    public void loadMoreMail() {}

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.el_write_mail_btn) {
            Intent intent = new Intent();
            intent.putExtra("type",0);
            intent.putExtra("to",WeiYouUtil.getUserJSON(wyma.vuStores.getExchangeEmailAddress(),wyma.vuStores.getExchangeName()));
            wyma.gotoWriteMail(intent);//来往邮件写邮件
        }else if (id == R.id.wy_back_btn) {
            wyma.onBackPressed();//返回
        }
    }
    @Override
    public void refreshMailItem(int position){
        mMenuAdapter.notifyItemChanged(position);
    }

    @Override
    public void onDestroy(){
        wyma.vuStores.clearExchangeListData();
        super.onDestroy();
    }
}

