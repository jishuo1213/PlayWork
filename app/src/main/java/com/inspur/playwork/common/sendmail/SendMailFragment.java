package com.inspur.playwork.common.sendmail;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.common.chosefile.ChoseFileDialogFragment;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.OkHttpClientManager;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.LoadFileHandlerThread;
import com.inspur.playwork.utils.loadfile.LoadFileManager;
import com.inspur.playwork.view.common.progressbar.CommonDialog;
import com.inspur.playwork.view.message.chat.ChatActivityNew;
import com.inspur.playwork.view.timeline.taskattachment.AttachmentRecyclerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Bugcode on 2016/3/16.
 */
public class SendMailFragment extends Fragment implements SendMailView, View.OnClickListener, RecipientFragment.RecipientFinishListener, ChoseFileDialogFragment.ChoseFileResListener {

    private static final String SMALL_MAIL = "smallMail";
    private static final String CHAT_INFO = "chatInfo";
    private static final String TASK_INFO = "taskInfo";

    private View rootView;
    private TextView recipientTextView;
    private TextView subjectTextView;
    private ImageView addRecipientImageView;
    private EditText msgBodyEditText;
    private WebView msgBodyWebView;
    private RecyclerView attchmentRecyview;
    private ImageView attachmentImageView;

    private SendMailPresenter sendMailPresenter;
    private SmallMailBean smallMail;
    private SendMailFinishListener sendMailFinishListener;
    private String historyContent = "";
    private DialogFragment progressDialog;

    private Set<String> downLoadSet = new HashSet<>();

    private Handler handler;

    public interface SendMailFinishListener {
        void sendMailSuccess();
    }

    public static SendMailFragment getInstance(SmallMailBean smallMail, ChatWindowInfoBean chatInfo, TaskBean taskInfo) {
        SendMailFragment sendMailFragment = new SendMailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SMALL_MAIL, smallMail);
        bundle.putParcelable(CHAT_INFO, chatInfo);
        bundle.putParcelable(TASK_INFO, taskInfo);
        sendMailFragment.setArguments(bundle);
        return sendMailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.smallMail = getArguments().getParcelable(SMALL_MAIL);
        ChatWindowInfoBean chatInfo = getArguments().getParcelable(CHAT_INFO);
        TaskBean taskInfo = getArguments().getParcelable(TASK_INFO);

        sendMailPresenter = new SendMailPresenterImpl(this, chatInfo, taskInfo);
        sendMailPresenter.register();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_sendmail, container, false);
        this.initView(rootView);
        this.initData();
        return rootView;
    }

    private void initView(View rootView) {
        progressDialog = CommonDialog.getInstance("发送中", true);
        recipientTextView = (TextView) rootView.findViewById(R.id.recipient_text_view);
        recipientTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        subjectTextView = (TextView) rootView.findViewById(R.id.subject_text_view);
        addRecipientImageView = (ImageView) rootView.findViewById(R.id.add_recipient_image_view);
        addRecipientImageView.setOnClickListener(this);
        attachmentImageView = (ImageView) rootView.findViewById(R.id.attachment_image_view);
        attachmentImageView.setOnClickListener(this);
        msgBodyEditText = (EditText) rootView.findViewById(R.id.msg_body_edit_text);
        msgBodyWebView = (WebView) rootView.findViewById(R.id.msg_body_web_view);
        attchmentRecyview = (RecyclerView) rootView.findViewById(R.id.recy_small_mail_attachment);
    }

    private void initData() {
        handler = new SendMailHandler(this);
        recipientTextView.setText(smallMail.getToUserNames());
        subjectTextView.setText(smallMail.subject);

        if (smallMail.type == SmallMailBean.SEND_MODE) {
            this._setMsgBodyView("", "", View.VISIBLE, View.GONE);
        } else if (smallMail.type == SmallMailBean.REPLY_MODE) {
            historyContent = sendMailPresenter.getHistoryContent(smallMail);
            this._setMsgBodyView("", historyContent, View.VISIBLE, View.VISIBLE);
            if (smallMail.attchments != null)
                smallMail.attchments.clear();
        } else if (smallMail.type == SmallMailBean.VIEW_MODE) {
            this._setMsgBodyView("", smallMail.content, View.GONE, View.VISIBLE);
            attachmentImageView.setVisibility(View.GONE);
            if (smallMail.attchments.size() > 0) {
                initAttchmentList();
            } else {
                attchmentRecyview.setVisibility(View.GONE);
            }
        }
    }

    private void initAttchmentList() {
        File file = new File(smallMail.getAttachmentFloder());
        if (!file.exists()) {
            boolean res = file.mkdir();
            if (!res)
                return;
        }
        attchmentRecyview.setVisibility(View.VISIBLE);
        attchmentRecyview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        AttachmentRecyclerAdapter attachmentRecyclerAdapter = new AttachmentRecyclerAdapter(attchmentRecyview);
        attachmentRecyclerAdapter.setAttachmentList(smallMail.attchments);
        attchmentRecyview.setAdapter(attachmentRecyclerAdapter);
    }

    private void _setMsgBodyView(String content, String history, int editVis, int webVis) {
        msgBodyEditText.setText(content);
        msgBodyWebView.loadData(history, "text/html; charset=utf-8", null);
        msgBodyEditText.setVisibility(editVis);
        msgBodyWebView.setVisibility(webVis);

        addRecipientImageView.setVisibility(editVis);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_text_view:
                break;
            case R.id.cancel_text_view:
                break;
            case R.id.add_recipient_image_view:
                this.showContactsFragment();
                break;
            case R.id.attachment_image_view:
                if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 103))
                    openFile();
                break;
            default:
                break;
        }
    }

    public void openFile() {
        ChoseFileDialogFragment dialogFragment = new ChoseFileDialogFragment();
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setListener(this);
    }

    public void clickOkBtn() {
        this.hideInputMethod();

        if (smallMail.toUserList.size() == 0) {
            UItoolKit.showToastShort(getActivity(), "收件人不能为空");
            return;
        }

        String bodyContent = msgBodyEditText.getText().toString().replace("\n", "</br>");
        if (TextUtils.isEmpty(bodyContent)) {
            UItoolKit.showToastShort(getActivity(), "邮件内容不能为空");
            return;
        }

        showDialog();

        if (choseFileList != null && choseFileList.size() > 0) {
//            PlayWorkApplication app = (PlayWorkApplication) getActivity().getApplication();
            LoadFileManager loadFileManager = LoadFileManager.findOrCreateRetainFragment(getFragmentManager());
            for (LocalFileBean mBean : choseFileList) {
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("user_id", PreferencesHelper.getInstance().getCurrentUser().id),
                        new OkHttpClientManager.Param("system_name", "weiyou"),
                };
                downLoadSet.add(mBean.currentPath);
                loadFileManager.upLoadFile(AppConfig.UPLOAD_FILE_URI, mBean.currentPath, params, mBean.currentPath, handler, true, LoadFileManager.UPLOAD_MSG_ATTACHMENT);
            }
            return;
        }
        sendMailPresenter.sendMail(smallMail, bodyContent, historyContent);
    }

    private void showContactsFragment() {
        RecipientFragment recipientFragment = new RecipientFragment();
        recipientFragment.setRecipientList(smallMail.toUserList);
        recipientFragment.setRecipientFinishListener(this);
        getFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, recipientFragment).addToBackStack(null).commit();
        ((ChatActivityNew) getActivity()).setIsRecipientFragmentShow(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendMailPresenter.unregister();
    }

    @Override
    public void showDialog() {
        progressDialog.show(getFragmentManager(), null);
    }

    @Override
    public void dismissDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void finished() {
        getFragmentManager().popBackStack();
        sendMailFinishListener.sendMailSuccess();
    }

    @Override
    public void updateRecipient(ArrayList<UserInfoBean> recipientList) {
        smallMail.toUserList = recipientList;
        recipientTextView.setText(smallMail.getToUserNames());
    }

    private void hideInputMethod() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setSendMailFinishListener(SendMailFinishListener sendMailFinishListener) {
        this.sendMailFinishListener = sendMailFinishListener;
    }

    public int getMode() {
        return smallMail.type;
    }


    private ArrayList<LocalFileBean> choseFileList;

    @Override
    public void onFileSelect(ArrayList<LocalFileBean> choseFileList) {

        if (this.choseFileList == null)
            this.choseFileList = choseFileList;
        else {
            this.choseFileList.addAll(choseFileList);
        }
        AttachmentRecyclerAdapter adapter = (AttachmentRecyclerAdapter) attchmentRecyview.getAdapter();
        if (attchmentRecyview.getVisibility() == View.GONE && adapter == null) {
            attchmentRecyview.setVisibility(View.VISIBLE);
            adapter = new AttachmentRecyclerAdapter(attchmentRecyview);
            attchmentRecyview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            adapter.setLocalFileList(choseFileList, false);
            attchmentRecyview.setAdapter(adapter);
        } else {
            adapter.setLocalFileList(choseFileList, true);
        }
    }

    public void upLoadOneFileSuccess(String clientId, String response) {
        Log.d(TAG, downLoadSet.size() + "upLoadOneFileSuccess() called with: " + "clientId = [" + clientId + "], response = [" + response + "]");
        AttachmentRecyclerAdapter adapter = (AttachmentRecyclerAdapter) attchmentRecyview.getAdapter();
        LocalFileBean localFileBean = adapter.updateFileByPath(clientId, true);
        try {
            JSONObject jsonObject = new JSONObject(response);
            TaskAttachmentBean taskAttachmentBean = new TaskAttachmentBean(localFileBean, jsonObject.optString("docid"));
            if (smallMail.attchments == null) {
                smallMail.attchments = new ArrayList<>();
            }
            smallMail.attchments.add(taskAttachmentBean);
            downLoadSet.remove(clientId);
            if (downLoadSet.size() == 0) {
                sendMailPresenter.sendMail(smallMail, msgBodyEditText.getText().toString(), historyContent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void upLoadOneFileFailure(String clientId) {
        AttachmentRecyclerAdapter adapter = (AttachmentRecyclerAdapter) attchmentRecyview.getAdapter();
        adapter.updateFileByPath(clientId, false);
        downLoadSet.remove(clientId);
        if (downLoadSet.size() == 0) {
            sendMailPresenter.sendMail(smallMail, msgBodyEditText.getText().toString(), historyContent);
        }
    }


    private void updateProgress(String clientId, int progress) {
        AttachmentRecyclerAdapter adapter = (AttachmentRecyclerAdapter) attchmentRecyview.getAdapter();
        adapter.updateProgress(clientId, progress);
    }

    private static class SendMailHandler extends Handler {

        private WeakReference<SendMailFragment> sendMailFragmentWeakReference;

        public SendMailHandler(SendMailFragment sendMailFragmen) {
            this.sendMailFragmentWeakReference = new WeakReference<>(sendMailFragmen);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case LoadFileHandlerThread.UPDATE_UPLOAD_PROGRESS: {
                    String clientId = (String) msg.obj;
                    int progress = msg.arg1;
                    sendMailFragmentWeakReference.get().updateProgress(clientId, progress);
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_SUCCESS: {
                    Bundle res = msg.peekData();
                    String clientId = (String) msg.obj;
                    String response = res.getString(LoadFileHandlerThread.UPLOAD_SUCCESS_RESPONSE);
                    sendMailFragmentWeakReference.get().upLoadOneFileSuccess(clientId, response);
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_FAILURE:
                    String clientId = (String) msg.obj;
                    sendMailFragmentWeakReference.get().upLoadOneFileFailure(clientId);
                    break;
            }
        }

    }

    private static final String TAG = "SendMailFragment";
}
