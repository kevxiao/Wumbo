package com.star.patrick.wumbo;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.UUID;

public class Sender implements Serializable {
    private String displayName;
    private UUID id;
    private PublicKey publicKey;

    public Sender(String displayName, PublicKey p) {
        this.id = UUID.randomUUID();
        this.displayName = displayName;
        this.publicKey = p;
    }

    public Sender(UUID id, String displayName) {
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
