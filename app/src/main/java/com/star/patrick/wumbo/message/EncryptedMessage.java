package com.star.patrick.wumbo.message;

import com.star.patrick.wumbo.User;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class EncryptedMessage implements Serializable {
    private UUID id;
    private SealedObject content;
    private User user;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    public EncryptedMessage(Message message, SecretKey secretKey) {
        this.content = null;
        try {
            Cipher encrypt = Cipher.getInstance("AES");
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey);
            this.content = new SealedObject(message.getContent().getType() == MessageContent.MessageType.TEXT ? (Text) message.getContent() : (Image) message.getContent(), encrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
        this.user = message.getUser();
        this.sendTime = message.getSendTime();
        this.channelId = message.getChannelId();
        this.id = message.getId();
    }

    public UUID getId() {
        return id;
    }

    public MessageContent getContent(SecretKey secretKey) {
        MessageContent msgContent = null;
        try {
            msgContent = (MessageContent) this.content.getObject(secretKey);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return msgContent;
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
}
