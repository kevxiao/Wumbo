package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


/**
 *  Handler that receives messages from the thread
 */
public class HandshakeHandler implements Runnable {
    private Socket socket;
    private Context context;

    HandshakeHandler(Socket socket, Context context){
        Log.d("SE4664", "Client accepted socket");
        this.socket = socket;
        this.context = context;
    }

    @Override
    public void run(){
        // workaround for auto-closing without local declaration
        try (Socket __ = socket) {
            InetAddress clientAddress = socket.getInetAddress();

            Log.d("Client's InetAddress", "" + clientAddress);

            WifiDirectServiceAdapter.addPeer(context, clientAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
