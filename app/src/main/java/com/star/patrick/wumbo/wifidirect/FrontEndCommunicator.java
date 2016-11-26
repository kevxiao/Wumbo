package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;

import com.star.patrick.wumbo.MessageBroadcastReceiver;
import com.star.patrick.wumbo.model.message.EncryptedMessage;

/**
 * Adapter functions that allows the backend that deals with
 * Wifi Direct to easily communicate with the front end user interface.
 */
public class FrontEndCommunicator {

    /**
     * Sends a newly received message to the front end after receiving it from
     * another device using the Wifi Direct Library.
     */
    public static void receivedMessage(Context context, EncryptedMessage message) {
        Intent messageIntent = new Intent(MessageBroadcastReceiver.WUMBO_MESSAGE_INTENT_ACTION);
        messageIntent.putExtra(MessageBroadcastReceiver.WUMBO_MESSAGE_EXTRA, message);
        context.sendBroadcast(messageIntent);
    }
}
