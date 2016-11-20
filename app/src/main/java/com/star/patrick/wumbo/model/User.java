package com.star.patrick.wumbo.model;

import android.os.Parcelable;
import android.util.Base64;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

public class User implements Serializable {
    private String displayName;
    private UUID id;
    private PublicKey publicKey;

    public User(String displayName, PublicKey p) {
        this(UUID.randomUUID(), displayName, p);
    }

    public User(UUID id, String displayName, String publicKey) {
        this.id = id;
        this.displayName = displayName;

        try {
            byte[] publkey = Base64.decode(publicKey, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publkey);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            this.publicKey = fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public User(UUID id, String displayName, PublicKey p) {
        this.id = id;
        this.displayName = displayName;
        this.publicKey = p;
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

    @Override
    public String toString() {
        return displayName;
    }
}
