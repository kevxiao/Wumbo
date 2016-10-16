package com.star.patrick.wumbo.wifidirect;


import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.os.Process;

import java.net.ServerSocket;
import java.net.Socket;

public class HandshakeDispatcherService extends IntentService {
    private Looper mServiceLooper;
    private ServerSocket serverSocket;
    public static final int PORT = 45454;

    public HandshakeDispatcherService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "HandshakeDispatcherService starting", Toast.LENGTH_SHORT).show();

        try {
            serverSocket = new ServerSocket(PORT);
            while (!Thread.currentThread().isInterrupted()) {
                HandshakeHandler thread = new HandshakeHandler(serverSocket.accept(), this);
                thread.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}