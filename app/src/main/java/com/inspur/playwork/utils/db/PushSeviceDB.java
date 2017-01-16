package com.inspur.playwork.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.playwork.model.common.SocketEvent;
import com.inspur.playwork.utils.ThreadPool;

import java.util.ArrayList;

/**
 * Created by fan on 16-8-2.
 */
public class PushSeviceDB {

    private static final String TAG = "PushSeviceDBFan";

    private static PushSeviceDB ourInstance = new PushSeviceDB();

    public static PushSeviceDB getInstance() {
        return ourInstance;
    }

    private DBHelper dbHelper;
    private SQLiteStatement insertSocketEventStatement;
    private SQLiteStatement updateSocketEventProcessStatement;
    private SQLiteStatement updateSocketNetProcessStatement;
    private String currentUserId = null;

    private PushSeviceDB() {
    }

    public void init(Context context, String userId) {
        Log.i(TAG, "init: userId:" + userId + "currentUserId:" + currentUserId);
        if (dbHelper == null || currentUserId == null) {
            currentUserId = userId;
            dbHelper = new DBHelper(context, userId + "_playwork_db");
        } else if (!currentUserId.equals(userId)) {
            if (dbHelper != null) {
                dbHelper.close();
            }
            dbHelper = new DBHelper(context, userId + "_playwork_db");
        }
    }

    public long insertSocketEvent(SocketEvent event) {
        if (insertSocketEventStatement == null) {
            insertSocketEventStatement = dbHelper.getWritableDatabase().compileStatement(SQLSentence.getUpsertSocketEventSql());
        }
        if (event != null) {
            fillInsertStatement(event);
        }
        return insertSocketEventStatement.executeInsert();
    }

    private void fillInsertStatement(SocketEvent event) {
        insertSocketEventStatement.bindString(1, event.fbId);
        insertSocketEventStatement.bindLong(2, event.eventCode);
        if (!TextUtils.isEmpty(event.eventInfo))
            insertSocketEventStatement.bindString(3, event.eventInfo);
        else
            insertSocketEventStatement.bindNull(3);
        insertSocketEventStatement.bindLong(4, (long) (event.isServerDelete ? 1 : 0));
        insertSocketEventStatement.bindLong(5, (long) (event.isClientProcess ? 1 : 0));
        insertSocketEventStatement.bindLong(6, event.reciveTime);
    }

    public void updateAfterServerDelete(String fbId) {
        if (updateSocketNetProcessStatement == null) {
            updateSocketNetProcessStatement = dbHelper.getWritableDatabase().compileStatement("update " +
                    SQLSentence.TABLE_SERVER_SOCKET_ENVENT +
                    " set " + SQLSentence.IS_SERVER_EVENT_DELETE + " = ? where " +
                    SQLSentence.EVENT_FEED_BACK_ID + " = ?");
        }
        updateSocketNetProcessStatement.bindLong(1, 1);
        updateSocketNetProcessStatement.bindString(2, fbId);
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                updateSocketNetProcessStatement.executeUpdateDelete();
            }
        });
    }

    public void updateAfterClentProcessed(String fbId) {
        if (updateSocketEventProcessStatement == null) {
            updateSocketEventProcessStatement = dbHelper.getWritableDatabase().compileStatement("update " +
                    SQLSentence.TABLE_SERVER_SOCKET_ENVENT +
                    " set " + SQLSentence.IS_CLIENT_HAS_PROCESS + " = ? where " +
                    SQLSentence.EVENT_FEED_BACK_ID + " = ?");
        }
        updateSocketEventProcessStatement.bindLong(1, 1);
        updateSocketEventProcessStatement.bindString(2, fbId);
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                updateSocketEventProcessStatement.executeUpdateDelete();
            }
        });
    }

    public ArrayList<SocketEvent> querySocketEvent() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        DBHelper.SocketEventCursor cursor = new DBHelper.SocketEventCursor(db.query(SQLSentence.TABLE_SERVER_SOCKET_ENVENT, null, null, null, null, null, SQLSentence.EVENT_RECIVE_TIME + " ASC "));
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            ArrayList<SocketEvent> eventList = new ArrayList<>();
            do {
                SocketEvent event = cursor.getSocketEvent();
                Log.i(TAG, "querySocketEvent: " + event.toString());
                eventList.add(event);
            } while (cursor.moveToNext());
            return eventList;
        }
        return null;
    }

    public long deleteSocketEventByFbId(String fbId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(SQLSentence.TABLE_SERVER_SOCKET_ENVENT, SQLSentence.IS_CLIENT_HAS_PROCESS + " = ? or " +
                SQLSentence.IS_SERVER_EVENT_DELETE + " = ? or " + SQLSentence.EVENT_FEED_BACK_ID + " = ? ", new String[]{1 + "", 1 + "", fbId});
    }

    public void insertSocketEventList(ArrayList<SocketEvent> unProcessEvent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        if (insertSocketEventStatement == null)
            insertSocketEventStatement = dbHelper.getWritableDatabase().compileStatement(SQLSentence.getUpsertSocketEventSql());

        try {
            for (SocketEvent event : unProcessEvent) {
                fillInsertStatement(event);
                Log.i(TAG, "insertSocketEventList: " + event.toString());
                insertSocketEventStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void logout() {
        if (insertSocketEventStatement != null)
            insertSocketEventStatement.close();
        if (updateSocketEventProcessStatement != null)
            updateSocketEventProcessStatement.close();
        if (updateSocketNetProcessStatement != null)
            updateSocketNetProcessStatement.close();
        insertSocketEventStatement = null;
        updateSocketEventProcessStatement = null;
        updateSocketNetProcessStatement = null;
    }
}

