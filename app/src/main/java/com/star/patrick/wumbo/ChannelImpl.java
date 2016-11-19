package com.star.patrick.wumbo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Image;
import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.message.MessageContent;
import com.star.patrick.wumbo.message.MessageList;
import com.star.patrick.wumbo.message.MessageListImpl;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ChannelImpl extends Observable implements Channel {

    private String name;
    private Sender me;
    private UUID id;
    private MessageList msgs;
    private NetworkManager networkMgr;
    private MainActivity mainContext;
    private ChannelManager channelManager;
    private SecretKey encKey;

    public ChannelImpl(String name, NetworkManager networkMgr, MainActivity context, Sender me, ChannelManager channelManager) {
        this(UUID.randomUUID(), name, networkMgr, context, me, channelManager, null);
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

    public ChannelImpl(UUID id, String name, NetworkManager networkMgr, MainActivity context, Sender me, ChannelManager channelManager, SecretKey key) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.id = id;
        this.mainContext = context;
        this.me = me;
        this.channelManager = channelManager;
        this.encKey = key;
    }

    public void send(String msgText) {
        Log.d("SE464", "Channel send string");
        Message msg = new Message(msgText, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        send(msg);
    }

    public void send(Uri imagePath, Context context) {
        Log.d("SE464", "Channel send image");
        Message msg = new Message(imagePath, context, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        ((Image)msg.getContent()).createImageFromFilepath(context);
        send(msg);
    }

    public void send(Message msg) {
        Log.d("SE464", "Channel send message");
        add(msg);
        EncryptedMessage emsg = new EncryptedMessage(msg, this.encKey);
        channelManager.send(emsg);
    }

    public void receive(EncryptedMessage emsg) {
        Log.d("SE464", "Channel receive");
        Message msg = new Message(emsg, this.encKey);
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
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mainContext.getApplicationContext(), (int)System.currentTimeMillis(), resultIntent, 0);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mainContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        msg.handleContentOnReceive(mainContext);
        add(msg);
    }

    private void add(Message msg) {
        msg.setReceiveTime(new Timestamp(new Date().getTime()));
        msgs.addMessage(msg);
        setChanged();
        notifyObservers();
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
}
