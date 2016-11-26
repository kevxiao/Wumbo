package com.star.patrick.wumbo.wifidirect;


import android.content.Context;
import android.util.Log;

import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Thread that handles incoming messages and sends them to the front end
 */
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
            Socket ignored = socket;  // workaround for auto-closing without local declaration
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            InetAddress address = socket.getInetAddress();
            Log.d("Client's InetAddress", "" + address);

            EncryptedMessage message = (EncryptedMessage) objectInputStream.readObject();

            // adds original message sender to list of peers
            WifiDirectServiceAdapter.addPeer(context, address);

            // sends newly received message to front end
            Log.d("SE464", "Message Received: " + message.toString());
            FrontEndCommunicator.receivedMessage(context, message);

            Log.d("SE464", "MessageHandler sent broadcast");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
