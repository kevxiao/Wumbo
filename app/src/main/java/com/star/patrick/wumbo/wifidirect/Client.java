package com.star.patrick.wumbo.wifidirect;


import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;


public class Client implements Device {
    private InetAddress hostAddress;

    public Client(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Override
    public void onConnect() {
        new HandshakeInitiator().executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    public void addClient(InetAddress inetAddress) {}

    private class HandshakeInitiator extends AsyncTask<Void, Void, Throwable> {
        @Override
        protected Throwable doInBackground(Void... params) {
            Socket socket = null;
            OutputStream outputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(hostAddress, HandshakeDispatcherService.PORT), 500);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject("HANDSHAKE");
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
}
