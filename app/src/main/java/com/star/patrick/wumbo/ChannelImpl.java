package com.star.patrick.wumbo;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

public class ChannelImpl extends Observable implements Channel{

    private String name;
    private UUID channelId;
    private MessageList msgs;
    private NetworkManager networkMgr;

    public ChannelImpl(String name, NetworkManager networkMgr) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.channelId = UUID.randomUUID();
    }

    public void send(String msgText) {
        Message msg = new Message(msgText, new Sender("Anon"), new Timestamp(Calendar.getInstance().getTimeInMillis()), channelId);
        msg.setReceiveTime(new Timestamp(new Date().getTime()));
        msgs.addMessage(msg);
        setChanged();
        notifyObservers();
        networkMgr.send(msg);
    }

    @Override
    public List<Message> getAllMessages() {
        return msgs.getAllMessages();
    }

    @Override
    public List<Message> getAllMessagesSince(Timestamp ts) {
        return msgs.getAllMessagesSince(ts);
    }
}
