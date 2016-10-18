package com.star.patrick.wumbo.wifidirect;


import java.net.InetAddress;

import com.star.patrick.wumbo.Message;



public class Client implements Device {
    private InetAddress hostAddress;

    public Client(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Override
    public void onConnect() {
        MessageSender.sendMessage(hostAddress, HandshakeDispatcherService.PORT, "HANDSHAKE", 5);
    }

    @Override
    public void addClient(InetAddress inetAddress) {}

    @Override
    public void sendMessage(Message message) {
        MessageSender.sendMessage(hostAddress, MessageDispatcherService.PORT, message);
    }
}
