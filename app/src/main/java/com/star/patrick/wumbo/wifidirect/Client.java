package com.star.patrick.wumbo.wifidirect;


import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.net.InetAddress;



public class Client implements Device {
    private InetAddress hostAddress;
    private Runnable onSendFailure;

    public Client(InetAddress hostAddress, Runnable onSendFailure) {
        this.hostAddress = hostAddress;
        this.onSendFailure = onSendFailure;
    }

    @Override
    public void onConnect() {
        new MessageSender(hostAddress, HandshakeDispatcherService.PORT, "HANDSHAKE", onSendFailure, 5);
    }

    @Override
    public void addClient(InetAddress inetAddress) {}

    @Override
    public void sendMessage(EncryptedMessage message) {
        new MessageSender(hostAddress, MessageDispatcherService.PORT, message, onSendFailure);
    }
}
