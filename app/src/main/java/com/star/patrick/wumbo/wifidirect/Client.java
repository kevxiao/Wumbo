package com.star.patrick.wumbo.wifidirect;


import java.net.InetAddress;

import com.star.patrick.wumbo.message.EncryptedMessage;



public class Client implements Device {
    private InetAddress hostAddress;

    public Client(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Override
    public void onConnect() {
        new MessageSender(hostAddress, HandshakeDispatcherService.PORT, "HANDSHAKE", 5);
    }

    @Override
    public void addClient(InetAddress inetAddress) {}

    @Override
    public void sendMessage(EncryptedMessage message) {
        new MessageSender(hostAddress, MessageDispatcherService.PORT, message);
    }
}
