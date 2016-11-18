package com.star.patrick.wumbo;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by giliam on 10/15/2016.
 */

public class Sender implements Serializable {
    private String displayName;
    private UUID id;

    public Sender(String displayName) {
        this.id = UUID.randomUUID();
        this.displayName = displayName;
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
}
