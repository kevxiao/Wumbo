package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.util.Log;
import com.star.patrick.wumbo.message.EncryptedMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MessageHandler implements Runnable {
    private Socket socket;
    private Context context;

    public MessageHandler(Socket socket, Context context) {
        Log.d("SE464", "Create MessageHandler");
        this.socket = socket;
        this.context = context;
    }

    @Override
    public void run() {
        Log.d("SE464", "MessageHandler thread run");
        try (
            Socket _ = socket;  // workaround for auto-closing without local declaration
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            InetAddress address = socket.getInetAddress();
            Log.d("Client's InetAddress", "" + address);

            EncryptedMessage message = (EncryptedMessage) objectInputStream.readObject();

            WifiDirectServiceAdapter.addPeer(context, address);

            Log.d("SE464", "Message Received: " + message.toString());
            FrontEndCommunicator.receivedMessage(context, message);

            Log.d("SE464", "MessageHandler sent broadcast");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
