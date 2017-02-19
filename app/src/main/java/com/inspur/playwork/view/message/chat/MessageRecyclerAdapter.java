package com.inspur.playwork.view.message.chat;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.inspur.playwork.R;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.EmojiHandler;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.loadfile.LoadFileHandlerThread;
import com.inspur.playwork.utils.loadfile.LoadFileManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Fan on 15-9-21.
 */
class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.ViewHolder> implements View.OnLongClickListener {

    private static final String TAG = "messageAdapterFan";

    private static final String DOWNLOAD_AVATAR = "LOAD_AVATAR";
    private static final String DOWNLOAD_IMAGE = "load_image";
    private static final String UPLOAD_IMAGE = "upload_image";


    private static final String RECALL_STRING = "撤回了一条消息";

//    private BitmapCacheManager arrayMap;

    private ArrayList<MessageBean> showMessageList;

//    private ArrayMap<String, Long> sendMessageMap;

    private RecyclerView messageRecyclerView;

    private Handler adapterHander;

    private MessageEventListener listener;

    private MessageBean chatNote;

    private boolean isTaskChat;

    private Context mContext;

    // 公/私设置请求完成的标识 是否是任务创建人标识
    private boolean isPrivateClicked, isTaskCreator;

    private ArrayMap<String, Long> avatars;

    private ArrayMap<String, WeakReference<ViewHolder>> urlRelatedViews;

    private LoadFileManager loadFileManager;

    private boolean isViewShow = true;


    interface MessageEventListener {
        void onImgClick(MessageBean bean);

        void onNoteFocusChanged(String quickNote);

        void onPlaceFocusChanged(String taskPlace);

        void onNoteAndPlaceFocusChanged(boolean hasFocus);

        void onAvatarClick(MessageBean bean, int pos);

        void onPrivateClick(int taskPrivate);

        void onItemLongClick(MessageBean messageBean, int pos);

        void onResendClcik(MessageBean messageBean, int pos);

        void onSmallMailClick(MessageBean messageBean, int pos);

        void onFileMsgClick(MessageBean messageBean, int pos);
    }

    MessageRecyclerAdapter(RecyclerView messageRecyclerView, LoadFileManager loadFileManager) {
        this.messageRecyclerView = messageRecyclerView;
        showMessageList = new ArrayList<>();
        NumberFormat nt = NumberFormat.getPercentInstance();
        nt.setMaximumFractionDigits(0);
        nt.setMaximumIntegerDigits(3);
        adapterHander = new AdapterHandler(this);
        this.loadFileManager = loadFileManager;
        setHasStableIds(true);
        chatNote = new MessageBean();
        chatNote.type = MessageBean.CHAT_NOTE;
        mContext = messageRecyclerView.getContext();
        isPrivateClicked = true;
        urlRelatedViews = new ArrayMap<>();
    }

    void setAllMessageList(ArrayList<MessageBean> allMessageList, boolean isTaskChat) {
        this.showMessageList = allMessageList;
        this.isTaskChat = isTaskChat;
        if (this.isTaskChat) {
            this.showMessageList.add(0, chatNote);
        }
    }

    MessageBean getLastMessage() {
        if (showMessageList.size() > 0) {
            return this.showMessageList.get(showMessageList.size() - 1);
        } else {
            return null;
        }
    }

    void notifyCustomProperty(String taskPlace, String taskNotes, int taskPrivate) {
        if (taskPlace != null) {
            chatNote.uuid = taskPlace; // 借用fileName存放任务地点
        }
        if (taskNotes != null) {
            chatNote.content = taskNotes; // 借用content存放随手记
        }
        if (taskPrivate > -1) {
            chatNote.setPrivate = taskPrivate; // 借用sendTime存放公/私标识
        }
        notifyItemChanged(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case MessageBean.EMOJI_MESSAGE_SEND:
            case MessageBean.TEXT_MESSAGE_SEND:
                layout = R.layout.layout_text_message_send;
                break;
            case MessageBean.EMOJI_MESSAGE_RECIVE:
            case MessageBean.TEXT_MESSAGE_RECIVE:
                layout = R.layout.layout_text_message_recive;
                break;
            case MessageBean.EMOJI_TEXT_SEND:
                break;
            case MessageBean.EMOJI_TEXT_RECIVE:
                break;
            case MessageBean.VOICE_MESSAGE_SEND:
                break;
            case MessageBean.VOICE_MESSAGE_RECIVE:
                break;
            case MessageBean.IMAGE_MESSAGE_SEND:
                layout = R.layout.layout_image_message_send;
                break;
            case MessageBean.IMAGE_MESSAGE_RECIVE:
                layout = R.layout.layout_image_message_recive;
                break;
            case MessageBean.ATTACHMENT_MESSAGE_SEND:
                layout = R.layout.layout_file_message_send;
                break;
            case MessageBean.ATTACHMENT_MESSAGE_RECEIVE:
                layout = R.layout.layout_file_message_recive;
                break;
            // 任务地点 和 随手记
            case MessageBean.CHAT_NOTE:
                layout = R.layout.layout_chat_note;
                break;
            case MessageBean.SYSTEM_TIP_MESSAGE:
            case MessageBean.RECALL_MESSAGE:
                layout = R.layout.layout_message_recall;
                break;
            case MessageBean.SMALL_MAIL_RECIVE:
                layout = R.layout.layout_meessage_small_mail_recive;
                break;
            case MessageBean.SMALL_MAIL_SEND:
                layout = R.layout.layout_message_small_mail_send;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), viewType);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MessageBean mBean = getItem(position);

        if (getItemViewType(position) == MessageBean.CHAT_NOTE) {
            /* 任务地点 */
            bindTaskNoteAndPlace(holder, mBean);
            return;
        }

        if (getItemViewType(position) == MessageBean.RECALL_MESSAGE) {//撤回消息
            bindRecallMsg(holder, mBean);
            return;
        }

        if (getItemViewType(position) == MessageBean.SYSTEM_TIP_MESSAGE) {//系统提示消息
            holder.tipText.setText(mBean.content);
            return;
        }

        setUpPublicPart(holder, mBean);

        if (mBean.type == MessageBean.TEXT_MESSAGE_RECIVE || mBean.type == MessageBean.TEXT_MESSAGE_SEND) {
            holder.messageContent.setText(mBean.content);
        } else if (mBean.type == MessageBean.IMAGE_MESSAGE_SEND || mBean.type == MessageBean.IMAGE_MESSAGE_RECIVE) {
            bindImageMsg(holder, position, mBean);
        } else if (mBean.type == MessageBean.ATTACHMENT_MESSAGE_SEND || mBean.type == MessageBean.ATTACHMENT_MESSAGE_RECEIVE) {
//            holder.messageContent.setText(CommonUtils.delHTMLTag(mBean.content));
            bindFileMsg(holder, position, mBean);
        } else if (mBean.type == MessageBean.SMALL_MAIL_RECIVE || mBean.type == MessageBean.SMALL_MAIL_SEND) {
            holder.sendToNames.setText(mBean.getSmallMailSendToName());
            holder.messageContent.setText(mBean.content);
            holder.messageRoot.setOnClickListener(smallMailClickListener);
        } else if (mBean.isEmojiMessage()) {
            holder.messageContent.setText(EmojiHandler.getInstance().getEmojiSpannableString(messageRecyclerView.getContext(),
                    mBean.content, (int) (holder.messageContent.getTextSize() * 15 / 10)));
        } else {
            holder.messageContent.setText(mBean.content);
        }
        setUpUserAvatars(holder, mBean);
    }

    private void bindFileMsg(ViewHolder holder, int position, MessageBean mBean) {
        holder.fileName.setText(mBean.attachmentBean.attachmentName);
        if (mBean.attachmentBean.isAttachmentDownloaded())
            holder.fileState.setText("已下载");
        else
            holder.fileState.setText("未下载");
        if (mBean.isCurrentUserMsg())
            holder.fileState.setText("已发送");
        holder.fileSize.setText(mBean.attachmentBean.getSize());
        holder.messageRoot.setOnClickListener(fileMsgClickListener);
    }

    private View.OnClickListener fileMsgClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            ViewHolder viewHolder =
//            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.getChildViewHolder((View) v.getParent());
            int pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent());
            MessageBean messageBean = getItem(pos);
            listener.onFileMsgClick(messageBean, pos);
        }
    };

    private void setUpPublicPart(ViewHolder holder, MessageBean mBean) {
        holder.messageRoot.setOnLongClickListener(this);

        holder.userAvatar.setOnClickListener(imgClickListener);

//        holder.userAvatar.setTag(holder.itemView);

        holder.sendName.setText(mBean.sendMessageUser.name);
        if (!mBean.isSendSuccess) {
            if (!mBean.isMessageNew) {
                holder.sendErrorView.setVisibility(View.VISIBLE);
                holder.sendErrorView.setOnClickListener(reSendClickListener);
            } else {
                holder.sendErrorView.setVisibility(View.GONE);
                holder.sendProgress.setVisibility(View.VISIBLE);
            }
        } else {
            holder.sendErrorView.setVisibility(View.GONE);
            holder.sendProgress.setVisibility(View.GONE);
        }
    }

    private void bindRecallMsg(ViewHolder holder, MessageBean mBean) {
        if (!mBean.isCurrentUserMsg())
            holder.tipText.setText("\"" + mBean.sendMessageUser.name + "\"" + RECALL_STRING);
        else
            holder.tipText.setText("你" + RECALL_STRING);
    }

    private void setUpUserAvatars(ViewHolder holder, MessageBean mBean) {
        if (avatars.containsKey(mBean.sendMessageUser.id)) {
            long avatarId = avatars.get(mBean.sendMessageUser.id);
            if (avatarId >= mBean.sendMessageUser.avatar) {
                mBean.sendMessageUser.avatar = avatarId;
            } else {
                avatars.put(mBean.sendMessageUser.id, mBean.sendMessageUser.avatar);
            }
        } else {
            avatars.put(mBean.sendMessageUser.id, mBean.sendMessageUser.avatar);
        }

        holder.sendReciveTime.setText(DateUtils.getSendReciveTimeDateText(mBean.sendTime));

        if (mBean.sendMessageUser.isAvatarFileExit()) {
            loadAvatarBitmap(mBean.sendMessageUser.getAvatarPath(), holder.userAvatar);
        } else {
            downLoadAvatar(mBean.sendMessageUser.getAvatarPath(), AppConfig.AVATAR_ROOT_PATH + mBean.sendMessageUser.avatar, holder);
        }
    }

    private void bindTaskNoteAndPlace(final ViewHolder holder, final MessageBean mBean) {
        holder.taskPlaceEditText.setText(chatNote.uuid);
        if (isTaskCreator) {
            holder.taskPlaceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    listener.onNoteAndPlaceFocusChanged(hasFocus);
                    if (!hasFocus && !holder.taskPlaceEditText.getText().toString().equals(mBean.uuid)) {
                        listener.onPlaceFocusChanged(holder.taskPlaceEditText.getText().toString());
                    }
                }
            });
        } else {
            holder.taskPlaceEditText.setFocusable(false); // 非任务创建人不能修改任务地点
        }
            /* 任务公/私 */
        setTaskPrivateText(chatNote.setPrivate, holder.taskPrivateTextView);
        if (isTaskCreator) {
            holder.taskPrivateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPrivateClicked) {
                        if (chatNote.setPrivate == 1) {
                            listener.onPrivateClick(0);
                        } else {
                            listener.onPrivateClick(1);
                        }
                        isPrivateClicked = false;
                    }
                }
            });
        }
            /* 任务随手记 */
        holder.quickNoteEditText.setText(chatNote.content);
        if (!holder.quickNoteEditText.getText().toString().equals("")) {
            if (Build.VERSION.SDK_INT >= 16)
                holder.quickNoteEditText.setBackground(null);
            else
                //noinspection deprecation
                holder.quickNoteEditText.setBackgroundDrawable(null);
        }
        holder.quickNoteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                listener.onNoteAndPlaceFocusChanged(hasFocus);
                if (!hasFocus && holder.quickNoteEditText.getText().toString().equals("")) {
                    //noinspection deprecation
                    holder.quickNoteEditText.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_chat_notes));
                } else {
                    //noinspection deprecation
                    holder.quickNoteEditText.setBackgroundDrawable(null);
                }
                if (!hasFocus && !holder.quickNoteEditText.getText().toString().equals(mBean.content)) {
                    listener.onNoteFocusChanged(holder.quickNoteEditText.getText().toString());
                }
            }
        });
    }

    private void bindImageMsg(final ViewHolder holder, final int position, final MessageBean mBean) {
        holder.msgImg.setOnClickListener(imgClickListener);
//        Bitmap imageBitmap = arrayMap.getBitmapFromMemoryCache(mBean.imagePath);
//        if (imageBitmap != null) {
//            holder.msgImg.setImageBitmap(imageBitmap);
//        } else {
        if (mBean.isImageFileExits()) {
            if (mBean.imgBitMap != null) {
                Log.i(TAG, "bindImageMsg: bindImageMsg");
                holder.msgImg.setImageBitmap(mBean.imgBitMap);
//                arrayMap.putBitmapIntoMemoryCache(mBean.imagePath, mBean.imgBitMap);
                mBean.imgBitMap = null;
            } else {
                loadBitmap(mBean, holder.msgImg);
            }
        } else {
            if (mBean.imgSrc != null)
                downLoadImageMsg(mBean.imgSrc.src, mBean.imagePath, holder);
        }
//        }
    }

    private void downLoadImageMsg(String src, String imagePath, ViewHolder viewHolder) {
        if (urlRelatedViews.containsKey(DOWNLOAD_IMAGE + src))
            return;
        urlRelatedViews.put(DOWNLOAD_IMAGE + src, new WeakReference<>(viewHolder));
        loadFileManager.downLoadFile(src, imagePath, DOWNLOAD_IMAGE + src, adapterHander, true, LoadFileManager.DOWNLOAD_MSG_PICTURE);
    }

    private void setTaskPrivateText(int taskPrivate, TextView textView) {
        if (taskPrivate == 1) {
            textView.setBackgroundColor(mContext.getResources().getColor(R.color.task_private_bg));
            textView.setTextColor(mContext.getResources().getColor(R.color.task_private_font_color));
            textView.setText("私");
        } else {
            textView.setBackgroundColor(mContext.getResources().getColor(R.color.task_public_bg));
            textView.setTextColor(mContext.getResources().getColor(R.color.task_public_font_color));
            textView.setText("公");
        }
    }

    void setTaskCreator(boolean isTaskCreator) {
        this.isTaskCreator = isTaskCreator;
    }

    @Override
    public long getItemId(int position) {
        MessageBean messageBean = getItem(position);
        if (!TextUtils.isEmpty(messageBean.uuid)) {
            return messageBean.uuid.hashCode();
        } else if (!TextUtils.isEmpty(messageBean.id)) {
            return messageBean.id.hashCode();
        }
        return -1;
    }

    private void downLoadAvatar(String filePath, String url, ViewHolder userAvatar) {
        if (urlRelatedViews.containsKey(DOWNLOAD_AVATAR + url)) {
            return;
        }
        urlRelatedViews.put(DOWNLOAD_AVATAR + url, new WeakReference<>(userAvatar));

        loadFileManager.downLoadFile(url, filePath, DOWNLOAD_AVATAR + url, adapterHander, false, LoadFileManager.DOWNLOAD_CHAT_AVATAR);
    }


    public void setListener(MessageEventListener listener) {
        this.listener = listener;
    }

    public void setAvatars(ArrayMap<String, Long> avatars) {
        this.avatars = avatars;
    }

    @Override
    public int getItemViewType(int position) {
        MessageBean mBean = getItem(position);
        return mBean.type;
    }

    private MessageBean getItem(int position) {
        return showMessageList.get(position);
    }

    @Override
    public int getItemCount() {
        return showMessageList.size();
    }

    void addMessage(MessageBean mBean) {
        showMessageList.add(mBean);
        notifyItemInserted(showMessageList.size() - 1);
        Log.i(TAG, "addMessage: scroll to bottom");
        messageRecyclerView.scrollToPosition(getItemCount() - 1);
    }

    void setPrivateClicked(boolean isPrivateClicked) {
        this.isPrivateClicked = isPrivateClicked;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;

        TextView sendReciveTime;

        ImageView userAvatar;

        View sendProgress;

        ImageView sendErrorView;

        TextView messageContent;

        TextView sendName;

        ImageView msgImg;

        public TextView progress;

        View messageRoot;

        EditText taskPlaceEditText; // 任务地点

        TextView taskPrivateTextView; // 公/私

        EditText quickNoteEditText; // 随手记

        TextView tipText;

        TextView sendToNames;

        public TextView fileName;
        TextView fileSize;
        TextView fileState;


        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            rootView = itemView;
            if (viewType == MessageBean.CHAT_NOTE) {
                taskPlaceEditText = (EditText) itemView.findViewById(R.id.et_task_address);
                taskPrivateTextView = (TextView) itemView.findViewById(R.id.tv_task_private);
                quickNoteEditText = (EditText) itemView.findViewById(R.id.et_quick_note);
            } else if (viewType == MessageBean.RECALL_MESSAGE || viewType == MessageBean.SYSTEM_TIP_MESSAGE) {
                tipText = (TextView) rootView.findViewById(R.id.tv_tip_text);
            } else if (viewType == MessageBean.ATTACHMENT_MESSAGE_RECEIVE || viewType == MessageBean.ATTACHMENT_MESSAGE_SEND) {
                sendReciveTime = (TextView) itemView.findViewById(R.id.tv_send_time);
                userAvatar = (ImageView) itemView.findViewById(R.id.iv_chat_avatar);
                sendProgress = itemView.findViewById(R.id.send_progress_bar);
                sendErrorView = (ImageView) itemView.findViewById(R.id.msg_status);
                sendName = (TextView) itemView.findViewById(R.id.tv_send_name);
                messageRoot = itemView.findViewById(R.id.fram_msg_container);

                fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
                fileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
                fileState = (TextView) itemView.findViewById(R.id.tv_file_state);
            } else {
                sendReciveTime = (TextView) itemView.findViewById(R.id.tv_send_time);
                userAvatar = (ImageView) itemView.findViewById(R.id.iv_chat_avatar);
                sendProgress = itemView.findViewById(R.id.send_progress_bar);
                sendErrorView = (ImageView) itemView.findViewById(R.id.msg_status);
                sendName = (TextView) itemView.findViewById(R.id.tv_send_name);
                messageRoot = itemView.findViewById(R.id.fram_msg_container);

                messageContent = (TextView) itemView.findViewById(R.id.tv_message_content);
                msgImg = (ImageView) itemView.findViewById(R.id.image_msg);
                progress = (TextView) itemView.findViewById(R.id.tv_upload_progress);
                sendToNames = (TextView) itemView.findViewById(R.id.tv_mail_to);
            }

        }
    }

    /**
     * 每次下拉刷新消息时都调用这个方法
     */
    void addMessageToShow(ArrayList<MessageBean> messageBeans) {
        for (int i = messageBeans.size() - 1; i >= 0; i--) {
            if (isTaskChat)
                showMessageList.add(1, messageBeans.get(i));
            else {
                showMessageList.add(0, messageBeans.get(i));
                notifyItemInserted(0);
            }
//            notifyItemInserted(1);
        }
        if (isTaskChat) {
            notifyItemRangeInserted(1, messageBeans.size());
            messageRecyclerView.scrollToPosition(1);
        } else {
            messageRecyclerView.scrollToPosition(messageBeans.size() - 1);
        }
    }

    MessageBean updateView(String uuid) {
        ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(uuid.hashCode());
        int pos;
        if (viewHolder == null) {
            pos = findItemByUUId(uuid);
            if (pos >= 0)
                return getItem(pos);
            else
                return null;
        } else {
            pos = messageRecyclerView.getChildAdapterPosition(viewHolder.itemView);
        }
        if (viewHolder.sendProgress != null) {
            viewHolder.sendProgress.setVisibility(View.GONE);
        }

        if (viewHolder.sendErrorView != null) {
            viewHolder.sendErrorView.setVisibility(View.GONE);
        }
        return getItem(pos);
    }

    private int findItemByUUId(String uuid) {
        for (int i = showMessageList.size() - 1; i > 0; i--) {
            MessageBean messageBean = showMessageList.get(i);
            if (!TextUtils.isEmpty(messageBean.uuid) && messageBean.uuid.equals(uuid))
                return i;
        }
        return -1;
    }


    private void loadBitmap(MessageBean messageBean, ImageView imageView) {
        String path = messageBean.imagePath;
        loadLocalImage(imageView, path);
    }

    private void loadLocalImage(ImageView imageView, final String path) {
//        if (PictureUtils.cancelPotentialWork(path, imageView)) {
//            LoadBitmapWorkerTask task = new LoadBitmapWorkerTask(imageView, arrayMap);
//            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//            imageView.setImageDrawable(drawable);
//            task.execute(path);
//        }

        //TODO:可以加上缓存机制
        Point point = PictureUtils.getImageWidthHeight(imageView.getContext(), path);

        Glide.with(messageRecyclerView.getContext()).
                load(new File(path)).
                placeholder(R.drawable.pictures_no).
                override(point.x, point.y).
                signature(new StringSignature("chat" + path)).
                dontAnimate().
                into(imageView);
    }

    private void loadAvatarBitmap(String path, ImageView imageView) {
//        if (PictureUtils.cancelPotentialWork(path, imageView)) {
//            NormalLoadBitmapTask task = new NormalLoadBitmapTask(imageView, arrayMap);
//            AsyncDrawable drawable = new AsyncDrawable(imageView.getContext().getResources(), null, task);
//            imageView.setImageDrawable(drawable);
//            task.execute(path);
//        }
        Glide.with(messageRecyclerView.getContext()).
                load(new File(path)).
                placeholder(R.drawable.icon_chat_default_avatar).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                into(imageView);
    }


    //   private int lastShowPos;
/*
    private void addLastCountMsgToShow(int startIndex, int endIndex) {
        lastShowPos = endIndex - startIndex;
        if (showMessageList.size() == 0 && allMessageList.size() > 0) {
            showMessageList.add(allMessageList.get(endIndex - 1));
            for (int i = (endIndex - 2); i >= startIndex; i--) {
                showMessageList.add(0, allMessageList.get(i));
            }
        } else {
            for (int i = endIndex - 1; i >= startIndex; i--) {
                showMessageList.add(0, allMessageList.get(i));
                notifyItemInserted(0);
            }
        }
    }

    public boolean isHasMoreMsg() {
        return currentShowFirstMessageIndex != 0;
    }

    public int getLastShowPos() {
        return lastShowPos;
    }*/


    private static class AdapterHandler extends Handler {
        private WeakReference<MessageRecyclerAdapter> adapterReference;

        AdapterHandler(MessageRecyclerAdapter adapter) {
            adapterReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LoadFileHandlerThread.UPDATE_DOWNLOAD_PROGRESS: {
                    String clientId = (String) msg.obj;
                    if (clientId.startsWith(DOWNLOAD_IMAGE)) {
                        if (msg.arg1 > 0) {
                            if (adapterReference.get() != null) {
                                adapterReference.get().updateDownLoadImgProgress(clientId, msg.arg1);
                            }
                        }
                    }

                    break;
                }
                case LoadFileHandlerThread.DOWNLOAD_SUCCESS: {
                    String clientId = (String) msg.obj;
                    Bundle data = msg.peekData();
                    if (clientId.startsWith(DOWNLOAD_AVATAR)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().downLoadAvatarSuccess(clientId, data.getString(LoadFileHandlerThread.DOWNLOAD_FILE_SAVE_PATH));
                    } else if (clientId.startsWith(DOWNLOAD_IMAGE)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().downLoadImageSuccess(clientId, data.getString(LoadFileHandlerThread.DOWNLOAD_FILE_SAVE_PATH));
                    }
                    break;
                }
                case LoadFileHandlerThread.DOWNLOAD_FAILURE: {
                    String clientId = (String) msg.obj;
                    if (clientId.startsWith(DOWNLOAD_AVATAR)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().downLoadAvatarFailure(clientId);
                    } else if (clientId.startsWith(DOWNLOAD_IMAGE)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().downLoadImageFailue(clientId);
                    }
                    break;
                }
                case LoadFileHandlerThread.UPDATE_UPLOAD_PROGRESS: {
                    String clientId = (String) msg.obj;
                    if (clientId.startsWith(UPLOAD_IMAGE)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().updateUpLoadImgProgress(clientId, msg.arg1);
                    }
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_SUCCESS: {
                    break;
                }
                case LoadFileHandlerThread.UPLOAD_FAILURE: {
                    String clientId = (String) msg.obj;
                    if (clientId.startsWith(UPLOAD_IMAGE)) {
                        if (adapterReference.get() != null)
                            adapterReference.get().uploadImgFailure(clientId);
                    }
                    break;
                }
            }
        }
    }

    void uploadImgFailure(String clientId) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(clientId.hashCode());
            if (viewHolder != null) {
                if (viewHolder.progress.getVisibility() == View.VISIBLE)
                    viewHolder.progress.setVisibility(View.GONE);
                viewHolder.sendProgress.setVisibility(View.GONE);
                viewHolder.sendErrorView.setVisibility(View.VISIBLE);
                viewHolder.sendErrorView.setOnClickListener(reSendClickListener);
            }
        }
    }

    void uploadFileFailure(String clientId) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(clientId.hashCode());
            if (viewHolder != null) {
                viewHolder.fileState.setText("上传失败");
                viewHolder.sendErrorView.setVisibility(View.VISIBLE);
                viewHolder.sendErrorView.setOnClickListener(reSendClickListener);
            }
        }
    }

    void downloadFileFailure(String id) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(id.hashCode());
            if (viewHolder == null)
                return;
            viewHolder.fileState.setText("下载失败");
        }
    }

    void updateDownLoadFileProgress(String id, int precent) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(id.hashCode());
            if (viewHolder == null)
                return;
            viewHolder.fileState.setText(precent + ".0%");
        }
    }

    void downloadFileSucess(String id) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(id.hashCode());
            if (viewHolder == null)
                return;
            viewHolder.fileState.setText("已下载");
        }
    }

    void sendMessageFailed(String failedUDid) {
        ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(failedUDid.hashCode());
        if (viewHolder != null) {
            viewHolder.sendProgress.setVisibility(View.GONE);
            viewHolder.sendErrorView.setVisibility(View.VISIBLE);
            viewHolder.sendErrorView.setOnClickListener(reSendClickListener);
        }
    }

    void upLoadImgSuccess(String uuid) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(uuid.hashCode());
            if (viewHolder != null) {
                if (viewHolder.progress.getVisibility() == View.VISIBLE)
                    viewHolder.progress.setVisibility(View.GONE);
            }
        }
    }

    void uploadFileSuccess(String uuid) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(uuid.hashCode());
            if (viewHolder != null) {
                viewHolder.fileState.setText("已发送");
            }
        }
    }

    void updateUpLoadImgProgress(String clientId, int arg1) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(clientId.hashCode());
            if (viewHolder == null)
                return;
            if (viewHolder.progress.getVisibility() == View.GONE)
                viewHolder.progress.setVisibility(View.VISIBLE);
            Log.d(TAG, "updateUpLoadImgProgress() called with: " + "clientId = [" + clientId + "], arg1 = [" + arg1 + "]");
            viewHolder.progress.setText(arg1 + ".0%");
        }
    }

    void updateUpLoadFileProgress(String uuid, int precent) {
        if (isViewShow) {
            ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForItemId(uuid.hashCode());
            if (viewHolder == null)
                return;
            viewHolder.fileState.setText(precent + ".0%");
        }
    }

    private void updateDownLoadImgProgress(String clientId, int arg1) {
        if (isViewShow) {
            ViewHolder viewHolder = urlRelatedViews.get(clientId).get();
            if (viewHolder == null)
                return;
            if (viewHolder.progress.getVisibility() == View.GONE)
                viewHolder.progress.setVisibility(View.VISIBLE);
            viewHolder.progress.setText(arg1 + ".0%");
        }
    }

    private void downLoadImageFailue(String clientId) {
        if (isViewShow) {
            ViewHolder viewHolder = urlRelatedViews.get(clientId).get();
            if (viewHolder == null)
                return;
            if (viewHolder.progress.getVisibility() == View.VISIBLE)
                viewHolder.progress.setVisibility(View.GONE);
        }
        urlRelatedViews.remove(clientId);
    }

    private void downLoadImageSuccess(String clientId, String filePath) {
        if (isViewShow) {
            ViewHolder relatedView = urlRelatedViews.get(clientId).get();
            if (relatedView == null)
                return;
            if (relatedView.progress.getVisibility() == View.VISIBLE)
                relatedView.progress.setVisibility(View.GONE);
            loadLocalImage(relatedView.msgImg, filePath);
        }
        urlRelatedViews.remove(clientId);
    }

    private void downLoadAvatarFailure(String clientId) {
        if (isViewShow) {
            ViewHolder relatedView = urlRelatedViews.get(clientId).get();
            if (relatedView == null)
                return;
            relatedView.userAvatar.setImageBitmap(PictureUtils.getDefaultAvatar(relatedView.itemView.getContext()));
        }
        urlRelatedViews.remove(clientId);
    }

    private void downLoadAvatarSuccess(String downLoadUrl, String filePath) {
        if (isViewShow) {
            ViewHolder relatedView = urlRelatedViews.get(downLoadUrl).get();
            if (relatedView == null)
                return;
            loadAvatarBitmap(filePath, relatedView.userAvatar);
        }
        urlRelatedViews.remove(downLoadUrl);
    }

/*    @SuppressLint("SetTextI18n")
    private void updateProgress(int pos, double precent) {
        ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForAdapterPosition(pos);
        if (viewHolder == null)
            return;
        if (viewHolder.progress.getVisibility() == View.GONE)
            viewHolder.progress.setVisibility(View.VISIBLE);
        viewHolder.progress.setText(nt.format(precent) + "");
        if (precent == 1) {
            viewHolder.progress.setVisibility(View.GONE);
        }
    }*/

/*    private void upLoadDone(int pos) {
        ViewHolder viewHolder = (ViewHolder) messageRecyclerView.findViewHolderForAdapterPosition(pos);
        if (viewHolder != null) {
            viewHolder.progress.setVisibility(View.GONE);
        }
        //notifyItemChanged(pos);
    }*/

    private View.OnClickListener imgClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_msg:
                    View rootView;
                    int pos;
                    try {
                        rootView = (View) v.getParent().getParent().getParent();
                        pos = messageRecyclerView.getChildAdapterPosition(rootView);
                    } catch (Exception e) {
                        rootView = (View) v.getParent().getParent();
                        pos = messageRecyclerView.getChildAdapterPosition(rootView);
                    }
                    if (listener != null) {
                        listener.onImgClick(getItem(pos));
                    }
                    break;
                case R.id.iv_chat_avatar:
                    View itemView = (View) v.getParent();
                    int itemPos = messageRecyclerView.getChildAdapterPosition(itemView);
                    listener.onAvatarClick(getItem(itemPos), itemPos);
                    break;
            }
        }
    };

    private View.OnClickListener reSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            listener.onResendClcik(getItem(pos), pos);
        }
    };

    private View.OnClickListener smallMailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos;
            try {
                pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent().getParent());
            } catch (Exception e) {
                pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent());
            }
            listener.onSmallMailClick(getItem(pos), pos);
        }
    };

    long getFirstShowMessageSendTime() {
        if (showMessageList != null && showMessageList.size() > 1) {
            if (isTaskChat)
                return showMessageList.get(1).sendTime;
            else
                return showMessageList.get(0).sendTime;
        }
        return -1;
    }

    @Override
    public boolean onLongClick(View v) {
        int pos;
        try {
            pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent().getParent());
        } catch (Exception e) {
            pos = messageRecyclerView.getChildAdapterPosition((View) v.getParent());
        }
        listener.onItemLongClick(getItem(pos), pos);
        return true;
    }

    private int getPositionByMsgId(String msgId) {
        for (MessageBean messageBean : showMessageList) {
            if (!TextUtils.isEmpty(messageBean.id) && messageBean.id.equals(msgId))
                return showMessageList.indexOf(messageBean);
        }
        return -1;
    }

    void deletMsgByMsgId(String msgId) {
        int pos = getPositionByMsgId(msgId);
        if (pos == -1) {
            pos = findItemByUUId(msgId);
        }
        showMessageList.remove(pos);
        notifyItemRemoved(pos);
    }

    void recallMsgByMsgId(String msgId) {
        int pos = getPositionByMsgId(msgId);
        if (pos == -1)
            return;
        MessageBean messageBean = showMessageList.get(pos);
        messageBean.type = MessageBean.RECALL_MESSAGE;
        notifyItemChanged(pos);
    }

}
