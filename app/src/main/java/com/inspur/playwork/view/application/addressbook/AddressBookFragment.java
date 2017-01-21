package com.inspur.playwork.view.application.addressbook;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.SearchPersonInfo;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.stores.application.ApplicationStores;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.progressbar.CommonDialog;
import com.inspur.playwork.view.message.chat.ChatActivityNew;
import com.inspur.playwork.weiyou.WriteMailActivity;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;

import java.util.ArrayList;

/**
 * Created by fan on 17-1-6.
 */
public class AddressBookFragment extends Fragment implements SearchView.OnCloseListener,
        SearchView.OnQueryTextListener, View.OnFocusChangeListener,
        SearchView.OnSuggestionListener, AddressBookViewOperation, SearchResultAdapter.ItemEventListener {
    private static final String TAG = "AddressBookFragment";

    public static final String ADDRESS_BOOK_CHAT_TAG = "getWindowInfoFromAddressBook";


    private SearchView searchView;
    private Handler handler;
    private RecyclerView searchResultView;
    private boolean isCanSearch = true;

    private VChatBean vChatBean;

    private DialogFragment progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new AddressBookHandler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_address_book, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        searchView = (SearchView) v.findViewById(R.id.search_address_book);
        searchResultView = (RecyclerView) v.findViewById(R.id.recy_search_result);
        searchResultView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnCloseListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(this);
        searchView.setOnSuggestionListener(this);
        searchView.setSuggestionsAdapter(new SearchSuggestAdapter(getActivity(), null, 0));
        searchView.setSearchableInfo(((SearchManager) (getActivity().getApplicationContext().
                getSystemService(Context.SEARCH_SERVICE))).getSearchableInfo(getActivity().getComponentName()));
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationStores.getInstance().setAddressBookReference(this);
        MessageStores.getInstance().setAddressBookRefrence(this);
        GroupStores.getInstance().setViewRefrence(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationStores.getInstance().setAddressBookReference(null);
        MessageStores.getInstance().setAddressBookRefrence(null);
        GroupStores.getInstance().setViewRefrence(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        handler.removeMessages(1);
        if (!TextUtils.isEmpty(query) && isCanSearch) {
            isCanSearch = false;
            setSearchSuggestCursor(null);
            ApplicationStores.getInstance().searchPersonInfo(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (!TextUtils.isEmpty(newText) && isCanSearch) {
            handler.removeMessages(1);
            handler.sendMessageDelayed(handler.obtainMessage(1, newText), 500);
        } else {
            setSearchSuggestCursor(null);
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public boolean onSuggestionSelect(int position) {
        doOnSuggestionSelect(position);
        return true;
    }

    private void doOnSuggestionSelect(int position) {
        SearchSuggestAdapter adapter = (SearchSuggestAdapter) searchView.getSuggestionsAdapter();
        Cursor cursor = adapter.getCursor();
        Log.i(TAG, "doOnSuggestionSelect: " + position);
        if (cursor.moveToPosition(position)) {
            isCanSearch = false;
            String id = cursor.getString(2);
            searchView.setQuery(id, false);
            ApplicationStores.getInstance().searchPersonInfo(id);
        }
    }

    @Override
    public boolean onSuggestionClick(int position) {
        doOnSuggestionSelect(position);
        return true;
    }

    @Override
    public void showSuggestList(final MatrixCursor cursor) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSearchSuggestCursor(cursor);
            }
        });
    }

    @Override
    public void showSearchResult(final ArrayList<SearchPersonInfo> list) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCanSearch = true;
                if (list != null) {
                    if (searchResultView.getAdapter() == null) {
                        SearchResultAdapter adapter = new SearchResultAdapter(searchResultView);
                        adapter.setListener(AddressBookFragment.this);
                        adapter.setResultList(list);
                        searchResultView.setAdapter(adapter);
                    } else {
                        searchResultView.getAdapter().notifyDataSetChanged();
                    }
                    if (list.size() == 0) {
                        UItoolKit.showToastShort(getActivity(), "没有找到相关的人,可能您要找的人已离职");
                    }
                } else {
                    UItoolKit.showToastShort(getActivity(), "网络错误");
                }
            }
        });
    }

    @Override
    public void getChatWindowInfoSuccess(final ChatWindowInfoBean windowInfoBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                startChatActivity(windowInfoBean, vChatBean);
            }
        });
    }

    @Override
    public void createChatSuccess(final VChatBean vChatBean, final ChatWindowInfoBean windowInfoBean) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                if (vChatBean == null) {
                    if (AddressBookFragment.this.vChatBean != null) {
                        startChatActivity(windowInfoBean, AddressBookFragment.this.vChatBean);
                    } else {
                        UItoolKit.showToastShort(getActivity(), "创建聊天出错");
                    }
                } else {
                    if (windowInfoBean != null)
                        startChatActivity(windowInfoBean, vChatBean);
                    else
                        UItoolKit.showToastShort(getActivity(), "创建聊天出错");

                }
            }
        });
    }

    private void startChatActivity(ChatWindowInfoBean windowInfoBean, VChatBean vChatBean) {
        Intent intent = new Intent(getActivity(), ChatActivityNew.class);
        intent.putExtra(ChatActivityNew.CHAT_WINDOW_INFO, windowInfoBean);
        intent.putExtra(ChatActivityNew.VCHAT_BEAN, vChatBean);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void searchUserInfoSuccess(ArrayList<UserInfoBean> userList) {
        if (userList != null) {
            GroupStores.getInstance().createNewChat(userList, "", ADDRESS_BOOK_CHAT_TAG);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissProgressDialog();
                    UItoolKit.showToastShort(getActivity(), "获取用户信息失败,无法创建聊天");
                }
            });
        }
    }


    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = CommonDialog.getInstance("正在创建聊天...");
            progressDialog.setCancelable(false);
            progressDialog.show(getFragmentManager(), null);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void setSearchSuggestCursor(MatrixCursor cursor) {
        CursorAdapter adapter = searchView.getSuggestionsAdapter();
        adapter.changeCursor(cursor);
    }

    @Override
    public void onItemClick(String email) {
        ApplicationStores.getInstance().viewMobileNum(email);
    }

    @Override
    public void onMobilePhoneClick(String phoneNum) {
        CommonUtils.callNum(phoneNum, getActivity());
    }

    @Override
    public void onTelClick(String telNum) {
        CommonUtils.callNum(telNum, getActivity());
    }

    @Override
    public void onSendMsgClick(String email) {
        showProgressDialog();
        String userId = email.split("@")[0].toLowerCase();
        Log.i(TAG, "onSendMsgClick: " + userId);
        vChatBean = MessageStores.getInstance().isSingleChatExist(userId);
        if (vChatBean == null) {
            GroupStores.getInstance().getUserInfoById(userId);
        } else {
            MessageStores.getInstance().getNormalChatWindowInfo(vChatBean, ADDRESS_BOOK_CHAT_TAG);
        }
    }

    @Override
    public void onSendEmailClick(String email,String name) {
        Log.i(TAG, "onSendEmailClick: email="+email+", name="+name);
        Intent intent = new Intent(getActivity(), WriteMailActivity.class);
        intent.putExtra("type", 0);
        intent.putExtra("to", WeiYouUtil.getUserJSON(email, name));
        startActivity(intent);
    }

    private static class AddressBookHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ApplicationStores.getInstance().searchPerson((String) msg.obj);
                    break;
            }
        }
    }
}
