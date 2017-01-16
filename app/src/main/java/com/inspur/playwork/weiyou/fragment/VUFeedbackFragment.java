package com.inspur.playwork.weiyou.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.view.common.chosepicture.ChosePictureFragment;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.store.FeedbackOperation;
import com.inspur.playwork.weiyou.view.ColorPickerDialog;
import com.inspur.playwork.weiyou.view.FontSizeSelectorSpinner;
import com.inspur.playwork.weiyou.view.VUConfirmDialog;
import com.inspur.playwork.weiyou.view.ass.FontSizeSpinnerAdapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by 孙 on 2015/11/16 0016.
 */
public class VUFeedbackFragment extends Fragment implements OnClickListener, FeedbackOperation,
        ChosePictureFragment.SelectedPicureListener,VUConfirmDialog.ConfirmDialogListener {
    private static final String TAG = "WriteMailFragment-->";
    private static final int IMAGE_REQUEST_CODE = 1;
//    private ImageUploadHandler auHandler;
    private WeiYouMainActivity wyma;

    private VUConfirmDialog vuConfirmDialog;

    private Button cancelSendMailBtn;
    private Button sendMailBtn;

    private TextView subjectTV;
    private RichEditor contentRichEditor;
    private HorizontalScrollView editorToolsLL;
    private ScrollView contentSV;
    private int currFontColor = 0;
    private int currFontBackgroundColor = 0;
    private FontSizeSelectorSpinner fontSizeSpinner;
    private List<Integer> fontSizeList;
    private ArrayList<File> imageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wyma = (WeiYouMainActivity)getActivity();
        wyma.vuStores.setFeedbackReference(this);
        imageList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "WriteMailFragment onCreateView");
        View v = inflater.inflate(R.layout.wy_fragment_write_feedback, container, false);
//        auHandler = new ImageUploadHandler(new WeakReference<>(this));
        initView(v);
        vuConfirmDialog = new VUConfirmDialog(wyma, "您真的要放弃已编辑的反馈信息吗？", "不反馈了", "继续编辑");
        vuConfirmDialog.setConfirmDialogListener(this);
        return v;
    }

    public void initView(View v) {
        editorToolsLL = (HorizontalScrollView) v.findViewById(R.id.wm_editor_tools);

        //初始化取消、发送、选人、选附件按钮，及其点击事件
        cancelSendMailBtn = (Button) v.findViewById(R.id.cancel_send_mail_btn);
        sendMailBtn = (Button) v.findViewById(R.id.send_mail_ok_btn);

        cancelSendMailBtn.setOnClickListener(this);
        sendMailBtn.setOnClickListener(this);
        contentSV = (ScrollView) v.findViewById(R.id.bottom_scroll_view);
        subjectTV = (TextView) v.findViewById(R.id.wm_subject_et);
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
//        contentRichEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
//            @Override
//            public void onTextChange(String text) {
////                mPreview.setText(text);
//            }
//        });
//        返回上一步
        v.findViewById(R.id.action_undo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.undo();
            }
        });
//        重做
        v.findViewById(R.id.action_redo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.redo();
            }
        });
//        加粗
        v.findViewById(R.id.action_bold).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setBold();
            }
        });
//        斜体
        v.findViewById(R.id.action_italic).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setItalic();
            }
        });
//        字体颜色
        v.findViewById(R.id.action_txt_color).setOnClickListener(new OnClickListener() {
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
        v.findViewById(R.id.action_bg_color).setOnClickListener(new OnClickListener() {
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
        v.findViewById(R.id.action_font_size).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                contentRichEditor.setEditorFontSize();
                fontSizeSpinner.setWidth(view.getWidth());
                fontSizeSpinner.showAsDropDown(view);
            }
        });
//        删除线
        v.findViewById(R.id.action_strikethrough).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setStrikeThrough();
            }
        });
//        下划线
        v.findViewById(R.id.action_underline).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setUnderline();
            }
        });

//        缩进
        v.findViewById(R.id.action_indent).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setIndent();
            }
        });
//        反缩进
        v.findViewById(R.id.action_outdent).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setOutdent();
            }
        });
//        居左
        v.findViewById(R.id.action_align_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignLeft();
            }
        });
//        居中
        v.findViewById(R.id.action_align_center).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignCenter();
            }
        });
//        居右
        v.findViewById(R.id.action_align_right).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setAlignRight();
            }
        });
//        引号
        v.findViewById(R.id.action_blockquote).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setBlockquote();
            }
        });
//        插入图片
        v.findViewById(R.id.action_insert_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(wyma, MultiImageSelectorActivity.class);
//                // 是否显示拍摄图片
//                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
//                // 最大可选择图片数量
//                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
//                // 选择模式
//                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
////                // 默认选择
////                if(mSelectPath != null && mSelectPath.size()>0){
////                    intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
////                }
//                startActivityForResult(intent, IMAGE_REQUEST_CODE);
//                contentRichEditor.insertImage("http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
//                        "dachshund");
            }
        });
//        插入超链接
        v.findViewById(R.id.action_insert_link).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                contentRichEditor.insertLink("https://github.com/wasabeef", "wasabeef");

            }
        });
//        下标
        v.findViewById(R.id.action_subscript).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setSubscript();
            }
        });
//        上标
        v.findViewById(R.id.action_superscript).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contentRichEditor.setSuperscript();
            }
        });

        subjectTV.requestFocus();
        wyma.showKeyboard(subjectTV);
    }

    public void onCancelSendFeedback() {
        String content = contentRichEditor.getHtml();
        if (subjectTV.getText().length() > 0 || (content != null && content.length() > 0)) {
            if (vuConfirmDialog.isPopWindowShowing()) {
                vuConfirmDialog.hidePopWindow();
            } else vuConfirmDialog.showPopWindow(contentSV);
        } else {
            wyma.isWritingFeedback = false;
            wyma.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        wyma.hideInputMethod();

        if (id == R.id.send_mail_ok_btn) { //发送按钮点击
            prepareToSendFeedback();
        } else if (id == R.id.cancel_send_mail_btn) {//取消按钮点击
            wyma.onBackPressed();
        }
    }

    String getSubjectText() {
        return subjectTV.getText().toString().trim();
    }

    public void prepareToSendFeedback(){
        String contentHtml = contentRichEditor.getHtml();
        if (contentHtml == null || contentHtml.length() == 0) {
            wyma.toast("请输入反馈内容");
            return;
        }
        wyma.vuStores.sendWeiYouFeedback
                (contentHtml,getSubjectText());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {//图片选择器 回调
            }
        }
    }

    @Override
    public void onPictureSelect(String path) {

    }

    @Override
    public void onButton1Click() {
        closeFeedback();
    }
    @Override
    public void onButton2Click() {
    }


    @Override
    public void closeFeedback() {
        wyma.isWritingFeedback = false;
        wyma.onBackPressed();
    }

    private static class ImageUploadHandler extends Handler {

        private WeakReference<VUFeedbackFragment> reference;

        public ImageUploadHandler(WeakReference<VUFeedbackFragment> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            VUFeedbackFragment wmf = reference.get();
            switch (msg.what) {
                case 0x00:
                    wmf.wyma.toast("上传图片失败，请检查网络及手机存储");
                    break;
                case 0x01:
                    break;
            }
        }
    }
}
