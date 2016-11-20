package com.star.patrick.wumbo.wifidirect;


import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.net.InetAddress;

public interface Device {
    void onConnect();
    void addClient(InetAddress inetAddress);
    void sendMessage(EncryptedMessage message);
}
