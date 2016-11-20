package com.star.patrick.wumbo.model;

import com.star.patrick.wumbo.MessageReceiver;

import java.util.Map;
import java.util.Observer;
import java.util.UUID;

/**
 * Created by giliam on 11/20/2016.
 */

public interface ContactsTracker extends MessageReceiver {
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
    Map<UUID,User> getContacts();
}
