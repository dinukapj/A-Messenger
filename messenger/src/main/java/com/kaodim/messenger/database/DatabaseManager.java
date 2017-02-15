package com.kaodim.messenger.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kaodim.messenger.models.PushNotificationModel;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 24/10/2016.
 */

public class DatabaseManager {


    private int mOpenCounter;

    private static DatabaseManager instance;
    private static SqlHelper mSqlHelper;
    private SQLiteDatabase mDatabase;


    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager();
            mSqlHelper = new SqlHelper(context);
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = mSqlHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }
    public static ArrayList<PushNotificationModel>  insertMessage(PushNotificationModel msg, Context context) {
        SQLiteDatabase database = DatabaseManager.getInstance(context).openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SqlHelper.COLUMN_CONVERSATION_ID, msg.conversationId);
        cv.put(SqlHelper.COLUMN_SENDER, msg.sender);
        cv.put(SqlHelper.COLUMN_MESSAGE, msg.message);
        database.insert(SqlHelper.TABLE_NAME, null, cv);

        ArrayList<PushNotificationModel> messages = new ArrayList<>();
        Cursor cursor = database.query(SqlHelper.TABLE_NAME, new String[] { SqlHelper.COLUMN_CONVERSATION_ID,
                SqlHelper.COLUMN_SENDER, SqlHelper.COLUMN_MESSAGE },null, null, null, null, null, null);
        if (cursor == null){
            return messages;
        }
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            messages.add(new PushNotificationModel(cursor.getString(0),
                    cursor.getString(1), cursor.getString(2)));
            cursor.moveToNext();
        }
        Log.d("DatabaseManager", getTableAsString(database, SqlHelper.TABLE_NAME));
        DatabaseManager.getInstance(context).closeDatabase();

        return messages;
    }
    public static  String getTableAsString(SQLiteDatabase db, String tableName) {
        // Here This method is for logging purposes only. It logs the table data
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        Log.d("TABLE_DATA", tableString);
        return tableString;
    }

    public static void clearAll(Context context) {
        SQLiteDatabase database = DatabaseManager.getInstance(context).openDatabase();
        database.delete(SqlHelper.TABLE_NAME,null,null);
        DatabaseManager.getInstance(context).closeDatabase();
    }
    public static void removeFromDatabase(String conversationId, Context context){ //HERE Removes  all if conversationId is null
        SQLiteDatabase database = DatabaseManager.getInstance(context).openDatabase();
        String WHERE_QUERY = null;
        if (conversationId != null){
            WHERE_QUERY =SqlHelper.COLUMN_CONVERSATION_ID +"="+conversationId;
        }
        database.delete(SqlHelper.TABLE_NAME,WHERE_QUERY,null);
        DatabaseManager.getInstance(context).closeDatabase();
    }
    public static void clearDatabaseExcept(ArrayList<String> unreadConversationIds, Context context){ //HERE Removes  all if conversationId is null
        SQLiteDatabase database = DatabaseManager.getInstance(context).openDatabase();
        String WHERE_QUERY = null;
        if ((unreadConversationIds != null) && !(unreadConversationIds.isEmpty())){
            WHERE_QUERY =SqlHelper.COLUMN_CONVERSATION_ID+ " NOT IN (";
            for (int i=0; i<unreadConversationIds.size(); i++){
                if (i==0){
                    WHERE_QUERY+=unreadConversationIds.get(i);
                    continue;
                }
                WHERE_QUERY+=","+unreadConversationIds.get(i);
            }
            WHERE_QUERY +=")";
        }
        database.delete(SqlHelper.TABLE_NAME,WHERE_QUERY,null);
        DatabaseManager.getInstance(context).closeDatabase();
    }
}