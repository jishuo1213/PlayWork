package com.inspur.playwork.view.common;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;

public class GuideActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final String TAG = "GuideActivity";

    public static final String USER_OPEN = "user_open";

    private int[] drawables = {R.drawable.guide_1, R.drawable.guide_2, R.drawable.guide_3,
            R.drawable.guide_4, R.drawable.guide_5, R.drawable.guide_6, R.drawable.guide_7};

    private int currentIndex;

    private View[] imageViews = new View[7];

    boolean isCreate = false;

    private RecyclerView tabRecyclerView;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_guide_activity);
        viewPager = (ViewPager) findViewById(R.id.page_guide);
        for (int i = 0; i < 6; i++) {
            imageViews[i] = LayoutInflater.from(this).inflate(R.layout.layout_single_imageview, viewPager, false);
        }
        imageViews[6] = LayoutInflater.from(this).inflate(R.layout.layout_guide_7, viewPager, false);
        currentIndex = 0;
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(new GuideViewPagerAdapter(imageViews));
        viewPager.setCurrentItem(0);
        if (savedInstanceState == null) {
            isCreate = true;
        }
        tabRecyclerView = (RecyclerView) findViewById(R.id.recycler_tabs);
        tabRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tabRecyclerView.setAdapter(new TabsAdapter(tabRecyclerView));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCreate) {
            imageViews[0].setOnClickListener(this);
            Glide.with(this).load(drawables[0]).skipMemoryCache(true).placeholder(R.color.guide_mask).into((ImageView) imageViews[0]);
            Log.i(TAG, "onResume: " + getIntent().getBooleanExtra(USER_OPEN, false));
            if (!getIntent().getBooleanExtra(USER_OPEN, false)) {
                UItoolKit.showToastShort(this, "引导一定要看完，不然下次还要再看一遍哦");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentIndex == 6) {
            Glide.clear(imageViews[currentIndex].findViewById(R.id.guide7_main_image));
        } else {
            Glide.clear(imageViews[currentIndex]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected: " + position);
        int prviousSelcet = currentIndex;
        if (prviousSelcet == 6) {
            Glide.clear(imageViews[prviousSelcet].findViewById(R.id.guide7_main_image));
        } else {
            Glide.clear(imageViews[prviousSelcet]);
        }
        currentIndex = position;
        View view = imageViews[position];
        if (currentIndex == 6) {
            view.findViewById(R.id.view_again).setOnClickListener(this);
            view.findViewById(R.id.view_go).setOnClickListener(this);
        } else {
//            ImageView view = (ImageView) imageViews[position % 5];
            Glide.with(this).load(drawables[position]).placeholder(R.color.guide_mask).skipMemoryCache(true).into((ImageView) view);
            view.setOnClickListener(this);
        }
        if (isAgain) {
            ((TabsAdapter) tabRecyclerView.getAdapter()).setPositionSelect(0);
            isAgain = false;
            return;
        }
        if (position > prviousSelcet) {
            ((TabsAdapter) tabRecyclerView.getAdapter()).showNext();
        } else if (position < prviousSelcet) {
            ((TabsAdapter) tabRecyclerView.getAdapter()).showPrevious();
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private boolean isAgain;

    @Override
    public void onClick(View v) {
        if (currentIndex == 6) {
            switch (v.getId()) {
                case R.id.view_again:
                    isAgain = true;
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.view_go:
                    if (getIntent().getBooleanExtra(USER_OPEN, false)) {
                        finish();
                        return;
                    }
                    PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.IS_GUIDE_PAGE_SHOW, true);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
            }
        } else {
            int index = currentIndex;
            viewPager.setCurrentItem(++index);
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(USER_OPEN, false)) {
            super.onBackPressed();
        }
    }

    private static class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.ViewHolder> {


        private int selectIndex = 0;
        private RecyclerView recyclerView;

        TabsAdapter(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.guide_tab_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == selectIndex) {
                holder.tab.setSelected(true);
            } else {
                holder.tab.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return 7;
        }

        void setPositionSelect(int i) {
            ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
            preViewHolder.tab.setSelected(false);
            ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(i);
            selectIndex = i;
            newViewHolder.tab.setSelected(true);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            View tab;

            public ViewHolder(View itemView) {
                super(itemView);
                tab = itemView.findViewById(R.id.tv_tab);
            }
        }

        void showNext() {
            ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
            preViewHolder.tab.setSelected(false);
            ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(++selectIndex);
            newViewHolder.tab.setSelected(true);
        }

        void showPrevious() {
            ViewHolder preViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(selectIndex);
            preViewHolder.tab.setSelected(false);
            ViewHolder newViewHolder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(--selectIndex);
            newViewHolder.tab.setSelected(true);
        }
    }


    private static class GuideViewPagerAdapter extends PagerAdapter {

        View[] imageViews;

        GuideViewPagerAdapter(View[] imageViews) {
            this.imageViews = imageViews;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViews[position]);
            return imageViews[position];
//            if (position == 6) {
//            } else {
//                container.addView(imageViews[position % 5]);
//                return imageViews[position % 5];
//            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViews[position]);
//            if (position == 6) {
//                container.removeView(imageViews[5]);
//            } else {
//                container.removeView(imageViews[position % 5]);
//            }
        }
    }
}
