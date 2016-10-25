package com.kaodim.messenger.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Kanskiy on 24/10/2016.
 */

public class SqlHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;


    public static final String TABLE_NAME = "AMessenger";
    public static final String COLUMN_CONVERSATION_ID ="MESSAGE_ID";
    public static final String COLUMN_SENDER ="SENDER";
    public static final String COLUMN_MESSAGE ="MESSAGE";



    private static final String DATABASE_CREATE ="CREATE TABLE " + TABLE_NAME
            + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "+ COLUMN_CONVERSATION_ID +" STRING, "+COLUMN_SENDER+" STRING, "+COLUMN_MESSAGE+" STRING)";

    public SqlHelper(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

//    public void insertMessage(MessagePushModel msg) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(COLUMN_CONVERSATION_ID, msg.id);
//        cv.put(COLUMN_SENDER, msg.sender);
//        cv.put(COLUMN_MESSAGE, msg.message);
//        db.insert(TABLE_NAME, null, cv);
//        db.close();
//    }
//    public ArrayList<MessagePushModel> getMessages(){
//        ArrayList<MessagePushModel> messages = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_CONVERSATION_ID,
//                COLUMN_SENDER, COLUMN_MESSAGE },null, null, null, null, null, null);
//        if (cursor == null){
//            return messages;
//        }
//        cursor.moveToFirst();
//        while(!cursor.isAfterLast()){
//            messages.add(new MessagePushModel(cursor.getInt(0),
//                    cursor.getString(1), cursor.getString(2)));
//            cursor.moveToNext();
//        }
//        cursor.close();
//        db.close();
//        return messages;
//    }

//    public void clearAll() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        db.delete(TABLE_NAME,null,null);
//        db.close();
//    }
}