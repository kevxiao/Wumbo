package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giliam on 11/19/2016.
 */

public class MessageBroadcastReceiver {
    public static final String WUMBO_MESSAGE_INTENT_ACTION = "com.star.patrick.wumbo.MESSAGE";
    public static final String WUMBO_MESSAGE_EXTRA = "message";

    private List<MessageReceiver> messageReceivers = new ArrayList<>();

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

    public MessageBroadcastReceiver(final MainActivity mainActivity) {
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

    public void add(MessageReceiver msgReceiver) {
        messageReceivers.add(msgReceiver);
    }

    private void receive(EncryptedMessage msg) {
        for (MessageReceiver msgReceiver : messageReceivers) {
            msgReceiver.receive(msg);
        }
    }

}
