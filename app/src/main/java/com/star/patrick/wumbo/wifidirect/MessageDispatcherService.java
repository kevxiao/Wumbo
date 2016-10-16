package com.star.patrick.wumbo.wifidirect;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giliam on 10/16/2016.
 */

public class MessageDispatcherService extends Service {
    private List<Thread> threadList = new ArrayList<>();
    private final static int PORT = 45455;

    @Override
    public void onCreate() {
        HandlerThread serviceThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
//
//        serviceThread.getLooper();
//        serviceHandler = new ServiceHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "message service starting", Toast.LENGTH_SHORT).show();

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Thread thread = new Thread(new MessageHandler(serverSocket.accept(), this));
                threadList.add(thread);
                thread.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
