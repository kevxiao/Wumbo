package com.star.patrick.wumbo.wifidirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Async Message Sender
 */
public class MessageSender extends AsyncTask<Void, Void, Exception> {
    private InetAddress hostAddress;
    private int port;
    private Object object;
    private int numRetries;
    private Runnable onFailure;

    /**
     * Send timeout
     */
    public static final int TIMEOUT_MS = 1000;

    public MessageSender(InetAddress hostAddress, int port, Object object, Runnable onFailure) {
        this(hostAddress, port, object, onFailure, 1);
    }

    /**
     * Sends the message to the hostAddress using a background thread pool
     */
    public MessageSender(InetAddress hostAddress, int port, Object object,
                         Runnable onFailure, int numRetries) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.object = object;
        this.numRetries = numRetries;
        this.onFailure = onFailure;

        Log.d("SE464", "address: "+hostAddress.toString()+" port: "+port);

        executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    /**
     * repeatedly try to send the message numRetries times before giving up
     */
    @Override
    protected Exception doInBackground(Void... params) {
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
            }
        }
        return exception;
    }

    /**
     * Runs the onFailure callback if an exception occurred during message send
     */
    @Override
    protected void onPostExecute(Exception e) {
        if (e != null && onFailure != null) {
            onFailure.run();
        }
    }
}