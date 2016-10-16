package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.Socket;

// Handler that receives messages from the thread
public final class HandshakeHandler extends Thread {
    private Socket socket;
    Context context;
    HandshakeHandler(Socket s, Context c){
        context = c;
        socket = s;
    }
    public void run(){
        Log.d("Client's InetAddress", "" + socket.getInetAddress());
        //TODO Change the intent's class to the right one
        Intent intent = new Intent(context, WiFiDirectService.class);
        intent.setAction(WifiDirectService.ADD_PEER_ACTION);
        intent.putExtra(WifiDirectService.EXTRA_INET_ADDRESS, socket.getInetAddress());
        context.startService(intent);
    }
}
