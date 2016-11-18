package com.star.patrick.wumbo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Contacts;

import com.star.patrick.wumbo.message.Message;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // table names
    private static final String TABLE_MESSAGE = "messages";
    private static final String CHANNEL_TABLE = "channels";
    private static final String USER_TABLE = "users";

    // Contacts Table Columns names
    private static final String MESSAGE_UUID = "uuid";
    private static final String MESSAGE_CONTENT = "content";
    private static final String MESSAGE_TYPE = "type";
    private static final String MESSAGE_CUUID = "cuuid";
    private static final String MESSAGE_SUUID = "suuid";
    private static final String MESSAGE_RTIME = "rtime";
    private static final String MESSAGE_STIME = "stime";

    // Channel Table Column names
    private static final String CHANNEL_UUID = "uuid";
    private static final String CHANNEL_NAME = "name";

    // Sender Table Column names
    private static final String USER_UUID = "uuid";
    private static final String USER_DISPLAY_NAME = "display_name";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGE + "("
                + MESSAGE_UUID + " TEXT PRIMARY KEY,"
                + MESSAGE_TYPE + " INTEGER,"
                + MESSAGE_RTIME + " INTEGER,"
                + MESSAGE_STIME + " INTEGER,"
                + MESSAGE_CUUID + " TEXT,"
                + MESSAGE_SUUID + "TEXT,"
                + MESSAGE_CONTENT + " TEXT,"
                + "FOREIGN KEY(cuuid) REFERENCES channels(id),"
                + "FOREIGN KEY(suuid) REFERENCES senders(id)" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        String CREATE_CHANNEL_TABLE =
                "CREATE TABLE " + CHANNEL_TABLE + " ( " +
                    CHANNEL_UUID + " TEXT PRIMARY KEY " +
                    ", " +
                    CHANNEL_NAME + " TEXT " +
                ") ";
        db.execSQL(CREATE_CHANNEL_TABLE);

        String CREATE_USER_TABLE =
                "CREATE TABLE " + USER_TABLE + " ( " +
                    USER_UUID + " TEXT PRIMARY KEY " +
                    ", " +
                    USER_DISPLAY_NAME + " TEXT " +
                ") ";
        db.execSQL(CREATE_USER_TABLE);
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
        Sender snd = getSender(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
        switch (cursor.getInt(cursor.getColumnIndex("type"))){
            case 0:
                msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                        cursor.getString(cursor.getColumnIndex("content")),
                        snd,
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                        UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))));
                break;
        }

        // return contact
        return msg;
    }

    // Getting All Contacts
    public List<Message> getAllMessagesSince(Timestamp ts) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE rtime > ? ORDERBY rtime DESC;", new String[]{String.valueOf(ts.getTime())});

        List<Message> msgs= new ArrayList<>();

        while (cursor!=null && cursor.moveToNext()){
            Message msg = null;
            Sender snd = getSender(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
            switch (cursor.getInt(cursor.getColumnIndex("type"))){
                case 0:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            cursor.getString(cursor.getColumnIndex("content")),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))));
                    msgs.add(msg);
                    break;
            }
        }

        return msgs;
    }

    public List<Message> getAllMessages() {
        return getAllMessagesSince(new Timestamp(0));
    }

    public Sender getSender(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE uuid = ? ",
                new String[]{id.toString()}
        );

        if (null != cursor) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        return new Sender(id, cursor.getString(cursor.getColumnIndex(USER_DISPLAY_NAME)));
    }

    public void addSender(Sender user) {

    }

    public void updateSenderDisplayName(UUID id, String displayName) {

    }

    public Channel getChannel(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM channels WHERE uuid = ? ",
                new String[]{id.toString()}
        );

        if (null != cursor) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        return new ChannelImpl(
                id,
                cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                new NetworkManagerImpl(),
                mainActivity,
                me,
                channelManager
        );
    }

    public void addChannel(Channel channel) {

    }

    public void removeChannel(UUID id) {

    }

    public Map<UUID, Channel> getChannesl() {
        return null;
    }
}
