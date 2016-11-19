package com.star.patrick.wumbo.wifidirect;


import android.util.Log;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Message;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Host implements Device {
    private Set<InetAddress> clientAddresses;

    public Host() {
        clientAddresses = new HashSet<>();
    }

    public void addClient(InetAddress address) {
        clientAddresses.add(address);
    }

    @Override
    public void sendMessage(EncryptedMessage message) {
        for (InetAddress address: clientAddresses) {
            Log.d("SE464", "Host sending to " + address.toString());
            MessageSender.sendMessage(address, MessageDispatcherService.PORT, message);
        }
    }

    @Override
    public void onConnect() {}
}
