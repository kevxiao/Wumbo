package com.star.patrick.wumbo.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import static com.star.patrick.wumbo.MainActivity.TAG;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            // Wifi Direct enabled or disabled
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "Wifi Direct is enabled.");
                }
                else {
                    Log.d(TAG, "Wifi Direct is disabled.");
                }
            } break;

            // available peers changed
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                Log.d(TAG, "Peers changed.");
                Intent newIntent = new Intent(context, WifiDirectService.class);
                newIntent.setAction(intent.getAction());
                newIntent.putExtras(intent);
                context.startService(newIntent);
            } break;

            // group formed
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                Log.d(TAG, "Connection changed.");
                Intent newIntent = new Intent(context, WifiDirectService.class);
                newIntent.setAction(intent.getAction());
                newIntent.putExtras(intent);
                context.startService(newIntent);
            } break;
        }
    }
}
