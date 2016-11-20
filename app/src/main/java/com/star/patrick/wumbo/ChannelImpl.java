package com.star.patrick.wumbo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Image;
import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.message.MessageContent;
import com.star.patrick.wumbo.message.MessageList;
import com.star.patrick.wumbo.message.MessageListImpl;
import com.star.patrick.wumbo.message.TransferImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ChannelImpl extends Observable implements Channel {

    private String name;
    private UUID id;
    private MessageList msgs;
    private MainActivity mainContext;
    private SecretKey encKey;
    private MessageCourier messageCourier;
    private Set<UUID> msgIds = new HashSet<>();

    public ChannelImpl(String name, MainActivity context, MessageCourier messageCourier) {
        this(UUID.randomUUID(), name, context, messageCourier, null);
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(Calendar.getInstance().getTimeInMillis());
            kgen.init(128, sr);
            encKey = kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public ChannelImpl(UUID id, String name, MainActivity context, MessageCourier messageCourier, SecretKey key) {
        this(id, name, context, messageCourier, key, new DatabaseHandler(context, messageCourier).getAllMessages(id));
    }

    public ChannelImpl(UUID id, String name, MainActivity context, MessageCourier messageCourier, SecretKey key, MessageList msgs) {
        this.name = name;
        this.msgs = msgs;
        this.id = id;
        this.mainContext = context;
        this.messageCourier = messageCourier;
        this.encKey = key;

        for (Message msg : msgs.getAllMessages()) {
            msgIds.add(msg.getId());
        }
    }

    public void send(User sender, String msgText) {
        Log.d("SE464", "Channel send string");
        final Message msg = new Message(msgText, sender, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        add(msg);
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                send(msg);
            }
        };
        sendThread.run();
    }

    public void send(User sender, Uri imagePath) {
        Log.d("SE464", "Channel send image");
        final String filepath = getFilePathFromId(id);
        try {
            copyFile(new File(getAbsolutePath(imagePath)), new File(filepath));
        } catch (Exception e){
            e.printStackTrace();
        }
        final Message msg = new Message(Uri.parse(filepath), sender, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        Log.d("SE464","Sending message: " + msg.getId());
        add(msg);

        Thread sendThread = new Thread() {
            @Override
            public void run() {
                try{
                    Log.d("SE464","Create bitmap from " + filepath);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    MediaStore.Images.Media.getBitmap(mainContext.getContentResolver(), Uri.fromFile(new File(filepath))).compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Message sendMsg = new Message(msg.getId(), stream.toByteArray(), msg.getUser(), msg.getSendTime(), msg.getChannelId());
                    send(sendMsg);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        sendThread.run();
    }

    public void send(Message msg) {
        Log.d("SE464", "Channel send message");
        EncryptedMessage emsg = new EncryptedMessage(msg, this.encKey);
        messageCourier.send(emsg);
    }

    public void receive(EncryptedMessage emsg) {
        Log.d("SE464", "Channel receive");
        if (!msgIds.contains(emsg.getId())) {
            Log.d("SE464", "Channel hasn't received this message before: " + emsg.getId());
            Message msg = new Message(emsg, this.encKey);
            Log.d("SE464", "Message ID: " + msg.getId() + " Message Sender: " + msg.getUser().getDisplayName());
            this.createNotification(msg);

            if(msg.getContent().getType() == MessageContent.MessageType.IMAGE) {
                msg = this.getImageMessageFromReceived(msg);
            }

            add(msg);
        }
    }

    private void add(Message msg) {
        Log.d("SE464", "Adding message to channel");
        msgIds.add(msg.getId());
        msg.setReceiveTime(new Timestamp(new Date().getTime()));
        msgs.addMessage(msg);
        setChanged();
        notifyObservers();

        //Add to database
        DatabaseHandler db = new DatabaseHandler(mainContext, messageCourier);
        db.addMessage(msg);
    }

    private void createNotification(Message msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mainContext)
                        .setSmallIcon(R.drawable.ic_wumbo)
                        .setContentTitle(this.name)
                        .setContentText(msg.getContent().getType() == MessageContent.MessageType.TEXT ? (String) msg.getContent().getMessageContent() : "Open to see image.");
        Intent resultIntent = new Intent(mainContext, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mainContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mainContext.getApplicationContext(), (int) System.currentTimeMillis(), resultIntent, 0);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mainContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private Message getImageMessageFromReceived(Message msg) {
        String filepath = getFilePathFromId(msg.getId());
        storeImage(msg, filepath);
        return new Message(Uri.parse(filepath), msg.getUser(), msg.getSendTime(), msg.getChannelId());
    }

    private void storeImage(Message msg, String filepath) {
        byte[] msgContent = (byte[]) msg.getContent().getMessageContent();
        if (msgContent != null) {
            Log.d("SE464", "Saving bitmap to " + filepath);
            FileOutputStream out = null;
            try{
                Bitmap bitmapImg = BitmapFactory.decodeByteArray(msgContent, 0, msgContent.length);
                out = new FileOutputStream(filepath);
                bitmapImg.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String getFilePathFromId(UUID msgId) {
        String fileName = msgId.toString() + ".jpg";
        ContextWrapper cw = new ContextWrapper(mainContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName);
        return mypath.getAbsolutePath();
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        Log.d("SE464","Copying image from " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    private String getAbsolutePath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = mainContext.getContentResolver().query(uri, projection, null, null, null);
        String path = uri.getPath();
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    @Override
    public List<Message> getAllMessages() {
        return msgs.getAllMessages();
    }

    @Override
    public List<Message> getAllMessagesSince(Timestamp ts) {
        return msgs.getAllMessagesSince(ts);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return Base64.encodeToString(encKey.getEncoded(), Base64.DEFAULT);
    }
}
