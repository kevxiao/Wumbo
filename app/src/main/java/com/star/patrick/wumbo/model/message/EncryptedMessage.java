package com.star.patrick.wumbo.model.message;

import com.star.patrick.wumbo.Encryption;
import com.star.patrick.wumbo.model.User;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * An encrypted message object used for message transfers
 */
public class EncryptedMessage implements Serializable {
    private MessageContent.MessageType type;
    private UUID id;
    private SealedObject content;
    private User user;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;
    /**
     * An AES key encrypted with a public key for a user message.
     * Note: This member is unused for a channel message.
     */
    private byte[] encryptedAES;

    /**
     * Constructor for an encrypted channel message using an unencrypted message
     * @param message The unencrypted message with the message contents to be transferred
     * @param secretKey The channel secret key that is used for encrypting an decrypting messages
     *                  for that channel
     */
    public EncryptedMessage(Message message, SecretKey secretKey) {
        this.initialize(message, secretKey);
    }

    /**
     * Constructor for an encrypted user message using an unencrypted message
     * @param message The unencrypted message with the message contents to be transferred
     * @param publicKey The designated user's public key used for encrypting messages to that user
     */
    public EncryptedMessage(Message message, PublicKey publicKey) {
        SecretKey key = Encryption.generateSecretKey();     // generate a secret key for encryption
        this.initialize(message, key);      // initialize message using the generated secret key
        // encrypt the secret key with the provided public key
        try {
            Cipher encrypt = Cipher.getInstance("RSA");     // use asymmetric RSA algorithm
            encrypt.init(Cipher.ENCRYPT_MODE, publicKey);   // initialize cipher for encryption
            this.encryptedAES = encrypt.doFinal(key.getEncoded());  // store encrypted key
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize encrypted message with metadata from the unencrypted message and encrypt the
     * message content with the provided secret key
     * @param message The unencrypted message with the message contents to be transferred
     * @param secretKey The secret key used to encrypt the message
     */
    private void initialize(Message message, SecretKey secretKey) {
        this.content = null;
        // encrypt the message contents with the provided secret key
        try {
            // use AES encryption with CBC hash and PKCS5 padding for key
            Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey);   // initialize cipher for encryption
            this.content = new SealedObject(message.getContent(), encrypt); // store encrypted content
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
        this.user = message.getUser();
        this.sendTime = message.getSendTime();
        this.channelId = message.getChannelId();
        this.id = message.getId();
        this.type = message.getContent().getType();
        this.encryptedAES = null;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Get the unencrypted message content for an AES encrypted message
     * @param secretKey Secret key for decryption
     * @return Unencrypted message contents
     */
    public MessageContent getContent(SecretKey secretKey) {
        MessageContent msgContent = null;
        try {
            msgContent = (MessageContent) this.content.getObject(secretKey);    // decrypt content
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return msgContent;
    }

    /**
     * Get the unencrypted message content for an RSA encrypted message
     * @param privateKey Private key for decryption
     * @return Unencrypted message contents
     */
    public MessageContent getContent(PrivateKey privateKey) {
        byte[] decodedAES = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");    // use RSA algorithm
            cipher.init(Cipher.DECRYPT_MODE, privateKey);       // initialize cipher for decryption
            decodedAES = cipher.doFinal(encryptedAES);          // decrypt AES key
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // decrypt message content with the decoded AES key
        return decodedAES != null ? getContent(new SecretKeySpec(decodedAES, 0, decodedAES.length, "AES")) : null;
    }

    public User getUser() {
        return user;
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public Timestamp getReceiveTime() {
        return receiveTime;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public void setReceiveTime(Timestamp receiveTime) {
        this.receiveTime = receiveTime;
    }

    public MessageContent.MessageType getContentType() {
        return type;
    }
}
