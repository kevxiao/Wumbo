package com.star.patrick.wumbo.wifidirect;

import android.os.AsyncTask;

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

    public static MessageSender sendMessage(InetAddress hostAddress, int port, Object object) {
        MessageSender messageSender = new MessageSender(hostAddress, port, object);
        messageSender.executeOnExecutor(THREAD_POOL_EXECUTOR);
        return messageSender;
    }

    private MessageSender(InetAddress hostAddress, int port, Object object) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.object = object;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect(new InetSocketAddress(hostAddress, port), 500);
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return e;
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
}