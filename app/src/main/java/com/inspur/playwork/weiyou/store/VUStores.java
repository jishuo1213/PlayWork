package com.inspur.playwork.weiyou.store;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;

import com.inspur.fan.decryptmail.DecryptMail;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.actions.message.MessageActions;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.common.LocalFileBean;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;
import com.inspur.playwork.stores.Stores;
import com.inspur.playwork.stores.message.GroupStores;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PictureUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.SingleRefreshManager;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailContacts;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.utils.db.bean.MailDirectory;
import com.inspur.playwork.utils.db.bean.MailTask;
import com.inspur.playwork.utils.encryptUtil.Base64Utils;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.inspur.playwork.weiyou.WeiYouMainActivity;
import com.inspur.playwork.weiyou.rsa.CAEncryptUtils;
import com.inspur.playwork.weiyou.rsa.CAObject;
import com.inspur.playwork.weiyou.utils.DBUtil;
import com.inspur.playwork.weiyou.utils.MailSynchronizer;
import com.inspur.playwork.weiyou.utils.MailUtil;
import com.inspur.playwork.weiyou.utils.NetStatusReceiver;
import com.inspur.playwork.weiyou.utils.OkHttpClientManager;
import com.inspur.playwork.weiyou.utils.VUFileUtil;
import com.inspur.playwork.weiyou.utils.WeiYouUtil;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MailSSLSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

/**
 * Created by sunyuan on 2016/11/23 0023 10:50.
 * Email: sunyuan@inspur.com
 */

public class VUStores extends Stores implements MailSynchronizer.SendMailResultListener {
    private static final String TAG = "VUStores---";

    public final static String MAIL_ACCOUNT_STORE_KEY = "currUsingMailAccount";

    public static final int CURR_NETWORK_TYPE_WIFI = 0;
    public static final int CURR_NETWORK_TYPE_2G = 0;
    public static final int CURR_NETWORK_TYPE_3G = 0;
    public static final int CURR_NETWORK_TYPE_4G = 0;

    private static final int CHECK_MAIL_RESULT = 0x11;
    private static final int DOWNLOAD_MAIL_RESULT = 0x12;
    private static final int FETCH_MESSAGES_RESULT = 0x13;
    private static final int DOWNLOAD_MAIL_DETAIL = 0x14;

    //  发送结果类型
    private static final int SEND_MAIL_RESULT = 0x90;
    private static final int SEND_MAIL_SUCCESS = 0x91;
    private static final int SEND_MAIL_FAILED = 0x92;
    private static final int SEND_MAIL_PARTIALLY = 0x93;
    private static final int SEND_MAIL_PBK_ERROR = 0x94;
    private static final int HANDLE_DRAFT_MAIL_TASK = 0x95;
    private static final int VERIFY_MAIL_ACCOUNT = 0x96;
//    private static final int PARSE_MAIL_DETAIL = 0x97;

    private static final int INIT_DEFAULT_MAIL_ACCOUNT = 0;
    private static final int SAVE_NEW_MAIL_ACCOUNT = 1;
    private static final int UPDATE_EXISTS_MAIL_ACCOUNT = 2;
    private static final String NOT_SHOW_GUIDE_PAGE = "notShowGuidePage";
    private static boolean allowSyncWithMobileTraffic; //是否允许使用手机流量同步邮件

    private Handler vuMailHandler;
    public List<CAObject> caList = new ArrayList<>();       //证书列表
    public CAObject currUsingCA;                            //当前使用的证书
    public int saveAccountCode = INIT_DEFAULT_MAIL_ACCOUNT; //账号保存操作 类型
    public ArrayList<Long> showingMailIdList = new ArrayList<>();//当前显示的邮件列表ID集合
    public ArrayList<MailDetail> exchangeMailList = new ArrayList<>();//往来邮件列表
    public ArrayList<MailAccount> mailAccountCache = new ArrayList<>();//当前登录账号关联的邮箱账号
    public MailAccount currMailAccount;//当前使用的邮箱账号
    public String currEmail;//当前邮箱账号地址

    public MailAccount newMailAccount;//新建邮箱账号缓存
    public int targetMailAccountIndex;//当前使用的邮箱账号
    public long currDirId;//当前已选择的邮箱目录
    public String currDirName;
    private ArrayList<MailDirectory> directoryData = new ArrayList<>();//当前邮箱账号下的目录列表数据

    public MailDetail currMail;
    public List<MailAttachment> currAttachmentList = new ArrayList<>();

    private ArrayMap<String, ArrayList<String>> downloadingUidCacheMap = new ArrayMap<>();//各邮箱账号正在下载的uid列表缓存

    private DecryptMail dm;
    private UserInfoBean selfUser;
    private static String selfEmail;

    private ArrayList<MailDetail> mailListData = new ArrayList<>();
    private ArrayList<MailDetail> outBoxMailListData = new ArrayList<>();

    private POP3Store store;
    private POP3Folder folder;
    private FetchProfile profile;
//    private Session pop3Session;

    private WeakReference<MailListOperation> mailListOperation = new WeakReference<>(null);
    private WeakReference<MailDetailOperation> mailDetailOperation = new WeakReference<>(null);
    private WeakReference<MailAttachmentOperation> mailAttachmentOperation = new WeakReference<>(null);
    private WeakReference<VUActivityOperation> vuActivityOperation = new WeakReference<>(null);
    private WeakReference<VUSettingsOperation> vuSettingsOperation = new WeakReference<>(null);
    private WeakReference<AddNewAccountOperation> addNewAccountOperation = new WeakReference<>(null);
    private WeakReference<AccountSettingsOperation> accountSettingsOperation = new WeakReference<>(null);
    private WeakReference<ExchangeMailListOperation> exchangeMailListOperation = new WeakReference<>(null);
    private WeakReference<AccountCaOperation> accountCaOperation = new WeakReference<>(null);
    private WeakReference<FeedbackOperation> feedbackOperation = new WeakReference<>(null);
    private WeakReference<ContactSelectorOperation> contactSelectOperation = new WeakReference<>(null);

    private WeakReference<WriteMailOperation> writeMailOperation = new WeakReference<>(null);
    private boolean isInContactSelector;

    private MailSynchronizer mailSynchronizer;
    public boolean isWritingMail = false;

    public VUStores() {
        super(Dispatcher.getInstance());
    }

    public void initVUData(boolean _isWritingMail) {
//        PreferencesHelper ph = PreferencesHelper.getInstance();
        isWritingMail = _isWritingMail;
        if (selfUser == null) {
            selfUser = PreferencesHelper.getInstance().getCurrentUser();
//            selfUser = ph.getUserInfoToNative();
            Log.i(TAG, "getInstance: selfUser = "+selfUser.toString());
            selfEmail = selfUser.id + AppConfig.EMAIL_SUFFIX;
            Log.i(TAG, "getInstance: selfEmail = "+selfEmail);
        }
        allowSyncWithMobileTraffic = PreferencesHelper.getInstance().readBooleanPreference("allowSyncWithMobileTraffic_" + selfUser.id);
        if(dm == null) {
            dm = new DecryptMail();
            dm.init();
        }
        if(vuMailHandler == null) vuMailHandler = new VUMailHandler(new WeakReference<>(this));
        if(mailSynchronizer == null) mailSynchronizer = new MailSynchronizer(dm, this);
        Log.i(TAG, "initVUData: mailAccountCache.size() = "+mailAccountCache.size());
        DBUtil.queryMailAccountList(selfUser.id);
    }

    /**
     * 读取邮箱账号列表回调
     *
     * @param maList
     */
    private void getMailAccountListCallback(ArrayList<MailAccount> maList) {
//        先从本地数据库查询邮箱账号列表 如果没有的话 初始化浪潮邮箱    QUERY_MAIL_ACCOUNT_LIST
        if (maList == null) {
            String enc_userId = EncryptUtil.encrypt2aesAD("home\\" + selfUser.id);
            newMailAccount = new MailAccount(null, selfUser.id, selfUser.id
                    , selfUser.name, selfEmail
                    , enc_userId, selfUser.passWord
                    , AppConfig.WY_CFG.POP3_SERVER_HOST
                    , AppConfig.WY_CFG.POP3_SERVER_PORT
                    , AppConfig.WY_CFG.POP3_SERVER_SSL
                    , AppConfig.WY_CFG.SMTP_SERVER_HOST
                    , AppConfig.WY_CFG.SMTP_SERVER_PORT
                    , AppConfig.WY_CFG.SMTP_SERVER_SSL
                    , AppConfig.WY_CFG.SMTP_SERVER_TLS
                    , "pop3s", true, true, true);
            saveOneMailAccount(newMailAccount, INIT_DEFAULT_MAIL_ACCOUNT);
        } else {
            mailAccountCache = maList;
            String currAccountEmail = PreferencesHelper.getInstance().readStringPreference(selfUser.id + MAIL_ACCOUNT_STORE_KEY);
            if (currAccountEmail.length() > 0) {
                for (MailAccount ma : maList) {
                    int index = maList.indexOf(ma);
//                    如果登录账号改密码了，则同步Inspur邮箱密码
                    if (ma.getEmail().equals(selfEmail) && !ma.getPassword().equals(selfUser.passWord)) {
                        ma.setPassword(selfUser.passWord);
                        maList.set(index, ma);
                        saveOneMailAccount(ma, UPDATE_EXISTS_MAIL_ACCOUNT);
                    }
                    if (currAccountEmail.equals(ma.getEmail())) {
                        switchMailAccount(index);//切换到上次的邮箱账号
                    }
                    refreshSpinner(mailAccountCache, index);
                }
            } else {
                switchMailAccount(0);//切换到第一个邮箱账号
                refreshSpinner(mailAccountCache, 0);
            }
        }
        if(vuActivityOperation.get()!=null) vuActivityOperation.get().initSettingButton();
    }

    /**
     * 保存邮箱账号
     *
     * @param mc
     */
    public void saveOneMailAccount(MailAccount mc, int code) {
        saveAccountCode = code;
        Log.d(TAG, "saveOneMailAccount() called with: " + "mc = [" + mc + "], code = [" + code + "]");
        DBUtil.saveMailAccount(mc);
    }

    /**
     * 保存邮箱账号的回调
     *
     * @param accountId
     */
    private void saveMailAccountCallback(boolean res, long accountId) {
        switch (saveAccountCode) {
            case INIT_DEFAULT_MAIL_ACCOUNT://第一次打开微邮 新增默认邮箱
                if (res == true) {
                    newMailAccount.setId(accountId);
                    mailAccountCache.add(newMailAccount);
                    switchMailAccount(0);
                    refreshSpinner(mailAccountCache, 0);
                } else {
                    toast("保存默认邮箱账号失败，请联系开发人员");
                }
                break;
            case SAVE_NEW_MAIL_ACCOUNT://设置中新增邮箱账号
                if (res == true) {
                    newMailAccount.setId(accountId);
                    mailAccountCache.add(newMailAccount);
                    addNewAccountOperation.get().saveMailAccountCallback(true);
                    refreshSpinner(mailAccountCache, mailAccountCache.size() - 1);
                    switchMailAccount(mailAccountCache.size() - 1);
                } else {
                    toast("非常抱歉，保存新邮箱账号失败了");
                }
                break;
            case UPDATE_EXISTS_MAIL_ACCOUNT://修改邮箱账号
                if (res == true) {
                    accountSettingsOperation.get().updateMailAccountCallback(true);
                    mailAccountCache.set(targetMailAccountIndex, newMailAccount);
                } else {
                    toast("非常抱歉，保存更新后的邮箱账号失败了");
                }
                break;
        }
        newMailAccount = null;
    }

    /**
     * 切换邮箱账号
     *
     * @param pos
     */
    public void switchMailAccount(int pos) {
        currMailAccount = mailAccountCache.get(pos);
        currEmail = currMailAccount.getEmail();
        Log.d(TAG, "switchMailAccount() called with: " + "pos = [" + pos + "]" + currEmail);
        PreferencesHelper.getInstance().writeToPreferences(selfUser.id + MAIL_ACCOUNT_STORE_KEY, currEmail);

//        初始化数字证书信息
        List<CAObject> _caList = CAEncryptUtils.getCAListData(currEmail);
        if (!_caList.isEmpty()) {
            for (CAObject cao : _caList) {
                if (cao.isDefaultCA()) {
                    currUsingCA = cao;
                }
            }
            if (currUsingCA == null) {//如果没有已选中的数字证书，则默认指定为第一个
                currUsingCA = _caList.get(0);
                currUsingCA.setDefaultCA(true);
                _caList.set(0, currUsingCA);

            }
            currUsingCA = CAEncryptUtils.readCAFile(currEmail, currUsingCA.getFilepath(), currUsingCA.getCaname(), currUsingCA.getPassword());
            if (currUsingCA.getErrorInfo() != null) {
                toast("数字证书初始化失败，请重新选择或安装");
            }
        } else {
            currUsingCA = null;
        }
        //初始化当前邮箱的正在下载邮件的缓存 ？？？？？？？？？？
        if (!downloadingUidCacheMap.containsKey(currEmail)) {
            downloadingUidCacheMap.put(currEmail, new ArrayList<String>());
        }
        Log.i(TAG, "switchMailAccount: vuActivityOperation.get() != null ? "+(vuActivityOperation.get() != null));
        if(!isWritingMail) {//如果是打开微邮
            DBUtil.queryMailDirectory(currEmail);//查询邮箱目录
            FileUtil.validateMailFolder(currEmail);//检查该邮箱的缓存文件目录是否存在，若不存在就创建一遍
            if (vuActivityOperation.get() != null)
                vuActivityOperation.get().switchSelectedAccount(currEmail);
//        去取发件箱邮件
            DBUtil.queryOutBoxMailList(currEmail);//查询发件箱邮件 并开启同步器
        }
        else{
            initWriteMailData();
        }
    }

    private void getMailDirectoryListCallback(ArrayList<MailDirectory> mdList) {
//        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_ATTACHMENT_LIST, currEmail, "附件列表", true, _now, _now));
        /*
         * 此处为当前邮箱的目录列表数据，静态数据
         * 连接上socket服务之后，还需要从socket服务器获取每个邮箱账号的目录列表
         */
        Log.i(TAG, "getMailDirectoryListCallback: " + Thread.currentThread().getName());
        directoryData.clear();
        Date _now = new Date();
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_INBOX, currEmail, "收件箱", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_UNREAD_MAIL, currEmail, "未读邮件", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_SENT_MAIL, currEmail, "已发送邮件", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_DRAFTBOX, currEmail, "草稿箱", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_OUTBOX, currEmail, "发件箱", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_DELETED_MAIL, currEmail, "已删除邮件", true, _now, _now));
        directoryData.add(new MailDirectory(AppConfig.WY_CFG.DIR_ID_MARKED_MAIL, currEmail, "已标注邮件", true, _now, _now));
        if (mdList != null) {
            directoryData.addAll(mdList);
        }
//        刷新侧边栏目录视图
        vuActivityOperation.get().renderMailDirectoryView();
    }

    public void switchMailDirectory(int position) {
//        Log.i(TAG, "switchMailDirectory: ....");
//        再次判断是否是切换了新账号
        boolean accountIsChanged = false;
        boolean isNeedSwitch = true;
        if (position == -1) {
            position = 0;
            accountIsChanged = true;
            isNeedSwitch = false;
        }

        MailDirectory dir = directoryData.get(position);
        if (accountIsChanged || currDirId != dir.getId()) {
            currDirId = dir.getId();
            currDirName = dir.getName();
            Log.i(TAG, "switchMailList dirId:" + currDirId);
            Log.i(TAG, "before showMailListFragment...");
            vuActivityOperation.get().showMailListFragment(currDirName);
            if (isNeedSwitch)
                vuActivityOperation.get().switchMailList(position);
        }
    }

    public void loadListData() {
//        设置带上拉下拉的listView
        mailListOperation.get().setListViewPullWay();
        if (currDirId == AppConfig.WY_CFG.DIR_ID_UNREAD_MAIL) {
            DBUtil.queryUnreadMailList(currEmail);
        } else if (currDirId == AppConfig.WY_CFG.DIR_ID_MARKED_MAIL) {
            DBUtil.queryMarkedMailList(currEmail);
        } else if (currDirIsOutbox()) {
            Log.i(TAG, "loadListData: outBoxMailListData.size() = "+outBoxMailListData);
            mailListData.clear();
            mailListData.addAll(outBoxMailListData);
            renderMailListView();
        } else {
            DBUtil.queryMailListByDirId(currEmail, currDirId);
        }
    }

    private void loadListDataCallback(SparseArray mlSA) {
        mailListData.clear();
        if (mlSA != null || mlSA.size() > 0)
            mailListData.addAll((ArrayList<MailDetail>) mlSA.get(0));
        Log.i(TAG, "loadListDataCallback mailListData.size(): " + mailListData.size());
        renderMailListView();
    }

    /***********************************************************************/

    private long optTimer = 0;

    private boolean isQueryNewMail = true;
    private boolean isDownloadingMail = false;

    private int downloadingMailCount;

    /**
     * 先获取本地所有邮件的uid列表，然后去检查服务器邮件UID列表
     */
    private Runnable refreshMailListViewRunnable = new Runnable() {
        @Override
        public void run() {
            renderMailListView();
        }
    };
    public void checkNewMail(boolean iqnm) {
        isQueryNewMail = iqnm;
        long currTime = new Date().getTime();
        if (currTime - optTimer > 5000 && !isDownloadingMail && currDirIsInbox()) {
            optTimer = currTime;
            DBUtil.queryMailUidList(currEmail);//取本地邮件Uid list
        } else {
            Log.w(TAG, "checkNewMail: 发送请求太频繁或正在下载邮件 " + isDownloadingMail);
            Handler tepmHandler = new Handler();
            tepmHandler.removeCallbacks(refreshMailListViewRunnable);
            tepmHandler.postDelayed(refreshMailListViewRunnable,3000);
        }
    }

    ArrayList<String> localUidList = new ArrayList<>();

    /**
     * 查询完本地邮件UID列表后的回调，用来抓取服务器邮件信息，并过滤出本地没有的邮件，最后下载保存
     * <p>
     * 下载邮件 机制：
     * <p>
     * 1.先获取本地的邮件的uid列表（已排好序，从最新到最早）
     * 2.1 若本地已经有邮件
     * 2.1.1 取第一个uid 为本地最新的邮件的uid
     * 2.1.2 倒序遍历服务器的邮件Message对象数组（为了使邮件按最新到最旧的顺序排列），
     * 把本地没有的邮件（本地最新的邮件除外）的uid 组装进serverUidList，
     * 并把相应的索引组装进serverUidIndexList
     * 2.1.3 获取本地最新的邮件uid在  serverUidList 中的索引 index
     * 2.1.3.1 若 index != -1，则本地最新邮件在serverUidList中，
     * 然后判断isQueryNewMail的值，true为取最新邮件，false为取更早的邮件
     * 2.1.3.1.1 若isQueryNewMail==true 判断index是否等于0，
     * 若是，则说明 <没有最新的邮件>
     * 若否，则 serverUidList 中索引值为0到 index 的邮件是最新的邮件，
     * 注意：这其中 还需要判断index 是否大于 10，每次最多下10封邮件
     * <p>
     * 2.1.3.1.2 若isQueryNewMail==false 判断index是否等于serverUidList.size()-1
     * 若是，则说明 <没有更早的邮件了>
     * 若否，则 serverUidList 中索引值为index 到 serverUidList.size()-1 的邮件是更早的邮件，
     * 注意：这其中 还需要判断index 是否大于 10，每次最多下10封邮件
     * <p>
     * 2.1.3.2 若 index == -1，则本地最新邮件可能在服务端已经被删了，取 serverUidList 前10封邮件
     * <p>
     * 2.2 本地没有邮件，则取最新的10封下载
     *
     * @param existsUidList
     */
    private void queryAllMailUidListCallback(final ArrayList<String> existsUidList) {
        //先同步本地uid列表缓存localUidList与刚取出来的本地邮件UID列表existsUidList
//        Log.i(TAG, "queryAllMailUidListCallback: ```````````````````````");
        if (localUidList.size() == 0 && existsUidList.size() > 0) {
            localUidList.addAll(existsUidList);
        } else {
            for (String _uid : existsUidList) {
                Log.i(TAG, "queryAllMailUidListCallback: existsUidList uid >>> "+_uid);
                if (!localUidList.contains(_uid)) {
                    localUidList.add(_uid);
                }
            }
        }
        String[] luidArr = new String[localUidList.size()];
        localUidList.toArray(luidArr);
//        Log.i(TAG, localUidList.size() + "------localUidList : " + Arrays.toString(luidArr));
//        showProgressDialog("正在检索新邮件");
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                List<MimeMessage> validMessageList = new ArrayList<>();//新邮件list
                try{
                    isDownloadingMail = true;
                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 0, 0, "正在连接邮件服务器..."));
                    javax.mail.Message [] messages = getInboxMessages();
                    Log.i(TAG, "getMailUIDList: messages.length = " + messages.length);
                    if (messages.length == 0) {
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "服务器没有您的邮件"));
                        return;
                    }
//                    Log.i(TAG, "queryAllMailUidListCallback: isQueryNewMail=" + isQueryNewMail);

                    List<String> serverUidList = new ArrayList<>();
                    List<Integer> serverUidIndexList = new ArrayList<>();
                    if (localUidList.size() > 0) {
                        String localNewestUid = localUidList.get(0);
                        Log.i(TAG, "run: localNewestUid = " + localNewestUid);
                        // 过滤出新邮件的对象 并组装进validMessageList
                        for (int i = messages.length - 1; i >= 0; i--) {
                            MimeMessage message = (MimeMessage) messages[i];
                            String uid = folder.getUID(message);
                            if (!localUidList.contains(uid) || localNewestUid.equals(uid)) {
                                serverUidList.add(uid);
                                serverUidIndexList.add(i);
                            }
//                                Log.i(TAG, "run: message.getMessageID() = "+message.getMessageID());
                        }

                        int serverUidListSize = serverUidList.size();

                        String[] uidArr = new String[serverUidListSize];
                        serverUidList.toArray(uidArr);
                        Log.i(TAG, serverUidListSize + "------server uid list: " + Arrays.toString(uidArr));

                        int index = serverUidList.indexOf(localNewestUid);
                        if (index != -1) {//本地最新邮件在服务器邮件列表中
                            Log.i(TAG, "download mail 本地最新邮件在服务器邮件列表中...");
                            if (isQueryNewMail) {//取最新邮件
                                Log.i(TAG, "download mail: 取最新邮件");
                                if (index > 0) {
                                    int begin = 0;
                                    if (index > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE) {
                                        begin = index - AppConfig.WY_CFG.MAIL_NUM_PER_PAGE;
                                    }
                                    serverUidList = serverUidList.subList(begin, index);
                                    Log.i(TAG, "download mail::begin = "+begin);
                                    Log.i(TAG, "download mail::index = "+index);
                                    Log.i(TAG, "download mail::serverUidList.size() = "+serverUidList.size());
                                    serverUidIndexList = serverUidIndexList.subList(begin, index);
                                    int sulSize = serverUidList.size();
                                    if (sulSize > 0) {
                                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 0, 0, "检索到" + sulSize + "封新邮件,正在下载"));
                                    }
                                } else {
                                    serverUidList.clear();
                                    serverUidIndexList.clear();
                                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "未发现新邮件"));
                                }
                            } else {//取更早的邮件
                                Log.i(TAG, "download mail: 取更早的邮件");
                                if (index < serverUidListSize - 1) {
                                    int end = (index < serverUidListSize - AppConfig.WY_CFG.MAIL_NUM_PER_PAGE)
                                            ? index + AppConfig.WY_CFG.MAIL_NUM_PER_PAGE + 1
                                            : serverUidListSize;
//                                    Log.i(TAG, "取更早的邮件: index = " + index);
//                                    Log.i(TAG, "取更早的邮件: end = " + end);
                                    serverUidList = serverUidList.subList(index + 1, end);
                                    serverUidIndexList = serverUidIndexList.subList(index + 1, end);
                                } else {
                                    serverUidList.clear();
                                    serverUidIndexList.clear();
                                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "没有更早的邮件了"));
                                }
//                                Log.i(TAG, "取更早的邮件 serverUidList.size() = " + serverUidListSize);
                            }
                        } else {//本地最新的邮件uid 不在服务器邮件UID列表中
                            Log.i(TAG, "run: 本地最新的邮件uid 不在服务器邮件UID列表中");
                            if (isQueryNewMail) {//取最新邮件
                                serverUidList = serverUidList.subList(0,(serverUidListSize > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE)?AppConfig.WY_CFG.MAIL_NUM_PER_PAGE:serverUidListSize);
                                serverUidIndexList = serverUidIndexList.subList(0,(serverUidListSize > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE)?AppConfig.WY_CFG.MAIL_NUM_PER_PAGE:serverUidListSize);
                            }else{
                                serverUidList = serverUidList.subList((serverUidListSize > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE)?serverUidListSize - AppConfig.WY_CFG.MAIL_NUM_PER_PAGE -1:0,serverUidListSize);
                                serverUidIndexList = serverUidIndexList.subList((serverUidListSize > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE)?serverUidListSize - AppConfig.WY_CFG.MAIL_NUM_PER_PAGE -1:0,serverUidListSize);
                            }
                        }
                        int suls = serverUidList.size();
                        Log.i(TAG, "serverUidList.size() = " + suls);
                        if (suls > 0) {
                            for (int j = 0; j < suls; j++) {
                                MimeMessage mmsg = (MimeMessage) messages[serverUidIndexList.get(j)];
                                validMessageList.add(mmsg);
                                Log.i(TAG, "即将下载第"+j+"封邮件的uid = "+serverUidList.get(j));
//                                    Log.i(TAG, "run: mmsg.getSubject() = " + mmsg.getSubject() );
                                localUidList.add(serverUidList.get(j));
                            }
                            Map<String, Object> paramsMap = new ArrayMap<>();
                            paramsMap.put("email", currEmail);
                            paramsMap.put("messageList", validMessageList);
                            downloadingMailCount = validMessageList.size();
                            vuMailHandler.sendMessage(vuMailHandler.obtainMessage(FETCH_MESSAGES_RESULT, 0, 0, paramsMap));
                        }
                    } else {

//                        Log.i(TAG, "run: 本地没有邮件");
                        int msgLen = messages.length;
                        int range = 0;
                        if (msgLen > AppConfig.WY_CFG.MAIL_NUM_PER_PAGE) {
                            range = msgLen - AppConfig.WY_CFG.MAIL_NUM_PER_PAGE;
                        }
//                        Log.i(TAG, "range----------------: " + range);
                        for (int i = msgLen - 1; i >= range; i--) {
                            MimeMessage mmsg = (MimeMessage) messages[i];
                            validMessageList.add(mmsg);
                            String uid = folder.getUID(mmsg);
                            Log.i(TAG, "即将下载第"+i+"封邮件的uid = "+uid);
                            localUidList.add(uid);
                        }
//                        Log.i(TAG, "validMessageList.size()-------: " + validMessageList.size());
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 0, 0, "正在为您下载最新的"+validMessageList.size()+"封邮件"));
                        Map<String, Object> paramsMap = new ArrayMap<>();
                        paramsMap.put("email", currEmail);
                        paramsMap.put("messageList", validMessageList);
                        downloadingMailCount = validMessageList.size();
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(FETCH_MESSAGES_RESULT, 0, 0, paramsMap));
                    }
                } catch (Exception e) {
//                } catch (GeneralSecurityException e) {
//                    e.printStackTrace();
                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "查询服务器邮件出现异常，请检查网络后重新尝试"));
//                } catch (NoSuchProviderException e) {
//                    e.printStackTrace();
//                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "查询服务器邮件出现异常"));
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "查询服务器邮件出现异常"));
                }
            }
        });
    }

    private void handleMimeMessage(List<MimeMessage> mimeMessageList, final String email) {
//        Log.i(TAG, "handleMimeMessage: mimeMessageList.size = " + mimeMessageList.size());
        final int pop3DownloadWay = getMailDownloadWay();
        int i;
        final int l = mimeMessageList.size();
        for (i = 0; i < l; i++) {
            final MimeMessage message = mimeMessageList.get(i);
            final int index = i;
            if (isExitedVU) return;//如果微邮已退出 则停止下载
            ThreadPool.exec(new Runnable() {
                @Override
                public void run() {
                    MailDetail md = new MailDetail();
                    try {
                        String uid = folder.getUID(message);
                        Log.i(TAG, System.currentTimeMillis()+"---begin handleMimeMessage: uid = "+uid);
//                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 0, 0, "正在下载邮件：" + MailUtil.getSubject(message)));

                        // 返回邮件信息
                        md.setUid(uid);
                        localUidList.add(uid);

                        Log.i(TAG, System.currentTimeMillis()+"---begin handleMimeMessage1: MessageID = "+message.getMessageID());
                        md.setEmail(email);
                        md.setMessageId(message.getMessageID());
                        md.setFrom(WeiYouUtil.getUserJSONStr(MailUtil.getFromMap(message)));
                        md.setTo(WeiYouUtil.getUserArrJSONStr(MailUtil.getRecipients(message, javax.mail.Message.RecipientType.TO)));
                        md.setCc(WeiYouUtil.getUserArrJSONStr(MailUtil.getRecipients(message, javax.mail.Message.RecipientType.CC)));
//                        md.setSc(WeiYouUtil.getUserArrJSONStr(MailUtil.getRecipients(message, javax.mail.Message.RecipientType.BCC)));

                        md.setSize(String.valueOf(message.getSize()));
                        md.setSubject(MailUtil.getSubject(message));
                        md.setIsMarked(false);
                        md.setIsRead(MailUtil.isNew(message));
                        md.setIsDeleted(false);
                        Date sentDate = message.getSentDate();
                        if (sentDate == null)
                            sentDate = new Date();

                        md.setSentDate(sentDate.getTime());
                        md.setCreateTime(sentDate);
                        md.setUpdateTime(sentDate);

                        String[] referencesArr = message.getHeader("References");
                        if (referencesArr != null) { //引用邮件的Message-ID
                            md.setReferences(referencesArr[0]);
                        }
                        // 连上转发器后 此处需要跟 目录数据对比，若有记录则把邮件存到相应目录
                        long mailDirId = AppConfig.WY_CFG.DIR_ID_INBOX;
                        md.setDirectoryId(mailDirId);

                        md.setEncrypted(false);
                        md.setSigned(false);
                        MailUtil.getMailEncType(message, md);
                        if(pop3DownloadWay == MAIL_DOWNLOAD_WAY_ALL) {
                            // 获取邮件内容
                            String filePath = FileUtil.getServerMailFilePath(email) + md.getUid() + ".xhw";

                            FileOutputStream fos = new FileOutputStream(filePath);

                            @SuppressWarnings("unchecked")
                            Enumeration<String> hdrLines = message.getNonMatchingHeaderLines(null);
                            LineOutputStream los = new LineOutputStream(fos);
                            while (hdrLines.hasMoreElements()) {
                                los.writeln(hdrLines.nextElement());
                            }

                            // The CRLF separator between header and content
                            los.writeln();
                            InputStream is = message.getRawInputStream();
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = is.read(buffer)) != -1)
                                fos.write(buffer, 0, len);
                            is.close();
                            fos.close();
                        }
//                        } else {
//                            MailUtil_sunyuan.getMailText(message, md);
//                        }

                        // 同步联系人
                        syncContact(md.getFrom());
                        syncContactArr(md.getTo());
                        syncContactArr(md.getCc());

                        md.setSendStatus(MailDetail.MAIL_TYPE_NO_STATUS);
                        ArrayList<MailAttachment> mal = new ArrayList<>();
                        if (!md.getEncrypted() && !md.getSigned()) {
                            Log.i(TAG, "普通邮件，保存附件去···" + md.getUid());
                            MailUtil.saveAttachments(message, email, mal);
                        }
//                        Log.i(TAG, "refreshCurrentView: mal.size() = " + mal.size());

                        DBUtil.saveMailDetail(md, mal);
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(DOWNLOAD_MAIL_RESULT, 1, index + 1));

                        //check mail directory...
                        if (currEmail.equals(md.getEmail()) && mailDirId == currDirId) {
                            mailListData.add(md);
                        }
                        Log.i(TAG, new Date(md.getSentDate()).toLocaleString() + " ---" + md.getSubject());

                        if (index == l - 1) {//邮件下载完后 解锁
                            isDownloadingMail = false;
                            vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "邮件下载完毕"));
                            folder.close(false);
                            store.close();
                            localUidList.clear();
                        }
//                        Log.i(TAG, System.currentTimeMillis()+"---end handleMimeMessage: "+uid);
                    } catch (Exception e) {
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT, 1, 0, "下载邮件时出现异常，请检查网络或重新尝试"));
                        isDownloadingMail = false;
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    public javax.mail.Message[] getInboxMessages() throws GeneralSecurityException, MessagingException {

        final String username = EncryptUtil.aesDecryptAD(currMailAccount.getAccount());
        final String password = EncryptUtil.aesDecryptAD(currMailAccount.getPassword());
        // 读取收件箱
        Properties properties = new Properties();

        // 创建属性对象
        properties.put("mail.mime.address.strict", "false");
        if ("pop3s".equals(currMailAccount.getProtocol())) {
            // props.setProperty("mail.pop3.ssl.enable", "true");
            // props.setProperty("mail.protocol.ssl.trust", "mail.inspur.com");
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustedHosts(new String[]{currMailAccount.getInComingHost()});
            properties.put("mail.pop3s.ssl.socketFactory", sf);
            // props.setProperty("mail.pop3s.port", "995");
        }

        // 获取会话对象
        Session pop3Session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        store = (POP3Store) pop3Session.getStore(currMailAccount.getProtocol());
        store.connect(currMailAccount.getInComingHost(), Integer.parseInt(currMailAccount.getInComingPort()), username, password);
        folder = (POP3Folder) store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);

        if (profile == null) {
            profile = new FetchProfile();
            profile.add(UIDFolder.FetchProfileItem.UID);
//            profile.add(FetchProfile.Item.ENVELOPE);
//            profile.add(FetchProfile.Item.SIZE);
        }

        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(CHECK_MAIL_RESULT,0,0,"服务器连接成功，正在检索邮件..."));

        javax.mail.Message[] messages = folder.getMessages();
        folder.fetch(messages, profile);
        return messages;
    }

    public boolean isExitedVU = false;

    /**
     * 同步联系人数组
     * @param JSONStr
     */
    public void syncContactArr(String JSONStr) {
        List<MailContacts> mcList = WeiYouUtil.parseContactListFromJSON(JSONStr);
        for (MailContacts mc : mcList) {
            DBUtil.syncContactToDB(mc);
        }
    }

    /**
     * 同步单个联系人
     *
     * @param JSONStr
     */
    public void syncContact(String JSONStr) {
        Log.i(TAG, "syncContact: .......... " + JSONStr);
        MailContacts mc = WeiYouUtil.parseContactFromJSON(JSONStr);
        DBUtil.syncContactToDB(mc);
    }

    private void refreshMailListView() {
//        Log.i(TAG, "refreshMailListView mailListData.size() = " + mailListData.size());
        //        邮件列表重新排序
        Collections.sort(mailListData, new Comparator<MailDetail>() {
            @Override
            public final int compare(MailDetail o, MailDetail t1) {
                return t1.getCreateTime().compareTo(o.getCreateTime());
            }
        });
        renderMailListView();
    }

//    // ！！！
//    private void updateMailListDataAndView(MailDetail md) {
////        if (currDirId == md.getDirectoryId()) {
////            int length = mailListData.size();
////            for (int i = 0; i < length; i++) {
////                if (mailListData.get(i).getId() == md.getId()) {
////                    mailListData.set(i, md);
////                }
////            }
////            refreshMailListView();
////        }
//    }
//
//    public void setMailReadStatus(MailDetail md) {
//        int index = mailListData.indexOf(md);
//        int index2 = exchangeMailList.indexOf(md);
//        if (!md.getIsRead()) md.setIsRead(true);
//        Log.i(TAG, "setMailReadStatus: updateMailDetail");
//        DBUtil.updateMailDetail(md, null);
//    }

    public void markMail(MailDetail md) {
        int index = mailListData.indexOf(md);
        int index2 = exchangeMailList.indexOf(md);
        boolean markStatus = !md.getIsMarked();
        md.setIsMarked(markStatus);
        if (currDirId == AppConfig.WY_CFG.DIR_ID_MARKED_MAIL && !markStatus)
            mailListData.remove(index);
        else
            mailListData.set(index, md);
        if (index2 != -1) exchangeMailList.set(index2, md);
        DBUtil.updateMailDetail(md, null);
    }

    public void deleteMail() {
//        mailDetailList.remove(currIndex);
//        showingMailIdList.remove(currIndex);
        Log.i(TAG, "deleteMail: currIndex = "+currIndex);
        Log.i(TAG, "deleteMail: mailDetailList.size() = "+mailDetailList);
        Log.i(TAG, "deleteMail: mailDetailList.get(currIndex) = "+mailDetailList.get(currIndex).getSubject());
        deleteMail(mailDetailList.get(currIndex));
    }

    public void deleteMail(MailDetail md) {
        mailListData.remove(md);
        exchangeMailList.remove(md);
        if (currDirId == AppConfig.WY_CFG.DIR_ID_DELETED_MAIL) {
            md.setIsDeleted(true);
            DBUtil.deleteOneMail(md.getEmail(), md.getId());
        } else {
            md.setDirectoryId(AppConfig.WY_CFG.DIR_ID_DELETED_MAIL);
            DBUtil.updateMailDetail(md, null);
            if (currDirIsOutbox()) {
                outBoxMailListData.remove(md);
                mailSynchronizer.removeMailTask(md.getEmail(),md.getSentDate());
            }
        }
    }
    private String exchangeEmailAddress;
    private String exchangeName;

    public String getExchangeEmailAddress() {
        return exchangeEmailAddress;
    }
    public String getExchangeName() {
        return exchangeName;
    }

    public void showExchangeMailList(MailDetail md) {
        long dirId = md.getDirectoryId();
        if (dirId != AppConfig.WY_CFG.DIR_ID_DRAFTBOX && dirId != AppConfig.WY_CFG.DIR_ID_SENT_MAIL && dirId != AppConfig.WY_CFG.DIR_ID_OUTBOX) {
            try {
                JSONObject fromjo=  new JSONObject(md.getFrom());
                exchangeEmailAddress = fromjo.getString("email");
                exchangeName = fromjo.getString("name");
                String _targetEmail = "\"" + exchangeEmailAddress + "\"";
                String _selfEmail = "\"" + currEmail + "\"";
                for (MailDetail _md : mailListData) {
                    //该联系人发的邮件 或是 自己发给该联系人的邮件
                    if (_md.getFrom().contains(_targetEmail) || (_md.getFrom().contains(_selfEmail) && (_md.getTo().contains(_targetEmail) || (_md.getCc() != null && _md.getCc().contains(exchangeEmailAddress))))) {
                        exchangeMailList.add(_md);
                        Log.i(TAG, "往来邮件——>" + _md.getSubject());
                    }
                }
                vuActivityOperation.get().openExchangeListFragment();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<MailDetail> getExchangeMailList() {
        return exchangeMailList;
    }

    private long timeSeperator = 0;
    private MailDetail prevMD = null;

    public void mailClickHandler(MailDetail md) {
        long now = new Date().getTime();
        if (prevMD != null) {
            if (prevMD.equals(md)) return;
        }
        if (now - timeSeperator < 1000) return;

        currMail = md;
        if (currDirId != AppConfig.WY_CFG.DIR_ID_DRAFTBOX) {
            showingMailIdList.clear();
            Log.i(TAG, "mailClickHandler: exchangeMailList.size() > 0 ? "+(exchangeMailList.size() > 0));
            if (exchangeMailList.size() > 0)
                for (MailDetail _md : exchangeMailList) {
                    showingMailIdList.add(_md.getId());
                }
            else
                for (MailDetail _md : mailListData) {
                    showingMailIdList.add(_md.getId());
                }
            vuActivityOperation.get().showMailDetail();
        } else {
            vuActivityOperation.get().reEditDraftMail();
        }
        timeSeperator = now;
    }

    /******************* 写邮件 ********************/

    private static final String IMG_PATH = "image_path";
    private static final int SHOW_COMPRESS_BITMAP = 0x03;
    private static final String COMPRESS_BITMAP = "compress_bitmap";
    private int quoteType;
    private MailDetail mailDetailCache;//邮件详情缓存(保存邮件到本地数据库的时候用到）
    private String currInputText;
    private String quoteMailContent = "";
    private String draftOrignalSubject = "";
    private String draftOrignalContent = "";
    private ArrayList<UserInfoBean> toList = new ArrayList<>();
    private ArrayList<UserInfoBean> ccList = new ArrayList<>();
    //    ArrayList<UserInfoBean> scList;
    private ArrayList<MailAttachment> attachmentList = new ArrayList<>();
    private int currContactType = 0;
    private ArrayList<UserInfoBean> contactSearchResult = new ArrayList<>();
    private String references = "";
    private List<String> noPbkRcptAL = new ArrayList<>();
    private boolean showSavePopWindowOnBackPress = false;
    private boolean canSendNoSubjectMail = false;
    private String cameraPhotoPath;

    private MailTask draftMailTask = null;

    private void resetWriteMailData() {
        mailDetailCache = null;
//        quoteMailContent = "";
        draftOrignalSubject = "";
        draftOrignalContent = "<br><br><br><br><br><br><p>--- 发自我的微邮Android客户端</p>";
        noPbkRcptAL.clear();
        attachmentList.clear();
        toList.clear();
        ccList.clear();
        contactSearchResult.clear();
        currContactType = 0;
        references = "";
        showSavePopWindowOnBackPress = false;
        canSendNoSubjectMail = false;
        sendMailButtonEnabled = true;
    }

    /**
     * 初始化数据及写邮件界面
     */
    public void initWriteMailData() {
        resetWriteMailData();
        boolean _saftyItemEnabled = currUsingCA != null;

        if (quoteType != WeiYouMainActivity.QUOTE_TYPE_REEDIT) {
            if (quoteType != WeiYouMainActivity.QUOTE_TYPE_NO_QUOTE) {
                MailDetail quoteMail = currMail;
                String content = quoteMail.getContent();
                String quoteRef = quoteMail.getReferences();
                references = quoteMail.getMessageId() + (quoteRef != null ? ("," + quoteRef) : "");
                if (quoteMailContent != null) {
//                    Log.i(TAG, "initWriteMailData: quoteMailContent = " + quoteMailContent);
//                    String mailFilePath = FileUtil.getCurrMailFilePath(currMail.getEmail())+ CAEncryptUtils.createSHA1(currMail.getUid())+".xhw";
                    if (quoteMailContent.startsWith(FileUtil.getMailCachePath())) {
                        Log.i(TAG, quoteMail.getUid() + "---initData: startsWith--->" + quoteMailContent.startsWith(FileUtil.getMailCachePath()));
                        try {
                            FileInputStream fin = new FileInputStream(quoteMailContent);
                            int length = fin.available();
                            byte[] buffer = new byte[length];
                            fin.read(buffer);
                            content = new String(buffer, "utf-8");
                            fin.close();
                        } catch (Exception e) {
                            Log.e(TAG, "initData: read quoteMail file error");
                            e.printStackTrace();
                            content = "引用邮件显示失败";
                        }
                    } else {
                        content = quoteMailContent;
                    }
                } else {//有用么？
                    if (!quoteMail.getEncrypted()) {
                        try {
                            content = new String(Base64Utils.decode(content), "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        content = "本邮件是微软加密邮件，请从outlook客户端打开查看";
                    }
                }
                try {
                    JSONArray toArr = new JSONArray(quoteMail.getTo());
                    int toArrLength = toArr.length();
                    String originalToStr = "", originalCcStr = "";
//                    发件人对象
                    UserInfoBean fromU = WeiYouUtil.parseJSONStrToUserInfoBean(quoteMail.getFrom());
                    ArrayList<UserInfoBean> originalToList = new ArrayList();
                    String myEmail = currMailAccount.getEmail();
                    Log.i(TAG, "initWriteMailData: to---"+toArr);
                    //遍历收件人
                    for (int i = 0; i < toArrLength; i++) {
                        JSONObject jo = (JSONObject) toArr.get(i);
                        UserInfoBean u = WeiYouUtil.parseJSONObjToUserInfoBean(jo);
                        originalToStr += "," + u.name;
                        if (!myEmail.equals(u.email)) {
                            originalToList.add(u);
                        }
                    }

                    if (quoteType == WeiYouMainActivity.QUOTE_TYPE_REPLY || quoteType == WeiYouMainActivity.QUOTE_TYPE_REPLY_ALL) {
                        draftOrignalSubject = "回复：" + quoteMail.getSubject();
                        if (quoteType == WeiYouMainActivity.QUOTE_TYPE_REPLY_ALL) {
                            boolean isExists = false;
//                            判断要回复的邮件的发件人是否在 收件人中已包含，若没有，则添加进去
                            for (UserInfoBean uib : originalToList) {
                                if (uib.email.equals(fromU.email)) {
                                    isExists = true;
                                }
                            }
                            if (!isExists) {
                                toList.add(fromU);
                            }
                            toList.addAll(originalToList);

                            String ccStr = quoteMail.getCc();
                            if (ccStr != null && ccStr.length() > 0) {
                                JSONArray ccArr = new JSONArray(ccStr);
                                for (int i = 0; i < ccArr.length(); i++) {
                                    JSONObject jo = (JSONObject) ccArr.get(i);
                                    UserInfoBean ccu = WeiYouUtil.parseJSONObjToUserInfoBean(jo);
                                    originalCcStr += "," + ccu.name;
                                    if (!myEmail.equals(ccu.email)) {
                                        boolean isContain = false;
                                        for (UserInfoBean uib : toList) {
                                            if (uib.email.equals(ccu.email)) {
                                                isContain = true;
                                            }
                                        }
                                        if (!isContain) {
                                            ccList.add(ccu);
                                        }
                                    }
                                }
                            }
                        } else {
                            toList.add(fromU);
                        }
                    } else if (quoteType == WeiYouMainActivity.QUOTE_TYPE_FORWARD) {//若是转发邮件 则把附件列表显示出来
                        draftOrignalSubject = "转发：" + quoteMail.getSubject();
                        attachmentList.addAll(currAttachmentList);
                        Log.i(TAG, "initWriteMailData: attachmentList.size() = " + attachmentList.size());
                    }
                    draftOrignalContent += "<div style='PADDING-LEFT: 10px; '><div style=\"PADDING-RIGHT: 8px; PADDING-LEFT: 8px; FONT-SIZE: 12px;FONT-FAMILY:tahoma;COLOR:#000000; BACKGROUND: #efefef; PADDING-BOTTOM: 8px; PADDING-TOP: 8px; white-space: normal\">" +
                            "<div><b>发件人：</b>&nbsp;" + fromU.name + "</div>" +
                            "<div><b>发送时间：</b>&nbsp;<span>" + quoteMail.getCreateTime().toLocaleString() + "</span></div>" +
                            "<div><b>收件人：</b>&nbsp;" + originalToStr.substring(1, originalToStr.length()) + "</div>";
                    if (originalCcStr.length() > 0) {
                        draftOrignalContent += "<div><b>抄送:</b>&nbsp;" + originalCcStr.substring(1, originalCcStr.length()) + "</div>";
                    }
                    draftOrignalContent += "<div><b>主题：</b>&nbsp;" + quoteMail.getSubject() + "</div></div><br>";
                    if (MailUtil.getContentTextType().equals("text/plain")) {
                        draftOrignalContent += "<pre>" + content + "</pre></div>";
                    } else {
                        draftOrignalContent += content + "</div>";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{//普通邮件
                if(writeMailOperation.get()!=null) {
                    MailDetail quoteMail = writeMailOperation.get().getParamMailDetail();
                    if (quoteMail != null) {
                        if (!TextUtils.isEmpty(quoteMail.getTo())) {
                            Log.i(TAG, "initWriteMailData: currMail.getTo() = " + quoteMail.getTo());
                            ArrayList<UserInfoBean> _toList = WeiYouUtil.parseJSONStrToUserInfoBeanList(quoteMail.getTo());
                            boolean toListAvailable = true;
                            for (UserInfoBean uib : _toList) {
                                if (!WeiYouUtil.isEmail(uib.email)) {
                                    Log.e(TAG, "initWriteMailData: 指定的收件人email格式不正确");
                                    toListAvailable = false;
                                    break;
                                }
                            }
                            if (toListAvailable) toList.addAll(_toList);
                            else _toList.clear();
                        }
                    }
                    List<MailAttachment> paramAttachments = writeMailOperation.get().getParamAttachments();
                    if (paramAttachments != null) attachmentList.addAll(paramAttachments);
                }
            }
        } else {//草稿箱邮件重新编辑
            MailDetail quoteMail = currMail;
            references = quoteMail.getReferences();
            String toStr = quoteMail.getTo();
            String ccStr = quoteMail.getCc();
//            String scStr = quoteMail.getSc();
            try {
                if (toStr.length() > 0) {
                    JSONArray toArr = new JSONArray(toStr);
                    int toArrLength = toArr.length();
                    for (int i = 0; i < toArrLength; i++) {
                        JSONObject jo = (JSONObject) toArr.get(i);
                        UserInfoBean u = WeiYouUtil.parseJSONObjToUserInfoBean(jo);
                        toList.add(u);
                    }
                }
                if (ccStr != null && ccStr.length() > 0) {
                    JSONArray ccArr = new JSONArray(ccStr);
                    int ccArrLength = ccArr.length();
                    for (int i = 0; i < ccArrLength; i++) {
                        JSONObject jo = (JSONObject) ccArr.get(i);
                        UserInfoBean u = WeiYouUtil.parseJSONObjToUserInfoBean(jo);
                        ccList.add(u);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            把草稿的附件列表显示出来
            Log.i(TAG, "initWriteMailData: quoteMail.getAttachments().size() = " + quoteMail.getAttachments().size());
            attachmentList.addAll(quoteMail.getAttachments());

            try {
                Log.i(TAG, "initWriteMailData: quoteMail.getContent()= " + quoteMail.getContent());
                draftOrignalContent = new String(Base64Utils.decode(quoteMail.getContent()), "UTF-8");
                draftOrignalSubject = quoteMail.getSubject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        boolean encryptMail = _saftyItemEnabled &&CAEncryptUtils.getDefaultEncrypt(currEmail);
        boolean signMail = _saftyItemEnabled && CAEncryptUtils.getDefaultSign(currEmail);
        writeMailOperation.get().fillDraftData(draftOrignalSubject, toList, ccList, draftOrignalContent, encryptMail, signMail, _saftyItemEnabled);

    }

    public void setCurrContactList(int type) {
        this.currContactType = type;
    }

    public void removeContactListItem(int index, int type) {
        showSavePopWindowOnBackPress = true;
        if (type == 1) toList.remove(index);
        else if (type == 2) ccList.remove(index);
    }

    public void addToContactList(UserInfoBean userInfoBean, int type) {
        boolean isExists = false;
        ArrayList<UserInfoBean> uibList = null;
        if(type == 0) type = currContactType;
        if(type == 1){uibList = toList;}
        else if(type == 2){uibList = ccList;}
        for (UserInfoBean uib : uibList) {
            if (uib.email.equals(userInfoBean.email)) {
                isExists = true;
            }
        }
        if (!isExists) {
            uibList.add(userInfoBean);
            writeMailOperation.get().generateContactTV(userInfoBean, type);
            showSavePopWindowOnBackPress = true;
        } else {
//            toast("该联系人已存在");
        }
    }

    public void addToContactList(int position, int type) {
        UserInfoBean userInfoBean = contactSearchResult.get(position);
        addToContactList(userInfoBean, type);
    }

    public void handleSearchingText(String inputText) {
        this.currInputText = inputText;
        if (inputText.length() > 0) {
            if (inputText.endsWith(" ")) {
                Log.i(TAG, "刚按了下空格键: " + inputText);
                inputText = inputText.trim();
                // 判断email是否合法
                handleInputText(inputText, 0);
                writeMailOperation.get().emptyInputText();
            } else {
                //            Log.i(TAG, "searchNameTextWatcher onTextChanged: " + inputText);
                DBUtil.sarchContactsByStr(inputText);
            }
        }
    }

    public void handleInputText(String inputText, int type) {
        // 判断email是否合法
        if (checkEmailAddress(inputText)) {
            Log.i(TAG, "手动输入 是邮箱地址");
//                    生成一个USER对象
            UserInfoBean _u = new UserInfoBean();
            _u.email = inputText;
            _u.name = inputText;
            if (!inputText.endsWith(AppConfig.EMAIL_SUFFIX))
                _u.id = inputText;
            else
                _u.id = inputText.split("@")[0];
            addToContactList(_u, type);//添加到联系人列表并在视图中显示
        }
    }

    public void mergeContactsSearchResult(List<MailContacts> mailContactsList) {
        contactSearchResult.clear();
        if (mailContactsList.size() > 0) {
            for (MailContacts mc : mailContactsList) {
//            boolean hasAlready = false;
//            for (UserInfoBean uib : contactSearchResult) {
//                if(mc.getEmail().equals(uib.email)){
//                    hasAlready = true;
//                }
//            }
//            if(!hasAlready){
//                Log.i(TAG, "mergeContactsSearchResult: ......"+mc.toString());
                contactSearchResult.add(new UserInfoBean(mc));
//            }
            }
            writeMailOperation.get().refreshSearchResultListView(contactSearchResult);
        } else {
            DBUtil.searchPerson(currInputText);//从服务器搜索联系人
        }
    }

    public void handleEncryptItemClick(boolean mailEncrypt) {
        if (quoteType != WeiYouMainActivity.QUOTE_TYPE_NO_QUOTE)
            showSavePopWindowOnBackPress = true;
//        CAEncryptUtils.setDefaultEncrypt(currEmail, mailEncrypt);
    }

    public void handleSignItemClick(boolean mailSign) {
        if (quoteType != WeiYouMainActivity.QUOTE_TYPE_NO_QUOTE)
            showSavePopWindowOnBackPress = true;
//        CAEncryptUtils.setDefaultSign(currEmail, mailSign);
    }

    public String generateOrignalContent() {
        String contentHtml = writeMailOperation.get().getDraftHtmlContent();
        return contentHtml == null ? "" : "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /></head><body>" + contentHtml + "</body></html>";
    }

    public String getEncodedContent(String mingwen) throws Exception {
        if (mingwen == null) mingwen = "";
        else
            mingwen = Base64Utils.encode(mingwen.getBytes("UTF-8"));
        return mingwen;
    }

    private boolean checkEmailAddress(String email) {//邮箱判断正则表达式
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(email);
        return mc.matches();
    }

    public void onCancelSendMail() {
        String html = writeMailOperation.get().getDraftHtmlContent();
        String newSubject = writeMailOperation.get().getSubjectText();
        if (!draftOrignalContent.equals(html) || !draftOrignalSubject.equals(newSubject)
                || showSavePopWindowOnBackPress) {
            writeMailOperation.get().showSaveDraftPopWindow();
        } else {
            Log.i(TAG, "onCancelSendMail: closeWriteMailFragment");
            finishWritingMail();
        }
    }

    private void showWriteMailToast(String msg){
        if(writeMailOperation.get()!=null)writeMailOperation.get().toast(msg);
    }

    private boolean checkMailDraft() {
        if (writeMailOperation.get().getSubjectText().length() == 0 && !canSendNoSubjectMail) {
            writeMailOperation.get().showRemindSubjectDialog();
            return false;
        }
        if (toList.size() == 0) {
            showWriteMailToast("请选择收件人");
            return false;
        }
        return true;
    }

    private void sentOrSaveMailError(String msg) {
        showWriteMailToast(msg);
        sendMailButtonEnabled = true;
    }

    /**
     * 保存草稿按钮
     */
    public void saveMailDraft() {
        String contentHtml = writeMailOperation.get().getDraftHtmlContent();
        generateDraftObject();
        mailDetailCache.setSendStatus(MailDetail.MAIL_TYPE_NO_STATUS);
        try {
            mailDetailCache.setContent(getEncodedContent(contentHtml));
        } catch (Exception e) {
            e.printStackTrace();
            sentOrSaveMailError("加密邮件正文时出现异常，保存草稿失败...");
            return;
        }
        Log.i(TAG, "saveMailDraft: quoteType: " + quoteType);
        if (quoteType == WeiYouMainActivity.QUOTE_TYPE_REEDIT) {
            DBUtil.updateMailDetail(mailDetailCache, attachmentList);
        } else {
            mailDetailCache.setDirectoryId(AppConfig.WY_CFG.DIR_ID_DRAFTBOX);
            DBUtil.saveMailDetail(mailDetailCache, attachmentList);
            if (AppConfig.WY_CFG.DIR_ID_DRAFTBOX == currDirId) {
                mailListData.add(mailDetailCache);
            }
        }
        finishWritingMail();
    }

    /**
     * 写邮件时点击“发送”按钮后触发
     */
    private boolean sendMailButtonEnabled = true;

    public void onSendMailButtonClick() {
        Log.i(TAG, "onSendMailButtonClick: sendMailButtonEnabled = " + sendMailButtonEnabled);
        if (sendMailButtonEnabled) {
            sendMailButtonEnabled = false;
            if (checkMailDraft()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String contentHtml = generateOrignalContent();
                            generateDraftObject();
                            mailDetailCache.setSendStatus(MailDetail.MAIL_TYPE_WILL_SEND);
                            mailDetailCache.setContent(getEncodedContent(contentHtml));
                            boolean generateRes = generateMailTask(contentHtml);
                            if (generateRes) {
                                vuMailHandler.sendMessage(vuMailHandler.obtainMessage(HANDLE_DRAFT_MAIL_TASK,1,0));
                            }else {
                                Log.i(TAG, "run: generateMailTask failed...");
                                sendMailButtonEnabled = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            vuMailHandler.sendMessage(vuMailHandler.obtainMessage(HANDLE_DRAFT_MAIL_TASK,0,0));
                            return;
                        }
                    }
                }).start();
            } else sendMailButtonEnabled = true;
        }
    }

    private void generateDraftObject() {
        if (quoteType == WeiYouMainActivity.QUOTE_TYPE_REEDIT) {
            mailDetailCache = currMail;
        } else {
            mailDetailCache = new MailDetail();
        }
        writeMailOperation.get().prepareToSendMail(mailDetailCache);
        mailDetailCache.setEmail(currEmail);
        Date now = new Date();
        mailDetailCache.setCreateTime(now);
        mailDetailCache.setUpdateTime(now);
        mailDetailCache.setSentDate(now.getTime());
        mailDetailCache.setReferences(references != null ? references : "");
        UserInfoBean fromu = new UserInfoBean();
        fromu.id = currMailAccount.getAccount();
        fromu.name = currMailAccount.getNickName();
        fromu.email = currEmail;
        mailDetailCache.setFrom(WeiYouUtil.getUserJSONStr(fromu));

        if (toList.size() > 0) {
            mailDetailCache.setTo(WeiYouUtil.getUserArrJSONStr(toList));
        } else mailDetailCache.setTo("");
        if (ccList.size() > 0) {
            mailDetailCache.setCc(WeiYouUtil.getUserArrJSONStr(ccList));
        } else mailDetailCache.setCc("");

        mailDetailCache.setIsDeleted(false);
        mailDetailCache.setIsRead(true);
        mailDetailCache.setIsMarked(false);
    }

    private boolean generateMailTask(String contentHtml) throws IOException, MessagingException, JSONException {
        boolean res = true;
        Session smtpSession = Session.getInstance(new Properties());
        SMTPMessage smtpMessage = new SMTPMessage(smtpSession);
        Log.i(TAG, "generateMailTask: 1");
        draftMailTask = new MailTask();
        MailUtil.fillContentToMessage(smtpMessage, contentHtml, attachmentList);

        Log.i(TAG, "generateMailTask: 2");
        // 把邮件对象写入一个本地缓存文件
        String mailFilePath = FileUtil.getLocalMailFilePath(currEmail) + "/" + new Date().getTime() + ".tempEmail";
        File txt = new File(mailFilePath);
        if (!txt.exists()) {
            txt.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(txt);
        smtpMessage.writeTo(fos);

        Log.i(TAG, "generateMailTask: 3");
        if (mailDetailCache.getSigned()) {
            Log.i(TAG, "generateMailTask: 3.1");
            if (currUsingCA == null) {
                showWriteMailToast("当前的邮箱账号未安装任何证书，无法发送签名邮件，您可以在微邮账号设置界面安装数字证书");
                return false;
            }
            Log.i(TAG, "generateMailTask: 3.2");
            String mailFilePathTemp = mailFilePath + "_signed";
            File tempFile = new File(mailFilePathTemp);
            tempFile.createNewFile();
            int signMailResult = dm.signedMail(mailFilePath, currUsingCA.getPassword(), mailFilePathTemp, currUsingCA.getFilepath());
            if (signMailResult == 0) {
                new File(mailFilePath).delete();
                mailFilePath = mailFilePathTemp;//把邮件文件的路径设置成 签名后的邮件文件的路径
            } else {
                showWriteMailToast("签名邮件时出现错误,请确定数字证书是否正确");
                return false;
            }
            Log.i(TAG, "generateMailTask: 3.3 signed res: " + signMailResult);
        }
        draftMailTask.setMessageFilePath(mailFilePath);
        draftMailTask.setEmail(mailDetailCache.getEmail());
        draftMailTask.setReferences(mailDetailCache.getReferences());
        draftMailTask.setSubject(mailDetailCache.getSubject());
        draftMailTask.setCreateTime(mailDetailCache.getSentDate());
        draftMailTask.setNickName(currMailAccount.getNickName());
        draftMailTask.setTo(mailDetailCache.getTo());
        draftMailTask.setCc(mailDetailCache.getCc());
        draftMailTask.setEncrypted(mailDetailCache.getEncrypted());
        draftMailTask.setAccount(currMailAccount.getAccount());
        draftMailTask.setPassword(currMailAccount.getPassword());
        draftMailTask.setOutgoingHost(currMailAccount.getOutGoingHost());
        draftMailTask.setOutgoingPort(currMailAccount.getOutGoingPort());
        draftMailTask.setOutgoingSSL(currMailAccount.getOutGoingSSL());
        draftMailTask.setOutgoingTLS(currMailAccount.getOutGoingTLS());

        Log.i(TAG, "generateMailTask: 4");
        ArrayList<UserInfoBean> allRcpts = new ArrayList<>();
        JSONArray ja = new JSONArray();
        allRcpts.addAll(toList);
        for(UserInfoBean _uib:ccList){
            boolean exists = false;
            for(UserInfoBean _uib2 : allRcpts){
                if(_uib.email.equals(_uib2.email)){
                    exists = true;
                    break;
                }
            }
            if(!exists) allRcpts.add(_uib);
        }
        for (UserInfoBean _uib : allRcpts) {
            String jsonStr = "{\"address\":\"" + _uib.email + "\",\"personal\":\"" + _uib.name + "\"}";
            JSONObject jo = new JSONObject(jsonStr);
            ja.put(jo);
        }
//        Log.i(TAG, "generateMailTask: 5");
        draftMailTask.setMailRcpts(ja.toString());
        draftMailTask.setRcptCount((long) ja.length());
//        Log.i(TAG, "generateMailTask: 6");
        return res;
    }

    public void saveMailToOutbox() {
        mailDetailCache.setDirectoryId(AppConfig.WY_CFG.DIR_ID_OUTBOX);
        if (quoteType == WeiYouMainActivity.QUOTE_TYPE_REEDIT) {
            DBUtil.updateMailDetail(mailDetailCache, attachmentList);
            boolean delRes = mailListData.remove(currMail);
            Log.i(TAG, "saveMailToOutbox: delRes = " + delRes);
            refreshMailListView();
        }
        outBoxMailListData.add(mailDetailCache);
        DBUtil.saveMailDetail(mailDetailCache, attachmentList);
        sendMailButtonEnabled = true;
        finishWritingMail();
        Log.i(TAG, "after saveMailToOutbox: mailDetailCache.getSentDate() = " + mailDetailCache.getSentDate());
    }

    /**
     * 完成写邮件，清理掉一次性的数据，关闭写邮件界面
     */
    private void finishWritingMail(){
//        quoteMailContent = "";
//        attachmentList.clear();
        writeMailOperation.get().closeWriteMailFragment();
    }

    public void sendWeiYouMail() {
        Log.i(TAG, "enter sendWeiYouMail method..."+toList.get(0));
//        sendLoadingMessage("正在发送邮件");
        if (draftMailTask.getEncrypted()) {//,只加密
            if (currUsingCA != null) {
                List<UserInfoBean> receives = new ArrayList<>();
                receives.addAll(toList);
//                receives.addAll(ccList);
                for(UserInfoBean _uib:ccList){
                    boolean exists = false;
                    for(UserInfoBean _uib2 : receives){
                        if(_uib.email.equals(_uib2.email)){
                            exists = true;
                            break;
                        }
                    }
                    if(!exists) receives.add(_uib);
                }
                String userids = "";
                for (UserInfoBean u : receives) {
                    userids += u.id + "|";
                }
                final String finalUids = userids.substring(0, userids.length() - 1);
                Log.i(TAG, "sendWeiYouMail: 准备下载公钥...");
                showProgressDialog("正在发送...");
                OkHttpClientManager.HttpOperationCallback hock = new OkHttpClientManager.HttpOperationCallback() {
                    @Override
                    public void onSuccess(String result) {
                        dismissProgressDialog();
                        Log.i(TAG, "sendWeiYouMail onSuccess: 下载完了公钥，长度为："+result.length());
                        try {
                            JSONArray ja = new JSONArray(result);
                            int pbkArrLen = ja.length();
                            String[] useridArr = finalUids.split("\\|");//这里的|之前必须带\\ 否则 结果不正确
                            for (String userid : useridArr) {
                                noPbkRcptAL.add(userid);
                            }
                            String tempPbk = null;
                            if (pbkArrLen > 0) {
                                for (int ii = 0; ii < pbkArrLen; ii++) {
                                    JSONObject jobj = (JSONObject) ja.opt(ii);
                                    if (tempPbk == null) tempPbk = jobj.optString("RawData");
                                    String uid = jobj.optString("UserId");
                                    if (noPbkRcptAL.contains(uid)) {
                                        noPbkRcptAL.remove(uid);
                                    }
                                }
                            }
                            if (noPbkRcptAL.size() > 0) {
                                for (String _uid : noPbkRcptAL) {
                                    JSONObject jobj = new JSONObject();
                                    jobj.put("RawData", tempPbk);
                                    jobj.put("UserId", _uid);
                                    ja.put(jobj);
                                }
                                String[] noPbkUserIdArr = new String[noPbkRcptAL.size()];
                                noPbkRcptAL.toArray(noPbkUserIdArr);
                                String noPbkUserIdStr = Arrays.toString(noPbkUserIdArr);
                                draftMailTask.setPublicKeys(ja.toString());
                                sendPbkErrorMessage(noPbkUserIdStr.substring(1, noPbkUserIdStr.length() - 1), tempPbk == null ? 0 : 1);//弹窗去！
                            } else {
                                saveMailToOutbox();
                                draftMailTask.setPublicKeys(ja.toString());
                                mailSynchronizer.addMailTask(draftMailTask);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            sendFailMessage("解析联系人的公钥出错，请检查网络连接后重新点击“发送”按钮");
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        dismissProgressDialog();
                        sendFailMessage("收件人公钥下载失败，请检查网络连接后重新点击“发送”按钮");
                    }
                };
                // 下载公钥
                CAEncryptUtils.getPublicKey(finalUids, selfUser.id, selfUser.passWord, hock);
            } else {
                sendFailMessage("当前的邮箱账号未安装任何证书，无法发送加密邮件，您可以在微邮账号设置界面安装数字证书");
            }
        } else { //非加密邮件
            Log.i(TAG, "sendWeiYouMail: 普通邮件。。。");
            saveMailToOutbox();
            mailSynchronizer.addMailTask(draftMailTask);
        }
    }

    void sendPbkErrorMessage(String msg, int showContinueSendButton) {
        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(SEND_MAIL_RESULT, SEND_MAIL_PBK_ERROR, showContinueSendButton, msg));
    }

    void sendFailMessage(String msg) {
        sendMailButtonEnabled = true;
        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(SEND_MAIL_RESULT, SEND_MAIL_FAILED, 0, msg));
    }

    public boolean isCertInstalled() {
        return currUsingCA != null;
    }

    public void continueToSendMail() {
        saveMailToOutbox();
        mailSynchronizer.addMailTask(draftMailTask);
    }

    public void sendNormalMail() {
        mailDetailCache.setEncrypted(false);
        saveMailToOutbox();
        draftMailTask.setEncrypted(false);
        mailSynchronizer.addMailTask(draftMailTask);
    }

    public void updateSentProgress(MailTask currMailTask) {
//        发送进度
        int sentPercentage = Math.round(100 * currMailTask.getSentRcptCount() / currMailTask.getRcptCount());
        Log.i(TAG, "updateSentProgress: currMailTask.getSentRcptCount() = " + currMailTask.getSentRcptCount());
        Log.i(TAG, "updateSentProgress: currMailTask.getRcptCount() = " + currMailTask.getRcptCount());
        String _email = currMailTask.getEmail();
        File temp;
        long _createTime = currMailTask.getCreateTime();
//        更新邮件发送进度
        DBUtil.updateMailSentPercentage(_email, _createTime, sentPercentage);
        if (sentPercentage == 100) {
//            如果邮件发送完毕，则把相应邮件转移到“已发送邮件”目录
            DBUtil.changeMailDirectory(_email, _createTime, AppConfig.WY_CFG.DIR_ID_SENT_MAIL);
            String fp = currMailTask.getMessageFilePath();
            temp = new File(fp);
            if (temp.exists()) temp.delete();
            if (currMailTask.getEncrypted()) {
                temp = new File(fp + "_encrypted");
                if (temp.exists()) temp.delete();
            }
        }
        if (currEmail.equals(_email)) { //处理视图的数据
            Log.i(TAG, "updateSentProgress: .............sentPercentage..........." + sentPercentage);
            Log.i(TAG, "updateSentProgress: currMailTask.getCreateTime() = " + currMailTask.getCreateTime());
            int targetIndex = -1;
            for (MailDetail _md : outBoxMailListData) {
                Log.i(TAG, "updateSentProgress: _md.getSentDate() = " + _md.getSentDate());
                if (_md.getSentDate().equals(currMailTask.getCreateTime())) {
                    targetIndex = outBoxMailListData.indexOf(_md);
                    Log.i(TAG, "updateSentProgress: targetIndex = " + targetIndex);
                    _md.setSentPercentage(sentPercentage);
                }
            }
            Log.i(TAG, "updateSentProgress: targetIndex = " + targetIndex);
            Log.i(TAG, "updateSentProgress: outBoxMailListData.size() = " + outBoxMailListData.size());
            if (targetIndex != -1 && targetIndex < outBoxMailListData.size()) {
//            如果当前目录是发件箱 则刷新视图
                if (currDirIsOutbox()) {
                    mailListData.clear();
                    mailListData.addAll(outBoxMailListData);
                    Log.i(TAG, "updateSentProgress: 去刷新视图......");
                    renderMailListView();
                }
                final int finalTargetIndex = targetIndex;
                if (sentPercentage == 100) {
                    Log.i(TAG, "run: send mail success");
                    //删除发件箱邮件列表中已经发送成功的这封邮件
                    outBoxMailListData.remove(finalTargetIndex);
                    if (currDirIsOutbox()) {
                        mailListData.clear();
                        mailListData.addAll(outBoxMailListData);
                        renderMailListView();
                    }
                }
            }

        }
    }

    @Override
    public void onSendSuccess(MailTask currMailTask) {
        updateSentProgress(currMailTask);
    }

    @Override
    public void onBeforeSend(MailTask mailTask) {
        String _email = mailTask.getEmail();
        DBUtil.updateMailSendStatus(_email, mailTask.getCreateTime(), MailDetail.MAIL_TYPE_IS_SENDING);
        if (currEmail.equals(_email)) { //处理视图的数据
            for (MailDetail _md : outBoxMailListData) {
                Log.i(TAG, "onBeforeSend: _md.getSentDate() = " + _md.getSentDate());
                if (_md.getSentDate().equals(mailTask.getCreateTime())) {
                    int targetIndex = outBoxMailListData.indexOf(_md);
                    Log.i(TAG, "onSendFailed: targetIndex = " + targetIndex);
                    _md.setSendStatus(MailDetail.MAIL_TYPE_IS_SENDING);
                    if(mailListOperation.get()!= null) mailListOperation.get().refreshMailItem(targetIndex);
                }
            }
        }
    }

    @Override
    public void onSendFailed(MailTask mTask) {
        String _email = mTask.getEmail();
        DBUtil.updateMailSendStatus(_email, mTask.getCreateTime(), MailDetail.MAIL_TYPE_SEND_FAILED);
        if (currEmail.equals(_email)) { //处理视图的数据
            for (MailDetail _md : outBoxMailListData) {
                Log.i(TAG, "onSendFailed: _md.getSentDate() = " + _md.getSentDate());
                if (_md.getSentDate().equals(mTask.getCreateTime())) {
                    int targetIndex = outBoxMailListData.indexOf(_md);
                    Log.i(TAG, "onSendFailed: targetIndex = " + targetIndex);
                    _md.setSendStatus(MailDetail.MAIL_TYPE_SEND_FAILED);
                    if(mailListOperation.get()!= null) mailListOperation.get().refreshMailItem(targetIndex);
                }
            }
        }
    }

    @Override
    public void onSendPartiallySuccess(MailTask mTask) {
        updateSentProgress(mTask);
    }

    private static Bitmap readBitmap(String imgPath) {
        try {
            return BitmapFactory.decodeFile(imgPath);
        } catch (Exception e) {
            return null;
        }

    }

    public String getCameraPhotoPath() {
        cameraPhotoPath = FileUtil.getImageFilePath() + selfUser.id + "_" + System.currentTimeMillis() + ".png";
        return cameraPhotoPath;
    }

    /**
     * Bitmap 转 base64
     *
     * @return
     */
    public static String getBase64Image(String imgPath) {
        // 将Bitmap转换成字符串
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        readBitmap(imgPath).compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //  压缩图片；
    public void compressAndGetImageBitmap() {

        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                int degree = PictureUtils.compressBitmap(cameraPhotoPath, 500 * 1024, cameraPhotoPath);
                Bitmap bitmapShow = PictureUtils.getChatMsgShowBitmap(cameraPhotoPath, degree);
                Message message = vuMailHandler.obtainMessage();
                message.what = SHOW_COMPRESS_BITMAP;
                Bundle args = new Bundle();
                args.putParcelable(COMPRESS_BITMAP, bitmapShow);
                args.putString(IMG_PATH, cameraPhotoPath);
                message.setData(args);
                vuMailHandler.sendMessage(message);
            }
        });
    }

    /**
     * 根据文件路径生成 MailAttachment（邮件附件） 对象，并添加到附件列表里
     *
     * @param p
     */
    public void addMailAttachmentByFilePath(String p) {
        File f = new File(p);
        try {
            MailAttachment ma = new MailAttachment();
            ma.setSize(VUFileUtil.getFileSize(f));
            ma.setName(f.getName());
            ma.setEmail(currEmail);
            ma.setPath(p);
            if (!attachmentList.contains(ma)) {
                attachmentList.add(ma);
                writeMailOperation.get().refreshAttachmentListView();
                showSavePopWindowOnBackPress = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLocalFileBeanListToAttachmentList(ArrayList<LocalFileBean> choseFileList) {

        if (choseFileList == null || choseFileList.size() == 0) {
            return;
        }
        for (LocalFileBean lfb : choseFileList) {
            MailAttachment ma = new MailAttachment();
            ma.setSize(lfb.size);
            ma.setName(lfb.name);
            ma.setEmail(currEmail);
            ma.setPath(lfb.currentPath);
            if (!attachmentList.contains(ma)) {
                attachmentList.add(ma);
                showSavePopWindowOnBackPress = true;
                writeMailOperation.get().refreshAttachmentListView();
            }
        }
    }

    public void addToAttachmentList(List<MailAttachment> selectedAttacmentList) {
        for (MailAttachment ma : selectedAttacmentList) {
            if (!attachmentList.contains(ma)) {
                attachmentList.add(ma);
                showSavePopWindowOnBackPress = true;
                writeMailOperation.get().refreshAttachmentListView();
            }
        }
    }

    public void removeAttachment(int position) {
        attachmentList.remove(position);
        showSavePopWindowOnBackPress = true;
        writeMailOperation.get().refreshAttachmentListView();
    }

        /* ************  ContactSelectorFragment 联系人查询接口  ************ */

    private ArrayList<UserInfoBean> mSearchPersonResultArrayList;//搜索结果 list
    private ArrayList<GroupInfoBean> mGroupChatList;//联系组 结果list
    private ArrayList<UserInfoBean> mRecentChatList, mDepartmentList;//最近联系人list、部门人员list
    private ArrayList<UserInfoBean> selectedContacts;//已选中的人list
//    private ArrayList<GroupInfoBean> selectedGroups;//已选中的人list

    public void initContactSelectorData() {
        this.mGroupChatList = new ArrayList<>();
        this.mRecentChatList = new ArrayList<>();
        this.mDepartmentList = new ArrayList<>();
        this.mSearchPersonResultArrayList = new ArrayList<>();
        this.selectedContacts = currContactType == 1 ? toList : ccList;
        GroupStores.getInstance().getAllRecentContacts();
        GroupStores.getInstance().searchByDept();
    }

    public ArrayList<UserInfoBean> getSelectedContacts() {
        return selectedContacts;
    }

    public ArrayList getMRecentChatList() {
        return mRecentChatList;
    }

    public ArrayList getMDepartmentList() {
        return mDepartmentList;
    }

    public ArrayList<UserInfoBean> getMSearchPersonResultArrayList() {
        return mSearchPersonResultArrayList;
    }

    public void removeSelectedContact(UserInfoBean suifb) {
        selectedContacts.remove(suifb);
    }

    public void removeSelectedContact(int index) {
        selectedContacts.remove(index);
    }

    private UserInfoBean isUserExists(UserInfoBean userInfoBean) {
        UserInfoBean userNeedToDelete = null;
        for (UserInfoBean u : selectedContacts) {
            if (u.id.equals(userInfoBean.id)) {
                userNeedToDelete = u;
            }
        }
        if (userNeedToDelete == null) {
            selectedContacts.add(userInfoBean);
            showSavePopWindowOnBackPress = true;
        }
        return userNeedToDelete;
    }

    /**
     * 增加单个人员接口回调方法
     *
     * @param userInfoBean 添加的人员信息
     */
    public void addMember(UserInfoBean userInfoBean) {
        UserInfoBean suifb = isUserExists(userInfoBean);
        if (suifb != null) {
            removeSelectedContact(suifb);
            showSavePopWindowOnBackPress = true;
        }
    }

    /**
     * 增加一组人员接口回调方法
     *
     * @param groupInfoBean 添加的群组信息
     */
    public void addMember(GroupInfoBean groupInfoBean) {
//        Log.i(TAG, "groupInfoBean:" + groupInfoBean.getAllMemberName());
        ArrayList<UserInfoBean> chooseMemberList = groupInfoBean.getMemberList();
        for (UserInfoBean userInfoBean : chooseMemberList) {
            isUserExists(userInfoBean);
        }
    }

    public void selectOver() {
        writeMailOperation.get().emptyContactTV();
        for (UserInfoBean u : selectedContacts) {
//          重新生成UserInfoBean 视图
            writeMailOperation.get().generateContactTV(u, currContactType);
        }
    }

    public void addContactFromSearchResult(int position) {
        addMember(mSearchPersonResultArrayList.get(position));
    }

    /**
     * 更新UI线程
     *
     * @param updateUIAction
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateUIAction updateUIAction) {
        switch (updateUIAction.getActionType()) {
            // 添加搜索到的联系人
            case MessageActions.SEARCH_PERSON:
                //noinspection unchecked
//                Log.i(TAG, (currSPA == null) + " SEARCH_PERSON result: " + updateUIAction.getActionData().size());
                if (isInContactSelector) {
                    mSearchPersonResultArrayList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(0);
                    contactSelectOperation.get().refreshSearchResultListView(mSearchPersonResultArrayList);
                } else {
                    ArrayList<UserInfoBean> searchRes = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(0);
                    for (UserInfoBean resUib : searchRes) {
                        boolean hasAlready = false;
                        for (UserInfoBean uib : contactSearchResult) {
                            if (resUib.email.equals(uib.email)) {
                                hasAlready = true;
                            }
                        }
                        if (!hasAlready) {
                            contactSearchResult.add(resUib);
                        }
                    }
                    writeMailOperation.get().refreshSearchResultListView(contactSearchResult);
                }
                break;
            // 获取联系人和联系组
            case MessageActions.GET_ALL_RECENT_CONTACTS:
                //noinspection unchecked
                mGroupChatList = (ArrayList<GroupInfoBean>) updateUIAction.getActionData().get(0); // 聊天组列表
                //noinspection unchecked
                mRecentChatList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(1); // 联系人列表
                contactSelectOperation.get().refreshAllListView(mRecentChatList, mGroupChatList);

                break;
            // 获取部门人员
            case MessageActions.SEARCH_USER_BY_DEPT:
                //noinspection unchecked
                Log.i(TAG, "SEARCH_USER_BY_DEPT-------");
                mDepartmentList = (ArrayList<UserInfoBean>) updateUIAction.getActionData().get(0); // 部门人员列表
                contactSelectOperation.get().refreshDepartmentListView(mDepartmentList);

                break;
            default:
                break;
        }
    }

        /* ************  ContactSelectorFragment 联系人查询接口  ************ */


    /********************       写邮件     ********************/

    /********************* 展示邮件 ********************/

    private ArrayList<MailDetail> mailDetailList;
    private int currIndex;

    public void initMailDetailData() {
        mailDetailList = new ArrayList<>();
        currIndex = showingMailIdList.indexOf(currMail.getId());
        Log.i(TAG, "initMailDetailData: currIndex = "+currIndex);
        for (int i = 0; i < showingMailIdList.size(); i++) {
            mailDetailList.add(new MailDetail());
//            mailDetailOperation.get().addViewToViewList();
        }
    }

    public void onPageChanged(int position) {
        currIndex = position;
        Log.i("MailDetail...", "onPageSelected position=" + position);
        MailDetail md = mailDetailList.get(position);
        if (md.getUid() == null) {
            requestMailDetail();
        } else {
            currMail = md;//更新当前邮件
            mailDetailOperation.get().renderCurrentView(md);
        }
    }

    public void requestMailDetail() {
        Long mailUid = showingMailIdList.get(currIndex);

        Log.i(TAG, "requestMailDetail: currIndex = "+currIndex+"，requestMailDetail mailUid= " + mailUid);
        if(mailUid != null) DBUtil.queryMailDetail(mailUid);
    }

    private void getMailDetailByMessageIDCallback(SparseArray mlSA) {
        currMail = (MailDetail) mlSA.get(0);
        mailDetailList.set(currIndex,currMail);
        mailDetailOperation.get().renderCurrentView(currMail);
    }


    private WebView currContentWV;
    public void renderMailDetail(WebView currContentWV) {
        this.currContentWV = currContentWV;
        currAttachmentList.clear();
        if (currMail.getUid() != null) {
            if (!currMail.getIsRead()) currMail.setIsRead(true);
//            Log.i(TAG, "refreshCurrentView: md.getContent()="+md.getContent());
//            if(TextUtils.isEmpty(md.getContent())) {//如果 没有 Content  则解析邮件文件
            String mailFilePath = FileUtil.getServerMailFilePath(currEmail) + currMail.getUid() + ".xhw";
            File mailFile = new File(mailFilePath);
            Log.i(TAG, "refreshCurrentView: mailFilePath = " + mailFilePath);
            if (!mailFile.exists()) {
                Log.e(TAG, "refreshCurrentView: mail file is not existed  ");
//                String content = "<p style='color:#666'>邮件源文件丢失</p>";
////                currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
//                quoteMailContent = content;
//                currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
                if(Long.parseLong(currMail.getSize())>5*1024*1024){//如果大于5MB，提示用户是否继续下载
                    int netWorkType = vuActivityOperation.get().getNetworkType();
                    if(netWorkType == CURR_NETWORK_TYPE_2G || netWorkType == CURR_NETWORK_TYPE_3G || netWorkType == CURR_NETWORK_TYPE_4G ){
                        mailDetailOperation.get().showDownloadComfirmDialog();
                    }
                }else{
                    downloadMailContent();
                }
            } else {
                parseMailSource(mailFilePath);
            }
        } else {
            try {
                currAttachmentList.addAll(currMail.getAttachments());
                quoteMailContent = new String(Base64Utils.decode(currMail.getContent()), "utf-8");
                Log.i(TAG, "renderMailDetail: currMail.getContent() = " + currMail.getContent());
                currContentWV.loadDataWithBaseURL(null, quoteMailContent, "text/html", "utf-8", null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            mailDetailOperation.get().renderAttachmentList(currAttachmentList);
        }
    }

    private void downloadMailContent(){
        Log.i(TAG, "downloadMailContent: ...");
        showProgressDialog("正在下载邮件正文...");
        Thread mdThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    javax.mail.Message [] messages = getInboxMessages();
                    if(messages.length == 0){
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(DOWNLOAD_MAIL_DETAIL, 1, 0, "服务器中未找到该邮件"));
                    }
                    boolean isFound = false;
                    for(javax.mail.Message message : messages){
                        String uid = folder.getUID(message);
                        if(currMail.getUid().equals(uid)){
                            isFound = true;
                            MimeMessage mmsg = (MimeMessage) message;
                            // 获取邮件内容
                            String filePath = FileUtil.getServerMailFilePath(currMail.getEmail()) + uid + ".xhw";

                            FileOutputStream fos = new FileOutputStream(filePath);

                            @SuppressWarnings("unchecked")
                            Enumeration<String> hdrLines = mmsg.getNonMatchingHeaderLines(null);
                            LineOutputStream los = new LineOutputStream(fos);
                            while (hdrLines.hasMoreElements()) {
                                los.writeln(hdrLines.nextElement());
                            }

                            // The CRLF separator between header and content
                            los.writeln();
                            InputStream is = mmsg.getRawInputStream();
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = is.read(buffer)) != -1)
                                fos.write(buffer, 0, len);
                            is.close();
                            fos.close();
                            folder.close(false);
                            store.close();
                            vuMailHandler.sendMessage(vuMailHandler.obtainMessage(DOWNLOAD_MAIL_DETAIL, 0, 0, filePath));
                            break;
                        }
                    }
                    if(!isFound){
                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(DOWNLOAD_MAIL_DETAIL, 1, 0, "服务器中未找到该邮件"));
                    }
                } catch (Exception e) {
                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(DOWNLOAD_MAIL_DETAIL, 1, 0, "下载邮件正文时出错，请检返回重试"));
                    e.printStackTrace();
                }
            }
        });
        mdThread.start();
    }

    private void parseMailSource(String mailFilePath){
        String mailCachePath = FileUtil.getMailCachePath();
        showProgressDialog("正在解析邮件");
        try {
            String certPath = FileUtil.getCurrMailSafetyPath(currEmail) + "inspur.cert";
            if (!new File(certPath).exists()) {//如果 签名 证书文件不存在 就创建
                FileOutputStream fos = new FileOutputStream(certPath);
                InputStream is = mailDetailOperation.get().getResources().openRawResource(R.raw.inspur);
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    fos.write(buffer);
                }
                fos.flush();
            }
            MimeBodyPart msg = new MimeBodyPart(new FileInputStream(mailFilePath));
            int encType = MailUtil.getMailEncType(msg,currMail);
            switch (encType) {
                case 0:
                    Log.i(TAG, "renderMailDetail: 普通邮件");
                    MailUtil.getMailText(msg, currMail, currContentWV);
                    MailUtil.saveAttachments(msg, currMail.getEmail(), currAttachmentList);
                    quoteMailContent = currMail.getContent();
                    break;
                case 1://加密邮件
                    parseEncryptMail(currContentWV, mailFilePath, certPath);
                    break;
                case 2://签名邮件
                    Log.i(TAG, "renderMailDetail: 最外层是签名邮件");

                    String msg_vfy = mailCachePath + currMail.getUid() + "_vfy";
                    int vfyRes = dm.verifySignedMail(mailFilePath, msg_vfy, certPath);
                    Log.i(TAG, "renderMailDetail: verify signed mail res:" + vfyRes);
                    if (vfyRes == 0) {
                        MimeBodyPart vfyMsg = new MimeBodyPart(new FileInputStream(msg_vfy));
                        if (MailUtil.getMailEncType(vfyMsg, currMail) == 1) {
                            parseEncryptMail(currContentWV, msg_vfy, certPath);
                        }else{
                            MailUtil.getMailText(vfyMsg, currMail, currContentWV);
                            MailUtil.saveAttachments(vfyMsg, currMail.getEmail(), currAttachmentList);
                        }
                    } else {//解签名失败
                        Log.e(TAG, "renderMailDetail: verify signed mail error2:");
                        MailUtil.getMailText(msg, currMail, currContentWV);
                        MailUtil.saveAttachments(msg, currMail.getEmail(), currAttachmentList);
                    }
                    quoteMailContent = currMail.getContent();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            dismissProgressDialog();
        }
        currMail.setContent("");//用完即清空，否则邮件正文太大时 数据库受不了~
        DBUtil.updateMailDetail(currMail, null);
        mailDetailOperation.get().renderAttachmentList(currAttachmentList);
    }

    private void parseEncryptMail(WebView currContentWV,String inputFilePath,String certPath) throws Exception {
        Log.i(TAG, "parseEncryptMail: 加密邮件");
        String content = "<p style='color:#666'>本邮件是加密邮件，您的邮箱账号未安装任何数字证书，无法查看邮件详细内容。以下方案可以助您查看加密邮件：</p>" +
                "<p style='text-indent:20px;color:#999;font-size:14px'>  1.建议您微邮设置中安装证书，步骤为：先把证书文件拷贝到手机存储中，" +
                "然后点&nbsp;设置-->账号-->数字证书设置-->导入新证书-->选择证书文件-->输入证书密码-->安装。</p>" +
                "<p style='text-indent:20px;color:#999;font-size:14px'>  2.从好时光windows客户端的微邮中打开查看。</p>" +
                "<p style='text-indent:20px;color:#999;font-size:14px'>  3.从微软outlook客户端中打开查看。</p>";
//                                currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        String mailCachePath = FileUtil.getMailCachePath();
        quoteMailContent = content;
        if (!isCertInstalled()) {//如果是加密邮件 且没装证书；
            currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
            Log.i(TAG, "refreshCurrentView: 没证书");
        } else {//有证书
            Log.i(TAG, "有证书wyma.currUsingCA.getFilepath() = " + currUsingCA.getFilepath());
            String msg_dec = mailCachePath + currMail.getUid() + "_dec";
            int decRes = dm.decryptMail(inputFilePath, currUsingCA.getPassword(), msg_dec, currUsingCA.getFilepath());
            Log.w(TAG, "renderMailDetail: decryptMail error: "+decRes );
            if (decRes != 0) {//解密失败
                decRes = dm.decryptCmsMail(inputFilePath, currUsingCA.getPassword(), msg_dec, currUsingCA.getFilepath());
                Log.e(TAG, "renderMailDetail: decryptCmsMail error:" + decRes);
                if (decRes != 0) {
                    quoteMailContent = content;
                    Log.i(TAG, "refreshCurrentView: 解密失败,decRes = " + decRes);
                    currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
                    return;
                }
            }
            Log.i(TAG, "refreshCurrentView: 解密成功@#￥%……&*（");
            //解密成功
            MimeBodyPart mbp = new MimeBodyPart(new FileInputStream(msg_dec));

            //再判断一次 邮件加密类型 看是否是加密并签名的邮件
            if (MailUtil.getMailEncType(mbp, currMail) == 2) {//是 加密并签名邮件
                Log.i(TAG, "refreshCurrentView: 是加密并签名邮件");
                String msg_dec_vfy = mailCachePath + currMail.getUid() + "_dec_vfy";
                int vfyRes = dm.verifySignedMail(msg_dec, msg_dec_vfy, certPath);
                if (vfyRes == 0) {
                    MimeBodyPart vfyMsg = new MimeBodyPart(new FileInputStream(msg_dec_vfy));
                    Log.i(TAG, "parseEncryptMail: 解签名成功");
                    MailUtil.getMailText(vfyMsg, currMail, currContentWV);
                    MailUtil.saveAttachments(vfyMsg, currMail.getEmail(), currAttachmentList);
                } else {//解签名失败
                    Log.e(TAG, "refreshCurrentView: verify signed mail error1:" + vfyRes);
                    MailUtil.getMailText(mbp, currMail, currContentWV);
                }
            } else { //是 加密
                Log.i(TAG, "refreshCurrentView: 只是加密邮件,");
                MailUtil.getMailText(mbp, currMail, currContentWV);
                MailUtil.saveAttachments(mbp, currMail.getEmail(), currAttachmentList);
            }
            quoteMailContent = currMail.getContent();
//                                }
        }
    }

    public void continueDownloadMail() {
        downloadMailContent();
    }

    public void cancelDownloadMail() {
        String content = "<p style='color:#666'>已取消下载邮件正文</p>";
        currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        dismissProgressDialog();
    }

    /********************      展示邮件     ********************/

    /********************* 设置 ********************/

    public static final int MAIL_DOWNLOAD_WAY_HEAD = -1;
    public static final int MAIL_DOWNLOAD_WAY_ALL = 1;

    public void changeMailDownloadWay(int flag){
        if(flag == MAIL_DOWNLOAD_WAY_HEAD || flag == MAIL_DOWNLOAD_WAY_ALL){
            PreferencesHelper.getInstance().writeToPreferences(selfUser.id+"_mail_download_way", flag);
        }
    }
    public int getMailDownloadWay(){
        return PreferencesHelper.getInstance().readIntPreference(selfUser.id+"_mail_download_way");
    }

    private String currSettingEmail;
    public void sendWeiYouFeedback(String contentHtml, String subjectText) {
        showProgressDialog("正在发送您的反馈信息...");
        com.inspur.playwork.utils.OkHttpClientManager.Param[] params = {
                new com.inspur.playwork.utils.OkHttpClientManager.Param("sendServer", currMailAccount.getOutGoingHost()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("sendPort", currMailAccount.getOutGoingPort()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("protocol", currMailAccount.getProtocol()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("username", currMailAccount.getAccount()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("password", currMailAccount.getPassword()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("fromuser", currMailAccount.getEmail()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("nickname", currMailAccount.getNickName()),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("touser", "sunyuan@inspur.com"),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("touser_name", "孙源"),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("isHtml", "true"),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("subject", "微邮Android端反馈：" + subjectText),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("content", contentHtml),
                new com.inspur.playwork.utils.OkHttpClientManager.Param("createTime", new Date().getTime() + "")
        };
        OkHttpClientManager.getInstance().initHandler(new Handler())
                .post(AppConfig.WY_CFG.URL_SEND_MAIL, params, new OkHttpClientManager.HttpOperationCallback() {

                    @Override
                    public void onSuccess(String result) {
                        Log.i("mail-----", result);
                        dismissProgressDialog();
                        try {
                            JSONObject jRes = new JSONObject(result);
                            String responseCode = jRes.optString("code");
                            if ("0000".equals(responseCode)) {
                                feedbackOperation.get().closeFeedback();//关闭写反馈页面
                            } else {
                                if ("0003".equals(responseCode)) {
                                    Log.e(TAG, "onSuccess: 邮箱账号密码错误 " + currMailAccount.getEmail());
                                } else
                                    Log.e(TAG, jRes.optString("code") + " " + jRes.optString("msg"));
                            }
                            toast("反馈发送成功");
                        } catch (JSONException je) {
                            je.printStackTrace();
                            toast("反馈发送出错，请检查网络");
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        dismissProgressDialog();
                        toast("反馈发送出错，请检查网络");
                    }
                });
    }

    public void openMailAccountSettings(int index) {
        targetMailAccountIndex = index;
        newMailAccount = mailAccountCache.get(index);
        currSettingEmail = newMailAccount.getEmail();
        caList = CAEncryptUtils.getCAListData(newMailAccount.getEmail());
        vuActivityOperation.get().openMailAccountSettingFragment();
    }

    public MailAccount getNewMailAccount() {
        return newMailAccount;
    }

    public void addNewMailAccount(final MailAccount mailAccount) {
        mailAccount.setEnabled(true);//启用账号
        mailAccount.setUserId(selfUser.id);
        newMailAccount = mailAccount;
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("正在验证邮箱账号...");
                int res = 0;
                try {
                    if (MailUtil.verifyMailAccount(mailAccount))
                        res = 1;
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                dismissProgressDialog();
                vuMailHandler.sendMessage(vuMailHandler.obtainMessage(VERIFY_MAIL_ACCOUNT,res,0));
            }
        }).start();
    }

    public void saveMailAccountChanges(String nickName, String account,
                                       String password, String inComingHost,
                                       String inComingPort, boolean inComingSSL, String outGoingHost,
                                       String outGoingPort, boolean outGoingSSL) {
        boolean isNameChanged = !newMailAccount.getNickName().equals(nickName);
        if(isNameChanged){
//            newMailAccount.setDisplayName(displayName);
            newMailAccount.setNickName(nickName);
        }
        if ((!account.equals(newMailAccount.getAccount())) || (!password.equals(newMailAccount.getPassword())) ||
                (!inComingHost.equals(newMailAccount.getInComingHost())) || (!inComingPort.equals(newMailAccount.getInComingPort())) ||
                (inComingSSL != newMailAccount.getInComingSSL()) || (!outGoingHost.equals(newMailAccount.getOutGoingHost())) ||
                (!outGoingPort.equals(newMailAccount.getOutGoingPort())) || (outGoingSSL != newMailAccount.getOutGoingSSL())) {
            newMailAccount.setAccount(account);
            newMailAccount.setPassword(password);
            newMailAccount.setInComingHost(inComingHost);
            newMailAccount.setInComingPort(inComingPort);
            newMailAccount.setInComingSSL(inComingSSL);
            newMailAccount.setOutGoingHost(outGoingHost);
            newMailAccount.setOutGoingPort(outGoingPort);
            newMailAccount.setOutGoingSSL(outGoingSSL);
            if (!inComingSSL && !outGoingSSL)
                newMailAccount.setProtocol("pop3");
            else newMailAccount.setProtocol("pop3s");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog("正在验证邮箱账号...");
                    int res = 0;
                    try {
                        if(MailUtil.verifyMailAccount(newMailAccount))
                            res = 2;
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    dismissProgressDialog();
                    vuMailHandler.sendMessage(vuMailHandler.obtainMessage(VERIFY_MAIL_ACCOUNT,res,0));
                }
            }).start();
        }else if(isNameChanged){
            saveOneMailAccount(newMailAccount, UPDATE_EXISTS_MAIL_ACCOUNT);
        }else accountSettingsOperation.get().updateMailAccountCallback(true);
    }

    public boolean isDefaultAccount() {
        return newMailAccount.getEmail().endsWith(AppConfig.EMAIL_SUFFIX);
    }

    public void deleteMailAccount() {
        DBUtil.deleteMailAccount(newMailAccount.getId());
        mailAccountCache.remove(newMailAccount);
    }

//    private boolean defaultEncryptMail = false;//是否默认发送加密邮件
//    private boolean defaultSignMail = false;//是否默认发送加密邮件
    private int deleteIndex;

    public void initAccountCaData() {
        deleteIndex = -1;
        accountCaOperation.get().refreshCaListView();
    }

    public void setDefaultEncryptWay(boolean isChecked) {
        CAEncryptUtils.setDefaultEncrypt(currSettingEmail, isChecked);
    }

    public void setDefaultSignWay(boolean isChecked) {
        CAEncryptUtils.setDefaultSign(currSettingEmail, isChecked);
    }

    /**
     * 安装新的证书
     *
     * @param cao
     */
    public void addNewCAToListView(CAObject cao) {
        if (currEmail.equals(currSettingEmail) && currUsingCA != null) {
            for(CAObject oriCA:caList){
                if(oriCA.isDefaultCA()){
                    oriCA.setDefaultCA(false);
                }
            }
        }
        caList.add(0, cao);
        if(currEmail.equals(currSettingEmail)) {
            currUsingCA = cao;
        }
        saveCaList();
    }

    /**
     * 保存证书列表
     */
    public void saveCaList() {
        CAEncryptUtils.saveCaListData(currSettingEmail, caList);
        CAEncryptUtils.setDefaultEncrypt(currSettingEmail, true);
        CAEncryptUtils.setDefaultSign(currSettingEmail, true);
        accountCaOperation.get().refreshCaListView();
    }

    /**
     * 设置当前使用的证书
     *
     * @param i
     */
    public void caClickHandler(int i) {
        if (currEmail.equals(currSettingEmail) && currUsingCA != null) {
            for(CAObject oriCA:caList){
                if(oriCA.isDefaultCA()){
                    oriCA.setDefaultCA(false);
                }
            }
        }
        CAObject targetCA = caList.get(i);
        targetCA.setDefaultCA(true);
//        caList.set(i, targetCA);
        saveCaList();
    }

    /**
     * 验证并保存数字证书
     *
     * @param caPath
     * @param caName
     * @param password
     */
    public void verifyAndSaveCert(String caPath, String caName, String password) {

        CAObject cao = CAEncryptUtils.readCAFile(currSettingEmail, caPath, caName, password);
        if (cao.getErrorInfo() == null) {
            for (CAObject cao1 : caList) {//检查证书是否已存在
                if (cao1.getSn().equals(cao.getSn())) {
                    toast("证书已存在，请勿重复安装");
                    return;
                }
            }
            addNewCAToListView(cao);
            toast("证书安装成功");
        } else {
            toast(cao.getErrorInfo());
        }
    }

    /**
     * 卸载已安装的数字证书
     */
    public void uninstallCert() {
        caList.remove(deleteIndex);
        CAEncryptUtils.saveCaListData(currSettingEmail, caList);
        if(currEmail.equals(currSettingEmail)) {
            if (caList.size() > 0) {//更新当前使用的证书
                currUsingCA = caList.get(0);
            } else {
                currUsingCA = null;
                CAEncryptUtils.setDefaultEncrypt(currSettingEmail, false);
                CAEncryptUtils.setDefaultSign(currSettingEmail, false);
            }
        }
        accountCaOperation.get().refreshCaListView();
    }

    /********************       设置     ********************/

    /**
     * 操作数据库数据回调监听
     *
     * @param sa
     */
    public void onEventMainThread(StoreAction sa) {
        int actionType = sa.getActionType();
        SparseArray mlSA = sa.getActiontData();
        switch (actionType) {
            case DataBaseActions.QUERY_MAIL_ACCOUNT_LIST_SUCCESS:
                Log.i(TAG, "QUERY_MAIL_ACCOUNT_LIST_SUCCESS ==>");
                ArrayList<MailAccount> maList = (ArrayList<MailAccount>) mlSA.get(0);
                getMailAccountListCallback(maList);
                break;
            case DataBaseActions.QUERY_MAIL_ACCOUNT_LIST_FAILED:
                Log.e(TAG, "QUERY_MAIL_ACCOUNT_LIST_FAILED ==>");
                getMailAccountListCallback(null);
                break;
            case DataBaseActions.SAVE_ONE_MAIL_ACCOUNT_SUCCESS:
                Log.i(TAG, "SAVE_ONE_MAIL_ACCOUNT_SUCCESS mlSA.get(0)==>" + mlSA.get(0));
                saveMailAccountCallback(true, Long.parseLong(mlSA.get(0).toString()));
                break;
            case DataBaseActions.SAVE_ONE_MAIL_ACCOUNT_FAILED:
                Log.e(TAG, "SAVE_ONE_MAIL_ACCOUNT_FAILED ==>");
                saveMailAccountCallback(false, -1);
                break;
            case DataBaseActions.DELETE_ONE_MAIL_ACCOUNT_SUCCESS:
                Log.i(TAG, "DELETE_ONE_MAIL_ACCOUNT_SUCCESS ==>");
                accountSettingsOperation.get().deleteMailAccountCallback(true);
                refreshSpinner(mailAccountCache, targetMailAccountIndex - 1);
                break;
            case DataBaseActions.DELETE_ONE_MAIL_ACCOUNT_FAILED:
                Log.e(TAG, "DELETE_ONE_MAIL_ACCOUNT_FAILED ==>");
                accountSettingsOperation.get().deleteMailAccountCallback(false);
                break;
            case DataBaseActions.QUERY_MAIL_DIRECTORY_LIST_SUCCESS:
                Log.i(TAG, "QUERY_MAIL_DIRECTORY_LIST_SUCCESS ==>");
                getMailDirectoryListCallback((ArrayList<MailDirectory>) mlSA.get(0));
                break;
            case DataBaseActions.QUERY_MAIL_DIRECTORY_LIST_FAILED:
                Log.e(TAG, "QUERY_MAIL_DIRECTORY_LIST_FAILED ==>");
                getMailDirectoryListCallback(null);
                break;
            case DataBaseActions.QUERY_ALL_MAIL_UID_LIST_SUCCESS:
                Log.i(TAG, "QUERY_ALL_MAIL_UID_LIST_SUCCESS ==>" + mlSA.toString());
                queryAllMailUidListCallback((ArrayList<String>) mlSA.get(0));
                break;
            case DataBaseActions.QUERY_ALL_MAIL_UID_LIST_FAILED:
//                查询目录列表失败回调
                Log.e(TAG, "QUERY_ALL_MAIL_UID_LIST_FAILED ==>");
                queryAllMailUidListCallback(new ArrayList<String>());
                break;
            case DataBaseActions.INSERT_ONE_MAIL_MESSAGE_SUCCESS:
                Log.i(TAG, "INSERT_ONE_MAIL_MESSAGE_SUCCESS ==>" + mlSA.toString());
                /* 第一种情况：下载的邮件保存成功 */
                long mailId = (long) mlSA.get(0);
                String msgId = (String) mlSA.get(1);
                for (int i = 0; i < mailListData.size(); i++) {
                    MailDetail _md = mailListData.get(i);
                    if (_md.getMessageId() != null && _md.getMessageId().equals(msgId)) {
                        _md.setId(mailId);
                        mailListData.set(i, _md);
                        Log.i(TAG, "saveLocalMailCallback() ==>" + _md.getUid());
                    }
                }

                /* 第二种情况：发件箱、草稿箱、已发送的邮件 保存成功 */
                if (mailDetailCache != null) {
                    mailDetailCache.setId(mailId);
                    if (currEmail.equals(mailDetailCache.getEmail()) && currDirId == mailDetailCache.getDirectoryId()) {
                        Log.i(TAG, "邮件在当前目录中···");
                        if (currDirIsOutbox()) {
                            mailListData.clear();
                            mailListData.addAll(outBoxMailListData);
                        }
                    }
                    quoteMailContent = null;
//                    resetWriteMailData();
                }
                refreshMailListView();
                break;
            case DataBaseActions.INSERT_ONE_MAIL_MESSAGE_FAILED:
//           保存邮件失败回调
                Log.e(TAG, "INSERT_ONE_MAIL_MESSAGE_FAILED ==>" + mlSA.toString());
                // 邮件缓存 设置上ID
                long dirId = mailDetailCache.getDirectoryId();
                if (dirId == AppConfig.WY_CFG.DIR_ID_OUTBOX || dirId == AppConfig.WY_CFG.DIR_ID_DRAFTBOX) {
                    Log.i(TAG, "onEventMainThread: 保存草稿失败");
//                    resetWriteMailData();
                }
                break;
            case DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_SUCCESS:
                Log.i(TAG, "QUERY_MAIL_LIST_BY_DIR_ID_SUCCESS ==>" + mlSA);
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MAIL_LIST_BY_DIR_ID_FAILED:
                Log.e(TAG, "QUERY_MAIL_LIST_BY_DIR_ID_FAILED ==>" + sa.getActiontData().toString());
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_OUT_BOX_MAIL_LIST_SUCCESS:
                outBoxMailListData.clear();
                Log.i(TAG, "QUERY_OUT_BOX_MAIL_LIST_SUCCESS ==>");
                outBoxMailListData.addAll((ArrayList<MailDetail>) mlSA.get(0));
                mailSynchronizer.startMailSychronizer();
                break;
            case DataBaseActions.QUERY_OUT_BOX_MAIL_LIST_FAILED:
                Log.i(TAG, "QUERY_OUT_BOX_MAIL_LIST_FAILED ==>");
                outBoxMailListData.clear();
                break;
            case DataBaseActions.QUERY_UNREAD_MAIL_LIST_SUCCESS:
                Log.i(TAG, "QUERY_UNREAD_MAIL_LIST_SUCCESS ==>");
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_UNREAD_MAIL_LIST_FAILED:
                Log.e(TAG, "QUERY_UNREAD_MAIL_LIST_FAILED ==>");
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MARKED_MAIL_LIST_SUCCESS:
                Log.i(TAG, "QUERY_MARKED_MAIL_LIST_SUCCESS ==>");
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MARKED_MAIL_LIST_FAILED:
                Log.e(TAG, "QUERY_MARKED_MAIL_LIST_FAILED ==>");
                loadListDataCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_MESSAGE_ID_SUCCESS:
                Log.i(TAG, "==QUERY_MAIL_DETAIL_BY_MESSAGE_ID_SUCCESS ==");
                getMailDetailByMessageIDCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_MESSAGE_ID_FAILED:
                Log.e(TAG, "==QUERY_MAIL_DETAIL_BY_MESSAGE_ID_FAILED ==");
                break;
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_ID_SUCCESS:
                Log.i(TAG, "==QUERY_MAIL_DETAIL_BY_ID_SUCCESS ==");
                getMailDetailByMessageIDCallback(mlSA);
                break;
            case DataBaseActions.QUERY_MAIL_DETAIL_BY_ID_FAILED:
                Log.e(TAG, "==QUERY_MAIL_DETAIL_BY_ID_FAILED ==");
                break;
            case DataBaseActions.DELETE_ONE_MAIL_SUCCESS:
                Log.i(TAG, "DELETE_ONE_MAIL_SUCCESS ==>" + sa.getActiontData().toString());

                break;
            case DataBaseActions.DELETE_ONE_MAIL_FAILED:
                Log.e(TAG, "DELETE_ONE_MAIL_FAILED ==>" + sa.getActiontData().toString());

                break;
            case DataBaseActions.UPDATE_MAIL_DETAIL_SUCCESS:
                Log.i(TAG, "UPDATE_MAIL_DETAIL_SUCCESS ==>");
//                updateMailListDataAndView((MailDetail) mlSA.get(0));
                break;
            case DataBaseActions.UPDATE_MAIL_DETAIL_FAILED:
                break;
            case DataBaseActions.CHANGE_MAIL_DIRECTORY_SUCCESS:
                Log.i(TAG, "UPDATE_MAIL_DETAIL_SUCCESS ==>");
                MailDetail _md1 = (MailDetail) mlSA.get(0);
                if (_md1.getDirectoryId() == currDirId) {
                    if (currDirId == AppConfig.WY_CFG.DIR_ID_SENT_MAIL) {
                        mailListData.add(_md1);
                        refreshMailListView();
                    }
                }
                break;
            case DataBaseActions.CHANGE_MAIL_DIRECTORY_FAILED:
                break;
            case DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE_SUCCESS:
                break;
            case DataBaseActions.UPDATE_MAIL_SENT_PERCENTAGE_FAILED:
                break;
            case DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_SUCCESS:
                break;
            case DataBaseActions.SAVE_ONE_MAIL_ATTACHMENT_FAILED:
                break;
            case DataBaseActions.SAVE_ONE_MAIL_CONTACTS_SUCCESS:
                break;
            case DataBaseActions.SAVE_ONE_MAIL_CONTACTS_FAILED:
                break;
            case DataBaseActions.QUERY_MAIL_CONTACTS_LIST_SUCCESS:
                Log.i(TAG, "onEventMainThread: QUERY_MAIL_CONTACTS_LIST_SUCCESS res = "+mlSA.toString());
                mergeContactsSearchResult((ArrayList<MailContacts>) mlSA.get(0));
                break;
            case DataBaseActions.QUERY_MAIL_CONTACTS_LIST_FAILED:
                mergeContactsSearchResult(new ArrayList<MailContacts>());
                break;
            case DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST_SUCCESS:
                break;
            case DataBaseActions.QUERY_MAIL_ATTACHMENT_LIST_FAILED:
                break;

        }
    }

    private class VUMailHandler extends Handler {
        private WeakReference<VUStores> reference;

        public VUMailHandler(WeakReference<VUStores> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            VUStores vuStores = reference.get();
//            Log.i(TAG, "dispatchMessage: "+msg.what);
//            Log.i(TAG, "dispatchMessage: "+msg.obj);
            VUActivityOperation vuao = vuStores.vuActivityOperation.get();
            switch (msg.what) {
                case CHECK_MAIL_RESULT://检查邮件完毕
//                    if(msg.arg1 == 0){
                    if (vuao!=null && !vuao.isPaused()) {
                        if (msg.obj != null) {
                            MailListOperation mlo = vuStores.mailListOperation.get();
                            if(mlo!=null) mlo.showDownloadInfo(msg.obj.toString(), msg.arg1 == 1);
                        }
                    }
                    if(msg.arg1 == 1) vuStores.isDownloadingMail = false;
                    break;
                case DOWNLOAD_MAIL_RESULT://下载邮件情况提示
                    MailListOperation mlo = vuStores.mailListOperation.get();
                    if(mlo!=null) mlo.showDownloadInfo(msg.arg2, downloadingMailCount);
                    break;
                case FETCH_MESSAGES_RESULT://下载指定的邮件
                    Map<String, Object> params = (Map<String, Object>) msg.obj;
                    vuStores.handleMimeMessage((List<MimeMessage>) params.get("messageList"), (String) params.get("email"));
                    break;
                case DOWNLOAD_MAIL_DETAIL://处理下载完的单封邮件
                    switch (msg.arg1){
                        case 0:
                            vuStores.parseMailSource((String)msg.obj);
                            break;
                        case 1: {
                            vuStores.toast((String)msg.obj);
                            vuStores.dismissProgressDialog();
                            String content = "<p style='color:#666'>邮件正文下载失败，请返回重试</p>";
                            vuStores.currContentWV.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
                            break;
                        }
                    }
                    break;
                case SHOW_COMPRESS_BITMAP://处理压缩完的图片
                    vuStores.addMailAttachmentByFilePath(msg.getData().getString(IMG_PATH));
                    break;

                case SEND_MAIL_RESULT://发邮件结果处理
                    if (msg.arg1 == SEND_MAIL_SUCCESS) {
                    } else if (msg.arg1 == SEND_MAIL_FAILED) {
//                        vuStores.showProgressDialog(msg.obj.toString());
                        if(msg.obj!= null && vuao!=null)vuao.toast(msg.obj.toString());
                    } else if (msg.arg1 == SEND_MAIL_PARTIALLY) {
//                        vuStores.toast(msg.obj.toString());

                    } else if (msg.arg1 == SEND_MAIL_PBK_ERROR) {
//                        弹窗！！！
                        vuStores.writeMailOperation.get().showNoPbkWarningDialog(msg.obj.toString(), msg.arg2);
                    }
                    break;
                case HANDLE_DRAFT_MAIL_TASK://处理发邮件任务
//                    Log.i(TAG, "dispatchMessage: toList.size() = " + toList.size());
//                    Log.i(TAG, "dispatchMessage: ccList.size() = " + ccList.size());
                    if(msg.arg1==1){
                        vuStores.sendWeiYouMail();
                    }else
                        vuStores.sentOrSaveMailError("发送邮件前出现异常，请重新点击“发送”按钮");
                    break;

//                case PARSE_MAIL_DETAIL:
                case VERIFY_MAIL_ACCOUNT://验证邮箱账号
                    if(msg.arg1 == 1){
                        if(vuao!=null)vuao.showProgressDialog("验证通过,正在保存邮箱账号...");
                        vuStores.saveOneMailAccount(newMailAccount, SAVE_NEW_MAIL_ACCOUNT);
                    }else if(msg.arg1 == 2) {
                        if(vuao!=null)vuao.toast("邮箱账号验证通过");
                        vuStores.saveOneMailAccount(newMailAccount, UPDATE_EXISTS_MAIL_ACCOUNT);
                    }else {
                        if(vuao!=null)vuao.toast("邮箱账号验证失败");
                    }
                    break;
            }

        }
    }

    /**
     * 联网状态发生改变时触发
     *
     * @param netType
     */
    public void onNetStatusChanged(int netType) {
        if (netType == NetStatusReceiver.NETWORN_WIFI ||
                (netType == NetStatusReceiver.NETWORN_MOBILE && allowSyncWithMobileTraffic)) {
            mailSynchronizer.startMailSychronizer();
        } else {
            mailSynchronizer.clearSyncMailTask();
        }
    }

    public void setDeleteIndex(int deleteIndex) {
        this.deleteIndex = deleteIndex;
    }

    public void setQuoteType(int quoteType) {
        this.quoteType = quoteType;
    }

    /**
     * 清空来往邮件列表数据
     */
    public void clearExchangeListData() {
        exchangeMailList.clear();
    }

    /**
     * 判断当前显示的邮箱目录是不是收件箱
     *
     * @return
     */
    public boolean currDirIsInbox() {
        return currDirId == AppConfig.WY_CFG.DIR_ID_INBOX;
    }

    /**
     * 判断当前显示的邮箱目录是不是发件箱
     *
     * @return
     */
    public boolean currDirIsOutbox() {
        return currDirId == AppConfig.WY_CFG.DIR_ID_OUTBOX;
    }

    /**
     * 获取邮件列表数据
     *
     * @return
     */
    public ArrayList<MailDetail> getMailListData() {
        return mailListData;
    }

    /**
     * 获取邮箱账号列表数据
     *
     * @return
     */
    public List<MailAccount> getMailAccountList() {
        return mailAccountCache;
    }

    /**
     * 获取目录列表数据
     *
     * @return
     */
    public List<MailDirectory> getDirListData() {
        return directoryData;
    }

    /**
     * 发送邮件时，直接发送，即使主题为空
     */
    public void sendNoSubjectMail() {
        canSendNoSubjectMail = true;
        onSendMailButtonClick();
    }

    /**
     * 获取写邮件时的附件数据
     *
     * @return
     */
    public List<MailAttachment> getDraftAttachmentList() {
        return attachmentList;
    }

    public void setIsInContactSelector(boolean isInContactSelector) {
        this.isInContactSelector = isInContactSelector;
    }

    public List<CAObject> getCaList() {
        return caList;
    }

    public int getCurrIndex() {
        return currIndex;
    }

    public boolean getDefaultEncryptMail() {
        return caList.size()>0&&CAEncryptUtils.getDefaultEncrypt(newMailAccount.getEmail());
    }

    public boolean getDefaultSignMail() {
        return caList.size()>0&&CAEncryptUtils.getDefaultSign(newMailAccount.getEmail());
    }

    public void setVUActivityReference(VUActivityOperation operation) {
        this.vuActivityOperation = new WeakReference<>(operation);
        Log.i(TAG, "setVUActivityReference: ..."+(this.vuActivityOperation.get() == null));
    }

    public void setMailListReference(MailListOperation operation) {
        this.mailListOperation = new WeakReference<>(operation);
    }

    public void setExchangeMailListOperation(ExchangeMailListOperation operation) {
        this.exchangeMailListOperation = new WeakReference<>(operation);
    }

    public void setMailDetailReference(MailDetailOperation operation) {
        this.mailDetailOperation = new WeakReference<>(operation);
    }

    public void setContactSelectOperation(ContactSelectorOperation operation) {
        this.contactSelectOperation = new WeakReference<>(operation);
    }

    public void setMailAttachmentReference(MailAttachmentOperation operation) {
        this.mailAttachmentOperation = new WeakReference<>(operation);
    }

    public void setVUSettingsReference(VUSettingsOperation operation) {
        this.vuSettingsOperation = new WeakReference<>(operation);
    }

    public void setAddNewAccountReference(AddNewAccountOperation operation) {
        this.addNewAccountOperation = new WeakReference<>(operation);
    }

    public void setAccountSettingsReference(AccountSettingsOperation operation) {
        this.accountSettingsOperation = new WeakReference<>(operation);
    }

    public void setAccountCaReference(AccountCaOperation operation) {
        this.accountCaOperation = new WeakReference<>(operation);
    }

    public void setFeedbackReference(FeedbackOperation operation) {
        this.feedbackOperation = new WeakReference<>(operation);
    }

    public void setWriteMailReference(WriteMailOperation operation) {
        this.writeMailOperation = new WeakReference<>(operation);
    }

    public void setSendMailButtonEnabled(boolean sendMailButtonEnabled) {
        this.sendMailButtonEnabled = sendMailButtonEnabled;
    }

    private void toast(String msg){
        if(vuActivityOperation.get()!=null) vuActivityOperation.get().toast(msg);
    }

    private void showProgressDialog(String msg){
        if(vuActivityOperation.get()!=null) vuActivityOperation.get().showProgressDialog(msg);
    }

    private void dismissProgressDialog(){
        if(vuActivityOperation.get()!=null) vuActivityOperation.get().dismissProgressDialog();
    }

    private void refreshSpinner(ArrayList<MailAccount> mailAccountCache, int index){
        if(vuActivityOperation.get()!=null) vuActivityOperation.get().refreshSpinner(mailAccountCache,index);
    }

    private void renderMailListView(){
        if(mailListOperation.get()!=null) mailListOperation.get().renderMailListView();
    }

    public boolean needShowGuidePage() {
        return !PreferencesHelper.getInstance().readBooleanPreference(selfUser.id + NOT_SHOW_GUIDE_PAGE);
    }

    public void notShowGuidePageAnymore() {
        PreferencesHelper.getInstance().writeToPreferences(selfUser.id + NOT_SHOW_GUIDE_PAGE,true);
    }

    public static VUStores getInstance() {
        return SingleRefreshManager.getInstance().getVUStores();
    }

    public void cleanWriteMailStores(){
        writeMailOperation = null;
        if(vuActivityOperation == null || vuActivityOperation.get() == null){
            unRegister();
        }
    }

    public void clean() {
//        mailListData.clear();
        if (mailSynchronizer != null) {
            mailSynchronizer.destroy();
            mailSynchronizer = null;
        }
//        vuActivityOperation = null;
        if (dm != null) {dm.jclean(); dm = null; }
        unRegister();
    }
}
