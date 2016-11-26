package com.star.patrick.wumbo.wifidirect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.model.message.Message;

import java.net.InetAddress;

import static com.star.patrick.wumbo.view.MainActivity.TAG;


public class WifiDirectService extends Service {
    public static final String ADD_PEER_ACTION = "com.star.patrick.wumbo.wifidirect.ADD_PEER";
    public static final String EXTRA_INET_ADDRESS = "clientInetAddress";

    public static final String SEND_MESSAGE_ACTION = "com.star.patrick.wumbo.wifidirect.SEND_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    public static final int GROUP_FORMED_CHECK_PERIOD = 5000;

    private WifiP2pManager manager;
    private Channel channel;
    private Device device;

    private int requestConnectionInfoCount = 0;
    private Runnable onSendFailure = new Runnable() {
        @Override
        public void run() {
            checkGroupFormed();
        }
    };

    /**
     * Starts looking for peers
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SE464", "wifidirect service created");

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        reconnect();
        checkGroupFormed();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Disconnects from all peers
     */
    private void disconnect(){
        manager.cancelConnect(channel, null);
        manager.clearLocalServices(channel, null);
        manager.clearServiceRequests(channel, null);
        manager.removeGroup(channel, null);
    }

    /**
     * Disconnects from all peers, and attempts to reconnect to new peers
     */
    private void reconnect() {
        disconnect();

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

    /**
     * Periodically checks if a new group has formed
     */
    private void checkGroupFormed() {
        requestConnectionInfoCount++;
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                requestConnectionInfoCount--;
                if (requestConnectionInfoCount > 0) {
                    return;
                }

                if (info.groupFormed) {
                    onGroupFormed(info.isGroupOwner, info.groupOwnerAddress);
                }
                else {
                    Thread nextCheck = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(GROUP_FORMED_CHECK_PERIOD);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkGroupFormed();
                        }
                    });
                    nextCheck.start();
                }
            }
        });
    }

    /**
     * Called when new peers have been discovered. Attempts to connect to them.
     */
    private void connectToNewPeers(WifiP2pDeviceList peers) {
        Log.d(TAG, "Peers found: "+ peers.toString());

        for (WifiP2pDevice device: peers.getDeviceList()) {
            WifiP2pConfig config = new WifiP2pConfig();
            Log.d(TAG, "Peers found: " + device.deviceAddress);
            config.deviceAddress = device.deviceAddress;

            // connect to devices that have not already been connected to
            if (device.status != WifiP2pDevice.CONNECTED &&
                device.status != WifiP2pDevice.INVITED) {
                manager.connect(channel, config, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully connected to peer.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed to connect to peer. " + reason);
                        // cancel connection if it failed
                        manager.cancelConnect(channel, new ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Cancelled connection");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "Failed to cancel "+reason);
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * When a group has formed, determines if this device is a host or a client,
     * and creates the appropriate class for the device type
     */
    private void onGroupFormed(boolean isHost, InetAddress hostAddress) {
        Log.d("SE464", "group formed " + hostAddress + ", Am I host? " + isHost);

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        if (isHost) {
            if (!(device instanceof Host)) {
                device = new Host(onSendFailure);
            }
        }
        else {
            device = new Client(hostAddress, onSendFailure);
        }

        device.onConnect();
    }

    /**
     * adds new peers
     */
    private void addPeer(InetAddress inetAddress) {
        if (!(device instanceof Host)) {
            device = new Host(onSendFailure);
        }
        device.addClient(inetAddress);
    }

    /**
     * sends messages received from the front end to other devices 
     */
    private void sendMessage(EncryptedMessage message) {
        if (device == null) {
            Log.d(TAG, "Device is null!");
        }
        else {
            device.sendMessage(message);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SE464", "wifidirect service intent received");

        if (intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        Log.d("SE464", intent.getAction());

        switch (intent.getAction()) {
            // list of peers changed => connect to new peers
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                WifiP2pDeviceList deviceList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                connectToNewPeers(deviceList);
            } break;

            // group formed => determine if this device is host or client
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pGroup p2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                if (networkInfo.isConnected() && p2pInfo.groupFormed) {
                    onGroupFormed(p2pInfo.isGroupOwner, p2pInfo.groupOwnerAddress);
                }
            } break;

            // adds new peers
            case ADD_PEER_ACTION: {
                InetAddress inetAddress = (InetAddress) intent.getSerializableExtra(EXTRA_INET_ADDRESS);
                addPeer(inetAddress);
            } break;

            // sends messages received from the front end
            case SEND_MESSAGE_ACTION: {
                EncryptedMessage message = (EncryptedMessage) intent.getSerializableExtra(EXTRA_MESSAGE);
                sendMessage(message);
            } break;
        }

        return START_NOT_STICKY;
    }

    /**
     * Stops connecting to peers when service is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        manager.stopPeerDiscovery(channel, null);
    }
}
