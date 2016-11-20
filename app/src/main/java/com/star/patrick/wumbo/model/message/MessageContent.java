package com.star.patrick.wumbo.model.message;

/**
 * Created by jesse on 16/11/16.
 */

public interface MessageContent{
    Object getMessageContent();
    MessageType getType();
    enum MessageType{
        TEXT,
        IMAGE,
        CHANNEL_INVITE
    }
}
