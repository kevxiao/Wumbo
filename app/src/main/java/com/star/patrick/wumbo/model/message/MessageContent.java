package com.star.patrick.wumbo.model.message;

import java.io.Serializable;

/**
 * Created by jesse on 16/11/16.
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
