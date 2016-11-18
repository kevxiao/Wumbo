package com.star.patrick.wumbo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public class Message implements Serializable {
    private UUID id;
    private String text;
    private User user;
    private Timestamp sendTime;
    private Timestamp receiveTime;
    private UUID channelId;

    public Message(String text, User user, Timestamp sendTime, UUID channelId) {
        this.text = text;
        this.user = user;
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
