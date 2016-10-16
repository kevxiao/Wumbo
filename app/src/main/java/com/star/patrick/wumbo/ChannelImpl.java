package com.star.patrick.wumbo;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class ChannelImpl implements Channel{

    private String name;
    private UUID channelId;
    private MessageListImpl msgs;
    private NetworkManager networkMgr;

    public ChannelImpl(String name, NetworkManager networkMgr) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.channelId = UUID.randomUUID();
    }

    public void send(String msgText) {
        Message msg = new Message(msgText, new Sender("Anon"), new Timestamp(Calendar.getInstance().getTimeInMillis()), channelId);
        msgs.addMessage(msg);
        networkMgr.send(msg);
    }
}
