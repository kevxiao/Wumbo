package com.star.patrick.wumbo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.star.patrick.wumbo.model.Channel;
import com.star.patrick.wumbo.model.ChannelImpl;
import com.star.patrick.wumbo.model.User;
import com.star.patrick.wumbo.model.message.Image;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.model.message.MessageList;
import com.star.patrick.wumbo.model.message.MessageListImpl;
import com.star.patrick.wumbo.model.message.Text;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "Wumbo";

    // table names
    private static final String TABLE_MESSAGE = "messages";
    private static final String CHANNEL_TABLE = "channels";
    private static final String USER_TABLE = "users";
    private static final String ME_TABLE = "me";

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
    private static final String USER_PUBLIC_KEY = "publkey";

    // Me Table Column names
    private static final String ME_UUID = "uuid";
    private static final String ME_PRIVATE_KEY = "privkey";

    private Context context;
    private MessageCourier messageCourier;

    public DatabaseHandler(Context context, MessageCourier messageCourier) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.messageCourier = messageCourier;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SE464", "Creating THE BIG DB");
        //Allow foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON;");
        //Allow database memory cleanup
        db.execSQL("PRAGMA auto_vacuum = FULL;");

        //Create the messages table
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

        //Create the channels table
        String CREATE_CHANNEL_TABLE =
                "CREATE TABLE IF NOT EXISTS " + CHANNEL_TABLE + " ( " +
                    CHANNEL_UUID + " TEXT PRIMARY KEY " +
                    ", " +
                    CHANNEL_NAME + " TEXT " +
                    ", " +
                    CHANNEL_KEY + " TEXT " +
                ") ";
        db.execSQL(CREATE_CHANNEL_TABLE);

        //Create users table
        String CREATE_USER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + USER_TABLE + " ( " +
                    USER_UUID + " TEXT PRIMARY KEY " +
                    ", " +
                    USER_DISPLAY_NAME + " TEXT " +
                    ", " +
                    USER_PUBLIC_KEY + " TEXT " +
                ") ";
        db.execSQL(CREATE_USER_TABLE);

        //Create me table which keeps track of the user's information
        String CREATE_ME_TABLE =
                "CREATE TABLE IF NOT EXISTS " + ME_TABLE + " ( " +
                    ME_UUID + " TEXT PRIMARY KEY, " +
                    ME_PRIVATE_KEY + " TEXT, " +
                    "FOREIGN KEY(" + ME_UUID + ") REFERENCES " + USER_TABLE + "(" + USER_UUID + ") " +
                ") ";
        db.execSQL(CREATE_ME_TABLE);

        //Create a trigger to delete messages that are past a certain point
        String MESSAGE_LIMIT_TRIGGER =
                "CREATE TRIGGER delete_till_500 INSERT ON messages WHEN (select count(*) from messages)>700\n"+
                "BEGIN\n"+
                        "DELETE FROM messages WHERE messages.uuid IN (SELECT messages.uuid FROM messages ORDER BY messages.rtime limit (select count(*) -500 from messages ));"+
                "END;";
        db.execSQL(MESSAGE_LIMIT_TRIGGER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + CHANNEL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ME_TABLE);

        // Create tables again
        onCreate(db);
    }

    public void addMessage(Message msg) {
        Log.d("SE464", "Saving a message to the database");
        addUser(msg.getUser());

        SQLiteDatabase db = this.getWritableDatabase();

        //Set the content of the messages into the db
        ContentValues values = new ContentValues();
        values.put(MESSAGE_UUID, msg.getId().toString());
        values.put(MESSAGE_CUUID, msg.getChannelId().toString());
        values.put(MESSAGE_SUUID, msg.getUser().getId().toString());
        values.put(MESSAGE_STIME, msg.getSendTime().getTime());
        values.put(MESSAGE_RTIME, msg.getReceiveTime().getTime());
        values.put(MESSAGE_TYPE, msg.getContent().getType().ordinal());

        //Set the content of the message depending on message type
        switch (msg.getContent().getType()) {
            case TEXT:
                values.put(MESSAGE_CONTENT, (String)msg.getContent().getMessageContent());
                break;
            case IMAGE:
                values.put(MESSAGE_CONTENT, (String)msg.getContent().getMessageContent());
                break;
        }

        db.insert(TABLE_MESSAGE, null, values);
        db.close();
    }

    public Message getMessage(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query for the message; return if it doesn't exist
        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE uuid=?;", new String[]{id.toString()});
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        //Create message from database row
        Message msg = null;
        User snd = getUser(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
        //Use proper constructor based on message type
        switch (cursor.getInt(cursor.getColumnIndex("type"))){
            case 0:
                msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                        new Text(cursor.getString(cursor.getColumnIndex("content"))),
                        snd,
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                        UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                );
                break;
            case 1:
                msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                        new Image(Uri.parse(cursor.getString(cursor.getColumnIndex("content")))),
                        snd,
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                        UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                        new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                );
                break;
        }

        cursor.close();
        db.close();

        return msg;
    }

    public List<Message> getAllMessagesSince(Timestamp ts) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query all messages since a time
        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE rtime > ? ORDER BY rtime ASC;", new String[]{String.valueOf(ts.getTime())});
        //Create a list of messages to return
        List<Message> msgs= new ArrayList<>();

        while (cursor!=null && cursor.moveToNext()){
            //Create message metadata
            Message msg = null;
            User snd = getUser(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
            //Create message based on the message type
            switch (cursor.getInt(cursor.getColumnIndex("type"))){
                case 0:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            new Text(cursor.getString(cursor.getColumnIndex("content"))),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                    );
                    break;
                case 1:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            new Image(Uri.parse(cursor.getString(cursor.getColumnIndex("content")))),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                    );
                    break;
            }
            //Add to the list of messages
            msgs.add(msg);
        }

        cursor.close();
        db.close();

        Log.d("SE464", "Retrieving " + msgs.size() + " messages from the database");
        return msgs;
    }

    public List<Message> getAllMessages() {
        return getAllMessagesSince(new Timestamp(1));
    }

    public User getUser(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query for user; return if it doesn't exist
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + USER_TABLE + " WHERE " + USER_UUID + " = ? ",
                new String[]{id.toString()}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        //Create user from db row
        User user = new User(
                id,
                cursor.getString(cursor.getColumnIndex(USER_DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndex(USER_PUBLIC_KEY))
        );

        cursor.close();
        db.close();

        return user;
    }


    public void updateUserDisplayName(UUID id, String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_DISPLAY_NAME, displayName);

        //Update the user's displayname in the db
        db.update(USER_TABLE, values, USER_UUID + " = ? ", new String[]{id.toString()});
        db.close();
    }

    public Channel getChannel(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query for channel; return null if it doesn't exist
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + CHANNEL_TABLE + " WHERE " + CHANNEL_UUID + " = ? ",
                new String[]{id.toString()}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        //Create channel out of db row
        Channel channel = new ChannelImpl(
                id,
                cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                context,
                messageCourier,
                Encryption.getSecretKeyFromEncoding(cursor.getString(cursor.getColumnIndex(CHANNEL_KEY))),
                this.getAllMessagesFromChannel(UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))))
        );
        cursor.close();
        db.close();

        return channel;
    }

    public void addChannel(Channel channel) {
        //Check if channel already exists
        if (getChannel(channel.getId()) != null)
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        //Insert a channel row into db
        ContentValues values = new ContentValues();
        values.put(CHANNEL_UUID, channel.getId().toString());
        values.put(CHANNEL_NAME, channel.getName());
        values.put(CHANNEL_KEY, channel.getKey());

        db.insert(CHANNEL_TABLE, null, values);
        db.close();
    }

    public void removeChannel(UUID id) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Delete channel from db
        db.delete(CHANNEL_TABLE, CHANNEL_UUID + " = ? ", new String[]{id.toString()});
        db.close();
    }

    public Map<UUID, Channel> getChannels() {
        Map<UUID, Channel> channels = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //Query for all channles; return empty map if no channels exist
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + CHANNEL_TABLE + " ORDER BY " + CHANNEL_NAME,
                new String[]{}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return channels;
        }

        //Create channels out of db rows
        do {
            Channel channel = new ChannelImpl(
                    UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))),
                    cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                    context,
                    messageCourier,
                    Encryption.getSecretKeyFromEncoding(cursor.getString(cursor.getColumnIndex(CHANNEL_KEY))),
                    this.getAllMessagesFromChannel(UUID.fromString(cursor.getString(cursor.getColumnIndex(CHANNEL_UUID))))
            );
            //Add channel to the map
            channels.put(channel.getId(), channel);
        } while ( cursor.moveToNext() );

        cursor.close();
        db.close();

        return channels;
    }

    public MessageList getAllMessagesFromChannel(UUID channelId) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query for all messages from a channel
        Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE cuuid = ? ORDER BY rtime ASC;", new String[]{String.valueOf(channelId.toString())});

        MessageList msgs= new MessageListImpl();

        //Create messages and add them to the message list
        while (cursor!=null && cursor.moveToNext()){
            Message msg = null;
            User snd = getUser(UUID.fromString(cursor.getString(cursor.getColumnIndex("suuid"))));
            switch (cursor.getInt(cursor.getColumnIndex("type"))){
                case 0:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            new Text(cursor.getString(cursor.getColumnIndex("content"))),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                    );
                    break;
                case 1:
                    msg = new Message(UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                            new Image(Uri.parse(cursor.getString(cursor.getColumnIndex("content")))),
                            snd,
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("stime"))),
                            UUID.fromString(cursor.getString(cursor.getColumnIndex("cuuid"))),
                            new Timestamp(cursor.getLong(cursor.getColumnIndex("rtime")))
                    );
                    break;
            }
            msgs.addMessage(msg);
        }

        cursor.close();
        db.close();

        return msgs;
    }

    public void setMe(UUID id, String privateKey) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Set the user's UUID and privateKey
        ContentValues values = new ContentValues();
        values.put(ME_UUID, id.toString());
        values.put(ME_PRIVATE_KEY, privateKey);

        db.insert(ME_TABLE, null, values);

        db.close();
    }

    public User getMe() {
        SQLiteDatabase db = this.getReadableDatabase();

        //Get the user's information; return null if he does not exist in db
        Cursor cursor = db.rawQuery(
                "SELECT " +
                    "m." + ME_UUID + " mid, " +
                    "u." + USER_DISPLAY_NAME + " udn, " +
                    "u." + USER_PUBLIC_KEY + " upk " +
                "FROM " + ME_TABLE + " m " +
                "INNER JOIN " + USER_TABLE + " u " +
                "ON m." + ME_UUID + " = u." + USER_UUID,
                new String[]{}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        //Create new user from db row
        User user = new User(
                UUID.fromString(cursor.getString(cursor.getColumnIndex("mid"))),
                cursor.getString(cursor.getColumnIndex("udn")),
                cursor.getString(cursor.getColumnIndex("upk"))
        );

        cursor.close();
        db.close();
        return user;
    }

    public void addUser(User user) {
        //Check if user already exists; update display name if needed
        if (getUser(user.getId()) != null) {
            updateUserDisplayName(user.getId(), user.getDisplayName());
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        //Set db row to user information
        ContentValues values = new ContentValues();
        values.put(USER_UUID, user.getId().toString());
        values.put(USER_DISPLAY_NAME, user.getDisplayName());
        values.put(USER_PUBLIC_KEY, Encryption.getEncodedPublicKey(user.getPublicKey()));

        db.insert(USER_TABLE, null, values);
        db.close();
    }

    public String getMePrivateKey() {
        SQLiteDatabase db = this.getReadableDatabase();

        //Get the user's private key; return null if it does not exist
        Cursor cursor = db.rawQuery(
                "SELECT " +
                        "m." + ME_PRIVATE_KEY + " mpk " +
                        "FROM " + ME_TABLE + " m",
                new String[]{}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return null;
        }

        String mePk = cursor.getString(cursor.getColumnIndex("mpk"));

        cursor.close();
        db.close();
        return mePk;
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        //Get all users in db; return empty list if they do not exist
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + USER_TABLE + " ORDER BY " + USER_DISPLAY_NAME,
                new String[]{}
        );
        if (cursor == null || !cursor.isBeforeFirst() || !cursor.moveToFirst()) {
            return users;
        }

        //Create users from db rows and add to list
        do {
            User user = new User(
                    UUID.fromString(cursor.getString(cursor.getColumnIndex(USER_UUID))),
                    cursor.getString(cursor.getColumnIndex(USER_DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(USER_PUBLIC_KEY))
            );
            users.add(user);
        } while ( cursor.moveToNext() );

        cursor.close();
        db.close();

        return users;
    }
}
