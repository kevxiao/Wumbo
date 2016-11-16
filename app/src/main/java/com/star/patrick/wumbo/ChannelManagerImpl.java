package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChannelManagerImpl implements ChannelManager {
    private MainActivity mainContext;
    private ChannelList channels = new ChannelListImpl();
    private Set<UUID> receivedMessageIds = new HashSet<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SE464", "Channel received a message");
            String action = intent.getAction();
            if (WUMBO_MESSAGE_INTENT_ACTION.equals(action)) {
                Message msg = (Message) intent.getSerializableExtra(WUMBO_MESSAGE_EXTRA);
                receive(msg);
            }
        }
    };

    public ChannelManagerImpl(MainActivity context) {
        this.mainContext = context;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WUMBO_MESSAGE_INTENT_ACTION);

        mainContext.setOnStartCallback(new Runnable() {
            @Override
            public void run() {
                mainContext.registerReceiver(receiver, filter);
            }
        });
        mainContext.setOnStopCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    mainContext.unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void receive(Message msg) {
        Log.d("SE464", "ChannelManager receive");
        if (!receivedMessageIds.contains(msg.getId())) {
            Log.d("SE464", "ChannelManager first time receiving");
            send(msg);
            if (channels.containsKey(msg.getChannelId())) {
                channels.get(msg.getChannelId()).receive(msg);
            }
        }
    }

    @Override
    public void send(Message msg) {
        Log.d("SE464", "ChannelManager send");
        receivedMessageIds.add(msg.getId());
        Intent sendMsgIntent = new Intent(mainContext, WifiDirectService.class);
        sendMsgIntent.setAction(WifiDirectService.SEND_MESSAGE_ACTION);
        sendMsgIntent.putExtra(WifiDirectService.EXTRA_MESSAGE, msg);
        mainContext.startService(sendMsgIntent);
    }

    @Override
    public void addChannel(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    @Override
    public void removeChannel(Channel channel) {
        channels.remove(channel.getId());
    }

    public static final String WUMBO_MESSAGE_INTENT_ACTION = "com.star.patrick.wumbo.MESSAGE";
    public static final String WUMBO_MESSAGE_EXTRA = "message";
}
