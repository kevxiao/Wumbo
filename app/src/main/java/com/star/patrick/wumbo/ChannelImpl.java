package com.star.patrick.wumbo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.star.patrick.wumbo.message.Message;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

public class ChannelImpl extends Observable implements Channel {

    private String name;
    private Sender me;
    private UUID id;
    private MessageList msgs;
    private NetworkManager networkMgr;
    private MainActivity mainContext;
    private ChannelManager channelManager;

    public ChannelImpl(String name, NetworkManager networkMgr, MainActivity context, Sender me, ChannelManager channelManager) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.id = UUID.randomUUID();
        this.mainContext = context;
        this.me = me;
        this.channelManager = channelManager;
    }

    public ChannelImpl(UUID id, String name, NetworkManager networkMgr, MainActivity context, Sender me, ChannelManager channelManager) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.id = id;
        this.mainContext = context;
        this.me = me;
        this.channelManager = channelManager;
    }

    public void send(String msgText) {
        Log.d("SE464", "Channel send string");
        Message msg = new Message(msgText, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        send(msg);
    }

    public void send(Uri imagePath, Context context) {
        Log.d("SE464", "Channel send image");
        Message msg = new Message(imagePath, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        send(msg);
    }

    public void send(Message msg) {
        Log.d("SE464", "Channel send message");
        add(msg);
        channelManager.send(msg);
    }

    public void receive(Message msg) {
        Log.d("SE464", "Channel receive");
        add(msg);
        //add notif
    }

    private void add(Message msg) {
        msg.setReceiveTime(new Timestamp(new Date().getTime()));
        msg.handleContentOnReceive();
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
}
