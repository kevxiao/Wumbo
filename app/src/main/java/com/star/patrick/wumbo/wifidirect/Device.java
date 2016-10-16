package com.star.patrick.wumbo.wifidirect;


import java.net.InetAddress;

public interface Device {
    void onConnect();
    void addClient(InetAddress inetAddress);
}
