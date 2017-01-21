package com.inspur.playwork.weiyou.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailAttachmentAdapter;
import com.inspur.playwork.weiyou.adapter.MailDetailPagerAdapter;
import com.inspur.playwork.weiyou.store.MailDetailOperation;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;
import com.inspur.playwork.weiyou.view.InsideListView;
import com.inspur.playwork.weiyou.view.VUConfirmDialog;
import com.inspur.playwork.weiyou.view.WordWrapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 孙 on 2015/11/16 0016.
 */
public class MailDetailFragment extends Fragment implements ViewPager.OnPageChangeListener,MailDetailOperation,VUConfirmDialog.ConfirmDialogListener{
    private static final String TAG = "MailDetailFragment-->";
//    private static final int DOWNLOAD_MAIL_OVER = 0x20;
//    private static final int DOWNLOAD_WP_ATTACHEMNT_SUCCESS = 0x03;
//    private static final int DOWNLOAD_ATTACHEMNT_FAIL = 0x00;
//    private ServiceHandler handler;
    private WeiYouMainActivity wyma;
    private ViewPager mdViewPager;
    private ArrayList<View> mdViewList;
    private MailDetailPagerAdapter mdpAdapter;
    private InsideListView currAttachmentLV;
    private WebView currContentWV;
    private View hideView;
    public VUConfirmDialog downloadConfirmDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        handler = new ServiceHandler(new WeakReference<>(this));
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setMailDetailReference(this);
//        Log.i(TAG, "onCreate: MailDetailFragment onCreate----");
        mdViewList = new ArrayList<>();
        wyma.vuStores.initMailDetailData();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wy_fragment_mail_detail, container, false);
//        Log.i(TAG, "onCreateView: MailDetailFragment onCreateView------------");
        initViewPager(v);

        downloadConfirmDialog = new VUConfirmDialog(wyma,"当前未连接WIFI,要下载的邮件大于5MB，是否继续下载？","不下载了","继续下载");
        downloadConfirmDialog.setOutsideTouchDisable();
        downloadConfirmDialog.setConfirmDialogListener(this);
        hideView = v.findViewById(R.id.md_hideView);
        return v;
    }

//    public void getMailDetailByMessageIDCallback(SparseArray mlSA) {
//        MailDetail mailDetailFromDb = (MailDetail) mlSA.get(0);
//        logi(mailDetailFromDb.getSubject() + "------getMailDetailByMessageIDCallback-------" + mailDetailFromDb.getUid());
//        refreshCurrentView(mailDetailFromDb, true);
//    }
    @Override
    public void addViewToViewList(){
        mdViewList.add(new View(wyma));
    }

    public void initViewPager(View v) {
        mdViewPager = (ViewPager) v.findViewById(R.id.mail_detail_viewpager);
        int count = wyma.vuStores.showingMailIdList.size();
        int l = count>5?5:count;
        for (int i = 0; i < l; i++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.wy_view_mail_detail, null);
            mdViewList.add(view);
        }
        mdpAdapter = new MailDetailPagerAdapter();
        mdpAdapter.setViewList(mdViewList);
        mdpAdapter.setCount(count);
        mdViewPager.setAdapter(mdpAdapter);
//        logi(currIndex + "---setCurrentItem---" + currMail.getSubject());
        mdViewPager.setCurrentItem(wyma.vuStores.getCurrIndex());
        mdViewPager.addOnPageChangeListener(this);
        wyma.vuStores.requestMailDetail();
    }

    @Override
    public void renderCurrentView(final MailDetail md){
//        此处删附件···
//        FileUtil.getCurrMailAttachmentsPath(email)

        final View currView = mdViewList.get(wyma.vuStores.getCurrIndex() % 5);
        String senderText = "";
        try {
            JSONObject from = new JSONObject(md.getFrom());
            senderText = from.getString("name");
            if (senderText.length() == 0) {
                senderText = from.getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        显示邮件发件人、主题、发送时间
        ((TextView) currView.findViewById(R.id.md_sender_text)).setText(senderText);
        ((TextView) currView.findViewById(R.id.md_subject)).setText(md.getSubject());
        ((TextView) currView.findViewById(R.id.md_time_text)).setText(md.getCreateTime().toLocaleString());

//        显示联系人
        TextView ccTitle = (TextView) currView.findViewById(R.id.md_cc_title);
        final WordWrapView toTVs = (WordWrapView) currView.findViewById(R.id.md_receiver_tvs);
        final WordWrapView ccTVs = (WordWrapView) currView.findViewById(R.id.md_cc_tvs);
        final TextView toFoldingTV = (TextView) currView.findViewById(R.id.md_to_folding);
        final TextView ccFoldingTV = (TextView) currView.findViewById(R.id.md_cc_folding);
        final TextView toMaskTV = (TextView) currView.findViewById(R.id.md_to_mask);
        final TextView ccMaskTV = (TextView) currView.findViewById(R.id.md_cc_mask);
        WeiYouUtil.fillWordWrapView(toTVs, md.getTo(), wyma,false);
        WeiYouUtil.fillWordWrapView(ccTVs, md.getCc(), wyma,true);
        toTVs.post(new Runnable() {
            @Override
            public void run() {
                final int toLayoutHeight = toTVs.getHeight();
//                Log.i(TAG, "run: toTVs toLayoutHeight = "+ toLayoutHeight);
                if(toTVs.isExpandable()){
                    toFoldingTV.setVisibility(View.VISIBLE);
                    toFoldingTV.setText(R.string.unfold_rcpts);
                    toMaskTV.setVisibility(View.VISIBLE);
//                    toTVs.foldView();
                    toFoldingTV.setOnClickListener(new View.OnClickListener() {
                        boolean isExpand = false;
                        @Override
                        public void onClick(View v) {
                            isExpand = !isExpand;
                            if(isExpand) {
                                toTVs.expandView();
                                toFoldingTV.setText(R.string.fold_rcpts);
                                toMaskTV.setVisibility(View.GONE);
                            }
                            else {
                                toTVs.foldView();
                                toFoldingTV.setText(R.string.unfold_rcpts);
                                toMaskTV.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else{
                    toFoldingTV.setVisibility(View.GONE);
                    toMaskTV.setVisibility(View.GONE);
                }
            }
        });

        ccTVs.post(new Runnable() {
            @Override
            public void run() {
                final int ccLayoutHeight = ccTVs.getHeight();
//                Log.i(TAG, "run: ccTVs ccLayoutHeight = "+ ccLayoutHeight);
                if(ccTVs.isExpandable()){
                    ccFoldingTV.setVisibility(View.VISIBLE);
                    ccFoldingTV.setText(R.string.unfold_rcpts);
                    ccMaskTV.setVisibility(View.VISIBLE);
                    ccTVs.foldView();
                    ccFoldingTV.setOnClickListener(new View.OnClickListener() {
                        boolean isExpand = false;
                        @Override
                        public void onClick(View v) {
                            isExpand = !isExpand;
                            if(isExpand) {
                                ccTVs.expandView();
                                ccFoldingTV.setText(R.string.fold_rcpts);
                                ccMaskTV.setVisibility(View.GONE);
                            }
                            else {
                                ccTVs.foldView();
                                ccFoldingTV.setText(R.string.unfold_rcpts);
                                ccMaskTV.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else{
                    ccFoldingTV.setVisibility(View.GONE);
                    ccMaskTV.setVisibility(View.GONE);
                }
            }
        });

//        邮件操作按钮
        currView.findViewById(R.id.wy_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            wyma.onBackPressed();
            }
        });
        currView.findViewById(R.id.md_reply_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyma.innerWriteMail(WeiYouMainActivity.QUOTE_TYPE_REPLY);
            }
        });
        currView.findViewById(R.id.md_reply_all_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyma.innerWriteMail(WeiYouMainActivity.QUOTE_TYPE_REPLY_ALL);
            }
        });
        currView.findViewById(R.id.md_forward_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyma.innerWriteMail(WeiYouMainActivity.QUOTE_TYPE_FORWARD);
            }
        });
        currView.findViewById(R.id.md_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyma.vuStores.deleteMail();
//                mdViewList.remove(wyma.vuStores.getCurrIndex());
//                mdpAdapter.notifyDataSetChanged();
                wyma.onBackPressed();//所有邮件都删除完了，则返回邮件列表页

            }
        });

        currContentWV = (WebView) currView.findViewById(R.id.md_content_wv);
        WebSettings ws = currContentWV.getSettings();
        //设置WebView的一些缩放功能点
        currContentWV.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        currContentWV.setHorizontalScrollBarEnabled(false);
//        ws.setSupportZoom(true);
        //设置WebView可触摸放大缩小
//        ws.setBuiltInZoomControls(true);
//        currContentWV.setInitialScale(70);
        currContentWV.setHorizontalScrollbarOverlay(true);
        //WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
        //ws.setUseWideViewPort(true);
        //提高渲染的优先级
//        ws.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //允许JS执行
//        ws.setJavaScriptEnabled(true);
        //把图片加载放在最后来加载渲染
        //ws.setBlockNetworkImage(true);
        //listview,webview中滚动拖动到顶部或者底部时的阴影
        currContentWV.setOverScrollMode(View.OVER_SCROLL_NEVER);
        //ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//使用缓存
        //ws.setCacheMode(WebSettings.LOAD_NO_CACHE); //默认不使用缓存！
        currContentWV.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); //取消滚动条白边效果
        currContentWV.setWebViewClient(new MyWebViewClient());

//      显示附件
        currAttachmentLV = (InsideListView) currView.findViewById(R.id.md_attachment_lv);
//        是加密邮件的话 显示 加密图标
        currView.findViewById(R.id.md_encrypt_icon).setVisibility(md.getEncrypted()?View.VISIBLE:View.GONE);
        currView.findViewById(R.id.md_signed_icon).setVisibility(md.getSigned()?View.VISIBLE:View.GONE);
        mdViewList.set(wyma.vuStores.getCurrIndex()%5, currView);
        mdpAdapter.notifyDataSetChanged();

        currView.post(new Runnable() {
            @Override
            public void run() {
                wyma.vuStores.renderMailDetail(currContentWV);
            }
        });
    }

    /**
     * 显示附件
     */
    @Override
    public void renderAttachmentList(final List<MailAttachment> currAttachmentList){
        if (currAttachmentList != null && currAttachmentList.size() > 0) {
            currAttachmentLV.setVisibility(View.VISIBLE);
    //        logi("currAttachmentList.size()--->" + wyma.currAttachmentList.size());
            MailAttachmentAdapter mdaa = new MailAttachmentAdapter(wyma, currAttachmentList, false,false);
            currAttachmentLV.setAdapter(mdaa);
            //点击打开附件
            currAttachmentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MailAttachment ma = currAttachmentList.get(position);
                    String amPath = ma.getPath();
                    Intent intent = FileUtil.getOpenFileIntent(getActivity(),amPath);
                    if (intent == null) {
                        UItoolKit.showToastShort(getActivity(), "未识别的文件类型");
                    } else {
                        try {
                            getActivity().startActivity(intent);
                        } catch (Exception e) {
                            UItoolKit.showToastShort(getActivity(), "没有找到应用程序打开该类型的文件");
                        }
                    }
                }
            });
            currAttachmentLV.setFocusable(false);//解决ScrollView内嵌ListView的问题
            WeiYouUtil.setListViewHeightBasedOnChildren(currAttachmentLV);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        wyma.vuStores.onPageChanged(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //    重写WebViewClient 页面加载完前显示加载中动画
    class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(wyma!=null) {
                wyma.dismissProgressDialog();
                wyma.toast("加载失败");
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "shouldOverrideUrlLoading: currContentWV 超链接:" + url);
            Uri uri = Uri.parse(url);
            if(wyma!=null) {
                if (url.startsWith("mailto:")) {
                    String _email = url.split("mailto:")[1];
                    Intent intent = new Intent();
                    intent.putExtra("type",0);
                    intent.putExtra("to",WeiYouUtil.getUserJSON(_email,null));
                    wyma.gotoWriteMail(intent);//写邮件
                } else if (url.startsWith("http") || url.startsWith("https")) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(uri);
                    startActivity(i);
                }
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(wyma!=null) {wyma.dismissProgressDialog();}
        }
    }

    @Override
    public void onButton1Click() {
        Log.i(TAG, "onButton1Click: cancelDownloadMail");
        wyma.vuStores.cancelDownloadMail();
    }

    @Override
    public void onButton2Click() {
        Log.i(TAG, "onButton2Click: continueDownloadMail");
        wyma.vuStores.continueDownloadMail();
    }

    @Override
    public void showDownloadComfirmDialog() {
        downloadConfirmDialog.showPopWindow(hideView);
    }


//    //下载微盘附件件
//    private void downloadMailAttachment(String url, final String filePath) throws Exception {
//        wyma.toast("正在下载附件...");
//        OkHttpClientManager.getInstance().downLoadWPFile(url, filePath, new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                Log.i(TAG, "downloadMailAttachment onFailure");
//                handler.sendMessage(handler.obtainMessage(DOWNLOAD_ATTACHEMNT_FAIL));
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                Log.i(TAG, "downloadMailAttachment onResponse");
//                if (response.isSuccessful()) {
//                    String fileName = response.header("Content-Disposition").split("=")[1];
//                    handler.sendMessage(handler.obtainMessage(DOWNLOAD_WP_ATTACHEMNT_SUCCESS, filePath + fileName));
//                }
//            }
//        });
//    }
//
//    private static class ServiceHandler extends Handler {
//
//        private WeakReference<MailDetailFragment> reference;
//
//        public ServiceHandler(WeakReference<MailDetailFragment> reference) {
//            this.reference = reference;
//        }
//
//        @Override
//        public void dispatchMessage(@NonNull Message msg) {
//            MailDetailFragment mdf = reference.get();
//            switch (msg.what) {
//                case DOWNLOAD_ATTACHEMNT_FAIL:
//                    mdf.wyma.toast("下载微盘附件失败");
//                    break;
//                case DOWNLOAD_WP_ATTACHEMNT_SUCCESS:
//                    Log.i(TAG, "startActivity filePath=" + msg.obj);
//                    mdf.getActivity().startActivity(VUFileUtil.openFile((String) msg.obj));
//                    break;
//                case DOWNLOAD_MAIL_OVER:
////                    处理邮件下载结果
////                    mdf.parseCkEmail((CkEmail) msg.obj);
//                    break;
//            }
//        }
//    }


}
