package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;

import java.net.InetAddress;

/**
 * Allows other threads to interact with WifiDirectService through intents
 */
public class WifiDirectServiceAdapter {

    /**
     * Adds a newly discovered peer
     */
    public static void addPeer(Context context, InetAddress address) {
        Intent intent = new Intent(context, WifiDirectService.class);
        intent.setAction(WifiDirectService.ADD_PEER_ACTION);
        intent.putExtra(WifiDirectService.EXTRA_INET_ADDRESS, address);
        context.startService(intent);
    }
}
