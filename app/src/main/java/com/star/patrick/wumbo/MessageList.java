package com.star.patrick.wumbo;

import java.sql.Timestamp;
import java.util.List;

public interface MessageList {
    List<Message> getAllMessagesSince(Timestamp ts);
    void addMessage(Message msg);
}