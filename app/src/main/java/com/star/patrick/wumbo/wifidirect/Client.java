package com.star.patrick.wumbo.wifidirect;


import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.net.InetAddress;


/**
 * Class representing a P2P client device for Wifi Direct
 */
public class Client implements Device {
    private InetAddress hostAddress;
    private Runnable onSendFailure;

    public Client(InetAddress hostAddress, Runnable onSendFailure) {
        this.hostAddress = hostAddress;
        this.onSendFailure = onSendFailure;
    }

    /**
     * On connect, send a handshake message to the host.
     * This allows the host to get the client's IP Address.
     * This allows bidirectional communication between the client and the server,
     * since the host does not have immediate access to the client's IP Addresses
     * until the client sends a message to the host.
     */
    @Override
    public void onConnect() {
        new MessageSender(
            hostAddress, HandshakeDispatcherService.PORT, "HANDSHAKE",
            onSendFailure, 5
        );
    }

    /**
     * Client should not be able to register another client
     */
    @Override
    public void addClient(InetAddress inetAddress) {}

    /**
     * All clients send messages to host, host then relays messages to other clients.
     */
    @Override
    public void sendMessage(EncryptedMessage message) {
        new MessageSender(hostAddress, MessageDispatcherService.PORT, message, onSendFailure);
    }
}
