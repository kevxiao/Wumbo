package com.star.patrick.wumbo;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.view.MainActivity;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageCourierImpl implements MessageCourier {
    private MainActivity mainContext;
    private Set<UUID> sentMessages = new HashSet<>();

    public MessageCourierImpl(MainActivity mainContext) {
        this.mainContext = mainContext;
    }

    @Override
    public void send(EncryptedMessage msg) {
        if (!sentMessages.contains(msg.getId())) {
            Log.d("SE464", "Haven't sent this message before: " + msg.getId());
            sentMessages.add(msg.getId());
            String fileName = msg.getId().toString() + "-emsg.tmp";
            ContextWrapper cw = new ContextWrapper(mainContext);
            File directory = cw.getDir("tmp", Context.MODE_PRIVATE);
            File file = new File(directory, fileName);
            Log.d("SE464", "message temp file: "+file.getAbsolutePath());

            //Save to another file
            try {
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream (file.getAbsolutePath(), false);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(msg);
                os.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent sendMsgIntent = new Intent(mainContext, WifiDirectService.class);
            sendMsgIntent.setAction(WifiDirectService.SEND_MESSAGE_ACTION);
            sendMsgIntent.putExtra(WifiDirectService.EXTRA_MESSAGE, file.getAbsolutePath());
            mainContext.startService(sendMsgIntent);
        }
    }

    @Override
    public void receive(EncryptedMessage msg) {
        send(msg);
    }
}
