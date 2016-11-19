package com.star.patrick.wumbo;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.UUID;

public class User implements Serializable {
    private String displayName;
    private UUID id;
    private PublicKey publicKey;

    public User(String displayName, PublicKey p) {
        this(UUID.randomUUID(), displayName, p);
    }

    public User(UUID id, String displayName, PublicKey p) {
        this.id = id;
        this.displayName = displayName;
        this.publicKey = p;
    }

    public User(UUID id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getId() {
        return id;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
