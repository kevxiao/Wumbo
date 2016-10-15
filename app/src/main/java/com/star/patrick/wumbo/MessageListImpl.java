package com.star.patrick.wumbo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageListImpl implements MessageList {
    List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> getAllMessages() {
        return messages;
    }

    @Override
    public List<Message> getAllMessagesSince(Timestamp ts) {
        return null;
    }

    @Override
    public void addMessage(Message msg) {
        messages.add(msg);
    }
}
