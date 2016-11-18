package com.star.patrick.wumbo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.star.patrick.wumbo.message.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Created by jesse on 18/11/16.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Wumbo";

    // Contacts table name
    private static final String TABLE_MESSAGE = "messages";

    // Contacts Table Columns names
    private static final String MESSAGE_UUID = "uuid";
    private static final String MESSAGE_CONTENT = "content";
    private static final String MESSAGE_TYPE = "type";
    private static final String MESSAGE_CUUID = "cuuid";
    private static final String MESSAGE_SUUID = "suuid";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MESSAGE + "("
                + MESSAGE_UUID + " TEXT PRIMARY KEY,"
                + MESSAGE_TYPE + " INTEGER,"
                + MESSAGE_CUUID + " TEXT,"
                + MESSAGE_SUUID + "TEXT,"
                + MESSAGE_CONTENT + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addMessage(Message msg) {}

    // Getting single contact
    public Message getMessage(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE uuid=?;", new String[]{id.toString()});
        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;

        Message msg = null;
        Sender snd = null;
        switch (cursor.getInt(cursor.getColumnIndex("type"))){
            case 0:
                msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                        cursor.getString(cursor.getColumnIndex("content")),
                        snd,
                        null,
                        null);
                break;
        }

        // return contact
        return msg;
    }

    // Getting All Contacts
    public List<Message> getAllMessagesSince(Timestamp ts) {
        return null;
    }

    public List<Message> getAllMessages() {
        return getAllMessagesSince(new Timestamp(0));
    }

}
