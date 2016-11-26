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
 * Class receives message broadcasts
 * Has a list of subscribed subscribedMessageReceivers that it calls receive on whenever it gets a broadcast
 */
public class MessageBroadcastReceiver {
    public static final String WUMBO_MESSAGE_INTENT_ACTION = "com.star.patrick.wumbo.MESSAGE";
    public static final String WUMBO_MESSAGE_EXTRA = "message";

    private List<MessageReceiver> subscribedMessageReceivers = new ArrayList<>();

    /**
     * BroadcastReceiver for receiving message broadcasts
     * Calls private receive method for MessageBroadcastReceiver
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Log.d("SE464", "MessageBroadcastReceiver received a message");
        String action = intent.getAction();
        if (WUMBO_MESSAGE_INTENT_ACTION.equals(action)) {
            EncryptedMessage msg =
                    (EncryptedMessage) intent.getSerializableExtra(WUMBO_MESSAGE_EXTRA);
            receive(msg);
        }
        }
    };

    /**
     * Register the receiver on the main start
     * Unregister receiver on the main stop
     */
    public MessageBroadcastReceiver(final MainActivity mainActivity) {
        // Filter for Wumbo messages
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

    /**
     * Add a MessageReceiver to the list of subscribed receivers
     */
    public void add(MessageReceiver msgReceiver) {
        subscribedMessageReceivers.add(msgReceiver);
    }

    /**
     * Pass on the encrypted message to all subscribed receivers
     */
    private void receive(EncryptedMessage msg) {
        for (MessageReceiver msgReceiver : subscribedMessageReceivers) {
            msgReceiver.receive(msg);
        }
    }

}
