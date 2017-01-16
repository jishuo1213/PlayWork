package com.inspur.playwork.utils.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.inspur.playwork.utils.db.bean.MailDirectory;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MAIL_DIRECTORY".
*/
public class MailDirectoryDao extends AbstractDao<MailDirectory, Long> {

    public static final String TABLENAME = "MAIL_DIRECTORY";

    /**
     * Properties of entity MailDirectory.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Email = new Property(1, String.class, "email", false, "EMAIL");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property IsSystem = new Property(3, Boolean.class, "isSystem", false, "IS_SYSTEM");
        public final static Property CreateTime = new Property(4, java.util.Date.class, "createTime", false, "CREATE_TIME");
        public final static Property UpdateTime = new Property(5, java.util.Date.class, "updateTime", false, "UPDATE_TIME");
    };

    private DaoSession daoSession;


    public MailDirectoryDao(DaoConfig config) {
        super(config);
    }
    
    public MailDirectoryDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MAIL_DIRECTORY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"EMAIL\" TEXT NOT NULL ," + // 1: email
                "\"NAME\" TEXT NOT NULL ," + // 2: name
                "\"IS_SYSTEM\" INTEGER," + // 3: isSystem
                "\"CREATE_TIME\" INTEGER," + // 4: createTime
                "\"UPDATE_TIME\" INTEGER);"); // 5: updateTime
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MAIL_DIRECTORY\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MailDirectory entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getEmail());
        stmt.bindString(3, entity.getName());
 
        Boolean isSystem = entity.getIsSystem();
        if (isSystem != null) {
            stmt.bindLong(4, isSystem ? 1L: 0L);
        }
 
        java.util.Date createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(5, createTime.getTime());
        }
 
        java.util.Date updateTime = entity.getUpdateTime();
        if (updateTime != null) {
            stmt.bindLong(6, updateTime.getTime());
        }
    }

    @Override
    protected void attachEntity(MailDirectory entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public MailDirectory readEntity(Cursor cursor, int offset) {
        MailDirectory entity = new MailDirectory( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // email
            cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // isSystem
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)), // createTime
            cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)) // updateTime
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, MailDirectory entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setEmail(cursor.getString(offset + 1));
        entity.setName(cursor.getString(offset + 2));
        entity.setIsSystem(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setCreateTime(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
        entity.setUpdateTime(cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(MailDirectory entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MailDirectory entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
