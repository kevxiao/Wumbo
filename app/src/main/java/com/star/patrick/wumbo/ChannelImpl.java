package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChannelImpl implements Channel {

    private String name;
    private UUID channelId;
    private MessageListImpl msgs;
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

    public ChannelImpl(String name, NetworkManager networkMgr, Context context) {
        this.name = name;
        this.msgs = new MessageListImpl();
        this.networkMgr = networkMgr;
        this.channelId = UUID.randomUUID();
        this.mainContext = context;

        IntentFilter filter = new IntentFilter();
        filter.addAction(WUMBO_MESSAGE_INTENT_ACTION);
        mainContext.registerReceiver(receiver, filter);
    }

    public void send(String msgText) {
        Message msg = new Message(msgText, new Sender("Anon"), new Timestamp(Calendar.getInstance().getTimeInMillis()), channelId);
        send(msg);
    }

    public void send(Message msg) {
        if (receivedMessageIds.contains(msg.getId())) {
            msgs.addMessage(msg);
            receivedMessageIds.add(msg.getId());
            networkMgr.send(msg);
        }
    }

    public static final String WUMBO_MESSAGE_INTENT_ACTION = "wumbo_message";
    public static final String WUMBO_MESSAGE_EXTRA = "message";
}
