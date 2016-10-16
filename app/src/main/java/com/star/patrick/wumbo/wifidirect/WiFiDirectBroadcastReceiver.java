package com.star.patrick.wumbo.wifidirect;

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
import android.os.AsyncTask;
import android.util.Log;

import com.star.patrick.wumbo.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
                        private static final int PORT = 8988;

                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                            // InetAddress from WifiP2pInfo struct.
                            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

                            if (info.groupFormed) {
                                new AsyncTask<Void, Integer, String>() {
                                    @Override
                                    protected String doInBackground(Void... params) {
                                        if (info.isGroupOwner) {
                                            ServerSocket serverSocket = null;
                                            Socket socket = null;
                                            InputStream inputStream = null;
                                            ObjectInputStream objectInputStream = null;
                                            try {
                                                serverSocket = new ServerSocket(PORT);
                                                socket = serverSocket.accept();
                                                Log.d("Client's InetAddress", "" + socket.getInetAddress());
                                                inputStream = socket.getInputStream();
                                                objectInputStream = new ObjectInputStream(inputStream);
                                                return (String) objectInputStream.readObject();
                                            } catch (IOException | ClassNotFoundException e) {
                                                e.printStackTrace();
                                                return "";
                                            } finally {
                                                if (objectInputStream != null) {
                                                    try {
                                                        objectInputStream.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                if (socket != null) {
                                                    try {
                                                        socket.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                if (serverSocket != null) {
                                                    try {
                                                        serverSocket.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            Socket socket = null;
                                            OutputStream outputStream = null;
                                            ObjectOutputStream objectOutputStream = null;
                                            try {
                                                socket = new Socket();
                                                socket.bind(null);
                                                socket.connect(new InetSocketAddress(groupOwnerAddress, PORT), 500);
                                                outputStream = socket.getOutputStream();
                                                objectOutputStream = new ObjectOutputStream(outputStream);
                                                objectOutputStream.writeObject("davidsu1995");
                                                return "";
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return "";
                                            } finally {
                                                if (objectOutputStream != null) {
                                                    try {
                                                        objectOutputStream.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                if (outputStream != null) {
                                                    try {
                                                        outputStream.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                if (socket != null) {
                                                    try {
                                                        socket.close();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(String s) {
                                        Log.d(TAG, "Message received from client: " + s);
                                    }
                                }.execute();
                            }

                        }
                    });
                }
            } break;
        }
    }
}
