package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;

import java.net.InetAddress;

public class WifiDirectServiceAdapter {

    public static void addPeer(Context context, InetAddress address) {
        Intent intent = new Intent(context, WifiDirectService.class);
        intent.setAction(WifiDirectService.ADD_PEER_ACTION);
        intent.putExtra(WifiDirectService.EXTRA_INET_ADDRESS, address);
        context.startService(intent);
    }

}
