package com.inspur.playwork.model.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.CommonUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 微聊页面的话题聊天和非话题聊天
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class VChatBean implements Parcelable, Comparable<VChatBean> {

    private static final String TAG = "VChatBeanFan";

//    private static final String USER_ID = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);

    public String groupId;
    public String topic;

    public int isGroup;

    public ArrayList<UserInfoBean> memberList; // 人员列表

    public String members;

    public String lastMsg;

    public long lastChatTime;

    public String msgId;

    public int unReadMsgNum;

    public boolean isNeedCreateAvatar;

    public ArrayList<String> avatarList; // 所有人员头像列表，用于九宫格头像合成

    public VChatBean() {
    }

    @Override
    public String toString() {
        return "VChatBean{" +
                ", lastChatTime=" + lastChatTime +
                ", lastMsg='" + lastMsg + '\'' +
                ", topic='" + topic + '\'' +
                ", groupId='" + groupId + '\'' +
                ", unReadMsgNum='" + unReadMsgNum + '\'' +
                '}';
    }


    public VChatBean(JSONObject result, ArrayMap<String, Long> avatars) {
        if (result.optBoolean("serviceNumber", false)) {
            groupId = result.optString("groupId");
            if (!TextUtils.isEmpty(result.optString("lastMsg", "")))
                lastMsg = EncryptUtil.aesDecrypt(result.optString("lastMsg"));
            PreferencesHelper.getInstance().writeToPreferences(PreferencesHelper.SERVICE_NUM_GROUP_ID, groupId);
            msgId = result.optString("msgId");
            lastChatTime = result.optLong("lastChatTime");
            unReadMsgNum = 0;
            topic = result.optString("subject");
            isGroup = 0;
            this.members = result.optString("member");
            return;
        }
        memberList = new ArrayList<>();
        avatarList = new ArrayList<>();
        groupId = result.optString("groupId");
        if (!TextUtils.isEmpty(result.optString("lastMsg", "")))
            lastMsg = EncryptUtil.aesDecrypt(result.optString("lastMsg"));

//        if (lastMsg == null) {
//            lastMsg = "";
//        }
//        if (lastMsg.startsWith("<p><img")) {
//            lastMsg = "[图片]";
//        } else {
//            lastMsg = CommonUtils.delHTMLTag(lastMsg);
//        }

        msgId = result.optString("msgId");
        isGroup = result.optInt("isGroup");
        JSONArray members = result.optJSONArray("member");
        this.members = result.optString("member");

        int memberCount = members.length();

        for (int j = 0; j < memberCount; j++) {
            JSONObject member = members.optJSONObject(j);
            UserInfoBean user = new UserInfoBean(member);
            memberList.add(user);
        }
        String memberNames = this.setAvatars(avatars);
        if (result.has("subject")) {
            topic = result.optString("subject");
        } else {
            topic = memberNames;
        }
        unReadMsgNum = 0;
        lastChatTime = result.optLong("lastChatTime");
    }

    @SuppressWarnings("unchecked")
    private VChatBean(Parcel in) {
        topic = in.readString();
        lastMsg = in.readString();
        lastChatTime = in.readLong();
        groupId = in.readString();

        memberList = in.readArrayList(getClass().getClassLoader());

        unReadMsgNum = in.readInt();
        avatarList = in.readArrayList(getClass().getClassLoader());
        msgId = in.readString();
        isGroup = in.readInt();
        isNeedCreateAvatar = in.readByte() > 0;
    }

    public String setAvatars(ArrayMap<String, Long> avatars) {

        if (avatarList == null)
            avatarList = new ArrayList<>();

        avatarList.clear();

        String names = "";
        int i = 0;
        for (UserInfoBean user : memberList) {
            if (isGroup == 0 && PreferencesHelper.getInstance().getCurrentUser().id.equals(user.id)) {
                continue;
            }
            if (avatars.containsKey(user.id)) {
                long avatarId = avatars.get(user.id);
                if (avatarId >= user.avatar) {
                    user.avatar = avatarId;
                } else {
                    avatars.put(user.id, user.avatar);
                }
            } else {
                avatars.put(user.id, user.avatar);
            }
            avatarList.add(user.id + "-" + user.avatar + ".png");
            if (i == 0)
                names += user.name;
            else
                names += ("," + user.name);
            i++;
        }
        return names;
    }

    public static final Creator<VChatBean> CREATOR = new Creator<VChatBean>() {
        @Override
        public VChatBean createFromParcel(Parcel in) {
            return new VChatBean(in);
        }

        @Override
        public VChatBean[] newArray(int size) {
            return new VChatBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topic);
        dest.writeString(lastMsg);
        dest.writeLong(lastChatTime);
        dest.writeString(groupId);

        dest.writeList(memberList);

        dest.writeInt(unReadMsgNum);
        dest.writeList(avatarList);
        dest.writeString(msgId);
        dest.writeInt(isGroup);

        dest.writeByte((byte) (isNeedCreateAvatar ? 1 : 0));
    }

    @Override
    public int compareTo(@NonNull VChatBean another) {
        if (this.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
            return -1;
        }

        if (another.groupId.equals(PreferencesHelper.getInstance().getServiceNumGroupId())) {
            return 1;
        }

        if (another.lastChatTime > this.lastChatTime) {
            return 1;
        } else if (another.lastChatTime == this.lastChatTime) {
            return 0;
        } else {
            return -1;
        }
//        return (int) (another.lastChatTime - this.lastChatTime);
    }

    public void setMember(String users) throws JSONException {
        JSONArray members = new JSONArray(users);
        setMember(members);
    }

    public String setMember(JSONArray members) {
        int memberCount = members.length();

        if (memberList == null)
            memberList = new ArrayList<>();

        memberList.clear();

        String memberNames = "";

        int i = 0;
        String useId = PreferencesHelper.getInstance().getCurrentUser().id;
        for (int j = 0; j < memberCount; j++) {
            JSONObject member = members.optJSONObject(j);
            UserInfoBean user = new UserInfoBean(member);
            memberList.add(user);
            if (user.id.equals(useId))
                continue;
            if (i == 0) {
                memberNames += user.name;
            } else {
                memberNames += "," + user.name;
            }
            i++;
        }
        return memberNames;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || !(o instanceof VChatBean)) && groupId.equals(((VChatBean) o).groupId);
    }
}
