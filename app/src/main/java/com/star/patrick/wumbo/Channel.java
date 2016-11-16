package com.star.patrick.wumbo;

import android.content.Context;
import android.net.Uri;

import com.star.patrick.wumbo.message.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

public interface Channel {
    void send(String msgText);
    void send(Uri imagePath, Context context);
    void addObserver(Observer obs);
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    UUID getId();
    void receive(Message msg);
}
