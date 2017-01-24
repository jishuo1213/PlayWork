package com.inspur.playwork.view.message.chat;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.common.sendmail.SendMailFragment;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.core.PlayWorkServiceNew;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.message.VChatBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.LoadFileManager;
import com.inspur.playwork.view.common.BaseActivity;
import com.inspur.playwork.view.common.chosepicture.ChosePictureFragment;
import com.inspur.playwork.view.message.chat.emoji.EmojiAdapter;

import org.json.JSONException;

/**
 * Created by Fan on 15-9-21.
 */
public class ChatActivityNew extends BaseActivity implements View.OnClickListener,
        ChosePictureFragment.SelectedPicureListener, SendMailFragment.SendMailFinishListener, EmojiAdapter.EmojiSelectListener {

    private static final String TAG = "ChatActivityFan";
    public static final String TASK_BEAN = "taskBean";
    public static final String CHAT_WINDOW_INFO = "chatWindowInfo";
    public static final String VCHAT_BEAN = "vChatBean";
//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    public ImageButton leftImageButton;
    public TextView titleTextView;
    public EditText titleEditText;
    public ImageButton rightImageButton;

    private ChatWindowInfoBean windowInfoBean;
    private TaskBean taskBean;
    private VChatBean vChatBean;

    private Fragment chatFragment;

    private PlayWorkServiceNew.Binder binder;

//    private BitmapCacheManager cacheManager;

    private LoadFileManager loadFileManager;

    private Fragment chosePictureFragment;

    private Fragment sendSmallMailFragment;

    private boolean isChosePictureShow;
    private boolean isSmallMailFragmentShow;
    private boolean isRecipientFragmentShow = false;

    private String mGroupName; // 修改后的任务名称/微聊话题

//    private ArrayList<VChatBean> changeVChatList;

    private IMLinearLayout rootView;

    private TextView sendMailButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat_activity);
        rootView = (IMLinearLayout) findViewById(R.id.root_view);
        initData();
        initView();
    }

    private void initData() {
        taskBean = getIntent().getParcelableExtra(TASK_BEAN);

        windowInfoBean = getIntent().getParcelableExtra(CHAT_WINDOW_INFO);
        if (windowInfoBean == null) {
            windowInfoBean = new ChatWindowInfoBean();
        }
        windowInfoBean.taskTitle = windowInfoBean.taskTitle == null ? "" : windowInfoBean.taskTitle;
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.OPEN_ONE_CHAT_WINDOW, windowInfoBean, taskBean);
        MessageStores.getInstance().openWindow(windowInfoBean, taskBean);
        mGroupName = windowInfoBean.taskTitle;

        vChatBean = getIntent().getParcelableExtra(VCHAT_BEAN);
        MessageStores.getInstance().setCurrentVchatBean(vChatBean);

        bindService(new Intent(this, PlayWorkServiceNew.class), connection, 0);
    }

    private void initView() {
        leftImageButton = (ImageButton) findViewById(R.id.iv_left);
        titleTextView = (TextView) findViewById(R.id.tv_title);
        rightImageButton = (ImageButton) findViewById(R.id.iv_right);
        leftImageButton.setOnClickListener(this);
        rightImageButton.setOnClickListener(this);
//        cacheManager = BitmapCacheManager.findOrCreateRetainFragment(getFragmentManager());
        loadFileManager = LoadFileManager.findOrCreateRetainFragment(getFragmentManager());
        titleEditText = (EditText) findViewById(R.id.edit_title);
        sendMailButton = (TextView) findViewById(R.id.send_mail_text_view);

        sendMailButton.setOnClickListener(this);
        if (windowInfoBean.groupId == null) {
            // 显示编辑框
            initTitleView(View.VISIBLE, View.GONE, windowInfoBean.taskTitle, View.GONE, View.VISIBLE, windowInfoBean.taskTitle);
        } else if ("".equals(windowInfoBean.taskTitle) && (PreferencesHelper.getInstance().getCurrentUser().id.equals(windowInfoBean.createUser)) && !windowInfoBean.isSingle) {
            // 显示编辑框
            initTitleView(View.VISIBLE, View.GONE, windowInfoBean.taskTitle, View.GONE, View.VISIBLE, windowInfoBean.taskTitle);
        } else if ("".equals(windowInfoBean.taskTitle) || windowInfoBean.isSingle) {
            // 显示人名
            initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.memberNames, View.GONE, View.GONE, windowInfoBean.memberNames);
        } else {
            // 显示话题
            initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.taskTitle, View.GONE, View.GONE, windowInfoBean.taskTitle);
        }
    }

    public void initTitleView(int leftVisibility, int titleTextVisibility, String titleText,
                              int rightVisibility, int titleEditVisibility, String titleEdit) {
        leftImageButton.setVisibility(leftVisibility);
        titleTextView.setVisibility(titleTextVisibility);
        titleTextView.setText(titleText);
        titleTextView.setOnClickListener(this);
        rightImageButton.setVisibility(rightVisibility);
        titleEditText.setVisibility(titleEditVisibility);
        titleEditText.setText(titleEdit);
        if ((titleEditVisibility == View.VISIBLE) && (windowInfoBean.groupId == null || !"".equals(windowInfoBean.taskTitle))) {
            titleEditText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public boolean isSmallMailFragmentShow() {
        return isSmallMailFragmentShow;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setTaskCustomProperty();
        ((ChatFragment) chatFragment).clean();
        setWindowGroupId("");
        unbindService(connection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                if (isRecipientFragmentShow) {
                    CommonUtils.back();
                    sendMailButton.setVisibility(View.VISIBLE);
                    return;
                }

                if (isChosePictureShow) {
                    CommonUtils.back();
                    return;
                }

                if (isSmallMailFragmentShow) {
                    this.initDialogForSmallMail();
                    return;
                }

                CommonUtils.back();
                break;
            case R.id.tv_title:
                if (PreferencesHelper.getInstance().getCurrentUser().id.equals(windowInfoBean.createUser) && !windowInfoBean.isSingle) {
                    initTitleView(View.VISIBLE, View.GONE, windowInfoBean.taskTitle, View.GONE, View.VISIBLE, windowInfoBean.taskTitle);
                }
                break;
            case R.id.send_mail_text_view:
                if (sendSmallMailFragment != null)
                    ((SendMailFragment) sendSmallMailFragment).clickOkBtn();
                break;
        }
    }

    public void showChosePictureFragment() {
        if (chosePictureFragment == null)
            chosePictureFragment = ChosePictureFragment.getInstance("发送", false);
        isChosePictureShow = true;
        getFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, chosePictureFragment).addToBackStack(null).commit();
    }

    public void showSmallMailFragment(SmallMailBean smallMailBean, int type) {
        smallMailBean.taskId = windowInfoBean.taskId;
        smallMailBean.chatId = windowInfoBean.mailId;
        smallMailBean.type = type;
        sendSmallMailFragment = SendMailFragment.getInstance(smallMailBean, windowInfoBean, taskBean);
        ((SendMailFragment) sendSmallMailFragment).setSendMailFinishListener(this);
        if (type != SmallMailBean.VIEW_MODE)
            sendMailButton.setVisibility(View.VISIBLE);
        isSmallMailFragmentShow = true;
        getFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, sendSmallMailFragment).addToBackStack(null).commit();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (PlayWorkServiceNew.Binder) service;
            setWindowGroupId(windowInfoBean.groupId);
            chatFragment = ChatFragment.getInstance(taskBean, windowInfoBean);
            rootView.setListener((IMLinearLayout.InputMethodListener) chatFragment);
            getFragmentManager().beginTransaction().
                    add(R.id.chat_fragment_container, chatFragment).commit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void setWindowGroupId(String groupId) {
        PlayWorkServiceNew playWorkService = (PlayWorkServiceNew) binder.getService();
        playWorkService.setChatWindowId(groupId, (taskBean == null) ? null : taskBean.taskId);
    }

    @Override
    public void onBackPressed() {


        if (isRecipientFragmentShow) {
            super.onBackPressed();
            sendMailButton.setVisibility(View.VISIBLE);
            return;
        }

        if (isSmallMailFragmentShow) {
            this.initDialogForSmallMail();
            return;
        }
        if (isChosePictureShow) {
            super.onBackPressed();
            isChosePictureShow = false;
            return;
        }

        if (((ChatFragment) chatFragment).isInterceptBackPress()) {
            return;
        }

        if ((chatFragment != null) && (((ChatFragment) chatFragment).isChosePersonShow())) {
            ((ChatFragment) chatFragment).hideChosePersonFragment();
            return;
        }


        Log.i(TAG, "onBackPressed: notifyvchatlist");
//        this.notifyVChatList();
        super.onBackPressed();
        //TODO
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

//    public BitmapCacheManager getArrayMap() {
//        return cacheManager;
//    }

    public LoadFileManager getLoadFileManager() {
        return loadFileManager;
    }

    public void exitChoosePerson() {
        if (chatFragment != null) {
            ((ChatFragment) chatFragment).hideChosePersonFragment();
        }
    }

    @Override
    public void onPictureSelect(String path) {
//        CommonUtils.back();
        getFragmentManager().popBackStack();
        isChosePictureShow = false;
        ((ChatFragment) chatFragment).sendChoseImage(path);
    }

    public void setTaskCustomProperty(String taskPlace, int taskPrivate, String taskTitle) {
        taskBean.taskPlace = taskPlace;
        taskBean.setPrivate = taskPrivate;
        taskBean.taskContent = taskTitle;
    }

    /**
     * 任务地点/随手记修改成功后调用
     */
    private void setTaskCustomProperty() {
        if (taskBean != null) {
            Intent intent = new Intent();
            intent.putExtra("taskPlace", taskBean.taskPlace);
            intent.putExtra("taskPrivate", taskBean.setPrivate);
            intent.putExtra("taskContent", taskBean.taskContent);
            setResult(RESULT_OK, intent);
        }
    }

    /**
     * 任务名称/微聊话题修改成功后调用
     */
    public void setTaskTitle() {
        titleTextView.setText(mGroupName);
        windowInfoBean.taskTitle = mGroupName;
    }

    public void setTaskTitle(String subject) {
        titleTextView.setText(subject);
        windowInfoBean.taskTitle = subject;
    }

    public void setWindowInfoBean(ChatWindowInfoBean chatWindowInfoBean) {
        this.windowInfoBean = chatWindowInfoBean;
    }

    /**
     * 封装VChatBean信息
     */
//    private String setVChatBean(ChatWindowInfoBean chatWindowInfoBean) {
//        VChatBean vChatBean = new VChatBean();
//        vChatBean.groupId = chatWindowInfoBean.groupId;
//        vChatBean.memberList = chatWindowInfoBean.allMemberList;
//        String memberNames = vChatBean.setAvatars(((PlayWorkApplication) getApplication()).getAvatars());
//
//        if (TextUtils.isEmpty(chatWindowInfoBean.taskTitle))
//            vChatBean.topic = memberNames;
//        else
//            vChatBean.topic = chatWindowInfoBean.taskTitle;
//        Log.i(TAG, "setVChatBean: " + memberNames + "+++++" + chatWindowInfoBean.taskTitle);
//        // 判断是否为单聊
//        if (!chatWindowInfoBean.isSingle) {
//            vChatBean.isGroup = 1;
//        }
//        vChatBean.members = chatWindowInfoBean.memberString;
//        vChatBean.lastChatTime = System.currentTimeMillis();
//        this.vChatBean = vChatBean;
//        return memberNames;
//    }

    /**
     * 封装最后一条消息
     */
//    public void setLastMessage() {
//        /*
//        封装最后一条消息
//         */
//        MessageBean messageBean = ((ChatFragment) chatFragment).getLastMessage();
//        if (messageBean == null) {
//            vChatBean.lastMsg = "无消息内容...";
//        } else {
////            if (messageBean.type == MessageBean.IMAGE_MESSAGE_SEND || messageBean.type == MessageBean.IMAGE_MESSAGE_RECIVE) {
////                vChatBean.lastMsg = "[图片]";
////            } else if (messageBean.type == MessageBean.RECALL_MESSAGE) {
////                vChatBean.lastMsg = messageBean.sendMessageUser.name + "撤回了一条消息";
////            } else {
////                vChatBean.lastMsg = messageBean.content;
////            }
//            if (messageBean.type == MessageBean.RECALL_MESSAGE) {
//                vChatBean.lastMsg = messageBean.sendMessageUser.name + "撤回了一条消息";
//            } else {
//                vChatBean.lastMsg = messageBean.content;
//            }
//            vChatBean.lastChatTime = messageBean.sendTime;
//            vChatBean.msgId = messageBean.id;
//        }
//        // 添加到返回列表
//        changeVChatList.add(vChatBean);
//        Log.i(TAG, "setLastMessage: " + vChatBean);
//    }

//    private void notifyVChatList() {
//        Log.d(TAG, "notifyVChatList() called with: " + (taskBean == null));
//        if (taskBean != null || windowInfoBean.groupId == null) {
//            return;
//        }
//        if (TextUtils.isEmpty(windowInfoBean.taskTitle)) {
//            vChatBean.topic = windowInfoBean.memberNames;
//        } else {
//            vChatBean.topic = windowInfoBean.taskTitle;
//        }
//        setLastMessage(); // 封装最后一条消息
//        Intent intent = new Intent();
//        intent.putExtra("changeVChatList", changeVChatList);
//        Log.i(TAG, "notifyVChatList: ==========" + changeVChatList.size());
//        setResult(RESULT_OK, intent);
//    }

    /**
     * 控制标题栏变化的方法
     */
    public void changeTitleEdit() {
        // 单聊或者编辑框隐藏时，不做修改，直接返回
        if (windowInfoBean.isSingle || (titleEditText.getVisibility() == View.GONE)) {
            return;
        }

        /*
        新建聊天时，不做任何操作直接丢失焦点
        非新建聊天时，输入内容为空或者输入内容不变则返回原标题。修改了话题内容则修改话题。
         */
        if (windowInfoBean.groupId == null) {
            titleEditText.clearFocus();
        } else {
            // 获取输入内容
            mGroupName = titleEditText.getText().toString();

            /*
            1、输入话题为空并且原话题为空。
            2、输入话题为空并且原话题不为空。
            3、输入话题不为空并且和原话题相等。
            4、输入话题不为空并且和原话题不等。
             */
            if ("".equals(mGroupName) && "".equals(windowInfoBean.taskTitle)) {
                titleEditText.clearFocus();
            } else if ("".equals(mGroupName) && !"".equals(windowInfoBean.taskTitle)) {
                // 显示原标题栏
                this.initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.taskTitle, View.GONE, View.GONE, windowInfoBean.taskTitle);
            } else if (!"".equals(mGroupName) && mGroupName.equals(windowInfoBean.taskTitle)) {
                // 显示原标题栏
                this.initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.taskTitle, View.GONE, View.GONE, windowInfoBean.taskTitle);
            } else if (!"".equals(mGroupName) && !mGroupName.equals(windowInfoBean.taskTitle)) {
                // 显示新标题栏
                this.initTitleView(View.VISIBLE, View.VISIBLE, mGroupName, View.GONE, View.GONE, mGroupName);
                /*
                修改话题
                 */
                if (chatFragment != null) {
                    ((ChatFragment) chatFragment).setTaskTitle(mGroupName);
                } else {
                    UItoolKit.showToastShort(this, "话题修改失败");
                    // 显示原标题栏
                    titleTextView.setText(windowInfoBean.taskTitle);
                }
            }
        }
    }

    /**
     * 获取titleEditText的焦点状态
     */
    public boolean titleEditFocused() {
        return titleEditText.hasFocus();
    }

    public void clearTitleEdit() {
        titleEditText.setText("");
        this.initTitleView(View.VISIBLE, View.GONE, windowInfoBean.taskTitle, View.GONE, View.VISIBLE, windowInfoBean.taskTitle);
    }

//    public VChatBean getvChatBean() {
//        return vChatBean;
//    }

    public String getEditTopic() {
        return titleEditText.getText().toString();
    }

    public void setVChatMembers() {
        if (taskBean != null) {
            return;
        }
        if (!TextUtils.isEmpty(windowInfoBean.taskTitle)) {
            vChatBean.topic = windowInfoBean.taskTitle;
        } else {
            vChatBean.topic = windowInfoBean.memberNames;
        }
        vChatBean.isNeedCreateAvatar = true;
        try {
            vChatBean.setMember(windowInfoBean.memberString);
            vChatBean.setAvatars(((PlayWorkApplication) getApplication()).getAvatars());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMailSuccess() {
        Log.i(TAG, "sendMailSuccess: ");
        setSendButtonVisibility(View.GONE);
        isSmallMailFragmentShow = false;
    }

    public void setSendButtonVisibility(int visibility) {
        sendMailButton.setVisibility(visibility);
    }

    public void setIsRecipientFragmentShow(boolean isRecipientFragmentShow) {
        this.isRecipientFragmentShow = isRecipientFragmentShow;
    }

    private void initDialogForSmallMail() {
        int mode = ((SendMailFragment) sendSmallMailFragment).getMode();
        if (mode == SmallMailBean.VIEW_MODE) {
            sendMailSuccess();
            getFragmentManager().popBackStack();
            return;
        }
        new AlertDialog.Builder(this).setTitle("确定放弃编辑吗").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMailSuccess();
                getFragmentManager().popBackStack();
            }
        }).setNegativeButton("取消", null).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((ChatFragment) chatFragment).openCamera();
                } else {
                    UItoolKit.showToastShort(this, "拍照请提供相机权限");
                }
                break;
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((ChatFragment) chatFragment).choseFile();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showChosePictureFragment();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((SendMailFragment) (sendSmallMailFragment)).openFile();
                } else {
                    UItoolKit.showToastShort(this, "请提供选择文件权限");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onEmojiClick(String emoji) {
        Log.d(TAG, "onEmojiClick() called with: emoji = [" + emoji + "]");
        ((ChatFragment) chatFragment).onEmojiClick(emoji);
    }

    @Override
    public void onBackspaceClick() {
        Log.d(TAG, "onBackspaceClick() called");
        ((ChatFragment) chatFragment).onBackspaceClick();
    }
}
