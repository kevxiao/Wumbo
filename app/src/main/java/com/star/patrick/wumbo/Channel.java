package com.star.patrick.wumbo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

public interface Channel {
    void send(String msgText);
    void addObserver(Observer obs);
    List<Message> getAllMessages();
    List<Message> getAllMessagesSince(Timestamp ts);
    UUID getId();
    void receive(Message msg);
}
