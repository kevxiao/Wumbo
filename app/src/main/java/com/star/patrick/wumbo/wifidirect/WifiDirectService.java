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
import android.util.Base64;
import android.util.Log;

import com.star.patrick.wumbo.Encryption;
import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.User;
import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.model.message.Text;
import com.star.patrick.wumbo.view.MainActivity;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.star.patrick.wumbo.view.MainActivity.TAG;


public class WifiDirectService extends Service {
    public static final String ADD_PEER_ACTION = "com.star.patrick.wumbo.wifidirect.ADD_PEER";
    public static final String EXTRA_INET_ADDRESS = "clientInetAddress";

    public static final String SEND_MESSAGE_ACTION = "com.star.patrick.wumbo.wifidirect.SEND_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    private WifiP2pManager manager;
    private Channel channel;
    private Device device;

    private Runnable onSendFailure;

    private RestartableEvent reconnectTask;
    private PausableRecurringEvent tryConnectTask;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SE464", "wifidirect service created");

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        onSendFailure = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Send failed, disconnecting and reconnecting");
                reconnectTask.restart(30);
            }
        };

        reconnectTask = new RestartableEvent(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        });
        reconnectTask.restart(0);

        tryConnectTask = new PausableRecurringEvent(new Runnable() {
            @Override
            public void run() {
                tryConnect();
            }
        }, 0, 5, TimeUnit.SECONDS, false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void disconnect(){
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
        manager.clearLocalServices(channel, null);
        manager.clearServiceRequests(channel, null);
        manager.removeGroup(channel, null);
//        manager.stopPeerDiscovery(channel, null);

//        try {
//            Class WifiDirectManagerClass = Class.forName("android.net.wifi.p2p.WifiP2pManager");
//            Method deletePersistentGroup = WifiDirectManagerClass.getMethod(
//                "deletePersistentGroup",
//                WifiP2pManager.Channel.class,
//                int.class,
//                WifiP2pManager.ActionListener.class
//            );
//
//            for (int netId = 0; netId < 32; netId++) {
//                deletePersistentGroup.invoke(manager, channel, netId, null);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void reconnect() {
        disconnect();

        tryConnectTask.resume();

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

    private void tryConnect() {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupFormed) {
                    onGroupFormed(info.isGroupOwner, info.groupOwnerAddress);
                }
            }
        });
    }

    private void connectToPeers(WifiP2pDeviceList peers) {
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
                        tryConnectTask.resume();
                    }
                });
            }
        }
    }

    private void onGroupFormed(boolean isHost, InetAddress hostAddress) {
        tryConnectTask.pause();

        reconnectTask.restart(30);

        Log.d("SE464", "group formed " + hostAddress + ", Am I host? " + isHost);

        Message message = new Message(
            new Text("Connected"),
            new User("System", null),
            new Timestamp(
                Calendar.getInstance().getTime().getTime()
            ),
            UUID.fromString(getResources().getString(R.string.public_uuid))
        );

        SecretKey key = Encryption.getSecretKeyFromEncoding(getResources().getString(R.string.public_secret_key));

        EncryptedMessage encryptedMessage = new EncryptedMessage(message, key);
        FrontEndCommunicator.receivedMessage(this, encryptedMessage);

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
                connectToPeers(deviceList);
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
                   device = new Host(onSendFailure);
                }
                device.addClient(inetAddress);
            } break;

            case SEND_MESSAGE_ACTION: {
                if (device == null) {
                    Log.d(TAG, "Device is null!");
                }
                else {
                    EncryptedMessage message = (EncryptedMessage) intent.getSerializableExtra(EXTRA_MESSAGE);
                    device.sendMessage(message);
                }
            } break;
        }

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        manager.stopPeerDiscovery(channel, null);
    }
}
