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
        User user = msg.getUser();
        if (contacts.containsKey(user.getId())) {
            if (!contacts.get(user.getId()).getDisplayName().equals(user.getDisplayName())) {
                //update name
                DatabaseHandler db = new DatabaseHandler(context, null);
                db.updateSenderDisplayName(user.getId(), user.getDisplayName());
                setChanged();
            }
        } else {
            DatabaseHandler db = new DatabaseHandler(context, null);
            db.addUser(user);
            setChanged();
        }
        contacts.put(user.getId(), user);
        notifyObservers();
    }

    @Override
    public Map<UUID, User> getContacts() {
        return contacts;
    }
}
