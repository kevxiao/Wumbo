package com.star.patrick.wumbo.model.message;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation for the message list
 */
public class MessageListImpl implements MessageList {
    private List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Get a list of messages that are more recent than the specified time
     * @param ts Timestamp for the specified time
     * @return List of messages that match the time criteria
     */
    @Override
    public List<Message> getAllMessagesSince(Timestamp ts) {
        // comparator for comparing the timestamps of the messages
        Comparator<Message> receiveTimeComparator = new Comparator<Message>() {
            @Override
            public int compare(Message msg1, Message msg2) {
                return msg1.getReceiveTime().compareTo(msg2.getReceiveTime());
            }
        };
        Message key = new Message(null, null, null, null, null, ts);

        // find messages using the comparator
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
