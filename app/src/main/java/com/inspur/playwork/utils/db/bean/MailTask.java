package com.inspur.playwork.utils.db.bean;

import com.inspur.playwork.utils.db.dao.DaoSession;
import com.inspur.playwork.utils.db.dao.MailTaskDao;

/**
 * Created by sunyuan on 2016/12/5 0005 10:33.
 * Email: sunyuan@inspur.com
 */
public class MailTask {
    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MailTaskDao myDao;

    private Long id;
    /** Not-null value. */
    private String  email = "";
    private String  references;
    private String  subject = "";
    private Long    createTime = 0L;
    private String  nickName;
    private String  to = "";
    private String  cc;
    private String  messageFilePath = "";
    private boolean isEncrypted = false;
    private String  publicKeys = "";
    private String  mailRcpts = "";
    private String  account = "";
    private String  password = "";
    private String  outgoingHost = "";
    private String  outgoingPort = "";
    private boolean outgoingSSL = false;
    private boolean outgoingTLS = false;
    private long rcptCount = 0;
    private long sentRcptCount = 0;

    public MailTask(){}

    public MailTask(Long id, String email, String references, String subject, Long createTime, String nickName,
                    String to, String cc, String messageFilePath, boolean isEncrypted, String publicKeys,
                    String mailRcpts, String account, String password, String outgoingHost, String outgoingPort,
                    boolean outgoingSSL, boolean outgoingTLS, Long rcptCount, Long sentRcptCount) {
        this.id = id;
        this.email = email;
        this.references = references;
        this.subject = subject;
        this.createTime = createTime;
        this.nickName = nickName;
        this.to = to;
        this.cc = cc;
        this.messageFilePath = messageFilePath;
        this.isEncrypted = isEncrypted;
        this.publicKeys = publicKeys;
        this.mailRcpts = mailRcpts;
        this.account = account;
        this.password = password;
        this.outgoingHost = outgoingHost;
        this.outgoingPort = outgoingPort;
        this.outgoingSSL = outgoingSSL;
        this.outgoingTLS = outgoingTLS;
        this.rcptCount = rcptCount;
        this.sentRcptCount = sentRcptCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getMessageFilePath() {
        return messageFilePath;
    }

    public void setMessageFilePath(String messageFilePath) {
        this.messageFilePath = messageFilePath;
    }

    public boolean getEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(String publicKeys) {
        this.publicKeys = publicKeys;
    }

    public String getMailRcpts() {
        return mailRcpts;
    }

    public void setMailRcpts(String mailRcpts) {
        this.mailRcpts = mailRcpts;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOutgoingHost() {
        return outgoingHost;
    }

    public void setOutgoingHost(String outgoingHost) {
        this.outgoingHost = outgoingHost;
    }

    public String getOutgoingPort() {
        return outgoingPort;
    }

    public void setOutgoingPort(String outgoingPort) {
        this.outgoingPort = outgoingPort;
    }

    public boolean getOutgoingSSL() {
        return outgoingSSL;
    }

    public void setOutgoingSSL(boolean outgoingSSL) {
        this.outgoingSSL = outgoingSSL;
    }

    public boolean getOutgoingTLS() {
        return outgoingTLS;
    }

    public void setOutgoingTLS(boolean outgoingTLS) {
        this.outgoingTLS = outgoingTLS;
    }

    public long getRcptCount() {
        return rcptCount;
    }

    public void setRcptCount(long rcptCount) {
        this.rcptCount = rcptCount;
    }

    public long getSentRcptCount() {
        return sentRcptCount;
    }

    public void setSentRcptCount(long sentRcptCount) {
        this.sentRcptCount = sentRcptCount;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMailTaskDao() : null;
    }
}
