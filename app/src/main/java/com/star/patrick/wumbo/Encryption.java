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
 * Encryption class for static encryption functionality
 */
public class Encryption {

    /**
     * Generate a key pair for RSA encryption
     * @return Generated key pair, null on failure
     */
    public static KeyPair generateKeyPair() {
        KeyPair userKeys = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA"); // use RSA algorithm
            Log.d("SE464", "Start generating user key pair");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");     // use SHA1PRNG for random
            sr.setSeed(Calendar.getInstance().getTimeInMillis());       // use current time as seed
            kpg.initialize(2048, sr);                                   // initialize for 2048 bits
            userKeys = kpg.generateKeyPair();
            if(userKeys != null) {
                Log.d("SE464", "Generated user key pair");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return userKeys;
    }

    /**
     * Generate a key for AES encryption
     * @return Generated secret key, null on failure
     */
    public static SecretKey generateSecretKey() {
        SecretKey key = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");        // use AES algorithm
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");     // use SHA1PRNG for random
            sr.setSeed(Calendar.getInstance().getTimeInMillis());       // use current time as seed
            kgen.init(128, sr);                                         // initialize for 128 bits
            key = kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Get a base64 encoded string for a private key
     * @param key Private key to be encoded
     * @return Base64 string encoding of the private key, empty string on failure
     */
    public static String getEncodedPrivateKey(PrivateKey key) {
        String privkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            // use PKCS6 encoding to get key specs for the private key
            PKCS8EncodedKeySpec spec = fact.getKeySpec(key, PKCS8EncodedKeySpec.class);
            privkey = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);     // base64 encode
            Log.d("SE464", "Private key: " + privkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return privkey;
    }

    /**
     * Get a base64 encoded string for a public key
     * @param key Public key to be encoded
     * @return Base64 string encoding of the public key, empty string on failure
     */
    public static String getEncodedPublicKey(PublicKey key) {
        String publkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            // use X509 encoding to get key specs for the public key
            X509EncodedKeySpec spec = fact.getKeySpec(key, X509EncodedKeySpec.class);
            publkey = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);     // base64 encode
            Log.d("SE464", "Public key: " + publkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return publkey;
    }

    /**
     * Get a private key object from a base64 string encoding
     * @param keyEncoding String representation of base64 encoded private key
     * @return Private key object retrieved from the base64 string encoding, null on failure
     */
    public static PrivateKey getPrivateKeyFromEncoding(String keyEncoding) {
        PrivateKey key = null;
        try {
            byte[] privkey = Base64.decode(keyEncoding, Base64.DEFAULT);        // base64 decode
            // use PKCS8 encoding to get key specs for the private key from the byte array
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privkey);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            key = fact.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Get a public key object from a base64 string encoding
     * @param keyEncoding String representation of base64 encoded public key
     * @return Public key object retrieved from the base64 string encoding, null on failure
     */
    public static PublicKey getPublicKeyFromEncoding(String keyEncoding) {
        PublicKey key = null;
        try {
            byte[] publkey = Base64.decode(keyEncoding, Base64.DEFAULT);        // base64 decode
            // use X509 encoding to get key specs for the public key from the byte array
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publkey);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            key = fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Get a base64 encoded string for a secret key
     * @param key Secret key to be encoded
     * @return Base64 string encoding of the secret key
     */
    public static String getEncodedSecretKey(SecretKey key) {
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    /**
     * Get a secret key object from a base64 string encoding
     * @param keyEncoding String representation of base64 encoded secret key
     * @return Secret key object retrieved from the base64 string encoding
     */
    public static SecretKey getSecretKeyFromEncoding(String keyEncoding) {
        byte[] encodedKey = Base64.decode(keyEncoding, Base64.DEFAULT);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }
}
