package com.star.patrick.wumbo.model.message;

import java.sql.Timestamp;
import java.util.List;

public interface MessageList {
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    void addMessage(Message msg);
}
