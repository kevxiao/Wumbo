package com.star.patrick.wumbo.wifidirect;


import android.content.Intent;
import android.util.Log;
import com.star.patrick.wumbo.ChannelManagerImpl;
import com.star.patrick.wumbo.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MessageHandler implements Runnable {
    private Socket socket;
    private MessageDispatcherService messageDispatcherService;

    public MessageHandler(Socket socket, MessageDispatcherService messageDispatcherService) {
        Log.d("SE464", "Creat MessageHandler");
        this.socket = socket;
        this.messageDispatcherService = messageDispatcherService;
    }

    @Override
    public void run() {
        Log.d("SE464", "MessageHandler thread run");
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {

            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            Message msg = (Message) objectInputStream.readObject();

            Log.d("Client's InetAddress", "" + socket.getInetAddress());
            //TODO Change the intent's class to the right one
            Intent intent = new Intent(messageDispatcherService, WifiDirectService.class);
            intent.setAction(WifiDirectService.ADD_PEER_ACTION);
            intent.putExtra(WifiDirectService.EXTRA_INET_ADDRESS, socket.getInetAddress());
            messageDispatcherService.startService(intent);

            Log.d("SE464", "Messsage Received: " + msg.toString());

            Intent messageIntent = new Intent(ChannelManagerImpl.WUMBO_MESSAGE_INTENT_ACTION);
            messageIntent.putExtra(ChannelManagerImpl.WUMBO_MESSAGE_EXTRA, msg);
            messageDispatcherService.sendBroadcast(messageIntent);

            Log.d("SE464", "MessageHandler sent broadcast");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
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
        }   //finally
    }
}
