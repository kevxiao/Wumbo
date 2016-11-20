package com.star.patrick.wumbo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.star.patrick.wumbo.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jesse on 15/10/16.
 */

public class PeerListFragment implements WifiP2pManager.PeerListListener{
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device;

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void clearPeers() {
        peers.clear();
        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(MainActivity.TAG, peers.toString());
            return;
        }
    }
}
