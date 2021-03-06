package com.inspur.playwork.utils.db.bean;

import java.util.List;
import com.inspur.playwork.utils.db.dao.DaoSession;
import de.greenrobot.dao.DaoException;

import com.inspur.playwork.utils.db.dao.MailDetailDao;
import com.inspur.playwork.utils.db.dao.MailDirectoryDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "MAIL_DIRECTORY".
 */
public class MailDirectory {

    private Long id;
    /** Not-null value. */
    private String email;
    /** Not-null value. */
    private String name;
    private Boolean isSystem;
    private java.util.Date createTime;
    private java.util.Date updateTime;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MailDirectoryDao myDao;

    private List<MailDetail> mails;

    public MailDirectory() {
    }

    public MailDirectory(Long id) {
        this.id = id;
    }

    public MailDirectory(Long id, String email, String name, Boolean isSystem, java.util.Date createTime, java.util.Date updateTime) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.isSystem = isSystem;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMailDirectoryDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getEmail() {
        return email;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }

    public java.util.Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(java.util.Date updateTime) {
        this.updateTime = updateTime;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<MailDetail> getMails() {
        if (mails == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MailDetailDao targetDao = daoSession.getMailDetailDao();
            List<MailDetail> mailsNew = targetDao._queryMailDirectory_Mails(id);
            synchronized (this) {
                if(mails == null) {
                    mails = mailsNew;
                }
            }
        }
        return mails;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetMails() {
        mails = null;
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

}
