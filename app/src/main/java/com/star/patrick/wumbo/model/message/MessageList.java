package com.star.patrick.wumbo.model.message;

import java.sql.Timestamp;
import java.util.List;

/**
 * Interface for the list of messages
 */
public interface MessageList {
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    void addMessage(Message msg);
}
