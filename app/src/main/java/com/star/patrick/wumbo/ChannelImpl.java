package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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
    private UUID channelId;
    private MessageList msgs;
    private NetworkManager networkMgr;
    private Context mainContext;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WUMBO_MESSAGE_INTENT_ACTION == action) {
                Message msg = (Message) intent.getSerializableExtra(WUMBO_MESSAGE_EXTRA);
                send(msg);
            }
        }
    };
    private Set<UUID> receivedMessageIds = new HashSet<>();

    public ChannelImpl(String name, NetworkManager networkMgr, Context context, Sender me) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.channelId = UUID.randomUUID();
        this.mainContext = context;
        this.me = me;

        IntentFilter filter = new IntentFilter();
        filter.addAction(WUMBO_MESSAGE_INTENT_ACTION);
        mainContext.registerReceiver(receiver, filter);
    }

    public void send(String msgText) {
        Message msg = new Message(msgText, me, new Timestamp(Calendar.getInstance().getTimeInMillis()), channelId);
        send(msg);
    }

    public void send(Message msg) {
        if (receivedMessageIds.contains(msg.getId())) {
            receivedMessageIds.add(msg.getId());
            msg.setReceiveTime(new Timestamp(new Date().getTime()));
            msgs.addMessage(msg);
            setChanged();
            notifyObservers();
            networkMgr.send(msg);
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

    public static final String WUMBO_MESSAGE_INTENT_ACTION = "wumbo_message";
    public static final String WUMBO_MESSAGE_EXTRA = "message";
}
