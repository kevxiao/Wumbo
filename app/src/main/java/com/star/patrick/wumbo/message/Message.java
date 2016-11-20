package com.star.patrick.wumbo.message;

import android.content.Context;
import android.net.Uri;

import com.star.patrick.wumbo.User;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public class Message implements Serializable {
    private UUID id;
    private MessageContent content;
    private User user;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    public Message(EncryptedMessage message, SecretKey secretKey) {
        this(message.getContent(secretKey), message.getUser(), message.getSendTime(), message.getChannelId());
        this.id = message.getId();
    }

    public Message(MessageContent content, User user, Timestamp sendTime, UUID channelId) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.user = user;
        this.sendTime = sendTime;
        this.channelId = channelId;
    }

    public Message(UUID id, MessageContent content, User user, Timestamp sendTime, UUID channelId, Timestamp receiveTime) {
        this(content, user, sendTime, channelId);
        this.id = id;
        this.receiveTime = receiveTime;
    }

    public Message(UUID id, MessageContent content, User user, Timestamp sendTime, UUID channelId) {
        this(content, user, sendTime, channelId);
        this.id = id;
    }

    //don't keep
//    public Message(String text, User user, Timestamp sendTime, UUID channelId) {
//        this(user, sendTime, channelId);
//        this.content = new Text(text);
//        this.id = UUID.randomUUID();
//    }
//
//    public Message(UUID id, String text, User user, Timestamp sendTime, UUID channelId) {
//        this(text, user, sendTime, channelId);
//        this.id = id;
//    }
//
//    public Message(Uri path, User user, Timestamp sendTime, UUID channelId) {
//        this(user, sendTime, channelId);
//        this.content = new Image(path);
//        this.id = UUID.randomUUID();
//    }
//
//    public Message(UUID id, Uri path, User user, Timestamp sendTime, UUID channelId) {
//        this(path, user, sendTime, channelId);
//        this.id = id;
//    }
//
//    public Message(UUID id, byte[] image, User user, Timestamp sendTime, UUID channelId) {
//        this(user, sendTime, channelId);
//        this.id = id;
//        this.content = new TransferImage(image);
//    }

    public UUID getId() {
        return id;
    }

    public MessageContent getContent() {
        return content;
    }

    public String getText() {
        return "Use get content instead";
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
