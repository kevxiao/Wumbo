package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by giliam on 11/15/2016.
 */

public class ChannelManagerImpl implements ChannelManager {
    private MainActivity mainContext;
    private ChannelList channels = new ChannelListImpl();

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
