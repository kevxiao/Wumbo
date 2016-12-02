package com.star.patrick.wumbo.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Handles incoming handshake requests and spawns a new thread to handle each new request
 */
public class HandshakeDispatcherService extends IntentService {
    /**
     * Port that accepts all incoming handshake connections
     */
    public static final int PORT = 63244;

    public HandshakeDispatcherService() {
        super("HandshakeDispatcherService");
    }

    /**
     * Accept incoming connections and spawns new HandshakeHandlers
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("SE464", "message service starting");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket incomingSocket = serverSocket.accept();
                Thread thread = new Thread(new HandshakeHandler(incomingSocket, this));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}