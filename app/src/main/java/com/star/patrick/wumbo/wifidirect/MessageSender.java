package com.star.patrick.wumbo.wifidirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MessageSender extends AsyncTask<Void, Void, Throwable> {
    private InetAddress hostAddress;
    private int port;
    private Object object;
    private int numRetries;

    public static MessageSender sendMessage(InetAddress hostAddress, int port, Object object) {
        return sendMessage(hostAddress, port, object, 1);
    }

    public static MessageSender sendMessage(InetAddress hostAddress, int port, Object object, int numRetries) {
        Log.d("SE464", "address: "+hostAddress.toString()+" port: "+port);
        MessageSender messageSender = new MessageSender(hostAddress, port, object, numRetries);
        messageSender.executeOnExecutor(THREAD_POOL_EXECUTOR);
        return messageSender;
    }

    private MessageSender(InetAddress hostAddress, int port, Object object) {
        this(hostAddress, port, object, 1);
    }

    private MessageSender(InetAddress hostAddress, int port, Object object, int numRetries) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.object = object;
        this.numRetries = numRetries;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        Exception exception = null;
        for (int i = 0; i <= numRetries; i++) {
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(hostAddress, port), 5000);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(object);
                Log.d("SE464", "Wrote message to socket");
                return null;
            } catch (IOException e) {
                exception = e;
                e.printStackTrace();
            } finally {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return exception;
    }
}