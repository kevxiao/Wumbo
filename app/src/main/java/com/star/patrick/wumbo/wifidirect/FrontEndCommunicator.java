package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;

import com.star.patrick.wumbo.ChannelManagerImpl;
import com.star.patrick.wumbo.message.Message;

public class FrontEndCommunicator {
    public static void receivedMessage(Context context, Message message) {
        Intent messageIntent = new Intent(ChannelManagerImpl.WUMBO_MESSAGE_INTENT_ACTION);
        messageIntent.putExtra(ChannelManagerImpl.WUMBO_MESSAGE_EXTRA, message);
        context.sendBroadcast(messageIntent);
    }
}
