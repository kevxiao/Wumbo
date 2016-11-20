package com.star.patrick.wumbo.wifidirect;


import android.util.Log;

import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Host implements Device {
    private Set<InetAddress> clientAddresses;
    private Runnable onSendFailure;

    public Host(Runnable onSendFailure) {
        clientAddresses = new HashSet<>();
        this.onSendFailure = onSendFailure;
    }

    public void addClient(InetAddress address) {
        clientAddresses.add(address);
    }

    @Override
    public void sendMessage(EncryptedMessage message) {
        for (InetAddress address: clientAddresses) {
            Log.d("SE464", "Host sending to " + address.toString());
            new MessageSender(address, MessageDispatcherService.PORT, message, onSendFailure);
        }
    }

    @Override
    public void onConnect() {}
}
