package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;



/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    activity.displaySnackbar("Wifi Direct is enabled!");
                }
                else {
                    activity.displaySnackbar("Wifi Direct is disabled!");
                }
            } break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                manager.requestPeers(channel, new PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        activity.displaySnackbar("Peers discovered: " + peers.toString());

                        //obtain a peer from the WifiP2pDeviceList
                        WifiP2pDevice device = peers.getDeviceList().iterator().next();
                        WifiP2pConfig config = new WifiP2pConfig();
                        activity.displaySnackbar("Peer is: " + device.deviceAddress);
                        config.deviceAddress = device.deviceAddress;
                        manager.connect(channel, config, new ActionListener() {
                            @Override
                            public void onSuccess() {
                                //success logic
                                activity.displaySnackbar("Successfully connected to peer.");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //failure logic
                                activity.displaySnackbar("Failed to connect to peer.");
                            }
                        });
                    }
                });
            } break;
        }
    }

}