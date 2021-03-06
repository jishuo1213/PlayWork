package com.inspur.playwork.utils.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.inspur.playwork.utils.db.bean.MailAccount;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MAIL_ACCOUNT".
*/
public class MailAccountDao extends AbstractDao<MailAccount, Long> {

    public static final String TABLENAME = "MAIL_ACCOUNT";

    /**
     * Properties of entity MailAccount.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserId = new Property(1, String.class, "userId", false, "USER_ID");
        public final static Property DisplayName = new Property(2, String.class, "displayName", false, "DISPLAY_NAME");
        public final static Property NickName = new Property(3, String.class, "nickName", false, "NICK_NAME");
        public final static Property Email = new Property(4, String.class, "email", false, "EMAIL");
        public final static Property Account = new Property(5, String.class, "account", false, "ACCOUNT");
        public final static Property Password = new Property(6, String.class, "password", false, "PASSWORD");
        public final static Property IsEncrypted = new Property(7, Boolean.class, "isEncrypted", false, "IS_ENCRYPTED");
        public final static Property InComingHost = new Property(8, String.class, "inComingHost", false, "IN_COMING_HOST");
        public final static Property InComingPort = new Property(9, String.class, "inComingPort", false, "IN_COMING_PORT");
        public final static Property InComingSSL = new Property(10, Boolean.class, "inComingSSL", false, "IN_COMING_SSL");
        public final static Property OutGoingHost = new Property(11, String.class, "outGoingHost", false, "OUT_GOING_HOST");
        public final static Property OutGoingPort = new Property(12, String.class, "outGoingPort", false, "OUT_GOING_PORT");
        public final static Property OutGoingSSL = new Property(13, Boolean.class, "outGoingSSL", false, "OUT_GOING_SSL");
        public final static Property OutGoingTLS = new Property(14, Boolean.class, "OutGoingTLS", false, "OUT_GOING_TLS");
        public final static Property Protocol = new Property(15, String.class, "protocol", false, "PROTOCOL");
        public final static Property DefaultEncryptMail = new Property(16, Boolean.class, "defaultEncryptMail", false, "DEFAULT_ENCRYPT_MAIL");
        public final static Property DefaultSignMail = new Property(17, Boolean.class, "defaultSignMail", false, "DEFAULT_SIGN_MAIL");
        public final static Property Enabled = new Property(18, Boolean.class, "enabled", false, "ENABLED");
    };


    public MailAccountDao(DaoConfig config) {
        super(config);
    }
    
    public MailAccountDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MAIL_ACCOUNT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER_ID\" TEXT NOT NULL ," + // 1: userId
                "\"DISPLAY_NAME\" TEXT," + // 2: displayName
                "\"NICK_NAME\" TEXT," + // 3: nickName
                "\"EMAIL\" TEXT NOT NULL ," + // 4: email
                "\"ACCOUNT\" TEXT," + // 5: account
                "\"PASSWORD\" TEXT," + // 6: password
                "\"IS_ENCRYPTED\" INTEGER," + // 7: isEncrypted
                "\"IN_COMING_HOST\" TEXT," + // 8: inComingHost
                "\"IN_COMING_PORT\" TEXT," + // 9: inComingPort
                "\"IN_COMING_SSL\" INTEGER," + // 10: inComingSSL
                "\"OUT_GOING_HOST\" TEXT," + // 11: outGoingHost
                "\"OUT_GOING_PORT\" TEXT," + // 12: outGoingPort
                "\"OUT_GOING_SSL\" INTEGER," + // 13: outGoingSSL
                "\"OUT_GOING_TLS\" INTEGER," + // 14: outGoingTLS
                "\"PROTOCOL\" TEXT," + // 15: protocol
                "\"DEFAULT_ENCRYPT_MAIL\" INTEGER," + // 16: keepMailAfterDeletedFromServer
                "\"DEFAULT_SIGN_MAIL\" INTEGER," + // 17: backupMailToServer
                "\"ENABLED\" INTEGER);"); // 18: enabled
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MAIL_ACCOUNT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MailAccount entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUserId());
 
        String displayName = entity.getDisplayName();
        if (displayName != null) {
            stmt.bindString(3, displayName);
        }
 
        String nickName = entity.getNickName();
        if (nickName != null) {
            stmt.bindString(4, nickName);
        }
        stmt.bindString(5, entity.getEmail());
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(6, account);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(7, password);
        }
 
        String inComingHost = entity.getInComingHost();
        if (inComingHost != null) {
            stmt.bindString(9, inComingHost);
        }
 
        String inComingPort = entity.getInComingPort();
        if (inComingPort != null) {
            stmt.bindString(10, inComingPort);
        }
 
        boolean inComingSSL = entity.getInComingSSL();
        stmt.bindLong(11, inComingSSL ? 1L: 0L);

        String outGoingHost = entity.getOutGoingHost();
        if (outGoingHost != null) {
            stmt.bindString(12, outGoingHost);
        }
 
        String outGoingPort = entity.getOutGoingPort();
        if (outGoingPort != null) {
            stmt.bindString(13, outGoingPort);
        }
 
        boolean outGoingSSL = entity.getOutGoingSSL();
        stmt.bindLong(14, outGoingSSL ? 1L: 0L);

        boolean outGoingTLS = entity.getOutGoingTLS();
        stmt.bindLong(15, outGoingTLS ? 1L: 0L);

        String protocol = entity.getProtocol();
        if (protocol != null) {
            stmt.bindString(16, protocol);
        }
 
        boolean defaultEncryptMail = entity.getDefaultEncryptMail();
        stmt.bindLong(17, defaultEncryptMail ? 1L: 0L);

        boolean defaultSignMail = entity.getDefaultSignMail();
        stmt.bindLong(18, defaultSignMail ? 1L: 0L);

        boolean enabled = entity.getEnabled();
        stmt.bindLong(19, enabled ? 1L: 0L);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public MailAccount readEntity(Cursor cursor, int offset) {
        MailAccount entity = new MailAccount( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // userId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // displayName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // nickName
            cursor.getString(offset + 4), // email
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // account
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // password
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // inComingHost
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // inComingPort
            cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0, // inComingSSL
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // outGoingHost
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // outGoingPort
            cursor.isNull(offset + 13) ? null : cursor.getShort(offset + 13) != 0, // outGoingSSL
            cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0, // outGoingSSL
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // protocol
            cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // defaultEncryptMail
            cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // defaultSignMail
            cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0 // enabled
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, MailAccount entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserId(cursor.getString(offset + 1));
        entity.setDisplayName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setNickName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setEmail(cursor.getString(offset + 4));
        entity.setAccount(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPassword(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setInComingHost(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setInComingPort(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setInComingSSL(cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0);
        entity.setOutGoingHost(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setOutGoingPort(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setOutGoingSSL(cursor.isNull(offset + 13) ? null : cursor.getShort(offset + 13) != 0);
        entity.setProtocol(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setDefaultEncryptMail(cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0);
        entity.setDefaultSignMail(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setEnabled(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(MailAccount entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MailAccount entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    public List<MailAccount> _queryMailAccountsByUserId(String userId){
        QueryBuilder<MailAccount> queryBuilder = queryBuilder();
        queryBuilder.where(MailAccountDao.Properties.UserId.eq(userId));
        return queryBuilder.list();
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
