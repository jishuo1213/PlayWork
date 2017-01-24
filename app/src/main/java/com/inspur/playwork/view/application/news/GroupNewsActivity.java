package com.inspur.playwork.view.application.news;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.news.DepartmentNewsBean;
import com.inspur.playwork.view.common.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by fan on 17-1-16.
 */
public class GroupNewsActivity extends BaseActivity implements View.OnClickListener,
        NewListFragment.NewsListEventListener, ViewNewsFragment.DetailNewsEvent {
    private static final String TAG = "GroupNewsActivity";

    private static String[] TABS_NAME = {"集团新闻", "集团公告", "公司新闻", "公司公告"};
    private TextView titleView;

    private int currentPos;
    private ArrayList<DepartmentNewsBean> detailNewsList;
    private ViewNewsFragment detailFragment;
    private NewListFragment listFragment;
    private boolean isDetailViewShow;
    private ArrayMap<String, String> companyName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_address_book);
        initCompanyName(this);
        titleView = (TextView) findViewById(R.id.tv_title);
        titleView.setText("集团新闻");
        ImageButton button = (ImageButton) findViewById(R.id.iv_left);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        listFragment = new NewListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(NewListFragment.TAB_COUNT, 4);
        bundle.putStringArray(NewListFragment.TAB_NAMES, TABS_NAME);
        listFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.fram_app_container, listFragment).commit();
    }

    @Override
    public void onClick(View v) {
        if (isDetailViewShow) {
            getFragmentManager().beginTransaction().remove(detailFragment).show(listFragment).commit();
            isDetailViewShow = false;
            return;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isDetailViewShow) {
            getFragmentManager().beginTransaction().remove(detailFragment).show(listFragment).commit();
            isDetailViewShow = false;
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onTabChange(int index) {
        titleView.setText(TABS_NAME[index]);
    }

    @Override
    public void onNewsClick(int pos, ArrayList<DepartmentNewsBean> newsList) {
        this.currentPos = pos;
        this.detailNewsList = newsList;
//        if (fragment == null) {
        detailFragment = new ViewNewsFragment();
//        }
        getFragmentManager().beginTransaction().hide(listFragment).add(R.id.fram_app_container, detailFragment).commit();
        isDetailViewShow = true;
    }

    @Override
    public String getCompanyName(String name) {
        String englishName = companyName.get(name);
        if (TextUtils.isEmpty(englishName)) {
            return name;
        }
        return englishName;
    }

    @Override
    public int getFirstPos() {
        return currentPos;
    }

    @Override
    public ArrayList<DepartmentNewsBean> getNewsList() {
        return detailNewsList;
    }

    public void initCompanyName(Context context) {
        companyName = new ArrayMap<>();
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(context.getResources().openRawResource(R.raw.company_name));
        JSONObject jsonObject = null;
        char[] buffer = new char[1024 * 4];
        try {
            int count;
            while ((count = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, count);
            }
            reader.close();
            jsonObject = new JSONObject(builder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Log.i(TAG, "initEmjiMap: " + key);
//                JSONObject emoji = jsonObject.optJSONObject(key);
                Log.i(TAG, "initEmjiMap: " + jsonObject.optString(key));
                companyName.put(key, jsonObject.optString(key));
            }
        }
    }
}
