package com.star.patrick.wumbo.wifidirect;


import com.star.patrick.wumbo.message.Message;

import java.net.InetAddress;

public interface Device {
    void onConnect();
    void addClient(InetAddress inetAddress);
    void sendMessage(Message message);
}
