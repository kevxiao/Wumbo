package com.star.patrick.wumbo.message;

import android.content.Context;
import android.net.Uri;

import com.star.patrick.wumbo.Sender;

import java.io.Serializable;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public class Message implements Serializable {
    private UUID id;
    private MessageContent content;
    private Sender sender;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    public Message(EncryptedMessage message, SecretKey secretKey) {
        this.id = message.getId();
        this.content = message.getContent(secretKey);
        this.sender = message.getSender();
        this.sendTime = message.getSendTime();
        this.receiveTime = message.getReceiveTime();
        this.channelId = message.getChannelId();
    }

    public Message(String text, Sender sender, Timestamp sendTime, UUID channelId) {
        this.content = new Text(text);
        this.sender = sender;
        this.sendTime = sendTime;
        this.channelId = channelId;
        this.id = UUID.randomUUID();
    }

    public Message(UUID id, String text, Sender sender, Timestamp sendTime, UUID channelId) {
        this(text, sender, sendTime, channelId);
        this.id = id;
    }

    public Message(Uri path, Context context, Sender sender, Timestamp sendTime, UUID channelId) {
        this.sender = sender;
        this.sendTime = sendTime;
        this.channelId = channelId;
        this.id = UUID.randomUUID();
        this.content = new Image(path, context, id);
    }

    public Message(UUID id, Uri path, Context context, Sender sender, Timestamp sendTime, UUID channelId) {
        this(path, context, sender, sendTime, channelId);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public MessageContent getContent() {
        return content;
    }

    public void handleContentOnReceive(Context c){
        switch(this.content.getType()){
            case IMAGE:
                Image temp = (Image)(this.content);
                temp.saveBitmap(c);
                temp.deleteBitmap();
        }

    }

    public String getText() {
        return "Use get content instead";
    }

    public Sender getSender() {
        return sender;
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
