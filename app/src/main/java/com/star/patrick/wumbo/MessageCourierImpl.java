package com.star.patrick.wumbo;

import android.content.Intent;
import android.util.Log;

import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.view.MainActivity;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageCourierImpl implements MessageCourier {
    private MainActivity mainContext;
    private Set<UUID> sentMessages = new HashSet<>();

    public MessageCourierImpl(MainActivity mainContext) {
        this.mainContext = mainContext;
    }

    @Override
    public void send(EncryptedMessage msg) {
        if (!sentMessages.contains(msg.getId())) {
            Log.d("SE464", "Haven't sent this message before: " + msg.getId());
            sentMessages.add(msg.getId());
            Intent sendMsgIntent = new Intent(mainContext, WifiDirectService.class);
            sendMsgIntent.setAction(WifiDirectService.SEND_MESSAGE_ACTION);
            sendMsgIntent.putExtra(WifiDirectService.EXTRA_MESSAGE, msg);
            mainContext.startService(sendMsgIntent);
        }
    }

    @Override
    public void receive(EncryptedMessage msg) {
        send(msg);
    }
}
