package com.star.patrick.wumbo.wifidirect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.star.patrick.wumbo.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;

import static com.star.patrick.wumbo.MainActivity.TAG;


public class WifiDirectService extends Service {
    public static final String ADD_PEER_ACTION = "com.star.patrick.wumbo.wifidirect.ADD_PEER";
    public static final String EXTRA_INET_ADDRESS = "clientinetaddress";

    public static final String SEND_MESSAGE_ACTION = "com.star.patrick.wumbo.wifidirect.SEND_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    private WifiP2pManager manager;
    private Channel channel;
    private Device device;

    private boolean inGroup = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SE464", "wifidirect service created");

//        if (manager != null) {
//            return;
//        }

//        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//        wifiManager.setWifiEnabled(false);
//        wifiManager.setWifiEnabled(true);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        channel = manager.initialize(this, getMainLooper(), null);
        discoverPeers();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void deletePersistentGroups(){
        try {
            Class WifiDirectManagerClass = Class.forName("android.net.wifi.p2p.WifiP2pManager");
            Method deletePersistentGroup = WifiDirectManagerClass.getMethod(
                "deletePersistentGroup", Channel.class, int.class, ActionListener.class
            );

            for (int netid = 0; netid < 32; netid++) {
                deletePersistentGroup.invoke(manager, channel, netid, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void discoverPeers() {
        deletePersistentGroups();

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

            if (device.status != WifiP2pDevice.CONNECTED && device.status != WifiP2pDevice.INVITED) {
                manager.connect(channel, config, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully connected to peer.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed to connect to peer. " + reason);
                    }
                });
            }
        }
    }

    private void onGroupFormed(boolean isHost, InetAddress hostAddress) {
        Log.d("SE464", "group formed " + hostAddress + ", Am I host? " + isHost);

        if (isHost) {
            if (!(device instanceof Host)) {
                device = new Host();
            }
        }
        else {
            device = new Client(hostAddress);
        }

        device.onConnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SE464", "wifidirect service intent received");

        if (intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        Log.d("SE464", intent.getAction());

        switch (intent.getAction()) {
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                WifiP2pDeviceList deviceList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                onPeerListChanged(deviceList);
            } break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pGroup p2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                if (networkInfo.isConnected() && p2pInfo.groupFormed) {
                    onGroupFormed(p2pInfo.isGroupOwner, p2pInfo.groupOwnerAddress);
                }
            } break;

            case ADD_PEER_ACTION: {
                InetAddress inetAddress = (InetAddress) intent.getSerializableExtra(EXTRA_INET_ADDRESS);
                if (!(device instanceof Host)) {
                   device = new Host();
                }
                device.addClient(inetAddress);
            } break;

            case SEND_MESSAGE_ACTION: {
                if (device == null) {
                    Log.d(TAG, "Device is null!");
                }
                else {
                    Message message = (Message) intent.getSerializableExtra(EXTRA_MESSAGE);
                    device.sendMessage(message);
                }
            } break;
        }

        return START_NOT_STICKY;
    }
}
