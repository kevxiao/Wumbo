package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.Socket;

// Handler that receives messages from the thread
public final class HandshakeHandler implements Runnable {
    private Socket socket;
    private Context context;
    HandshakeHandler(Socket s, Context c){
        Log.d("SE4664", "Client accepted socket");
        context = c;
        socket = s;
    }
    @Override
    public void run(){
        Log.d("Client's InetAddress", "" + socket.getInetAddress());
        //TODO Change the intent's class to the right one
        Intent intent = new Intent(context, WifiDirectService.class);
        intent.setAction(WifiDirectService.ADD_PEER_ACTION);
        intent.putExtra(WifiDirectService.EXTRA_INET_ADDRESS, socket.getInetAddress());
        context.startService(intent);
        try {
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
