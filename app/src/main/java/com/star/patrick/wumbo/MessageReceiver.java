package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.star.patrick.wumbo.message.EncryptedMessage;

/**
 * Created by giliam on 11/19/2016.
 */

public class MessageReceiver {
    public static final String WUMBO_MESSAGE_INTENT_ACTION = "com.star.patrick.wumbo.MESSAGE";
    public static final String WUMBO_MESSAGE_EXTRA = "message";

    private final MessageCourier messageCourier;
    private final ChannelManager channelManager;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SE464", "Channel received a message");
            String action = intent.getAction();
            if (WUMBO_MESSAGE_INTENT_ACTION.equals(action)) {
                EncryptedMessage msg = (EncryptedMessage) intent.getSerializableExtra(WUMBO_MESSAGE_EXTRA);
                receive(msg);
            }
        }
    };

    public MessageReceiver(final MainActivity mainActivity, MessageCourier messageCourier, ChannelManager channelManager) {
        this.messageCourier = messageCourier;
        this.channelManager = channelManager;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WUMBO_MESSAGE_INTENT_ACTION);

        mainActivity.setOnStartCallback(new Runnable() {
            @Override
            public void run() {
                mainActivity.registerReceiver(receiver, filter);
            }
        });
        mainActivity.setOnStopCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    mainActivity.unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void receive(EncryptedMessage msg) {
        //let channel manager decide if there are channels that need to receive this
        channelManager.receive(msg);

        //forward all received messages
        messageCourier.send(msg);
    }

}
