package com.star.patrick.wumbo.model;

import android.net.Uri;

import com.star.patrick.wumbo.MessageReceiver;
import com.star.patrick.wumbo.model.message.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

/**
 * Interface that represents a Channel and all interactions/responsibilities:
 * - Sending messages from the channel (image and text)
 * - Can receive messages to the channel
 * - Observable interface so that Observers may subscribe/unsubscribe and get notified
 * - Getters for channel members: ID, Name, Encryption key, messages in channel
 */
public interface Channel extends MessageReceiver {
    void send(User sender, String msgText);
    void send(User sender, Uri imagePath);
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    UUID getId();
    String getName();
    String getKey();
}
