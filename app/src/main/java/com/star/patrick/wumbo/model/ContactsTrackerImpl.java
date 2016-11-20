package com.star.patrick.wumbo.model;

import android.content.Context;

import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

/**
 * Created by giliam on 11/20/2016.
 */

public class ContactsTrackerImpl extends Observable implements ContactsTracker {
    private Map<UUID,User> contacts = new LinkedHashMap<>();
    private Context context;

    public ContactsTrackerImpl(Context context) {
        this.context = context;

        DatabaseHandler db = new DatabaseHandler(context, null);
        List<User> users = db.getUsers();

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User user, User t1) {
                return user.getDisplayName().compareTo(t1.getDisplayName());
            }
        });

        for (User user : users) {
            contacts.put(user.getId(), user);
        }
    }

    @Override
    public void receive(EncryptedMessage msg) {
        DatabaseHandler db = new DatabaseHandler(context, null);
        User user = msg.getUser();
        db.addUser(user);
        db.updateSenderDisplayName(user.getId(), user.getDisplayName());
        contacts.put(user.getId(), user);
        setChanged();
        notifyObservers();
    }

    @Override
    public Map<UUID, User> getContacts() {
        return contacts;
    }
}
