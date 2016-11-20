package com.star.patrick.wumbo;

import java.util.List;
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
