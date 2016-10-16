package com.star.patrick.wumbo.wifidirect;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;

import java.net.InetAddress;

import static com.star.patrick.wumbo.MainActivity.TAG;


public class WifiDirectService extends IntentService {
    public static final String ADD_PEER_ACTION = "com.star.patrick.wumbo.wifidirect.ADD_PEER_ACTION";
    public static final String EXTRA_INET_ADDRESS = "clientinetaddress";

    private WifiP2pManager manager;
    private Channel channel;

    private Device device;

    public WifiDirectService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        discoverPeers();
    }

    private void discoverPeers() {
        manager.discoverPeers(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Peer discovery initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Peer discovery initiation failed");
            }
        });
    }

    private void onPeerListChanged(WifiP2pDeviceList peers) {
        Log.d(TAG, "Peers found: "+ peers.toString());

        //obtain a peer from the WifiP2pDeviceList
        for (WifiP2pDevice device: peers.getDeviceList()) {
            WifiP2pConfig config = new WifiP2pConfig();
            Log.d(TAG, "Peers found: " + device.deviceAddress);
            config.deviceAddress = device.deviceAddress;

            manager.connect(channel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Successfully connected to peer.");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Failed to connect to peer.");
                }
            });
        }
    }

    private void onGroupFormed(boolean isHost, InetAddress hostAddress) {
        if (isHost) {
            device = new Host();
        }
        else {
            device = new Client(hostAddress);
        }

        device.onConnect();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                manager.requestPeers(channel, new PeerListListener(){
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        onPeerListChanged(peers);
                    }
                });
            } break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                manager.requestConnectionInfo(channel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                        if (info.groupFormed) {
                            onGroupFormed(info.isGroupOwner, info.groupOwnerAddress);
                        }
                    }
                });
            } break;

            case ADD_PEER_ACTION: {
                InetAddress inetAddress = (InetAddress) intent.getSerializableExtra(EXTRA_INET_ADDRESS);
                device.addClient(inetAddress);
            } break;
        }
    }
}
