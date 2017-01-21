package com.inspur.playwork.model.message;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.model.common.SimpleUserInfoBean;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.XmlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bugcode on 15-8-18.
 */
public class MessageBean implements Parcelable {


    private static final String TAG = "MessageBeanFan";

    public static final int MESSAGE_MAIL = 0; //邮件消息

    public static final int MESSAGE_TASK_CHAT = 1; //时间轴任务消息

    public static final int MESSAGE_CHAT = 2; //微聊聊天消息

    /**
     * 文本消息
     */
    public static final int TEXT_MESSAGE_SEND = 0x01;

    public static final int TEXT_MESSAGE_RECIVE = 0x02;

    /**
     * 图片消息
     */
    public static final int IMAGE_MESSAGE_SEND = 0x03;

    public static final int IMAGE_MESSAGE_RECIVE = 0x04;

    /**
     * 带表情的文字消息
     */
    public static final int EMOJI_TEXT_SEND = 0x05;

    public static final int EMOJI_TEXT_RECIVE = 0x06;

    /**
     * 语音消息
     */
    public static final int VOICE_MESSAGE_SEND = 0x07;

    public static final int VOICE_MESSAGE_RECIVE = 0x08;

    /**
     * 附件消息
     */
    public static final int ATTACHMENT_MESSAGE_SEND = 0x09;

    public static final int ATTACHMENT_MESSAGE_RECEIVE = 0x10;

    /**
     * 表情消息
     */
    public static final int EMOJI_MESSAGE_SEND = 0x11;
    public static final int EMOJI_MESSAGE_RECIVE = 0x12;

    /**
     * 任务地点 和 随手记
     */
    public static final int CHAT_NOTE = 0x0a;

    /**
     * 撤回消息
     */
    public static final int RECALL_MESSAGE = 0x0b;

    /**
     * 系统提示消息
     */
    public static final int SYSTEM_TIP_MESSAGE = 0x0c;

    /**
     * 小邮消息
     */

    public static final int SMALL_MAIL_SEND = 0x0d;
    public static final int SMALL_MAIL_RECIVE = 0x0e;


//    private static String userId = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);


    /**
     * 消息内容
     */
    public String content;

    /**
     * 消息接受发送的时间
     */
    public long sendTime;

    /**
     * 消息的类型
     * 此类型是指此消息是文本 还是文件 还是其他的消息
     */
    public int type;

    /**
     * 消息的发送类型，只有两种，微聊消息或者任务消息
     * 之后可能会有微盘消息之类的
     */
    public int sendType;

    /**
     * 消息服务端id
     */
    public String id;

    /**
     * 消息本地UUID
     */
    public String uuid;

    /**
     * 消息的所属聊天的groupId
     */
    public String groupId;

    /**
     * 当为任务聊天时，任务的创建时间
     */
    public long createTime;

    /**
     * 消息接收人，是一个json格式的字符串
     */
    public String to;

    /**
     * 是否已发送 发送成功/失败
     */
    public boolean isSendSuccess = true;

    public ArrayList<SimpleUserInfoBean> toUserBean;

    public boolean isMessageNew;

    public Bitmap imgBitMap;

    public UserInfoBean sendMessageUser;

    public String imagePath;

    public ImgMsgXmlBean imgSrc;

//    public String fileName;

//    public long fileSize;

//    public String fileUrl;

//    public String docId;

    public JSONArray toArray;

    public int setPrivate;

    private boolean isNeedNotify = true;

    public String smallMailId;

    private String toUserNames;

    public TaskAttachmentBean attachmentBean;

    //==================================这 start 四个字段是发消息时候要用到的字段
    public String mailId;

    public String taskTitle;

    public long taskCreateTime;

    public String taskId;

    //============================================end

    public MessageBean() {
    }

    // {"title":"玩工作sdk","createTime":1446843854634,"from":{"id":"lizhaoch","uid":"P00000741","avatar":1000408422,"name":"李照川","email":"lizhaoch@inspur.com"},"to":[{"avatar":1000408322,"id":"sunchj","uid":"P00014090","email":"sunchj@inspur.com","name":"孙长杰"},{"avatar":2482,"id":"zhaohaixing","uid":"P00016330","email":"zhaohaixing@inspur.com","name":"赵海兴"},{"avatar":1000408332,"id":"dongchch","uid":"P00018881","email":"dongchch@inspur.com","name":"董晨晨"},{"avatar":1000407792,"id":"jiangguofu","uid":"P00026390","email":"jiangguofu@inspur.com","name":"江国福"},{"avatar":2486,"id":"sunyuan","uid":"P00026391","email":"sunyuan@inspur.com","name":"孙源"},{"avatar":1000408401,"id":"fanjsh","uid":"P00030883","email":"fanjsh@inspur.com","name":"樊继硕"},{"avatar":2951,"id":"shaoshuai","uid":"P00031552","email":"shaoshuai@inspur.com","name":"邵帅"},{"avatar":2492,"id":"wangbh","uid":"P00000007","email":"wangbh@inspur.com","name":"王柏华"}],"content":"又错了","isEncrypt":false,"type":1,"notShowMe":false,"sendTime":1446843806199,"_mailId":"563042beb191839412e1da97","taskId":"563042bbb191839412e1da95","groupId":"563042beb191839412e1da97","userId":"fanjsh","ids":{"taskId":"563042bbb191839412e1da95","groupId":"563042beb191839412e1da97"},"isRead":0}

    public MessageBean(JSONObject message) {

        String userId = PreferencesHelper.getInstance().getCurrentUser().id;

        content = message.optString("content");
        if (content.lastIndexOf("\n") != -1) {
            content = content.substring(0, content.length() - 1);
        }
        content = content.replace("</br>", "\n");

        this.groupId = message.optString("_mailId");
        if (message.has("createTime"))
            createTime = message.optLong("createTime");
        sendTime = message.optLong("sendTime");
        id = message.optString("_id");
        sendMessageUser = new UserInfoBean(message.optJSONObject("from"));
        JSONArray toArray = message.optJSONArray("to");
        to = toArray.toString();

        initToList(toArray);

        sendType = message.optInt("type");

        Log.i(TAG, "MessageBean: " + sendMessageUser.id + "----------" + userId);
        if (content.contains("weiliao_images") && content.contains("<img")) {
            imgSrc = XmlHelper.praseChatImageMsg(content);
            if (imgSrc != null) {
                imagePath = FileUtil.getImageFilePath() + groupId + File.separator + imgSrc.id + ".png";
                if (sendMessageUser.id.equals(userId)) {
                    type = IMAGE_MESSAGE_SEND;
                } else {
                    type = IMAGE_MESSAGE_RECIVE;
                }
            } else {
                if (sendMessageUser.id.equals(userId)) {
                    type = TEXT_MESSAGE_SEND;
                } else {
                    type = TEXT_MESSAGE_RECIVE;
                }
            }
        } else if (content.contains("attachmentDownload")) {// 接收附件消息
            Log.i(TAG, "MessageBean: " + content);
            attachmentBean = XmlHelper.getFileMsgXmlBean(content);
            if (message.has("taskId"))
                attachmentBean.taskId = message.optString("taskId");
            else
                attachmentBean.taskId = groupId;
            // 去掉html标签
            if (sendMessageUser.id.equals(userId)) {
                type = ATTACHMENT_MESSAGE_SEND;
            } else {
                type = ATTACHMENT_MESSAGE_RECEIVE;
            }
        } else if (sendType == 1011) {
            type = RECALL_MESSAGE;
        } else if (sendType == 9999) {
            type = SYSTEM_TIP_MESSAGE;
            content = CommonUtils.delHTMLTag(content);
            Log.i(TAG, "MessageBean: system tip msg" + content);
        } else if (message.optInt("isMailMsg") == 1) {
            content = CommonUtils.delHTMLTag(content);
            if (sendMessageUser.id.equals(userId)) {
                type = SMALL_MAIL_SEND;
            } else {
                type = SMALL_MAIL_RECIVE;
            }
            smallMailId = message.optString("mailId");
        } else if (message.optBoolean("hasEmotion")) {
            if (sendMessageUser.id.equals(userId)) {
                type = EMOJI_MESSAGE_SEND;
            } else {
                type = EMOJI_MESSAGE_RECIVE;
            }
        } else {
            content = CommonUtils.delHTMLTag(content);
            if (sendMessageUser.id.equals(userId)) {
                type = TEXT_MESSAGE_SEND;
            } else {
                type = TEXT_MESSAGE_RECIVE;
            }
        }
        if (message.optInt("isRead") == 1)
            isNeedNotify = false;
    }


    protected MessageBean(Parcel in) {
        content = in.readString();
        sendTime = in.readLong();
        type = in.readInt();
        id = in.readString();
        uuid = in.readString();
        groupId = in.readString();
        createTime = in.readLong();
        to = in.readString();
        isSendSuccess = in.readByte() != 0;
        toUserBean = in.createTypedArrayList(SimpleUserInfoBean.CREATOR);
        isMessageNew = in.readByte() != 0;
        imgBitMap = in.readParcelable(Bitmap.class.getClassLoader());
        sendMessageUser = in.readParcelable(UserInfoBean.class.getClassLoader());
        imagePath = in.readString();
        setPrivate = in.readInt();
        isNeedNotify = in.readByte() != 0;
        smallMailId = in.readString();
        toUserNames = in.readString();
        imgSrc = in.readParcelable(ImgMsgXmlBean.class.getClassLoader());
        sendType = in.readInt();
    }

    public static final Creator<MessageBean> CREATOR = new Creator<MessageBean>() {
        @Override
        public MessageBean createFromParcel(Parcel in) {
            return new MessageBean(in);
        }

        @Override
        public MessageBean[] newArray(int size) {
            return new MessageBean[size];
        }
    };

    public void initToList(JSONArray toArray) {
        int size = toArray.length();

        toUserBean = new ArrayList<>();
        toUserBean.add(new SimpleUserInfoBean(sendMessageUser));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            JSONObject user = toArray.optJSONObject(i);
            SimpleUserInfoBean bean = new SimpleUserInfoBean(user);
            if (i > 0)
                sb.append(",");
            sb.append(bean.name);
            toUserBean.add(bean);
        }
        toUserNames = sb.toString();
    }

    public void initToList() {
        try {
            if (!TextUtils.isEmpty(to)) {
                JSONArray jsonArray = new JSONArray(to);
                initToList(jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isImageFileExits() {
        if (type == IMAGE_MESSAGE_RECIVE || type == IMAGE_MESSAGE_SEND) {
            File file = new File(imagePath);
            return file.exists();
        }
        return true;
    }

    public boolean isEmojiMessage() {
        return type == EMOJI_MESSAGE_RECIVE || type == EMOJI_MESSAGE_SEND;
    }


    public boolean isImageMsg() {
        return type == IMAGE_MESSAGE_RECIVE || type == IMAGE_MESSAGE_SEND;
    }

    public boolean isAttachmentMsg() {
        return type == ATTACHMENT_MESSAGE_RECEIVE || type == ATTACHMENT_MESSAGE_SEND;
    }

    public boolean isCanReCall() {
        return System.currentTimeMillis() - sendTime <= 120000;
    }

    public boolean isSmallMailMsg() {
        return type == SMALL_MAIL_RECIVE || type == SMALL_MAIL_SEND;
    }

    public boolean isCurrentUserMsg() {
        return sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id);
    }

    public boolean isNeedToNotify() {
        return isNeedNotify && type != MessageBean.SYSTEM_TIP_MESSAGE && !sendMessageUser.id.equals(PreferencesHelper.getInstance().getCurrentUser().id);
    }

    public String getSmallMailSendToName() {
        return toUserNames;
/*         StringBuilder sb = new StringBuilder();
       int index = 0;
        for (SimpleUserInfoBean user : toUserBean) {
            if (user.userId.equals(PreferencesHelper.getInstance().getCurrentUser().id))
                continue;
            if (index > 0)
                sb.append(",");
            sb.append(user.name);
            ++index;
        }
        return sb.toString();*/
    }

    @Override
    public String
    toString() {
        return "MessageBean{" +
                "content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", type=" + type +
                ", id='" + id + '\'' +
                ", uuid='" + uuid + '\'' +
                ", groupId='" + groupId + '\'' +
                ", createTime=" + createTime +
                ", isSendSuccess=" + isSendSuccess +
                ", isMessageNew=" + isMessageNew +
                ", to=" + to +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeLong(sendTime);
        dest.writeInt(type);
        dest.writeString(id);
        dest.writeString(uuid);
        dest.writeString(groupId);
        dest.writeLong(createTime);
        dest.writeString(to);
        dest.writeByte((byte) (isSendSuccess ? 1 : 0));
        dest.writeTypedList(toUserBean);
        dest.writeByte((byte) (isMessageNew ? 1 : 0));
        dest.writeParcelable(imgBitMap, flags);
        dest.writeParcelable(sendMessageUser, flags);
        dest.writeString(imagePath);
        dest.writeInt(setPrivate);
        dest.writeByte((byte) (isNeedNotify ? 1 : 0));
        dest.writeString(smallMailId);
        dest.writeString(toUserNames);
        dest.writeParcelable(imgSrc, flags);
        dest.writeInt(sendType);
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", content);
            jsonObject.put("createTime", createTime);
            jsonObject.put("from", sendMessageUser.getUserJson());
            jsonObject.put("sendTime", sendTime);
            jsonObject.put("to", new JSONArray(to));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
