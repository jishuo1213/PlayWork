package com.inspur.playwork.model.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.PreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Fan on 15-9-22.
 */
public class ChatWindowInfoBean implements Parcelable {

    private static final String TAG = "ChatWindowInfoFan";


    public ArrayList<UserInfoBean> allMemberList;

    public ArrayList<UserInfoBean> exitMemberList;

    public ArrayList<UserInfoBean> chatMemberList;

    public ArrayList<UserInfoBean> hideMemberList;

    public String groupId;
    public String mailId;
    public String createUser;
    public String taskTitle;
    public boolean isSingle;

    public String taskPlace;
    public String memberNames;

    public String taskId;

    public String memberString;
    public String lastToString;
    public String exitString;

    public ChatWindowInfoBean() {
    }

    public ChatWindowInfoBean(JSONObject groupInfoJson) {
        groupId = groupInfoJson.optString("groupId");
        mailId = groupInfoJson.optString("mailId");
        createUser = groupInfoJson.optString("createId");
        taskTitle = groupInfoJson.optString("subject");
        taskPlace = groupInfoJson.optString("taskPlace");
        isSingle = groupInfoJson.optBoolean("isSingle");
        JSONArray members = groupInfoJson.optJSONArray("member");// 全部人员
        JSONArray chatMember = groupInfoJson.optJSONArray("lastTo");// 正在聊天人员
        JSONArray exitMember = groupInfoJson.optJSONArray("exitedMember");// 退出人员
        memberString = members.toString();
//        if (members != null)
//        else
//            memberString = "[]";
        if (exitMember == null) {
            exitMember = new JSONArray();
            exitString = "[]";
        }
        exitString = exitMember.toString();
        initList(members, chatMember, exitMember);
    }

    public void initList(JSONArray members, JSONArray chatMember, JSONArray exitMember) {
        if (allMemberList == null)
            allMemberList = new ArrayList<>(); // all = member + exited
        if (hideMemberList == null)
            hideMemberList = new ArrayList<>(); // hide = member - lastTo - me
        if (chatMemberList == null)
            chatMemberList = new ArrayList<>(); // chat = lastTo == null ? (member - me) : lastTo - exitMember
        if (exitMemberList == null)
            exitMemberList = new ArrayList<>(); // exit = exited
        allMemberList.clear();
        hideMemberList.clear();
        chatMemberList.clear();
        exitMemberList.clear();
        int memberCount = members.length();
        int chatCount = chatMember.length();
        int exitCount = exitMember.length();
        Log.i(TAG, "initList: " + memberCount + "=====" + chatCount + "=====" + exitCount);
        String currentUser = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        initExitPersonList(exitMember, exitCount);
        if (chatCount > 0) {
            lastToString = chatMember.toString();
            initChatMemberList(chatMember, chatCount, currentUser);
        } else {
            lastToString = members.toString();
            initChatMemberList(members, memberCount, currentUser);
        }
        initHideMemberList(members, memberCount, currentUser);
    }

    private void initHideMemberList(JSONArray members, int memberCount, String currentUser) {
        memberNames = "";
        int j = 0;
        for (int i = 0; i < memberCount; i++) {
            JSONObject user = members.optJSONObject(i);
            String id = user.optString("id");
            UserInfoBean mBean = new UserInfoBean(user);
            if (isSingle && id.equals(currentUser)) {
                continue;
            }
            if (j == 0) {
                memberNames += mBean.name;
            } else {
                memberNames += ("," + mBean.name);
            }
            j++;
            if (!checkChatListHasPerson(id) && !id.equals(currentUser)) {
                Log.i(TAG, "initHideMemberList: add hide user");
                hideMemberList.add(mBean);
            }
            allMemberList.add(mBean);
        }
    }

    private void initExitPersonList(JSONArray chatMember, int exitCount) {
        String currentUser = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        for (int i = 0; i < exitCount; i++) {
            JSONObject exitUser = chatMember.optJSONObject(i);
            UserInfoBean mBean = new UserInfoBean(exitUser);
            if (mBean.id.equals(currentUser))
                continue;
            exitMemberList.add(mBean);
            allMemberList.add(mBean);
        }
    }

    private boolean checkChatListHasPerson(String id) {
        for (Parcelable chatUser : chatMemberList) {
            if (((UserInfoBean) chatUser).id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkExitListHasPerson(String id) {
        if (exitMemberList == null || exitMemberList.size() == 0) {
            return false;
        }
        for (Parcelable chatUser : exitMemberList) {
            if (((UserInfoBean) chatUser).id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void initChatMemberList(JSONArray chatMember, int chatCount, String currentUser) {
        for (int i = 0; i < chatCount; i++) {
            JSONObject chatUser = chatMember.optJSONObject(i);
            if (chatUser.optString("id").equals(currentUser)) {
                continue;
            }
            UserInfoBean mBean = new UserInfoBean(chatUser);
            Log.i(TAG, "initChatMemberList: " + mBean.name);
            if (!checkExitListHasPerson(mBean.id))
                chatMemberList.add(mBean);
        }
    }

    @SuppressWarnings("unchecked")
    protected ChatWindowInfoBean(Parcel in) {
        groupId = in.readString();
        mailId = in.readString();
        createUser = in.readString();
        taskTitle = in.readString();
        taskPlace = in.readString();
        allMemberList = in.readArrayList(getClass().getClassLoader());
        exitMemberList = in.readArrayList(getClass().getClassLoader());
        chatMemberList = in.readArrayList(getClass().getClassLoader());
        hideMemberList = in.readArrayList(getClass().getClassLoader());
        memberNames = in.readString();
        isSingle = in.readByte() != 0;
        taskId = in.readString();
    }

    public static final Creator<ChatWindowInfoBean> CREATOR = new Creator<ChatWindowInfoBean>() {
        @Override
        public ChatWindowInfoBean createFromParcel(Parcel in) {
            return new ChatWindowInfoBean(in);
        }

        @Override
        public ChatWindowInfoBean[] newArray(int size) {
            return new ChatWindowInfoBean[size];
        }
    };

    // all = member + exited
    // hide = member - lastTo - me
    // chat = lastTo - me
    // exit = exited
    public void calculateChangeMember() {
        UserInfoBean me = PreferencesHelper.getInstance().getCurrentUser();
        JSONArray members = new JSONArray();// 全部人员 last to + hide
        JSONArray lastToMember = new JSONArray();// 正在聊天人员 last to
        JSONArray exitMember = new JSONArray();// 退出人员 exit

        for (UserInfoBean exitPerson : exitMemberList) {
            exitMember.put(exitPerson.getUserJson());
        }

        memberNames = "";

        int i = 0;
        for (UserInfoBean lastToPerson : chatMemberList) {
            lastToMember.put(lastToPerson.getUserJson());
            members.put(lastToPerson.getUserJson());
            if (i == 0)
                memberNames += lastToPerson.name;
            else
                memberNames += ("," + lastToPerson.name);
            i++;
        }
        lastToMember.put(me.getUserJson());
        members.put(me.getUserJson());

        for (UserInfoBean hidePerson : hideMemberList) {
            members.put(hidePerson.getUserJson());
            if (i == 0)
                memberNames += hidePerson.name;
            else
                memberNames += ("," + hidePerson.name);
            i++;
        }

        Log.i(TAG, "calculateChangeMember: " + memberNames);

        memberString = members.toString();
        lastToString = lastToMember.toString();
        exitString = exitMember.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupId);
        dest.writeString(mailId);
        dest.writeString(createUser);
        dest.writeString(taskTitle);
        dest.writeString(taskPlace);
        dest.writeList(allMemberList);
        dest.writeList(exitMemberList);
        dest.writeList(chatMemberList);
        dest.writeList(hideMemberList);
        dest.writeString(memberNames);
        dest.writeByte((byte) (isSingle ? 1 : 0));
        dest.writeString(taskId);
    }

    public void initMemberLists(String allMembers, String lastTo, String exitMembers) {
        JSONArray members = null;// 全部人员
        JSONArray chatMember = null;// 正在聊天人员
        JSONArray exitMember = null;// 退出人员
        try {
            members = new JSONArray(allMembers);
            chatMember = new JSONArray(lastTo);
            exitMember = new JSONArray(exitMembers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initList(members, chatMember, exitMember);
    }

    @Override
    public String toString() {
        return "ChatWindowInfoBean{" +
                "allMemberList=" + allMemberList +
                ", exitMemberList=" + exitMemberList +
                ", chatMemberList=" + chatMemberList +
                ", hideMemberList=" + hideMemberList +
                ", groupId='" + groupId + '\'' +
                ", mailId='" + mailId + '\'' +
                ", createUser='" + createUser + '\'' +
                ", taskTitle='" + taskTitle + '\'' +
                ", isSingle=" + isSingle +
                ", taskPlace='" + taskPlace + '\'' +
                ", memberNames='" + memberNames + '\'' +
                ", taskId='" + taskId + '\'' +
                ", memberString='" + memberString + '\'' +
                ", lastToString='" + lastToString + '\'' +
                ", exitString='" + exitString + '\'' +
                '}';
    }
}
