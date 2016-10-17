package com.star.patrick.wumbo.wifidirect;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giliam on 10/16/2016.
 */

public class MessageDispatcherService extends IntentService {
    private List<Thread> threadList = new ArrayList<>();
    public final static int PORT = 63244;
    private ServerSocket serverSocket;

    public MessageDispatcherService() {
        super("MessageDispatcherService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
//
//        serviceThread.getLooper();
//        serviceHandler = new ServiceHandler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("SE464", "message service starting");

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
    }

    @Override
    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
