package com.star.patrick.wumbo.model;

import com.star.patrick.wumbo.MessageReceiver;

import java.util.Map;
import java.util.Observer;
import java.util.UUID;

/**
 * Interface for keeping a list of the users contacts
 */
public interface ContactsTracker extends MessageReceiver {
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
    Map<UUID,User> getContacts();
}
