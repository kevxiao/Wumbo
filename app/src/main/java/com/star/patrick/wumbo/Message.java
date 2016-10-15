package com.star.patrick.wumbo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public class Message implements Serializable {
    private String text;
    private Sender sender;
    private Timestamp sendTime;
    private Timestamp recieveTime;
    private Channel channel;
    private UUID id;

    public Message(String text, Sender sender, Timestamp sendTime, Channel channel) {
        this.text = text;
        this.sender = sender;
        this.sendTime = sendTime;
        this.channel = channel;
        this.id = UUID.randomUUID();
    }
}
