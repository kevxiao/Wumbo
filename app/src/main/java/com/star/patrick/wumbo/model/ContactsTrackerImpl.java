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
 * Implementation that adds any user that has a message pass by the user to the list of contacts
 */
public class ContactsTrackerImpl extends Observable implements ContactsTracker {
    private Map<UUID,User> contacts = new LinkedHashMap<>();
    private Context context;

    /**
     * Create adding a list of contacts from the database
     */
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

    /**
     * Strips the user information from a message received, and adds it to the contacts if it was
     * not there before
     * If already there but the display name is different, will update the display name
     */
    @Override
    public void receive(EncryptedMessage msg) {
        User user = msg.getUser();
        if (contacts.containsKey(user.getId())) {
            //If the contact exists, check if name is different
            if (!contacts.get(user.getId()).getDisplayName().equals(user.getDisplayName())) {
                //update display name in database
                DatabaseHandler db = new DatabaseHandler(context, null);
                db.updateUserDisplayName(user.getId(), user.getDisplayName());
                setChanged();
            }
        } else {
            //Otherwise if it was unknown, add the new known user to the database
            DatabaseHandler db = new DatabaseHandler(context, null);
            db.addUser(user);
            setChanged();
        }
        //Add to map of known users
        contacts.put(user.getId(), user);
        notifyObservers();
    }

    @Override
    public Map<UUID, User> getContacts() {
        return contacts;
    }
}
