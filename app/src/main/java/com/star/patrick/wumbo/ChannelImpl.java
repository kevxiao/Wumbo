package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
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
    private Set<UUID> receivedMessageIds = new HashSet<>();

    public ChannelImpl(String name, NetworkManager networkMgr, MainActivity context, Sender me) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.id = UUID.randomUUID();
        this.mainContext = context;
        this.me = me;
    }

    public void send(String msgText) {
        Message msg = new Message(msgText, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), id);
        send(msg);
    }

    public void send(Uri imagePath){}

    public void send(Message msg) {
        receivedMessageIds.add(msg.getId());
        msg.setReceiveTime(new Timestamp(new Date().getTime()));
        msgs.addMessage(msg);
        setChanged();
        notifyObservers();
        Intent sendMsgIntent = new Intent(mainContext, WifiDirectService.class);
        sendMsgIntent.setAction(WifiDirectService.SEND_MESSAGE_ACTION);
        sendMsgIntent.putExtra(WifiDirectService.EXTRA_MESSAGE, msg);
        mainContext.startService(sendMsgIntent);
    }

    public void receive(Message msg) {
        if (!receivedMessageIds.contains(msg.getId())) {
            send(msg);
            //add notif
        }
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
