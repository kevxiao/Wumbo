package com.star.patrick.wumbo.wifidirect;


import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.net.InetAddress;

/**
 * Interface for device types for Wifi Direct.
 * Known implementors: Host and Client
 */
public interface Device {

    /**
     * called when device connects to a group
     */
    void onConnect();

    /**
     * called when a new client connects
     */
    void addClient(InetAddress inetAddress);

    /**
     * called to send a message to other devices
     */
    void sendMessage(EncryptedMessage message);
}
