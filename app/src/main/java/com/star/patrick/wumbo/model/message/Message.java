package com.star.patrick.wumbo.model.message;

import com.star.patrick.wumbo.model.User;

import java.io.Serializable;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * Message class to store the message content and metadata
 */
public class Message implements Serializable {
    private UUID id;
    private MessageContent content;
    private User user;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    /**
     * Constructor for a message using the encrypted message and a channel's secret key
     * @param message The encrypted message to be decrypted
     * @param secretKey The secret key to be used to decrypt the message contents
     */
    public Message(EncryptedMessage message, SecretKey secretKey) {
        // decrypt message with secret key and construct message using the content and metadata
        this(message.getContent(secretKey), message.getUser(), message.getSendTime(), message.getChannelId());
        this.id = message.getId();
    }

    /**
     * Constructor for a message using the encrypted message and a user's private key
     * @param message The encrypted message to be decrypted
     * @param privateKey The private key to be used to decrypt the message contents
     */
    public Message(EncryptedMessage message, PrivateKey privateKey) {
        // decrypt message with private key and construct message using the content and metadata
        this(message.getContent(privateKey), message.getUser(), message.getSendTime(), message.getChannelId());
        this.id = message.getId();
    }

    /**
     * Constructor for a message using message contents and metadata
     * @param content Message content for the message
     * @param user  User associated with message (sender for channel messages, receiver for invites)
     * @param sendTime Metadata for the send time of the message
     * @param channelId The ID for the intended channel
     */
    public Message(MessageContent content, User user, Timestamp sendTime, UUID channelId) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.user = user;
        this.sendTime = sendTime;
        this.channelId = channelId;
    }

    /**
     * Construct message using a provided id and receive time
     * @param id Message ID
     * @param content Message content for the message
     * @param user  User associated with message (sender for channel messages, receiver for invites)
     * @param sendTime Metadata for the send time of the message
     * @param channelId The ID for the intended channel
     * @param receiveTime The receive time for the message (only used locally)
     */
    public Message(UUID id, MessageContent content, User user, Timestamp sendTime, UUID channelId, Timestamp receiveTime) {
        this(content, user, sendTime, channelId);
        this.id = id;
        this.receiveTime = receiveTime;
    }

    /**
     * Construct message using a provided id
     * @param id Message ID
     * @param content Message content for the message
     * @param user  User associated with message (sender for channel messages, receiver for invites)
     * @param sendTime Metadata for the send time of the message
     * @param channelId The ID for the intended channel
     */
    public Message(UUID id, MessageContent content, User user, Timestamp sendTime, UUID channelId) {
        this(content, user, sendTime, channelId);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public MessageContent getContent() {
        return content;
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
