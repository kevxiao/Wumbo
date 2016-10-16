package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.star.patrick.wumbo.MainActivity.TAG;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "Wifi Direct is enabled.");
                }
                else {
                    Log.d(TAG, "Wifi Direct is disabled.");
                }
            } break;

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                Log.d(TAG, "Peers changed.");
                mManager.requestPeers(mChannel, new PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d(TAG, "Peers found: "+ peers.toString());

                        //obtain a peer from the WifiP2pDeviceList
                        WifiP2pDevice device = peers.getDeviceList().iterator().next();
                        WifiP2pConfig config = new WifiP2pConfig();
                        Log.d(TAG, "Peers found: "+ device.deviceAddress);
                        config.deviceAddress = device.deviceAddress;
                        mManager.connect(mChannel, config, new ActionListener() {
                            @Override
                            public void onSuccess() {
                                //success logic
                                Log.d(TAG, "Successfully connected to peer.");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //failure logic
                                Log.d(TAG, "Failed to connect to peer.");
                            }
                        });
                    }
                });
            } break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                if (mManager == null) {
                    return;
                }

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            // InetAddress from WifiP2pInfo struct.
                            InetAddress groupOwnerAddress = info.groupOwnerAddress;

                            // After the group negotiation, we can determine the group owner.
                            if (info.groupFormed) {
                                if (info.isGroupOwner) {
                                    // is group owner
                                    // group owner doesn't have IP Addresses of clients
                                }
                                else {
                                    // is client
                                    // clients have IP Address of owner
                                }
                            }
                        }
                    });
                }
            } break;
        }
    }
}
