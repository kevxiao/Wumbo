package com.star.patrick.wumbo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Contacts;

import com.star.patrick.wumbo.message.Message;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    private Sender me;
    private MainActivity mainActivity;
    private ChannelManager channelManager;

    public DatabaseHandler(Context context, Sender me, MainActivity mainActivity, ChannelManager channelManager) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.me = me;
        this.mainActivity = mainActivity;
        this.channelManager = channelManager;
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
        db.execSQL("DROP TABLE IF EXISTS " + CHANNEL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addMessage(Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();

        addSender(msg.getSender());

        ContentValues values = new ContentValues();
        values.put(MESSAGE_UUID, msg.getId().toString());
        values.put(MESSAGE_CUUID, msg.getChannelId().toString());
        values.put(MESSAGE_SUUID, msg.getSender().getId().toString());
        values.put(MESSAGE_STIME, msg.getSendTime().getTime());
        values.put(MESSAGE_RTIME, msg.getReceiveTime().getTime());

        values.put(MESSAGE_TYPE, msg.getContent().getType().ordinal());
        switch (msg.getContent().getType()) {
            case TEXT:
                values.put(MESSAGE_CONTENT, (String)msg.getContent().getMessageContent());
                break;
        }

        db.insert(TABLE_MESSAGE, null, values);
    }

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
                "SELECT * FROM " + USER_TABLE + " WHERE " + USER_UUID + " = ? ",
                new String[]{id.toString()}
        );

        if (null != cursor) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        Sender user = new Sender(id, cursor.getString(cursor.getColumnIndex(USER_DISPLAY_NAME)));

        cursor.close();
        db.close();

        return user;
    }

    public void addSender(Sender user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_UUID, user.getId().toString());
        values.put(USER_DISPLAY_NAME, user.getDisplayName());

        db.insert(USER_TABLE, null, values);
    }

    public void updateSenderDisplayName(UUID id, String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_DISPLAY_NAME, displayName);

        db.update(CHANNEL_TABLE, values, USER_DISPLAY_NAME + " = ? ", new String[]{id.toString()});
        db.close();
    }

    public Channel getChannel(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + CHANNEL_TABLE + " WHERE " + CHANNEL_UUID + " = ? ",
                new String[]{id.toString()}
        );

        if (null != cursor) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        Channel channel = new ChannelImpl(
                id,
                cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                new NetworkManagerImpl(),
                mainActivity,
                me,
                channelManager
        );
        cursor.close();
        db.close();

        return channel;
    }

    public void addChannel(Channel channel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CHANNEL_UUID, channel.getId().toString());
        values.put(CHANNEL_NAME, channel.getName());

        db.insert(CHANNEL_TABLE, null, values);
        db.close();
    }

    public void removeChannel(UUID id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(CHANNEL_TABLE, CHANNEL_UUID + " = ? ", new String[]{id.toString()});
        db.close();
    }

    public Map<UUID, Channel> getChannels() {
        Map<UUID, Channel> channels = new LinkedHashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + CHANNEL_TABLE + " ORDER BY " + CHANNEL_NAME,
                new String[]{}
        );

        if (null != cursor) {
            cursor.moveToFirst();
        } else {
            return channels;
        }

        do {
            Channel channel = new ChannelImpl(
                    UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))),
                    cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                    new NetworkManagerImpl(),
                    mainActivity,
                    me,
                    channelManager
            );
            channels.put(channel.getId(), channel);
        } while ( !cursor.moveToNext() );

        cursor.close();
        db.close();

        return channels;
    }
}
