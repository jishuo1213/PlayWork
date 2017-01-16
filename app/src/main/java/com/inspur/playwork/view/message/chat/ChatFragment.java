package com.inspur.playwork.view.message.chat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.common.chosefile.ChoseFileDialogFragment;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.core.PlayWorkApplication;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.ChatWindowInfoBean;
import com.inspur.playwork.model.message.CustomProperty;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.message.SmallMailBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.message.MessageStores;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.EmojiHandler;
import com.inspur.playwork.utils.EmotionKeyboard;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.utils.loadfile.LoadFileHandlerThread;
import com.inspur.playwork.utils.loadfile.LoadFileManager;
import com.inspur.playwork.view.common.viewimage.ImageViewActivity;
import com.inspur.playwork.view.message.chat.emoji.EmojiPageAdapter;
import com.inspur.playwork.view.message.chat.emoji.PointTabAdapter;
import com.inspur.playwork.view.timeline.taskattachment.TaskAttachmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Fan on 15-9-21.
 */
public class ChatFragment extends Fragment implements ChatViewOperation, ChosePersonRecyclerAdapter.PersonSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, MessageRecyclerAdapter.MessageEventListener,
        ChatChosePersonFragment.CancelQuickChatListener,
        View.OnClickListener, IMLinearLayout.InputMethodListener, ChoseFileDialogFragment.ChoseFileResListener, PointTabAdapter.TabClickListener {

    public static final String TAG = "ChatFragmentFan";

    private static final String TASK_BEAN = "taskBean";
    private static final String WINDOW_INFO_BEAN = "window_info_bean";

    private static final int TAKE_PICTURE = 0;

    private RecyclerView messageRecyView;

    private EditText messageInputEdit;

    private Button voiceAndSendBtn;

    private ImageView voiceView;

    private TaskBean taskBean;

    private ChatWindowInfoBean windowInfoBean;

    private boolean isCanSend = false;

    private View rootView;

    private ChatChosePersonFragment chosePersonFragment;

    private View chosePersonRootView, chatRootView;

    private TextView selectedPersonName;

    private boolean isChosePersonShow = false; // 加人fragment显示状态

    private TranslationAnimListener animListener;

    private ArrayList<UserInfoBean> selectedPerson;

    private View moreMenu;

    private View emojiViewContainer;

    private String filePath;

    private SwipeRefreshLayout refreshLayout;

//    private BitmapCacheManager arrayMap;

    private LoadFileManager loadFileManager;

    private int inputMethodHeight;

    private boolean isClickEnable; // 加人按钮可用标识

    private String notes; // 存放随手记内容

    private boolean isTaskChat;

    private FrameLayout popWindowFrameLayout;
    private RecyclerView popWindowList;

    private PopupWindow toPersonPopupWindow;

    private Dialog alertDialog;

    private int infoHeight = -1;
//    private boolean isSingle; // 单聊标识，用于单聊转群聊的判断

    private MessageBean clickMessageBean;

    private ChatFragmentHander handler;

    private EmotionKeyboard emotionKeyboard;

    private ViewPager emojiPage;


    public static Fragment getInstance(TaskBean taskBean, ChatWindowInfoBean windowInfoBean) {
        Log.i(TAG, "getInstance: " + (taskBean == null));
        Fragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(TASK_BEAN, taskBean);
        args.putParcelable(WINDOW_INFO_BEAN, windowInfoBean);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment getInstance(ChatWindowInfoBean windowInfoBean) {
        Fragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(WINDOW_INFO_BEAN, windowInfoBean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        registerEventHandlers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.layout_chat_fragment, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        //removeViewTreeObserver();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "chat fragment onDestroy");
        super.onDestroy();
        MessageStores.getInstance().setChatViewRefrence(null);
//        EmojiHandler.getInstance().clearSpans();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
    }

    public void clean() {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.CLOSE_ONE_CHAT_WINDOW);
        MessageStores.getInstance().closeWindow();
        if (Dispatcher.getInstance().isRegistered(this)) {
            Dispatcher.getInstance().unRegister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "chat fragment onPause");
        super.onPause();
    }


    private void registerEventHandlers() {
        Dispatcher.getInstance().register(this);
        if (taskBean != null)
            Log.i(TAG, "registerEventHandlers: " + taskBean.unReadMessageNum);
        MessageStores.getInstance().clearServerUnReadMsg(taskBean == null ? null : taskBean.taskId,
                windowInfoBean.groupId);
        if (taskBean != null && taskBean.unReadMessageNum > 0) {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, windowInfoBean.groupId);
            TimeLineStoresNew.getInstance().setUnReadMsgRead(taskBean.taskId, windowInfoBean.groupId);
        } else {
            if (windowInfoBean.groupId != null) {
                MessageStores.getInstance().removeUnReadMsg(windowInfoBean.groupId);
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.DELETE_UNREAD_MSG_BY_GROUPID, windowInfoBean.groupId);
                Dispatcher.getInstance().dispatchUpdateUIEvent(MessageActions.SET_VCHAT_UNREAD_NUM);
            }
        }
        if (windowInfoBean.groupId != null) {
            ArrayMap<String, Integer> notificationMap = ((PlayWorkApplication) getActivity().getApplication()).getNotificationMap();
            if (notificationMap.containsKey(windowInfoBean.groupId)) {
                int id = notificationMap.get(windowInfoBean.groupId);
                ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
            }
            MessageStores.getInstance().getMessageList(windowInfoBean, 0L);
        }
//        VChatBean vChatBean = ((ChatActivityNew) getActivity()).getvChatBean();
//        if (vChatBean != null && vChatBean.unReadMsgNum > 0) {
//            Log.i(TAG, "registerEventHandlers: unread num" + vChatBean.unReadMsgNum);
//            vChatBean.unReadMsgNum = 0;
//            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SET_UNREAD_MSG_READ, null, windowInfoBean.groupId);
//            MessageStores.getInstance().clearServerUnReadMsg(null, windowInfoBean.groupId);
//        }
        // 获取随手记内容
        if (taskBean != null) {
//            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.GET_NOTES_BY_GROUP_ID, windowInfoBean.groupId);
            MessageStores.getInstance().getNotesByGroupId(windowInfoBean.groupId);
        }
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void onEventMainThread(UpdateUIAction action) {
        switch (action.getActionType()) {
            // 创建新微聊
//            case MessageActions.CREATE_NEW_CHAT:
//
//                if (action.getActionData().get(0) == null) {
//                    break;
//                }
//                // 检查头像图片路径是否存在
//                checkDirExit();
//
//                boolean isSingle = windowInfoBean.isSingle;//之前的单聊标志
//
//                windowInfoBean = (ChatWindowInfoBean) action.getActionData().get(0);
//                MessageStores.getInstance().setCurrentWindow(windowInfoBean);
////                boolean isOldSingle = (boolean) action.getActionData().get(1);
//                ArrayList<String> chatGroupIds = MessageStores.getInstance().getChatGroupIds();
//                boolean isExisted = chatGroupIds.contains(windowInfoBean.groupId);
//
//                ((ChatActivityNew) getActivity()).setWindowInfoBean(windowInfoBean);
//                ((ChatActivityNew) getActivity()).setWindowGroupId(windowInfoBean.groupId);
//                windowInfoBean.memberNames = ((ChatActivityNew) getActivity()).setVChatBean(windowInfoBean);
//
//                VChatBean vChatBean = ((ChatActivityNew) getActivity()).getvChatBean();
//
//                if (!isExisted) {
//                    chatGroupIds.add(windowInfoBean.groupId);
//                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_INFO, windowInfoBean);
//                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_WINDOW_SUMMARY, vChatBean, false, false);
//                }
//
//                /*
//                UI操作
//                 */
//                if (windowInfoBean.isSingle) { // 单聊
////                    isSingle = true;
////                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.IS_SINGLE_CHAT_EXISTED, windowInfoBean.groupId);
//                    Log.i(TAG, "onEventMainThread: dealSingleChat");
//                    MessageStores.getInstance().dealSingleChat(windowInfoBean.groupId);
//                    ((ChatActivityNew) getActivity()).initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.memberNames, View.GONE, View.GONE, windowInfoBean.memberNames);
//                } else {
//                    if (isSingle) { // 单聊转群聊
//                        ((ChatActivityNew) getActivity()).setLastMessage(); // 清空消息列表之前，封装单聊的最后一条消息
//                        ((ChatActivityNew) getActivity()).clearTitleEdit(); // 话题框变为可编辑状态
//                        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).setAllMessageList(new ArrayList<MessageBean>(), isTaskChat);
//                        messageRecyView.getAdapter().notifyDataSetChanged();
//                    } else { // 群聊
//                        ((ChatActivityNew) getActivity()).changeTitleEdit();
//                    }
//                }
//
//                /*
//                发送创建消息
//                只有新建的单/群聊发消息，如果单聊已经存在则不发消息
//                 */
//                if (!windowInfoBean.isSingle) {
//                    this.sendCreateMsg();
//                }

//                break;
            case MessageActions.ADD_MEMBER:
                boolean addRes = (boolean) action.getActionData().get(0);
                if (addRes) {
                    if (windowInfoBean.chatMemberList.size() > 0) {
                        ArrayList<UserInfoBean> addMemberList = (ArrayList<UserInfoBean>) action.getActionData().get(1);
                        this.sendAddMsg(addMemberList);
                    }
                }
                break;
            case MessageActions.UPDATE_WINDOW_INFO_WHEN_ADD_MEMEBER:
                JSONObject info = (JSONObject) action.getActionData().get(0);
                String id = (String) action.getActionData().get(1);
                if (id != null) {
                    windowInfoBean.initList(info.optJSONArray("members"), info.optJSONArray("lastTo"), info.optJSONArray("exited"));
                    if (isChosePersonShow) {
                        chosePersonFragment.notifyChosePersonView();
                    }
                    return;
                }
                updateWindowInfoMembers(info);
                break;
            case MessageActions.MESSAGE_DELETE_GROUP:
                getActivity().finish();
                break;
        }
    }

    private void showMessageList(long time, ArrayList<MessageBean> messageList) {
        if (time == 0) {
            if (messageRecyView.getAdapter() == null) {
                MessageRecyclerAdapter adapter = new MessageRecyclerAdapter(messageRecyView, null, loadFileManager);
                adapter.setListener(this);
                adapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
                adapter.setAllMessageList(messageList, isTaskChat);
                // 设置是否是任务创建人
                if (taskBean != null && PreferencesHelper.getInstance().getCurrentUser().id.equals(taskBean.taskCreator)) {
                    adapter.setTaskCreator(true);
                } else {
                    adapter.setTaskCreator(false);
                }
                messageRecyView.setAdapter(adapter);
                if (notes != null) {
                    adapter.notifyCustomProperty(taskBean.taskPlace, notes, taskBean.setPrivate);
                }
            } else {
                ((MessageRecyclerAdapter) messageRecyView.getAdapter()).setAllMessageList(messageList, isTaskChat);
                messageRecyView.getAdapter().notifyDataSetChanged();
            }
            scrollToBottom();
        } else {
            ((MessageRecyclerAdapter) messageRecyView.getAdapter()).addMessageToShow(messageList);
            refreshLayout.setRefreshing(false);
        }
    }

    private void initData() {
//        arrayMap = ((ChatActivityNew) getActivity()).getArrayMap();
        loadFileManager = ((ChatActivityNew) getActivity()).getLoadFileManager();
        Bundle args = getArguments();
        if (args.containsKey(TASK_BEAN))
            taskBean = args.getParcelable(TASK_BEAN);
        windowInfoBean = args.getParcelable(WINDOW_INFO_BEAN);
        animListener = new TranslationAnimListener();
        selectedPerson = new ArrayList<>();
        handler = new ChatFragmentHander(this);
        MessageStores.getInstance().setChatViewRefrence(this);
        MessageStores.getInstance().setLoadFileManager(loadFileManager);
//        MessageStores.getInstance().setViewHandler(handler);
//        screenHeight = DeviceUtil.getDeviceScreenHeight(getActivity());
        inputMethodHeight = PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT);
        isClickEnable = true;
        notes = null;
        isTaskChat = taskBean != null;
        checkDirExit();
//        isSingle = windowInfoBean.isSingle;
    }

    private void checkDirExit() {
        String fileName = FileUtil.getImageFilePath() + windowInfoBean.groupId;
        if (!FileUtil.isDirExist(fileName)) {
            FileUtil.creatSDDir(AppConfig.IMAGE_DIR + windowInfoBean.groupId);
        }
        checkAttachmentDir();
    }

    private void checkAttachmentDir() {
        if (taskBean != null) {
            if (!FileUtil.isDirExist(FileUtil.getAttachmentPath() + taskBean.taskId)) {
                FileUtil.creatSDDir("playWork/attachment/" + taskBean.taskId);
            }
        } else {
            if (!FileUtil.isDirExist(FileUtil.getAttachmentPath() + windowInfoBean.groupId)) {
                FileUtil.creatSDDir("playWork/attachment/" + windowInfoBean.groupId);
            }
        }
    }

    private void initView(View v) {
        messageRecyView = (RecyclerView) v.findViewById(R.id.recy_message);
        messageRecyView.setHasFixedSize(true);
        messageRecyView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageRecyView.setOnTouchListener(touchListener);
        messageInputEdit = (EditText) v.findViewById(R.id.edit_message_input);
        voiceAndSendBtn = (Button) v.findViewById(R.id.btn_send_and_voice);
        voiceView = (ImageView) v.findViewById(R.id.img_voice);
        messageInputEdit.addTextChangedListener(messageWatcher);
        messageInputEdit.setOnFocusChangeListener(messageFocusListener);


        ImageView quickAddButton = (ImageView) v.findViewById(R.id.btn_quick_add);
        if (windowInfoBean != null && PreferencesHelper.getInstance().getServiceNumGroupId().equals(windowInfoBean.groupId)) {
            quickAddButton.setVisibility(View.GONE);
        } else {
            quickAddButton.setOnClickListener(voiceAndSendListener);
        }

        voiceAndSendBtn.setOnClickListener(voiceAndSendListener);
        voiceView.setOnClickListener(this);

        ImageView moreBtn = (ImageView) v.findViewById(R.id.btn_more);
        moreBtn.setOnClickListener(moreMenuToolsClickListener);

        chatRootView = v.findViewById(R.id.chat_root_view);
        selectedPersonName = (TextView) v.findViewById(R.id.tv_selected_person);
        moreMenu = v.findViewById(R.id.more_menu);
        View chosePicture = v.findViewById(R.id.chose_image);
        View takePhoto = v.findViewById(R.id.take_photo);
        View chooseFile = v.findViewById(R.id.ll_choose_file);// 选择文件按钮

        if (emojiViewContainer == null) {
            emojiViewContainer = v.findViewById(R.id.emoji_container);
            initEmojiViews(emojiViewContainer);
        }


        moreMenu.setVisibility(View.GONE); // 功能菜单默认隐藏
        chosePicture.setOnClickListener(moreMenuToolsClickListener);
        takePhoto.setOnClickListener(moreMenuToolsClickListener);
        chooseFile.setOnClickListener(moreMenuToolsClickListener);
        View smallMailLayout = v.findViewById(R.id.layout_small_mail); //小邮件
        smallMailLayout.setOnClickListener(this);
        View chooseAttachment = v.findViewById(R.id.ll_choose_attachment); // 附件列表
        chooseAttachment.setOnClickListener(moreMenuToolsClickListener);
        if (taskBean == null) {
            smallMailLayout.setVisibility(View.GONE);
            chooseAttachment.setVisibility(View.GONE);
        }

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        refreshLayout.setProgressBackgroundColorSchemeColor(ResourcesUtil.getInstance().getColor(R.color.gray_half));

        //addRootViewTreeObserver();
    }


    private void initEmojiViews(View emojiViewContainer) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) emojiViewContainer.getLayoutParams();
        params.height = PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT);
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        emojiViewContainer.setLayoutParams(params);
        emojiPage = (ViewPager) emojiViewContainer.findViewById(R.id.pager_emoji);
        EmojiPageAdapter adapter = new EmojiPageAdapter(getFragmentManager());
        adapter.setFragmentArrayList(EmojiHandler.getInstance().getEmojiFragmentList());
        emojiPage.setCurrentItem(0);
        emojiPage.setAdapter(adapter);

        RecyclerView tabView = (RecyclerView) emojiViewContainer.findViewById(R.id.recry_tab_point);
        tabView.setLayoutManager(new GridLayoutManager(getActivity(), adapter.getCount()));
        PointTabAdapter tabAdapter = new PointTabAdapter(tabView, adapter.getCount(), 0);
        tabAdapter.setListener(this);
        tabView.setAdapter(tabAdapter);

        emojiPage.addOnPageChangeListener(tabAdapter);

//        emojiIndicatorView = (EmojiIndicatorView) emojiViewContainer.findViewById(R.id.emoji_tab);
//        emojiIndicatorView.initIndicator(adapter.getCount());

        emotionKeyboard = EmotionKeyboard.with(getActivity())
                .setEmotionView(rootView.findViewById(R.id.emoji_container))//绑定表情面板
                .bindToContent(chatRootView)//绑定内容view
                .bindToEditText(messageInputEdit)//判断绑定那种EditView
                .bindToEmotionButton(rootView.findViewById(R.id.emotion_button))//绑定表情按钮
                .build();
    }

    @Override
    public void onSizeChange(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChange: ");
        if ((oldh - h == inputMethodHeight) && !isChosePersonShow && !noteOrPlaceHasFocues && !((ChatActivityNew) getActivity()).isSmallMailFragmentShow()) {
            if (emojiViewContainer.getVisibility() == View.VISIBLE) {
                emojiViewContainer.setVisibility(View.GONE);
            }
            scrollToBottom();
        }
    }


    private View.OnClickListener moreMenuToolsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_more:
                    if (windowInfoBean.groupId == null) {
                        return;
                    }
                    hideInputMethod();
                    // 隐藏加人fragment
                    if (isChosePersonShow) {
                        hideChosePersonFragment();
                    }
                    if (moreMenu.getVisibility() == View.GONE)
                        moreMenu.setVisibility(View.VISIBLE);
                    else
                        moreMenu.setVisibility(View.GONE);
                    break;
                case R.id.chose_image:
                    moreMenu.setVisibility(View.GONE);
                    if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 102))
                        ((ChatActivityNew) getActivity()).showChosePictureFragment();
                    break;
                case R.id.take_photo:
                    moreMenu.setVisibility(View.GONE);
                    if (DeviceUtil.getPermission(getActivity(), Manifest.permission.CAMERA, 100))
                        openCamera();
                    break;
                case R.id.ll_choose_file:// 选择文件
                    moreMenu.setVisibility(View.GONE);

                    if (DeviceUtil.getPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 101))
                        choseFile();
                    break;
                case R.id.ll_choose_attachment:
                    if (taskBean != null) {
                        Intent in = new Intent(getActivity(), TaskAttachmentActivity.class);
                        in.putExtra("taskBean", taskBean);
                        startActivity(in);
                    }
                    break;
            }
        }
    };


    public void choseFile() {
        ChoseFileDialogFragment dialogFragment = new ChoseFileDialogFragment();
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setListener(ChatFragment.this);
    }


    public void openCamera() {
//        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        filePath = FileUtil.getImageFilePath() + windowInfoBean.groupId + File.separator;
        filePath = filePath + PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME) + "_" + System.currentTimeMillis() + ".png";
//        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
        Log.i(TAG, "openCamera: " + filePath);
        startActivityForResult(CommonUtils.getTakePhoteIntent(getActivity(), filePath), TAKE_PICTURE);
    }


    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.recy_message:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        messageInputEdit.clearFocus(); // 清除消息输入框焦点
                        MessageRecyclerAdapter.ViewHolder chatNoteViewHolder = (MessageRecyclerAdapter.ViewHolder) messageRecyView.findViewHolderForAdapterPosition(0);
                        if (chatNoteViewHolder != null && isTaskChat) {
                            chatNoteViewHolder.quickNoteEditText.clearFocus(); // 清除任务地点输入框焦点
                            chatNoteViewHolder.taskPlaceEditText.clearFocus(); // 清除随手记输入框焦点
                        }
                        /*
                        新建聊天时话题框丢失焦点，输入框获取焦点
                         */
                        boolean hasFocus = ((ChatActivityNew) getActivity()).titleEditFocused();
                        if (hasFocus && (windowInfoBean.groupId == null)) {
                            messageInputEdit.requestFocus();
                        } else {
                            hideInputMethod(); // 隐藏输入法
                        }
                        ((ChatActivityNew) getActivity()).changeTitleEdit();
                    }
                    break;
            }
            return false;
        }
    };

    private void showChosePersonFragment() {

        if (chosePersonRootView == null) {
            /*
            创建一个FrameLayout布局
             */
            chosePersonRootView = new FrameLayout(getActivity());
            chosePersonRootView.setLayoutParams(
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            chosePersonRootView.setId(R.id.chose_person_id);
        }

        ((ViewGroup) chatRootView).addView(chosePersonRootView);

        if (chosePersonFragment == null) {
            chosePersonFragment = new ChatChosePersonFragment();
            chosePersonFragment.setTaskInfo(taskBean);
            chosePersonFragment.setChatWindowInfo(windowInfoBean);
            chosePersonFragment.setListener(this);
            chosePersonFragment.setCancelQuickChatListener(this);
//            chosePersonFragment.setBitmapArrayMap(arrayMap);
        }

        getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fragment_xfraction_in, 0)
                .add(R.id.chose_person_id, chosePersonFragment).commit();
        isClickEnable = true;
        isChosePersonShow = true;
    }

    public void hideChosePersonFragment() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(chosePersonFragment.getView(), "xFraction", 0, 1);
        oa.setDuration(300);
        oa.addListener(animListener);
        oa.start();
        chosePersonFragment.setOkBtnClickable(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_content1:
                alertDialog.dismiss();
                if (!TextUtils.isEmpty(clickMessageBean.id)) {
//                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.DELETE_ONE_CHAT_MESSAGE, clickMessageBean.id, clickMessageBean.groupId, taskBean == null ? null : taskBean.taskId, clickMessageBean.isSendSuccess);
                    MessageStores.getInstance().deleteOneChatMsg(clickMessageBean.id, clickMessageBean.groupId, taskBean == null ? null : taskBean.taskId, clickMessageBean.isSendSuccess);
                } else if (!TextUtils.isEmpty(clickMessageBean.uuid)) {
//                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.DELETE_ONE_CHAT_MESSAGE, clickMessageBean.uuid, clickMessageBean.groupId, taskBean == null ? null : taskBean.taskId, clickMessageBean.isSendSuccess);
                    MessageStores.getInstance().deleteOneChatMsg(clickMessageBean.uuid, clickMessageBean.groupId, taskBean == null ? null : taskBean.taskId, clickMessageBean.isSendSuccess);
                }
                break;
            case R.id.tv_content2:
                alertDialog.dismiss();
                if (clickMessageBean.isCanReCall() && !TextUtils.isEmpty(clickMessageBean.id)) {
//                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.RECALL_COE_CHAT_MESSAGE, clickMessageBean);
                    MessageStores.getInstance().recallOneMessage(clickMessageBean);
                } else {
                    UItoolKit.showToastShort(getActivity(), "只能撤回两分钟之内的消息");
                }
                break;
            case R.id.tv_content3://回复
                alertDialog.dismiss();
                if (clickMessageBean.isSmallMailMsg()) {
                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID, "reply_small_mail", clickMessageBean.smallMailId);
                } else {
                    Log.i(TAG, "onClick: " + clickMessageBean.toArray);
                    if (clickMessageBean.to != null) {
                        selectedPerson.clear();
                        try {
                            JSONArray toArray = new JSONArray(clickMessageBean.to);
                            int length = toArray.length();
                            Log.i(TAG, "onClick: " + clickMessageBean.to);
                            for (int i = 0; i < length; i++) {
                                JSONObject user = toArray.optJSONObject(i);
                                UserInfoBean userBean = new UserInfoBean(user);
                                if (!userBean.id.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                                    selectedPerson.add(userBean);
                                }
                            }
                            if (!clickMessageBean.isCurrentUserMsg()) {
                                selectedPerson.add(clickMessageBean.sendMessageUser);
                            }
                            showSelectedPersonName();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.tv_content4://回复全部
                alertDialog.dismiss();
                Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID, "reply_all", clickMessageBean.smallMailId);
                break;
            case R.id.tv_content6:
                CommonUtils.copy(getActivity(), clickMessageBean.content);
                UItoolKit.showToast(getActivity(), "已复制", 500);
                alertDialog.dismiss();
                break;
            case R.id.tv_confrim:
                reSendDialog.dismiss();
                reSendMsg(clickMessageBean);
                break;
            case R.id.tv_cancel:
                reSendDialog.dismiss();
                break;
            case R.id.layout_small_mail:
                this.sendSmallMail();
                break;
            case R.id.img_voice:
//                Log.i(TAG, "onClick: ");
//                if (emojiViewContainer.getVisibility() == View.GONE) {
//                    emojiViewContainer.setVisibility(View.VISIBLE);
//                    scrollToBottom();
//                } else {
//                    emojiViewContainer.setVisibility(View.GONE);
//                }
                break;
        }
    }

    @Override
    public void onFileSelect(final ArrayList<LocalFileBean> choseFileList) {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                for (LocalFileBean mBean : choseFileList) {
//                    Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEND_FILE_MSG, mBean.currentPath, getSendToUsers());
                    if (mBean != null) {
                        MessageStores.getInstance().sendFileMsg(mBean.currentPath, getSendToUsers(), handler);
                    }
                }
            }
        });
    }

    @Override
    public void showMessageHistory(final ArrayList<MessageBean> messageList, final long time) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showMessageList(time, messageList);
            }
        });
    }

    @Override
    public void onSendMessageResult(final boolean isSuccess, final String uuid) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).updateView(uuid);
                } else {
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).sendMessageFailed(uuid);
                }
            }
        });
    }

    @Override
    public void onReciveMessage(final String groupId, final MessageBean messageBean) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (groupId.equals(windowInfoBean.groupId)) {
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).addMessage(messageBean);
                }
            }
        });
    }

    @Override
    public void onGetNotes(final String notes) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (messageRecyView.getAdapter() != null) {
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).notifyCustomProperty(taskBean.taskPlace, notes, taskBean.setPrivate);
                }
            }
        });
    }

    @Override
    public void onHaveNoMoreMeesage() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                UItoolKit.showToastShort(getActivity(), "没有更多可加载的消息");
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void setTaskProperty(final boolean result, final CustomProperty customProperty) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                switch (customProperty.method) {
                    case MessageActions.SET_TASK_PLACE: // 设置任务地点
                        if (result) {
                            ((ChatActivityNew) getActivity()).setTaskCustomProperty(customProperty.taskPlace, taskBean.setPrivate, taskBean.taskContent);
                        }
                        break;
                    case MessageActions.SET_TASK_PRIVATE: // 设置任务公/私
                        if (result) {
                            ((MessageRecyclerAdapter) messageRecyView.getAdapter()).notifyCustomProperty(taskBean.taskPlace, notes, customProperty.taskPrivate);
                            ((MessageRecyclerAdapter) messageRecyView.getAdapter()).setPrivateClicked(true);
                            ((ChatActivityNew) getActivity()).setTaskCustomProperty(taskBean.taskPlace, customProperty.taskPrivate, taskBean.taskContent);
                        }
                        break;
                    case MessageActions.SET_TASK_TITLE: // 设置任务名称
                        if (result) {
                            ((ChatActivityNew) getActivity()).setTaskCustomProperty(taskBean.taskPlace, taskBean.setPrivate, customProperty.taskTitle);
                            ((ChatActivityNew) getActivity()).setTaskTitle();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onSetNotesResult(final boolean result, final String note) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result) {
                    notes = note;
                }
            }
        });

    }

    @Override
    public void onUpdateGroupName(final boolean result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result) {
                    ((ChatActivityNew) getActivity()).setTaskTitle();
                    Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.UPDATE_CHAT_WINDOW_BY_GROUP_ID, 2, windowInfoBean);
                    UItoolKit.showToastShort(getActivity(), "话题修改成功");
                } else {
                    UItoolKit.showToastShort(getActivity(), "话题修改失败");
                }
            }
        });
    }

    @Override
    public void onDeleteOneMessage(final boolean result, final String msgId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result) {
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).deletMsgByMsgId(msgId);
                } else {
                    UItoolKit.showToastShort(getActivity(), "删除失败，请检查网络");
                }
            }
        });
    }

    @Override
    public void onRecallOneMessage(final boolean result, final String msgId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result)
                    ((MessageRecyclerAdapter) messageRecyView.getAdapter()).recallMsgByMsgId(msgId);
            }
        });
    }

    @Override
    public void showMsgBeforeSendToServer(final MessageBean messageBean) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                initMesaageAdapter();
                ((MessageRecyclerAdapter) messageRecyView.getAdapter()).addMessage(messageBean);
            }
        });
    }

    @Override
    public void onGetSmallMailDetail(final String mark, final SmallMailBean smallMailBean) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onGetSmallMailDetail: " + mark);
                smallMailBean.setAttachmentsTaskId(taskBean.taskId);
                switch (mark) {
                    case "reply_small_mail":
                        smallMailBean.initToUserNames();
                        smallMailBean.toUserList.clear();
                        smallMailBean.toUserList.add(smallMailBean.sendUser);
                        smallMailBean.sendUser = PreferencesHelper.getInstance().getCurrentUser();
                        ((ChatActivityNew) getActivity()).showSmallMailFragment(smallMailBean, SmallMailBean.REPLY_MODE);
                        break;
                    case "reply_all":
                        smallMailBean.initToUserNames();
                        smallMailBean.toUserList.add(smallMailBean.sendUser);
                        smallMailBean.sendUser = PreferencesHelper.getInstance().getCurrentUser();
                        Iterator<UserInfoBean> it = smallMailBean.toUserList.iterator();
                        while (it.hasNext()) {
                            if (it.next().id.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
                                it.remove();
                            }
                        }
                        ((ChatActivityNew) getActivity()).showSmallMailFragment(smallMailBean, SmallMailBean.REPLY_MODE);
                        break;
                    case "view_mail":
                        Log.i(TAG, "onEventMainThread: ===view_mail" + smallMailBean.toString());
                        ((ChatActivityNew) getActivity()).showSmallMailFragment(smallMailBean, SmallMailBean.VIEW_MODE);
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateNewChat(final ChatWindowInfoBean chatWindowInfoBean) {
        final boolean isSingle = windowInfoBean.isSingle;//之前的单聊标志

        if (chatWindowInfoBean != null) {
            checkDirExit();
            windowInfoBean = chatWindowInfoBean;
//            MessageStores.getInstance().setCurrentWindow(windowInfoBean);

            ((ChatActivityNew) getActivity()).setWindowInfoBean(windowInfoBean);
            ((ChatActivityNew) getActivity()).setWindowGroupId(windowInfoBean.groupId);
        }


//        Log.i(TAG, "onCreateNewChat: " + isSingle);

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (chatWindowInfoBean == null) {
                    UItoolKit.showToastShort(getActivity(), "创建聊天失败");
                } else {
                    if (chatWindowInfoBean.isSingle) { // 单聊
                        Log.i(TAG, "onEventMainThread: dealSingleChat");
                        ((ChatActivityNew) getActivity()).initTitleView(View.VISIBLE, View.VISIBLE, windowInfoBean.memberNames, View.GONE, View.GONE, windowInfoBean.memberNames);
                    } else {
                        if (isSingle) { // 单聊转群聊
                            ((ChatActivityNew) getActivity()).clearTitleEdit(); // 话题框变为可编辑状态
                            ((MessageRecyclerAdapter) messageRecyView.getAdapter()).setAllMessageList(new ArrayList<MessageBean>(), isTaskChat);
                            messageRecyView.getAdapter().notifyDataSetChanged();
                        } else { // 群聊
                            ((ChatActivityNew) getActivity()).changeTitleEdit();
                        }
                    }

                    if (!chatWindowInfoBean.isSingle) {
                        sendCreateMsg();
                    }
                }
            }
        });
    }

    @Override
    public void renameChatSubject(String subject) {
        Log.d(TAG, "renameChatSubject() called with: subject = [" + subject + "]");
        ((ChatActivityNew) getActivity()).setTaskTitle(subject);
    }

    public void onEmojiClick(String emoji) {
        EmojiHandler.getInstance().appendEmoji(messageInputEdit, emoji, (int) (messageInputEdit.getTextSize() * 15 / 10));
        messageInputEdit.setSelection(messageInputEdit.length());
    }

    @Override
    public void onTabClick(int selectIndex) {
        emojiPage.setCurrentItem(selectIndex, true);
    }

    public void onBackspaceClick() {
        messageInputEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    public boolean isInterceptBackPress() {
        return emotionKeyboard.interceptBackPress();
    }

    private class TranslationAnimListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            chosePersonFragment.setIsQuickChat(true);
            chosePersonFragment.setContactViewVisibility(View.INVISIBLE, View.GONE);
            getFragmentManager().beginTransaction().remove(chosePersonFragment).commit();
            ((ViewGroup) chatRootView).removeView(chosePersonRootView);
            isClickEnable = true;
            isChosePersonShow = false;
        }
    }

    @Override
    public void onPersonSelected(UserInfoBean userInfoBean) {
        if (selectedPerson.contains(userInfoBean))
            selectedPerson.remove(userInfoBean);
        else
            selectedPerson.add(userInfoBean);
        showSelectedPersonName();
    }

    private void showSelectedPersonName() {
        int size = selectedPerson.size();
        if (size > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("发送给：");
            int count = 0;
            for (UserInfoBean user : selectedPerson) {
                sb.append(user.name);
                if (count + 1 < size) {
                    sb.append("、");
                }
                count++;
            }
            if (selectedPersonName.getVisibility() == View.GONE) {
                selectedPersonName.setVisibility(View.VISIBLE);
            }
            selectedPersonName.setText(sb.toString());
        } else {
            selectedPersonName.setVisibility(View.GONE);
        }
    }

    @Override
    public void cancelQuickChat() {
        /*
        清空快捷选人内容
         */
        selectedPerson.clear();
        showSelectedPersonName();
    }

    private View.OnClickListener voiceAndSendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_send_and_voice:
                    // 隐藏加人fragment
                    if (isChosePersonShow) {
                        hideChosePersonFragment();
                        selectedPersonName.setVisibility(View.GONE);
                    }
                    if (windowInfoBean.groupId == null) {
                        UItoolKit.showToastShort(getActivity(), "请添加联系人");
                        return;
                    }
                    if (isCanSend) {
                        String content = messageInputEdit.getText().toString();
                        int type = MessageBean.TEXT_MESSAGE_SEND;
                        if (EmojiHandler.getInstance().hasEmoji(content)) {
                            type = MessageBean.EMOJI_MESSAGE_SEND;
                        }
                        sendMsg(content, type, getSendToUsers());
                    }
                    break;
                case R.id.btn_quick_add:
                    if (isClickEnable) {
                        isClickEnable = false;
                        messageInputEdit.clearFocus(); // 清除聊天输入框焦点
                        hideInputMethod(); // 隐藏输入法
                        emotionKeyboard.hideEmotionLayout(false);
                        if (isChosePersonShow) {
                            if (selectedPerson.size() > 0) { // 如果开始了快速聊天模式
                                cancelQuickChat(); // 取消快速聊天
                            }
                            hideChosePersonFragment();
                        } else {
//                            selectedPerson.clear();
                            showChosePersonFragment();
                        }
                    }
                    break;
            }
        }
    };

    private void sendMsg(String content, int type, ArrayList<UserInfoBean> toList) {
        MessageStores.getInstance().sendOneMessage(content, type, toList);
        messageInputEdit.setText("");
        cancelQuickChat();
    }

    private void reSendMsg(MessageBean sendMessageBean) {
        MessageStores.getInstance().reSendOneMessage(sendMessageBean, handler);
        messageInputEdit.setText("");
    }


//    private void sendMsg(MessageBean sendMessageBean, boolean toMe) {
//        sendMessageBean.sendTime = System.currentTimeMillis();
//        sendMessageBean.groupId = windowInfoBean.groupId;
//        sendMessageBean.isMessageNew = true;
//        sendMessageBean.isSendSuccess = false;
//        if (sendMessageBean.type != MessageBean.IMAGE_MESSAGE_SEND) {
//            UUID uuid = UUID.randomUUID();
//            sendMessageBean.uuid = uuid.toString();
//            this.initMesaageAdapter();
//            ((MessageRecyclerAdapter) messageRecyView.getAdapter()).addMessage(sendMessageBean);
//        }
//
//        if (toMe) {
//            JSONArray toArray = new JSONArray();
//            sendMessageBean.to = toArray.toString();
//            sendMessageBean.initToList(toArray);
//
//            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.INSERT_ONE_CHAT_MESSAGE, sendMessageBean);
//
//            MessageStores.getInstance().sendChatMsg(sendMessageBean, windowInfoBean.mailId);
//        }
//
//        messageInputEdit.setText("");
//    }

    private TextWatcher messageWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (TextUtils.isEmpty(s.toString())) {
                isCanSend = false;
//                voiceAndSendBtn.setText("");
//                voiceAndSendBtn.setBackgroundResource(R.drawable.chat_voice_button_background);
                voiceView.setVisibility(View.VISIBLE);
                voiceAndSendBtn.setVisibility(View.GONE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString().trim())) {
                voiceView.setVisibility(View.VISIBLE);
                voiceAndSendBtn.setVisibility(View.GONE);
                isCanSend = false;
            } else {
                voiceView.setVisibility(View.GONE);
                voiceAndSendBtn.setVisibility(View.VISIBLE);
                isCanSend = true;
            }
        }
    };

    private View.OnFocusChangeListener messageFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && isChosePersonShow) {
                hideChosePersonFragment();
            }
        }
    };

    private void scrollToBottom() {
        if (messageRecyView.getAdapter() != null) {
            messageRecyView.scrollToPosition(messageRecyView.getAdapter().getItemCount() - 1);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
//                    ThreadPool.exec(new Runnable() {
//                        @Override
//                        public void run() {
//                            Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEND_PICTURE_MESSAGE, filePath, filePath, getSendToUsers());
                    MessageStores.getInstance().sendImageMessage(getActivity(), filePath, filePath, getSendToUsers(), handler);
//                        }
//                    });
                    break;
                default:
                    break;
            }
        }
    }

    private ArrayList<UserInfoBean> getSendToUsers() {
        return selectedPerson.size() > 0 ? selectedPerson : windowInfoBean.chatMemberList;
    }

    @SuppressLint("InflateParams")
    private void showPopWindow(MessageBean messageBean, int pos) {

        if (popWindowList == null) {
            View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_pop_reclyerview, null);
            popWindowFrameLayout = (FrameLayout) rootView.findViewById(R.id.frame_pop_view);
            popWindowList = (RecyclerView) rootView.findViewById(R.id.recycler_pop_view);
            popWindowList.setLayoutManager(new GridLayoutManager(getActivity(), 5));
            PopWindowAdapter adapter = new PopWindowAdapter();
//            adapter.setAvatarCache(arrayMap);
            adapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
            adapter.setUserBeanList(messageBean.toUserBean);
            popWindowList.setAdapter(adapter);
            toPersonPopupWindow = new PopupWindow(rootView, DeviceUtil.dpTopx(getActivity(), 59) * 5, DeviceUtil.dpTopx(getActivity(), 65 * 2));
            toPersonPopupWindow.setFocusable(true);
            toPersonPopupWindow.setOutsideTouchable(true);
            //noinspection deprecation
            toPersonPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        } else {
            PopWindowAdapter adapter = (PopWindowAdapter) popWindowList.getAdapter();
            adapter.setUserBeanList(messageBean.toUserBean);
            adapter.notifyDataSetChanged();
        }
        if (infoHeight == -1) {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            infoHeight = r.top + DeviceUtil.dpTopx(getActivity(), 48);
        }
        MessageRecyclerAdapter.ViewHolder viewHolder = (MessageRecyclerAdapter.ViewHolder) messageRecyView.findViewHolderForLayoutPosition(pos);
        int[] location = new int[2];
        viewHolder.itemView.getLocationInWindow(location);
        if (messageBean.sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id)) {
            /*
            发送消息显示的弹出框
             */
            popWindowFrameLayout.setBackgroundResource(R.drawable.bg_send_window);
            int y = location[1] - toPersonPopupWindow.getHeight();
            if (y < infoHeight)
                y = location[1] + DeviceUtil.dpTopx(getActivity(), 70);
            toPersonPopupWindow.showAtLocation(viewHolder.itemView, Gravity.NO_GRAVITY, DeviceUtil.getDeviceScreenWidth(getActivity()) - DeviceUtil.dpTopx(getActivity(), 305), y);
        } else {
            /*
            接收消息显示的弹出框
             */
            popWindowFrameLayout.setBackgroundResource(R.drawable.bg_receive_window);
            int y = location[1] - toPersonPopupWindow.getHeight();
            if (y < infoHeight)
                y = location[1] + DeviceUtil.dpTopx(getActivity(), 70);
            toPersonPopupWindow.showAtLocation(viewHolder.itemView, Gravity.NO_GRAVITY, location[0] + DeviceUtil.dpTopx(getActivity(), 10), y);
        }
    }

    public void sendChoseImage(final String path) {
        final String destPath = FileUtil.getImageFilePath() + windowInfoBean.groupId + File.separator + PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME) + "_" + System.currentTimeMillis() + ".png";
//        ThreadPool.exec(new Runnable() {
//            @Override
//            public void run() {
//                Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SEND_PICTURE_MESSAGE, path, destPath, getSendToUsers());
        MessageStores.getInstance().sendImageMessage(getActivity(), path, destPath, getSendToUsers(), handler);
//            }
//        });
    }

    private void hideInputMethod() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRefresh() {
        MessageRecyclerAdapter adapter = (MessageRecyclerAdapter) messageRecyView.getAdapter();
        if (adapter == null || adapter.getFirstShowMessageSendTime() < 0) {
            UItoolKit.showToastShort(getActivity(), "没有更多可加载的消息");
            refreshLayout.setRefreshing(false);
        } else {
            Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.GET_GROUP_CHAT_HISTORY, windowInfoBean.groupId, adapter.getFirstShowMessageSendTime(), (taskBean == null) ? "" : taskBean.taskId);
        }
    }

    private static class ChatFragmentHander extends Handler {

        private WeakReference<ChatFragment> chatFragmentReference;

        ChatFragmentHander(ChatFragment fragment) {
            chatFragmentReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LoadFileHandlerThread.UPDATE_UPLOAD_PROGRESS: {
                    int type = msg.arg2;
                    int precent = msg.arg1;
                    if (type == LoadFileManager.UPLOAD_MSG_PICTURE) {
                        String uuid = (String) msg.obj;
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().updateUpLoadImgProgress(uuid, precent);
                        }
                    } else if (type == LoadFileManager.UPLOAD_MSG_ATTACHMENT) {
                        String uuid = (String) msg.obj;
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().updateUpLoadFileProgress(uuid, precent);
                        }
                    }
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_SUCCESS: {
                    int type = msg.arg2;
                    if (type == LoadFileManager.UPLOAD_MSG_PICTURE) {
                        String uuid = (String) msg.obj;

                        String response = msg.peekData().getString(LoadFileHandlerThread.UPLOAD_SUCCESS_RESPONSE);

                        MessageStores.getInstance().upLoadPictureSuccess(uuid, response);
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().upLoadPictureSuccess(uuid);
                        }
                    } else if (type == LoadFileManager.UPLOAD_MSG_ATTACHMENT) {
                        String uuid = (String) msg.obj;

                        String response = msg.peekData().getString(LoadFileHandlerThread.UPLOAD_SUCCESS_RESPONSE);

                        MessageStores.getInstance().upLoadFileSuccess(uuid, response);
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().upLoadFileSuccess(uuid);
                        }
                    }
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_FAILURE: {
                    int type = msg.arg2;
                    if (type == LoadFileManager.UPLOAD_MSG_PICTURE) {
                        String uuid = (String) msg.obj;

                        MessageStores.getInstance().upLoadPictureFailue(uuid);
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().upLoadPictureFailue(uuid);
                        }
                    } else if (type == LoadFileManager.UPLOAD_MSG_ATTACHMENT) {
                        String uuid = (String) msg.obj;

                        MessageStores.getInstance().upFileFailue(uuid);
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().upFileFailue(uuid);
                        }
                    }
                    break;
                }
                case LoadFileHandlerThread.DOWNLOAD_SUCCESS: {
                    int type = msg.arg2;
                    if (type == LoadFileManager.DOWNLOAD_MSG_ATTACHMENT) {
                        String id = (String) msg.obj;
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().downLoadFileSuccess(id);
                        }
                    }
                    break;
                }
                case LoadFileHandlerThread.UPDATE_DOWNLOAD_PROGRESS: {
                    int type = msg.arg2;
                    if (type == LoadFileManager.DOWNLOAD_MSG_ATTACHMENT) {
                        String id = (String) msg.obj;
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().updateDownLoadFileProgress(id, msg.arg1);
                        }
                    }
                    break;
                }
                case LoadFileHandlerThread.DOWNLOAD_FAILURE: {
                    int type = msg.arg2;
                    if (type == LoadFileManager.DOWNLOAD_MSG_ATTACHMENT) {
                        String id = (String) msg.obj;
                        if (chatFragmentReference.get() != null) {
                            chatFragmentReference.get().downLoadFileFailure(id);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void downLoadFileFailure(String id) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).downloadFileFailure(id);
    }

    private void updateDownLoadFileProgress(String id, int precent) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).updateDownLoadFileProgress(id, precent);
    }

    private void downLoadFileSuccess(String id) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).downloadFileSucess(id);
    }

    private void upFileFailue(String uuid) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).uploadFileFailure(uuid);
    }

    private void upLoadFileSuccess(String uuid) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).uploadFileSuccess(uuid);
    }

    private void updateUpLoadFileProgress(String uuid, int precent) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).updateUpLoadFileProgress(uuid, precent);
    }

    private void upLoadPictureFailue(String uuid) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).uploadImgFailure(uuid);
    }

    private void upLoadPictureSuccess(String uuid) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).upLoadImgSuccess(uuid);
    }

    private void updateUpLoadImgProgress(String uuid, int precent) {
        ((MessageRecyclerAdapter) messageRecyView.getAdapter()).updateUpLoadImgProgress(uuid, precent);
    }

    @Override
    public void onImgClick(MessageBean bean) {
        Intent intent = new Intent(getActivity(), ImageViewActivity.class);
        intent.putExtra("groupId", windowInfoBean.groupId);
        intent.putExtra("imagePath", bean.imagePath);
        startActivity(intent);
    }

    @Override
    public void onNoteFocusChanged(String quickNote) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SET_NOTES_BY_GROUP_ID, windowInfoBean.groupId, quickNote);
        MessageStores.getInstance().setNotesByGroupId(windowInfoBean.groupId, quickNote);
    }

    @Override
    public void onPlaceFocusChanged(String taskPlace) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SET_TASK_PLACE, taskBean.taskId, taskPlace);
        MessageStores.getInstance().setTaskPlace(taskBean.taskId, taskPlace);
    }

    private boolean noteOrPlaceHasFocues = false;

    @Override
    public void onNoteAndPlaceFocusChanged(boolean hasFocus) {
        noteOrPlaceHasFocues = hasFocus;
    }

    @Override
    public void onAvatarClick(MessageBean bean, int pos) {
        if (!windowInfoBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
            showPopWindow(bean, pos);
        } else {
            if (windowInfoBean.allMemberList.contains(PreferencesHelper.getInstance().getCurrentUser()))
                showPopWindow(bean, pos);
        }
    }

    @Override
    public void onPrivateClick(int taskPrivate) {
//        Dispatcher.getInstance().dispatchStoreActionEvent(MessageActions.SET_TASK_PRIVATE, taskBean.taskId, taskPrivate);
        MessageStores.getInstance().setTaskPrivate(taskBean.taskId, taskPrivate);
    }

    @Override
    public void onItemLongClick(MessageBean messageBean, int pos) {
        clickMessageBean = messageBean;
        boolean showRecall, isSmallMail, isShowCopy;
        showRecall = clickMessageBean.isCanReCall() && clickMessageBean.isCurrentUserMsg();
        isSmallMail = messageBean.isSmallMailMsg();
        isShowCopy = !messageBean.isImageMsg() && !messageBean.isAttachmentMsg();
        showPhotoDialog(showRecall, isSmallMail, isShowCopy);
    }

    @Override
    public void onResendClcik(MessageBean messageBean, int pos) {
        clickMessageBean = messageBean;
        showReSendDialog();
    }

    @Override
    public void onSmallMailClick(MessageBean messageBean, int pos) {
        Log.d(TAG, "onSmallMailClick() called with: " + "messageBean = [" + messageBean + "], pos = [" + pos + "]");
        Dispatcher.getInstance().dispatchDataBaseAction(DataBaseActions.QUERY_SMALL_MAIL_BY_MESSAGE_ID, "view_mail", messageBean.smallMailId);
    }

    @Override
    public void onFileMsgClick(MessageBean messageBean, int pos) {
        if (messageBean.isCurrentUserMsg()) {
            Log.i(TAG, "onFileMsgClick: " + messageBean.attachmentBean.localPath);
            if (messageBean.attachmentBean.localPath != null) {
                File file = new File(messageBean.attachmentBean.localPath);
                if (file.exists()) {
                    openFile(messageBean.attachmentBean.localPath);
                } else {
                    if (!TextUtils.isEmpty(messageBean.id) && !TextUtils.isEmpty(messageBean.attachmentBean.docId)) {
                        downLoadFile(messageBean.attachmentBean.getAttachDownLoadUrl(), messageBean.attachmentBean.getAttachFilePath(),
                                TextUtils.isEmpty(messageBean.uuid) ? messageBean.id : messageBean.uuid);
                    }
                }
            } else {
                if (messageBean.attachmentBean.isAttachmentDownloaded()) {
                    openFile(messageBean.attachmentBean.getAttachFilePath());
                } else {
                    if (!TextUtils.isEmpty(messageBean.id) && !TextUtils.isEmpty(messageBean.attachmentBean.docId)) {
                        downLoadFile(messageBean.attachmentBean.getAttachDownLoadUrl(), messageBean.attachmentBean.getAttachFilePath(),
                                TextUtils.isEmpty(messageBean.uuid) ? messageBean.id : messageBean.uuid);
                    }
                }
            }
        } else {
            if (messageBean.attachmentBean.isAttachmentDownloaded()) {
                openFile(messageBean.attachmentBean.getAttachFilePath());
            } else {
                downLoadFile(messageBean.attachmentBean.getAttachDownLoadUrl(), messageBean.attachmentBean.getAttachFilePath(),
                        messageBean.id);
            }
        }
    }

    private void downLoadFile(String attachDownLoadUrl, String attachFilePath, String clientId) {
        Log.d(TAG, "downLoadFile() called with: " + "attachDownLoadUrl = [" + attachDownLoadUrl + "], attachFilePath = [" + attachFilePath + "], clientId = [" + clientId + "]");
        loadFileManager.downLoadFile(attachDownLoadUrl, attachFilePath, clientId, handler, true, LoadFileManager.DOWNLOAD_MSG_ATTACHMENT);
    }

    private void openFile(String path) {
        Intent intent = FileUtil.getOpenFileIntent(getActivity(), path);
        if (intent == null)
            UItoolKit.showToastShort(getActivity(), "未识别的文件类型");
        else
            try {
                getActivity().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                UItoolKit.showToastShort(getActivity(), "没有找到应用程序打开该类型的文件");
            }
    }

    private Dialog reSendDialog;

    private void showReSendDialog() {
        if (reSendDialog == null) {
            reSendDialog = new Dialog(getActivity(), R.style.normal_dialog);
            reSendDialog.setContentView(R.layout.layout_resend_message);

            Window window = reSendDialog.getWindow();
            assert window != null;
            WindowManager.LayoutParams wmlp = window.getAttributes();
            wmlp.width = (int) (DeviceUtil.getDeviceScreenWidth(getActivity()) * 0.809);
            window.setAttributes(wmlp);

            TextView confrimTextView = (TextView) reSendDialog.findViewById(R.id.tv_confrim);
            TextView cancelTextView = (TextView) reSendDialog.findViewById(R.id.tv_cancel);

            confrimTextView.setOnClickListener(this);
            cancelTextView.setOnClickListener(this);
        }
        reSendDialog.show();
    }

//    private JSONArray getSendPresonArray(ArrayList<UserInfoBean> to) {
//        JSONArray array = new JSONArray();
//        for (UserInfoBean mBean : to) {
//            JSONObject user = mBean.getUserJson();
//            array.put(user);
//        }
//        return array;
//    }

    public void setTaskTitle(String taskTitle) {
        if (taskBean != null) {
            MessageStores.getInstance().setTaskTitle(taskBean.taskId, taskTitle);
        } else {
            MessageStores.getInstance().updateGroupName(windowInfoBean.groupId, taskTitle);
        }
    }

    public MessageBean getLastMessage() {
        if (messageRecyView.getAdapter() != null) {
            return ((MessageRecyclerAdapter) messageRecyView.getAdapter()).getLastMessage();
        } else {
            return null;
        }
    }

    public boolean isChosePersonShow() {
        return isChosePersonShow;
    }


    private void showPhotoDialog(boolean showReCall, boolean isSmallMail, boolean isShowCopy) {
        if (alertDialog == null) {
            alertDialog = new Dialog(getActivity(), R.style.normal_dialog);
            alertDialog.setContentView(R.layout.chat_menu_dialog);
            alertDialog.show();
            TextView deleteView = (TextView) alertDialog.findViewById(R.id.tv_content1);
            TextView recallView = (TextView) alertDialog.findViewById(R.id.tv_content2);
            TextView replyView = (TextView) alertDialog.findViewById(R.id.tv_content3);
            TextView replyAllView = (TextView) alertDialog.findViewById(R.id.tv_content4);
            TextView copyView = (TextView) alertDialog.findViewById(R.id.tv_content6);
            if (showReCall) {
                recallView.setVisibility(View.VISIBLE);
            } else {
                recallView.setVisibility(View.GONE);
            }

            if (windowInfoBean.groupId != null &&
                    windowInfoBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
                if (windowInfoBean.allMemberList.contains(PreferencesHelper.getInstance().getCurrentUser())) {
                    replyView.setVisibility(View.VISIBLE);
                } else {
                    replyView.setVisibility(View.GONE);
                }
            } else {
                replyView.setVisibility(View.VISIBLE);
            }

            if (isSmallMail) {
                replyAllView.setVisibility(View.VISIBLE);
            } else {
//                replyView.setVisibility(View.GONE);
                replyAllView.setVisibility(View.GONE);
            }

            if (isShowCopy) {
                copyView.setVisibility(View.VISIBLE);
            } else {
                copyView.setVisibility(View.GONE);
            }
            deleteView.setOnClickListener(this);
            recallView.setOnClickListener(this);
            replyView.setOnClickListener(this);
            replyAllView.setOnClickListener(this);
            copyView.setOnClickListener(this);
            return;
        }
        TextView recallView = (TextView) alertDialog.findViewById(R.id.tv_content2);
        if (showReCall) {
            recallView.setVisibility(View.VISIBLE);
        } else {
            recallView.setVisibility(View.GONE);
        }
        TextView replyView = (TextView) alertDialog.findViewById(R.id.tv_content3);
        TextView replyAllView = (TextView) alertDialog.findViewById(R.id.tv_content4);
        TextView copyView = (TextView) alertDialog.findViewById(R.id.tv_content6);

        if (windowInfoBean.groupId != null &&
                windowInfoBean.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
            if (windowInfoBean.allMemberList.contains(PreferencesHelper.getInstance().getCurrentUser())) {
                replyView.setVisibility(View.VISIBLE);
            } else {
                replyView.setVisibility(View.GONE);
            }
        } else {
            replyView.setVisibility(View.VISIBLE);
        }

        if (isSmallMail) {
            replyAllView.setVisibility(View.VISIBLE);
        } else {
//            replyView.setVisibility(View.GONE);
            replyAllView.setVisibility(View.GONE);
        }

        if (isShowCopy) {
            copyView.setVisibility(View.VISIBLE);
        } else {
            copyView.setVisibility(View.GONE);
        }
        alertDialog.show();
    }

    private void initMesaageAdapter() {
        if (messageRecyView.getAdapter() == null) {
            MessageRecyclerAdapter messageRecyclerAdapter = new MessageRecyclerAdapter(messageRecyView, null, loadFileManager);
            messageRecyclerAdapter.setAvatars(((PlayWorkApplication) getActivity().getApplication()).getAvatars());
            messageRecyclerAdapter.setListener(this);
            messageRecyView.setAdapter(messageRecyclerAdapter);
        }
    }

    private void updateWindowInfoMembers(JSONObject info) {
        if (info.has("exitUser")) {
            UserInfoBean exitUser = null;
            String exitId = info.optString("exitUser");
            Iterator<UserInfoBean> allIterator = windowInfoBean.allMemberList.iterator();
            while (allIterator.hasNext()) {
                UserInfoBean user = allIterator.next();
                if (user.id.equals(exitId)) {
                    allIterator.remove();
                    exitUser = user;
                    break;
                }
            }
            Iterator<UserInfoBean> chatIterator = windowInfoBean.chatMemberList.iterator();
            while (chatIterator.hasNext()) {
                UserInfoBean user = chatIterator.next();
                if (user.id.equals(exitId)) {
                    chatIterator.remove();
                    exitUser = user;
                    break;
                }
            }
            Iterator<UserInfoBean> hideIterator = windowInfoBean.hideMemberList.iterator();
            while (hideIterator.hasNext()) {
                UserInfoBean user = hideIterator.next();
                if (user.id.equals(exitId)) {
                    hideIterator.remove();
                    exitUser = user;
                    break;
                }
            }
            windowInfoBean.exitMemberList.add(exitUser);
            if (isChosePersonShow) {
                chosePersonFragment.notifyChosePersonView();
            }
            return;
        }
        JSONArray addUsers = info.optJSONArray("addMembers");
        int count = addUsers.length();
        ArrayList<UserInfoBean> addUserList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UserInfoBean user = new UserInfoBean(addUsers.optJSONObject(i));
            addUserList.add(user);
            windowInfoBean.hideMemberList.add(user);
            windowInfoBean.memberNames += ("," + user.name);
        }

        Iterator<UserInfoBean> addIterator = addUserList.iterator();

        for (UserInfoBean exitUser : windowInfoBean.exitMemberList) {
            while (addIterator.hasNext()) {
                if (addIterator.next().id.equals(exitUser.id)) {
                    addIterator.remove();
                }
            }
        }

        windowInfoBean.allMemberList.addAll(addUserList);

        JSONArray exitUsers = info.optJSONArray("exitedMember");
        windowInfoBean.exitMemberList.clear();
        int exitCount = exitUsers.length();
        for (int i = 0; i < exitCount; i++) {
            UserInfoBean user = new UserInfoBean(addUsers.optJSONObject(i));
            windowInfoBean.exitMemberList.add(user);
        }

        if (isChosePersonShow) {
            chosePersonFragment.notifyChosePersonView();
        }
    }

    /**
     * 发送创建人消息
     */
    private void sendCreateMsg() {
        sendMsg("创建人：" + PreferencesHelper.getInstance().getCurrentUser().name, MessageBean.SYSTEM_TIP_MESSAGE, windowInfoBean.chatMemberList);
    }

    /**
     * 发送增加人员消息
     */
    private void sendAddMsg(ArrayList<UserInfoBean> addMemberList) {
        String addName = "";
        for (UserInfoBean user : addMemberList) {
            addName += user.name + "、";
        }
        addName = addName.substring(0, addName.lastIndexOf("、"));
        sendMsg(PreferencesHelper.getInstance().getCurrentUser().name + "将" + addName + " 添加到该" +
                (taskBean == null ? "聊天" : "任务"), MessageBean.SYSTEM_TIP_MESSAGE, windowInfoBean.chatMemberList);
    }

    private void sendReCreateMsg() {
//        MessageBean addMsg = new MessageBean();
//        addMsg.sendMessageUser = PreferencesHelper.getInstance().getCurrentUser();
//        addMsg.content = " 你恢复了与 " + windowInfoBean.chatMemberList.get(0).name + " 的聊天";
//        addMsg.type = MessageBean.SYSTEM_TIP_MESSAGE;
//        sendMsg(addMsg, true);

        sendMsg(" 你恢复了与 " + windowInfoBean.chatMemberList.get(0).name + " 的聊天", MessageBean.SYSTEM_TIP_MESSAGE, new ArrayList<UserInfoBean>());
    }

    private void sendSmallMail() {
        UserInfoBean from = PreferencesHelper.getInstance().getCurrentUser();
        SmallMailBean smallMail = new SmallMailBean();
        smallMail.sendUser = from;
        smallMail.toUserList = windowInfoBean.chatMemberList;
//        smallMail.initSendToString();
        smallMail.sendTime = System.currentTimeMillis();
        smallMail.subject = windowInfoBean.taskTitle;
        ((ChatActivityNew) getActivity()).showSmallMailFragment(smallMail, SmallMailBean.SEND_MODE);
    }
}
