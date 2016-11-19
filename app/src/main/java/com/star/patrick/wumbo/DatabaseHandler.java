package com.star.patrick.wumbo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Base64;

import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.message.MessageList;
import com.star.patrick.wumbo.message.MessageListImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

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

    // Messages Table Columns names
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
    private static final String CHANNEL_KEY = "key";

    // User Table Column names
    private static final String USER_UUID = "uuid";
    private static final String USER_DISPLAY_NAME = "display_name";

    private MainActivity mainActivity;
    private MessageCourier messageCourier;

    public DatabaseHandler(Context context, MainActivity mainActivity, MessageCourier messageCourier) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mainActivity = mainActivity;
        this.messageCourier = messageCourier;

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SE464", "Creating THE BIG DB");
        db.execSQL("PRAGMA foreign_keys = ON;");

        String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGE + "("
                + MESSAGE_UUID + " TEXT PRIMARY KEY,"
                + MESSAGE_TYPE + " INTEGER,"
                + MESSAGE_RTIME + " INTEGER,"
                + MESSAGE_STIME + " INTEGER,"
                + MESSAGE_CUUID + " TEXT,"
                + MESSAGE_SUUID + " TEXT,"
                + MESSAGE_CONTENT + " TEXT,"
                + "FOREIGN KEY(cuuid) REFERENCES channels(uuid),"
                + "FOREIGN KEY(suuid) REFERENCES users(uuid)" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        String CREATE_CHANNEL_TABLE =
                "CREATE TABLE IF NOT EXISTS " + CHANNEL_TABLE + " ( " +
                    CHANNEL_UUID + " TEXT PRIMARY KEY " +
                    ", " +
                    CHANNEL_NAME + " TEXT " +
                    ", " +
                    CHANNEL_KEY + " TEXT " +
                ") ";
        db.execSQL(CREATE_CHANNEL_TABLE);

        String CREATE_USER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + USER_TABLE + " ( " +
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
        Log.d("SE464", "Saving a message to the database");
        SQLiteDatabase db = this.getWritableDatabase();

        addSender(msg.getUser());

        ContentValues values = new ContentValues();
        values.put(MESSAGE_UUID, msg.getId().toString());
        values.put(MESSAGE_CUUID, msg.getChannelId().toString());
        values.put(MESSAGE_SUUID, msg.getUser().getId().toString());
        values.put(MESSAGE_STIME, msg.getSendTime().getTime());
        values.put(MESSAGE_RTIME, msg.getReceiveTime().getTime());
        values.put(MESSAGE_TYPE, msg.getContent().getType().ordinal());
        switch (msg.getContent().getType()) {
            case TEXT:
                values.put(MESSAGE_CONTENT, (String)msg.getContent().getMessageContent());
                break;
        }

        db.insert(TABLE_MESSAGE, null, values);
        db.close();
    }

    // Getting single contact
    public Message getMessage(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE uuid=?;", new String[]{id.toString()});
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        Message msg = null;
        User snd = getSender(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
        switch (cursor.getInt(cursor.getColumnIndex("type"))){
            case 0:
                msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                        cursor.getString(cursor.getColumnIndex("content")),
                        snd,
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                        UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))));
                msg.setReceiveTime(new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime"))));
                break;
        }

        db.close();

        // return contact
        return msg;
    }

    // Getting All Contacts
    public List<Message> getAllMessagesSince(Timestamp ts) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE rtime > ? ORDER BY rtime ASC;", new String[]{String.valueOf(ts.getTime())});

        List<Message> msgs= new ArrayList<>();

        while (cursor!=null && cursor.moveToNext()){
            Message msg = null;
            User snd = getSender(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
            switch (cursor.getInt(cursor.getColumnIndex("type"))){
                case 0:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            cursor.getString(cursor.getColumnIndex("content")),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))));
                    msg.setReceiveTime(new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime"))));
                    msgs.add(msg);
                    break;
            }
        }

        db.close();

        Log.d("SE464", "Retrieving " + msgs.size() + " messages from the database");

        return msgs;
    }

    public List<Message> getAllMessages() {
        return getAllMessagesSince(new Timestamp(1));
    }

    public User getSender(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + USER_TABLE + " WHERE " + USER_UUID + " = ? ",
                new String[]{id.toString()}
        );

        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        User user = new User(id, cursor.getString(cursor.getColumnIndex(USER_DISPLAY_NAME)));

        cursor.close();
        db.close();

        return user;
    }

    public void addSender(User user) {
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

        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        byte[] encodedKey = Base64.decode(mainActivity.getResources().getString(cursor.getColumnIndex(CHANNEL_KEY)), Base64.DEFAULT);
        Channel channel = new ChannelImpl(
                id,
                cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                mainActivity,
                messageCourier,
                new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES"),
                this.getAllMessages(UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))))
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

        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        do {
            byte[] encodedKey = Base64.decode(mainActivity.getResources().getString(cursor.getColumnIndex(CHANNEL_KEY)), Base64.DEFAULT);
            Channel channel = new ChannelImpl(
                    UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))),
                    cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                    mainActivity,
                    messageCourier,
                    new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES"),
                    this.getAllMessages(UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))))
            );
            channels.put(channel.getId(), channel);
        } while ( cursor.moveToNext() );

        cursor.close();
        db.close();

        return channels;
    }

    public MessageList getAllMessages(UUID channelId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE cuuid = ? ORDER BY rtime ASC;", new String[]{String.valueOf(channelId.toString())});

        MessageList msgs= new MessageListImpl();

        while (cursor!=null && cursor.moveToNext()){
            Message msg = null;
            User snd = getSender(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
            switch (cursor.getInt(cursor.getColumnIndex("type"))){
                case 0:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            cursor.getString(cursor.getColumnIndex("content")),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))));
                    msg.setReceiveTime(new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime"))));
                    msgs.addMessage(msg);
                    break;
            }
        }

        cursor.close();
        db.close();

        return msgs;
    }
}
