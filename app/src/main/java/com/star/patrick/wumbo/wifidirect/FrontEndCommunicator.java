package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;

import com.star.patrick.wumbo.MessageReceiver;
import com.star.patrick.wumbo.message.EncryptedMessage;

public class FrontEndCommunicator {
    public static void receivedMessage(Context context, EncryptedMessage message) {
        Intent messageIntent = new Intent(MessageReceiver.WUMBO_MESSAGE_INTENT_ACTION);
        messageIntent.putExtra(MessageReceiver.WUMBO_MESSAGE_EXTRA, message);
        context.sendBroadcast(messageIntent);
    }
}
