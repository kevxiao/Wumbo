package com.star.patrick.wumbo.model.message;

import java.io.Serializable;

/**
 * Interface for the message content of a message
 */
public interface MessageContent extends Serializable{
    Object getMessageContent();
    MessageType getType();
    enum MessageType{
        TEXT,
        IMAGE,
        CHANNEL_INVITE
    }
}
