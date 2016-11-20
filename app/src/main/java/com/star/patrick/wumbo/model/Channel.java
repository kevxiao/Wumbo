package com.star.patrick.wumbo.model;

import android.net.Uri;

import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.model.message.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

public interface Channel {
    void send(User sender, String msgText);
    void send(User sender, Uri imagePath);
    void receive(EncryptedMessage msg);
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    UUID getId();
    String getName();
    String getKey();
}
