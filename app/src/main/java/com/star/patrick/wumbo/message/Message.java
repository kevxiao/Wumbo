package com.star.patrick.wumbo.message;

import com.star.patrick.wumbo.Sender;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public class Message implements Serializable {
    private UUID id;
    private String text;
    private Sender sender;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    public Message(String text, Sender sender, Timestamp sendTime, UUID channelId) {
        this.text = text;
        this.sender = sender;
        this.sendTime = sendTime;
        this.channelId = channelId;
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
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
