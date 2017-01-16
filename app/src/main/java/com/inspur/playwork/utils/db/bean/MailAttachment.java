package com.inspur.playwork.utils.db.bean;

import android.support.annotation.NonNull;

import com.inspur.playwork.utils.db.dao.DaoSession;
import de.greenrobot.dao.DaoException;

import com.inspur.playwork.utils.db.dao.MailAttachmentDao;
import com.inspur.playwork.utils.db.dao.MailDetailDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "MAIL_ATTACHMENT".
 */
public class MailAttachment implements Comparable<MailAttachment>{

    private Long id;
    private String name;
    private String path;
    private String url;
    private Long size;
    /** Not-null value. */
    private String email;
    private java.util.Date createTime;
    private long mailId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MailAttachmentDao myDao;

    private MailDetail mailDetail;
    private Long mailDetail__resolvedKey;


    public MailAttachment() {
    }

    public MailAttachment(Long id) {
        this.id = id;
    }

    public MailAttachment(Long id, String name, String path, String url, Long size, String email, java.util.Date createTime, long mailId) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.url = url;
        this.size = size;
        this.email = email;
        this.createTime = createTime;
        this.mailId = mailId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMailAttachmentDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    /** Not-null value. */
    public String getEmail() {
        return email;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmail(String email) {
        this.email = email;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }

    public long getMailId() {
        return mailId;
    }

    public void setMailId(long mailId) {
        this.mailId = mailId;
    }

    /** To-one relationship, resolved on first access. */
    public MailDetail getMailDetail() {
        long __key = this.mailId;
        if (mailDetail__resolvedKey == null || !mailDetail__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MailDetailDao targetDao = daoSession.getMailDetailDao();
            MailDetail mailDetailNew = targetDao.load(__key);
            synchronized (this) {
                mailDetail = mailDetailNew;
            	mailDetail__resolvedKey = __key;
            }
        }
        return mailDetail;
    }

    public void setMailDetail(MailDetail mailDetail) {
        if (mailDetail == null) {
            throw new DaoException("To-one property 'mailId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.mailDetail = mailDetail;
            mailId = mailDetail.getId();
            mailDetail__resolvedKey = mailId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    @Override
    public int compareTo(@NonNull MailAttachment another) {
        return another.createTime.compareTo(this.createTime);
    }
}
