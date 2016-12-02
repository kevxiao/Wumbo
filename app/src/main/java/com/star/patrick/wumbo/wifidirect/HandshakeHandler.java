package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Thread that handles handshakes from client devices
 */
public class HandshakeHandler implements Runnable {
    private Socket socket;
    private Context context;

    public HandshakeHandler(Socket socket, Context context){
        Log.d("SE4664", "Client accepted socket");
        this.socket = socket;
        this.context = context;
    }

    /**
     * gets clients' IP Address and saves it to the host device
     */
    @Override
    public void run(){
        // workaround for auto-closing without local declaration
        try (Socket ignored = socket) {
            InetAddress clientAddress = socket.getInetAddress();

            Log.d("Client's InetAddress", "" + clientAddress);

            WifiDirectServiceAdapter.addPeer(context, clientAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
