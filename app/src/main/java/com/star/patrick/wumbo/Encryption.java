package com.star.patrick.wumbo;

import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Kevin Xiao on 2016-11-20.
 */

public class Encryption {
    public static KeyPair generateKeyPair() {
        KeyPair userKeys = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            Log.d("SE464", "Start generating user key pair");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(Calendar.getInstance().getTimeInMillis());
            kpg.initialize(2048, sr);
            userKeys = kpg.generateKeyPair();
            if(userKeys != null) {
                Log.d("SE464", "Generated user key pair");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return userKeys;
    }

    public static SecretKey generateSecretKey() {
        SecretKey key = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(Calendar.getInstance().getTimeInMillis());
            kgen.init(128, sr);
            key = kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static String getEncodedPrivateKey(PrivateKey key) {
        String privkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = fact.getKeySpec(key, PKCS8EncodedKeySpec.class);
            byte[] packed = spec.getEncoded();
            privkey = Base64.encodeToString(packed, Base64.DEFAULT);
            Log.d("SE464", "Private key: " + privkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return privkey;
    }

    public static String getEncodedPublicKey(PublicKey key) {
        String publkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = fact.getKeySpec(key, X509EncodedKeySpec.class);
            publkey = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
            Log.d("SE464", "Public key: " + publkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return publkey;
    }

    public static PrivateKey getPrivateKeyFromEncoding(String keyEncoding) {
        PrivateKey key = null;
        try {
            byte[] privkey = Base64.decode(keyEncoding, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privkey);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            key = fact.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static PublicKey getPublicKeyFromEncoding(String keyEncoding) {
        PublicKey key = null;
        try {
            byte[] publkey = Base64.decode(keyEncoding, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publkey);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            key = fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static String getEncodedSecretKey(SecretKey key) {
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    public static SecretKey getSecretKeyFromEncoding(String keyEncoding) {
        byte[] encodedKey = Base64.decode(keyEncoding, Base64.DEFAULT);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }
}
