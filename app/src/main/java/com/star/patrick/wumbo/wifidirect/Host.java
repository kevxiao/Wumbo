package com.star.patrick.wumbo.wifidirect;


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
    public void onConnect() {

    }
}
