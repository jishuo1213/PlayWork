package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailHeadListAdapter;
import com.inspur.playwork.weiyou.store.MailListOperation;
import com.inspur.playwork.weiyou.view.ListViewDecoration;
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
public class MailListFragment extends Fragment implements MailListOperation,View.OnClickListener,
        MailHeadListAdapter.ItemClickListener {
    private static final String TAG = "MailListFragment-->";
    private WeiYouMainActivity wyma;

    private TextView dirNameTV;
    private ImageButton sideBarToggleBtn;
    private ImageButton writeMailBtn;
    private ImageButton searchMailBtn;
    private ImageButton exitButton;
    public AutoCompleteTextView mailSearchBox;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipeMenuRecyclerView mailListLv;
    private MailHeadListAdapter mMenuAdapter;
    private LinearLayoutManager llManager;
    private LinearLayout emptyView;
    private TextView downloadInfoTV;

    private LinearLayout guidePageLL;
//    public boolean isExitedVU = false;

    private boolean isFirstSetup = true;
    private ArrayList<MailDetail> mailListData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "mailListFragment onCreate: ..........");
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setMailListReference(this);
        mailListData = wyma.vuStores.getMailListData();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "mailListFragment onCreateView: ...........");
        final View view = inflater.inflate(R.layout.wy_fragment_mail_list, container, false);
//        打开侧边栏手势滑动
        if (wyma.drawer != null)
            wyma.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        sideBarToggleBtn = (ImageButton) view.findViewById(R.id.ml_menu_btn);
        writeMailBtn = (ImageButton) view.findViewById(R.id.ml_write_mail_btn);
        searchMailBtn = (ImageButton) view.findViewById(R.id.ml_search_mail_btn);
        exitButton = (ImageButton) view.findViewById(R.id.ml_exit_mail_btn);
        sideBarToggleBtn.setOnClickListener(this);
        writeMailBtn.setOnClickListener(this);
        searchMailBtn.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        dirNameTV = (TextView) view.findViewById(R.id.ml_dir_name);
        dirNameTV.setText(wyma.vuStores.currDirName);

        emptyView = (LinearLayout) view.findViewById(R.id.ml_empty_view);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wyma.vuStores.currDirIsInbox()) {
                    emptyView.findViewById(R.id.ml_no_mail_tv).setVisibility(View.GONE);
                    emptyView.findViewById(R.id.click_to_load_tv).setVisibility(View.GONE);
                    emptyView.findViewById(R.id.ml_is_loading_rl).setVisibility(View.VISIBLE);
                    wyma.vuStores.checkNewMail(true);
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mailListLv = (SwipeMenuRecyclerView) view.findViewById(R.id.ml_recycler_view);
        llManager = new LinearLayoutManager(wyma);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        mailListLv.setLayoutManager(llManager);// 布局管理器。
        mailListLv.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        mailListLv.addItemDecoration(new ListViewDecoration(wyma));// 添加分割线。
//        // 添加滚动监听。
//        mailListLv.addOnScrollListener(mOnScrollListener);
        // 设置菜单创建器。
        mailListLv.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听。
        mailListLv.setSwipeMenuItemClickListener(menuItemClickListener);

        mailSearchBox = (AutoCompleteTextView) view.findViewById(R.id.ml_search_mail_actv);
        initMailSearchBox();
        downloadInfoTV = (TextView) view.findViewById(R.id.download_info_tv);
        guidePageLL = (LinearLayout) view.findViewById(R.id.vu_guide_ll);
        guidePageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guidePageLL.setVisibility(View.GONE);
            }
        });
        if (wyma.vuStores.needShowGuidePage()){
            guidePageLL.setVisibility(View.VISIBLE);
            wyma.vuStores.notShowGuidePageAnymore();
        }else guidePageLL.setVisibility(View.GONE);

        if (isFirstSetup) {
            Log.i(TAG, "onCreateView: is first setup...");
            wyma.vuStores.loadListData();
            isFirstSetup = false;
        } else {
            Log.i(TAG, "onCreateView: is not first setup...");
            boolean mlistIsEmpty = mailListData.size() == 0;
            Log.i(TAG, "mailListFragment renderMailListView: " + mailListData);
            if(mlistIsEmpty) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.findViewById(R.id.ml_no_mail_tv).setVisibility(View.VISIBLE);
                emptyView.findViewById(R.id.click_to_load_tv).setVisibility(wyma.vuStores!=null && wyma.vuStores.currDirIsInbox()?View.VISIBLE:View.GONE);
                emptyView.findViewById(R.id.ml_is_loading_rl).setVisibility(View.GONE);
            }else emptyView.setVisibility(View.GONE);
            mMenuAdapter = new MailHeadListAdapter(wyma, mailListData, !mlistIsEmpty && wyma.vuStores.currDirIsInbox());
            mMenuAdapter.setIcListener(MailListFragment.this);
            mailListLv.setAdapter(mMenuAdapter);
        }
        return view;
    }

    /**
     * 初始化搜索框
     */
    private void initMailSearchBox() {
        mailSearchBox.setThreshold(1); // 1个字符开始匹配
        mailSearchBox.addTextChangedListener(searchNameTextWatcher);
        mailSearchBox.clearFocus();
    }

    /**
     * 通讯录搜索框TextWatcher
     */
    private TextWatcher searchNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mMenuAdapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    @Override
    public void onAvatarClick(MailDetail md) {
        wyma.vuStores.showExchangeMailList(md);
    }

    @Override
    public void onMailClickCB(MailDetail md) {
        wyma.vuStores.mailClickHandler(md);
    }

    @Override
    public void loadMoreMail() {
        wyma.vuStores.checkNewMail(false);
    }

    @Override
    public void renderMailListView() {
        wyma.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean mlistIsEmpty = mailListData.size() == 0;
                Log.i(TAG, "mailListFragment renderMailListView: " + mailListData);
                if(mlistIsEmpty) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.findViewById(R.id.ml_no_mail_tv).setVisibility(View.VISIBLE);
                    emptyView.findViewById(R.id.click_to_load_tv).setVisibility(wyma.vuStores!=null && wyma.vuStores.currDirIsInbox()?View.VISIBLE:View.GONE);
                    emptyView.findViewById(R.id.ml_is_loading_rl).setVisibility(View.GONE);
                }else
                    emptyView.setVisibility(View.GONE);
                if(mMenuAdapter == null) {
                    mMenuAdapter = new MailHeadListAdapter(wyma, mailListData, !mlistIsEmpty && wyma.vuStores.currDirIsInbox());
                    mMenuAdapter.setIcListener(MailListFragment.this);
                    mailListLv.setAdapter(mMenuAdapter);
                }else{
                    Log.i(TAG, "run: mailListData.size() = "+mailListData.size());
                    Log.i(TAG, "run: mMenuAdapter == null ? "+(mMenuAdapter == null));
                    mMenuAdapter.setFooterViewVisible(!mlistIsEmpty && wyma.vuStores.currDirIsInbox());
                    mMenuAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void setListViewPullWay() {
//        设置带上拉下拉的listView
        if(wyma.vuStores!=null)
            if (wyma.vuStores.currDirIsInbox()) {
                mSwipeRefreshLayout.setEnabled(true);
            } else {
                mSwipeRefreshLayout.setEnabled(false);
            }
    }

    public void hideSearchBox() {
        setListViewPullWay();
        mailSearchBox.setText("");
        mailSearchBox.setHint("搜索");
        ((LinearLayout) mailSearchBox.getParent()).setVisibility(View.GONE);
    }

    /**
     * 菜单创建器。在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            {
                SwipeMenuItem addItem = new SwipeMenuItem(wyma)
                        .setBackgroundDrawable(R.drawable.wy_ml_mark_item)// 点击的背景。
                        .setImage(R.drawable.wy_ml_mark_img) // 图标。
                        .setText("标记") // 文字。
                        .setTextSize(16) // 文字大小。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setWidth(DeviceUtil.dpTopx(wyma, 80)) // 宽度。
                        .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT); // 高度。
                swipeRightMenu.addMenuItem(addItem); // 添加一个按钮到左侧菜单。

                SwipeMenuItem deleteItem = new SwipeMenuItem(wyma)
                        .setBackgroundDrawable(R.drawable.wy_ml_delete_item)
                        .setImage(R.drawable.wy_mail_list_delete) // 图标。
                        .setText("删除") // 文字。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(16) // 文字大小。
                        .setWidth(DeviceUtil.dpTopx(wyma, 80))
                        .setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
            }
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
                        if (mMenuAdapter.getItemCount() > 1)
                            mMenuAdapter.notifyItemRemoved(adapterPosition);
                        else
                            renderMailListView();
                        break;
                }
            }
        }
    };

    /**
     * 刷新监听。
     */
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            wyma.vuStores.checkNewMail(true);
            mailListLv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 2000);
        }
    };

//    /**
//     * 加载更多
//     */
//    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            int visible  = llManager.getChildCount();
//            int total = llManager.getItemCount();
//            int past=llManager.findFirstCompletelyVisibleItemPosition();
//            if ((visible + past) >= total){
//                wyma.vuStores.checkNewMail(false);
//            }
////            if (!recyclerView.canScrollVertically(1)) {// 手指不能向上滑动了
////                wyma.vuStores.checkNewMail(false);
////                mMenuAdapter.notifyDataSetChanged();
////            }
//        }
//    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.i(TAG, "onClick---" + id);
        if (id == R.id.ml_write_mail_btn) {
            Intent intent = new Intent();
            intent.putExtra("type",0);
            wyma.gotoWriteMail(intent);
            //写邮件
        } else if (id == R.id.ml_exit_mail_btn) {
            Log.i(TAG, "微邮关闭按钮被点击了: ");
//            isExitedVU = true;
            wyma.finish();
            //关闭微邮
        } else if (id == R.id.ml_search_mail_btn) {
            LinearLayout mlSearchLL = ((LinearLayout) mailSearchBox.getParent());
            if (mlSearchLL.getVisibility() == View.GONE) {
                mlSearchLL.setVisibility(View.VISIBLE);
                mailSearchBox.requestFocus();
                mSwipeRefreshLayout.setEnabled(false);
                wyma.showKeyboard(mailSearchBox);
            } else {
                mlSearchLL.setVisibility(View.GONE);
                mailSearchBox.clearFocus();
                setListViewPullWay();
                wyma.hideInputMethod();
            }
            //搜邮件
        } else if (id == R.id.ml_menu_btn) {
            if (wyma.drawer.isDrawerOpen(GravityCompat.START)) {
                wyma.drawer.closeDrawer(GravityCompat.START);
            } else {
                wyma.drawer.openDrawer(GravityCompat.START);
            }
        }
    }

    public void setDirName(String dirName) {
        this.dirNameTV.setText(dirName);
    }
    @Override
    public void showDownloadInfo(int index,int sum){
        showDownloadInfo("正在下载"+sum+"封邮件，已下载"+index+"封",false);
    }
    @Override
    public void showDownloadInfo(String msg,boolean isDownloadOver){
        downloadInfoTV.setVisibility(View.VISIBLE);
        downloadInfoTV.setText(msg);
        downloadInfoTV.postDelayed(new Runnable() {
            @Override
            public void run() {
                downloadInfoTV.setHeight(0);
                downloadInfoTV.setVisibility(View.GONE);
            }
        },3000);
        if(isDownloadOver == true){
            emptyView.findViewById(R.id.ml_no_mail_tv).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.click_to_load_tv).setVisibility(View.VISIBLE);
            emptyView.findViewById(R.id.ml_is_loading_rl).setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshMailItem(int index) {
        mMenuAdapter.notifyItemChanged(index);
    }

    @Override
    public void onDestroy() {
        wyma.vuStores.setMailListReference(null);
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            wyma.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            renderMailListView();
        }
    }
    @Override
    public void onResume(){
        super.onResume();
    }
}

