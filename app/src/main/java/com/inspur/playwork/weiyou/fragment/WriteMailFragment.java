package com.inspur.playwork.weiyou.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.common.chosefile.ChoseFileDialogFragment;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.view.common.chosepicture.ChosePictureFragment;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.adapter.MailAttachmentAdapter;
import com.inspur.playwork.weiyou.adapter.SearchPersonAdapter;
import com.inspur.playwork.weiyou.store.VUStores;
import com.inspur.playwork.weiyou.store.WriteMailOperation;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;
import com.inspur.playwork.weiyou.view.ColorPickerDialog;
import com.inspur.playwork.weiyou.view.ContactInputView;
import com.inspur.playwork.weiyou.view.FlowLayout;
import com.inspur.playwork.weiyou.view.FontSizeSelectorSpinner;
import com.inspur.playwork.weiyou.view.InsideListView;
import com.inspur.playwork.weiyou.view.VUConfirmDialog;
import com.inspur.playwork.weiyou.view.VUFileSelectorDialog;
import com.inspur.playwork.weiyou.view.VUNoPbkWarnDialog;
import com.inspur.playwork.weiyou.view.VURemindSubjectDialog;
import com.inspur.playwork.weiyou.view.ass.FontSizeSpinnerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by 孙 on 2015/11/16 0016.
 */
public class WriteMailFragment extends Fragment implements WriteMailOperation, OnClickListener,
        MailAttachmentAdapter.RemoveButtonListener, ChosePictureFragment.SelectedPicureListener, ChoseFileDialogFragment.ChoseFileResListener,
        VUConfirmDialog.ConfirmDialogListener, VURemindSubjectDialog.ConfirmDialogListener, VUFileSelectorDialog.ConfirmDialogListener,
        VUNoPbkWarnDialog.NoPbkWarnDialogListener, SelectLocalAttachmentFragment.SelectLocalAttachmentListener {
    private static final String TAG = "WriteMailFragment-->";

//    private static final int FILE_UPLOAD_FAILURE = 0x00;
//    private static final int FILE_UPLOAD_PROGRESS = 0x02;
//    private static final int FILE_UPLOAD_SUCCESS = 0x01;

    private static final int ATTACHMENT_REQUEST_CODE = 1;
    private static final int IMAGE_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 3;
    private static final int MUSIC_REQUEST_CODE = 4;
    private static final int VIDEO_REQUEST_CODE = 5;

    //    private AttachmentUploadHandler auHandler;
    private WeiYouMainActivity wyma;

    private View preSelectedView;
    private SearchPersonAdapter currSPA;
    private SearchPersonAdapter toSPA;
    private SearchPersonAdapter ccSPA;

    private TextView hideTV;
    private VURemindSubjectDialog remindSubjectDialog;
    private VUConfirmDialog vuConfirmDialog;
    private VUFileSelectorDialog vuFileSelectorDialog;
    private VUNoPbkWarnDialog vuNoPbkWarnDialog;

    private FlowLayout toCIV;
    private FlowLayout ccCIV;
    public FlowLayout currFocusingCIV;
//    private ContactInputView toCIV;
//    private ContactInputView ccCIV;
//    public ContactInputView currFocusingCIV;

    private AutoCompleteTextView toACTV;
    private AutoCompleteTextView ccACTV;
    //    private AutoCompleteTextView scACTV;
    private AutoCompleteTextView currFocusingACTV;

    private Button cancelSendMailBtn;
    private Button sendMailBtn;

    private ImageView openToSelectorBtn;
    private ImageView openCcSelectorBtn;
    private ImageView openScSelectorBtn;
    private ImageView openAttachmentSelectorBtn;
    private InsideListView attachmentLV;
    private TextView subjectTV;
    private RichEditor contentRichEditor;
    private HorizontalScrollView editorToolsLL;
//    private WebView quoteMailWV;

    private int currFontColor = 0;
    private int currFontBackgroundColor = 0;
    private List<Integer> fontSizeList;

    private FontSizeSelectorSpinner fontSizeSpinner;
    private MailAttachmentAdapter maAdapter;
    private SelectLocalAttachmentFragment selectLocalAttachmentFragment;

    public boolean isInsertImage = false;//是否要插入图片

    private PopupWindow safetyOptPW;
    private CheckBox encryptCB;
    private CheckBox signCB;

    private WriteMailHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        wyma = (WeiYouMainActivity) getActivity();
        wyma.vuStores.setWriteMailReference(this);
        super.onCreate(savedInstanceState);
        handler = new WriteMailHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "WriteMailFragment onCreateView");
        View v = inflater.inflate(R.layout.wy_fragment_write_mail, container, false);
//        auHandler = new AttachmentUploadHandler(new WeakReference<>(this));
        initView(v);
        vuConfirmDialog = new VUConfirmDialog(wyma, "是否保存草稿", "不保存", "保存");
        vuConfirmDialog.setConfirmDialogListener(this);
        remindSubjectDialog = new VURemindSubjectDialog(wyma);
        remindSubjectDialog.setConfirmDialogListener(this);
        vuFileSelectorDialog = new VUFileSelectorDialog(wyma, this);
        selectLocalAttachmentFragment = new SelectLocalAttachmentFragment();
        selectLocalAttachmentFragment.setOnFinishSelectListener(this);

        initPopuptWindow();
        wyma.vuStores.initWriteMailData();
        initAttachmentListView();
        return v;
    }

    private void initView(View v) {
//        initPopWindow(v);
        editorToolsLL = (HorizontalScrollView) v.findViewById(R.id.wm_editor_tools);

//        toCIV = (ContactInputView) v.findViewById(R.id.wm_to_civ);
//        ccCIV = (ContactInputView) v.findViewById(R.id.wm_cc_civ);
        toCIV = (FlowLayout) v.findViewById(R.id.wm_to_civ);
        ccCIV = (FlowLayout) v.findViewById(R.id.wm_cc_civ);
//        scCIV = (ContactInputView) v.findViewById(R.id.wm_sc_civ);
        toCIV.setOnClickListener(this);
        ccCIV.setOnClickListener(this);
//        scCIV.setOnClickListener(civClickListener);

        toACTV = (AutoCompleteTextView) v.findViewById(R.id.wm_to_actv);
        ccACTV = (AutoCompleteTextView) v.findViewById(R.id.wm_cc_actv);
//        scACTV = (AutoCompleteTextView) v.findViewById(R.id.wm_sc_actv);
//        scACTV.setOnKeyListener(actvOKL);

        //初始化取消、发送、选人、选附件按钮，及其点击事件
        cancelSendMailBtn = (Button) v.findViewById(R.id.cancel_send_mail_btn);
        sendMailBtn = (Button) v.findViewById(R.id.send_mail_ok_btn);
        v.findViewById(R.id.wm_safty_opt_btn).setOnClickListener(this);
        openToSelectorBtn = (ImageView) v.findViewById(R.id.wm_to_selector_btn);
        openCcSelectorBtn = (ImageView) v.findViewById(R.id.wm_cc_selector_btn);
        openScSelectorBtn = (ImageView) v.findViewById(R.id.wm_sc_selector_btn);
        openAttachmentSelectorBtn = (ImageView) v.findViewById(R.id.wm_attachment_btn);

        cancelSendMailBtn.setOnClickListener(this);
        sendMailBtn.setOnClickListener(this);
        openToSelectorBtn.setOnClickListener(this);
        openCcSelectorBtn.setOnClickListener(this);
        openScSelectorBtn.setOnClickListener(this);
        openAttachmentSelectorBtn.setOnClickListener(this);

        attachmentLV = (InsideListView) v.findViewById(R.id.wm_attachment_lv);
        //手动调整listview高度（为了兼容scrollview内嵌套的listView）
        WeiYouUtil.setListViewHeightBasedOnChildren(attachmentLV);

        subjectTV = (TextView) v.findViewById(R.id.wm_subject_et);
//        quoteMailWV = (WebView) v.findViewById(R.id.wm_quote_mail_wv);
        contentRichEditor = (RichEditor) v.findViewById(R.id.wm_content_et);
        contentRichEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    editorToolsLL.setVisibility(View.VISIBLE);
                } else
                    editorToolsLL.setVisibility(View.GONE);
            }
        });

//        返回上一步
        v.findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.undo();
            }
        });
//        重做
        v.findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.redo();
            }
        });
//        加粗
        v.findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setBold();
            }
        });
//        斜体
        v.findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setItalic();
            }
        });
//        字体颜色
        v.findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                ColorPickerDialog dialog = new ColorPickerDialog(wyma, currFontColor, "选择字体颜色",
                        new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                ((TextView) view).setTextColor(color);
                                contentRichEditor.setTextColor(color);
                                currFontColor = color;
                            }
                        });
                dialog.show();
            }
        });
//        背景颜色
        v.findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ColorPickerDialog dialog = new ColorPickerDialog(wyma, currFontBackgroundColor, "选择背景颜色",
                        new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                v.setBackgroundColor(color);
                                contentRichEditor.setTextBackgroundColor(color);
                                currFontBackgroundColor = color;
                            }
                        });
                dialog.show();
            }
        });

        fontSizeSpinner = new FontSizeSelectorSpinner(wyma);
        fontSizeList = new ArrayList<>();
        fontSizeList.add(12);
        fontSizeList.add(14);
        fontSizeList.add(16);
        fontSizeList.add(18);
        fontSizeList.add(22);
        fontSizeList.add(24);
        fontSizeSpinner.refreshData(fontSizeList, 0);
        fontSizeSpinner.setItemListener(new FontSizeSpinnerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                int fz = fontSizeList.get(pos);
                ((TextView) editorToolsLL.findViewById(R.id.action_font_size)).setText(fz + "号");
                contentRichEditor.setEditorFontSize(fz);
            }
        });
//        字体大小
        v.findViewById(R.id.action_font_size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                contentRichEditor.setEditorFontSize();
                fontSizeSpinner.setWidth(view.getWidth());
                fontSizeSpinner.showAsDropDown(view);
            }
        });
//        删除线
        v.findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setStrikeThrough();
            }
        });
//        下划线
        v.findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setUnderline();
            }
        });

//        缩进
        v.findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setIndent();
            }
        });
//        反缩进
        v.findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setOutdent();
            }
        });
//        居左
        v.findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignLeft();
            }
        });
//        居中
        v.findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignCenter();
            }
        });
//        居右
        v.findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignRight();
            }
        });
//        引号
        v.findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setBlockquote();
            }
        });
//        插入图片
        v.findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 105)) {
                    isInsertImage = true;
                    wyma.showImageSelector();
                }
            }
        });
//        插入超链接
        v.findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                contentRichEditor.insertLink("https://github.com/wasabeef", "wasabeef");
            }
        });
//        下标
        v.findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setSubscript();
            }
        });
//        上标
        v.findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setSuperscript();
            }
        });
        hideTV = (TextView) v.findViewById(R.id.wm_hide_tv);
    }

    private void initAttachmentListView() {
        maAdapter = new MailAttachmentAdapter(wyma, wyma.vuStores.getDraftAttachmentList(), true, false);
        maAdapter.setRemoveButtonListener(this);
        attachmentLV.setAdapter(maAdapter);
        if (maAdapter.getCount() > 0) attachmentLV.setVisibility(View.VISIBLE);
        else attachmentLV.setVisibility(View.GONE);
    }

    private void initAutoCompleteTextView(ArrayList<UserInfoBean> toList, ArrayList<UserInfoBean> ccList, ArrayList<UserInfoBean> contactSearchResult) {
        toACTV.setOnKeyListener(actvOKL);
        ccACTV.setOnKeyListener(actvOKL);
        toACTV.setOnFocusChangeListener(actvOFCL);//输入框失去焦点时判断已输入的是否合法的email地址，是的话加入到收件人
        ccACTV.setOnFocusChangeListener(actvOFCL);//输入框失去焦点时判断已输入的是否合法的email地址，是的话加入到抄送人

        toSPA = new SearchPersonAdapter(wyma, toList, false);
        toSPA.setDataList(contactSearchResult);
        toACTV.setAdapter(toSPA);
        toACTV.setThreshold(1); // 1个字符开始匹配
        toACTV.addTextChangedListener(searchNameTextWatcher);
        toACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //           通讯录搜索结果OnItemClickListener
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toACTV.setText(""); // 清空输入内容
                wyma.vuStores.addToContactList(position, 1);
            }
        });
        toACTV.setDropDownWidth(DeviceUtil.getDeviceScreenWidth(wyma) - 100);
        toACTV.clearFocus();

        ccSPA = new SearchPersonAdapter(wyma, ccList, false);
        ccSPA.setDataList(contactSearchResult);
        ccACTV.setAdapter(ccSPA);
        ccACTV.setThreshold(1); // 1个字符开始匹配
        ccACTV.addTextChangedListener(searchNameTextWatcher);
        ccACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //           通讯录搜索结果OnItemClickListener
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ccACTV.setText(""); // 清空输入内容
                wyma.vuStores.addToContactList(position, 2);
            }
        });
        ccACTV.setDropDownWidth(DeviceUtil.getDeviceScreenWidth(wyma) - 100);
        ccACTV.clearFocus();
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
            String inputText = s.toString();
//            wyma.vuStores.handleSearchingText(inputText);
            handler.removeMessages(1000);
            handler.sendMessageDelayed(handler.obtainMessage(1000, inputText), 500);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private static class WriteMailHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    VUStores.getInstance().handleSearchingText((String) msg.obj);
                    break;
            }
        }
    }


    @Override
    public void emptyInputText() {
        currFocusingACTV.setText("");
    }

    @Override
    public String getSubjectText() {
        return subjectTV.getText().toString().trim();
    }

    @Override
    public String getDraftHtmlContent() {
        return contentRichEditor.getHtml();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        wyma.hideInputMethod();
        switch (id) {
            case R.id.wm_to_civ: //收件人输入框
                wyma.vuStores.setCurrContactList(1);
                currSPA = toSPA;
                currFocusingCIV = toCIV;
                currFocusingACTV = toACTV;
                toACTV.requestFocus();
                wyma.showKeyboard(toACTV);
                break;
            case R.id.wm_cc_civ: //抄送人输入框
                wyma.vuStores.setCurrContactList(2);
                currSPA = ccSPA;
                currFocusingCIV = ccCIV;
                currFocusingACTV = ccACTV;
                ccACTV.requestFocus();
                wyma.showKeyboard(ccACTV);
                break;
            case R.id.wm_to_selector_btn: //收件人选人
                currFocusingCIV = toCIV;
                currFocusingACTV = toACTV;
                wyma.vuStores.setCurrContactList(1);
                wyma.vuStores.setIsInContactSelector(true);
                wyma.showContactSelector(1);
                break;
            case R.id.wm_cc_selector_btn: //抄送选人
                currFocusingCIV = ccCIV;
                currFocusingACTV = ccACTV;
                wyma.vuStores.setCurrContactList(2);
                wyma.vuStores.setIsInContactSelector(true);
                wyma.showContactSelector(2);
                break;
//            case R.id.wm_sc_selector_btn:  //密送选人
////            Log.i("onItemClick---", "wm_sc_selector_btn clicked");
//                currFocusingCIV = scCIV;
//                wyma.vuStores.setCurrContactList(3);
//                wyma.showContactSelector(3);
//                break;
            case R.id.wm_attachment_btn:
                if (vuFileSelectorDialog.isPopWindowShowing()) {
                    vuFileSelectorDialog.hidePopWindow();
                } else {
                    vuFileSelectorDialog.showPopWindow(hideTV);
                }

                break;
            case R.id.send_mail_ok_btn:  //发送按钮点击
                wyma.vuStores.onSendMailButtonClick();
                break;
            case R.id.cancel_send_mail_btn: //取消按钮点击
                wyma.onBackPressed();
                break;
            case R.id.wm_safty_opt_btn: //加密选项按钮点击
                showSafetyOptionsWindow();
                break;
        }
    }

    @Override
    public void prepareToSendMail(MailDetail _md) {
        _md.setSubject(getSubjectText());
        _md.setEncrypted(encryptCB.isChecked());
        _md.setSigned(signCB.isChecked());
    }

    @Override
    public void showSaveDraftPopWindow() {
        if (vuConfirmDialog.isPopWindowShowing()) {
            vuConfirmDialog.hidePopWindow();
        } else vuConfirmDialog.showPopWindow(hideTV);
    }

    @Override
    public void onSendNoEncryptMailButtonClick() {
        wyma.vuStores.sendNormalMail();
    }

    @Override
    public void onContinueSendButtonClick() {
        wyma.vuStores.continueToSendMail();
    }

    @Override
    public void generateContactTV(UserInfoBean u, int type) {//ViewGroup.LayoutParams.WRAP_CONTENT  getResources().getDimension()
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DeviceUtil.dpTopx(wyma, 30));
        layoutParams.leftMargin = DeviceUtil.dpTopx(wyma, 2);
        layoutParams.topMargin = DeviceUtil.dpTopx(wyma, 2);
        TextView textView = new TextView(wyma);
        String str = u.name;
        if (str.length() == 0) {
            str = u.email;
        }
        textView.setText(str);
        textView.setBackgroundResource(R.drawable.wy_contact_drawable);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(getResources().getColor(R.color.wy_common_text_color));
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FlowLayout parentView = (FlowLayout) v.getParent();
                FlowLayout parentView = (FlowLayout) v.getParent();
                if (preSelectedView != null) {
                    if (preSelectedView != v) {
                        preSelectedView.setSelected(false);
                        ((TextView) preSelectedView).setTextColor(Color.BLACK);
                    } else {
                        switch (parentView.getId()) {
                            case R.id.wm_to_civ:
                                wyma.vuStores.removeContactListItem(parentView.indexOfChild(preSelectedView), 1);
                                break;
                            case R.id.wm_cc_civ:
                                wyma.vuStores.removeContactListItem(parentView.indexOfChild(preSelectedView), 2);
                                break;
//                            case R.id.     wm_sc_civ:
//                                wyma.vuStores.removeContactListItem(3,parentView.indexOfChild(preSelectedView));
//                                break;
                        }
                        parentView.removeView(preSelectedView);
                        preSelectedView = null;
                        return;
                    }
                }
                v.setSelected(true);
                ((TextView) v).setTextColor(Color.WHITE);
                preSelectedView = v;
            }
        });
        Log.i(TAG, type + " generateContactTV--->" + u.name);
        if (type == 1)
            toCIV.addViewBeforeLast(textView);
//            toCIV.addView(textView);
        else if (type == 2)
            ccCIV.addViewBeforeLast(textView);
//            ccCIV.addView(textView);
    }

    private void generateContactTV(ArrayList<UserInfoBean> ul, int type) {
        for (UserInfoBean u : ul) {
            generateContactTV(u, type);
        }
    }

    AutoCompleteTextView.OnFocusChangeListener actvOFCL = new AutoCompleteTextView.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!b) {
//                Log.i(TAG, "onFocusChange: blur...");
                AutoCompleteTextView prevFocusingACTV = (AutoCompleteTextView) view;
                String text = prevFocusingACTV.getText().toString();
                int type = 0;
                if (view.getId() == R.id.wm_to_actv) type = 1;
                else if (view.getId() == R.id.wm_cc_actv) type = 2;
                wyma.vuStores.handleInputText(text, type);
                prevFocusingACTV.setText("");
            }
        }
    };

    AutoCompleteTextView.OnKeyListener actvOKL = new AutoCompleteTextView.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DEL:
//                    FlowLayout parentView = ((FlowLayout) v.getParent());
                    FlowLayout parentView = ((FlowLayout) v.getParent());
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (TextUtils.isEmpty(((AutoCompleteTextView) v).getText())) {
                            if (preSelectedView != null) {
                                if (preSelectedView.getParent() == v.getParent()) {//如果当前civ包含已选中的textView
                                    int _index = parentView.indexOfChild(preSelectedView);
                                    wyma.vuStores.removeContactListItem(_index, parentView.getId() == R.id.wm_to_civ ? 1 : 2);
                                    parentView.removeView(preSelectedView);
                                    preSelectedView = null;
                                } else {
                                    preSelectedView.setSelected(false);
                                    ((TextView) preSelectedView).setTextColor(Color.BLACK);
                                    preSelectedView = parentView.getLastView();
                                    preSelectedView.setSelected(true);
                                    ((TextView) preSelectedView).setTextColor(Color.WHITE);
                                }
                                return true;
                            } else {
                                preSelectedView = parentView.getLastView();
                                if (preSelectedView != null) {
                                    preSelectedView.setSelected(true);
                                    ((TextView) preSelectedView).setTextColor(Color.WHITE);
                                }
                            }
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                default:
                    return false;
            }
        }
    };

    @Override
    public void refreshSearchResultListView(ArrayList<UserInfoBean> contactSearchResult) {
        if (currSPA != null) {
            currSPA.setDataList(contactSearchResult);
            currSPA.notifyDataSetChanged();
        }
    }

    @Override
    public void onPictureSelect(String path) {
        String[] filePaths = path.split(":");
        int l = filePaths.length;
        if (isInsertImage) {//编辑器的插入图像 按钮回调
            for (String filePath : filePaths) {
                contentRichEditor.insertImage(wyma.vuStores.getBase64Image(filePath), "");
            }
        } else {//附件 相册按钮 回调
            for (String filePath : filePaths) {
                wyma.vuStores.addMailAttachmentByFilePath(filePath);
            }
        }
    }

    /**
     * 不保存草稿按钮点击
     */
    @Override
    public void onButton1Click() {
        closeWriteMailFragment();
    }

    /**
     * 保存草稿按钮点击
     */
    @Override
    public void onButton2Click() {
        wyma.vuStores.saveMailDraft();
    }

    @Override
    public void closeWriteMailFragment() {
        wyma.isWritingMail = false;
        wyma.onBackPressed();
    }

    @Override
    public void sendNoSubjectMail() {
        wyma.vuStores.sendNoSubjectMail();
    }

    /**
     * 弹出窗口 拍照 按钮点击
     */
    @Override
    public void onCameraSelected() {
        if (DeviceUtil.getPermission(getActivity(), Manifest.permission.CAMERA, 100)) {
            openCamera();
        }
    }

    public void openCamera() {
        startActivityForResult(CommonUtils.getTakePhoteIntent(getActivity(), wyma.vuStores.getCameraPhotoPath()), CAMERA_REQUEST_CODE);
    }

    /**
     * 弹出窗口 相册 按钮点击
     */
    @Override
    public void onGallerySelected() {
        if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 104)) {
            isInsertImage = false;
            wyma.showImageSelector();
        }
    }

    /**
     * 弹出窗口 文档 按钮点击
     */
    @Override
    public void onFileSelected() {
        //附件按钮点击
        if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 103)) {
            choseFile();
        }
    }

    public void choseFile() {
        ChoseFileDialogFragment dialogFragment = new ChoseFileDialogFragment();
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setListener(WriteMailFragment.this);
    }

    @Override
    public void onLocalAttachmentSelected() {
        wyma.selectAttachmentFragment = new SelectLocalAttachmentFragment();
        wyma.selectAttachmentFragment.setOnFinishSelectListener(WriteMailFragment.this);
        wyma.openSelectAttachmentFragment();
    }

    @Override
    public void onFileSelect(ArrayList<LocalFileBean> choseFileList) {
        wyma.vuStores.addLocalFileBeanListToAttachmentList(choseFileList);
    }

    @Override
    public void onFinishSelect(List<MailAttachment> selectedAttacmentList) {
        wyma.vuStores.addToAttachmentList(selectedAttacmentList);
    }

    @Override
    public void fillDraftData(String subject, ArrayList<UserInfoBean> toList, ArrayList<UserInfoBean> ccList,
                              String quoteMailHead, boolean encrypted, boolean signed, boolean hasCurrUsingCa) {
        subjectTV.setText(subject);
        initAutoCompleteTextView(toList, ccList, new ArrayList<UserInfoBean>());
        generateContactTV(toList, 1);
        generateContactTV(ccList, 2);

        encryptCB.setEnabled(hasCurrUsingCa);
        encryptCB.setChecked(encrypted);
        encryptCB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                wyma.vuStores.handleEncryptItemClick(((CheckBox) view).isChecked());
            }
        });
        signCB.setEnabled(hasCurrUsingCa);
        signCB.setChecked(signed);
        signCB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                wyma.vuStores.handleSignItemClick(((CheckBox) view).isChecked());
            }
        });
        contentRichEditor.setHtml(quoteMailHead);
    }

//    /**
//     * 弹出窗口 音乐 按钮点击
//     */
////    @Override
////    public void onMusicSelected() {
////
////    }
//
//    /**
//     * 弹出窗口 视频 按钮点击
//     */
////    @Override
////    public void onVideoSelected() {
////
////    }

    @Override
    public void refreshAttachmentListView() {
        maAdapter.notifyDataSetChanged();
        if (maAdapter.getCount() > 0)
            attachmentLV.setVisibility(View.VISIBLE);
        else
            attachmentLV.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ATTACHMENT_REQUEST_CODE:
                    break;
                case CAMERA_REQUEST_CODE:
                    wyma.vuStores.compressAndGetImageBitmap();
                    break;
                case MUSIC_REQUEST_CODE:

                    break;
                case VIDEO_REQUEST_CODE:
                    break;
            }
        }
    }

    @Override
    public void onAttachmentClick(int position) {
//        Log.i(TAG, "onAttachmentClick:------- " + attachmentList.get(position).getName() + "position--" + position);
//        execUploadAttachment(attachmentList.get(position),position);
    }

    @Override
    public void onRemoveButtonClick(int position) {
        wyma.vuStores.removeAttachment(position);
    }

    /**
     * 创建PopupWindow
     */
    protected void initPopuptWindow() {
        // TODO Auto-generated method stub
        View popupWindow_view = wyma.getLayoutInflater().inflate(R.layout.wy_pw_safe_option, null, false);
        safetyOptPW = new PopupWindow(popupWindow_view, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);

        safetyOptPW.setAnimationStyle(R.style.MenuAnimationFade);
        safetyOptPW.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = wyma.getWindow().getAttributes();
                lp.alpha = 1f;
                wyma.getWindow().setAttributes(lp);
            }
        });
        encryptCB = (CheckBox) popupWindow_view.findViewById(R.id.mail_encrypt_checkbox);
        signCB = (CheckBox) popupWindow_view.findViewById(R.id.mail_sign_checkbox);
        popupWindow_view.findViewById(R.id.wy_confirm_ok_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                safetyOptPW.dismiss();
            }
        });
    }

    /***
     * 开启安全选项窗口
     */
    private void showSafetyOptionsWindow() {
        if (wyma.vuStores.isCertInstalled()) {
            ColorDrawable cd = new ColorDrawable(0x000000);
            safetyOptPW.setBackgroundDrawable(cd);
            WindowManager.LayoutParams lp = wyma.getWindow().getAttributes();
            lp.alpha = 0.4f;
            wyma.getWindow().setAttributes(lp);
            safetyOptPW.showAtLocation(hideTV, Gravity.BOTTOM, 0, 0);
        } else {
            wyma.toast("您当前的邮箱账号未安装任何证书，无法发送加密邮件，您可以在账号设置界面安装数字证书");
        }
    }

    @Override
    public void showNoPbkWarningDialog(String msg, int showContinueSendButton) {
        vuNoPbkWarnDialog = new VUNoPbkWarnDialog(wyma, msg, showContinueSendButton);
        vuNoPbkWarnDialog.setNoPbkWarnDialogListener(this);
        vuNoPbkWarnDialog.showPopWindow(hideTV);
    }

    @Override
    public void emptyContactTV() {
        int count = currFocusingCIV.getChildCount();
        if (count > 1) {//清空输入框内旧的userInfoBean视图
            currFocusingCIV.removeViews(0, count - 1);
        }
    }

    @Override
    public void onCancelSendButtonClick() {
        wyma.vuStores.setSendMailButtonEnabled(true);
    }

    @Override
    public void showRemindSubjectDialog() {
        remindSubjectDialog.showPopWindow(hideTV);
    }

    @Override
    public void onDestroy() {
        wyma.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onDestroy();
    }

//    private static class AttachmentUploadHandler extends Handler {
//        private WeakReference<WriteMailFragment> reference;
//        private WriteMailFragment wmf;
//        TextView statusTV = null;
//
//        public AttachmentUploadHandler(WeakReference<WriteMailFragment> reference) {
//            this.reference = reference;
//            wmf = reference.get();
//        }
//
//        @Override
//        public void dispatchMessage(@NonNull Message msg) {
//            switch (msg.what) {
//                case FILE_UPLOAD_FAILURE:
////                    wmf.wyma.toast("上传附件失败，请检查网络及手机存储");
//                    statusTV = (TextView) wmf.attachmentLV.getChildAt(msg.arg1).findViewById(R.id.wy_attachment_status);
//                    wmf.wyma.dismissProgressDialog();
//                    wmf.wyma.toast("附件" + wmf.attachmentList.get(msg.arg1).getName() + "上传失败，请重新上传或删除该附件后再发送邮件");
//                    statusTV.setText("重新上传");
//                    break;
//                case FILE_UPLOAD_SUCCESS:
//                    statusTV = (TextView) wmf.attachmentLV.getChildAt(msg.arg1).findViewById(R.id.wy_attachment_status);
//                    statusTV.setText("已上传");
//                    MailAttachment ma = (MailAttachment) msg.obj;
//                    DBUtil.saveMailAttachment(ma);
//                    wmf.attachmentList.set(msg.arg1, ma);
//                    Log.i(TAG, "FILE_UPLOAD_SUCCESS: " + msg.arg1);
//                    boolean isUploadOver = true;
//                    for (MailAttachment maaa : wmf.attachmentList) {
//                        String u = maaa.getUrl();
//                        if (u == null || u.length() == 0) {
//                            isUploadOver = false;
//                        }
//                    }
//                    if (isUploadOver) {
//                        wmf.wyma.dismissProgressDialog();
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                //如果都上传完了 就执行发邮件命令
//                                String attachmentInfo = "</br></br></br>以下是附件列表链接，点击可下载：<br>";
//                        /* 如果有附件，把附件拼接到邮件末尾 */
//                                for (MailAttachment maaa : wmf.attachmentList) {
//                                    Long maSize = maaa.getSize();
//                                    if (maSize == null) maSize = 0l;
//                                    attachmentInfo += "<a href='" + maaa.getUrl() + "' class='attachmentDownload'>" + maaa.getName() + "(" + VUFileUtil.convertFileSize(maSize) + ")</a><br>";
//                                }
//                                wmf.sendWeiYouMail(attachmentInfo);
//                            }
//                        }, 500);
//                    }
//
//                    break;
//                case FILE_UPLOAD_PROGRESS:
////                    显示文件下载进度
//                    statusTV = (TextView) wmf.attachmentLV.getChildAt(msg.arg1).findViewById(R.id.wy_attachment_status);
//                    statusTV.setText(msg.obj + "%");
//                    break;
//
//            }
//        }
//    }
//
//    private void execUploadAttachment(final MailAttachment ma, final int index) {
//        Param[] params = new Param[]{
//                new Param("user_id", wyma.selfUser.getUserId()),
//                new Param("system_name", "weiyou"),
//        };
//        Log.i(TAG, "filePath: " + ma.getPath());
//        Dispatcher.getInstance().dispatchNetWorkAction(CommonActions.UPLOAD_FILE_BY_HTTP_POST, new File(ma.getPath()), params,
//                new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        e.printStackTrace();
//                        auHandler.sendMessage(auHandler.obtainMessage(FILE_UPLOAD_FAILURE, index, 1));
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        Log.i(TAG, "execUploadAttachment onResponse: " + response.body());
//                        if (response.isSuccessful()) {
//                            try {
//                                JSONObject result = new JSONObject(response.body().string());
//                                int docId = result.getInt("docid");
//                                String uri = result.getString("download_url");
//                                MailAttachment m4a1 = ma;
//                                m4a1.setUrl(uri);
//                                auHandler.sendMessage(auHandler.obtainMessage(FILE_UPLOAD_SUCCESS, index, docId, m4a1));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                auHandler.sendMessage(auHandler.obtainMessage(FILE_UPLOAD_FAILURE, index, 1));
//                            }
//                        } else {
//                            auHandler.sendMessage(auHandler.obtainMessage(FILE_UPLOAD_FAILURE, index, 1));
//                        }
//                    }
//                }, new ProgressRequestListener() {
//                    @Override
//                    public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
//                        double percent = (double) bytesWritten / contentLength;
//                        auHandler.sendMessage(auHandler.obtainMessage(FILE_UPLOAD_PROGRESS, index, 0, VUFileUtil.df.format(percent * 100)));
//                    }
//                });
//    }
}
