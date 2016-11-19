package com.star.patrick.wumbo.message;

import com.star.patrick.wumbo.Sender;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MessageListImpl implements MessageList {
    private List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> getAllMessages() {
        return messages;
    }

    @Override
    public List<Message> getAllMessagesSince(Timestamp ts) {
        Comparator<Message> receiveTimeComparator = new Comparator<Message>() {
            @Override
            public int compare(Message msg1, Message msg2) {
                return msg1.getReceiveTime().compareTo(msg2.getReceiveTime());
            }
        };
        Message key = new Message("", null, null, null);
        key.setReceiveTime(ts);
        int index = Collections.binarySearch(messages, key, receiveTimeComparator);

        if (index >= messages.size()) {
            return new ArrayList<>();
        }

        while ( index < messages.size() &&
                index > 1 &&
                messages.get(index).getReceiveTime().equals(messages.get(index - 1).getReceiveTime())) {
            index--;
        }

        return messages.subList(index, messages.size());
    }

    @Override
    public void addMessage(Message msg) {
        messages.add(msg);

    }
}
