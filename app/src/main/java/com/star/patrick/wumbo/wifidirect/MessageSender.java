package com.star.patrick.wumbo.wifidirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MessageSender extends AsyncTask<Void, Void, Throwable> {
    private InetAddress hostAddress;
    private int port;
    private Object object;
    private int numRetries;

    public static final int TIMEOUT_MS = 1000;

    public MessageSender(InetAddress hostAddress, int port, Object object) {
        this(hostAddress, port, object, 1);
    }

    public MessageSender(InetAddress hostAddress, int port, Object object, int numRetries) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.object = object;
        this.numRetries = numRetries;

        Log.d("SE464", "address: "+hostAddress.toString()+" port: "+port);

        executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        Exception exception = null;

        for (int i = 0; i <= numRetries; i++) {
            try (Socket socket = new Socket()) {
                socket.bind(null);
                socket.connect(new InetSocketAddress(hostAddress, port), TIMEOUT_MS);
                try (
                    OutputStream outputStream = socket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
                ) {
                    objectOutputStream.writeObject(object);
                    Log.d("SE464", "Wrote message to socket");
                    return null;
                }
            } catch (IOException e) {
                exception = e;
                e.printStackTrace();
            }
        }
        return exception;
    }
}